package com.dcampus.sys.security;

import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * @author wfxu
 *
 */
public class CasUsernamePasswordToken extends UsernamePasswordToken {

	private static final long serialVersionUID = 1L;

	public CasUsernamePasswordToken() {
		super();
	}
	
	public CasUsernamePasswordToken(String username, String password, boolean rememberMe) {
		super(username, password, rememberMe);
	}

}
