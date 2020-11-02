package com.dcampus.weblib.service;

import java.sql.Timestamp;
import java.util.*;

import javax.persistence.Query;

import com.dcampus.weblib.entity.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dcampus.common.config.PropertyUtil;
import com.dcampus.common.config.ResourceProperty;
import com.dcampus.common.generic.GenericDao;
import com.dcampus.sys.entity.User;
import com.dcampus.sys.service.UserService;
import com.dcampus.sys.util.UserUtils;
import com.dcampus.weblib.dao.CategoryDao;
import com.dcampus.weblib.dao.MemberDao;
import com.dcampus.weblib.entity.CapacityDistribution.FromType;
import com.dcampus.weblib.entity.CapacityDistribution.ToType;
import com.dcampus.weblib.exception.PermissionsException;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.service.permission.RolePermissionUtils;
import com.dcampus.weblib.util.userutil.UserRemoteServiceUtil;
import com.dcampus.weblib.util.userutil.models.RemoteItems;

@Service
@Transactional(readOnly = false)
public class DomainService {

	@Autowired
	private GenericDao genericDao;
	@Autowired
	private MemberDao memberDao;
	@Autowired
	private CategoryDao categoryDao;
	
	@Autowired
	private RolePermissionUtils roleUtils;
	
	@Autowired
	private GrouperService grouperService;
	@Autowired
	private UserService userService;

	@Autowired
	@Lazy
	private GroupService groupService;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private LogService logService;
	@Autowired
	@Lazy
	private RoleService roleService;

	@Autowired
	private ApplicationService appService;

	
	/**
	 * 获取根域，不存在即创建一个
	 * @return
	 */
	public Domain getRootDomain() {
		// TODO Auto-generated method stub
		List<Domain> result = genericDao.findAll("from Domain as d where d.domainName = ?1", PropertyUtil.getRootDomainName());
		if (result != null && result.size() > 0) {
			return result.get(0);
		}
		return this.createRootDomain();
	}
     /*
     获取父域的信息
      */
	 public Map<String,Object> getParentDomainInfo(Long id){
		 Map<String, Object> map = new HashMap<String,Object>();
		 String domaincategoryId="select dc.category.id from "+DomainCategory.class.getName() +" as dc where dc.domain.id=?1";
		 Query qdc = genericDao.createQuery(domaincategoryId, new Object[] { id });
		 List  kk=qdc.getResultList();
		 map.put("category Id",kk.get(0));

		 String pCategorynameSql="select displayName from Category where id=?1";
		 Query qqqq=genericDao.createQuery(pCategorynameSql,new Object[] { kk.get(0) });
		 map.put("category name",qqqq.getResultList().get(0));
		 if(id==1){
			 map.put("父域Id",1);
			 map.put("父域名称","根域");
			 String pdomaincapacitysql="select availableCapacity from "+DomainCategory.class.getName() +" as dc where dc.domain.id=?1";
			 Query pcapacity=genericDao.createQuery(pdomaincapacitysql,new Object[] { id});
			 map.put("父域可用容量",pcapacity.getResultList().get(0));
			 return  map;
		 }
		//父域id不为1的时候
		 String pdomainIdsql="select cd.fromDomain.id from "+CapacityDistribution.class.getName()+" as cd  where cd.toDomain.id=?1";
		 Query q = genericDao.createQuery(pdomainIdsql, new Object[] { id });
		 List aa=q.getResultList();
		 if(aa==null || aa.size()==0|| aa.get(0)==null){
			 throw new GroupsException("父域ID不正常");
		 }else{
		    map.put("父域Id",aa.get(0));
			String pdomainnamesql="select domainName from Domain where id=?1";
			Query qq=genericDao.createQuery(pdomainnamesql,new Object[] { aa.get(0) });
			map.put("父域名称",qq.getResultList().get(0));

			String pdomaincapacitysql="select availableCapacity from "+DomainCategory.class.getName() +" as dc where dc.domain.id=?1";
			Query qqq=genericDao.createQuery(pdomaincapacitysql,new Object[] { aa.get(0) });
			map.put("父域可用容量",qqq.getResultList().get(0));
		 }
		 return  map;
	 }


	/**
	 * 创建根域
	 * @return
	 */
	public Domain createRootDomain() {
		Domain root = new Domain();
		List<Member> members = memberDao.getMembersByAccount("root");
		Member creator = null;
		if (members != null && members.size() >0) {
			creator = members.get(0);
		} else {
			throw new GroupsException("不存在root管理员，无法创建根域！");
		}
		root.setCreator(creator);
		root.setDomainName(PropertyUtil.getRootDomainName());
		Timestamp now = new Timestamp(System.currentTimeMillis());
		root.setCreateDate(now);
		root.setLastModified(now);
		genericDao.save(root);
		return root;
	}

	/**
	 * 添加根域管理员
	 * @param domainId 域id
	 * @param memberId 要添加的用户memberId
	 */
	public void addRootDomainManager(long domainId, Long[] memberId) {
		// 角色权限判断
		if (!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_role_m")) {
			throw new PermissionsException("无角色管理权限");
		}
		this.addDomainManager(domainId, memberId);
		
	}

	/**
	 * 添加域管理员
	 * @param domainId 域id
	 * @param memberId 要添加的用户memberid数组
	 */
	public void addDomainManager(long domainId, Long[] memberId) {
		// 角色权限判断
		if (!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_role_m")
				&& !roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_domain_m")) {
			throw new PermissionsException("无角色管理权限");
		}
		Domain domain = genericDao.get(Domain.class, domainId);
		if (domain == null) {
			throw new GroupsException("找不到对应的域");
		}
		for (long mid : memberId) {
			Member m = (Member) grouperService.getMemberById(mid);
			if (m == null) {
				throw new GroupsException("找不到对应的用户");
			}
			// 查找是否已经授权过
			DomainManager manager = null;
			String hql = "select distinct dm from DomainManager" 
					+ " as dm where dm.domain.id=? and dm.manager.id=?";
			List<DomainManager> list = genericDao.findAll(hql, domainId,mid);
			if(list!=null&&list.size()>0) {
				manager = list.get(0);
				Timestamp now = new Timestamp(System.currentTimeMillis());
				manager.setLastModified(now);
				genericDao.update(manager);
			}else {
				// 设置管理员
				manager = new DomainManager();
				manager.setDomain(domain);
				manager.setManager(m);
				Timestamp now = new Timestamp(System.currentTimeMillis());
				manager.setCreateDate(now);
				manager.setLastModified(now);
				manager.setAuthorizerId(UserUtils.getCurrentMemberId());//设置授权者为当前操作用户
				genericDao.save(manager);
			}
			//加到memberRole表
			DomainRole 	role = genericDao.findFirst("select distinct r from DomainRole as r where r.name=?1", DomainRole.DOMAIN_ADMIN);
			Long roleId =  role.getId();
			roleService.grantRoleToMember(roleId, memberId);

			// 记录日志
			logService.addOperateLog(Log.ACTION_TYPE_ADD,
					"添加管理员" + m.getAccount() + "到域“" + domain.getDomainName() + "”", domainId, domain.getDomainName());
		}
		
	}

	/**
	 * 收回域管理员权限
	 * @param domainId
	 * @param memberId
	 */
	public void revokeRootDomainManager(long domainId, Long[] memberId) {
		// 角色权限判断
		if (!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_role_m")) {
			throw new PermissionsException("无角色管理权限");
		}
		this.revokeDomainManager(domainId, memberId);
		
	}

	public void revokeDomainManager(long domainId, Long[] memberId) {
		// 角色权限判断
		if (!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_role_m")
				&& !roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_domain_m")) {
			throw new PermissionsException("无角色管理权限");
		}
		Domain domain = genericDao.get(Domain.class, domainId);
		if (domain == null) {
			throw new GroupsException("找不到对应的域");
		}
		StringBuffer sb = new StringBuffer();
		StringBuffer mids = new StringBuffer();
		for (long mid : memberId) {
			sb.append(" or dm.manager.id=" + mid + " ");
			mids.append(mid+" ");
		}
		String hql = "delete from DomainManager as dm where dm.domain.id=" + domainId
				+ " and (" + " 1=0 " + sb.toString() + " )";
		Query q = genericDao.createQuery(hql, null);
		q.executeUpdate();
		//删除授权memberRole表
		DomainRole 	role = genericDao.findFirst("select distinct r from DomainRole as r where r.name=?1", DomainRole.DOMAIN_ADMIN);
		Long roleId =  role.getId();
		roleService.revokeRoleFromMember(roleId, memberId);
		// 记录日志
		logService.addOperateLog(Log.ACTION_TYPE_DELETE,
				"删除域“" + domain.getDomainName() + "”内id为"+mids.toString()+"的管理员", domainId, domain.getDomainName());
		
	}

