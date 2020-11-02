package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * 应用和用户的中间表
 * 存放应用和用户的对应关系
 * @author patrick
 *
 */

@Entity
@Table(name = "weblib_app_member")
public class AppMember extends BaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/** 用户ID* */
	@Column(name = "member_id")
	private Long memberId;

	/** 应用ID* */
	@Column(name = "application_id")
	private Long applicationId;

	/** 是否管理员 0为否，1为是* */
	@Column(name = "is_manager")
	private int isManager;

	/** 创建时间* */
	@Column(name = "create_date")
	private Timestamp createDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getMemberId() {
		return memberId;
	}

	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}

	public Long getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(Long applicationId) {
		this.applicationId = applicationId;
	}

	public int getIsManager() {
		return isManager;
	}

	public void setIsManager(int isManager) {
		this.isManager = isManager;
	}

	public Timestamp getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}
	
	
	

}
