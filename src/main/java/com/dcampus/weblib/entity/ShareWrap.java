package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "weblib_share_wrap")
public class ShareWrap extends BaseEntity{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	@Column(name="message")
	private String message;
	
	@Column(name="create_date")
	private Timestamp createDate;
	
	@Column(name="resource_ids")
	private String resourceIds;
	
	@Column(name="recipients")
	private String recipients;
	
	@Column(name="provider_id")
	private long providerId;
	
	@Column(name="size")
	private long size=0L;

	public Long getId() {

		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the createDate
	 */
	public Timestamp getCreateDate() {
		return createDate;
	}

	/**
	 * @param createDate the createDate to set
	 */
	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}

	/**
	 * @return the resourceIds
	 */
	public String getResourceIds() {
		return resourceIds;
	}

	/**
	 * @param resourceIds the resourceIds to set
	 */
	public void setResourceIds(String resourceIds) {
		this.resourceIds = resourceIds;
	}

	/**
	 * @return the recipients
	 */
	public String getRecipients() {
		return recipients;
	}

	/**
	 * @param recipients the recipients to set
	 */
	public void setRecipients(String recipients) {
		this.recipients = recipients;
	}



	public long getProviderId() {
		return providerId;
	}
	public void setProviderId(long providerId) {
		this.providerId = providerId;
	}
	/**
	 * @return the size
	 */
	public long getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(long size) {
		this.size = size;
	}
	
}
