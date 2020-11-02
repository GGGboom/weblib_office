package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;
import com.dcampus.sys.entity.User;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * 
 * @author patrick
 *
 */
@Entity
@Table(name = "weblib_contact_user")
public class ContactUser extends BaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contact_id")
	private Contact contact;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contact_subject_id")
	private ContactSubject contactSubject;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;
	
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

	public ContactSubject getContactSubject() {
		return contactSubject;
	}

	public void setContactSubject(ContactSubject contactSubject) {
		this.contactSubject = contactSubject;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Timestamp getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}
	
	
	
}
