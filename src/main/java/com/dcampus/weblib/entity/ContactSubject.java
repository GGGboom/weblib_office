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
@Table(name = "weblib_contact_subject")
public class ContactSubject extends BaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public  static String SUBJECTTYPE_FOLDER = "folder";

	public  static String SUBJECTTYPE_TEAM = "team";

	public  static String SUBJECTTYPE_MEMBER = "member";
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contact_id")
	private Contact contact;

	@Column(name = "subject_id")
	private long subjectId;

	@Column(name = "subject_type")
	private String subjectType;

	@Column(name = "create_date")
	private Timestamp createDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Contact getContact() {
		return contact;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}

	public long getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(long subjectId) {
		this.subjectId = subjectId;
	}

	public String getSubjectType() {
		return subjectType;
	}

	public void setSubjectType(String subjectType) {
		this.subjectType = subjectType;
	}

	public Timestamp getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}
	
	

}
