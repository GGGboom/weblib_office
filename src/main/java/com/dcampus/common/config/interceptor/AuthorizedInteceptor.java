package com.dcampus.common.config.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NamedThreadLocal;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.dcampus.sys.util.UserUtils;
import com.dcampus.weblib.exception.PermissionsException;
import com.dcampus.weblib.service.permission.RolePermissionUtils;
import com.dcampus.weblib.service.permission.impl.Permission;

/**
 * 用户登录拦截器
 *
 * @author zim
 *
 */
public class AuthorizedInteceptor extends HandlerInterceptorAdapter {

	private static final long serialVersionUID = -6851496253025261992L;
	private NamedThreadLocal<Long>  startTimeThreadLocal =   
			new NamedThreadLocal<Long>("StopWatch-StartTime"); 
	
	String logUrl = "/log/";
	String grouperUrl = "/grouper/";
	String globalUrl = "/global/";
	String home = "/pages/c";
	
	@Autowired
	private Permission permission;
	
	@Autowired
	private RolePermissionUtils roleUtils;
	
	@Override  
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response,   
	Object handler) throws Exception {  
        long beginTime = System.currentTimeMillis();//1、开始时间  
        startTimeThreadLocal.set(beginTime);//线程绑定变量（该数据只有当前请求的线程可见）
	    //1、请求到登录页面 放行  
		String url = request.getRequestURI();
		Subject user = SecurityUtils.getSubject();
        if (url.indexOf(logUrl)>0 || url.contains(logUrl)){
        	//System.out.println(true);
        	if (user.isAuthenticated()) {
           		if(!permission.isAdmin(UserUtils.getCurrentMemberId())) {
            		if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_log_m")){
            			throw new PermissionsException("无日志管理权限");
            		}
           		}
                return true;
        	}
 
        }
        
//        if (url.indexOf(grouperUrl)>0 || url.contains(grouperUrl)){
//        	//System.out.println(true);
//        	
//        	if (user.isAuthenticated()) {
//           		if(!permission.isAdmin(UserUtils.getCurrentMemberId())) {
//            		if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_user_m")){
//            			throw new PermissionsException("无用户管理权限");
//            		}
//           		}
//                return true;
//        	}
// 
//        }
        
//        if (url.indexOf(globalUrl)>0 || url.contains(globalUrl)){
//        	//System.out.println(true);
//        	
//        	if (user.isAuthenticated()) {
//           		if(!permission.isAdmin(UserUtils.getCurrentMemberId())) {
//            		if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_sysconfig_m")){
//            			throw new PermissionsException("无系统配置权限");
//            		}
//           		}
//                return true;
//        	}
// 
//        }
        return true;
	          
//	    //2、TODO 比如退出、首页等页面无需登录，即此处要放行 允许游客的请求  
//	    
//	    if(UserUtils.getUser() != null) {  
//	        //更好的实现方式的使用cookie  
//	        return true;  
//	    } else {
//		    //response.sendRedirect(request.getContextPath() + home);  
//		    return false;  
//	    }


	} 
    @Override  
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,   
Object handler, Exception ex) throws Exception {  
        long endTime = System.currentTimeMillis();//2、结束时间  
        long beginTime = startTimeThreadLocal.get();//得到线程绑定的局部变量（开始时间）  
        long consumeTime = endTime - beginTime;//3、消耗的时间  
        if(consumeTime > 3000) {//此处认为处理时间超过500毫秒的请求为慢请求  
            //TODO 记录到日志文件  
            System.out.println(String.format("%s consume %d millis", request.getRequestURI(), consumeTime));  
        }          
    } 
}
