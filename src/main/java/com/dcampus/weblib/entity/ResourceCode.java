package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;
import java.sql.Timestamp;


/**
 * 资源外链分享
 * 
 * 一个资源和只能有一个外链
 * 
 * 
 * 
 * @author patrick
 *
 */

@Entity
@Table(name = "weblib_group_resource_code")
public class ResourceCode extends BaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 不需要提取码加密
	 */
	public static final int NOT_NEED_SETCODE = 0; 
	/**
	 * 需要提取码加密
	 */
	public static final int NEED_SETCODE = 1; 
	
	/** id **/
	@Id  
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	/** 资源id **/
	@ManyToOne(optional=false,fetch=FetchType.EAGER)
	@JoinColumn(name="resource_id")
	private GroupResource groupResource;

	/** 资源可提取次数 **/
	@Column(name="valid_times")
	private int validTimes;

	/** 资源过期时间 **/
	@Column(name = "expired_date")
	private Timestamp expiredDate;

	/** 资源注册者id **/
	@ManyToOne(optional =false)
	@JoinColumn(name="register_id")
	private Member register;

	/**提取码内容**/
	@Column
	private String code;
	
	/**令牌字符串**/
	@Column
	private String token;	
	
	/**是否需要提取码**/
	@Column(name ="set_code")
	private int setCode;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	

	public GroupResource getGroupResource() {
		return groupResource;
	}

	public void setGroupResource(GroupResource groupResource) {
		this.groupResource = groupResource;
	}

	public int getValidTimes() {
		return validTimes;
	}

	public void setValidTimes(int validTimes) {
		this.validTimes = validTimes;
	}

	public Timestamp getExpiredDate() {
		return expiredDate;
	}

	public void setExpiredDate(Timestamp expiredDate) {
		this.expiredDate = expiredDate;
	}


	public Member getRegister() {
		return register;
	}

	public void setRegister(Member register) {
		this.register = register;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public int getSetCode() {
		return setCode;
	}

	public void setSetCode(int setCode) {
		this.setCode = setCode;
	}	
	
	

}
