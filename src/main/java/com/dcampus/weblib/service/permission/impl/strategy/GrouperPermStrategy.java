package com.dcampus.weblib.service.permission.impl.strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dcampus.weblib.dao.CategoryDao;
import com.dcampus.weblib.dao.GroupDao;
import com.dcampus.weblib.dao.MemberDao;
//import com.dcampus.grouper.rmi.IGrouperAPI;
import com.dcampus.weblib.entity.Admin;
import com.dcampus.weblib.entity.Category;
import com.dcampus.weblib.entity.DomainManager;
import com.dcampus.weblib.entity.Group;
import com.dcampus.weblib.entity.GroupManager;
import com.dcampus.weblib.entity.GroupMemberBinding;
import com.dcampus.weblib.entity.GroupResource;
import com.dcampus.weblib.entity.GroupResourceReceive;
import com.dcampus.weblib.entity.Member;
import com.dcampus.weblib.entity.MemberRole;
import com.dcampus.weblib.entity.OldPerm;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.common.config.PropertyUtil;
import com.dcampus.common.generic.GenericDao;
import com.dcampus.weblib.service.GrouperService;
import com.dcampus.weblib.service.permission.PermCollection;
import com.dcampus.weblib.service.permission.IPermission.CategoryPerm;
import com.dcampus.weblib.service.permission.IPermission.GlobalPerm;
import com.dcampus.weblib.service.permission.IPermission.GroupPerm;
import com.dcampus.weblib.service.permission.impl.IPermStrategy;
import com.dcampus.weblib.service.permission.impl.PermProperty;
import com.dcampus.weblib.service.permission.impl.PermUtil;
import com.dcampus.weblib.util.userutil.UserRemoteServiceUtil;
import com.dcampus.weblib.util.userutil.models.RemoteItems;



/**
 * 与grouper进行绑定的权限策略<br>
 * 判断用户权限转换为：<br>
 * 1、判断用户是否有独立配置的权限，有则返回用户独立配置的权限<br>
 * 2、若用户没有独立配置的权限，则查看用户所在的用户组有无配置的权限，有则去并集并返回<br>
 * 3、用户和用户组都没有配置权限，则返回通用权限
 * 
 * Patrick修改
 * 去掉了所有跟讨论区forum有关的操作
 *
 * @author zim
 *
 */
@Component 
public class GrouperPermStrategy implements IPermStrategy {

	
	@Autowired
	private CategoryDao categoryDao;
	
	@Autowired
	private MemberDao memberDao;
	
	@Autowired
	private GroupDao groupDao;
	
	@Autowired 
	private GenericDao genericDao;

	public GlobalPerm[] getGlobalPerm(long memberId) throws GroupsException {
		OldPerm permBean = null;

		// 如果用户未登陆
		if (memberId == OldPerm.GLOBAL_NONMEMBER_ID) {
			// 查看是否有设置游客的全局权限
			permBean = getPerm(memberId, OldPerm.GLOBAL_TYPE_ID,
					OldPerm.PERM_TYPE_GLOBAL);

			if (permBean == null)
				return new GlobalPerm[0];
		} else {
			// 如果是管理员则拥有全局权限
			if (isAdmin(memberId))
				return GlobalPerm.all();

			// 不是管理员则查询相应的表项
			permBean = getPerm(memberId, OldPerm.GLOBAL_TYPE_ID,
					OldPerm.PERM_TYPE_GLOBAL);

			// 如果用户没有特殊权限，则查找所在组有无特殊权限
			if (permBean == null) {
				long premMask = 0;
				Member[] iMember = getTeamsOfMember(memberId);

				for (int i = 0; i < iMember.length; i++) {
					long teamId = iMember[i].getId();

					permBean = getPerm(teamId, OldPerm.GLOBAL_TYPE_ID,
							OldPerm.PERM_TYPE_GLOBAL);
					if (permBean != null) {
						premMask = premMask | permBean.getPermCode();
					}
				}

				if (permBean != null) {
					permBean.setPermCode(premMask);
				}
			}

			// 没有定制通用设置，取配置
			if (permBean == null)
				return PermProperty.getDefaultGlobalPerm();
		}
		// 转换成array
		List<GlobalPerm> list = new ArrayList<GlobalPerm>();
		for (GlobalPerm perm : GlobalPerm.all()) {
			if ((perm.getMask() & permBean.getPermCode()) != 0)
				list.add(perm);
		}
		return list.toArray(new GlobalPerm[list.size()]);
	}

