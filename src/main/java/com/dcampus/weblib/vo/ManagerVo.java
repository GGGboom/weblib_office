package com.dcampus.weblib.vo;

public final class ManagerVo {
	private long memberId;
	private String account;
	private String name;

	public ManagerVo(long memberId, String account, String name) {
		this.memberId = memberId;
		this.account = account;
		this.name = name;
	}

	/**
	 * @return the memberId
	 */
	public long getMemberId() {
		return memberId;
	}

	/**
	 * @param memberId
	 *            the memberId to set
	 */
	public void setMemberId(long memberId) {
		this.memberId = memberId;
	}

	/**
	 * @return the account
	 */
	public String getAccount() {
		return account;
	}

	/**
	 * @param account
	 *            the account to set
	 */
	public void setAccount(String account) {
		this.account = account;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

}