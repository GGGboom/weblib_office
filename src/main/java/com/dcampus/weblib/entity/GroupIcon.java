package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;


/**
 * 柜子图标类
 * @author patrick
 *
 */

@Entity
@Table(name = "weblib_group_icon")
public class GroupIcon extends BaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	
	@Id  
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	/** 图标名称 */
	private String name;
	
	/** 文件名称 */
	@Column(name = "file_name")
	private String fileName;
	
	/** 描述 */
	@Column(name="icon_desc")
	private String description;
	
	/** 顺序号 */
	@Column(name="enable_flag")
	private int sequence;
	
	/** 使用状态 */
	private boolean enable = true;
	

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

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

}
