package com.dcampus.weblib.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;


/**
 * 错误日志类
 * @author patrick
 *
 */
@Entity
@Table(name = "weblib_error_log")
public class ErrorLog extends Log{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**具体出错类型**/
	@Column(name="detail_type")
	private String detailType;

	public String getDetailType() {
		return detailType;
	}

	public void setDetailType(String detailType) {
		this.detailType = detailType;
	}
	
	
	

}
