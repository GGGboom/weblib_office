package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;

/**
 * 
 * @author Pretty
 * Type指的是权限作用域类型
 * 
 */

@Entity
@Table(name="weblib_perm")
public class OldPerm extends BaseEntity {

	private static final long serialVersionUID = 1L;
	
	/** 权限作用域类型，可能为:全局、分类、柜子（独立的）、讨论区、分类下的柜子（继承于分类）、未知**/
	
	/** 权限作用域类型，全局**/
	public static final int PERM_TYPE_GLOBAL = 1;
	/** 权限作用域类型，分类**/
	public static final int PERM_TYPE_CATEGORY = 2;
	/** 权限作用域类型，柜子（独立的）**/
	public static final int PERM_TYPE_GROUP = 3;
	/** 权限作用域类型，讨论区**/
	public static final int PERM_TYPE_FORUM = 4;
	/** 权限作用域类型，分类下的柜子（继承于分类）**/
	public static final int PERM_TYPE_GROUP_OF_CATEGORY = 5;
	/** 权限作用域类型，未知**/
	public static final int PERM_TYPE_UNKNOWN = 0;

	
	/** 全局权限类型id **/
	public static final long GLOBAL_TYPE_ID = 0L;

	/** 全局非会员用户id **/
	public static final long GLOBAL_NONMEMBER_ID = -1L;

	/** 全局会员用户id **/
	public static final long GLOBAL_MEMBER_ID = 0L;
	
	/** 不用判断权限的memberid **/
	public static final long SYSTEMADMIN_MEMBER_ID = -1L;
	

	/**权限ID**/
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	/** 权限所作用的用户id，外键（Member主键id） **/
	/**有特殊0，-1代表所有用户等**/
	@Column(name="member_id")
	private long memberId;
	
	/** 权限作用域类型id，可能为 全局、分类、柜子、讨论区 **/
	@Column(name="type_id")
	private Long typeId;
	
	/** 权限码 **/
	@Column(name="perm_code")
	private Long permCode;
	
	/** 权限作用域类型，分为category和group等 **/
	@Column(name="perm_type")
//	@Enumerated(EnumType.ORDINAL)
	private int permType;
	
	/**
	 * 是否将此权限继承至子文件夹
	 */
	@Column(name = "inherit_to_child")
	private boolean inheritToChild = true;
	
    /**
     * 是否覆盖父文件夹继承下来的权限
     */
	@Column(name = "override_parent")
	private boolean overrideParent = true;
	
    /**
     * 标志是否是继承下来的
     */
	@Transient
	private boolean inherited = false;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public long getMemberId() {
		return memberId;
	}

	public void setMemberId(long memberId) {
		this.memberId = memberId;
	}

	public Long getTypeId() {
		return typeId;
	}

	public void setTypeId(Long typeId) {
		this.typeId = typeId;
	}

	public Long getPermCode() {
		return permCode;
	}

	public void setPermCode(Long permCode) {
		this.permCode = permCode;
	}

	public int getPermType() {
		return permType;
	}

	public void setPermType(int permType) {
		this.permType = permType;

	}

	public boolean isInherited() {
		return inherited;
	}

	public void setInherited(boolean inherited) {
		this.inherited = inherited;
	}

	public boolean isInheritToChild() {
		return inheritToChild;
	}

	public void setInheritToChild(boolean inheritToChild) {
		this.inheritToChild = inheritToChild;
	}

	public boolean isOverrideParent() {
		return overrideParent;
	}

	public void setOverrideParent(boolean overrideParent) {
		this.overrideParent = overrideParent;
	}

}
