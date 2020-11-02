package com.dcampus.weblib.service;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dcampus.common.generic.GenericDao;
import com.dcampus.common.service.BaseService;
import com.dcampus.sys.util.UserUtils;
import com.dcampus.weblib.entity.Collection;
import com.dcampus.weblib.entity.Member;
import com.dcampus.weblib.exception.GroupsException;

/**
 * 收藏用户，用户组，组织，自建组等
 * @author patrick
 *
 */
@Service
@Transactional(readOnly = false)
public class CollectionService extends BaseService{
	@Autowired
	private GenericDao genericDao;
	
	/**
	 * 当前用户添加收藏
	 * @param mid
	 * @throws GroupsException
	 */
	public void addCollection(long mid)throws GroupsException {
		long collectorId = UserUtils.getCurrentMemberId();
		Member collector = genericDao.get(Member.class, collectorId);
		if (collector == null) {
			throw new GroupsException("收藏者不存在");
		}
		Timestamp now = new Timestamp(System.currentTimeMillis());
		Member obj = genericDao.get(Member.class, mid);
		if (obj == null) {
			throw new GroupsException("收藏对象不存在");
		}
		String type = obj.getMemberType();
		if (type.equals(Member.MEMBER_TYPE_FOLDER)) {
			throw new GroupsException("无法收藏组织");
		}
		String hql = "from " + Collection.class.getName() + " as c where c.collector.id=" + collectorId
				+ " and c.collection.id=" + mid;
		Query q = genericDao.createQuery(hql, null);
		List<Collection> cs = q.getResultList();
		if (cs != null && cs.size() > 0) {
			for (Collection c : cs) {
				c.setLastModified(now);
			}
		} else {
			Collection collection = new Collection();
			collection.setCollectDate(now);
			collection.setCollector(collector);
			collection.setCollection(obj);
			collection.setCollectionType(type);
			collection.setLastModified(now);
			genericDao.save(collection);
		}
	}
	/**
	 * 当前用户批量添加收藏
	 * @param memberIds
	 * @throws GroupsException
	 */
	public void addCollection(long[] memberIds)throws GroupsException {

		long collectorId = UserUtils.getCurrentMemberId();
		Member collector = genericDao.get(Member.class, collectorId);
		if (collector == null) {
			throw new GroupsException("收藏者不存在");
		}
		Timestamp now = new Timestamp(System.currentTimeMillis());
		for (long mid : memberIds) {
			Member obj = genericDao.get(Member.class, mid);
			if (obj == null) {
				throw new GroupsException("收藏对象不存在");
			}
			String type = obj.getMemberType();
			if (type.equals(Member.MEMBER_TYPE_FOLDER)) {
				throw new GroupsException("无法收藏组织");
			}
			String hql = "from " + Collection.class.getName() + " as c where c.collector.id=" + collectorId
					+ " and c.collection.id=" + mid;
			Query q = genericDao.createQuery(hql, null);
			List<Collection> cs = q.getResultList();
			if (cs != null && cs.size() > 0) {
				for (Collection c : cs) {
					c.setLastModified(now);
				}
			} else {
				Collection collection = new Collection();
				collection.setCollectDate(now);
				collection.setCollector(collector);
				collection.setCollection(obj);
				collection.setCollectionType(type);
				collection.setLastModified(now);
				genericDao.save(collection);
			}
		}
	}
	
	/**
	 * 当前用户移除收藏
	 * @param memberIds
	 * @throws GroupsException
	 */
	public void removeCollection(long[] memberIds)throws GroupsException {
		if (memberIds == null || memberIds.length == 0) {
			throw new GroupsException("memberIds参数错误");
		}
		long collectorId = UserUtils.getCurrentMemberId();
		Member collector = genericDao.get(Member.class, collectorId);
		if (collector == null) {
			throw new GroupsException("收藏者不存在");
		}
		StringBuffer hql = new StringBuffer("delete from " + Collection.class.getName()
				+ " as c where c.collector.id=" + collectorId + " and (1=0 ");
		for (long mid : memberIds) {
			hql.append(" or c.collection.id=" + mid);
		}
		hql.append(" )");
		Query q = genericDao.createQuery(hql.toString(), null);
		q.executeUpdate();
	}
	
	
	/**
	 * 查看我的收藏
	 * @param collectType
	 * @return
	 * @throws GroupsException
	 */
	public List<Member> getMyCollections(String collectType)throws GroupsException {
		long collectorId = UserUtils.getCurrentMemberId();
		StringBuffer hql = new StringBuffer(
				"select c.collection from " + Collection.class.getName() + " as c where c.collector.id=" + collectorId);
		if (collectType != null) {
			hql.append(" and c.collectionType='" + collectType.toLowerCase().trim() + "'");
		}
		Query q = genericDao.createQuery(hql.toString(), null);
		return q.getResultList();
	}
}
