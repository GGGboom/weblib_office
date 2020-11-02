package com.dcampus.weblib.dao;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.dcampus.common.generic.GenericDao;
import com.dcampus.weblib.entity.GroupType;

/**
 * 柜子类型Dao
 * @author patrick
 *
 */

@Repository
public class GroupTypeDao {
	@Autowired
	private GenericDao genericDao;
	
	public void saveOrUpdateGroupType(GroupType groupType) {
		if(groupType.getId() != null && groupType.getId() > 0){
			genericDao.update(groupType);
		} else {
			genericDao.save(groupType);
		}
	}
	
	public void deleteGroupType(GroupType groupType) {
		GroupType gt = genericDao.get(GroupType.class, groupType.getId());
		genericDao.delete(gt);
	}
	
	public void deleteGroupTypes(Collection<GroupType> groupTypes) {
		for (GroupType gt : groupTypes) {
			deleteGroupType(gt);
		}
	}
	
	public GroupType getGroupTypeByName(String name) {
		return genericDao.findFirst("from GroupType gt where gt.name = ?1", name);
	}
	
	public GroupType getGroupTypeById(long id) {
		return genericDao.get(GroupType.class, id);
	}
	

}
