package com.dcampus.weblib.service;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import com.dcampus.weblib.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dcampus.common.config.PropertyUtil;
import com.dcampus.common.config.ResourceProperty;
import com.dcampus.common.service.BaseService;
import com.dcampus.common.util.CacheUtil;
import com.dcampus.common.config.LogMessage;
import com.dcampus.common.generic.GenericDao;
import com.dcampus.sys.util.UserUtils;
import com.dcampus.weblib.dao.CategoryDao;
import com.dcampus.weblib.exception.PermissionsException;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.service.permission.IPermission.CategoryPerm;
import com.dcampus.weblib.service.permission.IPermission.GlobalPerm;
import com.dcampus.weblib.service.permission.RolePermissionUtils;
import com.dcampus.weblib.service.permission.impl.Permission;
import com.dcampus.weblib.util.CheckUtil;

/**
 * 分类操作service
 * @author patrick
 *
 */
@Service
@Transactional(readOnly=false)
public class CategoryService extends BaseService{
	@Autowired
	private CategoryDao categoryDao;
	
	@Autowired
	private GenericDao genericDao;
	
	@Autowired
	private LogService logService;
	
	@Autowired
	private RolePermissionUtils roleUtils;
	
	@Autowired
	private Permission permission;

	@Autowired
	@Lazy
	private  GroupService groupService;
	
	public void createCategory(Category category , boolean mark) {
		long memberId = UserUtils.getCurrentMemberId();
		this.createCategory(category, mark, memberId);
	}

	public void saveOrUpdateCategory(Category category) {
		categoryDao.saveOrUpdateCategory(category);
	}
	/**
	 * 创建分类，根据配置支持有限层级的分类
	 *
	 * @param category
	 * @param mark 是否要进行特殊字符检验
	 */
	public void createCategory(Category category , boolean mark, long memberId) throws GroupsException{
		if (memberId != OldPerm.SYSTEMADMIN_MEMBER_ID) {
			//判断权限
			boolean flag = permission.hasGlobalPerm(UserUtils.getCurrentMemberId(),
					GlobalPerm.CREATE_CATEGORY);
			if (!flag) {
				flag = permission.hasCategoryPerm(UserUtils.getCurrentMemberId(),
						category.getParentId(), CategoryPerm.CATEGORY_APPMANAGER);
			}

			if (category.getParentId() > 0) {
				if (!flag
						&& !permission.hasCategoryPerm(UserUtils.getCurrentMemberId(),
								category.getParentId(), CategoryPerm.CREATE_CATEGORY))
					throw PermissionsException.CategoryException;
			} else {
				if (!flag)
					throw PermissionsException.CategoryException;
			}
			if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_storage_m")){
				throw new PermissionsException("无存储管理权限");
			}
		}
		
		this.checkCategoryDepth(category);
		if(mark == true){
			CheckUtil.checkName(category.getName());
		}
		if (category.getParentId() <0)
			category.setParentId(0);
		categoryDao.saveOrUpdateCategory(category);
		
		String desc = LogMessage.getCategoryAdd(category.getDisplayName());
		logService.addOperateLog(Log.ACTION_TYPE_ADD, desc, category.getId(), category.getDisplayName());
		
