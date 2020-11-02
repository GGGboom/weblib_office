package com.dcampus.weblib.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dcampus.common.generic.GenericDao;
import com.dcampus.common.service.BaseService;
import com.dcampus.common.util.CacheUtil;
import com.dcampus.weblib.dao.OldPermDao;
import com.dcampus.weblib.entity.OldPerm;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.service.permission.IPermission;
import com.dcampus.weblib.service.permission.IPermission.CategoryPerm;
import com.dcampus.weblib.service.permission.IPermission.GlobalPerm;
import com.dcampus.weblib.service.permission.IPermission.GroupPerm;
import com.dcampus.weblib.service.permission.PermCollection;

/**
 * 权限操作service
 * 去掉所有跟讨论区有关的
 * @author patrick
 */
 
@Service
@Transactional(readOnly=false)
public class PermissionService extends BaseService {
	
	@Autowired
	private OldPermDao permDao;
	
	@Autowired
	private IPermission permission;
	
	/**
	 * 查询是否拥有全局权限
	 *
	 * @param memberId
	 * @param queryPerm
	 * @return
	 */
	 
	public boolean hasGlobalPermission(long memberId, IPermission.GlobalPerm queryPerm)
			throws GroupsException {
		return permission.hasGlobalPerm(memberId, queryPerm);
	}

	/**
	 * 查询是否拥有分区权限
	 *
	 * @param memberId
	 * @param categoryId
	 * @param queryPerm
	 * @return
	 * @throws GroupsException
	 */
	 
	public boolean hasCategoryPermission(long memberId,long categoryId,
			IPermission.CategoryPerm queryPerm) throws GroupsException{
		return permission.hasCategoryPerm(memberId, categoryId, queryPerm);
	}

	/**
	 * 查询是否拥有圈子权限
	 *
	 * @param memberId
	 * @param groupId
	 * @param queryPerm
	 * @return
	 * @throws GroupsException
	 */
	public boolean hasGroupPermission(long memberId, long groupId, IPermission.GroupPerm queryPerm)
			throws GroupsException{
		return  permission.hasGroupPerm(memberId, groupId, queryPerm);
	}


	/**
	 * 修改权限。提取PermCollection中type类型的权限，设置memberId用户在类型为type，id为typeId中的权限
	 *
	 * @param typeId
	 * @param memberId
	 * @param type
	 * @param permCollection
	 * @throws GroupsException
	 */
	public void modifyPermission(long typeId, long memberId,int type,
			PermCollection permCollection) throws GroupsException{
		if (type == OldPerm.PERM_TYPE_GLOBAL) {
			GlobalPerm[] perms = permCollection.getGlobalPerm();
			permission.modifyMemberGlobalPerms(memberId, perms);
		} else if (type == OldPerm.PERM_TYPE_CATEGORY) {
			CategoryPerm[] perms = permCollection.getCategoryPerm();
			permission.modifyMemberCategoryPerms(memberId, typeId, perms);
		} else if (type == OldPerm.PERM_TYPE_GROUP) {
			GroupPerm[] perms = permCollection.getGroupPerm();
			permission.modifyMemberGroupPerms(memberId, typeId, perms);
		} else if (type == OldPerm.PERM_TYPE_GROUP_OF_CATEGORY) {
			GroupPerm[] perms = permCollection.getGroupSelfPerm();
			permission.modifyMemberGroupInCategoryPerms(memberId, typeId, perms);
		}

		permission.resetPermission();
//		 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		CacheUtil.removeAll(CacheUtil.myVisualGroupCache);
	}

	/**
	 * 获取权限。提取memberId用户在类型为type，id为typeId中的权限
	 *
	 * @param typeId
	 * @param memberId
	 * @param type
	 * @return
	 * @throws GroupsException
	 */
	public PermCollection getPermission(long typeId, long memberId, int type)
			throws GroupsException {
		if (type == OldPerm.PERM_TYPE_GLOBAL) {
			return permission.getMemberGlobalPerm(memberId);
		} else if (type == OldPerm.PERM_TYPE_CATEGORY) {
			return permission.getMemberCategoryPerm(memberId, typeId);
		} else if (type == OldPerm.PERM_TYPE_GROUP) {
			/*try {
			*/	PermCollection temp = permission.getMemberGroupPerm(memberId, typeId);
				return temp;
			/*} catch (Exception e) {
				System.out.println("test======" + e.toString());	
				return null;
			}*/
			
			//return permission.getMemberGroupPerm(memberId, typeId);
		} else if (type == OldPerm.PERM_TYPE_GROUP_OF_CATEGORY) {
			return permission.getGroupPermissionToCategory(memberId, typeId);
		}
		return null;
	}

