package com.dcampus.weblib.service.permission.impl;

import org.springframework.stereotype.Component;

import com.dcampus.weblib.entity.OldPerm;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.service.permission.PermCollection;
import com.dcampus.weblib.service.permission.IPermission.CategoryPerm;
import com.dcampus.weblib.service.permission.IPermission.GlobalPerm;
import com.dcampus.weblib.service.permission.IPermission.GroupPerm;

/**
 * 权限策略接口
 * patrick修改
 * 去掉所有跟讨论区有关的方法
 *
 * @author zim
 *
 */
public interface IPermStrategy {
	/**
	 * 获取用户的全局权限
	 *
	 * @param memberId
	 * @return
	 * @throws GroupsException
	 */
	public GlobalPerm[] getGlobalPerm(long memberId) throws GroupsException;

	/**
	 * 获取用户的分类权限
	 *
	 * @param memberId
	 * @param categoryId
	 * @return
	 * @throws GroupsException
	 */
	public CategoryPerm[] getCategoryPerm(long memberId, long categoryId)
			throws GroupsException;

	/**
	 * 获取用户的柜子权限
	 *
	 * @param memberId
	 * @param groupId
	 * @return
	 * @throws GroupsException
	 */
	public PermCollection getGroupPerm(long memberId, long groupId)
			throws GroupsException;

//	/**
//	 * 获取用户的讨论区权限
//	 *
//	 * @param memberId
//	 * @param forumId
//	 * @return
//	 * @throws ServiceException
//	 */
//	public ForumPerm[] getForumPerm(long memberId, long forumId)
//			throws ServiceException;

	/**
	 * 判断是否是管理员
	 *
	 * @param memberId
	 * @return
	 * @throws GroupsException
	 */
	public boolean isAdmin(long memberId) throws GroupsException;

	/**
	 * 判断是否是柜子管理员
	 *
	 * @param memberId
	 * @param groupId
	 * @return
	 * @throws GroupsException
	 */
	public boolean isGroupManager(long memberId, long groupId)
			throws GroupsException;

	/**
	 * 判断是否是超级管理员
	 *
	 * @param memberId
	 * @return
	 * @throws GroupsException
	 */
	public boolean isSuperAdmin(long memberId) throws GroupsException;
	/**
	 * 获得马甲在柜子中默认权限
	 * @param memberId
	 * @param groupId
	 * @return
	 * @throws GroupsException
	 */
	public GroupPerm[] getDefaultGroupPerm(long memberId, long groupId);
	
	public OldPerm getPerm(long memberId, long typeId, int type);
	
	
	public GroupPerm[] getGroupPermIneritedCategory(long memberId, long categoryId, 
			boolean inherited);
	/**
	 * 判断某一个memberId是否属于用户userMemberId所在的组
	 * @param memberId
	 * @param userMemberId
	 * @return
	 */
	public boolean isContainMemberByTeam(long memberId, long userMemberId); 
	
	public boolean hasCreatGroupPermission(long memberId);
	
	/**
	 * @author patrick
	 * 按照自己的想法
	 * 直接查询接收分享记录 的表记录实现
	 * 
	 * @param resourceId
	 * @param memberId
	 * @return
	 */
	public boolean isReceivedResourceToMember(long resourceId, long memberId);
}
