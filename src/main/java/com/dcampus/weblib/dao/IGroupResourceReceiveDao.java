package com.dcampus.weblib.dao;

import java.util.Collection;
import java.util.List;

import javax.persistence.Query;

import com.dcampus.common.paging.IDataProvider;
import com.dcampus.common.paging.PageTerm;
import com.dcampus.common.paging.SearchTerm;
import com.dcampus.common.paging.SortTerm;
import com.dcampus.weblib.entity.GroupResource;
import com.dcampus.weblib.entity.GroupResourceReceive;

public interface IGroupResourceReceiveDao extends IDataProvider<GroupResourceReceive>{
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
	public GroupResourceReceive[] getMatchContent(SearchTerm searchTerm,
			SortTerm sortTerm, PageTerm pageTerm) throws Exception;
	
	public void saveOrUpdateResourceReceive(GroupResourceReceive receive);
	
	public void deleteResourceReceive(GroupResourceReceive receive);
	
	public void deleteResourceReceives(Collection<GroupResourceReceive> receives) ;

	public GroupResourceReceive getResourceReceiveById(long id);

	public List<GroupResourceReceive> getResourceReceivesByShare(long id);
	public long getMyNewReceiveCount(long memberId);
	
	/**
	 * 根据id获取接收记录
	 * @param recipientIds
	 * @return
	 */
	public List<Object[]> getNewReceiveByRecipientIds(List<Long> recipientIds);
}
