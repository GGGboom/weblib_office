package com.dcampus.weblib.service.permission;

import com.dcampus.weblib.exception.GroupsException;

/**
 * 圈子权限接口
 * Patrick修改
 * 去掉了所有跟讨论区forum有关的操作
 * @author zim
 *
 */
public interface IPermission {

	public static interface IPerm {
		long getMask();
	}

	/**
	 * 全局下的用户可以拥有的直接权限
	 *
	 * @author zim
	 *
	 */
	public static enum GlobalPerm implements IPerm {

		/** 添加分区 * */
		CREATE_CATEGORY(0x1L),
		/** 移动分区 * */
		MOVE_CATEGORY(0x2L),
		/** 关闭分区 * */
		CLOSE_CATEGORY(0x4L),
		/** 删除分类 * */
		DELETE_CATEGORY(0x8L),
		/** 发送短消息 * */
		SEND_MESSAGE(0x10L),
		/** 审核圈子* */
		AUDIT_GROUP(0x20L);

		private long mask;

		private GlobalPerm(long mask) {
			this.mask = mask;
		}

		public long getMask() {
			return mask;
		}

		public static GlobalPerm[] all() {
			return new GlobalPerm[] { MOVE_CATEGORY, CLOSE_CATEGORY,
					CREATE_CATEGORY, DELETE_CATEGORY, SEND_MESSAGE, AUDIT_GROUP };
		}
	}

	/**
	 * 用户在进入某个分类之后能够拥有的直接权限
	 *
	 * @author zim
	 *
	 */
	public static enum CategoryPerm implements IPerm {

		/** 修改分区 * */
		MODIFY_CATEGORY(0x1L),

		/** 创建圈子 * */
		CREATE_GROUP(0x2L),

		/** 创建高级圈子* */
		CREATE_ADVENCED_GROUP(0x4L),

		/** 浏览分区 **/
		VIEW_CATEGORY(0x8L),
		
		/** 添加子分区 * */
		CREATE_CATEGORY(0x10L),
		/** 可管理 * */
		MANAGE_CATEGORY(0x20L),
		/** 可控制 */
		CONTROL_CATEGORY(0x40L),
		
		/** 应用管理* */
		CATEGORY_APPMANAGER(0x80L);
		
		private long mask;

		private CategoryPerm(long mask) {
			this.mask = mask;
		}

		public long getMask() {
			return mask;
		}

		public static CategoryPerm[] all() {
			return new CategoryPerm[] { MODIFY_CATEGORY, CREATE_GROUP,
					CREATE_ADVENCED_GROUP, VIEW_CATEGORY,CREATE_CATEGORY,MANAGE_CATEGORY,CONTROL_CATEGORY,CATEGORY_APPMANAGER };
		}
	};

	/**
	 * 用户进入某个圈子后可以拥有的直接权限
	 *
	 * @author zim
	 *
	 */
	public static enum GroupPerm implements IPerm {

		/** 浏览圈子，专指浏览圈子中的讨论区和帖子 * */
		VIEW_GROUP(0x1L),
		/** 加入圈子 * */
		JOIN_GROUP(0x2L),
		/** 上传资源 * */
		UPLOAD_RESOURCE(0x4L),
		/** 查看成员 * */
		VIEW_MEMBER(0x8L),
		/** 发帖（针对圈子全局设置） * */
		ADD_POST(0x10L),
		/** 发帖不需审核 * */
		POST_NO_NEED_AUDIT(0x20L),
		/** 审核帖子 * */
		AUDIT_POST(0x40L),
		/** 邀请非会员加入 * */
		INVITE_MEMBER(0x80L),
		/** 修改圈子 * */
		MODIFY_GROUP(0x100L),
		/** 踢除会员 * */
		UNBIND_MEMBER(0x200L),
		/** 指定副圈主 * */
		APPOINT_GROUP_MANAGER(0x400L),
		/** 管理用户加入圈子的请求 * */
		MANAGE_MEMBER_REQUEST(0x800L),
		/** 删除资源 * */
		DELETE_RESOURCE(0x1000L),
		/** 关闭圈子 * */
		CLOSE_GROUP(0x2000L),
		/** 删除圈子 * */
		DELETE_GROUP(0x4000L),

