package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;
import com.dcampus.sys.entity.User;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;


/**
 * 日志类的基类
 * 主要是操作者的信息
 * @author patrick
 *
 */
@MappedSuperclass
public class Log extends BaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String ACTION_TYPE_ADD = "添加";
	public static final String ACTION_TYPE_DELETE = "删除";
	public static final String ACTION_TYPE_MODIFY = "修改";
	public static final String ACTION_TYPE_MOVE = "移动";
	public static final String ACTION_TYPE_COPY= "复制";
	public static final String ACTION_TYPE_CLOSE= "关闭";
	public static final String ACTION_TYPE_OPEN = "打开";
	public static final String ACTION_TYPE_EXPORT = "导出";
	public static final String ACTION_TYPE_IMPORT = "导入";
	public static final String ACTION_TYPE_RECOMMEND = "推荐";
	public static final String ACTION_TYPE_CANCEL = "取消";
	public static final String ACTION_TYPE_SET = "设置";
	public static final String ACTION_TYPE_SEND = "发送";
	public static final String ACTION_TYPE_APPLY = "申请";
	public static final String ACTION_TYPE_ACTIVATE = "激活"; 
	public static final String ACTION_TYPE_RESTORE = "还原";
	public static final String ACTION_TYPE_SHIELD = "屏蔽";
	public static final String ACTION_TYPE_PREVIEW = "预览";
	
	@Id
	@GeneratedValue
	private Long id;
	
	/**账户**/
	@Column(name="account")
	private String account;
	
	/**操作者姓名**/
	@Column(name="member_name")
	private String memberName;

//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "user_id")
//	private User user;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id")
	private User user;


	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
   
	/**操作者id**/
	@Column(name="member_id")
	private Long memberId;
	
	/**ip地址**/
	@Column(name="ip")
	private String ip;

	/**创建时间**/
	@Column(name="create_date",length=0)
	private Date createDate;

	/**终端**/
	@Column(name="terminal")
	private String terminal;

	/**动作**/
	@Column(name="action")
	private String action;

	/**描述**/
	@Lob
	@Basic(fetch = FetchType.LAZY)   
	@Type(type="text") 
	@Column(name="description")
	private String description;

	/**操作对象**/
	@Column(name="target_object")
	private String targetObject;

	/**操作对象id**/
	@Column(name="target_object_id")
	private long targetObjectId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public Long getMemberId() {
		return memberId;
	}

	public void setMemberId(long memberId2) {
		this.memberId = memberId2;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date date) {
		this.createDate = date;
	}

	public String getTerminal() {
		return terminal;
	}

	public void setTerminal(String terminal) {
		this.terminal = terminal;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTargetObject() {
		return targetObject;
	}

	public void setTargetObject(String targetObject) {
		this.targetObject = targetObject;
	}

	public long getTargetObjectId() {
		return targetObjectId;
	}

	public void setTargetObjectId(long targetObjectId2) {
		this.targetObjectId = targetObjectId2;
	}
	
	
	
	

}
