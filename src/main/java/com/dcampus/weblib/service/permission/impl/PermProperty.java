package com.dcampus.weblib.service.permission.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.dcampus.weblib.service.permission.IPermission.CategoryPerm;
import com.dcampus.weblib.service.permission.IPermission.GlobalPerm;
import com.dcampus.weblib.service.permission.IPermission.GroupPerm;

/**
 * 权限配置读取类
 * Patrick修改
 * 去掉了所有跟讨论区forum有关的操作
 *
 * @author zim
 *
 */
public class PermProperty {
	private static String defaultGlobalPerm;

	private static String defaultCategoryPerm;

	private static String defaultGroupNonmemberPerm;

	private static String defaultGroupMemberPerm;

	private static String publicGroupNonmemberPerm;

	private static String publicGroupMemberPerm;

	private static String privateGroupNonmemberPerm;

	private static String privateGroupMemberPerm;

	private static String messageboardGroupNonmemberPerm;

	private static String messageboardGroupMemberPerm;


	static {
		try {
			Properties properties = new Properties();
			properties.load(Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("groups.properties"));

			defaultGlobalPerm = properties.getProperty("DefaultGlobalPerm");

			defaultCategoryPerm = properties.getProperty("DefaultCategoryPerm");

			defaultGroupNonmemberPerm = properties
					.getProperty("DefaultGroupNonmemberPerm");

			defaultGroupMemberPerm = properties
					.getProperty("DefaultGroupMemberPerm");

			publicGroupNonmemberPerm = properties
					.getProperty("PublicGroupNonmemberPerm");

			publicGroupMemberPerm = properties
					.getProperty("PublicGroupMemberPerm");

			privateGroupNonmemberPerm = properties
					.getProperty("PrivateGroupNonmemberPerm");

			privateGroupMemberPerm = properties
					.getProperty("PrivateGroupMemberPerm");

			messageboardGroupNonmemberPerm = properties
					.getProperty("MessageboardGroupNonmemberPerm");

			messageboardGroupMemberPerm = properties
					.getProperty("MessageboardGroupMemberPerm");

		} catch (Exception e) {
			// log.error(e, e);
			e.printStackTrace();
		}
	}

	public static GlobalPerm[] getDefaultGlobalPerm() {
		if (defaultGlobalPerm == null || defaultGlobalPerm.length() == 0)
			return new GlobalPerm[0];

		String[] perms = defaultGlobalPerm.split(":");
		List<GlobalPerm> list = new ArrayList<GlobalPerm>();
		for (String perm : perms) {
			for (GlobalPerm globalPerm : GlobalPerm.all()) {
				if (perm.equalsIgnoreCase(globalPerm.name()))
					list.add(globalPerm);
			}
		}

		return list.toArray(new GlobalPerm[list.size()]);
	}

	public static CategoryPerm[] getDefaultCategoryPerm() {
		if (defaultCategoryPerm == null || defaultCategoryPerm.length() == 0)
			return new CategoryPerm[0];

		String[] perms = defaultCategoryPerm.split(":");
		List<CategoryPerm> list = new ArrayList<CategoryPerm>();
		for (String perm : perms) {
			for (CategoryPerm categoryPerm : CategoryPerm.all()) {
				if (perm.equalsIgnoreCase(categoryPerm.name()))
					list.add(categoryPerm);
			}
		}

		return list.toArray(new CategoryPerm[list.size()]);
	}

	public static GroupPerm[] getDefaultGroupNonmemberPerm() {
		if (defaultGroupNonmemberPerm == null
				|| defaultGroupNonmemberPerm.length() == 0)
			return new GroupPerm[0];

		String[] perms = defaultGroupNonmemberPerm.split(":");
		List<GroupPerm> list = new ArrayList<GroupPerm>();
		for (String perm : perms) {
			for (GroupPerm groupPerm : GroupPerm.all()) {
				if (perm.equalsIgnoreCase(groupPerm.name()))
					list.add(groupPerm);
			}
		}
		return list.toArray(new GroupPerm[list.size()]);
	}

	public static GroupPerm[] getDefaultGroupMemberPerm() {
		if (defaultGroupMemberPerm == null
				|| defaultGroupMemberPerm.length() == 0)
			return new GroupPerm[0];

		String[] perms = defaultGroupMemberPerm.split(":");
		List<GroupPerm> list = new ArrayList<GroupPerm>();
		for (String perm : perms) {
			for (GroupPerm groupPerm : GroupPerm.all()) {
				if (perm.equalsIgnoreCase(groupPerm.name()))
					list.add(groupPerm);
			}
		}
		return list.toArray(new GroupPerm[list.size()]);
	}

	public static GroupPerm[] getPublicGroupNonmemberPerm() {
		String[] perms = publicGroupNonmemberPerm.split(":");
		List<GroupPerm> list = new ArrayList<GroupPerm>();
		for (String perm : perms) {
			for (GroupPerm groupPerm : GroupPerm.all()) {
				if (perm.equalsIgnoreCase(groupPerm.name()))
					list.add(groupPerm);
			}
		}
		return list.toArray(new GroupPerm[list.size()]);
	}

	public static GroupPerm[] getPublicGroupMemberPerm() {
		String[] perms = publicGroupMemberPerm.split(":");
		List<GroupPerm> list = new ArrayList<GroupPerm>();
		for (String perm : perms) {
			for (GroupPerm groupPerm : GroupPerm.all()) {
				if (perm.equalsIgnoreCase(groupPerm.name()))
					list.add(groupPerm);
			}
		}
		return list.toArray(new GroupPerm[list.size()]);
	}

	public static GroupPerm[] getPrivateGroupNonmemberPerm() {
		String[] perms = privateGroupNonmemberPerm.split(":");
		List<GroupPerm> list = new ArrayList<GroupPerm>();
		for (String perm : perms) {
			for (GroupPerm groupPerm : GroupPerm.all()) {
				if (perm.equalsIgnoreCase(groupPerm.name()))
					list.add(groupPerm);
			}
		}
		return list.toArray(new GroupPerm[list.size()]);
	}

	public static GroupPerm[] getPrivateGroupMemberPerm() {
		String[] perms = privateGroupMemberPerm.split(":");
		List<GroupPerm> list = new ArrayList<GroupPerm>();
		for (String perm : perms) {
			for (GroupPerm groupPerm : GroupPerm.all()) {
				if (perm.equalsIgnoreCase(groupPerm.name()))
					list.add(groupPerm);
			}
		}
		return list.toArray(new GroupPerm[list.size()]);
	}

	public static GroupPerm[] getMessageboardGroupNonmemberPerm() {
		String[] perms = messageboardGroupNonmemberPerm.split(":");
		List<GroupPerm> list = new ArrayList<GroupPerm>();
		for (String perm : perms) {
			for (GroupPerm groupPerm : GroupPerm.all()) {
				if (perm.equalsIgnoreCase(groupPerm.name()))
					list.add(groupPerm);
			}
		}
		return list.toArray(new GroupPerm[list.size()]);
	}

	public static GroupPerm[] getMessageboardGroupMemberPerm() {
		String[] perms = messageboardGroupMemberPerm.split(":");
		List<GroupPerm> list = new ArrayList<GroupPerm>();
		for (String perm : perms) {
			for (GroupPerm groupPerm : GroupPerm.all()) {
				if (perm.equalsIgnoreCase(groupPerm.name()))
					list.add(groupPerm);
			}
		}
		return list.toArray(new GroupPerm[list.size()]);
	}
}
