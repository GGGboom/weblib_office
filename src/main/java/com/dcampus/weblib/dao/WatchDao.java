package com.dcampus.weblib.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.dcampus.common.generic.GenericDao;
import com.dcampus.weblib.entity.GroupType;
import com.dcampus.weblib.entity.Watch;

@Repository
public class WatchDao {
	@Autowired
	private GenericDao genericDao;
	
	/**
	 * 创建收藏记录
	 * @param watch
	 */
	public void saveOrUpdateWatch(Watch watch) {
		if (watch.getId() != null && watch.getId() > 0)
			genericDao.update(watch);
		else
			genericDao.save(watch);
	}

	/**
	 * 删除收藏记录
	 * @param watch
	 */
	public void deleteWatch(Watch watch) {
		Watch w = genericDao.get(Watch.class, watch.getId());
		genericDao.delete(w);
	}
	
	/**
	 * 删除收藏记录
	 * @param groupId
	 * @param type
	 */
	public void deleteWatchByGroupId(long groupId, String type) {
		List<Watch> ws = this.getWatchsByTarget(groupId, type, 0 , 100);
		if (ws != null && ws.size() >0) {
			for (Watch w : ws) {
				this.deleteWatch(w);
			}
		}
	}
	
	/**
	 * 根据id获取收藏
	 * @param id
	 * @return
	 */
	public Watch getWatchById(long id) {
		return genericDao.get(Watch.class, id);
	}

	/**
	 * 获取用户的收藏
	 *
	 * @param memberId
	 *            用户id
	 * @param type
	 *            收藏类型，分为group,thread
	 * @param start
	 *            开始序号
	 * @param limit
	 *            获取条数
	 * @return 收藏列表
	 */
	public List<Watch> getWatchsByMember(long memberId, String type, int start, int limit) {
		return genericDao.findAll(start, limit, "from Watch w where w.memberId = ?1 and w.watchType = ?2 ", memberId, type);
	}

	/**
	 * 获取用户的收藏总数
	 *
	 * @param memberId 用户id
	 * @param type 收藏类型，分为group,thread
	 * @return
	 */
	public long getNumberOfWatchsByMember(long memberId, String type) {
		Long result = genericDao.findFirst("select count(w) from Watch w where w.memberId = ?1 and w.watchType = ?2",  memberId, type);
		return result == null ? 0L : result.longValue();
	}

	/**
	 * 获取某个目标的收藏情况
	 *
	 * @param targetId
	 *            被收藏目标id
	 * @param type
	 *            收藏类型，分为group,thread
	 * @param start
	 *            开始序号
	 * @param limit
	 *            获取条数
	 * @return 收藏列表
	 */
	public List<Watch> getWatchsByTarget(long targetId, String type, int start, int limit) {
		return genericDao.findAll(start, limit, "from Watch w where w.targetId = ?1 and w.watchType = ?2 ", targetId, type);
	}

	/**
	 * 获取某个目标的收藏情况总数
	 *
	 * @param targetId 被收藏目标id
	 * @param type 收藏类型，分为group,thread
	 * @return
	 */
	public long getNumberOfWatchsByTarget(long targetId, String type) {
		Long result = genericDao.findFirst("select count(w) from Watch w where w.targetId = ?1 and w.watchType = ?2", targetId, type);
		return result == null ? 0L : result.longValue();
	}

	/**
	 * 获取收藏
	 *
	 * @param memberId 用户id
	 * @param targetId 被收藏目标id
	 * @param type 收藏类型，分为group,thread
	 * @return
	 */
	public Watch getWatch(long memberId, long targetId, String type) {
		return genericDao.findFirst("from Watch w where w.memberId = ?1 and w.targetId = ?2 and w.watchType = ?3 ", memberId, targetId, type);
	}

}
