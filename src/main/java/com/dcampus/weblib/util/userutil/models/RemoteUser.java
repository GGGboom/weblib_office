package com.dcampus.weblib.util.userutil.models;


/**
 * 远程调用的用户model
 * 
 * @author zdfeng
 */
public class RemoteUser {
	
	private String id;

	private String username;
	
	// 传输将在util中通过aes加密
	private String password;

	private String name;

	private String email;

	private String phone;

	private String address;
	
	private String zipcode;

	private String fax;

	public RemoteUser() {
	}
	
	public RemoteUser(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public RemoteUser(String id, String username, String password, String name, String email, String phone, String address, String zipcode, String fax) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.name = name;
		this.email = email;
		this.phone = phone;
		this.address = address;
		this.zipcode = zipcode;
		this.fax = fax;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}	
	
}
