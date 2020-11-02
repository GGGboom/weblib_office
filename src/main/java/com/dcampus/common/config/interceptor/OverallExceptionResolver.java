package com.dcampus.common.config.interceptor;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.shiro.authz.UnauthenticatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import com.dcampus.common.util.Log;
import com.dcampus.weblib.exception.ActionMessage;
import com.dcampus.weblib.exception.NotAuthorizedException;
import com.dcampus.weblib.exception.PermissionsException;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.exception.SystemStopedException;
import com.dcampus.weblib.service.LogService;

/**
 * 异常处理拦截器
 * @author patrick
 *
 */

public class OverallExceptionResolver implements HandlerExceptionResolver {

	@Autowired
	private LogService logService;
	 //记录数据库最大字符长度  
    private static final int WIRTE_DB_MAX_LENGTH = 50; 
    ActionMessage actionMessage = null;
	
	private static Log log = Log.getLog(OverallExceptionResolver.class);

    @Override
    public ModelAndView resolveException(HttpServletRequest request,
            HttpServletResponse response, Object handler, Exception exception) {

		Logger logger = LoggerFactory.getLogger("debug");
		String url = request.getRequestURL().toString();
		String param = request.getParameterMap().toString();
	    if(!(exception instanceof NotAuthorizedException)) {
	       logger.debug("exception url[{}] with param {}",url,param);
	       logger.debug("StackTrace",exception);
	    }
    	
		if (exception instanceof NotAuthorizedException) {
			actionMessage = new ActionMessage(ActionMessage.Type.ERROR,
					ActionMessage.CODE_NOT_LOGIN, exception.getMessage());
		} else if(exception instanceof UnauthenticatedException) {
			actionMessage = new ActionMessage(ActionMessage.Type.ERROR,
					ActionMessage.CODE_NOT_LOGIN, "请登录后再进行操作！");
		} else if(exception instanceof DataIntegrityViolationException) {
			actionMessage = new ActionMessage(ActionMessage.Type.ERROR,
					ActionMessage.CODE_NOT_LOGIN, "此位置已存在同名项！");
		} else if (exception instanceof SystemStopedException){
			actionMessage = new ActionMessage(ActionMessage.Type.ERROR,
					ActionMessage.CODE_SYSTEM_STOPED, exception.getMessage());
		} else if (exception instanceof PermissionsException){
			actionMessage = new ActionMessage(ActionMessage.Type.ERROR,
					ActionMessage.CODE_NO_PERMISSION, exception.getMessage());
		} else if (exception instanceof GroupsException){
			actionMessage = new ActionMessage(ActionMessage.Type.ERROR,
					ActionMessage.CODE_BAD_INPUT, exception.getMessage());
		}else {
			exception.printStackTrace();
			actionMessage = new ActionMessage(ActionMessage.Type.WARN, 
					ActionMessage.CODE_FORBIDDEN, exception.getMessage());
			actionMessage.getDetail();
			
		}
		
		String exceptionMessage = actionMessage.getDetail();  
        if(exceptionMessage != null){  
            if(exceptionMessage.length() > WIRTE_DB_MAX_LENGTH){  
                exceptionMessage = exceptionMessage.substring(0,WIRTE_DB_MAX_LENGTH);  
            }  
        } 
        
		if (!(exception instanceof NotAuthorizedException || exception instanceof UnauthenticatedException)) {
			logService.addErrorLog(actionMessage.getType().name(), exceptionMessage);
		}

        return processAjax(request, response, handler, exception);
	}
    
    private ModelAndView processAjax(HttpServletRequest request,  
            HttpServletResponse response, Object handler,  
            Throwable deepestException){  
        ModelAndView empty = new ModelAndView();  
        response.setHeader("Cache-Control", "no-store"); 
        response.setStatus(500);
        response.setContentType("application/json;charset=utf-8");
        JSONObject json = new JSONObject();  
        json.element("type","error");
        json.element("success", "false");
        json.element("code", "300");
        json.element("detail", actionMessage.getDetail()); 
        PrintWriter pw = null;  
        try {  
            pw=response.getWriter();  
            pw.write(json.toString());  
            pw.flush();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }finally{  
            pw.close();  
        }  
        empty.clear();  
        return empty;  
    }  

}