	private void addCategoryInheritedPerm(List<OldPerm> permList, long memberId, long categoryId) {
		Category categoryBean = this.getCategory(categoryId);
		if (categoryBean != null) {
			long pId = categoryBean.getParentId();
			if (pId > 0) {
				Category parentCategoryBean = this.getCategory(pId);
				while(parentCategoryBean != null) {
					OldPerm bean = getPerm(memberId, parentCategoryBean.getId(), OldPerm.PERM_TYPE_CATEGORY);
					if (bean != null) {
						if (!bean.isInheritToChild()) {
							break;
						}
						bean.setInherited(true);
						permList.add(bean);
						if (bean.isOverrideParent()) {
							break;	
						}
					}
					parentCategoryBean = this.getCategory(parentCategoryBean.getParentId());
				}
			}
		}
	}
	
	public CategoryPerm[] getCategoryPerm(long memberId, long categoryId)
			throws GroupsException {
		OldPerm permBean = null;
		List<OldPerm> permList = new ArrayList<OldPerm>();
		// 用户未登录
		if (memberId == OldPerm.GLOBAL_NONMEMBER_ID) {
			// 查看是否有设置游客的分类权限
			permBean = getPerm(OldPerm.GLOBAL_NONMEMBER_ID, categoryId,
					OldPerm.PERM_TYPE_CATEGORY);

			if (permBean == null)
				return new CategoryPerm[0];
		} else {
			// 管理员拥有所有权限
			if (isAdmin(memberId))
				return CategoryPerm.all();

			// 检查所在用户组是否是管理员或者拥有圈主权限
			if (isAdminByTeam(memberId))
				return CategoryPerm.all();

			// 不是管理员则查询相应的表项
			permBean = getPerm(memberId, categoryId, OldPerm.PERM_TYPE_CATEGORY);
			
			//
			if (permBean == null) {
				addCategoryInheritedPerm(permList, memberId, categoryId);
			}

			// 查看是否有设置分类通用权限
			if (permBean == null && permList.isEmpty()) {
				permBean = getPerm(memberId, OldPerm.GLOBAL_TYPE_ID,
						OldPerm.PERM_TYPE_CATEGORY);
			}
			if (permBean != null) {
				permList.add(permBean);
			}
			// 如果用户没有特殊权限，则查找所在组有无特殊权限
			if (permBean == null) {
				//long premMask = 0;
				Member[] iMember = getTeamsOfMember(memberId);

				for (int i = 0; i < iMember.length; i++) {
					List<OldPerm> teamPermList = new ArrayList<OldPerm>();
					long teamId = iMember[i].getId();

					// 获取用户组的分类权限
					permBean = getPerm(teamId, categoryId,
							OldPerm.PERM_TYPE_CATEGORY);
					
					if (permBean == null) {
						addCategoryInheritedPerm(teamPermList, teamId, categoryId);
					} else {
						teamPermList.add(permBean);
					}

					// 若无设置，查看用户组在分类下的通用权限
					if (permBean == null && teamPermList.isEmpty()) {
						permBean = getPerm(teamId, OldPerm.GLOBAL_TYPE_ID,
								OldPerm.PERM_TYPE_CATEGORY);
					}
					permList.addAll(teamPermList);
					/*
					// 有权限则进行合并
					if (permBean != null) {
						premMask = premMask | permBean.getPermCode();
					}*/
				}
				
			}
			//判断所有人组的权限
			if (permBean == null && permList.isEmpty()) {
				Member member = getEveryOneTeam();
				if (member != null) {
					permBean = getPerm(member.getId(), categoryId,
							OldPerm.PERM_TYPE_CATEGORY);
				}
			}
			// 没有定制通用设置，取配置
			if (permBean == null && permList.isEmpty())
				return PermProperty.getDefaultCategoryPerm();
			
			
		}
		long permCode = 0;
		for (OldPerm bean : permList) {
			permCode = permCode | bean.getPermCode();
		}
		return PermUtil.convertCategoryPerm(permCode);
	}


