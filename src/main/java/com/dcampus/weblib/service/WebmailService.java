package com.dcampus.weblib.service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dcampus.common.generic.GenericDao;
import com.dcampus.weblib.dao.GroupResourceShareDao;
import com.dcampus.weblib.entity.GroupResource;
import com.dcampus.weblib.entity.GroupResourceShare;
import com.dcampus.weblib.entity.Member;
import com.dcampus.weblib.entity.ResourceCode;
import com.dcampus.weblib.exception.GroupsException;

/**
 * 资源提取，邮件分享相关处理
 * @author patrick
 *
 */
@Service
@Transactional(readOnly=false)
public class WebmailService {

	@Autowired
	private GenericDao genericDao;
	@Autowired
	private GroupResourceShareDao shareDao;
	
	
	/**
	 * 新建或者保存分享外链
	 * @param code
	 */
	public void saveOrUpdateResourceCode(ResourceCode code) {
		if (code.getId() != null && code.getId() >0) {
			genericDao.update(code);
		} else {
			genericDao.save(code);
		}
	}
	/**
	 * 根据id获取资源分享外链
	 * @param id
	 * @return
	 */
	public ResourceCode getResourceCodeById(long id) {
		return genericDao.get(ResourceCode.class,id);
	}
	/**
	 * 删除资源分享外链
	 * @param code
	 */
	public void deleteResourceCode(ResourceCode code) {
		ResourceCode c = genericDao.get(ResourceCode.class, code.getId());
		genericDao.delete(c);
	}
	/**
	 * 批量删除资源分享外链
	 * @param codes
	 */
	public void deleteResourceCodes(List<ResourceCode> codes) {
		for(ResourceCode c : codes) {
			this.deleteResourceCode(c);
		}
	}
	/**
	 * 根据资源id获取资源 分享外链
	 * @param resourceId
	 * @return
	 */
	public ResourceCode getResourceCodeByResourceId(long resourceId) {
		return genericDao.findFirst("from ResourceCode rc where rc.groupResource.id = ?1", resourceId);
	}
	/**
	 * 根据资源id和是否需要提取码获取资源 分享外链
	 * @param resourceId
	 * @param setCode
	 * @return
	 */
	public ResourceCode getResourceCodeByResourceIdAndSetCode(long resourceId, int setCode) {
		return genericDao.findFirst("from ResourceCode rc where rc.groupResource.id = ?1 and rc.setCode = ?2", resourceId, setCode);
	}
	/**
	 * 根据可提取次数获取资源分享外链
	 * @param invalidTimes
	 * @return
	 */
	public List<ResourceCode> getResourceCodeByInvalidTimes(int invalidTimes) {
		return genericDao.findAll("from ResourceCode rc where rc.validTimes < ?1", invalidTimes);
	}
	/**
	 * 获取过期的资源分享外链
	 * @return
	 */
	public List<ResourceCode> getExpiredResourceCode() {
		return genericDao.findAll("from ResourceCode rc where rc.expiredDate < ?1", new Timestamp(System.currentTimeMillis()));
	}
	/**
	 * 根据令牌获取分享外链
	 * @param token
	 * @return
	 */
	public ResourceCode getResourceByToken(String token) {
		return genericDao.findFirst("from ResourceCode rc where rc.token = ?1", token);
	}
	
	/**
	 * 删除过期资源分享外链
	 */
	public void deleteByExpiredDate() {
		List<ResourceCode> codes = this.getExpiredResourceCode(); 
		if (codes != null && codes.size() >0) {
			this.deleteResourceCodes(codes);
		}
	}
	
	/**
	 * 根据id删除资源分享外链
	 * @param id
	 */
	public void deleteResourceCodeById (long id) {
		ResourceCode code = this.getResourceCodeById(id);
		if (code != null) {
			this.deleteResourceCode(code);
		}
	}
	
	/**
	 * 根据有效次数删除分享外链
	 * @param invalidTimes
	 */
	public void deleteResourceCodeByInvalidTimes(int invalidTimes) {
		List<ResourceCode> codes = this.getResourceCodeByInvalidTimes(invalidTimes);
		if (codes != null && codes.size() >0) {
			this.deleteResourceCodes(codes);
		}
	}
	/**
	 * 根据有效次数删除分享外链对应的分享记录
	 * @param invalidTimes
	 */
	public void deleteShareBeanByInvalidTimes(int invalidTimes) {
		genericDao.findFirst("delete from GroupResourceShare "
				+ "g where g.resourceCode in (select t.id from ResourceCode t where t.validTimes < ?1 )", invalidTimes);
	}
	/**
	 * 删除过期的分享外链对应的分享记录
	 */
	public void deleteShareBeanByExpiredDate() {
		genericDao.findFirst("delete from GroupResourceShare "
				+ "g where g.resourceCode in (select t.id from ResourceCode t where t.expiredDate < ?1 )", new Timestamp(System
						.currentTimeMillis()));
	}
	
	/**
	 * 新建分享外链并且创建分享记录
	 * @param bean
	 */
	public void createAndShare(ResourceCode bean) {
		Member register = bean.getRegister();
		GroupResource resource = bean.getGroupResource();
		if (register == null || register.getId() <= 0) {
			throw new GroupsException("用户对应的member不存在！");
		}
		if (resource == null || resource.getId() <= 0) {
			throw new GroupsException("对应的资源不存在！");
		}
		
		this.saveOrUpdateResourceCode(bean);
		GroupResourceShare share = new GroupResourceShare();
		share.setLastModified(new Date());
		share.setCreateDate(new Date());
		share.setProvider(register);
		share.setResource(resource);
		share.setShareType(GroupResourceShare.LINK_SHARE);
		share.setRecipient("");
		share.setResourceCode(bean);
		shareDao.saveOrUpdateResourceShare(share);
	}
}
