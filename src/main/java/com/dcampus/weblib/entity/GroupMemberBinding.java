package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;


/**
 * 柜子用户绑定
 * status直接从baseEntity中继承
 * @author patrick
 *
 */

@Entity
@Table(name = "weblib_group_member_binding")
public class GroupMemberBinding extends BaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** 请求状态 **/
	public static final int STATUS_REQUEST = 0;

	/** 通过状态 **/
	public static final int STATUS_PASS = 1;
	

	@Id  
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/** 柜子Id，外键（Group主键id） **/
	@Column(name = "group_id")
	private Long groupId;

	/** 用户Id，外键（Member主键id） **/
	@Column(name = "member_id")
	private Long memberId;
	
	/** 用户圈子绑定状态 **/
	@Column(name="`status`")
	private int status;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getGroupId() {
		return groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	public Long getMemberId() {
		return memberId;
	}

	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	

}
