package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;


/**
 * 柜子拓展信息
 * 现在一般用来存放单文件大小和资源空间大小限制
 * 但是也可以用来做扩展使用，存放键值对
 * 
 * @author patrick
 *
 */
@Entity
@Table(name = "weblib_group_extern")
public class GroupExtern extends BaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** 柜子扩展信息id **/
	@Id  
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/** 柜子id，外键（Group主键id） **/
	@ManyToOne(optional = false)
	@JoinColumn(name = "group_id")
	private Group group;

	/** 柜子名字，外键（Group候选键name字段） **/
	@Column(name = "group_name")
	private String groupName;

	/** 柜子地址，外键（Group候选键addr字段） **/
	@Column(name = "group_addr")
	private String groupAddr;

	/** 柜子扩展信息键 **/
	@Column(name = "attr_key")
	private String attrKey;

	/** 柜子扩展信息值 **/
	@Column(name = "attr_value")
	private String attrValue;


	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attrKey == null) ? 0 : attrKey.hashCode());
		result = prime * result + (int) (group.getId() ^ (group.getId() >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final GroupExtern other = (GroupExtern) obj;
		if (attrKey == null) {
			if (other.attrKey != null)
				return false;
		} else if (!attrKey.equals(other.attrKey))
			return false;
		if (group.getId() != other.group.getId())
			return false;
		return true;
	}

	public String getAttrKey() {
		return attrKey;
	}

	public void setAttrKey(String attrKey) {
		this.attrKey = attrKey;
	}

	public String getAttrValue() {
		return attrValue;
	}

	public void setAttrValue(String attrValue) {
		this.attrValue = attrValue;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getGroupAddr() {
		return groupAddr;
	}

	public void setGroupAddr(String groupAddr) {
		this.groupAddr = groupAddr;
	}

}
