package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;
import java.sql.Timestamp;


/**
 * 描述柜子的动态属性
 * 也就是一些操作的记录
 * 什么人在什么时间,在哪一范围对什么对象做了什么操作，结果如何
 * 
 * @author patrick
 *
 */

@Entity
@Table(name = "weblib_group_record")
public class GroupRecord extends BaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	/**描述动态产生位置，全局****/
	public static final String POSITION_GLOBAL = "global";
	/**描述动态产生位置，目录****/
	public static final String POSITION_CTEGORY = "category";
	/**描述动态产生位置，资源柜****/
	public static final String POSITION_GROUP = "group";
	/**描述动态产生位置，论坛****/
	public static final String POSITION_FORUM = "forum";


	
	/**动态的操作类型，新增***/
	public static final String ACTION_ADD = "add";
	/**动态的操作类型，更新***/
	public static final String ACTION_UPDATE = "update";
	/**动态的操作类型，删除***/
	public static final String ACTION_DELETE = "delete";
	/**动态的操作类型，分享***/
	public static final String ACTION_SHARE = "share";
	

	/**被操作对象类型描述，也是结果类型，分类目录**/
	public static final String OBJECT_TYPE_CATEGORY = "category";
	/**被操作对象类型描述，也是结果类型，帖子**/
	public static final String OBJECT_TYPE_POST = "post";
	/**被操作对象类型描述，也是结果类型，主题**/
	public static final String OBJECT_TYPE_THREAD = "thread";
	/**被操作对象类型描述，也是结果类型，论坛**/
	public static final String OBJECT_TYPE_FORUM = "forum";
	/**被操作对象类型描述，也是结果类型，文件柜**/
	public static final String OBJECT_TYPE_GROUP = "group";
	/**被操作对象类型描述，也是结果类型，member**/
	public static final String OBJECT_TYPE_MEMBER = "member";
	/**被操作对象类型描述，也是结果类型，资源**/
	public static final String OBJECT_TYPE_RESOURCE = "resource";
	
	
	/** 动态id **/
	@Id  
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/** 动态发起人id **/
	@Column(name = "member_id")
	private long memberId;

	/** 动态发起人名字，外键（Member候选键name字段） **/
	@Column(name = "member_name", length=200)
	private String memberName;

	/** 动态产生地id，目前产生地可能为全局、分类、柜子和讨论区四种 **/
	@Column(name = "position_id")
	private Long positionId;

	/** 动态产生地类型，供数据库使用 **/
	@Column(name="position_type")
	//@Enumerated(EnumType.STRING)
	private String recordPositionType;

	/** 动态产生时间 **/
	@Column(name = "create_date", length=0)
	private Timestamp createDate;

	/** 动态对象id，对象可能为分类、帖子、主题、讨论区、柜子、马甲、资源 **/
	@Column(name = "target_id")
	private Long targetId;

	/** 动态对象类型，供数据库使用 **/
	@Column(name="target_type")
	//@Enumerated(EnumType.STRING)
	private String recordTargetType;

	/** 动态的产生动作，供数据库使用 **/
	@Column(name="`action`")
	//@Enumerated(EnumType.STRING)
	private String recordAction;

	/** 动态的描述 **/
	@Column(name = "action_desc")
	private String actionDesc;

	/** 动态结果id，与targetId类似，可能为分类、帖子、主题、讨论区、柜子、马甲、资源 **/
	@Column(name = "result_id")
	private Long resultId;


	/** 动态结果类型，供数据库使用 **/
	@Column(name="result_type")
	//@Enumerated(EnumType.STRING)
	private String recordResultType;

	/** 动态结果描述 **/
	@Column(name = "result_desc")
	private String resultDesc;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}



	public long getMemberId() {
		return memberId;
	}

	public void setMemberId(long memberId) {
		this.memberId = memberId;
	}

	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}

	public Long getPositionId() {
		return positionId;
	}

	public void setPositionId(Long positionId) {
		this.positionId = positionId;
	}


	public Timestamp getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}

	public Long getTargetId() {
		return targetId;
	}

	public void setTargetId(Long targetId) {
		this.targetId = targetId;
	}


	public String getActionDesc() {
		return actionDesc;
	}

	public void setActionDesc(String actionDesc) {
		this.actionDesc = actionDesc;
	}

	public Long getResultId() {
		return resultId;
	}

	public void setResultId(Long resultId) {
		this.resultId = resultId;
	}


	public String getResultDesc() {
		return resultDesc;
	}

	public void setResultDesc(String resultDesc) {
		this.resultDesc = resultDesc;
	}

	public String getRecordPositionType() {
		return recordPositionType;
	}

	public void setRecordPositionType(String recordPositionType) {
		this.recordPositionType = recordPositionType;

	}

	public String getRecordTargetType() {
		return recordTargetType;
	}

	public void setRecordTargetType(String recordTargetType) {
		this.recordTargetType = recordTargetType;

	}

	public String getRecordAction() {
		return recordAction;
	}

	public void setRecordAction(String recordAction) {
		this.recordAction = recordAction;

	}

	public String getRecordResultType() {
		return recordResultType;
	}

	public void setRecordResultType(String recordResultType) {
		this.recordResultType = recordResultType;

	}

}
