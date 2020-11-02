package com.dcampus.sys.entity.keys;

import com.dcampus.common.paging.SimpleSearchItemKey;

public class UserSearchItemKey<T> extends SimpleSearchItemKey<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final UserSearchItemKey<String> CREATETYPE = new UserSearchItemKey<String>(
			"userCreateType", String.class);

	public static final UserSearchItemKey<String> STATUS = new UserSearchItemKey<String>(
			"userbaseStatus", String.class);

	public static final UserSearchItemKey<String> ACCOUNT = new UserSearchItemKey<String>(
			"account", String.class);

	public static final UserSearchItemKey<String> NAME = new UserSearchItemKey<String>(
			"name", String.class);

	public static final UserSearchItemKey<String> COMPANY = new UserSearchItemKey<String>(
			"company", String.class);

	public static final UserSearchItemKey<String> DEPARTMENT = new UserSearchItemKey<String>(
			"department", String.class);

	public static final UserSearchItemKey<String> POSITION = new UserSearchItemKey<String>(
			"position", String.class);

	public static final UserSearchItemKey<String> EMAIL = new UserSearchItemKey<String>(
			"email", String.class);

	public static final UserSearchItemKey<String> PHONE = new UserSearchItemKey<String>(
			"phone", String.class);

	public static final UserSearchItemKey<String> MOBILE = new UserSearchItemKey<String>(
			"mobile", String.class);

	public static final UserSearchItemKey<String> IM = new UserSearchItemKey<String>(
			"im", String.class);

	private UserSearchItemKey(String name, Class<T> valueClass) {
		super(name, valueClass);
	}

}
