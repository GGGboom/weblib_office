package com.dcampus.weblib.web.filter;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import com.dcampus.common.util.StringUtils;

/**
 * url重定向
 * 使项目兼容之前的.action形式的url
 * @author patrick
 *
 */
public class URLRewritenFilter implements Filter{
	
	private FilterConfig config;

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		// TODO Auto-generated method stub
	        String uri = "";  
	        HttpServletRequest httpRequest = null;  
	        if (request instanceof HttpServletRequest) {  
	            httpRequest = (HttpServletRequest)request;  
//	            System.out.println("URL:" + httpRequest.getRequestURL().toString());
//	            Map<String, String[]> map = httpRequest.getParameterMap();
//	            if (map != null && !map.isEmpty()) {
//	                for(Entry<String , String[]> entry : map.entrySet()){
//	                    System.out.print(entry.getKey()+":");//entry.getKey() 参数名;
//	                    //entry.getValue();参数值，类型为数组
//	                    System.out.println(StringUtils.join((Object[]) entry.getValue(), ","));
//	                }
//	            }
	            uri = httpRequest.getRequestURI();  
	            uri = uri.split("\\.action")[0]; 
				request.getRequestDispatcher(uri).forward(request,
						response);
	        }   
	        else{  
	            //go to next Filter  
	            chain.doFilter(request, response);  
	        }  
    }  
		

	@Override
	public void init(FilterConfig config) throws ServletException {
		// TODO Auto-generated method stub
		this.config = config;
		
	}

}