	/**
	 * 获取根域管理员
	 * @return
	 */
	public List<Member> getRootDomainManagers() {
		Domain root = this.getRootDomain();
		String hql = "select distinct dm.manager from DomainManager as dm where dm.domain.id= ?1" ;
		long rootDomainId = root == null? -1L : root.getId();
		List<Member> result = genericDao.findAll(hql, rootDomainId);
		if (result != null && result.size() > 0) {
			return result;
		}
		return new ArrayList<Member>();
	}
	
	/**
	 * 判断某个组织是否可以用来创建域，或关联到域。 判断标准是：如果已经关联到某个已有域，则抛出异常（暂时不考虑子组织是否已关联的情况）
	 * 
	 * @param fid
	 * @throws GroupsException
	 */
	public void checkFolderAvailable(long fid) throws GroupsException {
		List<Long> count = genericDao.findAll("select count(df.id) from DomainFolder as df where df.folder.id=?1", fid);
		if (count != null && !count.isEmpty() && count.get(0) > 0) {
			throw new GroupsException("此组织不能关联到域");
		}
	}

	/**
	 * 在指定的folders上创建域
	 * 
	 * @param folderIds
	 * @return
	 * @throws GroupsException
	 */
	public Domain createDomainOnFolder(Long[] folderIds, String domainName, String desc, Long fromDomainId,
			long capacity) throws GroupsException {
		// 创建新域
		Domain domain = new Domain();
		Timestamp now = new Timestamp(System.currentTimeMillis());
		domain.setCreateDate(now);
		domain.setDomainName(domainName);
		domain.setDesc(desc);
		domain.setCreator(new Member(UserUtils.getCurrentMemberId()));
		domain.setLastModified(now);
		genericDao.save(domain);
		// 把folder 绑定到domain
		if (folderIds != null) {
			for (long fid : folderIds) {
				DomainFolder df = new DomainFolder();
				df.setCreateDate(now);
				df.setCreator(new Member(UserUtils.getCurrentMemberId()));
				df.setDomain(domain);
				Member folder = (Member) grouperService.getFolder(fid);
				df.setFolder(folder);
				df.setFolderUUID(folder.getName());
				df.setLastModified(now);
				genericDao.save(df);
			}
		}
		//默认创建域内所有用户组
		createOrGetDomainAllMemberTeam(domain);
		// 根据容量来创建相应的分类、分配容量
		if (capacity == 0L) {
			throw new GroupsException("域容量配置有误");
		}
		// 创建分类

		long domainCategoryId = categoryService.getCategoriesByName("#domain").get(0).getId();
		Category categoryBean = new Category();
		categoryBean.setCreateDate(now);
		categoryBean.setDesc("域:" + domainName + "自动创建分类");
		categoryBean.setName(domainName);
		categoryBean.setDisplayName(domainName);
		categoryBean.setParentId(domainCategoryId);
		double num = categoryService.getMaxOrder(0);
		num++;
		categoryBean.setOrder(num);
		categoryBean.setCategoryStatus(Category.STATUS_NORMAL);
		//long catId = categoryService.createCategory(categoryBean, true);
		genericDao.save(categoryBean);
		

		// 记录容量分配
		Domain fromDomain = null;
		CapacityDistribution capacityDsb = new CapacityDistribution();
		capacityDsb.setCapacity(capacity);
		capacityDsb.setToDomain(domain);
		capacityDsb.setToType(CapacityDistribution.ToType.DOMAIN);
		capacityDsb.setToGroup(null);// 不是分配给柜子，设为null
		if (fromDomainId == null) {
			// 从系统总容量分配
			// 判断 容量来源是否有空闲容量
			long availableCapacity = groupService.getAvailableCapacity();
			if (availableCapacity < capacity) {
				throw new GroupsException("系统可分配容量不足");
			}
			capacityDsb.setFromDomain(null);
			capacityDsb.setFromType(CapacityDistribution.FromType.GLOBAL_CAPACITY);
		} else {
			fromDomain = genericDao.get(Domain.class, fromDomainId);
			if (fromDomain == null) {
				throw new GroupsException("容量分配父域不存在");
			}
			//  判断 容量来源是否有空闲容量
			long domainAvailableCapacity = getDomainAvailableCapacity(fromDomainId);
			if(domainAvailableCapacity<capacity) {
//				throw new GroupsException("父域可分配容量不足");
				throw new GroupsException("父域剩余容量为"+domainAvailableCapacity+",小于"+capacity);
			}
			capacityDsb.setFromDomain(fromDomain);
			capacityDsb.setFromType(CapacityDistribution.FromType.DOMAIN_CAPACITY);
			updateDomainAvailableCapacity(fromDomainId,domainAvailableCapacity-capacity);
		}
		capacityDsb.setMemberId(UserUtils.getCurrentMemberId());
		capacityDsb.setLastModified(now);
		genericDao.save(capacityDsb);
		
		// 关联域和分类
		DomainCategory dc = new DomainCategory();
		dc.setDomain(domain);
		dc.setCategory(categoryBean);
		dc.setTotalCapacity(capacity);
		dc.setAvailableCapacity(capacity);
		dc.setRelativePath("domain_" + domain.getId() + "_" + now.getTime());
		dc.setLastModified(now);
		genericDao.save(dc);
		
		
		//记录日志
		logService.addOperateLog(Log.ACTION_TYPE_ADD,
				"创建域“"+domainName+"”", domain.getId(), domain.getDomainName());
		logService.addOperateLog(Log.ACTION_TYPE_ADD,
				"创建域分类“"+categoryBean.getDisplayName()+"”", categoryBean.getId(), categoryBean.getDisplayName());
		if(fromDomainId==null) {
			logService.addOperateLog(Log.ACTION_TYPE_MODIFY,
					"从总空间分配"+(capacity)/1048576+"GB 空间到“"+domainName+"” 域", domain.getId(), domain.getDomainName());
		}else {
			logService.addOperateLog(Log.ACTION_TYPE_MODIFY,
					"从“"+fromDomain.getDomainName()+"” 域分配"+(capacity)/1048576+"GB 空间到“"+domainName+"” 域", domain.getId(), domain.getDomainName());
		}
		
		return domain;
	}
	
	/**
	 * 创建域内所有用户组，如果存在则直接返回
	 * @param domain
	 * @return
	 * @throws GroupsException
	 */
	public Member createOrGetDomainAllMemberTeam(Domain domain) throws GroupsException{
		Member allMemberTeam=null;
		if(domain==null) {
			throw new GroupsException("域不存在");
		}
		String hql = "select distinct df.folder from DomainFolder as df where df.domain.id=?";
		List<Member> list = genericDao.findAll(hql, domain.getId());
		if(list.isEmpty()) {
			throw new GroupsException("域未关联组织");
		}
		Member folder = list.get(0);
		//查找folder下是否已经存在“所有用户”组
		RemoteItems[] teams = UserRemoteServiceUtil.findGroupsByParent(folder.getName());
		if(teams!=null&&teams.length>0) {
			for(RemoteItems item:teams) {
				if("所有用户".equals(item.getName())) {
					allMemberTeam = (Member) mapping(item.getId(), item.getFullname(), Member.MEMBER_TYPE_TEAM);
					return allMemberTeam;
				}
			}
		}
		//如果不存在，则创建“所有用户”组
		String folderId = UserRemoteServiceUtil.groupSave("所有用户", "", folder.getAccount(), false);

		// 将folder作为用户添加到用户表中
		allMemberTeam = (Member) mapping(folderId, "所有用户", Member.MEMBER_TYPE_TEAM);
		
		return allMemberTeam;
	}
	private Member mapping(String id, String name, String type) {
		Member member = null;
		member = memberDao.getMemberByNameAccount(id, id, type);
		if (member == null) {
			Member bean = new Member();
			bean.setAccount(id);
			bean.setName(id);
			bean.setMemberStatus(Member.STATUS_NORMAL);
			bean.setMemberType(type);
			bean.setSignature(name);
			memberDao.saveOrUpdateMember(bean);
			member = bean;
		} else {
			member.setSignature(name);
			memberDao.saveOrUpdateMember(member);
		}
		return member;
	}