	public GroupPerm[] getGroupPermIneritedCategory(long memberId, long categoryId, boolean inherited) throws GroupsException {
		List<Category> categoryList = categoryDao.tracedCategoryList(categoryId);
		long permCode = 0;
		boolean none = true;//标志是否没有授权
		//获得用户类型的分类权限
		List<OldPerm> permList = this.getPermBeanIneritedCategory(memberId, categoryId, inherited);
		//key=分类id  value=用户类型的权限信息
		Map<Long, OldPerm> userPermMap = new HashMap<Long, OldPerm>();
		if (!permList.isEmpty()) {
			for (OldPerm temp : permList) {
				userPermMap.put(temp.getTypeId(), temp);
			}
			none = false;
		}
		//key=分类id  value = 用户所在的用户组的权限值或操作的值
		Map<Long, Long> teamPermCodeMap = new HashMap<Long, Long>();
		List<Member> memList = new ArrayList<Member>();
		//获得用户所在的组
		Member[] iMember = getTeamsOfMember(memberId);
		memList.addAll(Arrays.asList(iMember));
		
		Member member = getEveryOneTeam();
		if (member != null) {
			memList.add(member);
		}
		
		for (Member im : memList) {
			//获得用户组类型的分类权限
			List<OldPerm> teamPermList = this.getPermBeanIneritedCategory(im.getId(), categoryId, inherited);
			if (!teamPermList.isEmpty()) {
				//把 相同的分类id 对应 的用户组权限值归在一起
				for (OldPerm temp : teamPermList) {
					long value = teamPermCodeMap.get(temp.getTypeId()) == null ?
							0 : teamPermCodeMap.get(temp.getTypeId());
					value = value | temp.getPermCode();
					teamPermCodeMap.put(temp.getTypeId(), value);
				}
				none = false;
			}
		}

		
		if (none) {
			return null;
		}
		
		for (Category category : categoryList) {
			long id = category.getId();
			//以用户授权为准
			if (userPermMap.containsKey(id)) {
				OldPerm permBean = userPermMap.get(id);
				permCode = permCode | permBean.getPermCode();
				if (permBean.isOverrideParent()) {
					break;
				}
			} else {
				if (teamPermCodeMap.containsKey(id)) {
					permCode = permCode | teamPermCodeMap.get(id);
				}
			}
		}
		return PermUtil.convertGroupPerm(permCode);
		
	}
	
	private List<OldPerm> getPermBeanIneritedCategory(long memberId, long categoryId, boolean inherited)
		throws GroupsException {
		List<OldPerm> permList = new ArrayList<OldPerm>();
		Category categoryBean = this.getCategory(categoryId);
		if (categoryBean == null) {
			return permList;
		}
		OldPerm permBean = getPerm(memberId, categoryId, OldPerm.PERM_TYPE_GROUP_OF_CATEGORY);
		if (permBean != null) {
			permList.add(permBean);
		}
		if (inherited) {
			long pId = categoryBean.getParentId();
			if (pId > 0) {
				Category parentCategoryBean = this.getCategory(pId);
				while(parentCategoryBean != null) {
					OldPerm bean = getPerm(memberId, parentCategoryBean.getId(), OldPerm.PERM_TYPE_GROUP_OF_CATEGORY);
					if (bean != null) {
						if (!bean.isInheritToChild()) {
							break;
						}
						//bean.setInherited(true);
						permList.add(bean);
						if (bean.isOverrideParent()) {
							break;	
						}
					}
					parentCategoryBean = this.getCategory(parentCategoryBean.getParentId());
				}
			}
		}
		return permList;
	}
	
