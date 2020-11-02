package com.dcampus.sys.dao;

import com.dcampus.common.paging.IDataProvider;
import com.dcampus.common.paging.PageTerm;
import com.dcampus.common.paging.SearchTerm;
import com.dcampus.common.paging.SortTerm;
import com.dcampus.sys.entity.User;

public interface IUserDao extends IDataProvider<User>{
	/**
	 * 根据查询条件获得总数
	 * @param searchTerm
	 * @return
	 * @throws Exception
	 */
	public int getMatchCount(SearchTerm searchTerm) throws Exception;
	/**
	 * 根据查询条件、排序获得用户帐户数组
	 * @param searchTerm
	 * @param sortTerm
	 * @param pageTerm
	 * @return
	 * @throws Exception
	 */
	public User[] getMatchContent(SearchTerm searchTerm,
			SortTerm sortTerm, PageTerm pageTerm) throws Exception;
}
