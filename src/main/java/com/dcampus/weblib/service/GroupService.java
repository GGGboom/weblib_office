package com.dcampus.weblib.service;


import java.io.File;
import java.io.OutputStream;
import java.net.URI;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

import javax.persistence.Query;

import com.dcampus.sys.entity.User;
import com.dcampus.weblib.entity.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dcampus.common.config.LogMessage;
import com.dcampus.common.config.PropertyUtil;
import com.dcampus.common.config.ResourceProperty;
import com.dcampus.common.generic.GenericDao;
import com.dcampus.common.paging.AndSearchTerm;
import com.dcampus.common.paging.OrSearchTerm;
import com.dcampus.common.paging.PageNavigater;
import com.dcampus.common.paging.PageTerm;
import com.dcampus.common.paging.SearchItem;
import com.dcampus.common.paging.SearchTerm;
import com.dcampus.common.paging.SortTerm;
import com.dcampus.common.util.CacheUtil;
import com.dcampus.sys.util.UserUtils;
import com.dcampus.weblib.dao.CategoryDao;
import com.dcampus.weblib.dao.GroupDao;
import com.dcampus.weblib.dao.GroupManagerDao;
import com.dcampus.weblib.dao.GroupResourceDao;
import com.dcampus.weblib.dao.GroupResourceShareDao;
import com.dcampus.weblib.dao.GroupTypeDao;
import com.dcampus.weblib.dao.IGroupResourceDao;
import com.dcampus.weblib.dao.MemberDao;
import com.dcampus.weblib.dao.OldPermDao;
import com.dcampus.weblib.dao.WatchDao;
import com.dcampus.weblib.exception.PermissionsException;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.service.permission.IPermission.CategoryPerm;
import com.dcampus.weblib.service.permission.IPermission.GroupPerm;
import com.dcampus.weblib.service.permission.RolePermissionUtils;
import com.dcampus.weblib.service.permission.impl.Permission;
import com.dcampus.weblib.util.CheckUtil;
import com.dcampus.weblib.util.FilePathUtil;
import com.dcampus.weblib.util.FileUtil;
import com.dcampus.weblib.util.userutil.UserRemoteServiceUtil;
import com.dcampus.weblib.util.userutil.models.RemoteItems;

/**
 * 对资源柜子的处理，基于之前的逻辑，去掉一些无用的逻辑处理
 * 例如审核，讨论区,groupdecration
 * @author patrick
 *
 */
@Service
@Transactional(readOnly = false)
public class GroupService {
	private static final Logger log = Logger.getLogger(GroupService.class);
	@Autowired 
	private GroupDao groupDao;
	@Autowired 
	private IGroupResourceDao groupResourceDao;



	@Autowired
	private MemberDao memberDao;	
	
	@Autowired
	private GroupResourceShareDao shareDao;	
	
	@Autowired 
	private GroupTypeDao groupTypeDao;
	
	@Autowired
	private GroupManagerDao groupManagerDao;
	
	@Autowired
	private CategoryDao categoryDao;
	
	@Autowired
	private WatchDao watchDao;
	
	@Autowired
	private OldPermDao permDao;
	
	@Autowired
	private GenericDao genericDao;

	@Autowired
	private PermissionService permService;

	@Autowired
	@Lazy
	private GrouperService grouperService;

	@Autowired
	@Lazy
	private DomainService domainService;

	@Autowired
	private LogService logService;

    @Autowired
	private ApplicationService appService;

	
	@Autowired
	private Permission permission;

	@Autowired
	@Lazy
	private  CategoryService categoryService;



	public long getAllGroupSpaceSize(long topCategoryId) {
		Long result =genericDao.findFirst("select sum(g.totalFileSize) from Group g where g.topCategoryId = ?1 ", topCategoryId);
		if(result!=null)
			return result;
		else
			return 0;
	}

	public void saveOrUpdateGroup(Group group) {
		long memberId = UserUtils.getCurrentMemberId();
		this.saveOrUpdateGroup(group, memberId);
	}
	/**
	 * 涉及到柜子的额外信息（单文件大小限制和柜子总的空间）的处理的保存
	 * @param group
	 */
	@Transactional
	public void saveOrUpdateGroup(Group group, long memberId) {
		if (memberId != OldPerm.SYSTEMADMIN_MEMBER_ID) {
			if (!(permission.hasCategoryPerm(memberId, group
					.getCategory().getId(), CategoryPerm.CREATE_GROUP) || permission.hasCategoryPerm(memberId, group.getCategory().getId(),
							CategoryPerm.CATEGORY_APPMANAGER)))
				throw PermissionsException.GroupException;
		}
		Category category = group.getCategory();
		GroupType type = group.getGroupType();
		if (category == null || category.getId() <= 0) {
			throw new GroupsException("柜子所在分类不存在！");
		}
		if (type == null || type.getId() <= 0) {
			throw new GroupsException("柜子所属类别不存在！");
		}
		groupDao.saveOrUpdateGroup(group);

		// 更新柜子额外信息
		Map<String, String> externInfo = group.getExternInfo();

		if (externInfo == null || externInfo.size() == 0) {
			
		}
		else {
			// 添加externInfo
			for (String key : externInfo.keySet()) {
				GroupExtern bean = groupDao.getGroupExternByGroupAndKey(group.getId(), key);
				if (bean == null || bean.getId() <= 0) {
					bean = new GroupExtern();
				}
				bean.setAttrKey(key);
				bean.setAttrValue(externInfo.get(key));
				bean.setGroupAddr(group.getAddr());
				bean.setGroup(group);
				bean.setGroupName(group.getName());
				groupDao.saveOrUpdateGroupExtern(bean);
			}

			// 删除掉多余的externInfo
			List<GroupExtern> beans = groupDao.getGroupExternsByGroup(group.getId());
			for (GroupExtern bean : beans) {
				if (externInfo.get(bean.getAttrKey()) == null) {
					groupDao.deleteGroupExternByGroupAndKey(group.getId(), bean.getAttrKey());
				}
			}
		}
		CacheUtil.removeAll(CacheUtil.categoryGroupCache);
	}

	public long createGroup(Group group, boolean mark, String account) {
		long memberId = UserUtils.getCurrentMemberId();
		return this.createGroup(group, mark, account, memberId);
	}
	/**
	 * 创建一个柜子，需要创建柜子管理员
	 * 绑定用户到柜子
	 * 之前会同时默认创建一个讨论区，现没有
	 * @param group
	 * @param mark 是否检查柜子名字和地址
	 * @param account 需要绑定的用户
	 * @return
	 */
	/**
	 *group/createGroup_v2来直接创建柜子
	 */
	public long createGroup_v2(Group group, boolean mark, String account,
							   long memberId,Long topCategoryId,Long totalSize) {
		if (memberId != OldPerm.SYSTEMADMIN_MEMBER_ID) {
			if (!(permission.hasCategoryPerm(memberId, group
					.getCategory().getId(), CategoryPerm.CREATE_GROUP) || permission.hasCategoryPerm(memberId, group.getCategory().getId(),
							CategoryPerm.CATEGORY_APPMANAGER)))
				throw PermissionsException.GroupException;
		}
		// 检验
		if (mark == true) {
			CheckUtil.checkGroupName(group.getName());
			CheckUtil.checkGroupAddr(group.getAddr());
		}
		// 设置柜子统计信息
		group.setMemberCount(1);
		group.setTopCategoryId(topCategoryId);
		group.setAvailableCapacity(totalSize);
		group.setUsedCapacity(0L);
		group.setTotalFileSize(totalSize);

//		if(topCategoryId!=null){
//			Category category1=categoryService.getCategoryById(topCategoryId);
//			if (category1 == null) {
//				throw new  GroupsException("分类不存在");
//			}else {
//				if(category1.getAvailableCapacity()>=totalSize){
//					group.setTopCategoryId(topCategoryId);
//					group.setAvailableCapacity(totalSize);
//					group.setTotalFileSize(totalSize);
//					category1.setAvailableCapacity(category1.getAvailableCapacity()-totalSize);
//					categoryDao.saveOrUpdateCategory(category1);
//				}
//				else
//					throw new GroupsException("id为"+topCategoryId+"的分类的可用容量不足\n");
//			}
//		}

		// 设置柜子的等级为默认等级
		if (group.getGroupType() == null || group.getGroupType().getId() <= 0 ) {
			GroupType typeBean = groupTypeDao.getGroupTypeByName(PropertyUtil.getDefaultGroupType());
			group.setGroupType(typeBean);
			if (group.getTotalFileSize() == null) {
				group.setTotalFileSize(typeBean.getTotalFileSize());
			}
		}

		String path = "/";
		List<Category> clist = categoryDao.tracedCategoryList(group.getCategory().getId());
		for (int i = clist.size() - 1; i >= 0; i--) {
			Category c = clist.get(i);
			path = path + "c" + c.getId() + "/";
		}
		group.setPath(path);
		// 创建柜子
		this.saveOrUpdateGroup(group, memberId);
		//如果是域内柜子，修改容量分配记录
		domainService.distributeCapationToGroup_v2((Group) group, group.getTotalFileSize());
		
		String desc = LogMessage.getGroupAdd(group.getDisplayName());
		logService.addOperateLog(Log.ACTION_TYPE_ADD, desc, group.getId(), group.getDisplayName());
		/**************************************/
		// 创建柜子管理员       
		GroupManager managerBean = new GroupManager();
		List<Member> memberBean = null;
		if (account != null) {
			memberBean = memberDao.getMembersByAccount(account);
			managerBean.setMemberId(memberBean.get(0).getId());
			managerBean.setMemberName(memberBean.get(0).getAccount());
		} else {
			managerBean.setMemberId(group.getCreatorId());
			managerBean.setMemberName(group.getCreatorName());
		}
		managerBean.setGroupId(group.getId());
		managerBean.setCreateDate(group.getCreateDate());
		managerBean.setPriority(0);
		groupManagerDao.saveOrUpdateGroupManager(managerBean);

		// 绑定用户到柜子
		if (account != null) {
			memberDao.createMemberGroupBinding(memberBean.get(0).getId(), group.getId(), true);
		} else {
			memberDao.createMemberGroupBinding(group.getCreatorId(), group.getId(), true);
		}
		
		// 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		CacheUtil.removeAll(CacheUtil.myVisualGroupCache);
		CacheUtil.removeAll(CacheUtil.categoryGroupCache);
		return group.getId();	
	}

