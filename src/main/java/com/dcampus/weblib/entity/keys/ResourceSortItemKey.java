package com.dcampus.weblib.entity.keys;

import com.dcampus.common.paging.SimpleSortItemKey;

public class ResourceSortItemKey extends SimpleSortItemKey {

	public static final ResourceSortItemKey CreationDate = new ResourceSortItemKey(
			"createDate");

	public static final ResourceSortItemKey Type = new ResourceSortItemKey(
			"resourceType");

	public static final ResourceSortItemKey Priority = new ResourceSortItemKey(
			"priority");
	
	public static final ResourceSortItemKey DefaultFolder = new ResourceSortItemKey(
			"defaultFolder");
	
	public ResourceSortItemKey(String name) {
		super(name);
	}

	
}
