package com.dcampus.weblib.web.action;

import cn.edu.scut.cas.client.UserAttributes;
import cn.edu.scut.cas.client.filter.CASFilter;
import com.dcampus.common.config.LogMessage;
import com.dcampus.common.config.PropertyUtil;
import com.dcampus.common.util.Crypt;
import com.dcampus.common.util.JS;
import com.dcampus.common.web.BaseController;
import com.dcampus.sys.entity.User;
import com.dcampus.sys.security.HtmlUsernamePasswordToken;
import com.dcampus.sys.service.UserService;
import com.dcampus.sys.util.UserUtils;
import com.dcampus.weblib.entity.*;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.exception.NotAuthorizedException;
import com.dcampus.weblib.service.*;
import com.dcampus.weblib.service.permission.PermCollection;
import com.dcampus.weblib.util.CurrentUserWrap;
import com.dcampus.weblib.util.Filter;
import com.dcampus.weblib.util.IpGetter;
import com.dcampus.weblib.util.TerminalGetter;
import com.itextpdf.text.pdf.codec.Base64;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
/**
 *登录处理的controller 
 *只有本地认证，
 *LDAP认证和AD认证没有测试
 *也没有验证码登录
 * @author patrick
 *
 */
@Controller
@RequestMapping(value="/login")
public class LoginCtroller extends BaseController {
	private static final Logger logger = Logger.getLogger(LoginCtroller.class);
	@Autowired
	private GrouperService grouperService;
	@Autowired
	private UserService userService;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private GroupService groupService;
	@Autowired 
	private PermissionService permService;
	@Autowired 
	private ResourceService resourceService;
	@Autowired
	private LogService logService;
	
	/**
	 * 简单地本地认证，之后应该加入其他认证方式的支持
	 * @param account 用户名
	 * @param password 密码
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value={"/authenticate","/authenticate_v2"}, produces = "application/json; charset=UTF-8")
	public String authenticate(String account, String password, HttpServletRequest request, HttpServletResponse response) {
		String terminal = TerminalGetter.getTerminal(request);
		String ip = IpGetter.getRemoteAddr(request);
		Subject currentUser = SecurityUtils.getSubject();
		
		if (currentUser.isAuthenticated()) {
			currentUser.logout();
		}
		Session session  = currentUser.getSession();
		CurrentUserWrap currentUserWrap = new CurrentUserWrap();
		currentUserWrap.setMemberIp(ip);
		currentUserWrap.setTerminal(terminal);
		session.setAttribute("currentUserWrap",currentUserWrap);
		
		
		String casAccount = null;
		String name = null;
		//数园cas
		if (casAccount == null) {
			casAccount = (String) request.getSession().getAttribute(CASFilter.CAS_SESSION_USER);
			name = casAccount;
			UserAttributes userAttribute = (UserAttributes) request.getSession().getAttribute(CASFilter.CAS_SESSION_USER_ATTRIBUTES);
			if (userAttribute != null && userAttribute.getAttributes("cas:cn") != null) {
				name = userAttribute.getAttributes("cas:cn")[0];
				if (name == null) {
					name = casAccount;
				}
			}
		}
		// scut中央认证
		if (casAccount == null) {
			casAccount = (String) request.getSession().getAttribute("scut:ids:uid");
			name = casAccount;
		}
		//cerid
		if(casAccount == null){
			String user_in_session = (String) request.getSession().getServletContext().getAttribute("login_user_in_session");
			JSONObject jsonObject = JSONObject.fromObject(user_in_session);
			try {
				casAccount = jsonObject.getString("cerid");
				name = jsonObject.getString("userName");
			} catch (Exception e) {
				casAccount=null;
			}
		}
		
		String authType = null;
		if (casAccount == null) {
			// 本地登录
			authType = "local";
			System.out.println("localAccount=" + account + "本地认证即将开始");
			account = this.shiroLogin(account, password);
			System.out.println("localAccount=" + account + "本地认证成功");

			// 本地登录后，把登录信息放入各个中央认证的session
			// cas session
			request.getSession().setAttribute(CASFilter.CAS_SESSION_USER, account);
			request.getSession().setAttribute(CASFilter.CAS_SESSION_USER_ATTRIBUTES,
					new UserAttributes());
			// scut session
			request.getSession().setAttribute("scut:ids:uid", account);
			// cerid session
			// cerid filter 填充拦截判断信息
			String ticket = UUID.randomUUID().toString();
			Cookie cookie = new Cookie("CERUUS", ticket);
			/*
			 * if (domain != null && !"".equals(domain)) {//验证域名是否为IP地址 if
			 * (!isIPAdress(domain)) {//不是IP地址就设置域名（用来兼容谷歌浏览器）
			 * cookie.setDomain(domain); } }
			 */
			cookie.setMaxAge(60 * 60 * 24 * 7);
			cookie.setPath("/");
			response.addCookie(cookie);
			request.getSession().setAttribute("CERUUS", ticket);
			request.getSession().setAttribute("login_user_in_session", account);

		} else {
			// 中央认证
			System.out.println("中央认证开始");
			authType = "cas";
			account = casAccount;
			User userBaseBean = userService.getUserByAccount(account);
			if (userBaseBean == null) {
				System.out.println("中央认证创建本地用户");
				if (userBaseBean == null) {
					userBaseBean = new User();
				}

				userBaseBean.setAccount(account);
				userBaseBean.setName(name);
				userBaseBean.setPassword(account);
				userBaseBean.setUserbaseStatus(User.USER_STATUS_NORMAL);
				userService.createAccount(userBaseBean);

				// 默认注册一个同名马甲
				Member memberBean = new Member();
				memberBean.setAccount(account);
				memberBean.setName(account);
				memberBean.setMemberStatus(Member.STATUS_NORMAL);
				memberBean.setMemberType(Member.MEMBER_TYPE_PERSON);
				grouperService.createMember(memberBean);
			}
			password = account;
			System.out.println("中央认证模拟本地用户认证");
			account = this.shiroLogin(account, password);
		}
		session.setAttribute("authType",authType);
		StringBuffer returnStr = new StringBuffer();
		List<Member> members = new ArrayList<Member>();
		members = grouperService.getMembersByAccount(account);
		if (members == null || members.size() <= 0) {
			throw new GroupsException("该账号没有找到对应的member");
		}
		returnStr.append("{\"members\":[");
		for (Member member : members) {
			returnStr.append("{");
			returnStr.append("\"id\":").append(member.getId()).append(",");
			returnStr.append("\"name\":\"").append(JS.quote(Filter.convertHtmlBody(member.getName()))).append("\",");
			returnStr.append("\"signature\":\"").append(JS.quote(Filter.convertHtmlBody(member.getSignature()))).append("\",");
			if (member.getIcon() != null && member.getIcon().length() != 0) {
				returnStr.append("\"icon\":\"").append(PropertyUtil.getIconPrefix()).append(member.getIcon()).append("\"");
			}
			else {
				returnStr.append("\"icon\":\"").append(PropertyUtil.getDefaultIcon()).append("\"");
			}
			returnStr.append("},");
		}
		if (members.size() > 0)
			returnStr.setLength(returnStr.length() - 1);
		returnStr.append("]}");
		// 记录日志
