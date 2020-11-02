package com.dcampus.weblib.mail;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.context.ApplicationContext;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.dcampus.common.util.SpringApplicationContextHelper;
import com.dcampus.weblib.exception.GroupsException;

/**
 * 邮件发送器。<br>
 * SMTP信息通过在MailSenderInfo中配置。在系统启动时，将先从数据库中读取，并存储于其中。
 *
 * @author zim
 *
 */
public class MailSender {

	private static ApplicationContext context = SpringApplicationContextHelper
			.getInstance().getApplicationContext();;

	private static JavaMailSender sender;

	private static MailSenderInfo senderInfo;

	public static void sendMail(String mail, String topic, String body) {
		// 需要即时获取，以防MailSenderInfo有改变
		sender = (JavaMailSender) context.getBean("MailSender");
		senderInfo = (MailSenderInfo) context.getBean("MailSenderInfo");

		try {
			MimeMessage mailMessage = sender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mailMessage, true,
					"UTF-8");
			helper.setFrom(senderInfo.getSender());
			helper.setTo(mail);
			helper.setSubject(topic);
			helper.setText(body, true);
			helper.setSentDate(new Date(System.currentTimeMillis()));

			sender.send(mailMessage);
		} catch (MessagingException e) {
			throw new GroupsException(e);
		}
	}

	public static void sendMail(String[] recipient, String topic, String body) {
		// 需要即时获取，以防MailSenderInfo有改变
		sender = (JavaMailSender) context.getBean("MailSender");
		senderInfo = (MailSenderInfo) context.getBean("MailSenderInfo");
		
		try {
			MimeMessage mailMessage = sender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mailMessage, true,
					"UTF-8");
			InternetAddress from = new InternetAddress(senderInfo.getSender());
			from.setPersonal(senderInfo.getSendername());
			helper.setFrom(from);
			
			InternetAddress[] address =  new InternetAddress[recipient.length]; 
            int index = 0;
        	for (String email : recipient) {
        		address[index++] = new InternetAddress(email); 
            }
			helper.setTo(address);
			helper.setSubject(topic);
			helper.setText(body, true);
			helper.setSentDate(new Date(System.currentTimeMillis()));
			
			sender.send(mailMessage);
		} catch (MessagingException e) {
			throw new GroupsException(e);
		} catch (UnsupportedEncodingException e) {
			throw new GroupsException(e);
		}
	}
	
	public static void sendMail(String fromEmail, String recipient, String topic, String body) {
		// 需要即时获取，以防MailSenderInfo有改变
		sender = (JavaMailSender) context.getBean("MailSender");
		senderInfo = (MailSenderInfo) context.getBean("MailSenderInfo");

		try {
			MimeMessage mailMessage = sender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mailMessage, true,
					"UTF-8");
			InternetAddress from = new InternetAddress(senderInfo.getSender(), fromEmail);
			helper.setFrom(from);
			
			InternetAddress address =  new InternetAddress(recipient); 
           
			helper.setTo(address);
			helper.setSubject(topic);
			helper.setText(body, true);
			helper.setSentDate(new Date(System.currentTimeMillis()));

			sender.send(mailMessage);
		} catch (UnsupportedEncodingException e) {
			throw new GroupsException(e);
		} catch (MessagingException e) {
			throw new GroupsException(e);
		}
	}
	
	public static void main(String[] args) {
		//sendMail("zhzj@scut.edu.cn", "哈哈", "<b>这是一封加黑滴测试邮件！</b>");
	}
}
