package com.dcampus.weblib.dao;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;

import com.dcampus.common.generic.GenericDao;
import com.dcampus.common.paging.PageTerm;
import com.dcampus.common.paging.SearchTerm;
import com.dcampus.common.paging.SortTerm;
import com.dcampus.common.util.ArrayCast;
import com.dcampus.weblib.entity.GroupResource;
import com.dcampus.weblib.entity.GroupResourceReceive;

@Repository
public class GroupResourceReceiveDao implements IGroupResourceReceiveDao{
	@Autowired
	private GenericDao genericDao;
	
	public void saveOrUpdateResourceReceive(GroupResourceReceive receive) {
		if(receive.getId() != null && receive.getId() > 0){
			genericDao.update(receive);
		} else {
			genericDao.save(receive);
		}
	}
	
	public void deleteResourceReceive(GroupResourceReceive receive) {
		GroupResourceReceive r = genericDao.get(GroupResourceReceive.class, receive.getId());
		genericDao.delete(r);
	}
	
	public void deleteResourceReceives(Collection<GroupResourceReceive> receives) {
		for (GroupResourceReceive receive : receives) {
			deleteResourceReceive(receive);
		}
	}

	public GroupResourceReceive getResourceReceiveById(long id) {
		return genericDao.get(GroupResourceReceive.class, id);
	}

	public List<GroupResourceReceive> getResourceReceivesByShare(long id) {
		return genericDao.findAll("from GroupResourceReceive where share.id = ?1", id);
	}
	
	public long getMyNewReceiveCount(long memberId) {
		Long result = genericDao.findFirst( "select count(*) from GroupResourceReceive as r where r.recipient.id = ?1"
				+" and ( r.status='0' or r.status is null)", memberId);
		return result == null ? 0L : result.longValue();
	}
	
	/**
	 * 根据id获取接收记录
	 * @param recipientIds
	 * @return
	 */
	public List<Object[]> getNewReceiveByRecipientIds(List<Long> recipientIds) {
		String hql = "from GroupResourceReceive as r left join r.share as s  where r.recipient.id=?1 ";
		for (int i = 1; i < recipientIds.size(); i++) {
			hql += "or r.recipient.id=?2";
		}
		hql += " order by s.createDate desc ";
		Query query = this.genericDao.createQuery(hql, recipientIds.toArray());
		List<Object[]> result = query.getResultList();
		return result;
	}

	@Override
	public int getMatchCount(SearchTerm searchTerm) throws Exception {
		// TODO Auto-generated method stub
		return (int) genericDao.getMatchCount(GroupResourceReceive.class.getSimpleName(), searchTerm);
	}

	@Override
	public GroupResourceReceive[] getMatchContent(SearchTerm searchTerm,
			SortTerm sortTerm, PageTerm pageTerm) throws Exception {
		// TODO Auto-generated method stub
		Object[] o = genericDao.getMatchContent(GroupResourceReceive.class.getSimpleName(), searchTerm, sortTerm,
				pageTerm);
		return ArrayCast.cast(o, new GroupResourceReceive[o.length]);// TODO Auto-generated method stub
	}

}
