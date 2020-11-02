package com.dcampus.weblib.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.dcampus.sys.entity.User;
import com.dcampus.sys.service.UserService;
import com.dcampus.weblib.entity.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dcampus.common.generic.GenericDao;
import com.dcampus.common.paging.IDataProvider;
import com.dcampus.common.paging.PageTerm;
import com.dcampus.common.paging.SearchTerm;
import com.dcampus.common.paging.SortTerm;
import com.dcampus.common.service.BaseService;
import com.dcampus.common.util.ArrayCast;
import com.dcampus.sys.security.Principal;
import com.dcampus.weblib.util.CurrentUserWrap;

/**
 * 日志处理service
 * @author patrick
 *
 */
@Service
@Transactional(readOnly=false)
public class LogService extends BaseService {
	
	@Autowired
	private GenericDao genericDao;

	@Autowired
	@Lazy
	private GlobalService globalService;

	@Autowired
	@Lazy
	private UserService userService;

	/**
	 * 新增下载日志
	 * @param groupId 柜子id
	 * @param groupName 柜子名
	 * @param targetObjectId 被操作对象id
	 * @param targetObject 被操作对象
	 */
	public void addDownloadLog(long groupId, String groupName,long targetObjectId,String targetObject){
		if(globalService.getGlobalConfig().getWeblib_download().equalsIgnoreCase("yes")){
			DownloadLog log = new DownloadLog();
			log.setCreateDate(new Date());
			log.setGroupId(groupId);
			log.setGroupName(groupName);
			log.setTargetObject(targetObject);
			log.setTargetObjectId(targetObjectId);

			Subject user = SecurityUtils.getSubject();
			Session session  = user.getSession();
			CurrentUserWrap currentUser = (CurrentUserWrap)session.getAttribute("currentUserWrap");
			log.setAccount(currentUser.getAccount());
			log.setIp(currentUser.getMemberIp());
			log.setMemberId(currentUser.getMemberId());
			log.setMemberName(currentUser.getMemberName());
			System.out.println(currentUser.getMemberName());
			User user1=userService.getUserByAccount(currentUser.getMemberName());
			log.setUser(user1);
			System.out.println(user1.getName());
			log.setTerminal(currentUser.getTerminal());
			genericDao.save(log);
		}

	}
	
	/**
	 * 新增错误日志
	 * @param detailType 错误类型(error/warn)
	 * @param description 描述
	 */
	public void addErrorLog(String detailType,String description){
		if(globalService.getGlobalConfig().getWeblib_error().equalsIgnoreCase("yes")){
			ErrorLog log = new ErrorLog();
			log.setCreateDate(new Date());
			log.setDescription(description);
			log.setDetailType(detailType);

			Subject user = SecurityUtils.getSubject();
			Session session  = user.getSession();
			CurrentUserWrap currentUser = (CurrentUserWrap)session.getAttribute("currentUserWrap");
			log.setAccount(currentUser == null ? "" : currentUser.getAccount());
			log.setIp(currentUser == null ? "" : currentUser.getMemberIp());
			log.setMemberId(currentUser == null ? Long.MIN_VALUE : currentUser.getMemberId());
			log.setMemberName(currentUser == null ? "" : currentUser.getMemberName());
			if(currentUser != null){
				User user1=userService.getUserByAccount(currentUser.getMemberName());
				log.setUser(user1);
			}
			log.setTerminal(currentUser == null ? "" : currentUser.getTerminal());
			genericDao.save(log);
		}

	}
	
