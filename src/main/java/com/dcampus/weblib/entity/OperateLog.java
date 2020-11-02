package com.dcampus.weblib.entity;

import javax.persistence.Entity;
import javax.persistence.Table;


/**
 * 操作日志类，直接继承基类中的属性即可
 * 
 * @author patrick
 *
 */

@Entity
@Table(name = "weblib_operate_log")
public class OperateLog extends Log {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
