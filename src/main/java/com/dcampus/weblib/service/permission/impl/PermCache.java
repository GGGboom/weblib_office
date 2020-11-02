package com.dcampus.weblib.service.permission.impl;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import com.dcampus.weblib.service.permission.IPermission;
import com.dcampus.weblib.service.permission.PermCollection;
import com.dcampus.common.util.CacheUtil;

/**
 * 权限缓存工具类。
 * Patrick修改
 * 去掉了所有跟讨论区forum有关的操作
 *
 * @author zim
 *
 */
public class PermCache {
	private static CacheManager manager = CacheManager.getInstance();

	private static Cache cache = manager.getCache("permission-cache");

	/**
	 * 获取全局缓存键
	 *
	 * @param memberId
	 *            马甲id
	 * @return
	 */
	private static String getGlobalPermKey(long memberId) {
		return "GlobalPerm_" + memberId;
	}

	/**
	 * 获取分类缓存键
	 *
	 * @param memberId
	 *            马甲id
	 * @param categoryId
	 *            分类id
	 * @return
	 */
	private static String getCategoryPermKey(long memberId, long categoryId) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("CategoryPerm_");
		buffer.append(memberId);
		buffer.append("_");
		buffer.append(categoryId);
		return buffer.toString();
	}

	/**
	 * 获取分类缓存键
	 *
	 * @param memberId
	 *            马甲id
	 * @return
	 */
	private static String getCategoryPermKey(long memberId) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("CategoryPerm_");
		buffer.append(memberId);
		return buffer.toString();
	}

	/**
	 * 获取圈子缓存键
	 *
	 * @param memberId
	 *            马甲id
	 * @param groupId
	 *            圈子id
	 * @return
	 */
	private static String getGroupPermKey(long memberId, long groupId) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("GroupPerm_");
		buffer.append(memberId);
		buffer.append("_");
		buffer.append(groupId);
		return buffer.toString();
	}

	private static String getGroupPermCollectionKey(long memberId, long groupId) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("GroupPermCollection_");
		buffer.append(memberId);
		buffer.append("_");
		buffer.append(groupId);
		return buffer.toString();
	}
	
	private static String getGroupPermCollectionKey(long memberId) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("GroupPermCollection_");
		buffer.append(memberId);
		return buffer.toString();
	}
	
	private static String getGroupOfCategoryPermKey(long memberId, long categoryId) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("GroupOfCategoryPerm_");
		buffer.append(memberId);
		buffer.append("_");
		buffer.append(categoryId);
		return buffer.toString();
	}
	
	private static String getGroupOfCategoryPermKey(long memberId) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("GroupOfCategoryPerm_");
		buffer.append(memberId);
		return buffer.toString();
	}
	
	/**
	 * 获取圈子缓存键
	 *
	 * @param memberId
	 *            马甲id
	 * @return
	 */
	private static String getGroupPermKey(long memberId) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("GroupPerm_");
		buffer.append(memberId);
		return buffer.toString();
	}


	/**
	 * 从缓存中获取全局权限
	 *
	 * @param memberId
	 *            马甲id
	 * @return 若缓存无记录，返回null
	 */
	public static IPermission.GlobalPerm[] getGlobalPerm(long memberId) {
		return (IPermission.GlobalPerm[]) CacheUtil.getCache(cache,
				getGlobalPermKey(memberId));
	}

	/**
	 * 从缓存中获取分类权限
	 *
	 * @param memberId
	 *            马甲id
	 * @param categoryId
	 *            分类id
	 * @return 若缓存无记录，返回null
	 */
	public static IPermission.CategoryPerm[] getCategoryPerm(long memberId,
			long categoryId) {
		return (IPermission.CategoryPerm[]) CacheUtil.getCache(cache,
				getCategoryPermKey(memberId, categoryId));
	}

	/**
	 * 从缓存中获取圈子权限
	 *
	 * @param memberId
	 *            马甲id
	 * @param groupId
	 *            圈子id
	 * @return 若缓存无记录，返回null
	 */
	public static IPermission.GroupPerm[] getGroupPerm(long memberId,
			long groupId) {
		return (IPermission.GroupPerm[]) CacheUtil.getCache(cache,
				getGroupPermKey(memberId, groupId));
	}

	public static PermCollection getGroupPermissionToCategory(long memberId,
			long categoryId) {
		return (PermCollection) CacheUtil.getCache(cache,
				getGroupOfCategoryPermKey(memberId, categoryId));
	}
	
	public static PermCollection getGroupPermCollection(long memberId,
			long groupId) {
		return (PermCollection) CacheUtil.getCache(cache,
				getGroupPermCollectionKey(memberId, groupId));
	}

	/**
	 * 设置全局权限到缓存
	 *
	 * @param memberId
	 *            马甲id
	 * @param globalPerms
	 *            全局权限集合
	 */
	public static void setGlobalPerm(long memberId,
			IPermission.GlobalPerm[] globalPerms) {
		CacheUtil.setCache(cache, getGlobalPermKey(memberId), globalPerms);
	}

	/**
	 * 设置分类权限到缓存
	 *
	 * @param memberId
	 *            马甲id
	 * @param categoryId
	 *            分类id
	 * @param categoryPerms
	 *            分类权限集合
	 */
	public static void setCategoryPerm(long memberId, long categoryId,
			IPermission.CategoryPerm[] categoryPerms) {
		CacheUtil.setCache(cache, getCategoryPermKey(memberId, categoryId),
				categoryPerms);
	}

	/**
	 * 设置圈子权限到缓存
	 *
	 * @param memberId
	 *            马甲id
	 * @param groupId
	 *            圈子id
	 * @param groupPerms
	 *            圈子权限集合
	 */
	public static void setGroupPerm(long memberId, long groupId,
			IPermission.GroupPerm[] groupPerms) {
		CacheUtil.setCache(cache, getGroupPermKey(memberId, groupId),
				groupPerms);
	}


	public static void setGroupPermCollection(long memberId, long groupId,
			PermCollection pc) {
		CacheUtil.setCache(cache, getGroupPermCollectionKey(memberId, groupId),
				pc);
	}
	
	public static void setGroupPermCollectionToCategory(long memberId, long categoryId,
			PermCollection pc) {
		CacheUtil.setCache(cache, getGroupOfCategoryPermKey(memberId, categoryId),
				pc);
	}
	
	/**
	 * 从缓存中移除全局权限
	 *
	 * @param memberId
	 *            马甲id
	 */
	public static void removeGlobalPerm(long memberId) {
		CacheUtil.removeCache(cache, getGlobalPermKey(memberId));
	}

	/**
	 * 从缓存中移除分类权限
	 *
	 * @param memberId
	 *            马甲id
	 * @param categoryId
	 *            分类id
	 */
	public static void removeCategoryPerm(long memberId, long categoryId) {
		CacheUtil.removeCache(cache, getCategoryPermKey(memberId, categoryId));
	}

	/**
	 * 从缓存中移除圈子权限
	 *
	 * @param memberId
	 *            马甲id
	 * @param groupId
	 *            圈子id
	 */
	public static void removeGroupPerm(long memberId, long groupId) {
		CacheUtil.removeCache(cache, getGroupPermKey(memberId, groupId));
	}


	/**
	 * 移除某个马甲的所有权限缓存
	 *
	 * @param memberId
	 *            马甲id
	 */
	public static void removeAllPermCache(long memberId) {
		CacheUtil.removeCache(cache, getGlobalPermKey(memberId));
		CacheUtil.removeCache(cache, getCategoryPermKey(memberId));
		CacheUtil.removeCache(cache, getGroupPermKey(memberId));
		CacheUtil.removeCache(cache, getGroupOfCategoryPermKey(memberId));
		CacheUtil.removeCache(cache, getGroupPermCollectionKey(memberId));
	}

	/**
	 * 移除所有权限缓存
	 */
	public static void removeAllPermCache() {
		CacheUtil.removeCache(cache, "GlobalPerm_");
		CacheUtil.removeCache(cache, "CategoryPerm_");
		CacheUtil.removeCache(cache, "GroupPerm_");
		CacheUtil.removeCache(cache, "GroupOfCategoryPerm_");
		CacheUtil.removeCache(cache, "GroupPermCollection_");
	}
	
	public static void removeAllGroupPermCache() {
		CacheUtil.removeCache(cache, "GroupPerm_");
		CacheUtil.removeCache(cache, "GroupOfCategoryPerm_");
		CacheUtil.removeCache(cache, "GroupPermCollection_");
	}
}
