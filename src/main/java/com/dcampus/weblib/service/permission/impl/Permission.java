package com.dcampus.weblib.service.permission.impl;

import com.dcampus.weblib.entity.Category;
import com.dcampus.weblib.entity.Group;
import com.dcampus.weblib.entity.Member;
import com.dcampus.weblib.entity.OldPerm;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.common.generic.GenericDao;
import com.dcampus.weblib.service.permission.IPermission;
import com.dcampus.weblib.service.permission.PermCollection;
import com.dcampus.weblib.service.permission.IPermission.CategoryPerm;
import com.dcampus.weblib.service.permission.IPermission.GroupPerm;

import static com.dcampus.weblib.service.permission.impl.PermUtil.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly=false)
public class Permission implements IPermission {

	@Autowired
	private GenericDao genericDao;
	
	@Autowired
	@Qualifier("grouperPermStrategy")
	private IPermStrategy permStrategic;

//	public void setPermStrategic(IPermStrategy strategic) {
//		permStrategic = strategic;
//	}

	private CategoryPerm[] getCategoryPerms(long memberId, long categoryId)
			throws GroupsException {
		CategoryPerm[] categoryPerms = PermCache.getCategoryPerm(memberId,
				categoryId);
		if (categoryPerms == null) {
			categoryPerms = permStrategic.getCategoryPerm(memberId, categoryId);

			PermCache.setCategoryPerm(memberId, categoryId, categoryPerms);
		}
		return categoryPerms;
	}

	private GlobalPerm[] getGlobalPerms(long memberId) throws GroupsException {
		GlobalPerm[] globalPerms = PermCache.getGlobalPerm(memberId);
		if (globalPerms == null) {
			globalPerms = permStrategic.getGlobalPerm(memberId);

			PermCache.setGlobalPerm(memberId, globalPerms);
		}
		return globalPerms;
	}
	
	private GroupPerm[] getGroupPerms(long memberId, long groupId)
			throws GroupsException {
		PermCollection pc = getGroupPermissionToGroup(memberId, groupId);
		return pc.getGroupPerm();
	}

	public PermCollection getGroupPermissionToGroup(long memberId, long groupId)
				throws GroupsException {
		PermCollection pc = null; //= PermCache.getGroupPermCollection(memberId, groupId);
		if (pc == null) {
			pc = permStrategic.getGroupPerm(memberId, groupId);
			PermCache.setGroupPermCollection(memberId, groupId, pc);
		}
		return pc;
	}
	public PermCollection getGroupPermissionToCategory(long memberId, long categoryId)
			throws GroupsException {
		PermCollection pc = PermCache.getGroupPermissionToCategory(memberId, categoryId);
		if (pc == null) {
			pc = new PermCollection();
			if (isAdmin(memberId)) {
				pc.addInheritedPerm(GroupPerm.all());
				PermCache.setGroupPermCollectionToCategory(memberId, categoryId, pc);
				return pc;
			}
			Category categoryBean = null;

			//categoryBean = CacheDao.getCategoryDao().getCategory(categoryId);
			//只有通用dao
			categoryBean = genericDao.get(Category.class, categoryId);
			//获得分类本身的权限
			OldPerm perm = permStrategic.getPerm(memberId, categoryId, OldPerm.PERM_TYPE_GROUP_OF_CATEGORY );
			if (perm != null) {
				pc.addGroupSelfPerm(PermUtil.convertGroupPerm(perm.getPermCode()));
			}
			//获得上层分类继承下来的权限
			if (perm == null || !perm.isOverrideParent()) {
				if (categoryBean.getParentId() > 0) {
					GroupPerm[] perms = permStrategic.getGroupPermIneritedCategory(memberId, categoryBean.getParentId(), true);
					if (perms != null) {
						pc.addInheritedPerm(perms);
					}
				}					
			}
		
			//pc.resetGroupPerm();
			PermCache.setGroupPermCollectionToCategory(memberId, categoryId, pc);
		}
		return pc;
	}
	

	public boolean hasCategoryPerm(long memberId, long categoryId,
			CategoryPerm perm) throws GroupsException {
		CategoryPerm[] categoryPerms = getCategoryPerms(memberId, categoryId);

		for (CategoryPerm categoryPerm : categoryPerms) {
			if (categoryPerm == IPermission.CategoryPerm.MANAGE_CATEGORY)
				return true;
			if (perm == categoryPerm)
				return true;
		}
		return false;
	}


