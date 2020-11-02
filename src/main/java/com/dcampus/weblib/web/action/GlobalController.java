package com.dcampus.weblib.web.action;

import com.dcampus.sys.entity.User;
import com.dcampus.sys.util.UserUtils;
import com.dcampus.weblib.entity.*;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.util.FileUtil;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dcampus.common.config.PropertyUtil;
import com.dcampus.common.util.HTML;
import com.dcampus.common.util.JS;
import com.dcampus.common.web.BaseController;
import com.dcampus.sys.service.UserService;
import com.dcampus.weblib.mail.MailSenderInfo;
import com.dcampus.weblib.service.CategoryService;
import com.dcampus.weblib.service.GlobalService;
import com.dcampus.weblib.service.GroupService;
import com.dcampus.weblib.service.PermissionService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

/**
 * 全局配置
 * @author patrick
 *
 */
@Controller
@RequestMapping(value = "/global")
public class GlobalController extends BaseController{

	@Autowired
	private UserService userService;
	@Autowired
	private GlobalService globalService;
	@Autowired
	private PermissionService permService;
	@Autowired
	private GroupService groupService;
	@Autowired
	private CategoryService categoryService;


	/**
	 * 取物理总容量和可用容量
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/getStorageCapacity", produces = "application/json; charset=UTF-8")
	public  String getStorageCapacity(){
		Global global=globalService.getGlobalConfig();
		Long totalCapacity=global.getTotalStorageCapacity();
		Long availableCapacity=global.getAvailableStorageCapacity();
		return "{\"totalCapacity\":"+totalCapacity+",\"availableCapacity\":"+availableCapacity+"}";
	}

	/**
	 * 更新物理可用容量
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/updateStorageCapacity", produces = "application/json; charset=UTF-8")
	public  String updateStorageCapacity(){
		Global global=globalService.getGlobalConfig();
		Long totalCapacity=global.getTotalStorageCapacity();
		Long usedCapacity =globalService.getAllGroupusedCapacity();
		Long availableCapacity=	totalCapacity-usedCapacity;
		global.setAvailableStorageCapacity(availableCapacity);
		globalService.updateGlobal(global);
		return "{\"availableCapacity\":"+availableCapacity+"}";
	}


	/**
	 * 设置log开关
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/settingLogSwitch", produces = "application/json; charset=UTF-8")
	public  String settingLogSwitch(@RequestParam(value = "login",defaultValue = "true") boolean login,
									@RequestParam(value = "error",defaultValue = "true") boolean error,
									@RequestParam(value = "operate",defaultValue = "true") boolean operate,
									@RequestParam(value = "upload",defaultValue = "false") boolean upload,
									@RequestParam(value = "download",defaultValue = "false") boolean download){

        globalService.settingLogSwitch(login, error, operate, upload, download);

		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 获取log开关信息
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/getLogStatus", produces = "application/json; charset=UTF-8")
	public  String getLogStatus(){
		Global globalBean = globalService.getGlobalConfig();
		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		buffer.append("\"login\":").append(globalBean.getWeblib_login().equalsIgnoreCase("yes")).append(",");
		buffer.append("\"operate\":").append(globalBean.getWeblib_operate().equalsIgnoreCase("yes")).append(",");
		buffer.append("\"error\":").append(globalBean.getWeblib_error().equalsIgnoreCase("yes")).append(",");
		buffer.append("\"upload\":").append(globalBean.getWeblib_upload().equalsIgnoreCase("yes")).append(",");
		buffer.append("\"download\":").append(globalBean.getWeblib_download().equalsIgnoreCase("yes"));
		buffer.append("}");
		return buffer.toString();
	}
	/**
	 * 获取全局配置
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/getGlobalConfig", produces = "application/json; charset=UTF-8")
	public String getGlobalConfig() {
		Global globalBean = globalService.getGlobalConfig();
		//个人资源库分类
		Category personCategory = categoryService.getCategoryByName(PropertyUtil.getDefaultPersonGroupCategory());
		long allocatedSpaceSize = groupService.getAllocatedSpaceSize(new long[] {personCategory.getId()});
		long userMaxLimit = PropertyUtil.getUserMaxLimit();
		//修改系统显示personalSpace
		GroupType groupType=groupService.getGroupTypeByName("个人");
		long personalGroupSpaceLimit=groupType.getTotalFileSize();
//		long personalGroupSpaceLimit = PropertyUtil.getPersonalGroupSpaceLimit();
		long totalGroupSpaceMaxLimit = PropertyUtil.getTotalGroupSpaceMaxLimit();
		
		long admins = userService.countAdmin(Admin.SUPER_ADMIN);

		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		buffer.append("\"smtp_auth\":").append(globalBean.isSmtpAuth()).append(",");
		buffer.append("\"admins\":").append(admins == 0 ? 0L : admins).append(",");
		buffer.append("\"userLimit\":").append(userMaxLimit == 0?0L:userMaxLimit).append(",");
		buffer.append("\"spaceLimit\":").append(totalGroupSpaceMaxLimit == 0?0L:totalGroupSpaceMaxLimit).append(",");
		buffer.append("\"personalSpace\":").append(personalGroupSpaceLimit == 0?0L:personalGroupSpaceLimit).append(",");
		buffer.append("\"allocated\":").append(allocatedSpaceSize == 0?0L:allocatedSpaceSize).append(",");
		buffer.append("\"site_domain\":\"").append(JS.quote(globalBean.getSiteDomain())).append("\"").append(",");
		buffer.append("\"site_name\":\"").append(JS.quote(globalBean.getSiteName())).append("\"").append(",");
		buffer.append("\"site_closereason\":\"").append(JS.quote(HTML.escape(globalBean.getSiteCloseReason()))).append("\"").append(",");
		buffer.append("\"smtp_sender\":\"").append(globalBean.getSmtpSender()).append("\"").append(",");
		buffer.append("\"smtp_sendername\":\"").append(globalBean.getSmtpSendername()).append("\"").append(",");		
		buffer.append("\"site_close\":").append(globalBean.getGlobalSiteStatus() != Global.STATUS_NORMAL).append(",");
		buffer.append("\"smtp_port\":").append(globalBean.getSmtpPort()).append(",");
		buffer.append("\"copyToDifferentGroup\":").append(globalBean.isCopyToDifferentGroup()).append(",");
		buffer.append("\"moveToDifferentGroup\":").append(globalBean.isMoveToDifferentGroup()).append(",");				
		buffer.append("\"smtp_host\":\"").append(globalBean.getSmtpHost()).append("\"").append(",");
		buffer.append("\"site_seo_keywords\":\"").append(JS.quote(HTML.escape(globalBean.getSiteSeoKeywords()))).append("\"").append(",");
		buffer.append("\"site_seo_description\":\"").append(JS.quote(HTML.escape(globalBean.getSiteSeoDescription()))).append("\"").append(",");
		buffer.append("\"site_auditgroup\":").append(globalBean.isSiteAuditGroup()).append(",");
		buffer.append("\"site_statcode\":\"").append(JS.quote(HTML.escape(globalBean.getSiteStatCode()))).append("\"").append(",");
		buffer.append("\"smtp_username\":\"").append(globalBean.getSmtpUsername()).append("\"").append(",");
		buffer.append("\"smtp_password\":\"").append(JS.quote(HTML.escape(globalBean.getSmtpPassword()))).append("\"").append(",");
		buffer.append("\"client_auditRegister\":").append(globalBean.isAuditClientRegister());
		buffer.append("}");
		return buffer.toString();
	}
	
	/**
	 * 全局基本配置
	 * @param smtp_host
	 * @param smtp_password
	 * @param smtp_sender
	 * @param smtp_username
	 * @param smtp_sendername
	 * @param smtp_auth
	 * @param smtp_port
	 * @param site_domain 站点域名
	 * @param site_name 站点名称
	 * @param copyToDifferentGroup 是否允许跨柜复制
	 * @param moveToDifferentGroup 是否允许跨柜移动
	 * @return
	 */
//	@RequiresUser
//	@ResponseBody
//	@RequestMapping(value="/manageSite", produces = "application/json; charset=UTF-8")
//	public String manageSite(String smtp_host, String smtp_password, String smtp_sender,String smtp_username, String smtp_sendername, Boolean smtp_auth, Integer smtp_port,
//			String site_domain, String site_name , Boolean site_audit_group, Boolean copyToDifferentGroup, Boolean moveToDifferentGroup,  Boolean auditClientRegister) {
//
////		//
////		// 是否关闭站点
////		if (site_close) {
////			globalService.closeSite(site_closereason);
////		} else {
////			globalService.openSite();
////		}
//
//		// 配置smtp
//		MailSenderInfo info = new MailSenderInfo();
//		info.setHost(smtp_host);
//		info.setPassword(smtp_password);
//		info.setSender(smtp_sender);
//		info.setUsername(smtp_username);
//		info.setAuth(smtp_auth);
//		info.setPort(smtp_port);
//		info.setSendername(smtp_sendername);
//		globalService.configSmtp(info);
////		// 配置搜索引擎优化代码
////		globalService.configSeo(site_seo_keywords, site_seo_description);
////		// 配置统计代码
////		globalService.configStatCode(site_statcode);
//		// 配置站点域名
//		globalService.configDomain(site_domain);
//		// 配置站点名称
//		globalService.configName(site_name);
//		// 是否审核圈子
////		globalService.configAuditGroup(site_audit_group);
//		globalService.configCopyAndMove(copyToDifferentGroup, moveToDifferentGroup);
//		//是否审核注册
////		globalService.configAuditClientRegister(auditClientRegister);
//		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
//	}

	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/manageSite", produces = "application/json; charset=UTF-8")
	public String manageSite(String smtp_host, String smtp_password, String smtp_sender,String smtp_username, String smtp_sendername, Boolean smtp_auth, Integer smtp_port,
							 String site_domain, String site_name , Boolean site_audit_group, Boolean copyToDifferentGroup, Boolean moveToDifferentGroup,  Boolean auditClientRegister,
							 Long personalSpace) {

//		//
//		// 是否关闭站点
//		if (site_close) {
//			globalService.closeSite(site_closereason);
//		} else {
//			globalService.openSite();
//		}

		// 配置smtp
		MailSenderInfo info = new MailSenderInfo();
		info.setHost(smtp_host);
		info.setPassword(smtp_password);
		info.setSender(smtp_sender);
		info.setUsername(smtp_username);
		info.setAuth(smtp_auth);
		info.setPort(smtp_port);
		info.setSendername(smtp_sendername);
		globalService.configSmtp(info);
//		// 配置搜索引擎优化代码
//		globalService.configSeo(site_seo_keywords, site_seo_description);
//		// 配置统计代码
//		globalService.configStatCode(site_statcode);
		// 配置站点域名
		globalService.configDomain(site_domain);
		// 配置站点名称
		globalService.configName(site_name);
		// 是否审核圈子
//		globalService.configAuditGroup(site_audit_group);
		globalService.configCopyAndMove(copyToDifferentGroup, moveToDifferentGroup);

		//配置personalSpace
		GroupType groupType=groupService.getGroupTypeByName("个人");
		groupType.setTotalFileSize(personalSpace);
		groupService.saveOrUpdateGroupType(groupType);
		//是否审核注册
//		globalService.configAuditClientRegister(auditClientRegister);
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}