	private GroupPerm[] getDefaultGroupMemberPerm(long groupId) throws GroupsException{
		Group group = getGroup(groupId);
		if (group == null) {
			return new GroupPerm[0];
		}
		if (group.getGroupUsage() == null) {
			return new GroupPerm[0];
		}
		if (group.getGroupUsage() == Group.USAGE_NORMAL) {
			return PermProperty.getDefaultGroupMemberPerm();
		}
		if (group.getGroupUsage() == Group.USAGE_GUESTBOOK) {
			return PermProperty.getMessageboardGroupMemberPerm();
		}
		if (group.getGroupUsage() == Group.USAGE_PRIVATE) {
			return PermProperty.getPrivateGroupMemberPerm();
		}
		if (group.getGroupUsage() == Group.USAGE_PUBLIC) {
			return PermProperty.getPublicGroupMemberPerm();
		}
		return new GroupPerm[0];
	}
	
	private GroupPerm[] getDefaultGroupNonmemberPerm(long groupId) throws GroupsException{
		Group group = getGroup(groupId);
		if (group == null) {
			return new GroupPerm[0];
		}
		if (group.getGroupUsage() == null) {
			return new GroupPerm[0];
		}
		if (group.getGroupUsage() == Group.USAGE_NORMAL) {
			return PermProperty.getDefaultGroupNonmemberPerm();
		}
		if (group.getGroupUsage() == Group.USAGE_GUESTBOOK) {
			return PermProperty.getMessageboardGroupNonmemberPerm();
		}
		if (group.getGroupUsage() == Group.USAGE_PRIVATE) {
			return PermProperty.getPrivateGroupNonmemberPerm();
		}
		if (group.getGroupUsage() == Group.USAGE_PUBLIC) {
			return PermProperty.getPublicGroupNonmemberPerm();
		}
		return new GroupPerm[0];
	}
	
	public GroupPerm[] getDefaultGroupPerm(long memberId, long groupId)
			throws GroupsException {
		
		// 检查用户是否是圈子的会员
		OldPerm permBean = null;
		if (isGroupMember(memberId, groupId)) {
			// 圈子会员
			permBean = getPerm(OldPerm.GLOBAL_MEMBER_ID, groupId,
					OldPerm.PERM_TYPE_GROUP);
		
			if (permBean == null)
				return getDefaultGroupMemberPerm(groupId);
		
		} else {
			// 非圈子会员按大众情况处理
			permBean = getPerm(OldPerm.GLOBAL_NONMEMBER_ID, groupId,
					OldPerm.PERM_TYPE_GROUP);
		
			if (permBean == null)
				return getDefaultGroupNonmemberPerm(groupId);
		}
		
		// 转换成array
		List<GroupPerm> list = new ArrayList<GroupPerm>();
		for (GroupPerm perm : GroupPerm.all()) {
			if ((perm.getMask() & permBean.getPermCode()) != 0
					&& canHasGroupPerm(memberId, perm)) {
				list.add(perm);
			}
		}
		
		return list.toArray(new GroupPerm[list.size()]);
	}
	
