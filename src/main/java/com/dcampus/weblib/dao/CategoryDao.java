package com.dcampus.weblib.dao;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.dcampus.common.generic.GenericDao;
import com.dcampus.weblib.entity.Category;
import com.dcampus.weblib.exception.GroupsException;
import org.springframework.transaction.annotation.Transactional;

/**
 * 增加对父亲的判断，0和NULL的转换
 * @author patrick
 *
 */
@Repository
public class CategoryDao {

	@Autowired
	private GenericDao genericDao;

	public Category getCategoryByParentIdAndDisplayName(Long parentId,String displayName){
		String hql="from Category where parentId=?1 and displayName=?2";
		Category category=genericDao.findFirst(hql,parentId,displayName);
		if(category!=null)
		   return category;
		else
			return null;
	}

	@Transactional
	public void saveOrUpdateCategory(Category category) {
		if(category.getId() != null && category.getId() > 0){
			genericDao.update(category);
		} else {
			genericDao.save(category);
		}
	}
	
	public void deleteCategory(Category category) {
		Category g = genericDao.get(Category.class, category.getId());
		genericDao.delete(g);
	}
	
	public void deleteCategories(Collection<Category> categories) {
		for (Category g : categories) {
			deleteCategory(g);
		}
	}
	
	/**
	 * 获取某个分类
	 *
	 * @param id
	 * @return
	 */
	public Category getCategoryById(long id) {
		return genericDao.get(Category.class, id);
	}
	
	/**
	 * 根据id;status批量获取子分类信息
	 * @param parentId
	 * @param status
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<Category> getCategoriesByParent(long parentId, int status,int start,int limit) throws GroupsException {
		return genericDao.findAll(start,limit,"from Category c where c.parentId = ?1 and c.categoryStatus = ?2 order by c.order asc", parentId, status);
	}

	/**
	 * 根据id;status批量获取子分类信息
	 * @param parentId
	 * @param status
	 * @return
	 */
	public List<Category> getCategoriesByParent(long parentId, int status) throws GroupsException {
		return genericDao.findAll("from Category c where c.parentId = ?1 and c.categoryStatus = ?2 order by c.order asc", parentId, status);
	}


	/**
	 * 根据id批量获取所有状态子分类信息
	 * @param id
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<Category> getCategoriesByParent(long id,int start,int limit) {
		return genericDao.findAll(start,limit,"from Category c where c.parentId = ?1 order by c.order asc", id);
	}

	/**
	 * 根据id批量获取所有状态子分类信息
	 * @param id
	 * @return
	 */
	public List<Category> getCategoriesByParent(long id) {
		return genericDao.findAll("from Category c where c.parentId = ?1 order by c.order asc", id);
	}
	
	/**
	 * 根据分类名批量获取分类信息
	 *	
	 * @param name
	 * @return
	 */
	public List<Category> getCategoriesByName(String name) {
		return genericDao.findAll("from Category c where c.name = ?1", name);
	}
	
	/**
	 * 根据分类名获取分类信息
	 *	
	 * @param name
	 * @return
	 */
	public Category getCategoryByName(String name) {
		return genericDao.findFirst("from Category c where c.name = ?1 ", name);
	}
	
	/**
	 * 根据父亲获得子分类 最大顺序号
	 * @param id
	 * @return
	 */
	public double getMaxOrderByParent(long id) {
		Double result = genericDao.findFirst("select max(c.order) from Category c where parentId = ?1", id);
		return result == null ? 0.0 : result.doubleValue();
	}
	
	/**
	 * 根据分类id获得上溯分类
	 * @param categoryId
	 * @return
	 */
	public List<Category> tracedCategoryList(long categoryId) {
		List<Category> list = new ArrayList<Category>();
		Category bean =  this.getCategoryById(categoryId);
		list.add(bean);
		long pid = bean.getParentId();
		while (pid > 0) {
			Category temp = this.getCategoryById(pid);
			list.add(temp);
			pid = temp.getParentId();
		}
		return list;
	}
	
	/**
	 * 根据分类id获取子分类
	 * @param categoryId
	 * @param recursive 是否递归
	 * @return
	 */
	public List<Category> getSubCategoryList(long categoryId, boolean recursive) {
		List<Category> list = new ArrayList<Category>();
		List<Category> beans =  this.getCategoriesByParent(categoryId);
		
		if (beans != null && beans.size() > 0) {
			list.addAll(beans);
			if (recursive) {
				for (Category c : beans) {
					List<Category> ss = this.getSubCategoryList(c.getId(), true);
					if (!ss.isEmpty()) {
						list.addAll(ss);	
					}
				}
			}
		}
		return list;
	}
	
	/**
	 * 获取所有的分类
	 * @return
	 */
	public List<Category> getAllCategories() {
		return genericDao.findAll("from Category order by id asc");
	}

}
