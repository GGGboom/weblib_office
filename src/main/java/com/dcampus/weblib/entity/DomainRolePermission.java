package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;

/**
 * 多域角色权限表
 * @author patrick
 *
 */

@Entity
@Table(name = "weblib_domain_role_permission")
public class DomainRolePermission extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3081950187013496944L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "role_id")
	private DomainRole role;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "permission_id")
	private DomainPermission permission;

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
	 * @return the permission
	 */
	public DomainPermission getPermission() {
		return permission;
	}

	/**
	 * @param permission
	 *            the permission to set
	 */
	public void setPermission(DomainPermission permission) {
		this.permission = permission;
	}

}
