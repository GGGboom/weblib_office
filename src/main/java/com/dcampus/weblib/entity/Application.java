package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;
import java.sql.Timestamp;


/**
 * 应用类,每个应用有个应用根分类目录
 * 
 * @author patrick
 *
 */


@Entity
@Table(name = "weblib_application")
public class Application extends BaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/** 应用名字* */
	@Column(length = 100)
	private String name;

	/** 应用描述* */
	@Column(name = "description", length = 500)
	private String desc;

	/** 创建者id，外键（Member主键id）* */
	@Column(name = "creator_id")
	private long creatorId;

	/** 创建者名字，外键（Member候选键name字段）* */
	@Column(name = "creator_name")
	private String creatorName;

	/** 创建时间* */
	@Column(name = "create_date")
	private Timestamp createDate;
	
	/** 目录id，外键（Category主键id）* */
	@Column(name = "category_id")
	private long categoryId;
	
	/** 应用总空间* */
	@Column(name = "total_space")
	private String totalSpace;
	
	/** 应用剩余可用空间* */
	@Column(name = "available_space")
	private String availableSpace;

	/** 应用关联的域*/
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "domain_id",referencedColumnName="id")
	private Domain domain;

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

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

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}


	public long getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(long creatorId) {
		this.creatorId = creatorId;
	}

	public String getCreatorName() {
		return creatorName;
	}

	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}

	public Timestamp getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}



	public long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(long categoryId) {
		this.categoryId = categoryId;
	}

	public String getTotalSpace() {
		return totalSpace;
	}

	public void setTotalSpace(String totalSpace) {
		this.totalSpace = totalSpace;
	}

	public String getAvailableSpace() {
		return availableSpace;
	}

	public void setAvailableSpace(String availableSpace) {
		this.availableSpace = availableSpace;
	}	
	
	


}
