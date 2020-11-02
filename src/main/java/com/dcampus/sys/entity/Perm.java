package com.dcampus.sys.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * 操作权限 Permission 对象
 */
@Entity
@Table(name="sys_perm")
public class Perm extends BaseEntity {

	private static final long serialVersionUID = 1L;

	/** 操作关键字， 格式为    资源模块:资源类型:资源操作（官方定义：资源类型:资源操作:资源ID） */
	@Id
//	@GeneratedValue(generator="idGenerator")    
//	@GenericGenerator(name="idGenerator", strategy="assigned")
	@Column(length=100)
	private String id;

	/** 操作名称 */
	@Column(nullable=false)
	private String name;

	/** 拥有此操作的角色 */
	@OneToMany(mappedBy="perm", cascade=CascadeType.REMOVE, fetch=FetchType.LAZY)
	private Set<RolePerm> rolePerms = new HashSet<RolePerm>();

	public Perm() {
	}

	public Perm(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Set<RolePerm> getRolePerms() {
		return rolePerms;
	}
	public void setRolePerms(Set<RolePerm> rolePerms) {
		this.rolePerms = rolePerms;
	}
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
