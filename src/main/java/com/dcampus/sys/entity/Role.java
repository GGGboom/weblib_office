package com.dcampus.sys.entity;

import com.dcampus.common.persistence.BaseEntity;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * 管理员具体角色
 */
@Entity
@Table(name="sys_role")
public class Role extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/** 角色名称 */
	@Column(unique=true)
	@Length(min=1, max=100)
	private String name;

	/** 所属模块 */
	@Column(unique=true)
	private Module module;

	/** 拥有此角色的用户 */
	@OneToMany(mappedBy="role", cascade=CascadeType.REMOVE, fetch=FetchType.LAZY)
	private Set<UserRole> userRoles = new HashSet<UserRole>();

	/** 此角色拥有的操作 */
	@OneToMany(mappedBy="role", cascade=CascadeType.REMOVE, fetch=FetchType.LAZY)
	private Set<RolePerm> rolePerms = new HashSet<RolePerm>();

	public Role() {
	}
	public Role(Long id) {
		this.id = id;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Set<UserRole> getUserRoles() {
		return userRoles;
	}
	public void setUserRoles(Set<UserRole> userRoles) {
		this.userRoles = userRoles;
	}
	public Set<RolePerm> getRolePerms() {
		return rolePerms;
	}
	public void setRolePerms(Set<RolePerm> rolePerms) {
		this.rolePerms = rolePerms;
	}
	public Module getModule() {
		return module;
	}
	public void setModule(Module module) {
		this.module = module;
	}
}
