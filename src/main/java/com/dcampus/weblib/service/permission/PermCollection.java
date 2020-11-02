package com.dcampus.weblib.service.permission;

import static com.dcampus.weblib.service.permission.impl.PermUtil.addPermission;
import static com.dcampus.weblib.service.permission.impl.PermUtil.deletePermission;

import java.util.HashSet;
import java.util.Set;

import com.dcampus.weblib.service.permission.IPermission.CategoryPerm;
import com.dcampus.weblib.service.permission.IPermission.GlobalPerm;
import com.dcampus.weblib.service.permission.IPermission.GroupPerm;
import com.dcampus.weblib.service.permission.impl.PermUtil;

/**
 * 权限集合类
 * 
 * Patrick修改
 * 去掉了所有跟讨论区forum有关的操作
 *
 * @author zim
 *
 */
public class PermCollection {

	private long gloPerm;

	private Set<GlobalPerm> globalPerms = new HashSet<GlobalPerm>();

	private long cPerm;

	private Set<CategoryPerm> categoryPerms = new HashSet<CategoryPerm>();

	private long gPerm;

	private Set<GroupPerm> groupPerms = new HashSet<GroupPerm>();

	private long groupSelfPerm;

	private Set<GroupPerm> groupSelfPerms = new HashSet<GroupPerm>();
	
	private long inheritedPerm;
	
	private Set<GroupPerm> groupInheritedCategoryPerms = new HashSet<GroupPerm>();
	

	public PermCollection() {

	}

	public PermCollection(GlobalPerm[] gloPerms, CategoryPerm[] cPerms,
			GroupPerm[] gPerms) {
		for (GlobalPerm perm : gloPerms) {
			this.gloPerm |= perm.getMask();
			this.globalPerms.add(perm);
		}

		for (CategoryPerm perm : cPerms) {
			this.cPerm |= perm.getMask();
			this.categoryPerms.add(perm);
		}

		for (GroupPerm perm : gPerms) {
			this.gPerm |= perm.getMask();
			this.groupPerms.add(perm);
		}

	}

	public GlobalPerm[] getGlobalPerm() {
		return categoryPerms.toArray(new GlobalPerm[0]);
	}

	public CategoryPerm[] getCategoryPerm() {
		return categoryPerms.toArray(new CategoryPerm[0]);
	}

	public GroupPerm[] getGroupPerm() {
		return groupPerms.toArray(new GroupPerm[0]);
	}


	public GroupPerm[] getGroupInheritedCategoryPerm() {
		return groupInheritedCategoryPerms.toArray(new GroupPerm[0]);
	}
	
	public GroupPerm[] getGroupSelfPerm() {
		return groupSelfPerms.toArray(new GroupPerm[0]);
	}

	public PermCollection full() {
		addGlobalPerm(IPermission.GlobalPerm.all());
		addCategoryPerm(IPermission.CategoryPerm.all());
		addGroupPerm(IPermission.GroupPerm.all());

		return this;
	}

	// ///////////////////////////////////////////////////////////////////////
	/**
	 * 获取全局权限
	 *
	 * @param addPerms
	 */
	public void addGlobalPerm(GlobalPerm[] addPerms) {
		for (GlobalPerm perm : addPerms) {
			gloPerm = addPermission(gloPerm, perm.getMask());
			globalPerms.add(perm);
		}
	}

	/**
	 * 删除全局权限
	 *
	 * @param removePerms 移除的全局权限
	 */
	public void removeGlobalPerm(GlobalPerm[] removePerms) {
		for (GlobalPerm perm : removePerms) {
			gloPerm = deletePermission(gloPerm, perm.getMask());
			globalPerms.remove(perm);
		}
	}

	// ///////////////////////////////////////////////////////////////////////
	/**
	 * 添加分类权限
	 *
	 * @param addPerms 添加的具体权限
	 */
	public void addCategoryPerm(CategoryPerm[] addPerms) {
		for (CategoryPerm perm : addPerms) {
			cPerm = addPermission(cPerm, perm.getMask());
			categoryPerms.add(perm);
		}
	}

	/**
	 * 移除分类权限
	 *
	 * @param removePerms 移除的具体权限
	 */
	public void removeCategoryPerm(CategoryPerm[] removePerms) {
		for (CategoryPerm perm : removePerms) {
			cPerm = deletePermission(cPerm, perm.getMask());
			categoryPerms.remove(perm);
		}
	}

	// ///////////////////////////////////////////////////////////////////////
	/**
	 * 添加圈子权限
	 *
	 * @param addPerms 具体权限
	 */
	public void addGroupPerm(GroupPerm[] addPerms) {
		for (GroupPerm perm : addPerms) {
			gPerm = addPermission(gPerm, perm.getMask());
			groupPerms.add(perm);
			
			//allGroupPerm = addPermission(allGroupPerm, perm.getMask());
			//allGroupPerms.add(perm);
		}
	}

	/**
	 * 移除圈子权限
	 *
	 * @param removePerms 移除的内容
 	 */
	public void removeGroupPerm(GroupPerm[] removePerms) {
		for (GroupPerm perm : removePerms) {
			gPerm = deletePermission(gPerm, perm.getMask());
			groupPerms.remove(perm);
		}
	}
	
	public void removeGroupSelfPerm(GroupPerm[] removePerms) {
		for (GroupPerm perm : removePerms) {
			groupSelfPerm = deletePermission(gPerm, perm.getMask());
			groupSelfPerms.remove(perm);
		}
	}
//	 ///////////////////////////////////////////////////////////////////////
	/**
	 * 添加圈子继承的权限
	 *
	 * @param addPerms  具体权限
	 */
	public void addInheritedPerm(GroupPerm[] addPerms) {
		if (addPerms != null) {
			for (GroupPerm perm : addPerms) {
				inheritedPerm = addPermission(inheritedPerm, perm.getMask());
				groupInheritedCategoryPerms.add(perm);
				groupPerms.add(perm);
				gPerm = addPermission(gPerm, perm.getMask());
			}
		}
		
	}
	
	public void addGroupSelfPerm(GroupPerm[] addPerms) {
		if (addPerms != null) {
			for (GroupPerm perm : addPerms) {
				groupSelfPerm = addPermission(groupSelfPerm, perm.getMask());
				groupSelfPerms.add(perm);
				groupPerms.add(perm);
				gPerm = addPermission(gPerm, perm.getMask());
			}
		}
		
	}

	public void resetGroupPerm() {
		groupPerms.clear();
		groupPerms.addAll(groupSelfPerms);
		groupPerms.addAll(groupInheritedCategoryPerms);
	}

	// ///////////////////////////////////////////////////////////////////////

	public static void main(String[] args) {
		Set<GroupPerm> groupPerms = new HashSet<GroupPerm>();
		GroupPerm p = GroupPerm.ADD_FOLDER;
		GroupPerm p2 = GroupPerm.ADD_FOLDER;
		
		groupPerms.add(p);
		groupPerms.add(p2);
		System.out.println(groupPerms.size());
	}
}
