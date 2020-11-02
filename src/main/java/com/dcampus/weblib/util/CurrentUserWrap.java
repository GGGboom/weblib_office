package com.dcampus.weblib.util;

import java.io.Serializable;

/**
 * 记录日志减少参数长度
 * 用户信息记录
 * @author patrick
 *
 */
public class CurrentUserWrap implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private long memberId;
	
	private String memberIp;
	
	private String terminal;
	
	private String memberName;
	
	private String account;
	

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}

	public long getMemberId() {
		return memberId;
	}

	public void setMemberId(long memberId) {
		this.memberId = memberId;
	}

	public String getMemberIp() {
		return memberIp;
	}

	public void setMemberIp(String memberIp) {
		this.memberIp = memberIp;
	}

	public String getTerminal() {
		return terminal;
	}

	public void setTerminal(String terminal) {
		this.terminal = terminal;
	}
	

}
