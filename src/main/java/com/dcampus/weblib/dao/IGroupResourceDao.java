package com.dcampus.weblib.dao;

import java.util.Collection;
import java.util.List;

import com.dcampus.common.paging.IDataProvider;
import com.dcampus.common.paging.PageTerm;
import com.dcampus.common.paging.SearchTerm;
import com.dcampus.common.paging.SortTerm;
import com.dcampus.common.util.ArrayCast;
import com.dcampus.weblib.entity.GroupResource;

public interface IGroupResourceDao extends IDataProvider<GroupResource>{
	
	/**
	 * 根据查询条件获得总数
	 * @param searchTerm
	 * @return
	 * @throws Exception
	 */
	public int getMatchCount(SearchTerm searchTerm) throws Exception;
	/**
	 * 根据查询条件、排序获得资源数组
	 * @param searchTerm
	 * @param sortTerm
	 * @param pageTerm
	 * @return
	 * @throws Exception
	 */
	public GroupResource[] getMatchContent(SearchTerm searchTerm,
			SortTerm sortTerm, PageTerm pageTerm) throws Exception;
	

	public void saveOrUpdateResource(GroupResource resource);
	
	public void deleteResource(GroupResource resource);
	
	public void deleteResources(Collection<GroupResource> resources);
	
	public GroupResource getResourceById(long id);

	/**
	 * 获取柜子的资源总大小
	 * @param groupId
	 * @return
	 */
	public long getResourcesSizeByGroup(long groupId);


	/**
	 * 获取资源大小(新,递归统计了该文件夹下所有文件的大小)
	 * @param path 
	 * @param id
	 * @param groupId
	 * @return
	 */
	public long getResourceDirSize_new(String path, String id, long groupId);
	
	/**
	 * 获取资源大小
	 *
	 * @param parentId
	 * @return
	 */
	public long getResourceDirSize(long parentId);
	/**
	 * 获取资源
	 *
	 * @param groupId 柜子id
	 * @param name 资源名字
	 * @param parentId 父亲id
	 * @param type 资源类型文件，文件夹，链接
	 * @return
	 */
	public GroupResource getResourceByDetails(long groupId, String name, long parentId, int type);

	/**
	 * 根据父资源id获取资源列表
	 *
	 * @param parentId 父亲id
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<GroupResource> getResourcesByParent(long parentId, String status, int start, int limit);
	
	/**
	 * 根据父资源id获取资源列表
	 *
	 * @param parentId 父亲id
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<GroupResource> getResourcesByParent(long parentId, int start, int limit);
	
	/**
	 * 根据父资源id获取资源列表
	 *
	 * @param parentId 父亲id
	 * @return
	 */
	public List<GroupResource> getResourcesByParent(long parentId, String status);
	
	/**
	 * 根据父资源id获取资源总数
	 *
	 * @param parentId 父亲id
	 * @return
	 */
	public long getNumberOfResourcesByParent(long parentId, int type, String status);



	/**
	 * 获取用户的资源列表
	 *
	 * @param memberId
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<GroupResource> getResourcesByMember(long memberId, int start, int limit);
	
	/**
	 * 根据柜子id获取柜子所有资源，包括了回收站
	 * @param groupId 柜子id
	 * @param type 资源类型文件，文件夹，链接
	 * @param status 资源状态，是否删除
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<GroupResource> getResourcesByGroup(long groupId, int type, String status, int start, int limit);
	
	/**
	 * 根据柜子id获取柜子所有资源，包括了回收站
	 * @param groupId 柜子id
	 * @param status 资源状态，是否删除
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<GroupResource> getResourcesByGroup(long groupId, String status, int start, int limit);
	
	/**
	 * 根据柜子id获取柜子顶级资源，包括了回收站
	 * @param groupId 柜子id
	 * @param type 资源类型文件，文件夹，链接
	 * @param status 资源状态，是否删除
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<GroupResource> getTopResourcesByGroup(long groupId, int type, String status, int start, int limit);
	
	/**
	 * 根据柜子id获取柜子顶级资源，包括了回收站
	 * @param groupId 柜子id
	 * @param status 资源状态，是否删除
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<GroupResource> getTopResourcesByGroup(long groupId, String status, int start, int limit);
	
	
	
	public void updateViewUuid(long resourceId, String uuid);
	/**
	 * 获取所有上传未完成的资源
	 * @param memberId
	 * @return
	 */
	public List<GroupResource> getAllUnfinishedResources(long memberId);


}