	public boolean hasGlobalPerm(long memberId, GlobalPerm perm)
			throws GroupsException {
		GlobalPerm[] globalPerms = getGlobalPerms(memberId);

		for (GlobalPerm globalPerm : globalPerms) {
			if (perm == globalPerm)
				return true;
		}
		return false;
	}

	public boolean hasGroupPerm(long memberId, long groupId, GroupPerm perm)
			throws GroupsException {
		GroupPerm[] groupPerms = getGroupPerms(memberId, groupId);

		for (GroupPerm groupPerm : groupPerms) {
			if (groupPerm == IPermission.GroupPerm.MANAGE_GROUP || groupPerm == IPermission.GroupPerm.GROUP_APPMANAGER || groupPerm == IPermission.GroupPerm.GROUP_APPUSER)
				return true;
			if (perm == groupPerm)
				return true;
		}
		return false;
	}

	public PermCollection getMemberGlobalPerm(long memberId)
			throws GroupsException {
		PermCollection collection = new PermCollection();
		collection.addGlobalPerm(getGlobalPerms(memberId));
		return collection;
	}

	public PermCollection getMemberCategoryPerm(long memberId, long categoryId)
			throws GroupsException {
		PermCollection collection = new PermCollection();
		collection.addGlobalPerm(getGlobalPerms(memberId));
		collection.addCategoryPerm(getCategoryPerms(memberId, categoryId));
		return collection;
	}

	public PermCollection getMemberGroupPerm(long memberId, long groupId)
			throws GroupsException {
//		Group groupBean = CacheDao.getGroupDao().getGroup(groupId);
//		Category categoryBean = CacheDao.getCategoryDao().getCategory(
//				groupBean.getCategoryId());
		Group groupBean = genericDao.get(Group.class, groupId);
		Category categoryBean = genericDao.get(Category.class, groupBean.getCategory().getId());

		PermCollection collection = new PermCollection();
		collection.addGlobalPerm(getGlobalPerms(memberId));
		collection.addCategoryPerm(getCategoryPerms(memberId, categoryBean
				.getId()));
		collection.addGroupPerm(getGroupPerms(memberId, groupId));
		return collection;
	}

	/**
	 * 重置权限
	 */
	public void resetPermission(long memberId) throws GroupsException {
		PermCache.removeAllPermCache(memberId);
	}

	/**
	 * 重置所有用户权限
	 *
	 * @throws GroupsException
	 */
	public void resetPermission() throws GroupsException {
		PermCache.removeAllPermCache();
	}

	public void modifyMemberCategoryPerms(long memberId, long categoryId,
			CategoryPerm[] perms) throws GroupsException {
		long permCode = convert(perms);
		// 获取系统默认的权限
		long defaultPermCode = convert(PermProperty.getDefaultCategoryPerm());
		if (memberId <= 0) {
			memberId = OldPerm.GLOBAL_NONMEMBER_ID;
		}
		//FIXED 这里先不删除
		// 跟系统默认的相比，若一样，则删除该权限记录
		//if (permCode == defaultPermCode) {
		//	CacheDao.getPermDao().delete(memberId, categoryId,
		//			OldPerm.Type.CATEGORY);
		//} else {
			createOrUpdatePerm(memberId, categoryId,
					OldPerm.PERM_TYPE_CATEGORY, permCode);
		//}

		// 清楚权限缓存
		PermCache.removeAllPermCache(memberId);
	}


	public void modifyMemberGlobalPerms(long memberId, GlobalPerm[] perms)
			throws GroupsException {
		long permCode = convert(perms);
		// 获取系统默认的权限
		long defaultPermCode = convert(PermProperty.getDefaultGlobalPerm());
		if (memberId <= 0) {
			memberId = OldPerm.GLOBAL_NONMEMBER_ID;
		}
		//FIXED 这里先不删除
		// 跟系统默认的相比，若一样，则删除该权限记录
		//if (permCode == defaultPermCode) {
		//	CacheDao.getPermDao().delete(memberId, OldPerm.GLOBAL_TYPE_ID,
		//			OldPerm.Type.GLOBAL);
		//} else {
			createOrUpdatePerm(memberId, OldPerm.GLOBAL_TYPE_ID,
					OldPerm.PERM_TYPE_GLOBAL, permCode);
		//}

		// 清楚权限缓存
		PermCache.removeAllPermCache(memberId);
	}