		/** 添加讨论区 * */
		ADD_FORUM(0x8000L),
		/** 关闭讨论区 * */
		CLOSE_FORUM(0x10000L),
		/** 删除讨论区 * */
		DELETE_FORUM(0x20000L),
		/** 指定讨论区区长 * */
		APPOINT_FORUM_MANAGER(0x40000L),

		/** 下载资源 * */
		DOWNLOAD_RESOURCE(0x80000L),
		/** 浏览资源 * */
		VIEW_RESOURCE(0x100000L),
		/** 发帖改帖时发送邮件通知圈内用户* */
		MAIL_ON_POST(0x200000L),

		/** 管理过滤字* */
		BAD_WORD(0x400000L),

		/** 共享资源 **/
		SHARE_RESOURCE(0x800000L),
		/** 修改资源 **/
		MODIFY_RESOURCE(0x1000000L),
		/** 创建文件夹* */
		ADD_FOLDER(0x2000000L),
		/** 可管理* */
		MANAGE_GROUP(0x4000000L),
		
		// 目前没用
		/** 应用管理* */
		GROUP_APPMANAGER(0x8000000L),
		/** 可管理* */
		GROUP_APPUSER(0x10000000L);
		//=====
		
		
		private long mask;

		private GroupPerm(long mask) {
			this.mask = mask;
		}

		public long getMask() {
			return mask;
		}

		public static GroupPerm[] all() {
			return new GroupPerm[] { VIEW_GROUP, JOIN_GROUP, UPLOAD_RESOURCE,
					VIEW_MEMBER, ADD_POST, POST_NO_NEED_AUDIT, AUDIT_POST,
					INVITE_MEMBER, MODIFY_GROUP, UNBIND_MEMBER,
					APPOINT_GROUP_MANAGER, MANAGE_MEMBER_REQUEST,
					DELETE_RESOURCE, CLOSE_GROUP, DELETE_GROUP,

					ADD_FORUM, CLOSE_FORUM, DELETE_FORUM,
					APPOINT_FORUM_MANAGER,

					DOWNLOAD_RESOURCE, VIEW_RESOURCE, MAIL_ON_POST,

					BAD_WORD, SHARE_RESOURCE, MODIFY_RESOURCE, ADD_FOLDER,MANAGE_GROUP,
					
					GROUP_APPMANAGER,GROUP_APPUSER
			};
		}
	};


	/**
	 * 查询是否拥有某项全局权限
	 *
	 * @param memberId
	 * @param perm
	 * @return
	 * @throws GroupsException
	 */
	boolean hasGlobalPerm(long memberId, GlobalPerm perm)
			throws GroupsException;

	/**
	 * 查询是否拥有某项分类权限
	 *
	 * @param memberId
	 * @param categoryId
	 * @param perm
	 * @return
	 * @throws GroupsException
	 */
	boolean hasCategoryPerm(long memberId, long categoryId, CategoryPerm perm)
			throws GroupsException;

	/**
	 * 查询是否拥有某项圈子权限
	 *
	 * @param memberId
	 * @param groupId
	 * @param perm
	 * @return
	 * @throws GroupsException
	 */
	boolean hasGroupPerm(long memberId, long groupId, GroupPerm perm)
			throws GroupsException;

	/**
	 * 获取用户在全局的所有权限
	 *
	 * @param memberId
	 * @return
	 * @throws GroupsException
	 */
	PermCollection getMemberGlobalPerm(long memberId) throws GroupsException;

	/**
	 * 获取用户在某个分类中所具有的所有权限，包括全局权限在内
	 *
	 * @param memberId
	 * @param categoryId
	 * @return
	 * @throws GroupsException
	 */
	PermCollection getMemberCategoryPerm(long memberId, long categoryId)
			throws GroupsException;

