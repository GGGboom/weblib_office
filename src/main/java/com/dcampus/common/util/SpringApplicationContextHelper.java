package com.dcampus.common.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;

/**
 * ApplicationContext对象获取帮助类<br>
 * 要使用该帮助类，需要在Spring配置中配置该类
 *
 * @author zim
 *
 */
public class SpringApplicationContextHelper implements ApplicationContextAware, ServletContextAware {

	private static SpringApplicationContextHelper helper;
	private ApplicationContext ctx = null;
	private ServletContext servletContext;
	private SpringApplicationContextHelper() {
	};

	/**
	 * 单例获取方法
	 *
	 * @return
	 */
	public static SpringApplicationContextHelper getInstance() {
		if (helper == null)
			helper = new SpringApplicationContextHelper();
		return helper;
	}

	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		this.ctx = ctx;
	}

	/**
	 * 获取ApplicationContext实例
	 *
	 * @return
	 */
	public ApplicationContext getApplicationContext() {
		return this.ctx;
	}

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public ServletContext getServletContext() {
		return servletContext;
	}
}