	public void modifyMemberGroupPerms(long memberId, long groupId,
			GroupPerm[] perms) throws GroupsException {
		long permCode = convert(perms);
		// 获取系统默认的权限
		long defaultPermCode = 0;
		if (memberId < 0) {
			memberId = OldPerm.GLOBAL_NONMEMBER_ID;
			//defaultPermCode = convert(PermProperty
					//.getDefaultGroupNonmemberPerm());
		}
		defaultPermCode = convert(permStrategic.getDefaultGroupPerm(memberId, groupId));
		//FIXED 这里先不删除
		//if (permCode == defaultPermCode) {
			//CacheDao.getPermDao().delete(memberId, groupId,
				//	OldPerm.Type.GROUP);
		//} else {
			createOrUpdatePerm(memberId, groupId, OldPerm.PERM_TYPE_GROUP,
					permCode);
		//}
		// 清楚权限缓存
		PermCache.removeAllPermCache(memberId);
	}

	public void modifyMemberGroupInCategoryPerms(long memberId, long categoryId,
			GroupPerm[] perms) throws GroupsException {
		long permCode = convert(perms);
		if (memberId < 0) {
			memberId = OldPerm.GLOBAL_NONMEMBER_ID;
		}
		createOrUpdatePerm(memberId, categoryId, OldPerm.PERM_TYPE_GROUP_OF_CATEGORY,
				permCode);
		// 清楚权限缓存
		PermCache.removeAllPermCache(memberId);
	}
	private void createOrUpdatePerm(long memberId, long typeId,
			int type, long permCode) throws GroupsException {
		OldPerm bean = null;
		bean = genericDao.findFirst("from OldPerm p where p.memberId = ? and p.typeId = ? and p.permType = ?"
				, memberId, typeId, type);
		if (bean != null) {
			bean.setPermCode(permCode);
			genericDao.update(bean);
		} else {
			bean = new OldPerm();
			bean.setMemberId(memberId);
			bean.setPermCode(permCode);
			bean.setPermType(type);
			bean.setTypeId(typeId);
			genericDao.save(bean);
		}
//		try {
//			bean = genericDao.findFirst("from OldPerm p where p.member = ? and p.typeId = ? and p.permType = ?"
//					, member, typeId, type);
//			bean.setPermCode(permCode);
//			genericDao.update(bean);
//		} catch (DataNotFoundException e) {
//			bean = new Perm();
//			bean.setMemberId(memberId);
//			bean.setPermCode(permCode);
//			bean.setPermType(type);
//			bean.setTypeId(typeId);
//			CacheDao.getPermDao().create(bean);
//		}
	}

	public boolean isAdmin(long memberId) throws GroupsException {
		return permStrategic.isAdmin(memberId);
	}

	public boolean isGroupManager(long memberId, long groupId)
			throws GroupsException {
		return permStrategic.isGroupManager(memberId, groupId);
	}

	public boolean isSuperAdmin(long memberId) throws GroupsException {
		return permStrategic.isSuperAdmin(memberId);
	}

	public void removeGroupPermission(long groupId) throws GroupsException {
		//CacheDao.getPermDao().deleteByType(groupId, OldPerm.PERM_TYPE_GROUP); 
		this.deletePermByType(groupId, OldPerm.PERM_TYPE_GROUP);
		// 清楚权限缓存
		PermCache.removeAllPermCache();
	}

	public void removeCategoryPermission(long categoryId)
			throws GroupsException {
		//CacheDao.getPermDao().deleteByType(categoryId, OldPerm.PERM_TYPE_CATEGORY);
		this.deletePermByType(categoryId, OldPerm.PERM_TYPE_CATEGORY);
		// 清楚权限缓存
		PermCache.removeAllPermCache();
	}

	public boolean isProjectManager(long memberId) throws GroupsException {
		if (!isAdmin(memberId)) {
//			如果有创建圈子权限，则是项目管理员
			return this.hasCategoryPerm(memberId, OldPerm.GLOBAL_TYPE_ID,
					IPermission.CategoryPerm.CREATE_GROUP);	
		}
		return false;
	}

	public boolean hasCreatGroupPermission(long memberId) throws GroupsException {
		return permStrategic.hasCreatGroupPermission(memberId);
	}

	public boolean isContainMemberByTeam(long memberId, long userMemberId)throws GroupsException{
		return permStrategic.isContainMemberByTeam(memberId, userMemberId);
	}

	public boolean isReceivedResourceToMember(long resourceId, long memberId) {
		return permStrategic.isReceivedResourceToMember(resourceId, memberId);
	}
	
	private void deletePermByType(long typeId, int permType) {
		List<OldPerm> perms = genericDao.findAll("from OldPerm p where p.typeId = ? "
				+ "and p.permType = ?", typeId, permType);
		for (OldPerm p : perms) {
			genericDao.delete(p);
		}
	}
	
}
