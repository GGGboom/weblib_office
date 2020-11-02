package com.dcampus.sys.security;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.subject.Subject;


/**
 * roles or AuthorizationFilter
 */
public class RolesOrAuthorizationFilter extends org.apache.shiro.web.filter.authz.AuthorizationFilter {
	@Override
	protected boolean isAccessAllowed(ServletRequest servletrequest,
			ServletResponse servletresponse, Object mappedValue) throws Exception {
		
		 Subject subject = getSubject(servletrequest, servletresponse);
		 String[] rolesArray = (String[]) mappedValue;
		 if (rolesArray == null || rolesArray.length == 0) {
			 return true;
		 }
		 for(int i = 0; i < rolesArray.length; i++) {
			 if (subject.hasRole(rolesArray[i])) {
				 return true;
			 }
		 }
		 return false;
	}
}
