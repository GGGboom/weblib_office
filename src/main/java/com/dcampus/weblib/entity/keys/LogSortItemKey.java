package com.dcampus.weblib.entity.keys;

import com.dcampus.common.paging.SimpleSortItemKey;

public class LogSortItemKey extends SimpleSortItemKey {

	public static final LogSortItemKey CreateDate = new LogSortItemKey("createDate");

	public LogSortItemKey(String name) {
		super(name);
	}

}
