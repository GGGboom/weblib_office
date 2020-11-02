package com.dcampus.sys.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.dcampus.sys.entity.User;
import com.dcampus.sys.entity.UserRole;
import com.dcampus.sys.security.Principal;
import com.dcampus.sys.service.UserService;
import com.dcampus.weblib.exception.NotAuthorizedException;
import com.dcampus.weblib.util.CurrentUserWrap;

/**
 * 用户工具类
 */
@Service
@Lazy(false)
public class UserUtils implements ApplicationContextAware {
	
	private static UserService userService;
	
	public static User getUser(){
		User user = (User) getCache("user");
		if (user == null){
			Principal principal = (Principal)SecurityUtils.getSubject().getPrincipal();
			if (principal != null){
				user = userService.getUserByAccount(principal.getUsername());
				putCache("user", user);
			}
		}
		return user;
	}
	
	public static User getUser(boolean isRefresh){
		if (isRefresh){
			removeCache("user");
		}
		return getUser();
	}

	public static Long getUserId() {
		return getUser().getId();
	}
	public static String getAccount() {
		return getUser().getAccount();
	}
	public static String getCommonName() {
		return getUser().getName();
	}
	public static Date getLastLoginTime() {
		return getUser().getLastLoginTime();
	}

	@SuppressWarnings("unchecked")
	public static List<String> getRoleNameList(){
		List<String> roleKeyList = (List<String>)getCache("roleKeyList");
		if (roleKeyList == null){
			roleKeyList = new ArrayList<String>();
			User user = getUser();
			for (UserRole ur : user.getUserRoles()) {
				roleKeyList.add(ur.getRole().getName());
			}
			putCache("roleKeyList", roleKeyList);
		}
		return roleKeyList;
	}
	
	public static boolean hasRole(String key){
		boolean hasRole = false;
		List<String> roleKeyList = getRoleNameList();
		for(String role: roleKeyList){
			if(role.equals(key.trim())){
				hasRole = true;
				break;
			}
		}
		return hasRole;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext){
		userService = (UserService) applicationContext.getBean("userService");
	}
	
	// ============== User Cache ==============
	public static Object getCache(String key) {
		Object obj = getCacheMap().get(key);
		return obj==null?null:obj;
	}

	public static void putCache(String key, Object value) {
		getCacheMap().put(key, value);
	}

	public static void removeCache(String key) {
		getCacheMap().remove(key);
	}
	
	private static Map<String, Object> getCacheMap(){
		Principal principal = (Principal) SecurityUtils.getSubject().getPrincipal();
		return principal != null ? principal.getCacheMap() : new HashMap<String, Object>();
	}
	

	public final static long getCurrentMemberId() {
		 Subject user = SecurityUtils.getSubject();
		 Session session  = user.getSession();
		 CurrentUserWrap currentUser = (CurrentUserWrap)session.getAttribute("currentUserWrap");
		 if (currentUser == null) {
			 throw new NotAuthorizedException("用户未登录");
		 }
		 return currentUser.getMemberId();
	}
}
