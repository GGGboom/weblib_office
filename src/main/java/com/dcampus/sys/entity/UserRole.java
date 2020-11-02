package com.dcampus.sys.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;

/**
 * 用户-角色
 */
@Entity
@Table(name="sys_user_role")
public class UserRole extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
    @ManyToOne(cascade=CascadeType.REFRESH, optional=false)
    @JoinColumn(name="user_id")
	private User user;

    @ManyToOne(cascade=CascadeType.REFRESH, optional=false)
    @JoinColumn(name="role_id")
	private Role role;
    
    public UserRole() {
	}
    public UserRole(Long id) {
    	this.id = id;
	}
    public UserRole(User user, Role role) {
    	this.user = user;
    	this.role = role;
	}
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Role getRole() {
		return role;
	}
	public void setRole(Role role) {
		this.role = role;
	}
}
