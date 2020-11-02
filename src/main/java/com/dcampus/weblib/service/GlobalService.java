package com.dcampus.weblib.service;

import com.dcampus.weblib.entity.AppModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dcampus.common.config.LogMessage;
import com.dcampus.common.generic.GenericDao;
import com.dcampus.common.util.SpringApplicationContextHelper;
import com.dcampus.sys.util.UserUtils;
import com.dcampus.weblib.entity.Global;
import com.dcampus.weblib.entity.Log;
import com.dcampus.weblib.exception.PermissionsException;
import com.dcampus.weblib.mail.MailSenderInfo;
import com.dcampus.weblib.service.permission.RolePermissionUtils;
import com.dcampus.weblib.service.permission.impl.Permission;

import java.util.List;

/**
 * 全局类处理service
 * @author patrick
 *
 */
@Service
@Transactional(readOnly = false)
public class GlobalService {
	
	private ApplicationContext context = SpringApplicationContextHelper.getInstance().getApplicationContext();
	@Autowired
	private GenericDao genericDao;
	@Autowired
	@Lazy
	private LogService logService;
	@Autowired
	private RolePermissionUtils roleUtils;
	
	@Autowired
	private Permission permission;

	/**
	 * 获取所有柜子的已经使用的容量
	 * @return
	 */
	public Long getAllGroupusedCapacity(){
		String hql="select COALESCE(sum(usedCapacity),0) from Group";
		Long result=genericDao.findFirst(hql);
		return result;
	}

	public void updateGlobal(Global global){
		genericDao.update(global);
	}

	/**
	 * 设置log开关
	 */
	public void settingLogSwitch(boolean login,boolean error,boolean operate,boolean upload,boolean download){
		Global globalConfig =getGlobalConfig();
		globalConfig.setWeblib_download_log(download);
		globalConfig.setWeblib_login_log(login);
		globalConfig.setWeblib_operate_log(operate);
		globalConfig.setWeblib_upload_log(upload);
		globalConfig.setWeblib_error_log(error);
		genericDao.update(globalConfig);
	}
	
	/**
	 * 获取全局配置
	 *与之前的策略一样，返回第一个
	 * @return
	 */
	public Global getGlobalConfig() {
		return genericDao.findFirst("from Global");
	}
	
	/**
	 * 关闭站点
	 * @param reason
	 */
	public void closeSite(String reason) {
		this.checkAdminPerm();
		Global bean = this.getGlobalConfig();
		bean.setGlobalSiteStatus(Global.STATUS_CLOSE);
		bean.setSiteCloseReason(reason);
		genericDao.update(bean);
		
		String desc = LogMessage.getSiteClose(bean.getSiteName(),reason);
		logService.addOperateLog(Log.ACTION_TYPE_CLOSE, desc, bean.getId(), bean.getSiteName());
	}

	/**
	 * 开启站点
	 */
	public void openSite() {
		this.checkAdminPerm();
		Global bean = this.getGlobalConfig();
		bean.setGlobalSiteStatus(Global.STATUS_NORMAL);
		genericDao.update(bean);
		
		String desc = LogMessage.getSiteOpen(bean.getSiteName());
		logService.addOperateLog(Log.ACTION_TYPE_OPEN, desc, bean.getId(), bean.getSiteName());
	}

	/**
	 * 配置smtp
	 *
	 * @param info
	 */
	public void configSmtp(MailSenderInfo info) {
		this.checkAdminPerm();
		MailSenderInfo orignalInfo = (MailSenderInfo) context.getBean("MailSenderInfo");
		orignalInfo.setHost(info.getHost());
		orignalInfo.setSender(info.getSender());
		orignalInfo.setUsername(info.getUsername());
		orignalInfo.setPassword(info.getPassword());
		orignalInfo.setAuth(info.getAuth());
		orignalInfo.setPort(info.getPort());
		orignalInfo.setSendername(info.getSendername());
		// 存储
		Global bean = this.getGlobalConfig();
		bean.setSmtpSender(info.getSender());
		bean.setSmtpPort(info.getPort());
		bean.setSmtpHost(info.getHost());
		bean.setSmtpAuth(info.getAuth());
		bean.setSmtpUsername(info.getUsername());
		bean.setSmtpPassword(info.getPassword());
		bean.setSmtpSendername(info.getSendername());
		genericDao.update(bean);
		
		String desc = LogMessage.getSmtpSet();
		logService.addOperateLog(Log.ACTION_TYPE_SET, desc, bean.getId(), bean.getSiteName());
	}

	/**
	 * 配置统计代码
	 *
	 * @param statCode
	 */
	public void configStatCode(String statCode) {
		Global global = this.getGlobalConfig();
		global.setSiteStatCode(statCode);
		genericDao.update(global);
	}

	/**
	 * 配置搜索引擎优化代码
	 *
	 * @param keywords
	 * @param description
	 */
	public void configSeo(String keywords, String description) {
		this.checkAdminAndConfigPerm();
		Global global = this.getGlobalConfig();
		global.setSiteSeoKeywords(keywords);
		global.setSiteSeoDescription(description);
		genericDao.update(global);
	}

