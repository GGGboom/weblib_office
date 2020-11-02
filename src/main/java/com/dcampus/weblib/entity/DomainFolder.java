package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "weblib_domain_folder")
public class DomainFolder extends BaseEntity{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "domain_id")
	private Domain domain;

	@Column(name = "folder_uuid",nullable=false)
	private String folderUUID;//映射到grouper_stems
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "folder_id",nullable=false)
	private Member folder;

	@Column(name = "create_date")
	private Timestamp createDate;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "creator_id")
	private Member creator;//创建者的memberId

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

	public String getFolderUUID() {
		return folderUUID;
	}

	public void setFolderUUID(String folderUUID) {
		this.folderUUID = folderUUID;
	}

	public Member getFolder() {
		return folder;
	}

	public void setFolder(Member folder) {
		this.folder = folder;
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
	
	
}