	public PermCollection getGroupPerm(long memberId, long groupId)
			throws GroupsException {
		PermCollection pc = new PermCollection();
		// 管理员或者圈主拥有所有权限
		if (isAdmin(memberId) || isGroupManager(memberId, groupId)) {
			pc.addInheritedPerm(GroupPerm.all());
			return pc;
		}

		// 检查所在用户组是否是管理员或者拥有圈主权限
		if (isAdminByTeam(memberId) || isGroupManagerByTeam(memberId, groupId)) {
			pc.addInheritedPerm(GroupPerm.all());
			return pc;
		}
		
		
		OldPerm permBean = getPerm(memberId, groupId, OldPerm.PERM_TYPE_GROUP);
//		如果本身
		if (permBean == null || !permBean.isOverrideParent()) {
//			获得从分类继承的权限
			//获得圈子详细信息
			Group group = getGroup(groupId);
			if (group != null) {
				//获得圈子所在分类的全部权限
				GroupPerm[] perms = this.getGroupPermIneritedCategory(memberId, group.getCategory().getId(), true);
				//如果有权限配置
				if (perms != null) {
					pc.addInheritedPerm(perms);
					//如果本身圈子没有权限配置，则返回继承的分类权限
					if (permBean == null) {
						return pc;
					} else {
						pc.addGroupSelfPerm(PermUtil.convertGroupPerm(permBean.getPermCode()));
						//本身圈子权限配置+继承的分类权限
						return pc;
					}
				}
			}
		} else {
			pc.addGroupSelfPerm(PermUtil.convertGroupPerm(permBean.getPermCode()));
			return pc;
		}
//		 检查用户是否是圈子的会员
		if (isGroupMember(memberId, groupId)) {
			// 圈子会员
			permBean = getPerm(memberId, groupId, OldPerm.PERM_TYPE_GROUP);

//			获得从分类继承的权限
			//获得圈子详细信息
			Group group = getGroup(groupId);
			if (group != null) {
				//获得圈子所在分类的全部权限
				GroupPerm[] perms = this.getGroupPermIneritedCategory(memberId, group.getCategory().getId(), true);
				//如果有权限配置
				if (perms != null) {
					pc.addInheritedPerm(perms);
					//如果本身圈子没有权限配置，则返回继承的分类权限
					if (permBean == null) {
						return pc;
					} else {
						
						pc.addGroupSelfPerm(PermUtil.convertGroupPerm(permBean.getPermCode()));
						//本身圈子权限配置+继承的分类权限
						return pc;
						
					}
				}
			}
			
			if (permBean == null) {
				permBean = getPerm(OldPerm.GLOBAL_MEMBER_ID, groupId,
						OldPerm.PERM_TYPE_GROUP);
			}

			if (permBean == null) {
				pc.addInheritedPerm(getDefaultGroupMemberPerm(groupId));
				return pc;
			}

		}
		// 检查用户所在组是否是圈子的会员
		else if (isGroupMemberByTeam(memberId, groupId)) {
			long permMask = 0;
			Member[] iMember = getTeamsOfMember(memberId);

			for (int i = 0; i < iMember.length; i++) {
				long teamId = iMember[i].getId();
				if (isGroupMember(teamId, groupId)) {
					permBean = getPerm(teamId, groupId, OldPerm.PERM_TYPE_GROUP);
					if (permBean == null) {
						permBean = getPerm(OldPerm.GLOBAL_MEMBER_ID, groupId,
								OldPerm.PERM_TYPE_GROUP);
						if (permBean != null) {
							permMask = permBean.getPermCode();	
						}
						
					}
					// 对用户组的权限进行叠加
					else {
						permMask = permMask | permBean.getPermCode();
					}
				}
			}
			if (permBean == null) {
				pc.addInheritedPerm(getDefaultGroupMemberPerm(groupId));
				return pc;
			}

			// 设置叠加后的权限
			permBean.setPermCode(permMask);
		} else {
//			判断所有人组的权限
			if (permBean == null) {
				Member member = getEveryOneTeam();
				if (member != null) {
					permBean = getPerm(member.getId(), groupId,
							OldPerm.PERM_TYPE_GROUP);
				}
			}
			if (permBean == null) {
			// 非圈子会员按大众情况处理
			permBean = getPerm(OldPerm.GLOBAL_NONMEMBER_ID, groupId,
					OldPerm.PERM_TYPE_GROUP);
			}
			if (permBean == null) {
				pc.addInheritedPerm(getDefaultGroupNonmemberPerm(groupId));
				return pc;
			}
		}
		pc.addGroupSelfPerm(PermUtil.convertGroupPerm(permBean.getPermCode()));
		return pc;
		
	}
	