	/**
	 *创建柜子
	 * @return
	 */
	public long createGroup(Group group, boolean mark, String account,long memberId) {
		if (memberId != OldPerm.SYSTEMADMIN_MEMBER_ID) {
			if (!(permission.hasCategoryPerm(memberId, group
					.getCategory().getId(), CategoryPerm.CREATE_GROUP) || permission.hasCategoryPerm(memberId, group.getCategory().getId(),
					CategoryPerm.CATEGORY_APPMANAGER)))
				throw PermissionsException.GroupException;
		}
		// 检验
		if (mark == true) {
			CheckUtil.checkGroupName(group.getName());
			CheckUtil.checkGroupAddr(group.getAddr());
		}
		// 设置柜子统计信息
		group.setMemberCount(1);

		// 设置柜子的等级为默认等级
		if (group.getGroupType() == null || group.getGroupType().getId() <= 0 ) {
			GroupType typeBean = groupTypeDao.getGroupTypeByName(PropertyUtil.getDefaultGroupType());
			group.setGroupType(typeBean);
			if (group.getTotalFileSize() == null) {
				group.setTotalFileSize(typeBean.getTotalFileSize());
			}
		}

		String path = "/";
		List<Category> clist = categoryDao.tracedCategoryList(group.getCategory().getId());
		for (int i = clist.size() - 1; i >= 0; i--) {
			Category c = clist.get(i);
			path = path + "c" + c.getId() + "/";
		}
		group.setPath(path);
		// 创建柜子
		this.saveOrUpdateGroup(group, memberId);
		//如果是域内柜子，修改容量分配记录
		domainService.distributeCapationToGroup((Group) group,group.getTotalFileSize());

		String desc = LogMessage.getGroupAdd(group.getDisplayName());
		logService.addOperateLog(Log.ACTION_TYPE_ADD, desc, group.getId(), group.getDisplayName());
		/**************************************/
		// 创建柜子管理员
		GroupManager managerBean = new GroupManager();
		List<Member> memberBean = null;
		if (account != null) {
			memberBean = memberDao.getMembersByAccount(account);
			managerBean.setMemberId(memberBean.get(0).getId());
			managerBean.setMemberName(memberBean.get(0).getAccount());
		} else {
			managerBean.setMemberId(group.getCreatorId());
			managerBean.setMemberName(group.getCreatorName());
		}
		managerBean.setGroupId(group.getId());
		managerBean.setCreateDate(group.getCreateDate());
		managerBean.setPriority(0);
		groupManagerDao.saveOrUpdateGroupManager(managerBean);

		// 绑定用户到柜子
		if (account != null) {
			memberDao.createMemberGroupBinding(memberBean.get(0).getId(), group.getId(), true);
		} else {
			memberDao.createMemberGroupBinding(group.getCreatorId(), group.getId(), true);
		}

		// 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		CacheUtil.removeAll(CacheUtil.myVisualGroupCache);
		CacheUtil.removeAll(CacheUtil.categoryGroupCache);
		return group.getId();
	}

	/**
	 *新增用户同时创建柜子
	 * @return
	 */
	public long createGroup_v1(User bean,Group group, boolean mark, String account, long memberId) {
		if (memberId != OldPerm.SYSTEMADMIN_MEMBER_ID) {
			if (!(permission.hasCategoryPerm(memberId, group
					.getCategory().getId(), CategoryPerm.CREATE_GROUP) || permission.hasCategoryPerm(memberId, group.getCategory().getId(),
					CategoryPerm.CATEGORY_APPMANAGER)))
				throw PermissionsException.GroupException;
		}
		// 检验
		if (mark == true) {
			CheckUtil.checkGroupName(group.getName());
			CheckUtil.checkGroupAddr(group.getAddr());
		}
		// 设置柜子统计信息
		group.setMemberCount(1);

		// 设置柜子的等级为默认等级
		if (group.getGroupType() == null || group.getGroupType().getId() <= 0 ) {
			GroupType typeBean = groupTypeDao.getGroupTypeByName(PropertyUtil.getDefaultGroupType());
			group.setGroupType(typeBean);
			if (group.getTotalFileSize() == null) {
				group.setTotalFileSize(typeBean.getTotalFileSize());
			}
		}
		//编辑分类
		Category categoryPerson=categoryService.getCategoryByName("#person");
//		GroupType groupType=getGroupTypeByName("个人");
//		if(categoryPerson.getAvailableCapacity()>=groupType.getTotalFileSize()){
//			Long total=groupType.getTotalFileSize();
//			group.setTopCategoryId(categoryPerson.getId());
//			categoryPerson.setAvailableCapacity(categoryPerson.getAvailableCapacity()-total);
//			categoryDao.saveOrUpdateCategory(categoryPerson);
//		}else {
//			throw new GroupsException("个人资源库分类的可用容量不足\n");
//		}
		group.setTopCategoryId(categoryPerson.getId());


		String path = "/";
		List<Category> clist = categoryDao.tracedCategoryList(group.getCategory().getId());
		for (int i = clist.size() - 1; i >= 0; i--) {
			Category c = clist.get(i);
			path = path + "c" + c.getId() + "/";
		}
		group.setPath(path);
		group.setUser(bean);
		// 创建柜子
		this.saveOrUpdateGroup(group, memberId);
		//如果是域内柜子，修改容量分配记录
		domainService.distributeCapationToGroup_v1((Group) group, group.getTotalFileSize(),account);

		String desc = LogMessage.getGroupAdd(group.getDisplayName());
		logService.addOperateLog(Log.ACTION_TYPE_ADD, desc, group.getId(), group.getDisplayName());
		/**************************************/
		// 创建柜子管理员
		GroupManager managerBean = new GroupManager();
		List<Member> memberBean = null;
		if (account != null) {
			memberBean = memberDao.getMembersByAccount(account);
			managerBean.setMemberId(memberBean.get(0).getId());
			managerBean.setMemberName(memberBean.get(0).getAccount());
		} else {
			managerBean.setMemberId(group.getCreatorId());
			managerBean.setMemberName(group.getCreatorName());
		}
		managerBean.setGroupId(group.getId());
		managerBean.setCreateDate(group.getCreateDate());
		managerBean.setPriority(0);
		groupManagerDao.saveOrUpdateGroupManager(managerBean);

		// 绑定用户到柜子
		if (account != null) {
			memberDao.createMemberGroupBinding(memberBean.get(0).getId(), group.getId(), true);
		} else {
			memberDao.createMemberGroupBinding(group.getCreatorId(), group.getId(), true);
		}

		// 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		CacheUtil.removeAll(CacheUtil.myVisualGroupCache);
		CacheUtil.removeAll(CacheUtil.categoryGroupCache);
		return group.getId();
	}
	/**
	 *创建应用同时创建柜子
	 * @return
	 */
	public long createGroupforApplication(Group group, boolean mark, String account,long memberId) {
		if (memberId != OldPerm.SYSTEMADMIN_MEMBER_ID) {
			if (!(permission.hasCategoryPerm(memberId, group
					.getCategory().getId(), CategoryPerm.CREATE_GROUP) || permission.hasCategoryPerm(memberId, group.getCategory().getId(),
					CategoryPerm.CATEGORY_APPMANAGER)))
				throw PermissionsException.GroupException;
		}
		// 检验
		if (mark == true) {
			CheckUtil.checkGroupName(group.getName());
			CheckUtil.checkGroupAddr(group.getAddr());
		}
		// 设置柜子统计信息
		group.setMemberCount(1);

		// 设置柜子的等级为默认等级
		if (group.getGroupType() == null || group.getGroupType().getId() <= 0 ) {
			GroupType typeBean = groupTypeDao.getGroupTypeByName(PropertyUtil.getDefaultGroupType());
			group.setGroupType(typeBean);
			if (group.getTotalFileSize() == null) {
				group.setTotalFileSize(typeBean.getTotalFileSize());
			}
		}
		//编辑分类
		Category categoryPerson=categoryService.getCategoryByName("#application");
//		GroupType groupType=getGroupTypeByName("个人");
//		if(categoryPerson.getAvailableCapacity()>=groupType.getTotalFileSize()){
//			Long total=groupType.getTotalFileSize();
//			group.setTopCategoryId(categoryPerson.getId());
//			categoryPerson.setAvailableCapacity(categoryPerson.getAvailableCapacity()-total);
//			categoryDao.saveOrUpdateCategory(categoryPerson);
//		}else {
//			throw new GroupsException("个人资源库分类的可用容量不足\n");
//		}
		group.setTopCategoryId(categoryPerson.getId());


		String path = "/";
		List<Category> clist = categoryDao.tracedCategoryList(group.getCategory().getId());
		for (int i = clist.size() - 1; i >= 0; i--) {
			Category c = clist.get(i);
			path = path + "c" + c.getId() + "/";
		}
		group.setPath(path);
		// 创建柜子
		this.saveOrUpdateGroup(group, memberId);
		//如果是域内柜子，修改容量分配记录
		domainService.distributeCapationToGroupforApplicaiton((Group) group, group.getTotalFileSize(),account);

		String desc = LogMessage.getGroupAdd(group.getDisplayName());
		logService.addOperateLog(Log.ACTION_TYPE_ADD, desc, group.getId(), group.getDisplayName());
		/**************************************/
		// 创建柜子管理员
		GroupManager managerBean = new GroupManager();
		List<Member> memberBean = null;
		if (account != null) {
			memberBean = memberDao.getMembersByAccount(account);
			managerBean.setMemberId(memberBean.get(0).getId());
			managerBean.setMemberName(memberBean.get(0).getAccount());
		} else {
			managerBean.setMemberId(group.getCreatorId());
			managerBean.setMemberName(group.getCreatorName());
		}
		managerBean.setGroupId(group.getId());
		managerBean.setCreateDate(group.getCreateDate());
		managerBean.setPriority(0);
		groupManagerDao.saveOrUpdateGroupManager(managerBean);

		// 绑定用户到柜子
		if (account != null) {
			memberDao.createMemberGroupBinding(memberBean.get(0).getId(), group.getId(), true);
		} else {
			memberDao.createMemberGroupBinding(group.getCreatorId(), group.getId(), true);
		}

		// 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		CacheUtil.removeAll(CacheUtil.myVisualGroupCache);
		CacheUtil.removeAll(CacheUtil.categoryGroupCache);
		return group.getId();
	}

