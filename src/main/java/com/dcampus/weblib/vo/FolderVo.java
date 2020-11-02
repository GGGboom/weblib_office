package com.dcampus.weblib.vo;

public final class FolderVo {
	private long memberId;
	private String type = "folder";
	private String name;

	public FolderVo(long memberId, String name) {
		this.memberId = memberId;
		this.name = name;
	}

	/**
	 * @return the memberId
	 */
	public long getMemberId() {
		return memberId;
	}

	/**
	 * @param memberId the memberId to set
	 */
	public void setMemberId(long memberId) {
		this.memberId = memberId;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	

}