	/**
	 * 新增登陆日志
	 * @param account 用户名
	 * @param result 登录结果
	 * @param reason 失败原因
	 */
//	public void addLoginLog(String account,String result,String reason){
//		if (globalService.getGlobalConfig().getWeblib_login().equalsIgnoreCase("yes")){
//			LoginLog log = new LoginLog();
//			log.setAccount(account);
//			log.setCreateDate(new Date());
//			log.setReason(reason);
//			log.setResult(result);
//
//			Subject user = SecurityUtils.getSubject();
//			Session session  = user.getSession();
//			CurrentUserWrap currentUser = (CurrentUserWrap)session.getAttribute("currentUserWrap");
//			log.setIp(currentUser.getMemberIp());
//			log.setMemberId(currentUser.getMemberId());
//			log.setMemberName(currentUser.getMemberName());
//			log.setTerminal(currentUser.getTerminal());
//			genericDao.save(log);
//		}
//
//	}

	//modify by mi  新的方法
	public void addLoginLog(String account,String result,String reason){
		if (globalService.getGlobalConfig().getWeblib_login().equalsIgnoreCase("yes")){
			LoginLog log = new LoginLog();
			log.setAccount(account);
			log.setCreateDate(new Date());
			log.setReason(reason);
			log.setResult(result);

			Subject user = SecurityUtils.getSubject();
			Session session  = user.getSession();
			CurrentUserWrap currentUser = (CurrentUserWrap)session.getAttribute("currentUserWrap");
			log.setIp(currentUser.getMemberIp());
			log.setMemberId(currentUser.getMemberId());
			log.setMemberName(currentUser.getMemberName());
			User user1=userService.getUserByAccount(currentUser.getMemberName());
			log.setUser(user1);
			log.setTerminal(currentUser.getTerminal());
			genericDao.save(log);
		}

	}
	/**
	 * 
	 * @param action Log类中定义的操作类型静态常量
	 * @param description 描述
	 * @param targetObjectId 被操作对象Id
	 * @param targetObject 被操作对象
	 */
	public void addOperateLog(String action,String description,long targetObjectId,String targetObject) {
		if(globalService.getGlobalConfig().getWeblib_operate().equalsIgnoreCase("yes")){
			OperateLog log = new OperateLog();
			log.setAction(action);
			log.setCreateDate(new Date());
			log.setDescription(description);
			log.setTargetObject(targetObject);
			log.setTargetObjectId(targetObjectId);

			Subject user = SecurityUtils.getSubject();
			Session session  = user.getSession();
			CurrentUserWrap currentUser = (CurrentUserWrap)session.getAttribute("currentUserWrap");
			log.setAccount(currentUser.getAccount());
			log.setIp(currentUser.getMemberIp());
			log.setMemberId(currentUser.getMemberId());
			log.setMemberName(currentUser.getMemberName());

			User user1=userService.getUserByAccount(currentUser.getMemberName());
			log.setUser(user1);

			log.setTerminal(currentUser.getTerminal());
			genericDao.save(log);
		}

	}

	public void addUploadLog(long groupId,String groupName,long targetObjectId,String targetObject) {
		if(globalService.getGlobalConfig().getWeblib_upload().equalsIgnoreCase("yes")){
			UploadLog log = new UploadLog();
			log.setCreateDate(new Date());
			log.setGroupId(groupId);
			log.setGroupName(groupName);
			log.setTargetObject(targetObject);
			log.setTargetObjectId(targetObjectId);

			Subject user = SecurityUtils.getSubject();
			Session session  = user.getSession();
			CurrentUserWrap currentUser = (CurrentUserWrap)session.getAttribute("currentUserWrap");
			log.setAccount(currentUser.getAccount());
			log.setIp(currentUser.getMemberIp());
			log.setMemberId(currentUser.getMemberId());
			log.setMemberName(currentUser.getMemberName());
			System.out.println(currentUser.getMemberName());
			User user1=userService.getUserByAccount(currentUser.getMemberName());
			log.setUser(user1);
			System.out.println(user1.getName());
			log.setTerminal(currentUser.getTerminal());
			genericDao.save(log);
		}

	}
	