	/**
	 * 关闭柜子
	 * 需要记录日志
	 * @param id
	 */
	public void closeGroup(long id) {
		if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), id,
				GroupPerm.CLOSE_GROUP))
			throw PermissionsException.GroupException;
		Group groupBean = groupDao.getGroupById(id);
		groupBean.setGroupStatus(Group.STATUS_CLOSE);
		groupDao.saveOrUpdateGroup(groupBean);
		
		// 记录日志
		String desc = LogMessage.getGroupClose(groupBean.getDisplayName());
		logService.addOperateLog(Log.ACTION_TYPE_CLOSE, desc, id, groupBean.getDisplayName());
		// 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		CacheUtil.removeAll(CacheUtil.myVisualGroupCache);
	}

	/**
	 * 重新打开柜子
	 * 需要记录日志
	 * @param id
	 */
	public void restoreGroup(long id) {
		if (!permission.isAdmin(UserUtils.getCurrentMemberId()))
			throw PermissionsException.GroupException;
		Group groupBean = groupDao.getGroupById(id);
		groupBean.setGroupStatus(Group.STATUS_NORMAL);
		groupDao.saveOrUpdateGroup(groupBean);
		
		// 记录日志
		String desc = LogMessage.getGroupOpen(groupBean.getDisplayName());
		logService.addOperateLog(Log.ACTION_TYPE_OPEN, desc, id, groupBean.getDisplayName());
		
		// 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		CacheUtil.removeAll(CacheUtil.myVisualGroupCache);
		CacheUtil.removeAll(CacheUtil.categoryGroupCache);
	}
	
	/**
	 * 获取所有的柜子
	 * @return
	 */
	public List<Group> getAllGroups() {
		if (!permission.isAdmin(UserUtils.getCurrentMemberId()))
			throw PermissionsException.GroupException;
		return groupDao.getAllGroups();
	}

	/**
	 * 修改柜子信息
	 * paiban为ExtendField1字段
	 * 需要记录日志
	 * @param group 柜子
	 * @param mark 是否检查柜子名字
	 */
	public void modifyGroup(Group group, boolean mark) {
		if (!(permission.hasGroupPerm(UserUtils.getCurrentMemberId(), group.getId(),
				GroupPerm.MODIFY_GROUP) || permission.hasCategoryPerm(UserUtils.getCurrentMemberId(), group.getCategory().getId(),
						CategoryPerm.CATEGORY_APPMANAGER))) {
			throw PermissionsException.GroupException;
		}
		// 若柜子已经关闭，不可操作
		CheckUtil.checkNormalGroup(group);
		String name = group.getName();
		// 检查柜子名字
		if (mark == true) {
			CheckUtil.checkGroupName(name);
		}
		groupDao.saveOrUpdateGroup(group);
		
		// 记录日志
		String desc2 = LogMessage.getGroupNameMod(group.getDisplayName());
		logService.addOperateLog(Log.ACTION_TYPE_MODIFY, desc2, group.getId(), group.getDisplayName());
		
		// 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		CacheUtil.removeAll(CacheUtil.myVisualGroupCache);
	}
	
	public void modifyGroup(long id, double order) throws GroupsException {
		Group groupBean = groupDao.getGroupById(id);
		if (order == groupBean.getOrder()) {
			return;
		}
		groupBean.setOrder(order);
		groupBean.setLastModifiedDate(new Timestamp(System.currentTimeMillis()));
		groupBean.setLastModifiedMemberName(UserUtils.getCommonName());
		groupDao.saveOrUpdateGroup(groupBean);

		// 记录日志
		String desc = LogMessage.getGroupSequenceMod(groupBean.getDisplayName());
		logService.addOperateLog(Log.ACTION_TYPE_MODIFY, desc, id, groupBean.getDisplayName());

		// 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		CacheUtil.removeAll(CacheUtil.categoryGroupCache);
	}
	/**
	 * 修改一个柜子的名字和描述
	 * @param id
	 * @param name
	 * @param desc
	 * @param iconId
	 * @param displayName
	 * @param mark
	 * @param paiban
	 */
	public void modifyGroup(long id, String name, String desc, long iconId, String displayName, boolean mark,String paiban) {
		if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), id,
				GroupPerm.MODIFY_GROUP)) {
			throw PermissionsException.GroupException;
		}
		Group agroup = this.getGroupById(id);
		// 若柜子已经关闭，不可操作
		CheckUtil.checkNormalGroup(agroup);

		// 检查柜子名字
		if (mark == true) {
			CheckUtil.checkGroupName(name);
		}
		agroup.setName(name);
		agroup.setDesc(desc);
		agroup.setDisplayName(displayName);
		agroup.setExtendField1(paiban);
		agroup.setGroupIcon(iconId);
		agroup.setLastModified(new Date(System.currentTimeMillis()));
		agroup.setLastModifiedMemberName(UserUtils.getAccount());
		groupDao.saveOrUpdateGroup(agroup);
		
		// 记录日志
		String desc2 = LogMessage.getGroupNameMod(agroup.getDisplayName());
		logService.addOperateLog(Log.ACTION_TYPE_MODIFY, desc2, id, agroup.getDisplayName());
		
		// 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		CacheUtil.removeAll(CacheUtil.myVisualGroupCache);
		CacheUtil.removeAll(CacheUtil.categoryGroupCache);
	}

	@Transactional
	public void modifyGroup_v2(long id, String name, String desc, long iconId, String displayName,
							   boolean mark,String paiban,Long totalSize,Long resourcesSize,
							   String creatorName,Long topCategoryId) {
		if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), id,
				GroupPerm.MODIFY_GROUP)) {
			throw PermissionsException.GroupException;
		}
		Group agroup = this.getGroupById(id);
		// 若柜子已经关闭，不可操作
		CheckUtil.checkNormalGroup(agroup);

		// 检查柜子名字
		if (mark == true) {
			CheckUtil.checkGroupName(name);
		}
		agroup.setName(name);
		agroup.setDesc(desc);
		agroup.setDisplayName(displayName);
		agroup.setExtendField1(paiban);
		agroup.setGroupIcon(iconId);
		agroup.setLastModified(new Date(System.currentTimeMillis()));
		agroup.setLastModifiedMemberName(UserUtils.getAccount());

		agroup.setCreatorName(creatorName);
		Long size=agroup.getTotalFileSize();
		if(totalSize<(agroup.getTotalFileSize()-agroup.getAvailableCapacity()))
			throw new GroupsException("柜子总容量不能小于已用容量");
		if(agroup.getApplicationId()>0){//是一个应用柜子
			Long capacityCurrent=agroup.getTotalFileSize();
			if(agroup.getTotalFileSize()<totalSize){//容量增大


			}
		}
		//编辑柜子的topCategoryId
		if(topCategoryId!=null) {
			Category category = categoryDao.getCategoryById(topCategoryId);
			if (category == null)
				throw new GroupsException("传入的分类Id没有意义");
			if (agroup.getTopCategoryId() == null) {
				if (category.getAvailableCapacity() < totalSize)
					throw new GroupsException("分类可用容量不足");
				else {
					//本来没有topcategoryId
					if (totalSize != null && totalSize > 0) {
						modifyTotalFileSize(id, totalSize);
					}
					agroup.setTopCategoryId(topCategoryId);
					category.setAvailableCapacity(category.getAvailableCapacity() - totalSize);
				}
			} else {
				//调小容量
				if (size > totalSize) {
					category.setAvailableCapacity(category.getAvailableCapacity() + (size - totalSize));

					if (totalSize != null && totalSize > 0) {
						modifyTotalFileSize(id, totalSize);
					}
				} else {//调大容量
					Long distance = totalSize - size;
					if (category.getAvailableCapacity() > distance){
						category.setAvailableCapacity(category.getAvailableCapacity() - distance);
						if (totalSize != null && totalSize > 0) {
							modifyTotalFileSize(id, totalSize);
						}
					}
					else
						throw new GroupsException("分类的容量不足，柜子容量过大");
				}
			}
			categoryDao.saveOrUpdateCategory(category);
			if(agroup.getApplicationId()>0){
				Application application=appService.getApplicationById(agroup.getApplicationId());
				application.setAvailableSpace(String.valueOf(category.getAvailableCapacity()));
				genericDao.update(application);
			}
			agroup.setTopCategoryId(topCategoryId);
		}else {
			//没有topcategory参数的话，就修改柜子的总容量
			if (totalSize != null && totalSize > 0) {
				modifyTotalFileSize(id, totalSize);
			}
		}
		agroup.setAvailableCapacity(totalSize-resourcesSize);
		agroup.setUsedCapacity(resourcesSize);
		groupDao.saveOrUpdateGroup(agroup);


		// 记录日志
		String desc2 = LogMessage.getGroupNameMod(agroup.getDisplayName());
		logService.addOperateLog(Log.ACTION_TYPE_MODIFY, desc2, id, agroup.getDisplayName());

		// 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		CacheUtil.removeAll(CacheUtil.myVisualGroupCache);
		CacheUtil.removeAll(CacheUtil.categoryGroupCache);
	}


	/**
	 * 调整柜子类型为typeId
	 * @param groupId
	 * @param typeId
	 */
	public void modifyType(long groupId, long typeId) {
		if (!permission.isAdmin(UserUtils.getCurrentMemberId()))
			throw PermissionsException.GroupException;
		Group bean = groupDao.getGroupById(groupId);
		GroupType type = groupTypeDao.getGroupTypeById(typeId);
		bean.setGroupType(type);
		groupDao.saveOrUpdateGroup(bean);
		
		// 记录日志
		GroupType typeBean = groupTypeDao.getGroupTypeById(typeId);
		String desc = LogMessage.getGroupByTypeMod(bean.getDisplayName(), typeBean.getName());
		logService.addOperateLog(Log.ACTION_TYPE_MODIFY, desc, bean.getId(), bean.getDisplayName());
		CacheUtil.removeAll(CacheUtil.categoryGroupCache);
	}

	/**
	 * 移动柜子
	 * @param id
	 * @param categoryId
	 */
	public void moveGroup(long id, long categoryId) {
		if (!(permission.hasGroupPerm(UserUtils.getCurrentMemberId(), id,
				GroupPerm.MODIFY_GROUP) || permission.hasCategoryPerm(UserUtils.getCurrentMemberId(), categoryId,
						CategoryPerm.CATEGORY_APPMANAGER)))
			throw PermissionsException.GroupException;
		Group group = groupDao.getGroupById(id);
		Category source = group.getCategory();
		Category category = categoryDao.getCategoryById(categoryId);
		// 若柜子已经关闭，不可操作
		CheckUtil.checkNormalGroup(group);
		// 若分类已经关闭，不可操作
		CheckUtil.checkNormalCategory(category);

		group.setCategory(category);
		groupDao.saveOrUpdateGroup(group);
		
		// 记录日志
		String desc = LogMessage.getGroupMove(source.getDisplayName(), group.getDisplayName(), category.getDisplayName());
		logService.addOperateLog(Log.ACTION_TYPE_MOVE, desc, id, group.getDisplayName());
		
		// 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		CacheUtil.removeAll(CacheUtil.myVisualGroupCache);
		CacheUtil.removeAll(CacheUtil.categoryGroupCache);
	}


	/**
	 * 设置柜子用途
	 *
	 * @param id
	 * @param usage 资源柜用途，分为normal, public , private
	 */
	public void setGroupUsage(long id, String usage) {
		if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), id,
				GroupPerm.MODIFY_GROUP))
			throw PermissionsException.GroupException;
		Group group = groupDao.getGroupById(id);
		// 若柜子已经关闭，不可操作
		CheckUtil.checkNormalGroup(group);
		group.setGroupUsage(usage);
		groupDao.saveOrUpdateGroup(group);
	}



	/**
	 * 获取分类及其子孙分类下的所有柜子列表
	 *
	 * @param categoryId
	 * @param start
	 * @param limit
	 * @param recursion
	 * @return
	 */
