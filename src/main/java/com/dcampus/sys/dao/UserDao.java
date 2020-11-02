package com.dcampus.sys.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.dcampus.common.generic.GenericDao;
import com.dcampus.common.paging.IDataProvider;
import com.dcampus.common.paging.PageTerm;
import com.dcampus.common.paging.SearchTerm;
import com.dcampus.common.paging.SortTerm;
import com.dcampus.common.util.ArrayCast;
import com.dcampus.sys.entity.User;

/**
 * 为了查询框架写的
 * @author patrick
 *
 */
@Repository
public class UserDao implements IUserDao{

	@Autowired
	private GenericDao genericDao;
	@Override
	public User[] getMatchContent(SearchTerm searchTerm, SortTerm sortTerm, PageTerm pageTerm)
			throws Exception {
		Object[] o = genericDao.getMatchContent(User.class.getSimpleName(), searchTerm, sortTerm,
				pageTerm);
		return ArrayCast.cast(o, new User[o.length]);
	}

	@Override
	public int getMatchCount(SearchTerm searchTerm) throws Exception {
		return (int) genericDao.getMatchCount(User.class.getSimpleName(), searchTerm);
	}

}