	/**
	 * 获取用户在某个圈子中所具有的所有权限，包括全局权限和分类权限
	 *
	 * @param memberId  用户id
	 * @param groupId  柜子id
	 * @return  PermCollection 权限集合
	 * @throws GroupsException  抛出异常
	 */
	PermCollection getMemberGroupPerm(long memberId, long groupId)
			throws GroupsException;


	/**
	 * 重置用户的权限，这并不是将用户权限置空，而是清除权限缓存，使缓存和实际权限同步
	 *
	 * @param memberId 用户id
	 * @throws GroupsException 抛出异常
	 */
	void resetPermission(long memberId) throws GroupsException;

	/**
	 * 重置所有用户的权限，这并不是将用户权限置空，而是清除权限缓存，使缓存和实际权限同步
	 *
	 * @throws GroupsException  抛出异常
	 */
	void resetPermission() throws GroupsException;

	/**
	 * 修改用户全局权限
	 *
	 * @param memberId  用户id
	 * @param perms   权限组
	 * @throws GroupsException  抛出异常
	 */
	void modifyMemberGlobalPerms(long memberId, GlobalPerm[] perms)
			throws GroupsException;

	/**
	 * 修改用户分类权限
	 *
	 * @param memberId  用户id
	 * @param categoryId  分类id
	 * @param perms  权限组
	 * @throws GroupsException  抛出异常
	 */
	void modifyMemberCategoryPerms(long memberId, long categoryId,
			CategoryPerm[] perms) throws GroupsException;

	/**
	 * 修改用户圈子权限
	 *
	 * @param memberId  用户id
	 * @param groupId  柜子id
	 * @param perms  权限组
	 * @throws GroupsException 抛出异常
	 */
	void modifyMemberGroupPerms(long memberId, long groupId, GroupPerm[] perms)
			throws GroupsException;


	/**
	 * 判断是否是管理员
	 *
	 * @param memberId 用户id
	 * @return boolean 是否是管理员
	 * @throws GroupsException 抛出异常
	 */
	boolean isAdmin(long memberId) throws GroupsException;

	/**
	 * 判断是否是圈子管理员
	 *
	 * @param memberId 用户id
	 * @param groupId 柜子id
	 * @return boolean  是否是圈子管理员
	 * @throws GroupsException 抛出异常
	 */
	public boolean isGroupManager(long memberId, long groupId)
			throws GroupsException;

	/**
	 * 判断是否是超级管理员
	 *
	 * @param memberId 用户id
	 * @return boolean 是否是超级管理员
	 * @throws GroupsException 抛出异常
	 */
	public boolean isSuperAdmin(long memberId) throws GroupsException;

	/**
	 * 删除圈子所有对用户的特殊权限设置
	 *
	 * @param groupId 柜子id
	 * @throws GroupsException 抛出异常
	 */
	public void removeGroupPermission(long groupId) throws GroupsException;

	/**
	 * 删除分类对所有用户设置的所有特殊权限
	 *
	 * @param categoryId  分类id
	 * @throws GroupsException 抛出异常
	 */
	public void removeCategoryPermission(long categoryId)
			throws GroupsException;
	/**
	 * 是否项目管理员（文件柜管理员）
	 * @param memberId
	 * @return  boolean  是否项目管理员
	 * @throws GroupsException  抛出异常
	 */
	public boolean isProjectManager(long memberId)throws GroupsException;
	
	public void modifyMemberGroupInCategoryPerms(long memberId, long categoryId,
			GroupPerm[] perms) throws GroupsException;
	
	public PermCollection getGroupPermissionToGroup(long memberId,
			long groupId)throws GroupsException;
	
	public PermCollection getGroupPermissionToCategory(long memberId,
			long categoryId)throws GroupsException;
	
	public boolean hasCreatGroupPermission(long memberId)throws GroupsException;
	/**
	 * 判断某一个memberId是否属于用户userMemberId所在的组
	 * @param memberId   用户id
	 * @param userMemberId   用户id
	 * @return  boolean 是否属于
	 */
	public boolean isContainMemberByTeam(long memberId, long userMemberId)throws GroupsException; 
	
	public boolean isReceivedResourceToMember(long resourceId, long memberId);
}