//	public List<Group> getGroupsInCategory(long categoryId, int start, int limit, boolean recursion, String account) {
//		int  status[] = null;
//		int cstatus = Category.STATUS_NORMAL;
//		List<Member> members = memberDao.getMembersByAccount(account);
//
//		if (permService.isAdmin(members.get(0).getId())) {
//			status = new int[] { Group.STATUS_NORMAL};
//			cstatus = Category.STATUS_NORMAL;
//		}
//
//		// 这里获取分类以及其所有子孙分类
//		List<Category> categoryList = new ArrayList<Category>();
//
//		Category categoryBean = categoryDao.getCategoryById(categoryId);
//		categoryList.add(categoryBean);
//
//		// 需要递归地把子孙分类下的柜子也一并取出
//		if (recursion) {
//			for (int i = 0; i < categoryList.size(); ++i) {
//				List<Category> subBeans = categoryDao.getCategoriesByParent(categoryList.get(i).getId(), cstatus);
//				for (Category subBean : subBeans) {
//					categoryList.add(subBean);
//				}
//			}
//		}
//
//		// 将所有分类下的柜子取出
//		final long[] cids = new long[categoryList.size()];
//		for (int i = 0; i < cids.length; ++i) {
//			cids[i] = categoryList.get(i).getId();
//		}
//
//		final int s[] = status;
//		return groupDao.getGroupsByCategory(cids, s, CheckUtil.reviseStart(start),
//				CheckUtil.reviseLimit(limit, PropertyUtil.getGroupDefaultPageSize()));
//	}


	/**
	 * 获取分类下的所有柜子列表
	 * @param categoryId
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<Group> getGroupsInCategory(long categoryId, int start, int limit) {
		String key = "CategoryId_"+categoryId;//id:timestamp 作为key
		//List<Group> list = (List<Group>) CacheUtil.getCache(CacheUtil.categoryGroupCache, key);
		List<Group> list=null;
		if (list == null) {
			list = groupDao.getGroupsByCategory(categoryId, start, limit);
			CacheUtil.setCache(CacheUtil.categoryGroupCache, key, list);
		}
		return list;
	}

	/**
	 * 获取分类下的所有柜子列表
	 * @param categoryId
	 * @return
	 */
	public List<Group> getGroupsInCategory(long categoryId) {
		String key = "CategoryId_"+categoryId;//id:timestamp 作为key
		List<Group> list = (List<Group>) CacheUtil.getCache(CacheUtil.categoryGroupCache, key);
		//if (list == null) {
		list = groupDao.getGroupsByCategory(categoryId);
		//CacheUtil.setCache(CacheUtil.categoryGroupCache, key, list);
		//}
		System.out.println(list);
		return list;
	}

	/**
	 * 获取分类下的所有柜子列表总数
	 * @param categoryId
	 * @return
	 */
	public Long getGroupsInCategoryTotalCount(long categoryId) {
		return genericDao.findFirst("select count(gp) from Group gp where gp.category.id = ?1",categoryId);
	}


	public Group getGroupById(long id) {
		return groupDao.getGroupById(id);
	}

	public Group getGroupByOwner(String owner) {
		return genericDao.findFirst("from Group where owner=?1",owner);
	}

//	/**
//	 * 获取某段时间内创建的柜子
//	 *
//	 * @param begin
//	 * @param end
//	 * @return
//	 * @throws ServiceException
//	 */
//	Group[] getGroups(Timestamp begin, Timestamp end)
//			throws ServiceException{
//		
//	}
//
//	/**
//	 * 获取最新柜子列表
//	 *
//	 * @param num
//	 * @return
//	 * @throws ServiceException
//	 */
//	Group[] getNewGroups(int num) throws ServiceException;

	/**
	 * 根据名字获得柜子
	 * 获取后判断若柜子已关闭，不可访问，除非是管理员
	 * @param name
	 * @return
	 */
	public Group getGroupByName(String name) {
		return groupDao.getGroupByName(name);
	}
//
//	/**
//	 * 根据柜子地址获取柜子信息
//	 *
//	 * @param addr
//	 * @return
//	 * @throws ServiceException
//	 */
//	Group getGroupByAddr(String addr) throws ServiceException;
	/**
	 * 根据柜子所在分类和显示名称获得柜子信息
	 * @param categoryId 分类
	 * @param displayName 显示名称
	 * @return
	 * @throws GroupsException
	 */
	public Group getGroupByDisplyName(long categoryId,String displayName) {
		return groupDao.getGroupByDisplyName(categoryId, displayName);
	}

	/**
	 * 根据柜子显示名称获得柜子信息
	 * @param displayName 显示名称
	 * @return
	 * @throws GroupsException
	 */
	public List<Group> getGroupByDisplyName(String displayName) {
		return groupDao.getGroupByDisplyName(displayName);
	}

	public List<Group> getGroupByDisplyNamePart(String displayName,int start,int limit) {
		return groupDao.getGroupByDisplyNamePart(displayName,start,limit);
	}
//
//	/**
//	 * 获取柜子附加信息
//	 *
//	 * @param groupId
//	 * @return
//	 * @throws ServiceException
//	 */
//	IGroupDecorationBean getGroupDecoration(long groupId)
//			throws ServiceException;
//
//	/**
//	 * 修改柜子公告
//	 *
//	 * @param groupId
//	 * @param inform
//	 * @throws ServiceException
//	 */
//	void modifyGroupInform(long groupId, String inform) throws ServiceException;
//
//	/**
//	 * 修改柜子标签
//	 *
//	 * @param groupId
//	 * @param tag
//	 * @throws ServiceException
//	 */
//	void modifyGroupTag(long groupId, String tag) throws ServiceException;
//
//	/**
//	 * 上传柜子logo
//	 *
//	 * @param groupId
//	 * @param logo
//	 * @throws ServiceException
//	 */
//	void uploadGroupLogo(long groupId, File logo) throws ServiceException;
//
//	/**
//	 * 上传柜子图标
//	 *
//	 * @param groupId
//	 * @param icon
//	 * @throws ServiceException
//	 */
//	void uploadGroupIcon(long groupId, File icon) throws ServiceException;
//
//	/**
//	 * 获取圈主信息
//	 *
//	 * @param groupId
//	 * @return
//	 * @throws ServiceException
//	 */
//	Member[] getGroupManagers(long groupId) throws ServiceException;

	/**
	 * 获取用户所在的柜子列表，包括用户所在的用户组加入的柜子
	 *
	 * @param memberId
	 * @return
	 */
	public Group[] getGroupsForMember(long memberId) {

		Map<Long, Group> groupMap = new TreeMap<Long, Group>();

		// 获取个人加入的柜子
		long[] groupIds = memberDao.getGroupsForMember(memberId);
		if (groupIds != null && groupIds.length > 0) {
			for (long id : groupIds) {
				groupMap.put(id, groupDao.getGroupById(id));
			}
		}

		// 获取个人所在组加入的柜子
		Member[] teams = grouperService.getTeamsOfMember(memberId);
		for (Member team : teams) {
			groupIds = memberDao.getGroupsForMember(team.getId());
			if (groupIds != null && groupIds.length > 0) {
				for (long id : groupIds) {
					groupMap.put(id, groupDao.getGroupById(id));
				}
			}
		}
		// 所有组
		Member everyOneTeam = grouperService.getEveryOneTeam();
		if (everyOneTeam != null) {
			groupIds = memberDao.getGroupsForMember(everyOneTeam.getId());
			if (groupIds != null && groupIds.length > 0) {
				for (long id : groupIds) {
					groupMap.put(id, groupDao.getGroupById(id));
				}
			}
		}
		// 转成列表
		List<Group> groupList = new ArrayList<Group>();
		for (Group bean : groupMap.values()) {
			groupList.add(bean);
		}
		return this.filterGroups(groupList.toArray(new Group[groupMap.size()]), memberId);
	
	}