	private Member getEveryOneTeam() {
		if (PropertyUtil.EVERYONE_TEAM_ID > 0) {
			return this.getMember(PropertyUtil.EVERYONE_TEAM_ID);
		}
		String[] defaultTeam =PropertyUtil.getSystemDefaultManageTeamName().split(";");
		String name = PropertyUtil.getSystemDefaultOrgName() + ":" + defaultTeam[1];
		/*WsGroup group = null;
		try {
			group = wsFindGroup.findGroupByName(name, true).getGroupResults()[0];
		} catch (Exception e) {			
		}*/
		//Group group = api.findGroupByName(name);
		RemoteItems group = UserRemoteServiceUtil.findGroupByName(name, true)[0];
		if (group != null) {
			Member member = mapping(group.getId(), Member.MEMBER_TYPE_TEAM);
			PropertyUtil.EVERYONE_TEAM_ID = member.getId();
			return member;
		}
		return null;
	}
	
	private Member[] getTeamsOfMember(long memberId)
			throws GroupsException {
		// grouper1.0
		// Group[] groups = api.getGroups(String.valueOf(memberId), null);
		// grouper2.0
		/*IMemberDao memberDao = CacheDao.getMemberDao();		
		WsGroup[] groups = null;
		try {
			groups = wsGetGroups.getGroups(
					memberDao.getMember(memberId).getAccount()).getResults()[0]
					.getWsGroups();
		} catch (Exception e) {
		}
		if (groups == null || groups[0] == null) {
			return new Member[0];
		}
		int len = groups.length;
		Member[] members = new Member[len];
		for (int i = 0; i < groups.length; i++) {
			WsGroup g = groups[i];
			members[i] = mapping(g.getUuid(), Member.Type.TEAM);
		}*/
		RemoteItems[] groups = UserRemoteServiceUtil.getGroupsOfUser(this.getMember(memberId).getAccount());
		int len = groups.length;
		Member[] members = new Member[len];
		for (int i = 0; i < groups.length; i++) {
			members[i] = mapping(groups[i].getId(), Member.MEMBER_TYPE_TEAM);
		}
		return members;
	}

	
	private Member mapping(String name, String type) {
		Member member = null;
//		try {
//			member = genericDao.getMember(name, name, type);
//		} catch (DataNotFoundException e) {
//			Member bean = new Member();
//			bean.setAccount(name);
//			bean.setName(name);
//			bean.setStatus(Member.Status.NORMAL);
//			bean.setType(type);
//			memberDao.create(bean);
//			member = bean;
//		}
//		return member;

		member = memberDao.getMemberByNameAccount(name, name, type);
		if (member == null) {
			Member bean = new Member();
			bean.setAccount(name);
			bean.setName(name);
			bean.setMemberStatus(Member.STATUS_NORMAL);
			bean.setMemberType(type);
			memberDao.saveOrUpdateMember(bean);
			member = bean;
		}
		return member;
		
	}


	
	/**
	 * 现在不存在游客这一说法，所以直接返回true
	 * @param memberId
	 * @param perm
	 * @return
	 */
	private boolean canHasGroupPerm(long memberId, GroupPerm perm) {
		return true;
	}
	

	public boolean isAdmin(long memberId) {
		boolean isRoleModuleEnable = PropertyUtil.getEnableRoleModule();
		if(isRoleModuleEnable){
			//如果启用了管理员角色模块，则使用新的管理员判断方法：
			//直接查询用户是否绑定了管理员角色
			//判断用户是否在‘管理员’组里，后Admin表里，如果是，则表明该用户有系统管理员角色
			
			String hql = "from "+MemberRole.class.getName()+" as mr where mr.member.id=?1";
			
			Query query = genericDao.createQuery(hql, new Object[]{memberId} );
			List results = query.getResultList();
			if(!results.isEmpty()){
				return true;
			}
		}
		boolean isDomainModuleEnable = PropertyUtil.getEnableDomainModule();
		if(isDomainModuleEnable) {
			//如果启用了多域模块，则需要判断该用户是否是域管理员
			String checkDomainAdmin = "from " + DomainManager.class.getName() + " as dm where dm.manager.id=?1";
			Query checkDomainAdminQuery = genericDao.createQuery(checkDomainAdmin, new Object[]{memberId});
			List<DomainManager> dms = checkDomainAdminQuery.getResultList();
			if (dms != null && !dms.isEmpty()) {
				return true;
			}
		}
		Admin a= genericDao.findFirst("from Admin a where a.member.id = ?1 ", memberId);
		if (a != null) {
			return true;
		}
		return isAdminByTeam(memberId);
	}

