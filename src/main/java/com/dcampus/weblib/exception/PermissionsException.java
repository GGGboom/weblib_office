package com.dcampus.weblib.exception;

import com.dcampus.common.config.ResourceProperty;

/**
 * 权限异常
 *
 * @author zim
 *
 */
public class PermissionsException extends GroupsException {
	private static final long serialVersionUID = -7790471154471757965L;

	public PermissionsException(String msg) {
		super(msg);
	}

	/** 分类权限异常 **/
	public static final PermissionsException CategoryException = new PermissionsException(
			ResourceProperty.getCategoryPermissionExceptionString());
	
	/** 分类权限异常 **/
	public static final PermissionsException ApplicationException = new PermissionsException(
			ResourceProperty.getApplicationPermissionExceptionString());

	/** 讨论区权限异常 **/
	public static final PermissionsException ForumException = new PermissionsException(
			ResourceProperty.getForumPermissionExceptionString());

	/** 圈子权限异常 **/
	public static final PermissionsException GroupException = new PermissionsException(
			ResourceProperty.getGroupPermissionExceptionString());

	/** 帖子权限异常 **/
	public static final PermissionsException PostException = new PermissionsException(
			ResourceProperty.getPostPermissionExceptionString());

	/** 主题权限异常 **/
	public static final PermissionsException ThreadException = new PermissionsException(
			ResourceProperty.getThreadPermissionExceptionString());

	/** 用户权限异常 **/
	public static final PermissionsException MemberException = new PermissionsException(
			ResourceProperty.getMemberPermissionExceptionString());

	/** 短消息权限异常 **/
	public static final PermissionsException MessageException = new PermissionsException(
			ResourceProperty.getMessagePermissionExceptionString());

	/** 邀请函权限异常 **/
	public static final PermissionsException InvitationExcpeiont = new PermissionsException(
			ResourceProperty.getInvitationPermissionExceptionString());

	/** 过滤字权限异常 **/
	public static final PermissionsException BadWordException = new PermissionsException(
			ResourceProperty.getBadWordPermissionExceptionString());

	/** 站点权限异常 **/
	public static final PermissionsException GlobalException = new PermissionsException(
			ResourceProperty.getGlobalPermissionExceptionString());
	
	/** Rss管理权限异常 **/
	public static final PermissionsException RssException = new PermissionsException(
			ResourceProperty.getRssPermissionExceptionString());
}