//
//	/**
//	 * 获取柜子动态信息
//	 *
//	 * @param groupId
//	 * @param start
//	 * @param limit
//	 * @return
//	 * @throws ServiceException
//	 */
//	IResult<IGroupRecordBean> getGroupRecords(long groupId, int start, int limit)
//			throws ServiceException;
//	/**
//	 * 获得非个人文件柜的柜子动态信息
//	 * @param start
//	 * @param limit
//	 * @return
//	 * @throws ServiceException
//	 */
//	IResult<IGroupRecordBean> getImpersonalGroupRecords(int start,
//			int limit) throws ServiceException;
//	/**
//	 * 获取用户所加入的所有柜子的最新动态，每个柜子只获取最新的一条
//	 *
//	 * @return
//	 * @throws ServiceException
//	 */
//	Group[] getMyGroupsByLastRecords() throws ServiceException;
//
//	/**
//	 * 获取某一时间段的柜子动态列表
//	 *
//	 * @param groupId
//	 * @param begin
//	 * @param end
//	 * @param start
//	 * @param limit
//	 * @return
//	 * @throws ServiceException
//	 */
//	IResult<IGroupRecordBean> getGroupRecordsByDate(long groupId,
//			Timestamp begin, Timestamp end, int start, int limit)
//			throws ServiceException;
//
//	/**
//	 * 获取用户在某一时间段内创建的柜子总数
//	 *
//	 * @param creatorName
//	 * @param begin
//	 * @param end
//	 * @return
//	 * @throws ServiceException
//	 */
//	int getNumberOfGroupsByCreator(long creatorId, Timestamp begin,
//			Timestamp end) throws ServiceException;
//
//	/**
//	 * 创建柜子管理员
//	 *
//	 * @param memberId
//	 * @param groupId
//	 * @throws ServiceException
//	 */
//	void createGroupManager(long memberId, long groupId) throws ServiceException;
//
//	/**
//	 * 删除柜子管理员
//	 *
//	 * @param memberId
//	 * @param groupId
//	 * @throws ServiceException
//	 */
//	void deleteGroupManager(long memberId, long groupId) throws ServiceException;
//
//	/**
//	 * 搜索柜子
//	 *
//	 * @param query
//	 *            搜索条件
//	 * @param filter
//	 *            搜索过滤器
//	 * @param sort
//	 *            搜索排序方式
//	 * @param page
//	 *            搜索分页
//	 * @return 搜索结果，包含总数和柜子列表
//	 * @throws ServiceException
//	 */
//	SearchResult<Group> searchGroups(SearchQuery<Group> query,
//			SearchFilter filter, com.dcampus.ztools.zsearch.searcher.Sort sort,
//			Page page) throws ServiceException;

	/**
	 * 修改柜子资源空间大小
	 *
	 * @param id
	 * @param filesize
	 *            单位为K，若取值负数，则表示不设置，柜子使用默认值
	 * @throws GroupsException
	 */
	public void modifyTotalFileSize(long id, long filesize) {
		
		if (!permission.isAdmin(UserUtils.getCurrentMemberId())
				&& !permission.hasGroupPerm(UserUtils.getCurrentMemberId(), id, GroupPerm.MANAGE_GROUP))
			throw PermissionsException.GroupException;
		Group group = groupDao.getGroupById(id);
		CheckUtil.checkNormalGroup(group);

		long totalFileSize = 0;
		GroupType grouptype = group.getGroupType();
		if (grouptype == null || grouptype.getId() <= 0) {
			totalFileSize = PropertyUtil.getGroupResourceSingleFileSize();
		}
		totalFileSize = grouptype.getTotalFileSize();


		// filesize大于或等于0，同时跟propertyUtil中配置的不同，表示需要设置值
		if (filesize >= 0 && filesize != totalFileSize) {
			Map<String, String> map = group.getExternInfo();
			if (map == null)
				map = new HashMap<String, String>();
			map.put(Group.ExternInfo.TotalFileSize.toString(), filesize + "");
			group.setExternInfo(map);
			group.setTotalFileSize(filesize);
		}
		// filesize小于0，表示使用默认，将已设置的记录删除
		else {

			Map<String, String> map = group.getExternInfo();
			if (map != null) {
				map.remove(Group.ExternInfo.TotalFileSize.toString());
				group.setExternInfo(map);
				//编辑柜子容量，新增的内容
				if(filesize>0)
					group.setTotalFileSize(filesize);
			}
		}

		this.saveOrUpdateGroup(group);
		// 记录日志
		if (filesize != totalFileSize) {
			String desc = LogMessage.getGroupSizeMod(group.getDisplayName());
			logService.addOperateLog(Log.ACTION_TYPE_MODIFY, desc, id, group.getDisplayName());
		}
	}

	/**
	 * 修改柜子单文件大小限制
	 *
	 * @param id
	 * @param filesize 单位为K，若取值负数，则表示不设置，柜子使用默认值
	 * @throws GroupsException
	 */
	public void modifySingleFileSize(long id, long filesize) {
		
		if (!permission.isAdmin(UserUtils.getCurrentMemberId())
				&& !permission.hasGroupPerm(UserUtils.getCurrentMemberId(), id, GroupPerm.MANAGE_GROUP))
			throw PermissionsException.GroupException;
		Group group = groupDao.getGroupById(id);
		CheckUtil.checkNormalGroup(group);

		long singleFileSize = 0;
		GroupType grouptype = group.getGroupType();
		if (grouptype == null || grouptype.getId() <= 0) {
			singleFileSize = PropertyUtil.getGroupResourceSingleFileSize();
		}
		singleFileSize = grouptype.getSingleFileSize();


		// filesize大于或等于0，同时跟propertyUtil中配置的不同，表示需要设置值
		if (filesize >= 0 && filesize != singleFileSize) {
			Map<String, String> map = group.getExternInfo();
			if (map == null)
				map = new HashMap<String, String>();
			map.put(Group.ExternInfo.SingleFileSize.toString(), filesize + "");
			group.setExternInfo(map);
		}
		// filesize小于0，表示使用默认，将已设置的记录删除
		else {
			Map<String, String> map = group.getExternInfo();
			if (map != null) {
				map.remove(Group.ExternInfo.SingleFileSize.toString());
				group.setExternInfo(map);
			}
		}

		this.saveOrUpdateGroup(group);
		
		// 记录日志
		if (filesize != singleFileSize) {
			String desc = LogMessage.getGroupFileSizeMod(group.getDisplayName());
			logService.addOperateLog(Log.ACTION_TYPE_MODIFY, desc, id, group.getDisplayName());
		}
	}
//
//	/**
//	 * 重建柜子索引
//	 *
//	 * @throws ServiceException
//	 */
//	void rebuildGroupIndex() throws ServiceException;
//
//	/**
//	 * 推荐柜子
//	 *
//	 * @param groupId
//	 * @throws ServiceException
//	 */
//	void recommandGroup(long groupId) throws ServiceException;
//
//	/**
//	 * 取消推荐柜子
//	 *
//	 * @param groupId
//	 * @throws ServiceException
//	 */
//	void unrecommandGroup(long groupId) throws ServiceException;
//
//	/**
//	 * 获取推荐柜子列表
//	 *
//	 * @throws ServiceException
//	 */
//	Group[] getRecommandedGroups() throws ServiceException;
//
//	/**
//	 * 创建柜子类型
//	 *
//	 * @param bean
//	 * @throws ServiceException
//	 */
//	void createGroupType(IGroupTypeBean bean) throws ServiceException;
//
//	/**
//	 * 删除柜子类型
//	 *
//	 * @param id
//	 * @throws ServiceException
//	 */
//	void deleteGroupType(long id) throws ServiceException;
//
//	/**
//	 * 修改柜子类型
//	 *
//	 * @param newBean
//	 * @throws ServiceException
//	 */
//	void modifyGroupType(IGroupTypeBean newBean) throws ServiceException;
//
//	/**
//	 * 获取柜子类型列表
//	 *
//	 * @param start
//	 * @param limit
//	 * @return
//	 * @throws ServiceException
//	 */
//	IResult<IGroupTypeBean> getGroupTypes(int start, int limit)
//			throws ServiceException;
//
	/**
	 * 根据类型id获得类型信息
	 * @param id
	 * @return
	 */
	public GroupType getGroupTypeById(long id) {
		return groupTypeDao.getGroupTypeById(id);
	}

	/**
	 * 根据名字获取柜子类型信息
	 * @param name 类型的名字
	 * @return
	 */
	public GroupType getGroupTypeByName(String name) {
		return groupTypeDao.getGroupTypeByName(name);
	}