	/**
	 * 获得某个域的可用空间
	 * @param domainId
	 * @return 单位KB
	 */
	public long getDomainAvailableCapacity(long domainId) {
		long domainAvailableCapacity = 0L;
		String hql = "select availableCapacity from DomainCategory as dc where dc.domain.id=?";
		Long result = genericDao.findFirst(hql, domainId);
		if (result!=null) {
			domainAvailableCapacity=result.longValue();
		}

		return domainAvailableCapacity > 0 ? domainAvailableCapacity : 0;
	}
	/**
	 * 更新某个域的可用空间
	 * @param domainId
	 * @param newAvailableCapacity 单位KB
	 * @throws GroupsException
	 */
	public void updateDomainAvailableCapacity(long domainId,long newAvailableCapacity )throws GroupsException{
		String hql = "update DomainCategory as dc set dc.availableCapacity = ? where dc.domain.id=?";
		genericDao.update(hql, newAvailableCapacity, domainId );
		// 记录日志
		Domain domain = genericDao.get(Domain.class, domainId);
		logService.addOperateLog(Log.ACTION_TYPE_MODIFY, "更新域“" + domain.getDomainName() + "”可用容量为"+newAvailableCapacity+"KB", domainId,
				domain.getDomainName());
	}

	public void deleteDomain(Long domainId,boolean isDeleteAssociatedFolder) throws GroupsException {
		//得到域id所对应的域对象
		Domain target = genericDao.get(Domain.class, domainId);
		if (target != null) {
			// 判断该域的容量是否已经分配出去，如果是则不能删除，直到手工回收了所有容量才能删除
			DomainCategory dc = target.getDomainCategory();//获得域所对应的分类对象
			Set<DomainFolder> dfs=target.getDomainFolder();
			boolean enable2delete = (dc.getTotalCapacity() == dc.getAvailableCapacity());

			if (!enable2delete) {
				throw new GroupsException("无法删除域，请手动回收已分配空间");
			}
			//删除相关容量分配记录
			String hql = "delete from CapacityDistribution as cd where cd.fromDomain.id=?";
			genericDao.update(hql, domainId);
			//归还获得的容量,规则：对于从父域分配来的容量，哪里来，哪里去，从系统分配的直接删除分配记录
			hql = "from CapacityDistribution as cd where cd.toDomain.id=?";
			List<CapacityDistribution> list = genericDao.findAll(hql, domainId);
			if(list!=null) {
				for(CapacityDistribution cd: list) {
					if(cd.getFromType().equals(FromType.DOMAIN_CAPACITY)) {
						Domain fromDomain = cd.getFromDomain();
						long available = getDomainAvailableCapacity(fromDomain.getId());
						updateDomainAvailableCapacity(fromDomain.getId(), available+cd.getCapacity());
					}
                    //删除容量分配记录
					genericDao.delete(cd);
				}
			}

			//删除域与分类的记录
            genericDao.delete(dc);
			//判断域与应用时候有关联
			Application app=appService.getApplicationByDomain(domainId);
			if(app!=null){
				throw  new GroupsException("这个域关联了"+app.getName()+"应用，请在应用那边解除关联后再删除");
			}
			//删除关联组织，以及关联信息
			if(dfs!=null){
				for(DomainFolder df:dfs){
					Member member=df.getFolder();
					String hqll="from "+DomainFolder.class.getName()+" as df where df.folder.id=?";
					List<DomainFolder> dff=genericDao.createQuery(hqll,member.getId()).getResultList();
					//删除关联信息
					genericDao.delete(df);
					//删除组织
					if(isDeleteAssociatedFolder){
						if(dff.size()>1)
							throw  new GroupsException("此域关联的组织关联多个域，不能删除此域的组织");
						grouperService.shiftUpLastModified(member.getId(), new Date());
						grouperService.deleteFolder(member.getId());
					}


				}
			}
            //删除域
			genericDao.delete(target);// 设置了级联删除 域相关信息（关联组织、管理员）
			//删除分类
			categoryService.deleteCategory(dc.getCategory().getId());
		}
		// 记录日志
		logService.addOperateLog(Log.ACTION_TYPE_DELETE, "删除域“" + target.getDomainName() + "”", domainId,
				target.getDomainName());
	}


	/**
	 * 根据个人id查询个人所在的域的根组织
	 * 
	 * @return
	 * @throws GroupsException
	 */
	public List<Member> getMyDomainTreeRoots() throws GroupsException {
		String account = UserUtils.getAccount();
		List<Member> roots = new ArrayList<Member>();
		JSONObject json = UserRemoteServiceUtil.getGroupsOfMembers(new String[] { account });
		if (json.isNullObject() || json.isEmpty()) {
			return roots;
		}
		JSONArray groups = json.getJSONArray("groups");
		Set<Long> uniqueFilter = new HashSet<Long>();
		if (groups != null) {
			String hql = " from DomainFolder as df where df.folder.id=?";
			for (int i = 0; i < groups.size(); i++) {
				JSONObject group = groups.getJSONObject(i);
				JSONArray parentFolders = group.getJSONArray("parentFolders");
				if (parentFolders == null || parentFolders.isEmpty()) {
					continue;
				}
				Member parentFolder = null;
				for (int j = parentFolders.size() - 1; j >= 0; j--) {// 从最直接的父组织开始遍历
					JSONObject pFolder = parentFolders.getJSONObject(j);
					String uuid = pFolder.getString("uuid");
					// Member[] folder = imm.getMembersByName(uuid);
					List<Member> folder = this.getFolderByName(uuid);
					if (folder == null || folder.size() == 0 || folder.get(0) == null) {
						continue;
					} else {
						parentFolder = folder.get(0);
					}
					// 判断parentFolder 是否关联到域
					long mid = parentFolder.getId();
					Object[] param = new Object[] { mid };
					Query q = genericDao.createQuery(hql, param);
					List<DomainFolder> dfs = q.getResultList();
					if (dfs != null && dfs.size() > 0 ) {
						if (parentFolder.getSignature() == null) {
							parentFolder.setSignature(pFolder.getString("name"));
						}
						parentFolder.setIsLeaf(false);
						if (!uniqueFilter.contains(mid)) {
							// 存在与这个父组织关联的域
							uniqueFilter.add(mid);
							roots.add((Member) parentFolder);
						}
						break;
					}
				}
			}
		}

		return roots;
	}

