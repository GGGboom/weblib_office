package com.dcampus.weblib.web.action;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dcampus.weblib.util.adaptor.XlsTokenDownloadUrlAdaptor;
import org.apache.commons.io.FileUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dcampus.common.config.ResourceProperty;
import com.dcampus.common.util.JS;
import com.dcampus.common.util.Log;
import com.dcampus.common.util.Properties;
import com.dcampus.common.web.BaseController;
import com.dcampus.weblib.entity.GroupResource;
import com.dcampus.weblib.entity.Member;
import com.dcampus.weblib.entity.ResourceCode;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.mail.MailSender;
import com.dcampus.weblib.service.GlobalService;
import com.dcampus.weblib.service.GroupService;
import com.dcampus.weblib.service.GrouperService;
import com.dcampus.weblib.service.PermissionService;
import com.dcampus.weblib.service.ResourceService;
import com.dcampus.weblib.service.WebmailService;
import com.dcampus.weblib.service.permission.IPermission.GroupPerm;
import com.dcampus.weblib.util.Base64Encode;
import com.dcampus.weblib.util.CurrentUserWrap;
import com.dcampus.weblib.util.FileDownloadUtil;
import com.dcampus.weblib.util.FileNameEncoder;
import com.dcampus.weblib.util.FileSizeConversion;
import com.dcampus.weblib.util.FileUtil;
import com.dcampus.weblib.util.Filter;
import com.dcampus.weblib.util.RandomStringGenerater;

@Controller
@RequestMapping(value="/webmail")
public class WebmailController extends BaseController {
	@Autowired
	private WebmailService webmailService;
	@Autowired
	private GlobalService globalService;
	@Autowired
	private ResourceService resourceService;
	@Autowired
	private PermissionService permService;
	@Autowired
	private GroupService groupService;
	@Autowired
	private GrouperService grouperService;
	
	private static GroupsException exception = new GroupsException("无法访问资源");
	
	private static Log log = Log.getLog(WebmailController.class); 

	private static final long OneDay = 1L * 24 * 3600 * 1000;

	/** 资源提取码可使用次数 **/
	private static int resourceCodeValidTimes = 7;

	/** 资源提取码有效天数 **/
	private static int resourceCodeValidDays = 10*365;
	/** 邮件附件模板 **/
	private static String emailAttachmentStyle;
	/** 邮件模板 */
	private static String emailTemplate = "";
	/** 附件下载地址 */
	private static String attachmentDownUrl;
	
	private static Properties properties;
	static {
		try {
			properties = new Properties(Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("groups.properties"));
			resourceCodeValidTimes = Integer.parseInt(properties
					.getProperty("resourceCodeValidTimes"));
			resourceCodeValidDays = Integer.parseInt(properties
					.getProperty("resourceCodeValidDays"));
			emailAttachmentStyle = properties.getProperty("emailAttachmentStyle");
			attachmentDownUrl = properties.getProperty("attachmentDownUrl");
			File file = new ClassPathResource("emailTemplate.properties").getFile();
			emailTemplate = FileUtils.readFileToString(file, "UTF-8");
		} catch (Exception e) {
			log.error(e, e);
		}
	}
	
