package com.dcampus.weblib.exception;

/**
 * 用户未登录异常
 *
 * @author zim
 *
 */
public class NotAuthorizedException extends GroupsException {

	private static final long serialVersionUID = -4989642353631112170L;

	public NotAuthorizedException(String msg) {
		super(msg);
	}

}
