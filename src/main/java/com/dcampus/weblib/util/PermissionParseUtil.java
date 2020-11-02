package com.dcampus.weblib.util;

import java.util.List;

import com.dcampus.weblib.service.permission.IPermission.CategoryPerm;
import com.dcampus.weblib.service.permission.IPermission.GroupPerm;

public class PermissionParseUtil {

	public static void parseGroupPermission(List<GroupPerm> list, String type) {
		if (type == null)
			return;

		type = type.toLowerCase();
		if (type.indexOf("view") != -1) {
			list.add(GroupPerm.VIEW_RESOURCE);
		}
		if (type.indexOf("upload") != -1) {
			list.add(GroupPerm.UPLOAD_RESOURCE);
		}
		if (type.indexOf("delete") != -1) {
			list.add(GroupPerm.DELETE_RESOURCE);
		}
		if (type.indexOf("modify") != -1) {
			list.add(GroupPerm.MODIFY_RESOURCE);
		}
		if (type.indexOf("adddir") != -1) {
			list.add(GroupPerm.ADD_FOLDER);
		}
		if (type.indexOf("manage") != -1) {
			list.add(GroupPerm.MANAGE_GROUP);
		}
	}
	
	public static void parseCategoryPermission(List<CategoryPerm> list, String type) {
		if (type == null)
			return;

		type = type.toLowerCase();
		if (type.indexOf("view") != -1) {
			list.add(CategoryPerm.VIEW_CATEGORY);
		}
		if (type.indexOf("addcategory") != -1) {
			list.add(CategoryPerm.CREATE_CATEGORY);
		}
		if (type.indexOf("addgroup") != -1) {
			list.add(CategoryPerm.CREATE_GROUP);
		}
		if (type.indexOf("manage") != -1) {
			list.add(CategoryPerm.MANAGE_CATEGORY);
		}
	}
}
