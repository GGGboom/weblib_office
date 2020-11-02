package com.dcampus.sys.security;

import org.apache.shiro.authc.UsernamePasswordToken;

public class HtmlUsernamePasswordToken extends UsernamePasswordToken {
	private static final long serialVersionUID = 1L;

	public HtmlUsernamePasswordToken() {
		super();
	}
	
	public HtmlUsernamePasswordToken(String username, String password, boolean rememberMe) {
		super(username, password, rememberMe);
	}
}
