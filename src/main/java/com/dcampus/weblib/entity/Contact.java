package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * 
 * @author patrick
 *
 */
@Entity
@Table(name = "weblib_contact")
public class Contact extends BaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "contact_name", length = 100)
	private String contactName;

	@Column(name = "create_date")
	private Timestamp createDate;
	
	@Column(name = "creator_id")
	private long creatorId;

	@Column(name = "creator_name")
	private String creatorName;

	@Column(name = "`desc`", length = 500)
	private String desc;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public Timestamp getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
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

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	

}
