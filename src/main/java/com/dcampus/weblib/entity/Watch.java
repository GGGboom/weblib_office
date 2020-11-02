package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;

/**
 * 我的收藏，之前是收藏圈子和主题
 * 现在暂时只考虑收藏柜子，可能会进一步收藏文件
 * 数据字典不变
 * @author patrick
 *
 */
@Entity
@Table(name = "weblib_watch")
public class Watch extends BaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 被收藏类型，柜子
	 */
	public static final String WATCH_TYPE_GROUP = "group";
	public static final String WATCH_TYPE_THREAD = "thread";
	/**
	 * 收藏状态，激活
	 */
	public static final String WATCH_STATUS_ACTIVE = "active";
	/**
	 * 收藏状态，未激活
	 */
	public static final String WATCH_STATUS_INACTIVE= "inactive";
	
	/** 关注id **/
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)	
	private long id;

	
	/** 关注的目标id，可能为柜子 **/
	@Column(name="target_id")
	private long targetId;

	/** 关注者id，外键（MemberBean主键id） **/
	@Column(name="member_id")
	private long memberId;

	/** 关注目标类型，供数据库使用 **/
	@Column(name="watch_type")
	private String watchType;

	/** 关注状态，供数据库使用。激活之后，任何动态都会通知用户，未激活的则不会 **/
	@Column(name="watch_status")
	private String watchStatus = WATCH_STATUS_INACTIVE; 

	public Long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getTargetId() {
		return targetId;
	}

	public void setTargetId(long targetId) {
		this.targetId = targetId;
	}

	public long getMemberId() {
		return memberId;
	}

	public void setMemberId(long memberId) {
		this.memberId = memberId;
	}

	public String getWatchType() {
		return watchType;
	}

	public void setWatchType(String watchType) {
		this.watchType = watchType;
	}

	public String getWatchStatus() {
		return watchStatus;
	}

	public void setWatchStatus(String watchStatus) {
		this.watchStatus = watchStatus;
	}

}
