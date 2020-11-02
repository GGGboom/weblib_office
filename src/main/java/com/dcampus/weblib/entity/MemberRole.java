package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;
import java.sql.Timestamp;


@Entity
@Table(name = "weblib_member_role")
public class MemberRole extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "member_id")
	private Member member;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "role_id")
	private DomainRole role;

	@Column(name = "grant_time")
	private Timestamp grantTime;

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the member
	 */
	public Member getMember() {
		return member;
	}

	/**
	 * @param member
	 *            the member to set
	 */
	public void setMember(Member member) {
		this.member = member;
	}

	/**
	 * @return the role
	 */
	public DomainRole getRole() {
		return role;
	}

	/**
	 * @param role
	 *            the role to set
	 */
	public void setRole(DomainRole role) {
		this.role = role;
	}

	/**
	 * @return the grantTime
	 */
	public Timestamp getGrantTime() {
		return grantTime;
	}

	/**
	 * @param grantTime
	 *            the grantTime to set
	 */
	public void setGrantTime(Timestamp grantTime) {
		this.grantTime = grantTime;
	}

}
