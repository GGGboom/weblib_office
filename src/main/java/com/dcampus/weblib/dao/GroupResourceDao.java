package com.dcampus.weblib.dao;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.dcampus.common.generic.GenericDao;
import com.dcampus.common.paging.PageTerm;
import com.dcampus.common.paging.SearchTerm;
import com.dcampus.common.paging.SortTerm;
import com.dcampus.common.util.ArrayCast;
import com.dcampus.sys.entity.User;
import com.dcampus.weblib.entity.GroupResource;

/**
 * 涉及到根目录的要增加额外的判断
 * 先判断parentId是否为0，为0表示是根目录
 * parent对象为空(parent is Null)
 * @author patrick
 *
 */
@Repository
public class GroupResourceDao implements IGroupResourceDao{
	
	@Autowired
	private GenericDao genericDao;
	
	public void clear(){
		genericDao.clear();
	}

	public void saveOrUpdateResource(GroupResource resource) {
		if(resource.getId() != null && resource.getId() >0) {
			genericDao.update(resource);
		} else {
			genericDao.save(resource);
		}
	}
	
	public void deleteResource(GroupResource resource) {
		GroupResource r = genericDao.get(GroupResource.class, resource.getId());
		genericDao.delete(r);
	}
	
	public void deleteResources(Collection<GroupResource> resources) {
		for(GroupResource r : resources) {
			this.deleteResource(r);
		}
	}
	
	public GroupResource getResourceById(long id) {
		return genericDao.get(GroupResource.class, id);
	}

	/**
	 * 获取柜子的资源总大小
	 * @param groupId
	 * @return
	 */
	public long getResourcesSizeByGroup(long groupId) {
		Long result = genericDao.findFirst("select sum(size)from GroupResource where agroup.id = ?1 "
				+ "and resourceStatus =?2 and resourceType = ?3", groupId,GroupResource.RESOURCE_STATUS_NORMAL, GroupResource.RESOURCE_TYPE_FILE);
		return result == null ? 0L : result.longValue();
	}


	/**
	 * 获取资源大小(新,递归统计了该文件夹下所有文件的大小)
	 * @param path 
	 * @param id
	 * @param groupId
	 * @return
	 */
	public long getResourceDirSize_new(String path, String id, long groupId) {
		Long result = genericDao.findFirst("select sum(size)from GroupResource where path like '" + path + id + "/%' and agroup.id = ?1"
				+ "and resourceStatus =?2 and resourceType = ?3", groupId, GroupResource.RESOURCE_STATUS_NORMAL, GroupResource.RESOURCE_TYPE_FILE);
		return result == null ? 0L : result.longValue();
	}
	
	/**
	 * 获取资源大小
	 *
	 * @param parentId
	 * @return
	 */
	public long getResourceDirSize(long parentId) {
		Long result = genericDao.findFirst("select sum(size)from GroupResource where parentId = ?1 "
				+ "and resourceStatus =?2 and resourceType = ?3", parentId,GroupResource.RESOURCE_STATUS_NORMAL, GroupResource.RESOURCE_TYPE_FILE);
		return result == null ? 0L : result.longValue();
	}
	/**
	 * 获取资源
	 *
	 * @param groupId 柜子id
	 * @param name 资源名字
	 * @param parentId 父亲id
	 * @param type 资源类型文件，文件夹，链接
	 * @return
	 */
	public GroupResource getResourceByDetails(long groupId, String name, long parentId, int type) {
		return genericDao.findFirst("from GroupResource where agroup.id = ?1 and name = ?2 and parentId = ?3"
				+ "and resourceType =?4", groupId, name, parentId, type);
	}

	/**
	 * 根据父资源id获取资源列表
	 *
	 * @param parentId 父亲id
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<GroupResource> getResourcesByParent(long parentId, String status, int start, int limit) {
		return genericDao.findAll(start, limit, "from GroupResource where parentId = ?1 "
				+ "and resourceStatus = ?2 order by id asc", parentId, status);
	}
	
	/**
	 * 根据父资源id获取资源列表
	 *
	 * @param parentId 父亲id
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<GroupResource> getResourcesByParent(long parentId, int start, int limit) {
		return genericDao.findAll(start, limit, "from GroupResource where parentId = ?1 "
				+ "order by id asc", parentId);
	}
	
	/**
	 * 根据父资源id获取资源列表
	 *
	 * @param parentId 父亲id
	 * @return
	 */
	public List<GroupResource> getResourcesByParent(long parentId, String status) {
		return genericDao.findAll("from GroupResource where parentId = ?1 "
				+ "and resourceStatus = ?2 order by id asc", parentId, status);
	}
	
	/**
	 * 根据父资源id获取资源总数
	 *
	 * @param parentId 父亲id
	 * @return
	 */
	public long getNumberOfResourcesByParent(long parentId, int type, String status){
		Long result =  genericDao.findFirst("select count(*) from GroupResource where parentId = ?1 and resourceType = ?2"
				+ "and resourceStatus = ?3 ", parentId, type, status);
		return result == null ? 0L : result.longValue();
	}
	