	/**
	 * 判断用户是否是管理员
	 *
	 * @param memberId
	 * @return
	 * @throws GroupsException
	 */
	public boolean isAdmin(long memberId) throws GroupsException {
		return permission.isAdmin(memberId);
	}

	/**
	 * 判断用户是否是圈子管理员
	 *
	 * @param memberId
	 * @param groupId
	 * @return
	 * @throws GroupsException
	 */
	public boolean isGroupManager(long memberId, long groupId)
			throws GroupsException {
		return permission.isGroupManager(memberId, groupId);
	}

	/**
	 * 判断用户是否是超级管理员
	 *
	 * @param memberId
	 * @return
	 * @throws GroupsException
	 */
	public boolean isSuperAdmin(long memberId) throws GroupsException {
		return permission.isSuperAdmin(memberId);
	}

	/**
	 * 删除圈子对用户的特殊权限设置
	 *
	 * @param groupId
	 * @throws GroupsException
	 */
	public void removeGroupPermission(long groupId) throws GroupsException {
		permission.removeGroupPermission(groupId);
	}

	/**
	 * 删除分类对用户的所有特殊权限设置
	 *
	 * @param categoryId
	 * @throws GroupsException
	 */
	public void removeCategoryPermission(long categoryId) throws GroupsException {
		permission.removeCategoryPermission(categoryId);
	}
	/**
	 * 是否项目管理员（文件柜管理员）
	 * @param memberId
	 * @return
	 * @throws GroupsException
	 */
	public boolean isProjectManager(long memberId) throws GroupsException {
		return permission.isProjectManager(memberId);
	}
	
	/**
	 * 获得用户对目录的权限集合
	 * @param categoryId
	 * @param memberId
	 * @return
	 * @throws GroupsException
	 */
	public PermCollection getGroupPermissionToCategory(long categoryId,
			long memberId)throws GroupsException {
		return permission.getGroupPermissionToCategory(memberId, categoryId);
	}
	
	/**
	 * 获得用户对柜子（组）的权限集合
	 * @param groupId
	 * @param memberId
	 * @return
	 * @throws GroupsException
	 */
	public PermCollection getGroupPermissionToGroup(long groupId,
			long memberId)throws GroupsException {
		return permission.getGroupPermissionToGroup(memberId, groupId);
	}
	
	/**
	 * 根据typeId和permType查找perms
	 * @param typeId 作用域的id
	 * @param type 作用的类型
	 * @return
	 */
	public List<OldPerm> getPerms(long typeId, int type) {
		return permDao.getPermsByType(typeId, type);
	}

	public List<OldPerm> getPermsPart(long typeId, int type,int start,int limit) {
		return permDao.getPermsByTypePart(typeId, type,start,limit);
	}
	/**
	 * 根据id获取记录
	 * @param id
	 * @return
	 */
	public OldPerm getOldPermById(long id) {
		return permDao.getOldPermById(id);
	}
	
	/**
	 * 删除权限
	 * @param id
	 */
	public void deletePerm(long id) {
		OldPerm p = this.getOldPermById(id);
		permDao.deletePerm(p);
	}
	
	/**
	 * 批量删除权限
	 * @param ids
	 */
	public void deletePerms(long[] ids) {
		for (long id : ids) {
			this.deletePerm(id);
		}
		permission.resetPermission();
//		 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		CacheUtil.removeAll(CacheUtil.myVisualGroupCache);
	}
	
	/**
	 * 根据member和被作用域的id获取权限项
	 *
	 * @param memberId
	 * @param typeId
	 * @param type
	 * @return
	 */
	public OldPerm getPerm(long memberId, long typeId, int type) {
		return permDao.getPermByMemberAndType(memberId, typeId, type);
	}
	
	/**
	 * 是否有创建柜子权限
	 * @param memberId
	 * @return
	 */
	public boolean hasCreatGroupPermission(long memberId) {
		return permission.hasCreatGroupPermission(memberId);
	}
	
	/**
	 * 获取用户对应的所有权限
	 * @param memberId
	 * @param type
	 * @return
	 */
	public List<OldPerm> getMemberPerms(long memberId, int type) {
		return permDao.getMemberPerms(memberId, type);
	}
	
	/**
	 * 获取所有的权限
	 */
	public List<OldPerm> getAllPerms() {
		return permDao.getAllPerms();
	}

}
