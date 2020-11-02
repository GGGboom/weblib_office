package com.dcampus.weblib.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.mail.internet.MimeUtility;

/**
 * 根据不同浏览器，对文件名进行编码。
 * 
 * @author zim
 * 
 */
public class FileEncoder {
	public static String encode(String filename, String userAgent)
			throws Exception {
		String new_filename = URLEncoder.encode(filename, "UTF-8");
		String rtn = "filename=\"" + new_filename + "\"";
		if (userAgent != null) {
			userAgent = userAgent.toLowerCase();
			// IE浏览器，只能采用URLEncoder编码
			if (userAgent.indexOf("msie") != -1) {
				rtn = "filename=\"" + new_filename + "\"";
			}
			// Opera浏览器只能采用filename*
			else if (userAgent.indexOf("opera") != -1) {
				rtn = "filename*=UTF-8''" + new_filename;
			}
			// Safari浏览器，只能采用ISO编码的中文输出
			else if (userAgent.indexOf("safari") != -1) {
				rtn = "filename=\""
						+ new String(filename.getBytes("UTF-8"), "ISO8859-1")
						+ "\"";
			}
			// Chrome浏览器，只能采用MimeUtility编码或ISO编码的中文输出
			else if (userAgent.indexOf("applewebkit") != -1) {
				new_filename = MimeUtility.encodeText(filename, "UTF8", "B");
				rtn = "filename=\"" + new_filename + "\"";
			}
			// FireFox浏览器，可以使用MimeUtility或filename*或ISO编码的中文输出
			else if (userAgent.indexOf("mozilla") != -1) {
				rtn = "filename*=UTF-8''" + new_filename;
			}
		}

		return rtn;
	}
	public static void main(String[] args) {
		String filename = "sadf&s%f.txt";
		try {
			System.out.println();
			URLEncoder.encode(filename, "UTF-8");
			URLDecoder.decode(filename, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