//
//	/**
//	 * 随机取出num个柜子
//	 *
//	 * @param num
//	 * @return
//	 * @throws ServiceException
//	 */
//	Group[] getRandomGroups(int num) throws ServiceException;
//
//
//	void publishAlbum(long resourceId, long groupId, long parentId, String contentType)
//			throws ServiceException;
//	
//	void publishAlbum(long resourceId, long groupId, long parentId, String contentType, String paiban)
//			throws ServiceException;


	
	



	
	/**
	 * 获得某一层分类下面的最大顺序号
	 * @param categoryId
	 * @return
	 * @throws GroupsException
	 */
	public double getGroupMaxOrder(long categoryId) {
		 return groupDao.getMaxOrderInCategory(categoryId);
	}
	



	/**
	 * 删除柜子
	 * 删除一个柜子所涉及的操作十分的多，包括以下：
	 * 1、柜子信息的删除
	 * 2、柜子用户的删除
	 * 3、资源的删除
	 * 4、权限的删除
	 * 5、关注的删除
	 * @param groupId 柜子id
	 */
	public void deleteGroup(long groupId) {
		if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), groupId,
				GroupPerm.DELETE_GROUP))
			throw PermissionsException.GroupException;
		Group agroup = groupDao.getGroupById(groupId);
		if (agroup != null && agroup.getId() >= 0) {
			// 删除柜子用户绑定
			memberDao.deleteMemberGroupBinding(groupId);
			// 删除权限
			permService.removeGroupPermission(groupId);
			// 删除关注
			watchDao.deleteWatchByGroupId(groupId, Watch.WATCH_TYPE_GROUP);
			groupManagerDao.deleteGroupManagerByGroup(groupId);
			long availableCapacity1 =getAvailableCapacity();
			System.out.println("删除个人柜子前系统可用空间大小为"+availableCapacity1);
			//编辑分类容量
			if(agroup.getTopCategoryId()!=null){
				Category category=categoryDao.getCategoryById(agroup.getTopCategoryId());
				category.setAvailableCapacity(category.getAvailableCapacity()+agroup.getTotalFileSize());
	            categoryDao.saveOrUpdateCategory(category);
			}
			// 记录日志
			String desc = LogMessage.getGroupDelete(agroup.getDisplayName());
			logService.addOperateLog(Log.ACTION_TYPE_DELETE, desc, groupId, agroup.getDisplayName());
			groupDao.deleteGroup(agroup);

			// 清除myGroupsCache
			CacheUtil.removeAll(CacheUtil.myGroupsCache);
			CacheUtil.removeAll(CacheUtil.myVisualGroupCache);
			CacheUtil.removeAll(CacheUtil.categoryGroupCache);
			long availableCapacity =getAvailableCapacity();
			System.out.println("系统可用空间大小为"+availableCapacity);
			System.out.println("我删除的的柜子空间大小为"+agroup.getTotalFileSize());
		}
	}

	public void  deleteGroup_v2(long groupId) {
		if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), groupId,
				GroupPerm.DELETE_GROUP))
			throw PermissionsException.GroupException;
		Group agroup = groupDao.getGroupById(groupId);
		if (agroup != null && agroup.getId() >= 0) {
			//删除柜子的时候在应用中对应的空间增大
			//删除一个柜子
			Application application=appService.getApplicaitonByCategoryId(agroup.getCategory().getId());
			if(application!=null){
				long availCurrent=Long.parseLong(application.getAvailableSpace());
				application.setAvailableSpace(String.valueOf(availCurrent+agroup.getTotalFileSize()));
			}else {
				//删除柜子的时候在域中对对应空间增大
				String hql="from "+DomainCategory.class.getName()+" as dc where dc.category.id=?";
				List<DomainCategory>  dcs=genericDao.findAll(hql,agroup.getCategory().getId());
				if(dcs!=null && dcs.size()!=0){
					DomainCategory dc=dcs.get(0);
					long availCurrentdc=dc.getAvailableCapacity();
					dc.setAvailableCapacity(availCurrentdc+agroup.getTotalFileSize());
					//删除capacity分配
					hql="from "+CapacityDistribution.class.getName()+" as cd where cd.toGroup.id=?";
					CapacityDistribution cd= (CapacityDistribution) genericDao.findAll(hql,agroup.getId()).get(0);
				    if(cd!=null)
				    	genericDao.delete(cd);
				}
				if(agroup.getTopCategoryId()!=null){
					Category category=categoryDao.getCategoryById(agroup.getTopCategoryId());
					if(category!=null&&category.getTotalCapacity()>agroup.getTotalFileSize()){
						category.setAvailableCapacity(category.getAvailableCapacity()+agroup.getTotalFileSize());
						categoryDao.saveOrUpdateCategory(category);
					}
				}
			}

			// 删除柜子用户绑定
			memberDao.deleteMemberGroupBinding(groupId);
			// 删除权限
			permService.removeGroupPermission(groupId);
			// 删除关注
			watchDao.deleteWatchByGroupId(groupId, Watch.WATCH_TYPE_GROUP);
			groupManagerDao.deleteGroupManagerByGroup(groupId);

			// 记录日志
			String desc = LogMessage.getGroupDelete(agroup.getDisplayName());
			logService.addOperateLog(Log.ACTION_TYPE_DELETE, desc, groupId, agroup.getDisplayName());
			groupDao.deleteGroup(agroup);

			// 清除myGroupsCache
			CacheUtil.removeAll(CacheUtil.myGroupsCache);
			CacheUtil.removeAll(CacheUtil.myVisualGroupCache);
			CacheUtil.removeAll(CacheUtil.categoryGroupCache);
		}
	}

	public void  deleteGroup_v3(long groupId) {
		if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), groupId,
				GroupPerm.DELETE_GROUP))
			throw PermissionsException.GroupException;
		Group agroup = groupDao.getGroupById(groupId);
		if (agroup != null && agroup.getId() >= 0) {
			//删除柜子的时候在应用中对应的空间增大
			//删除一个柜子
			Application application=appService.getApplicaitonByCategoryId(agroup.getCategory().getId());
			if(application!=null){
				Category appCategory=categoryService.getCategoryById(application.getCategoryId());
				//同步
//				if(appCategory.getAvailableCapacity()!=null&&appCategory.getAvailableCapacity()==Long.parseLong(application.getAvailableSpace())){
				//更新关联应用可用容量
				appCategory.setAvailableCapacity(appCategory.getAvailableCapacity()+agroup.getTotalFileSize());
				categoryDao.saveOrUpdateCategory(appCategory);
//				}
				long availCurrent=Long.parseLong(application.getAvailableSpace());
				application.setAvailableSpace(String.valueOf(availCurrent+agroup.getTotalFileSize()));

			}else {
				//删除柜子的时候在域中对对应空间增大
				String hql="from "+DomainCategory.class.getName()+" as dc where dc.category.id=?";
				List<DomainCategory>  dcs=genericDao.findAll(hql,agroup.getCategory().getId());
				if(dcs!=null && dcs.size()!=0){
					DomainCategory dc=dcs.get(0);
					long availCurrentdc=dc.getAvailableCapacity();
					dc.setAvailableCapacity(availCurrentdc+agroup.getTotalFileSize());
					//删除capacity分配
					hql="from "+CapacityDistribution.class.getName()+" as cd where cd.toGroup.id=?";
					CapacityDistribution cd= (CapacityDistribution) genericDao.findAll(hql,agroup.getId()).get(0);
					if(cd!=null)
						genericDao.delete(cd);
				}
				if(agroup.getTopCategoryId()!=null){
					Category category=categoryDao.getCategoryById(agroup.getTopCategoryId());
					if(category!=null&&category.getTotalCapacity()>agroup.getTotalFileSize()){
						category.setAvailableCapacity(category.getAvailableCapacity()+agroup.getTotalFileSize());
						categoryDao.saveOrUpdateCategory(category);
					}
				}
			}

			// 删除柜子用户绑定
			memberDao.deleteMemberGroupBinding(groupId);
			// 删除权限
			permService.removeGroupPermission(groupId);
			// 删除关注
			watchDao.deleteWatchByGroupId(groupId, Watch.WATCH_TYPE_GROUP);
			groupManagerDao.deleteGroupManagerByGroup(groupId);

			// 记录日志
			String desc = LogMessage.getGroupDelete(agroup.getDisplayName());
			logService.addOperateLog(Log.ACTION_TYPE_DELETE, desc, groupId, agroup.getDisplayName());
			groupDao.deleteGroup(agroup);

			// 清除myGroupsCache
			CacheUtil.removeAll(CacheUtil.myGroupsCache);
			CacheUtil.removeAll(CacheUtil.myVisualGroupCache);
			CacheUtil.removeAll(CacheUtil.categoryGroupCache);
		}
	}

//	/**
//	 * 获得所有柜子
//	 * @return
//	 * @throws ServiceException
//	 */
//	Group[] getAllGroups() throws ServiceException;
//
//
//	/**
//	 * 修改分类顺序号
//	 * @param id
//	 * @param order
//	 * @throws ServiceException
//	 */
//	void modifyGroup(long id, double order) throws ServiceException;
//

//
//	void modifyGroup(long id, Group.Usage usage) throws ServiceException;
//
//	IResult<IGroupRecordBean> getGroupRecords(int start, int limit) throws ServiceException;
//
	public List<Group> getMyVisualGroups(long categoryId, long memberId) {
		List<Group> groups = this.getMyVisualGroupsWithoutPersonGroup(categoryId, memberId);
		String personGroupName = "" + memberId;
		// 添加个人文件柜
		Group personGroup = this.getGroupByName(personGroupName);
		if (categoryId == 0 || categoryId == personGroup.getCategory().getId()) {
			groups.add(personGroup);
		}
		return groups;
	}


	/**
	 * 批量删除图标
	 * @param ids
	 */
	public void deleteIcons(Collection<Long> ids){
		for (long id : ids) {
			this.deleteIcon(id);
		}
	}
	/**
	 * 根据id删除图标
	 * @param id
	 */
	public void deleteIcon(long id){
		if (!permission.isAdmin(UserUtils.getCurrentMemberId()))
			throw PermissionsException.GroupException;
		GroupIcon icon = this.getGroupIconById(id);
		genericDao.delete(icon);
	}

	/**
	 * 新建或者保存柜子图标
	 * @param icon
	 */
	public void saveOrUpdateGroupIcon(GroupIcon icon) {
		if (!permission.isAdmin(UserUtils.getCurrentMemberId()))
			throw PermissionsException.GroupException;
		if(icon.getId() != null && icon.getId() > 0){
			genericDao.update(icon);
		} else {
			genericDao.save(icon);
		}
	}
	/**
	 * 根据id获取柜子图标
	 * @param id
	 * @return
	 */
	public GroupIcon getGroupIconById(long id){
		return genericDao.get(GroupIcon.class, id);
	}

	/**
	 * 获取所有柜子图标
	 * @return
	 */
	public List<GroupIcon> getAllGroupIcons() {
		return genericDao.findAll("from GroupIcon order by id desc");
	}
//
//
//	
//	boolean checkGroupManager(long groupId, long memberId) throws ServiceException;
//	
//	void reBuildTrees() throws ServiceException ;
//
//	
	/**
	 * 根据分类获取我可见的柜子，不包括个人文件柜
	 * @param categoryId 分类id
	 * @param memberId 我的memberId
	 * @return
	 */
	public List<Group> getMyVisualGroupsWithoutPersonGroup(long categoryId, long memberId) {
		String key = UserUtils.getCurrentMemberId() + "_" + categoryId;
		List<Group> groups = (List<Group>) CacheUtil.getCache(CacheUtil.myVisualGroupCache, key);
		if (groups == null) {
			groups = new ArrayList<Group>();
			String personGroupName = "" + memberId;
			Group personGroup = this.getGroupByName(personGroupName);

			boolean admin = permService.isAdmin(memberId);
			Category personCategory = categoryDao.getCategoriesByName(PropertyUtil.getDefaultPersonGroupCategory()).get(0);
			// 获取信息发布的分类
			long[] cids = null;
			String defaultCmsGroup = PropertyUtil.getDefaultCmsGroup();
			if (defaultCmsGroup != null && defaultCmsGroup.length() > 0) {
				String[] defaultCmsGroups = defaultCmsGroup.split(":");
				String defaultCmsCategory = defaultCmsGroups[0];
				Category cmsCategory = categoryDao.getCategoriesByName(defaultCmsCategory).get(0);
				cids = new long[] { cmsCategory.getId(), personCategory.getId() };
			} else {
				cids = new long[] { personCategory.getId() };
			}
			Map<Long, Group> gmap = new HashMap<Long, Group>();
			List<Group> allGroups = groupDao.getGroupsNotInCategory(cids);

			for (Group group : allGroups) {
				if (group.getGroupStatus() == Group.STATUS_NORMAL
						&& group.getId().longValue() != personGroup.getId().longValue()) {
					groups.add(group);
				}
				gmap.put(group.getId(), group);
			}

			if (!admin) {
				groups.clear();
				List<Group> groups2 = new ArrayList<Group>();
				// 获得所在组
				Member[] ms = grouperService.getTeamsOfMember(memberId);
				List<OldPerm> perms = this.getMemberPerms(ms, memberId);
				for (OldPerm p : perms) {
					if (p.getPermType() == OldPerm.PERM_TYPE_GROUP) {
						if (gmap.containsKey(p.getTypeId())) {
							groups2.add(gmap.get(p.getTypeId()));
						}
					}
					if (p.getPermType() == OldPerm.PERM_TYPE_CATEGORY) {
						groups2.addAll(groupDao.getGroupsByPath("/c" + p.getTypeId() + "/"));
					}
				}

				// 获取用户所在的柜子列表，包括用户所在的用户组加入的柜子
				Group[] groupBeans = this.getGroupsForMember(memberId);

				Set<Long> g = new HashSet<Long>();
				g.add(personGroup.getId());
				for (Group group : groups2) {
					if (group.getGroupStatus() == Group.STATUS_NORMAL) {
						if (!g.contains(group.getId())) {
							groups.add(group);
						}
						g.add(group.getId());
					}
				}
				for (Group group : groupBeans) {
					if (group.getGroupStatus() == Group.STATUS_NORMAL) {
						if (!g.contains(group.getId())) {
							groups.add(group);
						}
						g.add(group.getId());
					}
				}
			}
			CacheUtil.setCache(CacheUtil.myVisualGroupCache, key, groups);
		}
		return groups;
	}
	
	private List<OldPerm> getMemberPerms(Member[] teams, long memberId) {
		List<Long> mids = new ArrayList<Long>();
		mids.add(memberId);
		for (Member m : teams) {
			mids.add(m.getId());
		}
		Member b = grouperService.getEveryOneTeam();
		if (b != null) {
			mids.add(b.getId());
		}int length = mids.size();
		long[] ids = new long[length];
		for (int i = 0; i < length; i++) {
			ids[i] =  mids.get(i);
		}
		return permDao.getMemberCategoryOrGroupPerms(ids);

	}