	/**
	 * 配置站点域名
	 * @param domain 域名
	 */
	public void configDomain(String domain) {
		this.checkAdminAndConfigPerm();
		Global global = this.getGlobalConfig();
		// 若无改动则不需要更新数据库
		if (domain == null || domain.equals(global.getSiteDomain()))
			return;
		global.setSiteDomain(domain);
		genericDao.update(global);
		
		String desc = LogMessage.getDomainSet(domain);
		logService.addOperateLog(Log.ACTION_TYPE_SET, desc, global.getId(), global.getSiteName());
	}

	/**
	 * 配置站点名称
	 * @param name 站点名称
	 */
	public void configName(String name) {
		this.checkAdminAndConfigPerm();
		Global global = this.getGlobalConfig();
		// 若无改动则不需要更新数据库
		if (name == null || name.equals(global.getSiteName()))
			return;
		global.setSiteName(name);
		genericDao.update(global);
		
		String desc = LogMessage.getSiteNameSet(name);
		logService.addOperateLog(Log.ACTION_TYPE_SET, desc, global.getId(), global.getSiteName());
	}

	/**
	 * 配置全站是否审核新建柜子
	 * @param auditGroup 是否审核柜子
	 */
	public void configAuditGroup(boolean auditGroup) {
		this.checkAdminAndConfigPerm();
		Global global = this.getGlobalConfig();
		// 若无改动则不需要更新数据库
		if (global.isSiteAuditGroup() == auditGroup)
			return;
		global.setSiteAuditGroup(auditGroup);
		genericDao.update(global);
		
		String desc = LogMessage.getGroupAuditSet(auditGroup);
		logService.addOperateLog(Log.ACTION_TYPE_SET, desc, global.getId(), global.getSiteName());
	}


	/**
	 * 配置是否跨柜复制、移动
	 * @param copyToDifferentGroup
	 * @param moveToDifferentGroup
	 */
	public void configCopyAndMove(boolean copyToDifferentGroup, boolean moveToDifferentGroup) {
		this.checkAdminAndConfigPerm();
		Global global = this.getGlobalConfig();
		global.setCopyToDifferentGroup(copyToDifferentGroup);
		global.setMoveToDifferentGroup(moveToDifferentGroup);
		genericDao.update(global);
		
		String desc = LogMessage.getGroupCrossCopySet(copyToDifferentGroup,moveToDifferentGroup);
		logService.addOperateLog(Log.ACTION_TYPE_SET, desc, global.getId(), global.getSiteName());
	}
	/**
	 * 配置是否审核客户注册
	 *
	 * @param auditClientRegister
	 */
	public void configAuditClientRegister(boolean auditClientRegister) {
		this.checkAdminAndConfigPerm();
		Global global = this.getGlobalConfig();
		global.setAuditClientRegister(auditClientRegister);
		genericDao.update(global);
	}
	/**
	 * 新建配置
	 * 初始化调用，其他禁止调用
	 * @param global
	 */
	public void createGlobal(Global global) {
		genericDao.save(global);
	}

	/**
	 * 初始化调用，其他禁止调用
	 * @param global
	 */
	public void modifyGlobal(Global global) {
		// TODO Auto-generated method stub
		genericDao.update(global);
	}
	
	private void checkAdminPerm() throws PermissionsException{
		if (!permission.isAdmin(UserUtils.getCurrentMemberId()))
			throw PermissionsException.GlobalException;
	}
	
	private void checkAdminAndConfigPerm() throws PermissionsException{
		if (!permission.isAdmin(UserUtils.getCurrentMemberId()))
			throw PermissionsException.GlobalException;
		if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_sysconfig_m")){
			throw new PermissionsException("无系统配置管理权限");
		}
	}

	/**
	 * 新建appModule配置
	 * @param appModule
	 */
	public void createAppModule(AppModule appModule) {
		this.checkAdminAndConfigPerm();
		genericDao.save(appModule);

	}

	/**
	 * 修改appModule
	 * @param appModule
	 */
	public void updateAppModule(AppModule appModule) {
		this.checkAdminAndConfigPerm();
		genericDao.update(appModule);

	}

	/**
	 * 删除appModule
	 * @param appModule
	 */
	public void deleteAppModule(AppModule appModule) {
		this.checkAdminAndConfigPerm();
		AppModule appMod = getAppModuleById(appModule.getId());
		genericDao.delete(appMod);

	}

	/**
	 * 根据id获取appModule
	 * @param id
	 * @return
	 */
	public AppModule getAppModuleById(Long id) {
		return genericDao.get(AppModule.class, id);
	}

	/**
	 * 获取所有appModule配置
	 * @return
	 */
	public List<AppModule> getAllAppModule() {
		return genericDao.findAll("from AppModule ", null);
	}

}