	/**
	 * 通过id获取提取码URL
	 * @param id 资源id数组
	 * @param setCode 是否私密链接数组
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value={"/getTokenDownloadUrl","/getTokenDownloadUrl_v2"}, produces = "application/json; charset=UTF-8")
	public String getTokenDownloadUrl(Long[] id, Integer[] setCode) throws Exception {
		if(id == null || id.length <= 0) {
			throw new GroupsException("资源id不能为空");
		}
		if(id != null && setCode != null && id.length != setCode.length) {
			throw new GroupsException("资源id长度和是否加密长度不匹配");
		}
		//TO-DO 进行resouce对应的物理文件大小判断
		//如果resource对应的物理文件大小大于某一阈值
		//可以返回exception或者返回json数据
		//目的：文件过大会分享失败，所以要限制要分享的文件大小
		long theSizeOfSharedFile=0L;
		for(int i=0;i<id.length;i++){
			//到数据库weblib_group_resource里查询parent_id为id的数据的size的大小
			GroupResource resourceBean = resourceService.getResourceById(id[i]);
			theSizeOfSharedFile+=resourceBean.getSize();
		}
		//要分享的文件大小大于某个KB时，throw错误
		if(theSizeOfSharedFile>1024*1024*5) {
			throw new GroupsException("通过外链分享的文件大小超过5G，请采用站内分享");
		}
		///////加判断是否加密
		Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
		Long memberId = currentUserWrap.getMemberId();

		String siteDomain = globalService.getGlobalConfig().getSiteDomain();
		
		if (siteDomain != null && !hasScheme(siteDomain)) {
			siteDomain = "http://" + siteDomain;
		}
		StringBuffer server = new StringBuffer(64);
		server.append(siteDomain);
		List<Long> ids = new ArrayList<Long>();
		List<String> url = new ArrayList<String>();
		List<String> codes = new ArrayList<String>();
		
		if (id != null && id.length > 0) {
			List<ResourceCode> list = this.registResourceCode_new(id, setCode, memberId);
			for (ResourceCode bean : list) {
				/*GroupResource resource = CacheDao.getGroupResourceDao().getResource(
						bean.getResourceId());*/
				StringBuffer link = new StringBuffer(56);
				link.append(server.toString()).append(attachmentDownUrl).append("?token=").append(bean.getToken());
				url.add(link.toString());
				ids.add(bean.getGroupResource().getId());
				codes.add(bean.getCode());
			}
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		buffer.append("\"totalCount\":").append(url.size()).append(",");
		buffer.append("\"data\":[");
		for (int i = 0; i < url.size(); ++i) {
			buffer.append("{");
			buffer.append("\"id\":").append(ids.get(i)).append(",");
			buffer.append("\"code\":\"").append(codes.get(i)).append("\",");
			buffer.append("\"url\":\"").append(JS.quote(Filter.convertHtmlBody(url.get(i)))).append("\"");
			buffer.append("},");
		}
		if (url.size() > 0) {
			buffer.setLength(buffer.length() - 1);
		}
		buffer.append("]}");
		return buffer.toString();
	}

	@RequiresUser
	@ResponseBody
	@RequestMapping(value={"/exportTokenDownloadUrl","/exportTokenDownloadUrl_v2"}, produces = "application/json; charset=UTF-8")
	public String exportTokenDownloadUrl(Long[] id, Integer[] setCode, HttpServletResponse response)throws Exception{

		Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
		Long memberId = currentUserWrap.getMemberId();
		String siteDomain = globalService.getGlobalConfig().getSiteDomain();
		if (siteDomain != null && !hasScheme(siteDomain)) {
			siteDomain = "http://" + siteDomain;
		}
		StringBuffer server = new StringBuffer(64);
		server.append(siteDomain);
		List<Long> ids = new ArrayList<Long>();
		List<String> urls = new ArrayList<String>();
		List<String> codes = new ArrayList<String>();
		List<String> names = new ArrayList<String>();

		//前端请求时只用一个setCode参数，对整个导出进行统一设置
		int _setCode = setCode!=null&&setCode.length>0?setCode[0]:0;
		setCode = new Integer[id.length];
		for(int i=0;i<setCode.length;i++){
			setCode[i] = _setCode;
		}

		if (id != null && id.length > 0) {
			List<ResourceCode> list = this.registResourceCode_new(id, setCode, memberId);
			for (ResourceCode bean : list) {
				GroupResource resource = bean.getGroupResource();
				StringBuffer link = new StringBuffer(56);
				link.append(server.toString()).append(attachmentDownUrl).append("?token=").append(bean.getToken());
				urls.add(link.toString());
				ids.add(bean.getGroupResource().getId());
				codes.add(bean.getCode());
				names.add(resource.getName());
			}
		}


		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename="
				+ "Share.xls");

		OutputStream outputStream = null;
		try {
			outputStream = response.getOutputStream();
			XlsTokenDownloadUrlAdaptor adaptor = new XlsTokenDownloadUrlAdaptor();
			adaptor.exportDownloadUrl(outputStream, ids, urls, codes, names, setCode);
			if (outputStream != null)
				outputStream.close();
		} catch (Exception e) {
			if (outputStream != null)
				outputStream.close();
		}

		return null;
	}
	
	public static boolean hasScheme(String uri) {
        int pos = uri.indexOf(':');
        // don't want to be misguided by a potential ':' in the query section of the URI (is this possible / allowed?)
        // so consider only a ':' in the first 10 chars as a scheme
        return (pos > -1) && (pos < 10);
    }
	
	/**
	 * 根据id和是否加密的setCode注册提取码
	 * @param id 
	 * @param setCode
	 * @return list
	 * @throws Exception
	 */
	private List<ResourceCode> registResourceCode_new(Long[] id, Integer[] setCode, long memberId) throws Exception {
		List<ResourceCode> list = new ArrayList<ResourceCode>();
		Member register = grouperService.getMemberById(memberId);
		if (register == null || register.getId() <= 0) {
			throw new GroupsException("member不存在！");
		}
		for (int i=0; i<id.length ;i++) {
			// 权限判断，只有管理员和圈子管理员才能进行注册资源
			if (!this.canRegist(id[i],memberId))
				throw exception;
			if (setCode == null) {
				setCode = new Integer[id.length];
				for (int ii = 0; ii < setCode.length; ii++) {
					setCode[ii] = ResourceCode.NOT_NEED_SETCODE;
				}
			}			
			ResourceCode bean = null;
			// 查看是否已存在资源，若当前存在资源，则续期，若不存在则创建
			//bean = webmailService.getResourceCodeByResourceIdAndSetCode(id[i], setCode[i]);
			bean = webmailService.getResourceCodeByResourceId(id[i]);
			if (bean != null && bean.getId() > 0 ) {
				// 续期
				bean.setValidTimes(resourceCodeValidTimes);
				if(resourceCodeValidDays!=-1){
					bean.setExpiredDate(new Timestamp(System.currentTimeMillis()
							+ OneDay * resourceCodeValidDays));
				}else{
					bean.setExpiredDate(null);//-1时，不限制有效期
				}
				bean.setRegister(register);
				if (setCode[i] == ResourceCode.NEED_SETCODE) {
					//加密提取码
					bean.setCode(RandomStringGenerater.generate(4));   //生成code，生成随机串
				} else {
					bean.setCode(null);
				}
				bean.setSetCode(setCode[i]);
				webmailService.saveOrUpdateResourceCode(bean);
			} else {
				GroupResource resource = resourceService.getResourceById(id[i]);
				if (resource == null || resource.getId() <= 0) {
					throw new GroupsException("对应的资源不存在！");
				}
				bean = new ResourceCode();
				if (setCode[i] == ResourceCode.NEED_SETCODE) {
					//加密提取码
					bean.setCode(RandomStringGenerater.generate(4));   //生成code，生成随机串
				} else {
					bean.setCode(null); 
				}    //生成公开提取码
				bean.setRegister(register);
				bean.setGroupResource(resource);
				bean.setValidTimes(resourceCodeValidTimes);
				if(resourceCodeValidDays!=-1){
					bean.setExpiredDate(new Timestamp(System.currentTimeMillis()
							+ OneDay * resourceCodeValidDays));
				}else{
					bean.setExpiredDate(null);//-1时，不限制有效期
				}
				bean.setSetCode(setCode[i]);
				//bean.setToken(Long.toString(id[i]));    
				bean.setToken(this.changeToken(id[i], setCode[i]));           //通过id转换成提取码token			
				webmailService.createAndShare(bean);	
			}
			list.add(bean);
		}
		return list;	
	}
	
	/**
	 * 判断是否能注册文件外链
	 * @param resourceId
	 * @param memberId
	 * @return
	 */
	private boolean canRegist(long resourceId, long memberId) {
		GroupResource bean = resourceService.getResourceById(resourceId);
		if (bean.getGroup() == null || bean.getGroup().getId() <= 0) {
			throw  exception;
		}
		if (!permService.isAdmin(memberId)
				&& !permService.hasGroupPermission(memberId, bean.getGroup().getId(), GroupPerm.MANAGE_GROUP)
					&& !permService.hasGroupPermission(memberId, bean.getGroup().getId(), GroupPerm.DOWNLOAD_RESOURCE)
					&& !permService.isGroupManager(memberId, bean.getGroup().getId())) {
			return false;
		}
		return true;
	}
	
	/**
	 * 通过id转换成提取码token
	 * @param id
	 * @param setCode
	 * @return
	 */
	private String changeToken(long id, int setCode) {
		String random = RandomStringGenerater.generate(4); // 产生随机数
		// 通过ID转换成唯一标识的字符
		Base64Encode b = new Base64Encode();
		String tail = b.encode((Long.toString(id) + Integer.toString(setCode)).getBytes());             // ////////////////base64编码
		return random + tail;
	}
	
	/**
	 * 通过提取码获取资源信息
	 * @param token 
	 * @return 
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/getResourceInfoByToken", produces = "application/json; charset=UTF-8")
	public String getResourceInfoByToken(String token) throws Exception {
		// 获取资源记录
		ResourceCode bean = webmailService.getResourceByToken(token);
		if(bean == null )
			throw new GroupsException("下载资源已经删除");
		GroupResource res = bean.getGroupResource();
		if(res == null || res.getResourceStatus().equalsIgnoreCase(GroupResource.RESOURCE_STATUS_DELETE)) {
			throw new GroupsException("下载资源已经删除");
		}
		boolean file = res == null ? false : true;
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		sb.append("\"id\":").append(res.getId()).append(",");
		sb.append("\"filesize\":").append(res.getSize()).append(",");
		sb.append("\"beanCodeId\":").append(bean.getId()).append(",");
		sb.append("\"name\":\"").append(URLDecoder.decode(res.getName(), "UTF-8")).append("\",");
		sb.append("\"validTimes\":").append(bean.getValidTimes()).append(",");
		sb.append("\"file\":").append(file).append(",");
		sb.append("\"checkCode\":\"").append(bean.getSetCode()).append("\",");
		//sb.append("{\"validTimes\":").append((resourceCodeValidTimes.intValue() - bean.getValidTimes()));
		if(resourceCodeValidDays!=-1&&bean.getExpiredDate()!=null){
			sb.append("\"expiredDate\":\"").append(format.format(bean.getExpiredDate())).append("\"");
		}else{
		    sb.append("\"expiredDate\":\"").append("").append("\"");
		}
		sb.append("}");
		return sb.toString();
	}
	
	/**
	 * 校验token和加密
	 * @param token 
	 * @param code
	 * @return null
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/checkToken", produces = "application/json; charset=UTF-8")
	public String checkToken(String token, String code) throws Exception {
		if (token == null) {
			throw new GroupsException("提取码不能为空");
		}
		ResourceCode bean = webmailService.getResourceByToken(token);	
		// 是否已经过期
		Timestamp now = new Timestamp(System.currentTimeMillis());
		if (resourceCodeValidDays!=-1&&bean.getExpiredDate()!=null&&now.after(bean.getExpiredDate())) { 
			throw new GroupsException("提取码已经过期");
		}
			
		// 是否下载次数已满
		if (bean.getValidTimes() <= 0) {
			throw new GroupsException("提取码下载次数已满");
		}			

		// 提取码是否正确
		if (bean.getCode() != null)
			if (!bean.getCode().equals(code)) {
				throw new GroupsException("提取密码错误");
			}				
		
		GroupResource resourceBean = bean.getGroupResource();
		
		if (resourceBean == null || resourceBean.getResourceStatus() == GroupResource.RESOURCE_STATUS_DELETE) { 
			throw new GroupsException("下载资源已经删除");
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
	
	/**
	 * 新的下载action，根据提取码token下载文件，如果有加密则验证code，若无则跳过
	 * @param token 
	 * @param code
	 * @return null
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/downloadByToken", produces = "application/json; charset=UTF-8")
	public String downloadByToken(String token, String code,HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (token == null) {
			throw new GroupsException("提取码不能为空");
		}
		ResourceCode bean = webmailService.getResourceByToken(token);	
		// 是否已经过期
		Timestamp now = new Timestamp(System.currentTimeMillis());
		if (resourceCodeValidDays!=-1&&bean.getExpiredDate()!=null&&now.after(bean.getExpiredDate())) {
			throw new GroupsException("提取码已经过期");
		}
			
		// 是否下载次数已满
		if (resourceCodeValidTimes != -1 && bean.getValidTimes() <= 0) {
			throw new GroupsException("提取码下载次数已满");
		}			

		// 提取码是否正确
		if (bean.getCode() != null)
			if (!bean.getCode().equals(code)) {
				throw new GroupsException("提取密码错误");
			}				
		GroupResource resourceBean = bean.getGroupResource();
		if (resourceBean == null || resourceBean.getResourceStatus() == GroupResource.RESOURCE_STATUS_DELETE) {
			throw new GroupsException("下载资源已经删除");
		}
		String filename = null;
		if (resourceBean.getResourceType() == GroupResource.RESOURCE_TYPE_DIRECTORY) {
			filename = resourceBean.getName() + ".zip";
			filename = filename.replaceAll(" ", "");
			response.setContentType("application/octet-stream; charset=utf-8");
			String toFileName = FileNameEncoder.encode(filename, request.getHeader("User-agent"));
			response.setHeader("Content-Disposition", "attachment; " + toFileName);
			OutputStream outputStream = null;
			try {
				outputStream = response.getOutputStream();
				resourceService.directDownloadResource4Web(new Long[] { resourceBean.getId() }, outputStream);
				//可下载次数减一
				bean.setValidTimes(bean.getValidTimes()-1);
				webmailService.saveOrUpdateResourceCode(bean);
				if (outputStream != null)
					outputStream.close();
			}catch(Exception e){
				Throwable te = e.getCause();
				if(!(te instanceof SocketException))
					if(e instanceof IOException)
						throw (IOException)e;
					else
						throw new IOException(e.getLocalizedMessage());
			} finally{
				if (outputStream != null)
					outputStream.close();
			}
		} else {
			filename = resourceBean.getName();
			filename = filename.replaceAll(" ", "");
			File downFile = resourceService.getDownloadResource4Web(new long[]{resourceBean.getId()});
			if (downFile != null && downFile.exists()) {
				FileDownloadUtil.output(downFile, filename, request, response, 0);
				//可下载次数减一
				bean.setValidTimes(bean.getValidTimes()-1);
				webmailService.saveOrUpdateResourceCode(bean);
			} else {
				throw new GroupsException("无法获取下载资源");
			}
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
	
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/sendEmail", produces = "application/json; charset=UTF-8")
	public String sendEmail(Long[] id, String recipient, String sender,Integer[] setCode, String content, String title) throws Exception {
		System.out.println("ResourceDelegarot.sendEmail");
		Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
		Long memberId = currentUserWrap.getMemberId();
		if (sender == null || sender.length() == 0 ||
				recipient == null || recipient.length() == 0) {
			throw new GroupsException(ResourceProperty.getParamsErrorException());
		}
		String siteDomain = globalService.getGlobalConfig().getSiteDomain();
		if (siteDomain != null && !hasScheme(siteDomain)) {
			siteDomain = "http://" + siteDomain;
		}
		StringBuffer server = new StringBuffer(64);
		server.append(siteDomain);
		StringBuffer attachmentBuf = new StringBuffer();
		String siteName = globalService.getGlobalConfig().getSiteName();
		
		////
		StringBuffer linkBuf = new StringBuffer();
		StringBuffer resourceNameBuf = new StringBuffer();
		StringBuffer sizeBuf = new StringBuffer();
		////
		
		if (id != null && id.length > 0) {
			List<ResourceCode> list = this.registResourceCode_new(id, setCode, memberId);
			for (ResourceCode bean : list) {
				GroupResource resource = bean.getGroupResource();
				String resName = resource.getName();
				StringBuffer link = new StringBuffer(56);
				link.append(server.toString()).append(attachmentDownUrl).append("?token=").append(bean.getToken());
				String emailAttachmentStyle_withoutCode = "<p><a style=\"color:#2584b8;font:16px/150% 'Microsoft YaHei';text-decoration:underline;\" href=\""+link.toString()+"\" target=\"_blank\">\""+resource.getName()+"\"</a><span style=\"color:#949494;font:14px/150% 'Microsoft YaHei\">(\""+FileSizeConversion.conversion(resource.getSize())+"\")</span>";
				String emailAttachmentStyle_withCode = "<p><a style=\"color:#2584b8;font:16px/150% 'Microsoft YaHei';text-decoration:underline;\" href=\""+link.toString()+"\" target=\"_blank\">\""+resource.getName()+"\"</a><span style=\"color:#949494;font:14px/150% 'Microsoft YaHei\">(\""+FileSizeConversion.conversion(resource.getSize())+"\")</span><p><span style=\"font:14px/150% 'Microsoft YaHei';color:#333;font-weight:bold;\">提取码:\""+bean.getCode()+"\"</span></p>";
				String attachment;
				if (bean.getSetCode()==1) {
					attachment = emailAttachmentStyle_withCode;	
				} else {
					attachment = emailAttachmentStyle_withoutCode;
				}
				linkBuf.append(link.toString());
				resourceNameBuf.append(resource.getName());
				sizeBuf.append(FileSizeConversion.conversion(resource.getSize()));				
				attachmentBuf.append(attachment);				
			}
		}
		//String senderName = this.sender.substring(0, this.sender.indexOf("@"));
		String emailContent = emailTemplate.replaceAll("\\$\\{uri\\}", server.toString());
		emailContent = emailContent.replaceAll("\\$\\{site\\}", siteName);
		emailContent = emailContent.replaceAll("\\$\\{content\\}", content);
		emailContent = emailContent.replaceAll("\\$\\{attachment\\}", attachmentBuf.toString());
		emailContent = emailContent.replaceAll("\\$\\{sender\\}", sender);
		//////
		emailContent = emailContent.replaceAll("\\$\\{sender\\}", sender);
		emailContent = emailContent.replaceAll("\\$\\{sender\\}", sender);
		/////		
		String[] to = recipient.split(";");
		MailSender.sendMail(to , title, emailContent);
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
	
	
	
}