	public boolean isGroupManager(long memberId, long groupId) {
//		try {
//			CacheDao.getGroupManagerDao().getGroupManager(groupId, memberId);
//			return true;
//		} catch (DataNotFoundException e) {
//			return isGroupManagerByTeam(memberId, groupId);
//		}
		GroupManager gm = genericDao.findFirst("from GroupManager gm where gm.memberId = ?1 and gm.groupId = ?2 ", memberId, groupId);
		if (gm != null) {
			return true;
		}
		return isGroupManagerByTeam(memberId, groupId);
	}


	private boolean isGroupMember(long memberId, long groupId) {
//		try {
//			return CacheDao.getMemberDao().findMemberInGroup(memberId, groupId);
//		} catch (DataNotFoundException e) {
//			return false;
//		}
		GroupMemberBinding gmb = genericDao.findFirst("from GroupMemberBinding gmb where gmb.memberId = ?1 and gmb.groupId = ?2 ", memberId, groupId);
		if (gmb != null) {
			return true;
		}
		return false;
	}

	public OldPerm getPerm(long memberId, long typeId, int type) {
//		try {
//			return CacheDao.getPermDao().getPerm(memberId, typeId, type);
//		} catch (DataNotFoundException e) {
//			return null;
//		}
		OldPerm perm = genericDao.findFirst("from OldPerm p where p.memberId = ?1 and p.typeId = ?2 and p.permType = ?3", memberId, typeId, type);
		if (perm != null) {
			return perm;
		}
		return null;
	}

	public boolean isContainMemberByTeam(long memberId, long userMemberId) throws GroupsException {
		if (memberId == userMemberId) {
			return true;
		}
		Member member = getEveryOneTeam();
		if (memberId == member.getId().longValue()) {
			return true;
		}
		Member[] beans = getTeamsOfMember(userMemberId);
		for (Member m : beans) {
			if (memberId == m.getId().longValue()) {
				return true;
			}
		}
		return false;
	}

	private boolean isGroupManagerByTeam(long memberId, long groupId) {
		Member[] iMember = getTeamsOfMember(memberId);
		for (int i = 0; i < iMember.length; i++) {
			long teamId = iMember[i].getId();
			if (isGroupManager(teamId, groupId)) {
				return true;
			}
		}
		return false;
	}

	private boolean isGroupMemberByTeam(long memberId, long groupId) {
		Member[] iMember = getTeamsOfMember(memberId);
		for (int i = 0; i < iMember.length; i++) {
			long teamId = iMember[i].getId();
			if (isGroupMember(teamId, groupId)) {
				return true;
			}
		}
		return false;
	}

	private boolean isAdminByTeam(long memberId) {
		Member[] iMember = getTeamsOfMember(memberId);
		for (int i = 0; i < iMember.length; i++) {
			long teamId = iMember[i].getId();
			if (isAdmin(teamId)) {
				return true;
			}
		}
		return false;
	}


	public boolean isSuperAdmin(long memberId) throws GroupsException {
//		try {
//			Admin bean = CacheDao.getAdminDao().getAdmin(memberId);
//			return bean.getType() == Admin.Type.SUPER_ADMIN;
//		} catch (DataNotFoundException e) {
//			return false;
//		}
		Admin a= genericDao.get(Admin.class, memberId);
		if (a != null) {
			return a.getType() == Admin.SUPER_ADMIN;
		}
		return false;
	}
	
