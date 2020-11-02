package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;

/**
 * 多域角色
 * @author patrick
 *
 */

@Entity
@Table(name = "weblib_domain_role")
public class DomainRole extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7464197720241854878L;
	public static final String SYSTEM_ADMIN = "systemAdmin";
	public static final String DOMAIN_ADMIN = "domainAdmin";
	public static final String LOG_ADMIN = "logAdmin";
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	@Column(length = 100)
	private String name;
	@Column(name = "`desc`", length = 500)
	private String desc;

	/**
	 * @return the id
	 */
	public long getId() {
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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the desc
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * @param desc
	 *            the desc to set
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}
}
