package com.dcampus.weblib.service.permission.impl;

import java.util.ArrayList;
import java.util.List;

import com.dcampus.weblib.service.permission.IPermission.CategoryPerm;
import com.dcampus.weblib.service.permission.IPermission.GlobalPerm;
import com.dcampus.weblib.service.permission.IPermission.GroupPerm;

/**
 * 权限操作的帮助类
 * Patrick修改
 * 去掉了所有跟讨论区forum有关的操作
 * @author zim
 *
 */
public class PermUtil {

	/**
	 * 添加权限
	 *
	 * @param oldPermission
	 * @param addingPermission
	 * @return
	 */
	public static long addPermission(long oldPermission, long addingPermission) {
		return oldPermission | addingPermission;
	}

	/**
	 * 删除掉需要删除的权限，先把权限加上再通过异或去掉。 先加上权限再去掉的原因在于:
	 * 如果用户本不存在该权限，则oldPermission在某位上本为0，permission中位为1，
	 * 异或使newPermission位为1，变成了给用户加上某权限了。
	 *
	 * @param oldPermission
	 * @param deletingPermission
	 * @return
	 */
	public static long deletePermission(long oldPermission,
			long deletingPermission) {
		return (oldPermission | deletingPermission) ^ deletingPermission;
	}

	/**
	 * 将全局权限转换成long型值
	 *
	 * @param perms
	 *            全局权限集合
	 * @return
	 */
	public static long convert(GlobalPerm[] perms) {
		if (perms == null)
			return 0;

		long permCode = 0;
		for (GlobalPerm perm : perms) {
			permCode = addPermission(permCode, perm.getMask());
		}

		return permCode;
	}

	/**
	 * 将分类权限转换为long型值
	 *
	 * @param perms
	 *            分类权限集合
	 * @return
	 */
	public static long convert(CategoryPerm[] perms) {
		if (perms == null)
			return 0;

		long permCode = 0;
		for (CategoryPerm perm : perms) {
			permCode = addPermission(permCode, perm.getMask());
		}

		return permCode;
	}

	/**
	 * 将圈子权限转换成long型值
	 *
	 * @param perms
	 *            圈子权限集合
	 * @return
	 */
	public static long convert(GroupPerm[] perms) {
		if (perms == null)
			return 0;

		long permCode = 0;
		for (GroupPerm perm : perms) {
			permCode = addPermission(permCode, perm.getMask());
		}

		return permCode;
	}

	/**
	 * 将讨论区权限转换为long型值
	 *
	 * @param permCode
	 *            讨论区权限集合
	 * @return
	 */
	
	public static GroupPerm[] convertGroupPerm(long permCode) {
		List<GroupPerm> list = new ArrayList<GroupPerm>();
		for (GroupPerm perm : GroupPerm.all()) {
			if ((perm.getMask() & permCode) != 0) {
				list.add(perm);
			}
		}
		return list.toArray(new GroupPerm[list.size()]);
	}
	
	public static CategoryPerm[] convertCategoryPerm(long permCode) {
		List<CategoryPerm> list = new ArrayList<CategoryPerm>();
		for (CategoryPerm perm : CategoryPerm.all()) {
			if ((perm.getMask() & permCode) != 0) {
				list.add(perm);
			}
		}
		return list.toArray(new CategoryPerm[list.size()]);
	}
	
	public static boolean containPermission(GroupPerm[] perms,
			GroupPerm perm) {
		for (GroupPerm p : perms) {
			if (p == perm)
				return true;
		}
		return false;
	}
	
	public static boolean containCategoryPermission(CategoryPerm[] perms,
			CategoryPerm perm) {
		for (CategoryPerm p : perms) {
			if (p == perm)
				return true;
		}
		return false;
	}
	
	public static boolean hasPermission(GroupPerm[] perms,
			GroupPerm perm) {
		for (GroupPerm p : perms) {
			if (p == GroupPerm.MANAGE_GROUP) {
				return true;
			}
			if (p == perm)
				return true;
		}
		return false;
	}
}