	private List<Member> getFolderByName(String name) {
		String hql = "from Member as m where m.name='" + name + "' and m.memberType="
				+ "'folder'";
		Query q = genericDao.createQuery(hql, null);
		List<Member> result = q.getResultList();
		return result;
	}

//	public void editDomain(Long domainId, String name, String desc, Long fromDomainId, Long newcapacity)
//			throws GroupsException {
//		Domain domain = genericDao.get(Domain.class, domainId);
//		if (domain == null) {
//			throw new GroupsException("找不到对应的域");
//		}
//		if (name != null && !"".equals(name.trim())) {
//			domain.setDomainName(name.trim());
//		}
//		if (desc != null && !"".equals(desc.trim())) {
//			domain.setDesc(desc.trim());
//		}
//		DomainCategory dc = domain.getDomainCategory();
//		//当前域的总容量
//		long oldTotalCapacity = dc.getTotalCapacity();
//		//当前域的可用容量
//		long oldAvailableCapacity = dc.getAvailableCapacity();
//		if (newcapacity != null && oldTotalCapacity != newcapacity) {
//			// 修改了总容量大小
//			//newcapaciy表示域新的总容量
//			long range = newcapacity - oldTotalCapacity;
//			if (oldAvailableCapacity + range < 0) {
//				throw new GroupsException("修改域容量不得低于已使用容量");
//			}
//			dc.setAvailableCapacity(oldAvailableCapacity + range);
//			dc.setTotalCapacity(newcapacity);
//			genericDao.update(dc);
//			// 修改容量来源
//
//			CapacityDistribution capacityDsb = null;
//			if (fromDomainId == null) {
//				// 从系统容量分配的情况
//				// 判断 容量来源是否有空闲容量
//				long availableCapacity = groupService.getAvailableCapacity();
//				if (availableCapacity - range < 0) {
//					throw new GroupsException("系统可分配容量不足");
//				}
//				String hql = "from CapacityDistribution as cd where cd.fromType='"
//						+ FromType.GLOBAL_CAPACITY + "' and cd.toType='" + ToType.DOMAIN + "' and cd.toDomain.id=?";
//				Query q = genericDao.createQuery(hql, new Object[] { domainId });
//				List<CapacityDistribution> cds = q.getResultList();
//				if (cds != null && cds.size() > 0) {
//					capacityDsb = cds.get(0);
//				} else {
//					throw new GroupsException("找不到相应的分配记录，容量调整失败");
//				}
//			} else {
//				// 从另一个域分配容量的情况
//				Domain fromDomain = genericDao.get(Domain.class, fromDomainId);
//				if (fromDomain == null) {
//					throw new GroupsException("容量分配父域不存在");
//				}
//				// 判断 容量来源是否有空闲容量
//				long domainAvailableCapacity = getDomainAvailableCapacity(fromDomainId);
//				System.out.println("range:"+range);
//				System.out.println("domainAvailableCapacity:"+domainAvailableCapacity);
//				if (domainAvailableCapacity - range < 0) {
//					throw new GroupsException("父域可分配容量不足");
//				}
//				String hql = "from " + CapacityDistribution.class.getName() + " as cd where cd.fromType='"
//						+ FromType.DOMAIN_CAPACITY + "' and cd.fromDomain.id=? and cd.toType='" + ToType.DOMAIN
//						+ "' and cd.toDomain.id=?";
//				Query q = genericDao.createQuery(hql, new Object[] { fromDomainId, domainId });
//				List<CapacityDistribution> cds = q.getResultList();
//				if (cds != null && cds.size() > 0) {
//					capacityDsb = cds.get(0);
//				} else {
//					throw new GroupsException("找不到相应的分配记录，容量调整失败");
//				}
//				updateDomainAvailableCapacity(fromDomainId, domainAvailableCapacity - range);
//			}
//			// 修改容量分配记录
//			capacityDsb.setCapacity(capacityDsb.getCapacity() + range);
//			genericDao.update(capacityDsb);
//		}
//		Timestamp now = new Timestamp(System.currentTimeMillis());
//		domain.setLastModified(now);
//		// 记录日志
//		logService.addOperateLog(Log.ACTION_TYPE_MODIFY, "修改域“" + domain.getDomainName() + "”", domainId,
//				domain.getDomainName());
//	}

	public void editDomain(Long domainId, String name, String desc, Long fromDomainId, Long newcapacity)
			throws GroupsException {
		Domain domain = genericDao.get(Domain.class, domainId);
		if (domain == null) {
			throw new GroupsException("找不到对应的域");
		}
		if (name != null && !"".equals(name.trim())) {
			domain.setDomainName(name.trim());
		}
		if (desc != null && !"".equals(desc.trim())) {
			domain.setDesc(desc.trim());
		}
		DomainCategory dc = domain.getDomainCategory();
		//当前域的总容量
		long oldTotalCapacity = dc.getTotalCapacity();
		//当前域的可用容量
		long oldAvailableCapacity = dc.getAvailableCapacity();
		if (newcapacity != null && oldTotalCapacity != newcapacity) {
			// 修改了总容量大小
			//newcapaciy表示域新的总容量
			long range = newcapacity - oldTotalCapacity;
			if (oldAvailableCapacity + range < 0) {
				throw new GroupsException("修改域容量不得低于已使用容量");
			}
			dc.setAvailableCapacity(oldAvailableCapacity + range);
			dc.setTotalCapacity(newcapacity);
			genericDao.update(dc);
			// 修改容量来源
			CapacityDistribution capacityDsb = null;
			String pdomainIdsql="select cd.fromDomain.id from "+CapacityDistribution.class.getName()+" as cd  where cd.toDomain.id=?";
			Query qq = genericDao.createQuery(pdomainIdsql, new Object[] { domainId });
			List aa=qq.getResultList();
			long fromDomain=-2;
			if(aa.size()!=0)
				fromDomain= (long) aa.get(0);
			if(fromDomain!=-2){
				if(fromDomainId==null ||(fromDomainId!=null&& fromDomainId!=fromDomain))
					throw new GroupsException("父域ID输入错误");
			}else {
				if(fromDomainId!=null)
					throw new GroupsException("父域ID输入错误");
			}

			//从系统域分配空间
			if (aa.size()==0 || aa==null) {
				// 从系统容量分配的情况
				// 判断 容量来源是否有空闲容量
				long availableCapacity = groupService.getAvailableCapacity();
				if (availableCapacity - range < 0) {
					throw new GroupsException("系统可分配容量不足");
				}
				String hql = "from CapacityDistribution as cd where cd.fromType='"
						+ FromType.GLOBAL_CAPACITY + "' and cd.toType='" + ToType.DOMAIN + "' and cd.toDomain.id=?";
				Query q = genericDao.createQuery(hql, new Object[] { domainId });
				List<CapacityDistribution> cds = q.getResultList();
				if (cds != null && cds.size() > 0) {
					capacityDsb = cds.get(0);
				} else {
					throw new GroupsException("找不到相应的分配记录，容量调整失败");
				}
			} else {
				// 从另一个域分配容量的情况
//				Domain fromDomain = genericDao.get(Domain.class, fromDomainId);
//				if (fromDomain == null) {
//					throw new GroupsException("容量分配父域不存在");
//				}
				// 判断 容量来源是否有空闲容量
				long domainAvailableCapacity = getDomainAvailableCapacity(fromDomain);
				System.out.println("range:"+range);
				System.out.println("domainAvailableCapacity:"+domainAvailableCapacity);
				if (domainAvailableCapacity - range < 0) {
					throw new GroupsException("父域可分配容量不足");
				}
				String hql = "from " + CapacityDistribution.class.getName() + " as cd where cd.fromType='"
						+ FromType.DOMAIN_CAPACITY + "' and cd.fromDomain.id=? and cd.toType='" + ToType.DOMAIN
						+ "' and cd.toDomain.id=?";
				Query q = genericDao.createQuery(hql, new Object[] { fromDomain, domainId });
				List<CapacityDistribution> cds = q.getResultList();
				if (cds != null && cds.size() > 0) {
					capacityDsb = cds.get(0);
				} else {
					throw new GroupsException("找不到相应的分配记录，容量调整失败");
				}
				updateDomainAvailableCapacity(fromDomain, domainAvailableCapacity - range);
			}
			// 修改容量分配记录
			capacityDsb.setCapacity(capacityDsb.getCapacity() + range);
			genericDao.update(capacityDsb);
		}
		Timestamp now = new Timestamp(System.currentTimeMillis());
		domain.setLastModified(now);
		// 记录日志
		logService.addOperateLog(Log.ACTION_TYPE_MODIFY, "修改域“" + domain.getDomainName() + "”", domainId,
				domain.getDomainName());
	}
	public void addFolderToDomain(Long domainId, Long[] folderIds) throws GroupsException {
		Domain domain = genericDao.get(Domain.class, domainId);
		Timestamp now = new Timestamp(System.currentTimeMillis());
		domain.setLastModified(now);
		for (long fid : folderIds) {
			DomainFolder df = new DomainFolder();
			df.setCreateDate(now);
			df.setCreator(new Member(UserUtils.getCurrentMemberId()));
			df.setDomain(domain);
			Member folder = (Member) grouperService.getFolder(fid);
			df.setFolder(folder);
			df.setFolderUUID(folder.getName());
			df.setLastModified(now);
			genericDao.save(df);
		}

	}

	public void removeFolderFromDomain(Long domainId, Long[] folderIds) {
		StringBuffer hql = new StringBuffer(
				"from DomainFolder as df where df.domain.id=" + domainId + " and ( ");
		for (long fid : folderIds) {
			hql.append(" df.folder.id=" + fid + " or ");
		}
		hql.append(" 0=1 )");
		Query q = genericDao.createQuery(hql.toString(), null);
		q.executeUpdate();
	}

	public List<DomainFolder> getMyManageDomainRoots() {
		Long memberId = UserUtils.getCurrentMemberId();
		String hql = "select df " + " from DomainManager as dm , "
				+ "DomainFolder as df " + "where dm.domain.id=df.domain.id "
				+ "and dm.manager.id=?";
		Query q = genericDao.createQuery(hql, new Object[] { memberId });
		List<DomainFolder> dfs = q.getResultList();
		return dfs;
	}
	public List<Domain> getMyManageDomain() {
		Long memberId = UserUtils.getCurrentMemberId();
		String hql = "select distinct dm.domain from DomainManager as dm "
				+ "where dm.manager.id=?";
		Query q = genericDao.createQuery(hql, new Object[] { memberId });
		List<Domain> domains = q.getResultList();
		return domains;
	}

