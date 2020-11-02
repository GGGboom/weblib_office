package com.dcampus.sys.entity;

import com.dcampus.common.persistence.BaseEntity;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


/**
 * 用户信息类
 * 与weblib_userbase一致，放到sys中进行处理
 * 
 * 
 * @author patrick
 *
 */
@Entity
@Table(name = "sys_user")
public class User extends BaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** 记录状态：普通状态*/
	public static final String USER_STATUS_NORMAL = "normal";
	/** 记录状态：关闭状态 */
	public static final String USER_STATUS_DELETE = "delete";
	
	/**用户创建类型，增加*/
	public static final String USER_CREATE_TYPE_ADD = "add";
	/**用户创建类型，注册*/
	public static final String USER_CREATE_TYPE_REGISTER = "register";
	
	/**获取公钥是否可用,可用*/
	public static final String USER_PUBLICKEY_IS_AVILIABLE = "true";
	/**获取公钥是否可用,不可用*/
	public static final String USER_PUBLICKEY_NOT_AVILIABLE = "false";
	
	
	/** 用户id **/
	@Id  
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/** 用户名 **/
	private String account;

	/** 用户密码 **/
	private String password;

	/** 用户姓名 **/
	private String name;

	/** 所在单位 **/
	private String company;

	/** 所在部门 **/
	private String department;

	/** 职位 */
	private String position;

	/** 用户邮箱 **/
	private String email;

	/** 用户电话 **/
	private String phone;

	/** 用户手机 **/
	private String mobile;

	/** 用户IM **/
	private String im;

	
	/** 用户创建类型，用于与数据交互 **/
	@Column(name="`create_type`")
	private String userCreateType;
	
	/** 用户状态，用于与数据交互 **/
	@Column(name="`status`")
	private String userbaseStatus;

	// ////////////////////////////////////////////////////////////
	/** 用户公钥 **/
	@Lob
	@Basic(fetch = FetchType.LAZY)   
	@Type(type="text") 
	@Column(name = "public_key")
	private String publicKey;

	/** 用户私钥 **/
	@Lob
	@Basic(fetch = FetchType.LAZY)   
	@Type(type="text") 
	@Column(name = "private_key")
	private String privateKey;

	/** 用户序列码 **/
	@Column(name = "serial_number")
	private String serialNumber;


	/** 用户公钥是否可用，用于与数据库交互 **/
	@Column(name="is_available")
	private String userbaseIsAvailable;
	
	/**应用名称**/
	@Column(name = "app_name")
	private String appName;
	
	@Column(name="photo")
	private String photo;
	
	@Column(name="register_from")
	private String registerFrom;
	
	@Column(name="creator")
	private String creator;
	
	///////shiro框架添加的属性///////
	/** 最后登陆时间 */
	@Column(name="last_login_time")
	private Date lastLoginTime;
	/** 最后登陆ip */
	@Column(name="last_login_ip")
	private String lastLoginIp;
	
	/** 拥有的角色 */
	@OneToMany(mappedBy="user", cascade=CascadeType.REMOVE, fetch=FetchType.LAZY)
	private Set<UserRole> userRoles = new HashSet<UserRole>();
	
	public User() {
	}
	public User(Long id) {
		this.id = id;
	}

	public Date getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(Date lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public String getLastLoginIp() {
		return lastLoginIp;
	}

	public void setLastLoginIp(String lastLoginIp) {
		this.lastLoginIp = lastLoginIp;
	}

	public Set<UserRole> getUserRoles() {
		return userRoles;
	}

	public void setUserRoles(Set<UserRole> userRoles) {
		this.userRoles = userRoles;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAccount() {
		return account;
	}

	public Long getId() {
		return id;
	}

	public String getPassword() {
		return password;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public String getUserCreateType() {
		return userCreateType;
	}

	public void setUserCreateType(String userCreateType) {
		this.userCreateType = userCreateType;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getIm() {
		return im;
	}

	public void setIm(String im) {
		this.im = im;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}


	public String getUserbaseIsAvailable() {
		return userbaseIsAvailable;
	}

	public void setUserbaseIsAvailable(String userbaseIsAvailable) {
		this.userbaseIsAvailable = userbaseIsAvailable;
	}
	public String getPhoto() {
		return photo;
	}
	public void setPhoto(String photo) {
		this.photo = photo;
	}
	public String getUserbaseStatus() {
		return userbaseStatus;
	}
	public void setUserbaseStatus(String userbaseStatus) {
		this.userbaseStatus = userbaseStatus;
	}
	public String getRegisterFrom() {
		return registerFrom;
	}
	public void setRegisterFrom(String registerFrom) {
		this.registerFrom = registerFrom;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	
	

}
