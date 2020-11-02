package com.dcampus.weblib.exception;

import com.dcampus.common.config.ResourceProperty;

/**
 * Service层公用的Exception, 从由Spring管理事务的函数中抛出时会触发事务回滚.
 * @author ThinkGem
 */
public class GroupsException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public GroupsException() {
		super(ResourceProperty.getGroupsExceptionString());
	}

	public GroupsException(String message) {
		super(message);
	}

	public GroupsException(Throwable cause) {
		super(cause);
	}

	public GroupsException(String message, Throwable cause) {
		super(message, cause);
	}
}
