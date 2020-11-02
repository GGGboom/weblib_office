package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "weblib_domain")
public class Domain extends BaseEntity{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "domain_name", length = 100)
	private String domainName;

	@Column(name = "create_date")
	private Timestamp createDate;

	//private Timestamp lastModified;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creator_id")
	private Member creator;


	@Column(name = "`desc`", length = 500)
	private String desc;
	
	@OneToOne(mappedBy = "domain", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private DomainCategory domainCategory;
	

//	@OneToMany(mappedBy="domain",cascade=CascadeType.REMOVE,fetch=FetchType.EAGER)
//	private Set<DomainFolder> domainFolder = new HashSet<DomainFolder>();

	@OneToMany(mappedBy="domain",cascade=CascadeType.DETACH,fetch=FetchType.EAGER)
	private Set<DomainFolder> domainFolder = new HashSet<DomainFolder>();
	
	@OneToMany(mappedBy="domain",cascade=CascadeType.REMOVE,fetch=FetchType.LAZY)
	private Set<DomainManager> domainManager = new HashSet<DomainManager>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public Timestamp getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}

	public Member getCreator() {
		return creator;
	}

	public void setCreator(Member creator) {
		this.creator = creator;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public DomainCategory getDomainCategory() {
		return domainCategory;
	}

	public void setDomainCategory(DomainCategory domainCategory) {
		this.domainCategory = domainCategory;
	}

	public Set<DomainFolder> getDomainFolder() {
		return domainFolder;
	}

	public void setDomainFolder(Set<DomainFolder> domainFolder) {
		this.domainFolder = domainFolder;
	}

	public Set<DomainManager> getDomainManager() {
		return domainManager;
	}

	public void setDomainManager(Set<DomainManager> domainManager) {
		this.domainManager = domainManager;
	}


	
}
