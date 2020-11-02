package com.dcampus.weblib.util;

import javax.servlet.http.HttpServletRequest;

/**
 * 获取用户ip的工具类
 *
 * @author zim
 *
 */
public class IpGetter {

	/**
	 * 获取用户ip
	 *
	 * @param request
	 * @return
	 */
	public static String getRemoteAddr(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null || ip.trim().length() == 0
				|| "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.trim().length() == 0
				|| "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.trim().length() == 0
				|| "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}
}
