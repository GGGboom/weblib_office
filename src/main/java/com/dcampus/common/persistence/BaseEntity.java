/**
 * Copyright &copy; 2012-2013 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.dcampus.common.persistence;

import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.Date;


/**
 * Entity支持类
 * @author ThinkGem
 * @version 2013-01-15
 */
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

	public static final long serialVersionUID = 1L;


//	/** 记录状态：启用 */
//	public static final int Status_enabled = 1;
//	/** 记录状态：停用 */
//	public static final int Status_disabled = 2;
//	/** 记录状态：删除 */
//	public static final int Status_deleted = 9;

	/** 最近更新时间 */
	private Date lastModified;
	

	public Date getLastModified() {
		return lastModified;
	}
	
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	
}