	/**
	 * 获取系统版本号
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/getSystemVersion", produces = "application/json; charset=UTF-8")
	public String getSystemVersion(){
		String systemVersion = PropertyUtil.getSystemVersion();
		Integer thumbnailMaxWidth = PropertyUtil.getThumbnailMaxWith();
		Integer thumbnailMaxHeight = PropertyUtil.getThumbnailMaxHeight();
		Long thumbnailMaxLength = PropertyUtil.getThumbnailMaxLength();
		Integer thumbnailQuality = PropertyUtil.getThumbnailQuality();
		Double thumbnailProportion = PropertyUtil.getThumbnailProportion();
		Integer thumbnailDefaultWidth = PropertyUtil.getThumbnailDefaultWidth();
		Integer thumbnailDefaultHeight = PropertyUtil.getThumbnailDefaultHeight();
		Integer thumbnailMinWidth = PropertyUtil.getThumbnailMinWidth();
		Integer thumbnailMinHeight = PropertyUtil.getThumbnailMinHeight();
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		buffer.append("\"systemVersion\":\"")
		.append(systemVersion == null ? "" : systemVersion)
		.append("\"");
		buffer.append(",\"ssl\":false");
		buffer.append(",\"defaultConfig\":{");
		buffer.append("\"thumbnail\":{");
		buffer.append("\"thumbnailMaxWidth\":").append(thumbnailMaxWidth==null?"":thumbnailMaxWidth).append(",");
		buffer.append("\"thumbnailMaxHeight\":").append(thumbnailMaxHeight==null?"":thumbnailMaxHeight).append(",");
		buffer.append("\"thumbnailMaxLength\":").append(thumbnailMaxLength==null?"":thumbnailMaxLength).append(",");
		buffer.append("\"thumbnailQuality\":").append(thumbnailQuality==null?"":thumbnailQuality).append(",");
		buffer.append("\"thumbnailProportion\":").append(thumbnailProportion==null?"":thumbnailProportion).append(",");
		buffer.append("\"thumbnailDefaultWidth\":").append(thumbnailDefaultWidth==null?"":thumbnailDefaultWidth).append(",");
		buffer.append("\"thumbnailDefaultHeight\":").append(thumbnailDefaultHeight==null?"":thumbnailDefaultHeight).append(",");
		buffer.append("\"thumbnailMinWidth\":").append(thumbnailMinWidth==null?"":thumbnailMinWidth).append(",");
		buffer.append("\"thumbnailMinHeight\":").append(thumbnailMinHeight==null?"":thumbnailMinHeight);
		buffer.append("}");
		buffer.append(",\"enableRoleModule\":").append(PropertyUtil.getEnableRoleModule());
		buffer.append(",\"enableDomainModule\":").append(PropertyUtil.getEnableDomainModule());
		buffer.append(",\"casLoginUrl\":\"").append(PropertyUtil.getCasLoginUrl()).append("\"");
		buffer.append(",\"moduleUrl\":{");
		buffer.append("\"lms\":").append("\""+PropertyUtil.getLmsUrl()+"\"");
		buffer.append(",\"portal\":").append("\""+PropertyUtil.getPortalUrl()+"\"");
		buffer.append(",\"ccnl\":").append("\""+PropertyUtil.getCcnlUrl()+"\"");
		buffer.append("}");
		buffer.append(",\"serviceUrl\":{");
		buffer.append("\"ims\":").append("\""+PropertyUtil.getImsUrl()+"\"");
		buffer.append("}");
		buffer.append("}");
		buffer.append("}");
		return buffer.toString();
		
	}

	/**
	 * 获取所有appModule配置
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/getAllAppModule", produces = "application/json; charset=UTF-8")
	public String getAllAppModule() {
		List<AppModule> appModules = globalService.getAllAppModule();
		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		buffer.append("\"appModules\":[");
		if (appModules != null && appModules.size() > 0) {
			for (AppModule module : appModules) {
				buffer.append("{");
				buffer.append("\"id\":\"").append(module.getId()).append("\",");
				buffer.append("\"name\":\"").append(module.getName()).append("\",");
				buffer.append("\"description\":\"").append(module.getDesc()== null ? "" : module.getDesc()).append("\",");
				buffer.append("\"schema\":\"").append(module.getSchema() == null ? "" : module.getSchema()).append("\",");
				buffer.append("\"create_date\":\"").append(module.getCreateDate()).append("\",");
				buffer.append("\"url\":\"").append(module.getUrl()).append("\",");
				if (module.getLogo() == null) {
					buffer.append("\"logo\":\"").append("\"},");
				} else {
					buffer.append("\"logo\":\"").append(PropertyUtil.getModuleLogoFolderPath()+module.getLogo()).append("\"},");
				}
			}
			buffer.setLength(buffer.length()-1);
		}
		buffer.append("]}");
		return buffer.toString();
	}

	/**
	 * 创建appModule
	 * @param name
	 * @param url
	 * @param desc
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/createAppModule", produces = "application/json; charset=UTF-8")
	public String createAppModule(String name, String url, String desc, String schema) {
		if (name == null || url == null) {
			throw new GroupsException("模块名字或者地址为空！");
		}
		AppModule module = new AppModule();
		module.setName(name);
		module.setUrl(url);
		module.setDesc(desc);
		module.setSchema(schema);
		module.setCreateDate(new Timestamp(System.currentTimeMillis()));
		globalService.createAppModule(module);
		return "{\"appModuleId\":\""+module.getId()+"\"}";
	}

	/**
	 * 修改appModule
	 * @param id
	 * @param name
	 * @param url
	 * @param logo
	 * @param desc
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/modifyAppModule", produces = "application/json; charset=UTF-8")
	public String modifyAppModule(Long id, String name, String url, String logo, String desc, String schema) {
		if (id == null) {
			throw new GroupsException("模块id为空！");
		}
		AppModule module = globalService.getAppModuleById(id);
		if (module == null) {
			throw new GroupsException("找不到该id对应的appModule!");
		}
		if (name != null) {
			module.setName(name);
		}
		if (url != null) {
			module.setUrl(url);
		}
		if (desc != null) {
			module.setDesc(desc);
		}
		if (logo != null) {
			module.setLogo(logo);
		}
		if (schema != null) {
			module.setSchema(schema);
		}
		module.setLastModified(new Timestamp(System.currentTimeMillis()));
		globalService.updateAppModule(module);
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 根据ids删除appModule
	 * @param ids
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/deleteAppModule", produces = "application/json; charset=UTF-8")
	public String deleteAppModule(Long[] ids) {
		if (ids != null && ids.length >0) {
			for (Long id : ids) {
				AppModule module = globalService.getAppModuleById(id);
				globalService.deleteAppModule(module);
			}
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 上传appModule的logo
	 * @param appModuleId
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/uploadModuleLogo", produces = "application/json; charset=UTF-8")
	public String uploadProfilePic(Long appModuleId, HttpServletRequest request) throws Exception{
		AppModule module = globalService.getAppModuleById(appModuleId);
		if (module == null) {
			throw new GroupsException("找不到该id对应的appModule");
		}
		//上传文件
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		MultipartFile multipartFile =  null;
		multipartFile = multipartRequest.getFile("filedata");
		String filedataFileName = multipartFile.getOriginalFilename();
		File file = null;
		try {
			file = File.createTempFile("tmp", null);
			multipartFile.transferTo(file);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(file==null){
			throw new GroupsException("文件不能为空");
		}
		String filename = filedataFileName;
		String ext = filename.substring(filename.lastIndexOf(".") + 1);
		String filepath =System.currentTimeMillis() + "." + ext;
		String path = PropertyUtil.getModuleLogoRootPath();
		File folder =new File(path);
		if(!folder.exists()) {
			System.out.println("目标文件所在目录不存在，准备创建它！");
			if(!folder.mkdirs()) {
				System.out.println("创建目标文件所在目录失败！");
				throw new GroupsException("LOGO目录无法创建");
			}
		}
		File destFile = new File(folder,filepath);
		FileUtil.copyFileToServer(file, destFile.toURI().toString(), true);
		module.setLogo(filepath);
		module.setCreateDate(new Timestamp(System.currentTimeMillis()));
		globalService.updateAppModule(module);
		String headPicUrl = PropertyUtil.getModuleLogoFolderPath()+filepath;
		//删除multipartFile转换成File的临时文件
		if(file.exists()){
			file.delete();
		}
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		sb.append("\"url\":\"").append(headPicUrl).append("\"");
		sb.append("}");
		return sb.toString();
	}

}
