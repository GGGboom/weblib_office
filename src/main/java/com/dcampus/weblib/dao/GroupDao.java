package com.dcampus.weblib.dao;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.dcampus.common.generic.GenericDao;
import com.dcampus.weblib.entity.Group;
import com.dcampus.weblib.entity.GroupDecoration;
import com.dcampus.weblib.entity.GroupExtern;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class GroupDao {

	@Autowired
	private GenericDao genericDao;

	@Transactional
	public void saveOrUpdateGroup(Group group) {
		if(group.getId() != null && group.getId() > 0){
			genericDao.update(group);
		} else {
			genericDao.save(group);
		}
		
	}
	
	public void deleteGroup(Group group) {
		Group g = genericDao.get(Group.class, group.getId());
		genericDao.delete(g);
	}
	
	public void deleteGroups(Collection<Group> groups) {
		for (Group g : groups) {
			deleteGroup(g);
		}
	}
	
	/**
	 * 获取某个柜子
	 *
	 * @param id
	 * @return
	 */
	public Group getGroupById(long id) {
		Group group = genericDao.get(Group.class, id);
		if (group != null) {
			// 获取额外信息
			group.setExternInfo(getGroupExternInfo(group.getId()));
		}
		return group;
	}
	
	/**
	 * 根据名字获得柜子
	 * @param name
	 * @return
	 */
	public Group getGroupByName(String name) {
		Group group = genericDao.findFirst("from Group g where g.name = ?1", name);
		if (group != null) {
			// 获取额外信息
			group.setExternInfo(getGroupExternInfo(group.getId()));
		}
		return group;
	}
	
	/**
	 * 获取某一段时间内创建的柜子
	 * 需要过滤柜子
	 * @param begin
	 * @param end
	 * @return
	 */
	public List<Group> getGroupsByCreateDate(Timestamp begin, Timestamp end) {
		List<Group> groups = genericDao.findAll("from Group g where g.createDate >= ?1 and g.createDate <= ?2 "
				+ "order by g.createDate desc", begin, end);
		for (Group group : groups) {
			if (group != null) {
				// 获取额外信息
				group.setExternInfo(getGroupExternInfo(group.getId()));
			}
		}
		return groups;
	}
	
	/**
	 * 获取分类下的柜子列表
	 * @param categoryId
	 * @param status
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<Group> getGroupsByCategory(long categoryId, int status,
			int start, int limit) {
		List<Group> groups = genericDao.findAll(start, limit, "from Group g where g.category.id = ?1 "
				+ "and g.groupStatus = ?2 ", categoryId, status);
		for (Group group : groups) {
			if (group != null) {
				// 获取额外信息
				group.setExternInfo(getGroupExternInfo(group.getId()));
			}
		}
		return groups;
	}

	/**
	 * 获取分类下的柜子列表
	 * @param categoryId
	 * @return
	 */
	public List<Group> getGroupsByCategory(long categoryId) {

		List<Group> groups = genericDao.findAll( "from Group g where g.category.id = ?1 "
				+ "and g.groupStatus = 1 ", categoryId);
		for (Group group : groups) {
			if (group != null) {
				// 获取额外信息
				group.setExternInfo(getGroupExternInfo(group.getId()));
			}
		}
		return groups;
	}
	
	/**
	 * 获取分类下的柜子列表
	 * @param categoryId
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<Group> getGroupsByCategory(long categoryId, int start, int limit) {
		List<Group> groups =  genericDao.findAll(start, limit, "from Group g where g.category.id = ?1 order by g.order, g.displayName asc", categoryId);
		for (Group group : groups) {
			if (group != null) {
				// 获取额外信息
				group.setExternInfo(getGroupExternInfo(group.getId()));
			}
		}
		return groups;
	}
	
	/**
	 * 根据柜子所在分类和显示名称获得柜子
	 * @param categoryId 分类
	 * @param displayName 显示名称
	 * @return
	 */
	public Group getGroupByDisplyName(long categoryId, String displayName) {
		Group group = genericDao.findFirst("from Group where category.id = ?1 and displayName = ?2", categoryId, displayName);
		if (group != null) {
			// 获取额外信息
			group.setExternInfo(getGroupExternInfo(group.getId()));
		}
		return group;
	}

	/**
	 * 根据柜子显示名称获得柜子
	 * @param displayName 显示名称
	 * @return
	 */
	public List<Group> getGroupByDisplyName(String displayName) {
		List<Group> groups = genericDao.findAll("from Group where displayName  like  '%"+displayName+"%'");
		if (groups != null) {
			// 获取额外信息
			for(Group group:groups)
			     group.setExternInfo(getGroupExternInfo(group.getId()));
		}
		return groups;
	}
	public List<Group> getGroupByDisplyNamePart(String displayName,int start,int limit) {
		List<Group> groups = genericDao.findAll(start,limit,"from Group where displayName  like  '%"+displayName+"%'");
		if (groups != null) {
			// 获取额外信息
			for(Group group:groups)
				group.setExternInfo(getGroupExternInfo(group.getId()));
		}
		return groups;
	}

	/**
	 * 获取柜子额外信息
	 * @param groupId
	 * @return
	 */
	public Map<String, String> getGroupExternInfo(long groupId) {
		Map<String, String> externInfo = new TreeMap<String, String>();
		List<GroupExtern> objects = this.getGroupExternsByGroup(groupId);
		for (Object o : objects) {
			GroupExtern bean = (GroupExtern) o;
			externInfo.put(bean.getAttrKey(), bean.getAttrValue());
		}
		return externInfo;
	}
	
	/**
	 * 根据柜子id获取GroupExtern
	 * @return
	 */
	public List<GroupExtern> getGroupExternsByGroup(long groupId) {
		return genericDao.findAll("from GroupExtern where group.id = ?1", groupId);
	}
	
	/**
	 * 根据柜子id和key获取GroupExtern
	 * @return
	 */
	public GroupExtern getGroupExternByGroupAndKey(long groupId,String key) {
		return genericDao.findFirst("from GroupExtern where group.id = ?1 and attrKey = ?2", groupId,key);
	}
	
	public void deleteGroupExtern(GroupExtern extern) {
		GroupExtern ge = genericDao.get(GroupExtern.class, extern.getId());
		genericDao.delete(ge);
	}
	
	/**
	 * 根据柜子id删除GroupExtern
	 * @param groupId
	 */
	public void deleteGroupExternByGroup(long groupId) {
		List<GroupExtern>  list = this.getGroupExternsByGroup(groupId);
		for (GroupExtern ge : list) {
			this.deleteGroupExtern(ge);
		}		
	}
	
	/**
	 * 根据柜子id;attrKey删除GroupExtern
	 * @param groupId
	 */
	public void deleteGroupExternByGroupAndKey(long groupId, String attrKey) {
		GroupExtern ge = this.getGroupExternByGroupAndKey(groupId,attrKey);
		this.deleteGroupExtern(ge);	 
	}
	/**
	 * 新建或者更新柜子额外信息
	 * @param extern
	 */
	public void saveOrUpdateGroupExtern(GroupExtern extern) {
		if(extern.getId() != null && extern.getId() > 0){
			genericDao.update(extern);
		} else {
			genericDao.save(extern);
		}
		
	}
	
	
	
	/**
	 * 获取所有的柜子
	 * @return
	 */
	public List<Group> getAllGroups() {
		return genericDao.findAll("from Group order by id asc");
	}

	/**
	 * 根据分类id返回分类下用的柜子空间
	 * @param categoryId
	 * @return
	 */
	public long getAllocatedSpaceSize(long categoryId) {
		return genericDao.findFirst("select sum(g.totalFileSize) from Group g where g.category.id = ?1 ", categoryId);
	}
	
	/**
	 * 获取系统已经分配的容量，参数为需要排除的分类id
	 * @param categoryIds
	 * @return
	 */
	public long getAllocatedSpaceSize(long[] categoryIds) {
		StringBuffer hql = new StringBuffer();
		hql.append("select sum(totalFileSize) from Group g");
		
		if (categoryIds.length>0) {
			hql.append(" where g.category.id not in (");
			for (long id : categoryIds) {
				hql.append(id).append(",");
			}
			
			hql.deleteCharAt(hql.length() - 1);
			
			hql.append(")");
		}
		Long result= genericDao.findFirst(hql.toString(), null);
		return result == null?0L:result.longValue();
	}
	

	/**
	 * 获得某一层分类下面的最大顺序号
	 * @param categoryId
	 * @return
	 */
	public double getMaxOrderInCategory(long categoryId) {
		Double result = genericDao.findFirst("select max(g.order) from Group g where g.category.id = ?1 ", categoryId);
		return result == null ? 0.0 : result.doubleValue();
	}

	/**
	 * 获取不在分类里的柜子
	 * @param categoryIds 分类id数组
	 * @return
	 */
	public List<Group> getGroupsNotInCategory(long[] categoryIds) {
		// TODO Auto-generated method stub
		StringBuffer hql = new StringBuffer();
		hql.append(" from Group g where g.category.id not in (");
		for (long id : categoryIds) {
			hql.append(id).append(",");
		}
		hql.deleteCharAt(hql.length() - 1);
		hql.append(")");
		List <Group> groups = genericDao.findAll(hql.toString());
		for (Group group : groups) {
			if (group != null) {
				// 获取额外信息
				group.setExternInfo(getGroupExternInfo(group.getId()));
			}
		}
		return groups;
	}
	
	/**
	 * 根据路径模糊查找柜子
	 * @param path 路径
	 * @return
	 */
	public  List<Group> getGroupsByPath(String path) {
		List<Group> groups = genericDao.findAll("from Group where path like ?", "%" + path + "%");
		for (Group group : groups) {
			if (group != null) {
				// 获取额外信息
				group.setExternInfo(getGroupExternInfo(group.getId()));
			}
		}
		return groups;
	}

	/**
	 * 获取应用下的所有柜子
	 * @param appId
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<Group> getGroupsByApp(long appId, int start, int limit) {
		List<Group> groups = genericDao.findAll(start, limit, "from Group where applicationId = ?1", appId);
		for (Group group : groups) {
			if (group != null) {
				// 获取额外信息
				group.setExternInfo(getGroupExternInfo(group.getId()));
			}
		}
		return groups;
	}
	
	/**
	 * 获取应用下的所有柜子
	 * @param appId
	 * @return
	 */
	public List<Group> getGroupsByApp(long appId) {
		List<Group> groups = genericDao.findAll("from Group where applicationId = ?1", appId);
		for (Group group : groups) {
			if (group != null) {
				// 获取额外信息
				group.setExternInfo(getGroupExternInfo(group.getId()));
			}
		}
		return groups;
	}

	/**
	 * 获取某个柜子
	 *
	 * @param id
	 * @return
	 */
	public GroupDecoration getGroupDecorationById(long id) {
		GroupDecoration groupd = genericDao.get(GroupDecoration.class, id);
		return groupd;
	}

	public GroupDecoration getGroupDecorationByGroup(Long groupId) {
		// TODO Auto-generated method stub
		Group group = this.getGroupById(groupId);
		if(group!= null) {
			Set<GroupDecoration> ss = group.getGroupDecorations();
			if (ss.iterator().hasNext()) {
				return ss.iterator().next();
			}
		}
		return null;
	}
}
