package com.dcampus.weblib.entity;

import javax.persistence.Entity;
import javax.persistence.Table;


/**
 * 登录日志，登陆结果以及出错的原因
 * @author PatRick
 *
 */

@Entity
@Table(name = "weblib_login_log")
public class LoginLog extends Log {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**登录结果**/
	private String result;
	
	/**如果出错，出错的原因**/
	private String reason;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
	
	

}