	/**
	 * 获取最近下载日志
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<DownloadLog> getLatestDownloadLogs(int start, int limit) {
		return genericDao.findAll(start, limit, "from DownloadLog order by createDate desc");
	}

	/**
	 * 获取最近出错日志
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<ErrorLog> getLatestErrorLogs(int start, int limit) {
		return genericDao.findAll(start, limit, "from ErrorLog order by createDate desc");
	}
	/**
	 * 获取最近登陆日志
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<LoginLog> getLatestLoginLogs(int start, int limit) {
		return genericDao.findAll(start, limit, "from LoginLog order by createDate desc");
	}

	/**
	 * 获取资金操作日志
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<OperateLog> getLatestOperateLogs(int start, int limit){
		return genericDao.findAll(start, limit, "from OperateLog order by createDate desc");
	}

	/**
	 * 获取最近上传日志
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<UploadLog> getLatestUploadLogByAccount(int start, int limit, String account) {
		return genericDao.findAll(start, limit, "from UploadLog where account = ?1 order by createDate desc", account);
	}

	/**
	 * 获取最近下载日志
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<DownloadLog> getLatestDownloadLogByAccount(int start, int limit, String account) {
		return genericDao.findAll(start, limit, "from DownloadLog where account = ?1 order by createDate desc", account);
	}
	/**
	 * 获取最近上传日志
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<UploadLog> getLatestUploadLogs(int start, int limit) {
		return genericDao.findAll(start, limit, "from UploadLog order by createDate desc");
	}
	
	/**
	 * 下载日志总数
	 * @return
	 */
	public long getDownloadLogTotalCount() {
		return genericDao.findFirst("select count(dl) from DownloadLog dl");
	}
	/**
	 * 出错日志总数
	 * @return
	 */
	public long getErrorLogTotalCount() {
		return genericDao.findFirst("select count(el) from ErrorLog el");
	}

	/**
	 * 登录日志总数
	 * @return
	 */
	public long getLoginLogTotalCount() {
		return genericDao.findFirst("select count(ll) from LoginLog ll");
	}

	/**
	 * 操作日志总数
	 * @return
	 */
	public long getOperateLogTotalCount() {
		return genericDao.findFirst("select count(ol) from OperateLog ol");
	}

	/**
	 * 上传日志总数
	 * @return
	 */
	public long getUploadLogTotalCount() {
		return genericDao.findFirst("select count(ul) from UploadLog ul");
	}

	/**
	 * 删除下载日志
	 * @param id
	 */
	public void deleteDownloadLog(long id) {
		DownloadLog l = genericDao.get(DownloadLog.class, id);
		genericDao.delete(l);
	}

	/**
	 * 删除出错日志
	 * @param id
	 */
	public void deleteErrorLog(long id) {
		ErrorLog l = genericDao.get(ErrorLog.class, id);
		genericDao.delete(l);
	}

	/**
	 * 删除登陆日志
	 * @param id
	 */
	public void deleteLoginLog(long id) {
		LoginLog l = genericDao.get(LoginLog.class, id);
		genericDao.delete(l);
	}

	/**
	 * 删除操作日志
	 * @param id
	 */
	public void deleteOperateLog(long id) {
		OperateLog l = genericDao.get(OperateLog.class, id);
		genericDao.delete(l);
	}
  
	/**
	 * 删除上传日志
	 * @param id
	 */
	public void deleteUploadLog(long id) {
		UploadLog l = genericDao.get(UploadLog.class, id);
		genericDao.delete(l);
	}
	
	
	public DownloadLog[] searchDownloadLogs(SearchTerm searchTerm,SortTerm sortTerm,PageTerm pageTerm) {
		return this.getMatchContentDownloadLog(searchTerm, sortTerm, pageTerm);
	}
	public ErrorLog[] searchErrorLogs(SearchTerm searchTerm,SortTerm sortTerm,PageTerm pageTerm){
		return this.getMatchContentErrorLog(searchTerm, sortTerm, pageTerm);
	}
	public LoginLog[] searchLoginLogs(SearchTerm searchTerm,SortTerm sortTerm,PageTerm pageTerm){
		return this.getMatchContentLoginLog(searchTerm, sortTerm, pageTerm);
	}
	public OperateLog[] searchOperateLogs(SearchTerm searchTerm,SortTerm sortTerm,PageTerm pageTerm){
		return this.getMatchContentOperateLog(searchTerm, sortTerm, pageTerm);
	}
	public UploadLog[] searchUploadLogs(SearchTerm searchTerm,SortTerm sortTerm,PageTerm pageTerm) {
		return this.getMatchContentUploadLog(searchTerm, sortTerm, pageTerm);
	}
	
