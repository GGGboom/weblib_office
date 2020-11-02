package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;
import java.sql.Timestamp;


/**
 * 描述柜子管理员的属性
 * 
 * @author patrick
 *
 */

@Entity
@Table(name = "weblib_group_manager")
public class GroupManager extends BaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	@Id  
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/** 柜子ID，外键（Group主键id） **/
	@Column(name = "group_id")
	private Long groupId;

	/** 用户ID，即柜子管理员ID，外键（Member主键id） **/
	@Column(name = "member_id")
	private Long memberId;

	/** 用户名字，即柜子管理员名字，外键（Member候选键name字段） **/
	@Column(name = "member_name",length=100)
	private String memberName;

	/** 柜子管理员优先级 **/
	@Column(name = "priority")
	private int priority;

	/** 柜子管理员绑定时间 **/
	@Column(name = "create_date", length=0)
	private Timestamp createDate;

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

	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public Timestamp getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}


	
	

}
