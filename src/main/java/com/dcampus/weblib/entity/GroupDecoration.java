package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;


/**
 * 柜子附属信息，包括logo图片，图标和标签等
 * 
 * 貌似也没用到？？
 * @author patrick
 *
 */

@Entity
@Table(name = "weblib_group_decoration")
public class GroupDecoration extends BaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** 柜子附属信息ID **/
	@Id  
	@GeneratedValue(strategy = GenerationType.AUTO) 
	private Long id;

	/** 柜子id，外键（Group主键id） **/
	@ManyToOne(optional = false)
	@JoinColumn(name = "group_id")
	private Group group;

	/** logo图片 **/
	@Column(name = "logo_icon", length=100)
	private String logoIcon;

	/** 柜子公告通知 **/
	@Column(name = "inform")
	private String inform;

	/** 柜子标签 **/
	@Column(length=100)
	private String tag;

	/** 柜子图标 **/
	@Column(name = "group_icon", length=100)
	private String groupIcon;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	public String getLogoIcon() {
		return logoIcon;
	}

	public void setLogoIcon(String logoIcon) {
		this.logoIcon = logoIcon;
	}

	public String getInform() {
		return inform;
	}

	public void setInform(String inform) {
		this.inform = inform;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getGroupIcon() {
		return groupIcon;
	}

	public void setGroupIcon(String groupIcon) {
		this.groupIcon = groupIcon;
	}
	
	

}
