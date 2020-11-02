package com.dcampus.weblib.util;


import javax.servlet.http.HttpServletRequest;

public class TerminalGetter {
	public static String getTerminal(HttpServletRequest request){
		String agent = request.getHeader("User-Agent");
		if (agent != null && agent.length() > 200) {
			agent = agent.substring(0, 200);
		}
		return agent;
	}
}