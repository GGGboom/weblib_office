package com.dcampus.weblib.util.userutil.models;

/**
 * 远程调用的分组、目录model
 * 
 * @author zdfeng
 */
public class RemoteItems {

	// uuid
	private String id;

	// extension
	private String name;

	// description
	private String description;

	// name
	private String fullname;

	// type Of Group or Stem
	//value groups of stems
	private String type;

	//
	private Boolean isLeaf;

	public RemoteItems() {
	}

	public RemoteItems(String id, String name, String description,
			String fullname, String type) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.fullname = fullname;
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean getIsLeaf() {
		return isLeaf;
	}

	public void setIsLeaf(Boolean isLeaf) {
		this.isLeaf = isLeaf;
	}

}