//
//	/*******************************************************来自之前memberManager的方法********************************************/
//	/**
//	 *将用户加入柜子，没有之前的审核处理
//	 * 
//	 * @param memberId
//	 * @param groupId
//	 * @return
//	 * @throws ServiceException
//	 */
//	public boolean joinGroup(long memberId, long groupId) throws ServiceException {
//		CheckUtil.checkNormalGroup(group);
//	}

	/**
	 * 绑定用户到柜子中，用户将马上拥有柜子的权限
	 * 
	 * @param memberId
	 * @param groupId
	 * @throws GroupsException
	 */
	public void bindMemberToGroup(long memberId, long groupId) throws GroupsException {
		Group group = groupDao.getGroupById(groupId);		
		CheckUtil.checkNormalGroup(group);
		boolean bind = false;
		bind = memberDao.findMemberInGroup(memberId, groupId);
		if (!bind) {
			memberDao.createMemberGroupBinding(memberId, groupId, true);
		}
		this.statGroupMembers(groupId);
		
		// 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		CacheUtil.removeAll(CacheUtil.myVisualGroupCache);
		CacheUtil.removeAll(CacheUtil.memberTeamCache);
	}

	/**
	 * 移除用户柜子绑定
	 * 
	 * @param memberId
	 * @param groupId
	 * @throws GroupsException
	 */
	public void removeMemberGroupBinding(long memberId, long groupId) throws GroupsException {
		Group group = groupDao.getGroupById(groupId);		
		CheckUtil.checkNormalGroup(group);
		memberDao.deleteMemberGroupBinding(memberId, groupId);
		this.statGroupMembers(groupId);
		// 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		CacheUtil.removeAll(CacheUtil.myVisualGroupCache);
		CacheUtil.removeAll(CacheUtil.memberTeamCache);
	}
////
////	/**
////	 * 获取柜子的申请用户列表
////	 * 
////	 * @param groupId
////	 * @param start
////	 * @param limit
////	 * @return
////	 * @throws ServiceException
////	 */
////	IResult<Member> getRequestingMembersInGroup(long groupId, int start,
////			int limit) throws ServiceException;
////
	/**
	 * 获取加入柜子的用户
	 * 
	 * @param groupId
	 * @param start
	 * @param limit
	 * @return
	 * @throws GroupsException
	 */
	public List<Member> getMembersInGroup(long groupId, int start, int limit) throws GroupsException {
		long[] memberIds = memberDao.getMembersInGroup(groupId, CheckUtil.reviseStart(start),
				CheckUtil.reviseLimit(limit, PropertyUtil.getGroupMemberDefaultPageSize()));
		List<Member> list = new ArrayList<Member>();
		if (memberIds == null || memberIds.length <=0) {
			return null;
		}
		for (long id : memberIds) {
			Member m = memberDao.getMemberById(id);
			if (m != null) {
				list.add(memberDao.getMemberById(id));
			}
		}
		return list;
	}

	public List<Member> getMembersInGroupTotal(long groupId) throws GroupsException {
		long[] memberIds = memberDao.getMembersInGroupTotal(groupId);
		List<Member> listTotal = new ArrayList<Member>();
		if (memberIds == null || memberIds.length <=0) {
			return null;
		}
		for (long id : memberIds) {
			Member m = memberDao.getMemberById(id);
			if (m != null) {
				listTotal.add(memberDao.getMemberById(id));
			}
		}
		return listTotal;
	}