//		String result = LogMessage.getSuccess();
//		System.out.println("member:"+currentUserWrap.getMemberName());
//		logService.addLoginLog(account, result, "");
		return returnStr.toString();
	}
	
	/**
	 * 中央认证
	 */
	@RequestMapping(value={"/casLogin"}, produces = "application/json; charset=UTF-8")
	public void casLogin( HttpServletRequest request, HttpServletResponse response) throws IOException {
		String terminal = TerminalGetter.getTerminal(request);
		String ip = IpGetter.getRemoteAddr(request);
		Subject currentUser = SecurityUtils.getSubject();
		
		if (currentUser.isAuthenticated()) {
			currentUser.logout();
		}
		Session session  = currentUser.getSession();
		CurrentUserWrap currentUserWrap = new CurrentUserWrap();
		currentUserWrap.setMemberIp(ip);
		currentUserWrap.setTerminal(terminal);
		session.setAttribute("currentUserWrap",currentUserWrap);
		
		
		String casAccount = null;
		String name = null;
		//数园cas&ccnlCAS(支持scut账号)
		if (casAccount == null) {
			casAccount = (String) request.getSession().getAttribute(CASFilter.CAS_SESSION_USER);
			name = casAccount;
			try{
				UserAttributes userAttribute = (UserAttributes) request.getSession().getAttribute(CASFilter.CAS_SESSION_USER_ATTRIBUTES);
				if (userAttribute != null && userAttribute.getAttributes("cas:cn") != null) {
					name = userAttribute.getAttributes("cas:cn")[0];
					if (name == null) {
						name = casAccount;
					}
				}
			}catch (Exception e){
				//转换异常标明是ccnl中央认证，可以不做处理
			}
		}
		//cerid
		if(casAccount == null){
			String user_in_session = (String) request.getSession().getServletContext().getAttribute("login_user_in_session");
			JSONObject jsonObject = JSONObject.fromObject(user_in_session);
			try {
				casAccount = jsonObject.getString("cerid");
				name = jsonObject.getString("userName");
			} catch (Exception e) {
				casAccount=null;
			}
		}
		
		String authType = null;
		String account = null;
		String entryptionPassword = null;
		// 中央认证
		System.out.println("中央认证开始");
		authType = "cas";
		account = casAccount;
		User userBaseBean = userService.getUserByAccount(account);
		if (userBaseBean == null) {
			System.out.println("中央认证创建本地用户");
			userBaseBean = new User();
			userBaseBean.setAccount(account+(int)((Math.random()*9+1)*100000));
			userBaseBean.setName(name);
			userBaseBean.setPassword(account);
			userBaseBean.setUserbaseStatus(User.USER_STATUS_NORMAL);
			userService.createAccount(userBaseBean);

			// 默认注册一个同名马甲
			Member memberBean = new Member();
			memberBean.setAccount(account);
			memberBean.setName(account);
			memberBean.setMemberStatus(Member.STATUS_NORMAL);
			memberBean.setMemberType(Member.MEMBER_TYPE_PERSON);
			grouperService.createMember(memberBean);
		}
		entryptionPassword = userBaseBean.getPassword();
		System.out.println("中央认证模拟本地用户认证");
		account = this.shiroLogin4ENPassword(account, entryptionPassword);

		session.setAttribute("authType",authType);
		
		List<Member> members = new ArrayList<Member>();
		members = grouperService.getMembersByAccount(casAccount);
		this.selectMemberForCas(members.get(0).getId(), response);
	}
	
	private String shiroLogin(String account, String password) {
		Subject currentUser = SecurityUtils.getSubject();
		if (account == null || password == null || account.trim().length() == 0
				|| password.trim().length() == 0) {
			throw new NotAuthorizedException("用户帐户名或者密码为空!");
		}
		String entryptionPassword = UserService.entryptPassword(password);
		if (!currentUser.isAuthenticated()) {
			////////////////////////////////
			try {
				UsernamePasswordToken token = new UsernamePasswordToken(account, entryptionPassword);
				token.setRememberMe(false);
				currentUser.login(token);
//				currentUser.login(new HtmlUsernamePasswordToken(account,entryptionPassword, false));
			} catch (Exception e) {
				currentUser.logout();
				logger.info(e.toString());
				throw new NotAuthorizedException("密码错误");
			}
			/////////////////////////////
		}
		return account;
	}

	private String shiroLogin4ENPassword(String account, String entryptionPassword) {
		Subject currentUser = SecurityUtils.getSubject();
		if (account == null || entryptionPassword == null || account.trim().length() == 0
				|| entryptionPassword.trim().length() == 0) {
			throw new NotAuthorizedException("用户帐户名或者密码为空!");
		}
		if (!currentUser.isAuthenticated()) {
			////////////////////////////////
			try {
				currentUser.login(new HtmlUsernamePasswordToken(account,entryptionPassword, false));
			} catch (Exception e) {
				currentUser.logout();
				logger.info(e.toString());
				throw new NotAuthorizedException("密码错误");
			}
			/////////////////////////////
		}
		return account;
	}

	
	public String encryptedAuthenticate( String account, String password, HttpServletRequest request, HttpServletResponse response) throws Exception{
		try {
			String priString = (String)request.getSession().getAttribute("privateKey");
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");  
			byte[] buffer2 = Base64.decode(priString);
            PKCS8EncodedKeySpec keySpec2 = new PKCS8EncodedKeySpec(buffer2);  
            RSAPrivateKey privateKey =  (RSAPrivateKey) keyFactory.generatePrivate(keySpec2);
			//System.out.println("pass："+password);
            account = Crypt.RsaDecrypt(account, privateKey);
			password = Crypt.RsaDecrypt(password, privateKey);
		} catch (Exception e) {
			throw new GroupsException("登录解密失败");
		}
		request.getSession().removeAttribute("privateKey");
		return this.authenticate( account, password, request, response);
	}
	

	/**
	 * 中央认证等其他登陆方式需要选择一个member
	 * @param memberId 
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="selectMember", produces = "application/json; charset=UTF-8")
	public String selectMember(Long memberId) {
		Subject currentUser = SecurityUtils.getSubject();
		Session session  = currentUser.getSession();
		String account = UserUtils.getAccount();
		StringBuffer returnStr = new StringBuffer();
		List<Member> members = grouperService.getMembersByAccount(account);
		for (Member member : members) {
			if (memberId.longValue() == member.getId().longValue()) {
				//将一些基本的member信息存储旦session中
				
				CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
				currentUserWrap.setMemberId(memberId);
				currentUserWrap.setMemberName(member.getName());
				currentUserWrap.setAccount(member.getAccount());
				session.setAttribute("currentUserWrap",currentUserWrap);
				this.selectOrCreateCategory(member);
				
				returnStr.append("{\"id\":").append(member.getId());						
				returnStr.append(",\"name\":\"").append(JS.quote(Filter.convertHtmlBody(member.getName())));					
				returnStr.append("\",\"signatrue\":\"").append(JS.quote(Filter.convertHtmlBody(member.getSignature())));			
				if (member.getIcon() != null && member.getIcon().length() != 0) {
					returnStr.append("\",\"icon\":\"").append(PropertyUtil.getIconPrefix()).append(member.getIcon()).append("\"");
				}
				else {
					returnStr.append("\",\"icon\":\"").append(PropertyUtil.getDefaultIcon()).append("\"");
				}					
				returnStr.append("}");
				return returnStr.toString();
			}
		}
		// 没有对应的用户
		throw new GroupsException("帐号不存在或者该帐号下没有可用用户");
	}
	
	public void selectMemberForCas(Long memberId, HttpServletResponse response) throws IOException {
		this.selectMember(memberId);
		response.sendRedirect("../pages/c/vue/#/cas");
	}
	
	/**
	 * 中央认证等其他登陆方式需要选择一个member
	 * @param memberId 
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="selectMember_v2", produces = "application/json; charset=UTF-8")
	public String selectMember_v2(Long memberId) {
		Subject currentUser = SecurityUtils.getSubject();
		Session session  = currentUser.getSession();
		String account = UserUtils.getAccount();
		List<Member> members = grouperService.getMembersByAccount(account);
		for (Member member : members) {
			if (memberId.longValue() == member.getId().longValue()) {
				//将一些基本的member信息存储旦session中
				CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
				currentUserWrap.setMemberId(memberId);
				currentUserWrap.setAccount(member.getAccount());
				currentUserWrap.setMemberName(member.getName());
				session.setAttribute("currentUserWrap",currentUserWrap);
//				this. selectOrCreateCategory(member);
				String result = LogMessage.getSuccess();
				System.out.println("member:"+currentUserWrap.getMemberName());
				logService.addLoginLog(account, result, "");
				return "{\"code\":200}";
			}
		}
		// 没有对应的用户
		throw new GroupsException("帐号不存在或者该帐号下没有可用用户");	
	}
	
	/**
	 * 首次登陆创建个人柜子并创建默认目录
	 * @param member
	 * @return
	 */
	private void selectOrCreateCategory(Member member){
		Subject currentUser = SecurityUtils.getSubject();
		Session session  = currentUser.getSession();
		String authType = (String)session.getAttribute("authType");				
		String[] categoryNames = PropertyUtil.getDefaultGroupCategory()[0]
				.split(":");
		String categoryName = categoryNames[0];
		String categoryDisplayName = categoryNames[1];
		Category category = null;
		List<Category> categories= categoryService.getCategoriesByName(categoryName);
		if (categories == null || categories.size() <= 0) {
			category = new Category();
			category.setName(categoryName);
			category.setDisplayName(categoryDisplayName);
			category.setParentId(0);
			category.setCategoryStatus(Category.STATUS_NORMAL);
			category.setCreateDate(new Timestamp(System
					.currentTimeMillis()));
			categoryService.createCategory(category, false, OldPerm.SYSTEMADMIN_MEMBER_ID);
		} else {
			category = categories.get(0);
		}
		Group group = groupService.getGroupByName(member.getId().toString());
		if( group == null || group.getId() < 0) {
			Group groupBean = new Group();
			groupBean.setName("" + member.getId());
			groupBean.setCreatorName(member.getAccount());
			groupBean.setDisplayName(member.getAccount());
			groupBean.setAddr("" + member.getId());
			groupBean.setCategory(category);
			groupBean.setCreateDate(new Timestamp(System
					.currentTimeMillis()));
			groupBean.setCreatorId(member.getId());
			groupBean.setGroupStatus(Group.STATUS_NORMAL);
			groupBean.setGroupUsage(Group.USAGE_PRIVATE);
			groupBean.setOwner(member.getAccount());
			
			GroupType typeBean = groupService.getGroupTypeByName(PropertyUtil
					.getPersonalGroupType());
			groupBean.setGroupType(typeBean);
			groupBean.setTotalFileSize(0L);//表示忽略
			groupService.createGroup(groupBean, false, null, OldPerm.SYSTEMADMIN_MEMBER_ID);

			PermCollection pc = new PermCollection();
			
			try
			{
				permService.modifyPermission(groupBean.getId(), member.getId(),OldPerm.PERM_TYPE_GROUP, pc);
			} catch(Exception e) {
				//todo nothing
			}
						

			resourceService.createResourceDir(groupBean.getId(), PropertyUtil
					.getRecyclerName(), 0, member.getId(), true);
			resourceService.createResourceDir(groupBean.getId(), "待办事项", 0,
					member.getId(), true, GroupResource.DOCUMENT_TYPE_UNKNOWN , 0);
			resourceService.createResourceDir(groupBean.getId(), "我的文档", 0,
					member.getId(), true, GroupResource.DOCUMENT_TYPE_UNKNOWN , 0);
			resourceService.createResourceDir(groupBean.getId(), "我的图片", 0,
					member.getId(), true, GroupResource.DOCUMENT_TYPE_UNKNOWN , 0);
			resourceService.createResourceDir(groupBean.getId(), "我的视频", 0,
					member.getId(), true, GroupResource.DOCUMENT_TYPE_UNKNOWN , 0);
			//创建邮件大附件文件夹
			resourceService.getPersonEmailAttachFolder(member.getId());
		}
	}

	
	/**
	 * 登录退出，返回用户登录界面
	 */
	@ResponseBody
	@RequestMapping(value="/logout")
	public void htmlLogout(HttpServletRequest request, HttpServletResponse response){
		if(UserUtils.getUser() != null){
			SecurityUtils.getSubject().logout();

			//移除中央认证的session
			HttpSession session = request.getSession();
			session.removeAttribute(CASFilter.CAS_SESSION_USER);
			session.removeAttribute(CASFilter.CAS_SESSION_USER_ATTRIBUTES);

			// 移除 scut session
			session.removeAttribute("scut:ids:uid");

			// 移除cerid相关session内容
			session.removeAttribute("CERUUS");
			session.removeAttribute("login_user_in_session");
			//String domain = ServletActionContext.getRequest().getParameter("domain");
			Cookie[] cookies = request.getCookies();
			for (Cookie cookie : cookies) {
				if (("CERUUS").equals(cookie.getName())) {
					cookie.setMaxAge(0);
					// 重点是这里1,必须设置domain属性的值
					//cookie.setDomain(domain);
					// 重点是这里2,必须设置path属性的值
					cookie.setPath("/");
					response.addCookie(cookie);
				}
			}

		}
		try {
			response.sendRedirect("/");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@ResponseBody
	@RequestMapping(value="/loginByAdmin", produces = "application/json; charset=UTF-8")
	public String loginByAdmin(String account, String password, String spy, HttpServletRequest request) throws Exception {
		String terminal = TerminalGetter.getTerminal(request);
		String ip = IpGetter.getRemoteAddr(request);
		Boolean result = userService.checkPassword(account, password);
		if (result) {
			List<Member> members = grouperService.getMembersByAccount(account);
			for (Member m : members) {
				if (permService.isAdmin(m.getId())) {
					User spyAccount = userService.getUserByAccount(spy);
					if (spyAccount == null) {
						throw new GroupsException("spy:"+spy+"对应的账号不存在");
					}
					String spyPasswd = spyAccount.getPassword();
					Subject currentUser = SecurityUtils.getSubject();

					if (currentUser.isAuthenticated()) {
						currentUser.logout();
					}
					try {
						currentUser.login(new HtmlUsernamePasswordToken(spy,spyPasswd, false));
					} catch (Exception e) {
						currentUser.logout();
						logger.info(e.toString());
						throw new NotAuthorizedException("密码错误");
					}

					Session session  = currentUser.getSession();
					CurrentUserWrap currentUserWrap = new CurrentUserWrap();
					currentUserWrap.setMemberIp(ip);
					currentUserWrap.setTerminal(terminal);
					session.setAttribute("currentUserWrap",currentUserWrap);
					List<Member> spyMembers = grouperService.getMembersByAccount(spy);
					if(spyMembers == null || spyMembers.get(0) == null) {
						throw new GroupsException("spy:"+spy+"没有对应的member账号");
					}
					this.selectMember(spyMembers.get(0).getId());
					// 记录日志
					String loginresult = LogMessage.getSuccess();
					logService.addLoginLog(spy, loginresult, "");
					return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
				}
			}
			throw new GroupsException("你不是管理员账号");
		} else {
			throw new GroupsException("管理员账号密码错误");
		}
	}
}