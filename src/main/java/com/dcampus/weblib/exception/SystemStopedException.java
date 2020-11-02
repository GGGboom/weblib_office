package com.dcampus.weblib.exception;

/**
 * 系统暂停异常
 *
 * @author zim
 *
 */
public class SystemStopedException extends GroupsException {

	private static final long serialVersionUID = 2159103619669031518L;

	public SystemStopedException(String msg) {
		super(msg);
	}

}