	private Category getCategory(long categoryId) {
		return categoryDao.getCategoryById(categoryId);
	}
	
	private Group getGroup(long groupId) {
		return groupDao.getGroupById(groupId);
	}
	
	private Member getMember(long memberId) {
		return memberDao.getMemberById(memberId);
	}

	public boolean hasCreatGroupPermission(long memberId) throws GroupsException {
//		OldPerm[] list = CacheDao.getPermDao().getMemberPerms(memberId, Type.CATEGORY);
//		for (OldPerm bean : list) {
//			CategoryPerm[] perms = PermUtil.convertCategoryPerm(bean.getPermCode());
//			if (PermUtil.containCategoryPermission(perms, CategoryPerm.CREATE_GROUP)) {
//				return true;
//			}
//		}
		
		Member member = this.getMember(memberId);
		if (member != null) {
			List<OldPerm> list = genericDao.findAll("from OldPerm p where p.memberId = ?1 and p.permType = ?2 ", memberId, OldPerm.PERM_TYPE_CATEGORY);
			for (OldPerm bean : list) {
				CategoryPerm[] perms = PermUtil.convertCategoryPerm(bean.getPermCode());
				if (PermUtil.containCategoryPermission(perms, CategoryPerm.CREATE_GROUP)) {
					return true;
				}
			}
		
	//		获得用户所在的组
			List<Member> memList = new ArrayList<Member>();
			Member[] iMember = getTeamsOfMember(memberId);
			memList.addAll(Arrays.asList(iMember));
			
			Member membertemp = getEveryOneTeam();
			if (membertemp != null) {
				memList.add(membertemp);
			}
			for (Member m : memList) {
				list = genericDao.findAll("from OldPerm p where p.memberId = ?1 and p.permType = ?2 ", m.getId(), OldPerm.PERM_TYPE_CATEGORY);
				for (OldPerm bean : list) {
					CategoryPerm[] perms = PermUtil.convertCategoryPerm(bean.getPermCode());
					if (PermUtil.containCategoryPermission(perms, CategoryPerm.CREATE_GROUP)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private GroupResource getResource(long resourceId) {
		return genericDao.get(GroupResource.class, resourceId);
	}
	
	public boolean isReceivedResourceToMember(long resourceId, long memberId) {
		GroupResource resource = this.getResource(resourceId);
		if (resource == null)
			return false;
		Member member = this.getMember(memberId);
		Member everyOneTeam = getEveryOneTeam();
		Member[] beans = getTeamsOfMember(memberId);
		if (member == null && beans == null && everyOneTeam == null)
			return false;
		
		if (this.isReceivedResourceToMemberSelf(member, resource))
			return true;
		
		if (this.isReceivedResourceToMemberSelf(everyOneTeam, resource))
			return true;

		for (Member b : beans) {
			if (this.isReceivedResourceToMemberSelf(b, resource))
				return true;
		}
		return false;
	}
	
	/**
	 * 判断资源及其父资源是否被共享给该member本身
	 * @param member
	 * @param resource
	 * @return
	 */
	private boolean isReceivedResourceToMemberSelf(Member member, GroupResource resource) {
		//查本身是否被共享
		GroupResourceReceive grr = genericDao.findFirst("from GroupResourceReceive grr where grr.recipient = ?1 "
				+ "and grr.resource = ?2 ", member, resource);
		if (grr != null) {
			return true;
		}
		//查父目录是否被共享
		long[] ids = resource.getParentIdsByPath();
		if (ids != null) {
			for (long id:ids) {
				GroupResource resourceparent = this.getResource(id);
				GroupResourceReceive parent = genericDao.findFirst("from GroupResourceReceive grr where grr.recipient = ?1 "
						+ "and grr.resource = ?2 ", member, resourceparent);
				if (parent != null) {
					return true;
				}
			}
		}
		return false;
	}
	
}
