package com.dcampus.sys.entity.keys;

import com.dcampus.common.paging.SimpleSortItemKey;

public class UserSortItemKey extends SimpleSortItemKey {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final UserSortItemKey ID = new UserSortItemKey("id");
	
	public static final UserSortItemKey ACCOUNT = new UserSortItemKey("account");
	
	public static final UserSortItemKey NAME = new UserSortItemKey("name");
	
	public static final UserSortItemKey STATUS = new UserSortItemKey("userbaseStatus");

	private UserSortItemKey(String name) {
		super(name);
	}
}