	/**
	 * 从给定的member列表中，查找与域有关联的组织，并以map形式返回
	 * 
	 * @param memberBeans
	 */
	public Map<Long, Domain> getDomainMapFromFolderArray(Member[] memberBeans) {
		Map<Long, Domain> domainMap = new HashMap<Long, Domain>();
		String hql = "select df from DomainFolder as df where 0=1 ";

		for (Member m : memberBeans) {
			if (m.getMemberType().equals(Member.MEMBER_TYPE_FOLDER)) {
				hql += " or df.folder.id=" + m.getId() + " ";
			}
		}
		Query q = genericDao.createQuery(hql, null);
		List<DomainFolder> dfs = q.getResultList();
		for (DomainFolder df : dfs) {
			domainMap.put(df.getFolder().getId(), df.getDomain());
		}
		return domainMap;

	}

	public Domain getDomainById(Long domainId) {
		return genericDao.get(Domain.class, domainId);
	}

	public List<DomainFolder> getDomainFoldersByDomainId(Long domainId) {
		String hql = "from DomainFolder as df where df.domain.id = " + domainId;
		Query q = genericDao.createQuery(hql, null);
		return q.getResultList();
	}

	public List<Member> getManagersByDomainId(Long domainId) {
		String hql = "select dm.manager from DomainManager as dm where dm.domain.id="
				+ domainId;
		Query q = genericDao.createQuery(hql, null);
		System.out.println(q);
		return q.getResultList();
	}