	public long searchDownloadLogsCount(SearchTerm searchTerm) {
		return genericDao.getMatchCount(DownloadLog.class.getSimpleName(), searchTerm);
	}
	public long searchErrorLogsCount(SearchTerm searchTerm){
		return genericDao.getMatchCount(ErrorLog.class.getSimpleName(), searchTerm);
	}
	public long searchLoginLogsCount(SearchTerm searchTerm){
		return genericDao.getMatchCount(LoginLog.class.getSimpleName(), searchTerm);
	}
	public long searchOperateLogsCount(SearchTerm searchTerm){
		return genericDao.getMatchCount(OperateLog.class.getSimpleName(), searchTerm);
	}
	public long searchUploadLogsCount(SearchTerm searchTerm) {
		return genericDao.getMatchCount(UploadLog.class.getSimpleName(), searchTerm);
	}


	
	public DownloadLog[] getMatchContentDownloadLog(SearchTerm searchTerm,
			SortTerm sortTerm, PageTerm pageTerm)  {
		// TODO Auto-generated method stub
		Object[] o = genericDao.getMatchContent(DownloadLog.class.getSimpleName(), searchTerm, sortTerm,
				pageTerm);
		return ArrayCast.cast(o, new DownloadLog[o.length]);
	}
	
	public LoginLog[] getMatchContentLoginLog(SearchTerm searchTerm,
			SortTerm sortTerm, PageTerm pageTerm)  {
		// TODO Auto-generated method stub
		Object[] o = genericDao.getMatchContent(LoginLog.class.getSimpleName(), searchTerm, sortTerm,
				pageTerm);
		return ArrayCast.cast(o, new LoginLog[o.length]);
	}
	
	public ErrorLog[] getMatchContentErrorLog(SearchTerm searchTerm,
			SortTerm sortTerm, PageTerm pageTerm)  {
		// TODO Auto-generated method stub
		Object[] o = genericDao.getMatchContent(ErrorLog.class.getSimpleName(), searchTerm, sortTerm,
				pageTerm);
		return ArrayCast.cast(o, new ErrorLog[o.length]);
	}
	
	public OperateLog[] getMatchContentOperateLog(SearchTerm searchTerm,
			SortTerm sortTerm, PageTerm pageTerm)  {
		// TODO Auto-generated method stub
		Object[] o = genericDao.getMatchContent(OperateLog.class.getSimpleName(), searchTerm, sortTerm,
				pageTerm);
		return ArrayCast.cast(o, new OperateLog[o.length]);
	}
	
	public UploadLog[] getMatchContentUploadLog(SearchTerm searchTerm,
			SortTerm sortTerm, PageTerm pageTerm) {
		// TODO Auto-generated method stub
		Object[] o = genericDao.getMatchContent(UploadLog.class.getSimpleName(), searchTerm, sortTerm,
				pageTerm);
		return ArrayCast.cast(o, new UploadLog[o.length]);
	}

	public void addDownloadLog4System(long groupId, String groupName,long targetObjectId,String targetObject) {
		// TODO Auto-generated method stub
		DownloadLog log = new DownloadLog();
		log.setCreateDate(new Date());
		log.setGroupId(groupId);
		log.setGroupName(groupName);
		log.setTargetObject(targetObject);
		log.setTargetObjectId(targetObjectId);
		
		log.setAccount("system");
		log.setIp("127.0.0.1");
		log.setMemberId(Long.MIN_VALUE);
		log.setMemberName("系统管理员");  
		genericDao.save(log);
		
	}
}
