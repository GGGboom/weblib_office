package com.dcampus.weblib.entity.keys;

import com.dcampus.common.paging.SimpleSortItemKey;

public class ResourceReceiveSortItemKey extends SimpleSortItemKey {

	public static final ResourceReceiveSortItemKey Id = new ResourceReceiveSortItemKey(
			"id");

	private ResourceReceiveSortItemKey(String name) {
		super(name);
	}

}