	/**
	 * 判断某人是否是根域管理员
	 * 
	 * @param memberId
	 *            为null时，判断当前用户
	 * @return
	 */
	public boolean checkRootDomainManager(Long memberId) {
		if (memberId == null) {
			memberId = UserUtils.getCurrentMemberId();
		}
		String hql = "from DomainManager as dm where dm.manager.id=" + memberId
				+ " and dm.domain.domainName='" + PropertyUtil.getRootDomainName() + "'";
		Query q = genericDao.createQuery(hql, null);
		List result = q.getResultList();
		if (result != null && result.size() > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 判断根域是否存在
	 * 
	 * @return
	 */
	public boolean checkRootDomainExist() {
		String hql = "from Domain as d where  d.domainName='"
				+ PropertyUtil.getRootDomainName() + "'";
		Query q = genericDao.createQuery(hql, null);
		List result = q.getResultList();
		if (result != null && result.size() > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 根据需要创建域的某个目标组织的id，来获取包含该组织的父域，这些父域作为容量分配来源
	 * @param targetFolderId
	 * @return
	 */
	public List<Domain> getAvailableCapacitySource(long targetFolderId) {
		List<Domain> source  = new ArrayList<Domain>();
		if(targetFolderId==0||targetFolderId==-1) {
			
			return source;
		}
		//获得目标组织
		Member tar = grouperService.getFolder(targetFolderId);
		//查询该组织的所有父组织
		JSONObject json = UserRemoteServiceUtil.getParentsOfFolderOrTeam(new String[] {tar.getAccount()}, "folder");
		JSONArray parentFolders = json.getJSONArray("parentFolders");
		if(parentFolders!=null) {
			
			//String hql = "select df.domain from "+DomainFolder.class.getName()+" as df where ";

			for (int j = 0; j < parentFolders.size(); j++) {
				JSONObject pFolder = parentFolders.getJSONObject(j);
				String folderUUID = pFolder.getString("uuid");
				Member folder = (Member) memberDao.getFolderByName(folderUUID);
				String hql = "select distinct df.domain from DomainFolder as df "
						+" where df.folder.id=? ";
				Query q = genericDao.createQuery(hql, new Object[] {folder.getId()});
				List<Domain> list = q.getResultList();
				if(list!=null&&list.size()>0) {
					source.addAll(list);
				}
			}
			//把当前选择节点也放入查询范围
			Member folder = (Member) memberDao.getFolderByName(tar.getName());
			String hql = "select distinct df.domain from DomainFolder as df "
					+" where df.folder.id=? ";
			Query q = genericDao.createQuery(hql, new Object[] {folder.getId()});
			List<Domain> list = q.getResultList();
			if(list!=null&&list.size()>0) {
				source.addAll(list);
			}
			
			
		}
		//如果用户不是总域管理员，则返回有管理权限范围内的域
		if(!this.isRootDomainAdmin(UserUtils.getCurrentMemberId())) {
			long mid = UserUtils.getCurrentMemberId();
			Iterator<Domain> it = source.iterator();
			while(it.hasNext()) {
				Domain d = it.next();
				Set<DomainManager> managers =d.getDomainManager();
				for(DomainManager domainManager:managers) {
					if(domainManager.getManager().getId()==mid) {
						return source;
					}
				}
				it.remove();//去掉无管理权限的域来源
			}
		}
		
		
		return source;
	}

	
	/** 判断用户是否是域管理员（不包括总域管理员）是是
	 * @param memberId
	 * @return
	 */
	public boolean isDomainAdmin(long memberId) {
		Domain root = this.getRootDomain();
		String hql = "select distinct dm.manager from DomainManager" 
				+ " as dm where dm.manager.id=" + memberId + " and dm.domain.id !=" + (root == null ? -1 : root.getId());
		Query q = genericDao.createQuery(hql, null);
		List<Member> result = q.getResultList();
		if (result == null || result.size() == 0 || result.get(0) == null)
			return false;
		return true;
	}
	
	/**
	 * 判断用户是否是总域管理员
	 * @param memberId
	 * @return
	 */
	public boolean isRootDomainAdmin(long memberId) {
		Domain root = this.getRootDomain();
		String hql = "select distinct dm.manager from DomainManager" 
				+ " as dm where dm.manager.id=" + memberId + " and dm.domain.id =" + (root == null ? -1 : root.getId());
		Query q = genericDao.createQuery(hql, null);
		List<Member> result = q.getResultList();
		if (result == null || result.size() == 0 || result.get(0) == null)
			return false;
		return true;
	}

	/**
	 * 根据操作者的memberId来获得他所管理的域所关联的分类
	 * @param memberId
	 * @return
	 */
	public Category[] getManageDomainRootCategory(long memberId) {
		String hql = "select distinct dc.category from DomainCategory as dc , "
				+ DomainManager.class.getName() + " as dm where dm.manager.id=? and dc.domain.id=dm.domain.id";
		Query q = genericDao.createQuery(hql, new Object[] {memberId});
		List<Category> result = q.getResultList();
		return result.toArray(new Category[0]);
	}
	
	/** 
	 * 判断分类是否是某个域的存储根分类，即是否直属于#domain分类，并且关联到某个域 
	 * @param cid
	 * @return
	 */
	public Domain isDomainRootCategory(long cid) {
		List<Category>cdo = categoryDao.getCategoriesByName("#domain");
		if (cdo == null) {
			throw new GroupsException("根域还未创建！");
		}
		Category domainCategoryRoot = cdo.get(0);
		long rootId = domainCategoryRoot.getId();
		Category category = genericDao.get(Category.class, cid);
		if(category==null) {
			return null;
		}
		long pid = category.getParentId();
		if(pid==rootId) {
			//并且所关联的域没被删除
			
			String hql = "select distinct dc from DomainCategory as dc where dc.category.id=?";
			Query q = genericDao.createQuery(hql, new Object[] {cid});
			List<DomainCategory> result = q.getResultList();
			if(result==null||result.isEmpty()||result.get(0)==null) {
				return null;
			}
			//没被删除，则是域内分类
			//找到对应的域并返回
			String hql2 = "select distinct dc.domain from DomainCategory as dc where dc.category.id=?";
			Query q2 = genericDao.createQuery(hql2, new Object[] {cid});
			List<Domain> result2 = q2.getResultList();
			if(result2!=null&&result2.size()>0) {
				return result2.get(0);
			}
		}
		return null;
		
	}
	
	/**
	 * 判断一个分类是否是属于某个域，如果是，返回这个域，如果不是返回null
	 * @param cid
	 * @return
	 */
	public Domain isDomainCategory(long cid) {
		long curCategoryId =cid;
		List<Category>cdo = categoryDao.getCategoriesByName("#domain");
		if (cdo == null) {
			throw new GroupsException("根域还未创建！");
		}
		Category domainCategoryRoot = cdo.get(0);
		long rootId = domainCategoryRoot.getId();
		boolean isDomainCategory = false;
		while(curCategoryId>0) {
			Category category = genericDao.get(Category.class, curCategoryId);
			long pid = category.getParentId();
			if(pid==rootId) {
				//并且所关联的域没被删除
				boolean  isDeleted = false;
				String hql = "select distinct dc from DomainCategory as dc where dc.category.id=?";
				Query q = genericDao.createQuery(hql, new Object[] {curCategoryId});
				List<DomainCategory> result = q.getResultList();
				if(result==null||result.isEmpty()||result.get(0)==null) {
					isDeleted = true;
				}
				isDomainCategory = !isDeleted;//没被删除，则是域内组织
				break;
			}else {
				curCategoryId = pid;
			}
		}
		if(isDomainCategory) {
			//找到对应的域并返回
			String hql = "select distinct dc.domain from DomainCategory as dc where dc.category.id=?";
			Query q = genericDao.createQuery(hql, new Object[] {curCategoryId});
			List<Domain> result = q.getResultList();
			if(result!=null&&result.size()>0) {
				return result.get(0);
			}
		}
		return null;
	}
	
	/**
	 * 判断柜子是否是属于某个域
	 * @param gid
	 * @return
	 */
	public Domain isDomainGroup(long gid) {
		Group group = genericDao.get(Group.class, gid);
		if(group!=null) {
			return isDomainCategory(group.getCategory().getId());//判断柜子所属分类是否是某个域的分类
		}
		return null;
	}
	//初始普通柜子分配容量
	public void distributeCapationToGroup(Group toGroup, long capacity) {
		// 判断创建的柜子是否属于某个域，如果是则从域分配容量

		Domain domain = isDomainCategory(toGroup.getCategory().getId());
		if (domain != null) {
			System.out.println("个人资源库分类关联了域，域ID为"+domain.getId());
			// 是在域内重建柜子，则需要从该域分配空间
			// 判断域内可用空间是否足够
			long availableCapacity = getDomainAvailableCapacity(domain.getId());
			if (capacity > availableCapacity) {
				throw new GroupsException("域内可用空间不足");
			}
			// 记录容量分配
			CapacityDistribution cd = new CapacityDistribution();
			cd.setCapacity(capacity);
			cd.setFromDomain(domain);
			cd.setFromType(FromType.DOMAIN_CAPACITY);
			cd.setLastModified(new Timestamp(System.currentTimeMillis()));
			cd.setMemberId(UserUtils.getCurrentMemberId());
			cd.setToGroup(toGroup);
			cd.setToType(ToType.DOMAIN_GROUP);
			genericDao.save(cd);
            //更新域的可用容量
			updateDomainAvailableCapacity(domain.getId(), availableCapacity - capacity);

		} else {
			// 不是域内柜子，从系统空间分配
			// 判断系统可用空间是否足够
			long availableCapacity = groupService.getAvailableCapacity();
			System.out.println("可用空间大小为"+availableCapacity);
			System.out.println("我需要的柜子空间大小为"+capacity);
			if (availableCapacity < capacity) {
				throw new GroupsException("系统可分配容量不足");
			}
		}

	}

	//容量从个人资源库获取
	public void distributeCapationToGroup_v1(Group toGroup, long capacity,String account) {
		// 判断创建的柜子是否属于某个域，如果是则从域分配容量

		Domain domain = isDomainCategory(toGroup.getCategory().getId());
		if (domain != null) {
			System.out.println("个人资源库分类关联了域，域ID为"+domain.getId());
			// 是在域内重建柜子，则需要从该域分配空间
			// 判断域内可用空间是否足够
			long availableCapacity = getDomainAvailableCapacity(domain.getId());
			if (capacity > availableCapacity) {
				throw new GroupsException("域内可用空间不足");
			}
			// 记录容量分配
			CapacityDistribution cd = new CapacityDistribution();
			cd.setCapacity(capacity);
			cd.setFromDomain(domain);
			cd.setFromType(FromType.DOMAIN_CAPACITY);
			cd.setLastModified(new Timestamp(System.currentTimeMillis()));
			cd.setMemberId(UserUtils.getCurrentMemberId());
			cd.setToGroup(toGroup);
			cd.setToType(ToType.DOMAIN_GROUP);
			genericDao.save(cd);

			updateDomainAvailableCapacity(domain.getId(), availableCapacity - capacity);

		} else {
			//不是属于分类,就从个人柜子下面获取容量
			System.out.println("aaa");
			Category categoryPerson=categoryService.getCategoryByName("#person");
			GroupType groupType=groupService.getGroupTypeByName("个人");
			if(categoryPerson.getAvailableCapacity()>=groupType.getTotalFileSize()){
				Long total=groupType.getTotalFileSize();

				categoryPerson.setAvailableCapacity(categoryPerson.getAvailableCapacity()-total);
				categoryDao.saveOrUpdateCategory(categoryPerson);
			}else {
				//删除已经创建的用户
				grouperService.deleteAccount(account);
				throw new GroupsException("个人资源库分类的可用容量不足");
			}
		}
	}

	//获取应用分类的容量
	public void distributeCapationToGroupforApplicaiton(Group toGroup, long capacity,String account) {
		// 判断创建的柜子是否属于某个域，如果是则从域分配容量

		Domain domain = isDomainCategory(toGroup.getCategory().getId());
		if (domain != null) {
			System.out.println("个人资源库分类关联了域，域ID为"+domain.getId());
			// 是在域内重建柜子，则需要从该域分配空间
			// 判断域内可用空间是否足够
			long availableCapacity = getDomainAvailableCapacity(domain.getId());
			if (capacity > availableCapacity) {
				throw new GroupsException("域内可用空间不足");
			}
			// 记录容量分配
			CapacityDistribution cd = new CapacityDistribution();
			cd.setCapacity(capacity);
			cd.setFromDomain(domain);
			cd.setFromType(FromType.DOMAIN_CAPACITY);
			cd.setLastModified(new Timestamp(System.currentTimeMillis()));
			cd.setMemberId(UserUtils.getCurrentMemberId());
			cd.setToGroup(toGroup);
			cd.setToType(ToType.DOMAIN_GROUP);
			genericDao.save(cd);

			updateDomainAvailableCapacity(domain.getId(), availableCapacity - capacity);

		} else {
			//不是属于分类,就从个人柜子下面获取容量
			Category categoryPerson=categoryService.getCategoryByName("#application");
			if(categoryPerson.getAvailableCapacity()>=capacity){
				Long total=capacity;
				categoryPerson.setAvailableCapacity(categoryPerson.getAvailableCapacity()-total);
				categoryDao.saveOrUpdateCategory(categoryPerson);
			}else {
				//删除已经创建的用户
				grouperService.deleteAccount(account);
				throw new GroupsException("分类的可用容量不足");
			}
		}

	}
	public void adjustDomainGroupCapacityDistribution(Group group,long newCapacity) {
		if(newCapacity<0) {
			throw new GroupsException("容量不能设置为负数");
		}
		Domain domain = isDomainCategory(group.getCategory().getId());
		if (domain != null) {
			//柜子属于域
			long oldCapacity = groupService.getResourceSpaceSize(group.getId());//调整前容量
			if(newCapacity==oldCapacity) {
				return;
			}

			long range = newCapacity - oldCapacity;// 调整幅度，正数表示增大容量，负数表示减少容量
			long availableCapacity = getDomainAvailableCapacity(domain.getId());
			if (range > availableCapacity) {
				throw new GroupsException("域内可用空间不足");
			}
			//找到已有的分配记录
			String hql = "from CapacityDistribution as cd where cd.fromType='DOMAIN_CAPACITY' and cd.toType='DOMAIN_GROUP' and  cd.fromDomain.id=? and cd.toGroup.id=?";
			Query q = genericDao.createQuery(hql, new Object[] {domain.getId(),group.getId()});
			List<CapacityDistribution> result = q.getResultList();
			CapacityDistribution cd = null;
			if(result==null||result.isEmpty()) {
				//没有分配记录则新建分配记录
				cd = new CapacityDistribution();
				cd.setCapacity(newCapacity);
				cd.setFromDomain(domain);
				cd.setFromType(FromType.DOMAIN_CAPACITY);
				cd.setMemberId(UserUtils.getCurrentMemberId());
				cd.setToGroup(group);
				cd.setToType(ToType.DOMAIN_GROUP);

			}else {
				cd = result.get(0);
			}
			newCapacity = cd.getCapacity()+range;
			cd.setCapacity(newCapacity);
			if(range>0) {
				cd.setMemberId(UserUtils.getCurrentMemberId());//增大容量时，修改操作者，减小容量时不记录
			}
			Timestamp now = new Timestamp(System.currentTimeMillis());
			cd.setLastModified(now);
			genericDao.update(cd);
			updateDomainAvailableCapacity(domain.getId(), availableCapacity - range);

		}
	}
	//修改的普通柜子的创建，域内柜子分配容量，不是域内柜子topCategory分配容量
	public void distributeCapationToGroup_v2(Group toGroup, long capacity) {
		// 判断创建的柜子是否属于某个域，如果是则从域分配容量

		Domain domain = isDomainCategory(toGroup.getCategory().getId());
		if (domain != null) {
			// 是在域内重建柜子，则需要从该域分配空间
			// 判断域内可用空间是否足够
			long availableCapacity = getDomainAvailableCapacity(domain.getId());
			if (capacity > availableCapacity) {
				throw new GroupsException("域内可用空间不足");
			}
			// 记录容量分配
			CapacityDistribution cd = new CapacityDistribution();
			cd.setCapacity(capacity);
			cd.setFromDomain(domain);
			cd.setFromType(FromType.DOMAIN_CAPACITY);
			cd.setLastModified(new Timestamp(System.currentTimeMillis()));
			cd.setMemberId(UserUtils.getCurrentMemberId());
			cd.setToGroup(toGroup);
			cd.setToType(ToType.DOMAIN_GROUP);
			genericDao.save(cd);
			//更新域的可用容量
			updateDomainAvailableCapacity(domain.getId(), availableCapacity - capacity);

			//更新分类的容量
			if(toGroup.getTopCategoryId()!=null){
				Category category1=categoryService.getCategoryById(toGroup.getTopCategoryId());
				if(category1.getAvailableCapacity()>=capacity){
					category1.setAvailableCapacity(category1.getAvailableCapacity()-capacity);
					categoryDao.saveOrUpdateCategory(category1);
					if(toGroup.getApplicationId()>0){
						Application application=appService.getApplicationById(toGroup.getApplicationId());
						application.setAvailableSpace(String.valueOf(category1.getAvailableCapacity()));
						genericDao.update(application);
					}
				}
				else{
					throw new GroupsException(toGroup.getCategory().getDisplayName()+"分类的可用容量不足");
				}
			}


		} else {
			// 不是域内柜子，从系统空间分配
			// 判断系统可用空间是否足够
//			long availableCapacity = groupService.getAvailableCapacity();
////			System.out.println("可用空间大小为"+availableCapacity);
////			System.out.println("我需要的柜子空间大小为"+capacity);
////			if (availableCapacity < capacity) {
////				throw new GroupsException("系统可分配容量不足");
////			}
			//不是域内柜子，从分类直接分配容量
			if(toGroup.getTopCategoryId()!=null){
				Category category1=categoryService.getCategoryById(toGroup.getTopCategoryId());
				if(category1.getAvailableCapacity()>=capacity){
					category1.setAvailableCapacity(category1.getAvailableCapacity()-capacity);
					categoryDao.saveOrUpdateCategory(category1);
					if(toGroup.getApplicationId()>0){
						Application application=appService.getApplicationById(toGroup.getApplicationId());
						application.setAvailableSpace(String.valueOf(category1.getAvailableCapacity()));
						genericDao.update(application);
					}
				}
				else{
					throw new GroupsException(toGroup.getCategory().getDisplayName()+"分类的可用容量不足");
				}
			}

		}

	}


	/**
	 * 当删除柜子时 ，回收域内柜子所分配的空间
	 * @param group
	 */
	public void recoverDomainGroupCapacityDistribution(Group group) {
		Domain domain = isDomainCategory(group.getCategory().getId());
		if (domain == null) {
			return;// 不是域内柜子
		}
		long capacity = groupService.getResourceSpaceSize(group.getId());
		updateDomainAvailableCapacity(domain.getId(),getDomainAvailableCapacity(domain.getId())+capacity);
		// 删除已有的分配记录
		String hql = "delete from CapacityDistribution" 
				+ " as cd where cd.fromType='DOMAIN_CAPACITY' and cd.toType='DOMAIN_GROUP' and  cd.fromDomain.id=? and cd.toGroup.id=?";
		Query q = genericDao.createQuery(hql, new Object[] { domain.getId(), group.getId() });
		q.executeUpdate();

	}

	
	/**
	 * 域管理员给个人分配、调整、回收个人柜子额外容量大小
	 * @param memberId
	 * @param domainId
	 * @param newcapacity 分配的新容量大小
	 */
	public void modifyPersonalGroupCapacity(long memberId, Long domainId, long newcapacity) throws GroupsException{
		if(newcapacity<0) {
			throw new GroupsException("分配容量值不可为负数");
		}
		//获得个人柜子
		String personalGroupName = "" + memberId;
		Group personalGroup = groupService.getGroupByName(personalGroupName);
		//获得容量来源域
		Domain fromDomain = getDomainById(domainId);
		
		//找到已有的分配记录
		String hql = "from CapacityDistribution as cd where cd.fromType='DOMAIN_CAPACITY' and cd.toType='PERSONAL_GROUP' and  cd.fromDomain.id=? and cd.toGroup.id=?";
		Query q = genericDao.createQuery(hql, new Object[] {domainId,personalGroup.getId()});
		List<CapacityDistribution> result = q.getResultList();
		CapacityDistribution cd = null;
		long oldCapacity = 0;//域给该用户柜子已分配的容量大小
		if(result==null||result.isEmpty()) {
			// 没有分配记录则新建分配记录
			cd = new CapacityDistribution();
			cd.setCapacity(newcapacity);
			cd.setFromDomain(fromDomain);
			cd.setFromType(FromType.DOMAIN_CAPACITY);
			cd.setMemberId(UserUtils.getCurrentMemberId());
			cd.setToGroup((Group) personalGroup);
			cd.setToType(ToType.PERSONAL_GROUP);
		}else {
			cd = result.get(0);
			oldCapacity = cd.getCapacity();
		}
		long range = newcapacity-oldCapacity;//调整幅度
		//判断域可用容量是否足够
		long availableCapacity = getDomainAvailableCapacity(fromDomain.getId());
		if (range > availableCapacity) {
			throw new GroupsException("域内可用空间不足");
		}
		//修改柜子容量
		long old = groupService.getResourceSpaceSize(personalGroup.getId());//调整前容量
		personalGroup.setTotalFileSize(old+range);
		genericDao.update(personalGroup);
		//genericDao.update((Group)personalGroup);
		//修改域可用容量
		updateDomainAvailableCapacity(domainId, availableCapacity-range);
		//修改分配记录
		
		cd.setCapacity(newcapacity);
		if(range>0) {
			cd.setMemberId(UserUtils.getCurrentMemberId());
		}
		Timestamp now = new Timestamp(System.currentTimeMillis());
		cd.setLastModified(now);
		genericDao.save(cd);
	}

	/**
	 * 获取某人上传资源的域标签，
	 * @param memberId null 时则查询本人
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Domain> getDomainTagOfMember(Long memberId) {
		if(memberId==null) {
			memberId = UserUtils.getCurrentMemberId();
		}
		// 获得个人柜子
		String personalGroupName = "" + memberId;
		Group personalGroup = groupService.getGroupByName(personalGroupName);

		String hql = "select cd.fromDomain from CapacityDistribution as cd where cd.toType='PERSONAL_GROUP' and cd.fromType='DOMAIN_CAPACITY' and cd.toGroup.id=?";
		Query q = genericDao.createQuery(hql, new Object[] {personalGroup.getId()});
		
		return q.getResultList();
	}

	/**
	 * 根据domainId 获取该域下的所有用户的个人柜子，其中，柜子的容量为当前域分配给该用户的额外容量
	 * @param domainId
	 * @return
	 */
	public List<Group> getAllPersonalGroupByDomain(long domainId) {
		List<Group> list = new ArrayList<Group>();
		Domain domain = this.getDomainById(domainId);
		if (domain == null) {
			throw new GroupsException("找不到对应的域");
		}
		//获得该域下的所有人
		Set<DomainFolder> dfs = domain.getDomainFolder();
		Set<Member> uniqueSet = new HashSet<Member>();
		for(DomainFolder df: dfs) {
			Member folder = df.getFolder();
			Member [] members = grouperService.getMemberInFolder(folder.getId());
			uniqueSet.addAll(Arrays.asList(members));
		}
		//查询当前域给哪些个人柜子分配了容量
		Map<Long ,CapacityDistribution> cdMap = new HashMap<Long, CapacityDistribution>();
		String hql  ="from "+CapacityDistribution.class.getName()+" as cd where cd.toType='PERSONAL_GROUP' and cd.fromType='DOMAIN_CAPACITY' and cd.fromDomain.id=?";
		Query q = genericDao.createQuery(hql, new Object[] {domainId});
		List<CapacityDistribution> cds = q.getResultList();
		for(CapacityDistribution cd: cds) {
			long groupId = cd.getToGroup().getId();
			cdMap.put(groupId, cd);
		}
		//获得每个人的个人柜子
		for(Member member: uniqueSet) {
			String personGroupName = "" + member.getId();
			Group pg;
			try {
				pg = groupService.getGroupByName(personGroupName);
				if (pg != null && pg.getId() > 0) {
					if(cdMap.containsKey(pg.getId())) {
						pg.setTotalFileSize(cdMap.get(pg.getId()).getCapacity());
					}else {
						pg.setTotalFileSize(0L);
					}
					list.add(pg);
				}

			} catch (GroupsException e) {
				continue;
			}
			
		}
		return list;
	}

	/**
	 * 创建一个管理员账号，用于创建域的同时，创建新管理员用户
	 * @param account
	 * @param name
	 * @param password
	 * @return
	 */
	public Member registNewManagerAccount(String account, String name, String password) {
		if (PropertyUtil.getUserMaxLimit() != null) {
			long size = userService.getNumberOfUser() 
					- userService.countAdmin(Admin.SUPER_ADMIN);
			if (size >= PropertyUtil.getUserMaxLimit().longValue()) {
				throw new GroupsException(ResourceProperty.getOverUserMaxLimitException(
						""+PropertyUtil.getUserMaxLimit().longValue()));
			}
		}
		if(account != null &&!"".equals(account.trim())){
			User  existAccount =userService.getUserByAccountOrMobile(account);
			if(existAccount!=null){
				throw new GroupsException("账号"+account+"已经存在");
			}
		}
		User bean = new User();
		bean.setAccount(account);
		bean.setPassword(password);
		bean.setName(name);
		
		
		bean.setUserCreateType(User.USER_CREATE_TYPE_ADD);
		bean.setUserbaseStatus(User.USER_STATUS_NORMAL);
		userService.createAccount(bean);

		// 默认注册一个同名马甲
		Member memberBean = new Member();
		memberBean.setAccount(account);
		memberBean.setName(account);
		memberBean.setMemberStatus(Member.STATUS_NORMAL);
		memberBean.setMemberType(Member.MEMBER_TYPE_PERSON);
		grouperService.createMember(memberBean);
		return memberBean;
	}

	/**
	 * 获取当前用户所创建的域管理员
	 * @return
	 */
	public List<Member> getMyCreatedDomainManager(Long domainId) throws GroupsException{
		List<Member> list;
		long curMemberId = UserUtils.getCurrentMemberId();
		if(!isDomainAdmin(curMemberId)&&!isRootDomainAdmin(curMemberId)) {
			throw new GroupsException("非域管理员无法查询");
		}
		String hql = "select distinct dm.manager from DomainManager as dm where dm.authorizerId=?";
		if(domainId!=null&&domainId!=0) {
			hql+=(" and dm.domain.id="+domainId);
		}
		Query q = genericDao.createQuery(hql, new Object[] {curMemberId});
		list = q.getResultList();
		return list;
	}

	/**
	 * 获得某个域的容量来源组成
	 * 获得父域相关信息接口
	 * @param domain
	 * @return
	 */
	public List<CapacityDistribution> getDomainCapacityStruct(Domain domain) throws GroupsException{
		if(domain==null) {
			throw new GroupsException("域不存在");
		}
		String hql = "from CapacityDistribution as cd where cd.toDomain.id=? and toType='"+ToType.DOMAIN+"'";
		Query query = genericDao.createQuery(hql, new Object[] {domain.getId()});
		
		return query.getResultList();
	}

	/**
	 * 获得某个域的资源实际占用空间大小
	 * @param domain
	 * @return
	 */
	public long getDomainResourceSize(Domain domain) {
		if (domain == null)
			return 0;
		Category category = null;
		if(domain.getId().longValue()==this.getRootDomain().getId()) {
			//如果查询的是总域，则返回所有域的资源占用大小
			category = categoryService.getCategoriesByName("#domain").get(0);
		}else {
			category = domain.getDomainCategory().getCategory();
		}
		//domain.getDomainCategory().getCategory();
		List<Group> groups = groupService.getGroupsInCategory(category.getId());
		if(groups==null||groups.size()==0) {
			return 0;
		}
//		String hql = "select sum(size) from GroupResource as r where r.resourceType=2 and r.groupId in ( :gids )";
		String hql = "select sum(size) from "+ GroupResource.class.getName()+" as r where r.resourceType=2 and r.agroup.id in ( :gids )";
		List<Long> gids = new ArrayList<Long>();
		for(Group g:groups) {
			gids.add(g.getId());
		}
		Query q = genericDao.createQuery(hql,null);
		q.setParameter("gids", gids);

		long size= q.getSingleResult()==null?0:(Long)q.getSingleResult();
		return size;
	}

	/**
	 * 获得某个组织节点（用户组、组织）所关联的直属（最近）域
	 * @param id
	 * @param type
	 * @return
	 */
	public Domain getDomainByTeamOrFolder(long id, String type) {
		
		try {
			if (Member.MEMBER_TYPE_TEAM.equals(type)) {
				Member team = grouperService.getTeam(id);
				JSONObject json = UserRemoteServiceUtil.getParentsOfFolderOrTeam(new String[] { team.getAccount() },
						team.getMemberType());
				JSONArray parentFolders = json.getJSONArray("parentFolders");
				if (parentFolders == null || parentFolders.isEmpty()) {
					return null;
				}
				for (int j = parentFolders.size() - 1; j >= 0; j--) {// 从最直接的父组织开始遍历
					JSONObject pFolder = parentFolders.getJSONObject(j);
					String folderUUID = pFolder.getString("uuid");
					Member parent = (Member) memberDao.getFolderByName(folderUUID);
					Domain d = isDomainRootFolder(parent.getId());
					if(d!=null) {
						return d;
					}
					
				}
				
			} else if (Member.MEMBER_TYPE_FOLDER.equals(type)) {
				Member folder = grouperService.getFolder(id);
				Domain d = isDomainRootFolder(id);//查询自身是否是域所关联的根组织
				if(d!=null) {
					return d;
				}
				JSONObject json = UserRemoteServiceUtil.getParentsOfFolderOrTeam(new String[] { folder.getAccount() },
						folder.getMemberType());
				JSONArray parentFolders = json.getJSONArray("parentFolders");
				if (parentFolders == null || parentFolders.isEmpty()) {
					return null;
				}
				for (int j = parentFolders.size() - 1; j >= 0; j--) {// 从最直接的父组织开始遍历
					JSONObject pFolder = parentFolders.getJSONObject(j);
					String folderUUID = pFolder.getString("uuid");
					Member parent = (Member) memberDao.getFolderByName(folderUUID);
					d = isDomainRootFolder(parent.getId());
					if(d!=null) {
						return d;
					}
					
				}
				
			}else {
				return null;
			}
		} catch (GroupsException e) {
			return null;
		}
		return null;
	}
	
	public Domain isDomainRootFolder(long folderId) {
		String hql = " from DomainFolder as df where df.folder.id=?";
		Query q = genericDao.createQuery(hql, new Object[] {folderId});
		List<DomainFolder> dfs = q.getResultList();
		if(dfs!=null&&dfs.size()>0&&dfs.get(0)!=null) {
			return dfs.get(0).getDomain();
		}
		return null;
	}

	
	

}
