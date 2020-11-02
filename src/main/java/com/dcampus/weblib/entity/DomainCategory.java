package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;

@Entity
@Table(name = "weblib_domain_category")
public class DomainCategory extends BaseEntity{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "domain_id",referencedColumnName="id")
	private Domain domain;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "category_id")
	private Category category;

	@Column(name = "total_capacity")
	private long totalCapacity;// kb

	@Column(name = "available_capacity")
	private long availableCapacity;// kb
	
	@Column(name = "relative_path")
	private String relativePath;

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

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public long getTotalCapacity() {
		return totalCapacity;
	}

	public void setTotalCapacity(long totalCapacity) {
		this.totalCapacity = totalCapacity;
	}

	public long getAvailableCapacity() {
		return availableCapacity;
	}

	public void setAvailableCapacity(long availableCapacity) {
		this.availableCapacity = availableCapacity;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

}
