package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "weblib_domain_manager")
public class DomainManager extends BaseEntity{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "domain_id")
	private Domain domain;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "manager_id")
	private Member manager; //管理员的memberId
	
	@Column(name = "authorizer_id")
	private Long authorizerId;//授权者memberId
	
	@Column(name = "create_date")
	private Timestamp createDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public Member getManager() {
		return manager;
	}

	public void setManager(Member manager) {
		this.manager = manager;
	}

	public Long getAuthorizerId() {
		return authorizerId;
	}

	public void setAuthorizerId(Long authorizerId) {
		this.authorizerId = authorizerId;
	}

	public Timestamp getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}
	
	
}