		// 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		CacheUtil.removeAll(CacheUtil.categoryGroupCache);
	}
	/**
	 * 创建分类，根据配置支持有限层级的分类
	 *
	 * @param category
	 * @param mark 是否要进行特殊字符检验
	 */
	public void createCategory4Init(Category category , boolean mark, long memberId) throws GroupsException{
		if (memberId != OldPerm.SYSTEMADMIN_MEMBER_ID) {
			//判断权限
			boolean flag = permission.hasGlobalPerm(UserUtils.getCurrentMemberId(),
					GlobalPerm.CREATE_CATEGORY);

			if (!flag) {
				flag = permission.hasCategoryPerm(UserUtils.getCurrentMemberId(),
						category.getParentId(), CategoryPerm.CATEGORY_APPMANAGER);
			}

			if (category.getParentId() > 0) {
				if (!flag
						&& !permission.hasCategoryPerm(UserUtils.getCurrentMemberId(),
								category.getParentId(), CategoryPerm.CREATE_CATEGORY))
					throw PermissionsException.CategoryException;
			} else {
				if (!flag)
					throw PermissionsException.CategoryException;
			}
			if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_storage_m")){
				throw new PermissionsException("无存储管理权限");
			}
		}
		
		this.checkCategoryDepth(category);
		if(mark == true){
			CheckUtil.checkName(category.getName());
		}
		if (category.getParentId() <0)
			category.setParentId(0);
		categoryDao.saveOrUpdateCategory(category);
		
		String desc = LogMessage.getCategoryAdd(category.getDisplayName());
		//logService.addOperateLog(Log.ACTION_TYPE_ADD, desc, category.getId(), category.getDisplayName());

	}

	/**
	 * 删除分类，将删除子分类和所有分类下的柜子
	 * 之前是先删柜子再删分类
	 * 现在数据库有级联，自动删除分类下的所有柜子
	 * 删除后需要删除该分类的特殊权限设置
	 *
	 * @param id
	 */
	public void deleteCategory(long id) {
		//判断权限
		if (!permission.hasGlobalPerm(UserUtils.getCurrentMemberId(),
				GlobalPerm.DELETE_CATEGORY)
				&& !permission.hasCategoryPerm(UserUtils.getCurrentMemberId(), id,
						CategoryPerm.MANAGE_CATEGORY)
				&& !permission.hasCategoryPerm(UserUtils.getCurrentMemberId(), id,
						CategoryPerm.CATEGORY_APPMANAGER))
			throw PermissionsException.CategoryException;
		if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_storage_m")){
			throw new PermissionsException("无存储管理权限");
		}
		this.checkCategoryRelatedToDomain(id);
		List<Category> subCategories = categoryDao.getCategoriesByParent(id);
		Category myself = categoryDao.getCategoryById(id);
		for (Category category : subCategories) {
			categoryDao.deleteCategory(category);
		}
		categoryDao.deleteCategory(myself);
		
		String desc = LogMessage.getCategoryDelete(myself.getDisplayName());
		logService.addOperateLog(Log.ACTION_TYPE_ADD, desc, myself.getId(), myself.getDisplayName());
		CacheUtil.removeAll(CacheUtil.categoryGroupCache);
	}
	
	

	private void checkCategoryRelatedToDomain(long cid) throws GroupsException{
		// TODO Auto-generated method stub
	      String hql="from "+DomainCategory.class.getName()+" as dc where dc.category.id=?";
	      Object[] param = new Object[] {cid};
	      Query q = genericDao.createQuery(hql, param);
	      List<DomainCategory> result = q.getResultList();
	      if(result!=null&&result.size()>0) {
	         throw new GroupsException("该分类已绑定到域，无法直接删除。尝试先删除所绑定域："+result.get(0).getDomain().getDomainName()+"");
	      }
	      return;
	}

	/**
	 * 移动分类
	 * 使用时需要记录日志
	 *
	 * @param id
	 * @param newParentId
	 * @throws GroupsException
	 */
	public void moveCategory(long id, long newParentId) throws GroupsException {
		//判断权限
		boolean flag = permission.hasGlobalPerm(UserUtils.getCurrentMemberId(),
				GlobalPerm.MOVE_CATEGORY);
		if (!flag) {
			flag = permission.hasCategoryPerm(UserUtils.getCurrentMemberId(), id,
					CategoryPerm.CATEGORY_APPMANAGER);
		}
		if (newParentId > 0) {
			boolean manageOldCate = permission.hasCategoryPerm(
					UserUtils.getCurrentMemberId(), id, CategoryPerm.MANAGE_CATEGORY);
			boolean manageNewCate = permission.hasCategoryPerm(
					UserUtils.getCurrentMemberId(), newParentId,
					CategoryPerm.MANAGE_CATEGORY);
			if (!flag && !(manageOldCate && manageNewCate))
				throw PermissionsException.CategoryException;
		} else {
			if (!flag)
				throw PermissionsException.CategoryException;
		}
		if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_storage_m")){
			throw new PermissionsException("无存储管理权限");
		}
		//权限判断完毕
		if (newParentId > 0) {
			Category targetBean = categoryDao.getCategoryById(newParentId);

			if (targetBean.getCategoryStatus() != Category.STATUS_NORMAL)
				throw new GroupsException(ResourceProperty
						.getNotNormalCategoryString());

			// 如果目标父分区的深度已经到达最大，则不能再加入子分区了
			int currDepth = this.getCategoryDepth(targetBean);
			if (currDepth >= PropertyUtil.getCategoryMaxDepth())
				throw new GroupsException(ResourceProperty
						.getCategoryReachMaxDepthString());
		}
		

		// 分类不能移动到其子孙分类中，检查newParentId是否是id分类的子孙
		if (id == newParentId)
			throw new GroupsException(ResourceProperty
					.getCannotMoveCategoryString());

		List<Long> list = new ArrayList<Long>();
		list.add(id);
		for (int i = 0; i < list.size(); ++i) {
			List<Category> beans = categoryDao.getCategoriesByParent(list.get(i));
			
			for (Category bean : beans) {
				if (newParentId == bean.getId())
					throw new GroupsException(ResourceProperty
							.getCannotMoveCategoryString());

				list.add(bean.getId());
			}
		}

		// 移动分类
		Category bean = categoryDao.getCategoryById(id);
		bean.setParentId(newParentId);
		categoryDao.saveOrUpdateCategory(bean);
		Category targetBean = categoryDao.getCategoryById(newParentId);
		String desc = LogMessage.getCategoryMove(bean.getDisplayName(), targetBean.getDisplayName());
		logService.addOperateLog(Log.ACTION_TYPE_ADD, desc, bean.getId(), bean.getDisplayName());
		
		// 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		CacheUtil.removeAll(CacheUtil.myVisualGroupCache);
		CacheUtil.removeAll(CacheUtil.categoryGroupCache);
	}

	/**
	 * 关闭分类
	 * @param id
	 */
	public void closeCategory(long id) {
		//判断权限
		if (!permission.hasGlobalPerm(UserUtils.getCurrentMemberId(),
				GlobalPerm.CLOSE_CATEGORY)
				&& !permission.hasCategoryPerm(UserUtils.getCurrentMemberId(), id,
						CategoryPerm.MANAGE_CATEGORY)
				&& !permission.hasCategoryPerm(UserUtils.getCurrentMemberId(), id,
						CategoryPerm.CATEGORY_APPMANAGER))
			throw PermissionsException.CategoryException;
		
		if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_storage_m")){
			throw new PermissionsException("无存储管理权限");
		}
		
		Category category = categoryDao.getCategoryById(id);
		category.setCategoryStatus(Category.STATUS_CLOSE);
		categoryDao.saveOrUpdateCategory(category);
		
		String desc = LogMessage.getCategoryClose(category.getDisplayName());
		logService.addOperateLog(Log.ACTION_TYPE_CLOSE, desc, id, category.getDisplayName());
		// 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		CacheUtil.removeAll(CacheUtil.myVisualGroupCache);
	}

	/**
	 * 恢复分类，重新开启该分类，相对于closeCategory
	 * @param id
	 */
	public void restoreCategory(long id) {
		//判断权限
		if (!permission.hasGlobalPerm(UserUtils.getCurrentMemberId(),
				GlobalPerm.CLOSE_CATEGORY)
				&& !permission.hasCategoryPerm(UserUtils.getCurrentMemberId(), id,
						CategoryPerm.CATEGORY_APPMANAGER))
			throw PermissionsException.CategoryException;
		if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_storage_m")){
			throw new PermissionsException("无存储管理权限");
		}
		Category category = categoryDao.getCategoryById(id);
		category.setCategoryStatus(Category.STATUS_NORMAL);
		categoryDao.saveOrUpdateCategory(category);
		
		String desc = LogMessage.getCategoryOpen(category.getDisplayName());
		logService.addOperateLog(Log.ACTION_TYPE_OPEN, desc, id, category.getDisplayName());
		
		// 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		CacheUtil.removeAll(CacheUtil.myVisualGroupCache);
		CacheUtil.removeAll(CacheUtil.categoryGroupCache);
	}

	/**
	 * 批量获取分类信息
	 *
	 * @param parentId
	 * @param start
	 * @param limit
	 * @return
	 * @throws GroupsException
	 */
	public List<Category> getCategorieByParent(long parentId,int start,int limit) {
		if (permission.isAdmin(UserUtils.getCurrentMemberId())) {
			return categoryDao.getCategoriesByParent(parentId,start,limit);
		} else {
			return categoryDao.getCategoriesByParent(parentId, Category.STATUS_NORMAL,start,limit);
		}
	}

	/**
	 * 批量获取分类信息
	 *
	 * @param parentId
	 * @return
	 * @throws GroupsException
	 */
	public List<Category> getCategorieByParent(long parentId) {
		if (permission.isAdmin(UserUtils.getCurrentMemberId())) {
			return categoryDao.getCategoriesByParent(parentId);
		} else {
			return categoryDao.getCategoriesByParent(parentId, Category.STATUS_NORMAL);
		}
	}

	public long getCategoriesTotalCount(long parentId)
	{
		return genericDao.findFirst("select count(ctc) from Category ctc where ctc.parentId=?1",parentId);
	}
	
	/**
	 * 根据分类名批量获取分类信息
	 *
	 * @param name
	 * @return
	 */
	public List<Category> getCategoriesByName(String name) {
		return categoryDao.getCategoriesByName(name);
	}
	
	/**
	 * 根据分类名获取分类信息
	 *
	 * @param name
	 * @return
	 */
	public Category getCategoryByName(String name) {
		return categoryDao.getCategoryByName(name);
	}

	/**
	 * 获取分类信息
	 *
	 * @param id
	 * @return
	 * @throws GroupsException
	 */
	public Category getCategoryById(long id) {
		return categoryDao.getCategoryById(id);
	}



	/**
	 * 更改分类的名字和描述信息
	 *
	 * @param id
	 * @param displayName
	 * @param desc
	 */
	public void modifyCategory(long id, String displayName, String desc) {
		//判断权限
		if (!permission.hasCategoryPerm(UserUtils.getCurrentMemberId(), id,
				CategoryPerm.MODIFY_CATEGORY)
				&& !permission.hasCategoryPerm(UserUtils.getCurrentMemberId(), id,
						CategoryPerm.CATEGORY_APPMANAGER))
			throw PermissionsException.CategoryException;
		if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_storage_m")){
			throw new PermissionsException("无存储管理权限");
		}
		
		CheckUtil.checkName(displayName);
		Category category = categoryDao.getCategoryById(id);
		category.setDisplayName(displayName);
		category.setDesc(desc);
		categoryDao.saveOrUpdateCategory(category);
		
		String description = LogMessage.getCategoryNameDescMod(category.getDisplayName());
		logService.addOperateLog(Log.ACTION_TYPE_MODIFY, description, id, category.getDisplayName());
		// 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		CacheUtil.removeAll(CacheUtil.categoryGroupCache);
	}
	public void modifyCategory_v2(long id, String displayName, String desc,
							   long total_capacity,String creatorName) {
		//判断权限
		if (!permission.hasCategoryPerm(UserUtils.getCurrentMemberId(), id,
				CategoryPerm.MODIFY_CATEGORY)
				&& !permission.hasCategoryPerm(UserUtils.getCurrentMemberId(), id,
				CategoryPerm.CATEGORY_APPMANAGER))
			throw PermissionsException.CategoryException;
		if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_storage_m")){
			throw new PermissionsException("无存储管理权限");
		}

		CheckUtil.checkName(displayName);
		Category category = categoryDao.getCategoryById(id);
		if(category.getTotalCapacity()!=null){
			if(total_capacity <0){
				throw new GroupsException("分类总容量应大于等于0！");
			}
		}
		//已经使用的容量
		if(category.getTotalCapacity()!=null){//不为空的情况下
			Long usedSize=category.getTotalCapacity()-category.getAvailableCapacity();
			if(total_capacity<usedSize)
				throw new GroupsException("分类的总容量不能小于分类的已用容量");
			category.setAvailableCapacity(total_capacity-usedSize);
			category.setTotalCapacity(total_capacity);
		}
		else{//为空的情况下
			Long totalCapacity =0L;//已经使用的容量
			totalCapacity =groupService.getAllGroupSpaceSize(id);
			if(totalCapacity>total_capacity)
				throw new GroupsException("分类的总容量不能小于分类的已用容量");
			else {//设置可用容量
				category.setAvailableCapacity(total_capacity-totalCapacity);
			}
		}


		category.setDisplayName(displayName);
		category.setDesc(desc);


		category.setCreatorName(creatorName);
		categoryDao.saveOrUpdateCategory(category);

		String description = LogMessage.getCategoryNameDescMod(category.getDisplayName());
		logService.addOperateLog(Log.ACTION_TYPE_MODIFY, description, id, category.getDisplayName());
		// 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		CacheUtil.removeAll(CacheUtil.categoryGroupCache);
	}

	public void modifyCategory(long id, String displayName) {
		long memberId =UserUtils.getCurrentMemberId();
		this.modifyCategory(id, displayName, memberId);
	}
	/**
	 * 更改分类的名字
	 *
	 * @param id
	 * @param displayName
	 */
	public void modifyCategory(long id, String displayName, long memberId) {
		if (memberId != OldPerm.SYSTEMADMIN_MEMBER_ID) {
			//判断权限
			if (!permission.hasCategoryPerm(memberId, id,
					CategoryPerm.MODIFY_CATEGORY)
					&& !permission.hasCategoryPerm(memberId, id,
							CategoryPerm.CATEGORY_APPMANAGER))
				throw PermissionsException.CategoryException;
			if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_storage_m")){
				throw new PermissionsException("无存储管理权限");
			}
		}
		CheckUtil.checkName(displayName);
		
		Category category = categoryDao.getCategoryById(id);
		String sName = category.getDisplayName();
		category.setDisplayName(displayName);
		categoryDao.saveOrUpdateCategory(category);
		
		String description = LogMessage.getCategoryNameMod(sName, displayName);
		logService.addOperateLog(Log.ACTION_TYPE_MODIFY, description, id, category.getDisplayName());
		// 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		CacheUtil.removeAll(CacheUtil.categoryGroupCache);
		
	}

	/**
	 * 更改分类的名字
	 *
	 * @param id
	 * @param displayName
	 */
	public void modifyCategory4Init(long id, String displayName, long memberId) {
		if (memberId != OldPerm.SYSTEMADMIN_MEMBER_ID) {
			//判断权限
			if (!permission.hasCategoryPerm(memberId, id,
					CategoryPerm.MODIFY_CATEGORY)
					&& !permission.hasCategoryPerm(memberId, id,
					CategoryPerm.CATEGORY_APPMANAGER))
				throw PermissionsException.CategoryException;
			if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_storage_m")){
				throw new PermissionsException("无存储管理权限");
			}
		}
		CheckUtil.checkName(displayName);

		Category category = categoryDao.getCategoryById(id);
		String sName = category.getDisplayName();
		category.setDisplayName(displayName);
		categoryDao.saveOrUpdateCategory(category);

		// 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		CacheUtil.removeAll(CacheUtil.categoryGroupCache);

	}


	/**
	 * 获得某一层分类下面的最大顺序号
	 * @param parentId
	 * @return
	 */
	public double getMaxOrder(long parentId) throws GroupsException{
		return categoryDao.getMaxOrderByParent(parentId);
	}
	/**
	 * 修改分类顺序号
	 * @param id
	 * @param order
	 */
	public void modifyCategoryOrder(long id, double order) {
		//判断权限
		if (!permission.hasCategoryPerm(UserUtils.getCurrentMemberId(), id,
				CategoryPerm.MODIFY_CATEGORY)
				&& !permission.hasCategoryPerm(UserUtils.getCurrentMemberId(), id,
						CategoryPerm.CATEGORY_APPMANAGER))
			throw PermissionsException.CategoryException;
		if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_storage_m")){
			throw new PermissionsException("无存储管理权限");
		}
		Category category = categoryDao.getCategoryById(id);
		if (order == category.getOrder()) {
			return;
		}
		category.setOrder(order);
		categoryDao.saveOrUpdateCategory(category);
		
		String description = LogMessage.getCategorySequenceMod(category.getDisplayName());
		logService.addOperateLog(Log.ACTION_TYPE_MODIFY, description, id, category.getDisplayName());
		// 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		CacheUtil.removeAll(CacheUtil.categoryGroupCache);
	}
	
	/**
	 * 根据分类id获得上溯分类
	 * @param categoryId
	 * @return
	 */
	public List<Category> tracedCategoryList(long categoryId) {
		return categoryDao.tracedCategoryList(categoryId);
	}
	/**
	 * 根据分类id获取子分类
	 * @param categoryId
	 * @param recursive 是否递归
	 * @return
	 */
	public List<Category> getSubCategoryList(long categoryId, boolean recursive) {
		return categoryDao.getSubCategoryList(categoryId, recursive);
	}
	
	/**
	 * 获取所有的分类
	 * @return
	 */
	public List<Category> getAllCategories() {
		return categoryDao.getAllCategories();
	}
	/**
	 * 判断分类的深度是否超标
	 * @param category
	 * @throws GroupsException
	 */
	private void checkCategoryDepth(Category category) throws GroupsException {
		int currDepth = this.getCategoryDepth(category);

		// 判断深度是否超标
		if (currDepth > PropertyUtil.getCategoryMaxDepth())
			throw new GroupsException(ResourceProperty
					.getCategoryReachMaxDepthString());
	}
	
	/**
	 * 获取分类的深度
	 * @param category
	 * @return
	 */
	private int getCategoryDepth(Category category) {
		int currDepth = 1;
		while (true) {
			// 深度为1
			if (category.getParentId() == 0)
				break;

			category = categoryDao.getCategoryById(category.getParentId());
			++currDepth;
		}
		return currDepth;
	}
	
	
	
}
