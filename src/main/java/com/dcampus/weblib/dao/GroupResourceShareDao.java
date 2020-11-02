package com.dcampus.weblib.dao;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.dcampus.common.generic.GenericDao;
import com.dcampus.weblib.entity.GroupResourceShare;
import com.dcampus.weblib.entity.ShareResponse;
import com.dcampus.weblib.entity.ShareWrap;

@Repository
public class GroupResourceShareDao {
	@Autowired
	private GenericDao genericDao;

	public void saveOrUpdateResourceShare(GroupResourceShare share) {
		if(share.getId() != null && share.getId() > 0){
			genericDao.update(share);
		} else {
			genericDao.save(share);
		}
	}
	
	public void deleteResourceShare(GroupResourceShare share) {
		GroupResourceShare s = genericDao.get(GroupResourceShare.class, share.getId());
		genericDao.delete(s);
	}
	
	public void deleteResourceShares(Collection<GroupResourceShare> shares) {
		for (GroupResourceShare share : shares) {
			deleteResourceShare(share);
		}
	}

	public GroupResourceShare getResourceShareById(long id) {
		return genericDao.get(GroupResourceShare.class, id);
	}

	public List<GroupResourceShare> getSharesByResourceAndProvider(long resourceId, long providerId, int start,
			int limit) {
		return genericDao.findAll(start,  limit, "from GroupResourceShare where resource.id = ?1"
				+ "and provider.id = ?2", resourceId, providerId);
	}
	
	public long countResourceShare(long resourceId, long providerId) {
		return genericDao.findFirst("select count(*) from GroupResourceShare where resource.id = ?1"
				+ "and provider.id = ?2", resourceId, providerId);
	}

	public void saveOrUpdateShareWrap(ShareWrap shareWrap) {
		if(shareWrap.getId() != null && shareWrap.getId() > 0){
			genericDao.update(shareWrap);
		} else {
			genericDao.save(shareWrap);
		}
	}

	public List<ShareResponse> getShareResponseByShareWrapAndResponder(long shareWrapId, long responderId) {
		return genericDao.findAll("from ShareResponse as r  where r.responder.id=?1 and r.shareWrap.id=?2", responderId, shareWrapId);
	}

	public List<GroupResourceShare> getAllResourceShareByProvider(long memberId, int start, int limit) {
		return genericDao.findAll(start,  limit, "from GroupResourceShare where provider.id = ?1 order by createDate desc",  memberId);
	}
}
