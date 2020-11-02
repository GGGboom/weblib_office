package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;

/**
 * 资源柜类型，包含类型的名称，空间大小限制
 * 单文件大小限制，是否支持发送邮件，类型（内置和自定义）
 * 
 * @author patrick
 *
 */

@Entity
@Table(name = "weblib_group_type")
public class GroupType extends BaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**资源柜类型的类型，未知，内置的和用户自定义的*/
	/**资源柜类型的类型，内置的*/
	public static final int GROUP_TYPE_BUILDIN = 1;
	/**资源柜类型的类型,用户自定义的*/
	public static final int GROUP_TYPE_USERDEFINED = 2;
	/**资源柜类型的类型，未知的*/
	public static final int GROUP_TYPE_UNKNOWN = 0;
	
	/** 资源柜等级id **/
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;

	/** 资源柜类型名字 **/
	private String name;

	/** 该资源柜类型支持的资源空间大小，以K为单位 **/
	@Column(name = "total_file_size")
	private Long totalFileSize;

	/** 该资源柜类型支持的资源单文件大小，以K为单位 **/
	@Column(name ="single_file_size")
	private Long singleFileSize;

	/** 该柜子类型支持的是否在发帖后邮件通知 **/
	@Transient
	private boolean mailOnPost;

	/** 该资源柜类型支持的是否在发帖后邮件通知，用于数据库表示**/
	@Column(name="mail_on_post")
	private int groupMailOnPost;
	

	/**资源柜类型的类型，用于数据库使用*/
	@Column(name="`type`")
	private int groupType;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getTotalFileSize() {
		return totalFileSize;
	}

	public void setTotalFileSize(Long totalFileSize) {
		this.totalFileSize = totalFileSize;
	}

	public Long getSingleFileSize() {
		return singleFileSize;
	}

	public void setSingleFileSize(Long singleFileSize) {
		this.singleFileSize = singleFileSize;
	}

	public boolean isMailOnPost() {
		return mailOnPost;
	}

	public void setMailOnPost(boolean mailOnPost) {
		this.mailOnPost = mailOnPost;
		this.groupMailOnPost = mailOnPost ? 1 : 0;
	}

	public int getGroupMailOnPost() {
		return groupMailOnPost;
	}

	public void setGroupMailOnPost(int groupMailOnPost) {
		this.groupMailOnPost = groupMailOnPost;
		this.mailOnPost = groupMailOnPost > 0;
	}


	public int getGroupType() {
		return groupType;
	}

	public void setGroupType(int groupType) {
		this.groupType = groupType;
	}
}