////
////	/**
////	 * 判断用户是否是柜子会员
////	 * 
////	 * @param memberId
////	 * @param groupId
////	 * @return
////	 * @throws ServiceException
////	 */
////	boolean checkMemberInGroup(long memberId, long groupId)
////			throws ServiceException;
//	
	/********************************************************收藏相关方法开始******************************************************************/
	/**
	 * 添加关注
	 * @param targetId 被收藏目标id
	 * @param type 收藏类型，分为group,thread
	 */
	public void addWatch(long memberId, long targetId, String type) {
		Watch watch = watchDao.getWatch(memberId, targetId, type);
		if (watch == null || watch.getId() <= 0) {
			watch = new Watch();
			watch.setMemberId(memberId);
			watch.setTargetId(targetId);
			watch.setWatchStatus(Watch.WATCH_STATUS_INACTIVE);
			watch.setWatchType(type);
			watchDao.saveOrUpdateWatch(watch);
			
			String desc = LogMessage.getWatchAdd(type);
			logService.addOperateLog(Log.ACTION_TYPE_ADD, desc, watch.getId(), "");
		}
	}

	/**
	 * 删除关注
	 * @param targetId 被收藏目标id
	 * @param type
	 */
	public void deleteWatch(long memberId, long targetId, String type) {
		Watch watch = watchDao.getWatch(memberId, targetId, type);
		if (watch != null && watch.getId() >= 0) {
			watchDao.deleteWatch(watch);
		}
		
		//记录日志
		String desc = LogMessage.getWatchDelete(type);
		logService.addOperateLog(Log.ACTION_TYPE_DELETE, desc, targetId, "");
	}

	/**
	 * 批量删除关注
	 * 
	 * @param id2
	 */
	public void deleteWatch(Long[] id2) {
		Watch w = null;
		for (long id : id2) {
			w = watchDao.getWatchById(id);
			if (w != null && w.getId() >= 0) {
				watchDao.deleteWatch(w);
			}
		}
	}

	/**
	 * 获取关注列表
	 * 需要记录日志
	 * @param memberId 用户id
	 * @param type 被收藏目标id
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<Watch> getWatchesByMember(long memberId, String type, int start, int limit) {
		return watchDao.getWatchsByMember(memberId, type, start, limit);
	}
	/**
	 * 获取关注
	 * @param memberId 用户id
	 * @param targetId 被收藏目标id
	 * @param type 收藏类型，分为group,thread
	 * @return
	 */
	public Watch getWatch(long memberId, long targetId, String type) {
		return watchDao.getWatch(memberId, targetId, type);
	}
	/********************************************************收藏相关方法结束******************************************************************/
	/***********************************************废弃的或者认为没用到的方法******************************************************/
	/**
	 * 设置柜子是否需要审核用户加入
	 * @param id
	 * @param audit
	 */
	@Deprecated
	public void setAuditMember(long id, int audit) {
		if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), id,
				GroupPerm.MODIFY_GROUP))
			throw PermissionsException.GroupException;
		Group group = groupDao.getGroupById(id);
		
		// 若柜子已经关闭，不可操作
		CheckUtil.checkNormalGroup(group);
		group.setMemberAudit(audit);
		groupDao.saveOrUpdateGroup(group);
	}
	/**
	 * 设置柜子是否需要审核新帖
	 * 认为没用到，暂时没实现
	 * @param id
	 * @param audit
	 */
	@Deprecated
	 public void setAuditPost(long id, int audit) {
		
	}
	
	/**
	 * 已废弃，请使用getGroupsByCreateDate
	 * @param begin
	 * @param end
	 * @return
	 */ 
	@Deprecated
	public List<Group> getGroupsByTime(Timestamp begin, Timestamp end) {
		if (end.before(begin))
			return new ArrayList<Group>();

		return groupDao.getGroupsByCreateDate(begin, end);
	}
	public void saveOrUpdateGroupType(GroupType bean, long memberId) {
		if (memberId != OldPerm.SYSTEMADMIN_MEMBER_ID) {
			if (!permission.isAdmin(memberId))
				throw PermissionsException.GroupException;
		}
		groupTypeDao.saveOrUpdateGroupType(bean);
	}
	
	public void saveOrUpdateGroupType(GroupType bean) {
		long memberId = UserUtils.getCurrentMemberId();
		this.saveOrUpdateGroupType(bean, memberId);
	}

	/**
	 * 获取系统已经分配的容量，参数为需要排除的分类id
	 * @param categoryIds
	 * @return
	 * @throws GroupsException
	 */
	public long getAllocatedSpaceSize(long[] categoryIds) throws GroupsException {
		// 获取共有柜子
		return groupDao.getAllocatedSpaceSize(categoryIds);
	}
	
	public IBaseBean<Long>[] trees(long memberId, long parentId, boolean containPersonGroup) {
		
		String key = String.valueOf(memberId) + containPersonGroup + Long.toString(parentId);
		IBaseBean<Long>[] trees = (IBaseBean<Long>[]) CacheUtil.getCache(CacheUtil.myGroupsCache, key);
		if (trees == null) {
			Map<Long, List<IBaseBean<Long>>> map = null;
			map = new TreeMap<Long, List<IBaseBean<Long>>>();
			List<Group> groups = this.getMyVisualGroupsWithoutPersonGroup(0L, memberId);
			String personGroupName = "" + memberId;

			Group personGroup = this.getGroupByName(personGroupName);
			if (containPersonGroup) {
				if (parentId == 0 || parentId == personGroup.getCategory().getId()) {
					groups.add(personGroup);
				}
			} else {
				groups.remove(personGroup);
			}
			
			classify(map, groups.toArray(new Group[0]));
			sortMapByOrder(map);
			List<IBaseBean<Long>> list_temp = map.get(parentId);

			trees = list_temp == null ? (new IBaseBean[0])
					: list_temp.toArray(new IBaseBean[list_temp.size()]);
			CacheUtil.setCache(CacheUtil.myGroupsCache, key, trees);
		}

	
		return trees;
	}
	
	/**
	 * 对柜子进行归类
	 * @param map
	 * @param beans
	 */
	private void classify(Map<Long, List<IBaseBean<Long>>> map, Group[] beans) {
		for (Group bean : beans) {
			List<IBaseBean<Long>> list = map.get(bean.getCategory().getId());
			if (list == null) {
				list = new ArrayList<IBaseBean<Long>>();
				map.put(bean.getCategory().getId(), list);
			}
			list.add(bean);
		}

		// 追溯上一层，即对分类进行归类
		List<Category> list = new ArrayList<Category>();
		for (Group bean : beans) {
			if (bean!= null && bean.getCategory().getId() > 0 && !find(bean.getCategory().getId(), list)) {
				Category categoryBean = bean.getCategory();
				list.add(categoryBean);
			}
		}

		if (list.size() > 0) {
			classify(map, list);
		}
	}
	
	
	/**
	 * 对分类进行归类
	 * @param map
	 * @param categories
	 */
	private void classify(Map<Long, List<IBaseBean<Long>>> map, List<Category> categories) {
		for (Category bean : categories) {
			if (bean!= null) {
				List<IBaseBean<Long>> list = map.get(bean.getParentId());

				if (list == null) {
					list = new ArrayList<IBaseBean<Long>>();
					map.put(bean.getParentId(), list);
				}

				if (!find(bean, list)) {
					list.add(bean);

				}
			}
		}

		List<Category> list = new ArrayList<Category>();
		for (Category bean : categories) {
			if (bean!= null && bean.getParentId() != 0 && !find(bean.getParentId(), list)) {
				Category categoryBean = categoryDao.getCategoryById(bean.getParentId());
				list.add(categoryBean);
			}
		}

		if (list.size() > 0) {
			classify(map, list);
		}
	}
	/**
	 * list中是否有分类
	 * @param categoryId
	 * @param list
	 * @return
	 */
	private boolean find(long categoryId, List<Category> list) {
		for (Category bean : list) {
			if (bean!= null && bean.getId() == categoryId)
				return true;
		}
		return false;
	}
	
	/**
	 * list中是否有basebean
	 * @param bean
	 * @param list
	 * @return
	 */
	private boolean find(IBaseBean<Long> bean, List<IBaseBean<Long>> list) {
		for (IBaseBean<Long> baseBean : list) {
			// 同为GroupBean，且id相同
			if (bean instanceof Group && baseBean instanceof Group
					&& bean.getId().longValue() == baseBean.getId().longValue()) {
				return true;
			}
			// 同为CategoryBean，且id相同
			if (bean instanceof Category && baseBean instanceof Category
					&& bean.getId().longValue() == baseBean.getId().longValue()) {
				return true;
			}
		}
		return false;
	}
	/**
	 * 按照order排序
	 * @param map
	 */
	private void sortMapByOrder(Map<Long, List<IBaseBean<Long>>> map) {
		for (Map.Entry<Long, List<IBaseBean<Long>>> entry : map.entrySet()) {
			List<IBaseBean<Long>> value = entry.getValue();
			List<IBaseBean<Long>> list = new ArrayList<IBaseBean<Long>>();
			List<Group> groups = new ArrayList<Group>();
			List<Category> categorys = new ArrayList<Category>();
			for (IBaseBean<Long> bean : value) {
				if (bean instanceof Group) {
					groups.add((Group) bean);
				} else {
					categorys.add((Category) bean);
				}
			}
			Collections.sort(groups, Group.COMPARE_SORT_ORDER);
			Collections.sort(categorys, Category.COMPARE_SORT_ORDER);
			list.addAll(categorys);
			list.addAll(groups);
			map.put(entry.getKey(), list);
		}
	}
	
	public  Group[] filterGroups(Group[] beans, long memberId) {
		if (beans == null)
			return new Group[0];

		// admin不需要过滤
		if (permService.isAdmin(memberId)) {
			return beans;
		}

		List<Group> list = new ArrayList<Group>();
		for (Group bean : beans) {
			// 如果柜子处于关闭状态，不可见
			if (bean.getGroupStatus() != Group.STATUS_NORMAL)
				continue;

			// 柜子管理员不需要过滤
			if (permService.isGroupManager(memberId, bean.getId())) {
				list.add(bean);
				continue;
			}

			// 非柜子管理员，必须满足usage!=private或是柜子会员才可见
			if (bean.getGroupUsage() != Group.USAGE_PRIVATE
					|| grouperService.checkMemberInGroup(memberId, bean.getId())) {

				list.add(bean);
			}
		}

		return list.toArray(new Group[list.size()]);
	}

	public  List<Group> filterGroups(List<Group> list, long memberId) {
		if (list == null) {
			list = new ArrayList<Group>();
			return list;
		}

		Group[] beans = filterGroups(
				list.toArray(new Group[list.size()]), memberId);
		// 清空list
		list.clear();
		// 重新填装list
		for (Group bean : beans) {
			list.add(bean);
		}
		return list;
	}

	/**
	 * 根据appId获取柜子
	 * @param appId
	 * @return
	 */
	public List<Group> getGroupsByApp(Long appId) {
		return groupDao.getGroupsByApp(appId);
	}
	
	/**
	 * 更新柜子成员数
	 *
	 * @param groupId
	 */
	public  void statGroupMembers(long groupId) {
		int memberCount = (int) memberDao.getNumberOfAllMembersInGroup(groupId);

		Group statBean = groupDao.getGroupById(groupId);
		statBean.setMemberCount(memberCount);
		groupDao.saveOrUpdateGroup(statBean);
	}
	/**
	 * 更新资源拥有者
	 * @param oldMemberId 
	 * @param newMemberId
	 */
	public void modifyResourceOwner(long oldMemberId, long newMemberId) {
		// TODO Auto-generated method stub
		if (oldMemberId == newMemberId)
			return;
		Member memberBean = memberDao.getMemberById(newMemberId);

		int start = 0;
		int limit = 100;

		while (true) {

			List<GroupResource> resourceBeans = groupResourceDao.getResourcesByMember(oldMemberId, start, limit);

			if (resourceBeans == null || resourceBeans.size() <= 0) {
				break;
			} else {
				for (GroupResource bean : resourceBeans) {
					boolean flag = false;
					Group group = groupDao.getGroupById(bean.getGroup().getId());
					if (group == null) {
						this.forceDeleteResource(bean);
					} else {
						flag = true;
					}
					if (flag) {
						bean.setCreatorId(newMemberId);
						bean.setMemberName(memberBean.getName());
						groupResourceDao.saveOrUpdateResource(bean);
						// TODO
						// 转移物理文件
						try {
							String path = FilePathUtil.getFileFullPath(oldMemberId, bean.getFilePath());
							File srcFile = new File(new URI(path));
							path = FilePathUtil.getFileFullPath(newMemberId, bean.getFilePath());
							File destFile = new File(new URI(path));
							if(destFile.exists())
								FileUtils.moveFile(srcFile, destFile);
						} catch (Exception exx) {
							log.error(exx, exx);
						}
					}
				}
			}
			start += limit;
		}
	}
	private void forceDeleteResource(GroupResource bean) {
		// TODO Auto-generated method stub
		if (bean.getResourceType()== GroupResource.RESOURCE_TYPE_FILE) {
			// 删除数据库表项，删除表项需在删除文件之前，因为出现异常的话，数据库可以回滚
			//groupResourceDao.delete(new long[] { bean.getId() });
			groupResourceDao.deleteResource(bean);//使用jpa 的级联删除方式，避免存在已分享资源无法删除
			// 删除服务器文件
			String file = FilePathUtil.getFileFullPath(bean);
			FileUtil.deleteFile(file);
		} else {
			groupResourceDao.deleteResource(bean);
		}
	}
	/**
	 * 获取系统剩余空间
	 * @return
	 */
	public long getAvailableCapacity() {
		// TODO Auto-generated method stub
		long ac = 0L;
		long total = PropertyUtil.getTotalGroupSpaceMaxLimit() * 1024 * 1024;// 系统总空间GB*1024*1024
		// 所有已分配空间的柜子空间总和
		long allGroupsCapacity = groupDao.getAllocatedSpaceSize(new long[0]);
		// 所有域剩余的可用空间
		long allDomainAvailableCapacity = 0L;
		String hql = "select sum(availableCapacity) from DomainCategory as dc ";
		Long result = genericDao.findFirst(hql, null);
		if (result !=null) {
			allDomainAvailableCapacity = result.longValue();
		}
		ac = total - allGroupsCapacity - allDomainAvailableCapacity;
		return ac;
	}
	
	public long getResourceSpaceSize(long groupId) throws GroupsException {
		Group bean = groupDao.getGroupById(groupId);
		if (!permService.isAdmin(UserUtils.getCurrentMemberId())) {
			CheckUtil.checkNormalGroup(bean);
		}
		
		if (bean.getTotalFileSize() != null && bean.getTotalFileSize().longValue() > 0) {
			return bean.getTotalFileSize();
		}
		Map<String, String> map = bean.getExternInfo();
		if (map == null || map.get(Group.ExternInfo.TotalFileSize.toString()) == null) {
			GroupType grouptype = bean.getGroupType();
			if (grouptype == null || grouptype.getId() <= 0) {
				return PropertyUtil.getGroupResourceSize();
			} else {
				return grouptype.getTotalFileSize();
			}
		}
		return Long.parseLong(map.get(Group.ExternInfo.TotalFileSize.toString()));
	}
	
 
	public Member[] getRecentContacts(Long providerId, Integer limit) {
		// TODO Auto-generated method stub
		final long memberId = providerId;
		List<GroupResourceShare> beans = shareDao.getAllResourceShareByProvider(memberId, 0, limit);
		List<String> resipientAccountsOrTeamNames = new ArrayList<String>();
		if (beans != null && beans.size() > 0) {
			for (GroupResourceShare b : beans) {
				String str = b.getRecipient();
				if (str == null || str.length() == 0 || resipientAccountsOrTeamNames.contains(str)) {
					continue;
				}
				if (str.contains(";")) {
					String[] recipients = str.split(";");
					// resipientAccountsOrTeamNames.addAll(Arrays.asList(recipients));
					for (String s : recipients) {
						if (!resipientAccountsOrTeamNames.contains(s)) {
							resipientAccountsOrTeamNames.add(s);
						}
					}
				} else {
					if (!resipientAccountsOrTeamNames.contains(str)) {
						resipientAccountsOrTeamNames.add(str);
					}
				}
			}
		}
		List<Member> members = new ArrayList<Member>();
		int count = 0;
		for (String account : resipientAccountsOrTeamNames) {
			if (count >= limit) {
				break;
			}
			if (account != null && account.length() > 0) {
				Member mem = grouperService.getMemberByNameAndAccount(account, account,Member.MEMBER_TYPE_PERSON);
				if (mem == null) {
					// account 不是一个member，而是一个组的name
					RemoteItems[] item = UserRemoteServiceUtil.findGroupByName(account, true);
					if (item != null && item.length > 0 && item[0] != null) {
						String uuid = item[0].getId();
						mem = grouperService.getMembersByName(uuid).get(0);
						mem.setSignature(mem.getSignature() == null ? account : mem.getSignature());// 更新数据库中team类型member的signature
						members.add(mem);
						count++;
					}
				} else {
					members.add(mem);
					count++;
				}	
			}
		}
		return members.toArray(new Member[0]);
	}
	public GroupDecoration getGroupDecoration(Long groupId) {
		// TODO Auto-generated method stub
		return groupDao.getGroupDecorationByGroup(groupId);
	}
	public Member[] getGroupManagers(Long groupId) {
		// TODO Auto-generated method stub
		List<GroupManager> gm = groupManagerDao.getGroupManagerByGroup(groupId);
		Member[] memberBeans = new Member[gm.size()];
		for (int i = 0; i < memberBeans.length; ++i) {
			memberBeans[i] = memberDao.getMemberById(gm.get(i).getMemberId());
		}
		return memberBeans;
	}


}