	/**
	 * 根据父资源id获取资源总数
	 *
	 * @param parentId 父亲id
	 * @return
	 */
	public long getNumberOfResourcesByParent(long parentId, String status){
		Long result =  genericDao.findFirst("select count(*) from GroupResource where parentId = ?1 "
				+ "and resourceStatus = ?2 ", parentId, status);
		return result == null ? 0L : result.longValue();
	}

	/**
	 * 根据父资源id获取资源总数
	 *
	 * @param parentId 父亲id
	 * @return
	 */
	public long getNumberOfResourcesByParent(long parentId){
		Long result =  genericDao.findFirst("select count(*) from GroupResource where parentId = ?1", parentId);
		return result == null ? 0L : result.longValue();
	}

	/**
	 * 批量修改资源的拥有者
	 * 暂时未实现
	 * @param oldMemberId
	 * @param newMemberId
	 */
	@Deprecated
	public void updateOwner(long oldMemberId, long newMemberId) {
	}

	/**
	 * 获取用户的资源列表
	 *
	 * @param memberId
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<GroupResource> getResourcesByMember(long memberId, int start, int limit){
		return genericDao.findAll(start, limit, "from GroupResource where creatorId = ?1", memberId);
	}
	
	/**
	 * 根据柜子id获取柜子所有资源，包括了回收站
	 * @param groupId 柜子id
	 * @param type 资源类型文件，文件夹，链接
	 * @param status 资源状态，是否删除
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<GroupResource> getResourcesByGroup(long groupId, int type, String status, int start, int limit) {
		return genericDao.findAll(start, limit, "from GroupResource where agroup.id = ?1 and resourceType =?2 "
				+ "and resourceStatus = ?3", groupId, type, status);
	}
	
	/**
	 * 根据柜子id获取柜子所有资源，包括了回收站
	 * @param groupId 柜子id
	 * @param status 资源状态，是否删除
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<GroupResource> getResourcesByGroup(long groupId, String status, int start, int limit) {
		return genericDao.findAll(start, limit, "from GroupResource where agroup.id = ?1"
				+ "and resourceStatus = ?2", groupId, status);
	}
	
	/**
	 * 根据柜子id获取柜子顶级资源，包括了回收站
	 * @param groupId 柜子id
	 * @param type 资源类型文件，文件夹，链接
	 * @param status 资源状态，是否删除
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<GroupResource> getTopResourcesByGroup(long groupId, int type, String status, int start, int limit) {
		return genericDao.findAll(start, limit, "from GroupResource where agroup.id = ?1 and resourceType =?2 "
					+ "and resourceStatus = ?3 and parentId = ?4", groupId, type, status, 0L);
	}
	
	/**
	 * 根据柜子id获取柜子顶级资源，包括了回收站
	 * @param groupId 柜子id
	 * @param status 资源状态，是否删除
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<GroupResource> getTopResourcesByGroup(long groupId, String status, int start, int limit) {
		return genericDao.findAll(start, limit, "from GroupResource where agroup.id = ?1 "
					+ "and resourceStatus = ?2 and parentId = ?3", groupId, status, 0L);
	}
	
	
	/**
	 * 根据柜子id获取柜子顶级资源数量，包括了回收站
	 * @param groupId 柜子id
	 * @param status 资源状态，是否删除
	 * @return
	 */
	public Long getTopResourcesAmountByGroup(long groupId, String status) {
		return (Long)genericDao.findFirst("select count(*) from GroupResource where agroup.id = ?1 "
					+ "and resourceStatus = ?2 and parentId = ?3", groupId, status, 0L);
	}
	
	
	
	public void updateViewUuid(long resourceId, String uuid) {
		 genericDao.update("update GroupResource set viewUuid = ?1 where id =?2", uuid, resourceId);
	}

	/**
	 * 获取所有上传未完成的资源
	 * @param memberId
	 * @return
	 */
	public List<GroupResource> getAllUnfinishedResources(long memberId) {
		return genericDao.findAll("select gr from GroupResource gr where gr.creatorId = ?1 "
				+ "and gr.finishSign = ?2", memberId, GroupResource.UPLOAD_UNFINISH);
	}

	@Override
	public int getMatchCount(SearchTerm searchTerm) throws Exception {
		return (int) genericDao.getMatchCount(GroupResource.class.getSimpleName(), searchTerm);
	}

	@Override
	public GroupResource[] getMatchContent(SearchTerm searchTerm,
			SortTerm sortTerm, PageTerm pageTerm) throws Exception {
		// TODO Auto-generated method stub
		Object[] o = genericDao.getMatchContent(GroupResource.class.getSimpleName(), searchTerm, sortTerm,
				pageTerm);
		return ArrayCast.cast(o, new GroupResource[o.length]);
	}

}
