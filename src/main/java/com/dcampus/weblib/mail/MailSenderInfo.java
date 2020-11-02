package com.dcampus.weblib.mail;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;

import com.dcampus.weblib.entity.Global;
import com.dcampus.weblib.service.GlobalService;

/**
 * SMTP信息<br>
 * 系统启动时，由Spring调用init()方法进行初始化，init方法将从数据库中读取站点配置的smtp信息。
 * 
 * 对之前groups中的代码进行相应修改
 *
 * @author patrick
 *
 */
public class MailSenderInfo {
	
	@Autowired
	GlobalService globalService;
	
	private String sender;

	private String sendername;
	
	private String host;

	private String username;

	private String password;

	private int port;

	private Properties mailProperties = new Properties();

	public MailSenderInfo() {
		mailProperties.put("mail.smtp.timeout", 2000);
	}
	
	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Properties getMailProperties() {
		return mailProperties;
	}

	public void setAuth(boolean auth) {
		this.mailProperties.put("mail.smtp.auth", auth + "");
	}

	public boolean getAuth() {
		return Boolean.parseBoolean(this.mailProperties.getProperty(
				"mail.smtp.auth", "true"));
	}

	public String getSendername() {
		return sendername;
	}

	public void setSendername(String sendername) {
		this.sendername = sendername;
	}

	public void init() {
		Global bean = globalService.getGlobalConfig();
		this.host = bean.getSmtpHost();
		this.sender = bean.getSmtpSender();
		this.username = bean.getSmtpUsername();
		this.password = bean.getSmtpPassword();
		this.port = bean.getSmtpPort();
		this.sendername = bean.getSmtpSendername();
		this.mailProperties.setProperty("mail.smtp.auth", bean.isSmtpAuth()
				+ "");
	}
}
