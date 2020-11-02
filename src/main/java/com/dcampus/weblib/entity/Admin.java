package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;
import java.util.Date;


/**
 * 管理员信息，包括每个管理员对应的memberId和被管理类型<br>
 * 
 * 与应用的管理员是两个概念
 * 
 * @author patrick
 *
 */

@Entity
@Table(name = "weblib_admin")
public class Admin extends BaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**管理员类型,普通管理员***/
	public static final int NORMAL_ADMIN = 1;
	/**管理员类型，超级管理员***/
	public static final int SUPER_ADMIN= 2;

	
	
	/**管理员项id**/
	@Id  
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	/**管理员对应的用户**/
	@OneToOne(optional = false)
	@JoinColumn(name = "member_id")
	private Member member;
	
	
	/**管理员类型，用于数据库表示 **/
	@Column(name = "type")
	private int type;
	
	/**创建时间***/
	@Column(name = "create_date")
	private Date createDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	
}
