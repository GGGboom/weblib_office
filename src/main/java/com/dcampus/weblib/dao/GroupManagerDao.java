package com.dcampus.weblib.dao;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.dcampus.common.generic.GenericDao;
import com.dcampus.weblib.entity.GroupManager;

@Repository
public class GroupManagerDao {
	
	@Autowired
	private GenericDao genericDao;
	
	public void saveOrUpdateGroupManager(GroupManager groupManager) {
		if(groupManager.getId() != null && groupManager.getId() > 0){
			genericDao.update(groupManager);
		} else {
			genericDao.save(groupManager);
		}
	}
	
	public void deleteGroupManager(GroupManager groupManager) {
		GroupManager gm = genericDao.get(GroupManager.class, groupManager.getId());
		genericDao.delete(gm);
	}
	
	public void deleteGroupManagerById(long id) {
		GroupManager gm = this.getGroupManagerById(id);
		if (gm != null) {
			this.deleteGroupManager(gm);
		}
	}
	
	public void deleteGroupManagerByGroup(long groupId) {
		List<GroupManager> gm = this.getGroupManagerByGroup(groupId);
		if (gm != null) {
			this.deleteGroupManagers(gm);
		}
	}
	public void deleteGroupManagerByMember(long memberId) {
		List<GroupManager> gm = this.getGroupManagerByMember(memberId);
		if (gm != null) {
			this.deleteGroupManagers(gm);
		}
	}
	
	public void deleteGroupManagers(Collection<GroupManager> groupManagers) {
		for (GroupManager gm : groupManagers) {
			deleteGroupManager(gm);
		}
	}
	
	public GroupManager getGroupManagerById(long id) {
		return genericDao.get(GroupManager.class, id);
	}
	
	public List<GroupManager> getGroupManagerByGroup(long groupId) {
		return genericDao.findAll("from GroupManager gm where gm.groupId = ?1", groupId);
	}
	
	public List<GroupManager> getGroupManagerByMember(long memberId) {
		return genericDao.findAll("from GroupManager gm where gm.memberId = ?1", memberId);
	}

}
