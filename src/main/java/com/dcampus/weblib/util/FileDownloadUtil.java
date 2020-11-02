package com.dcampus.weblib.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dcampus.common.util.Crypt;
import com.dcampus.weblib.util.FileNameEncoder;

/**
 * 
 * 提供断点续传下载的工具类
 * 
 * @author dqyang
 *
 */
public class FileDownloadUtil {

	/**
	 * 解析 http 请求头中的参数，将文件写出到 http 输出流
	 * @param file 写出到http的文件
	 * @param filename 文件名
	 * @param request http请求
	 * @param response http响应
	 * @throws IOException
	 */
	public static void output(File file, String filename, HttpServletRequest request,
			HttpServletResponse response, int isInline) throws IOException{
		
		if(!file.isFile())
			throw new FileNotFoundException();
		
		byte[] keys = Crypt.encryptKey(file);
		
		long contentLength = file.length();
		int enLen = 0;
		if (keys != null) {
			contentLength = Crypt.realLength(file);
			enLen = Crypt.EncryptLen;
		}
		long byteStart = 0, byteEnd = contentLength;
		boolean rf = false;
		if (request.getHeader("Range") != null) {
			String rangeFull = request.getHeader("Range");
			String[] range = rangeFull.split("=");
			
			
			
			if (range.length == 2) {
				rf = true;
				rangeFull = range[1].trim();
				range = rangeFull.split("-");
				if (range.length > 0) {
					long rangeByteStart = -1L;
					try {
						rangeByteStart = Integer.decode(range[0]).intValue();
					} catch (NumberFormatException ex) {
						
					}
					if ((rangeByteStart >= 0L)
							&& (rangeByteStart < contentLength)) {
						byteStart = rangeByteStart;
					}
					
					if (range.length > 1) {
						long rangeByteEnd = -1L;
						try {
							rangeByteEnd = Integer.decode(range[1]).intValue();
						} catch (NumberFormatException ex) {
							
						}
						if ((rangeByteEnd >= 0L) && (rangeByteEnd < contentLength)) {
							byteEnd = rangeByteEnd + 1;
						}
					}
				}
			}
		}

		long total = byteEnd - byteStart;
		
		response.reset();
		response.setHeader("Content-Length", "" + total);
		response.addHeader("Accept-Ranges", "bytes");
		response.addHeader("Last-Modified", toRfc1123(file.lastModified()));
		
		if (total == contentLength) {
			response.setStatus(HttpServletResponse.SC_OK);
		} else {
			response.addHeader("Content-Range", "bytes " + byteStart + "-" + (byteEnd - 1)
					+ "/" + contentLength);

			response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
		}
		String contentType = setContentType(filename);
		response.setContentType(contentType+"; charset=utf-8");
		
		//response.setContentType("application/octet-stream");
		if(filename != null){
			String toFileName = filename;
			try {
				toFileName = FileNameEncoder.encode(filename, request.getHeader("User-agent"));
			} catch (Exception e) {
				toFileName=" filename=" + new String(filename.getBytes("gbk"), "iso-8859-1");
			}
			if (isInline == 1) {
				response.setHeader("Content-Disposition",
		                "inline; " + toFileName);
			} else {
				response.setHeader("Content-Disposition",
		                "attachment; " + toFileName);
			}
			
		}
		/*String contentType = file.getContentType();
		if (contentType != null) {
			response.setContentType(contentType);
		}*/
		InputStream in = new BufferedInputStream(new FileInputStream(file));
		OutputStream out = response.getOutputStream();
		
		
		//InputStream in = new ByteArrayInputStream(data); 
		in.skip(byteStart+enLen);
		byte[] buffer = new byte[1024*1024];
		int read = -1;
		try{
			while((read = in.read(buffer)) > 0){
				if(keys != null) {
					long start = byteStart%(long)enLen;
					Crypt.en(buffer, keys, (int)start);
				}	
				if(total < read){
					out.write(buffer, 0, (int)total);
					break;
				}else{
					out.write(buffer, 0, read);
					total -= read;
				}
			}			
		}catch(Exception e){
			Throwable te = e.getCause();
			if(!(te instanceof SocketException))
				if(e instanceof IOException)
					throw (IOException)e;
				else
					throw new IOException(e.getLocalizedMessage());
		}finally{
			try{
				in.close();
			}catch(Exception fe){
				//IGNORE
			}
			try{
	            out.flush();
	            out.close();
			}catch(Exception fe){
				//IGNORE
			}
		}
	}
	
	public static String setContentType(String returnFileName){
        String contentType = "application/octet-stream";  
        if (returnFileName.lastIndexOf(".") < 0)  
            return contentType;  
        returnFileName = returnFileName.toLowerCase();  
        returnFileName = returnFileName.substring(returnFileName.lastIndexOf(".")+1);  
          
        if (returnFileName.equals("html") || returnFileName.equals("htm") || returnFileName.equals("shtml")){  
            contentType = "text/html";  
        } else if (returnFileName.equals("css")){  
            contentType = "text/css";  
        } else if (returnFileName.equals("xml")){  
            contentType = "text/xml";  
        } else if (returnFileName.equals("gif")){  
            contentType = "image/gif";  
        } else if (returnFileName.equals("jpeg") || returnFileName.equals("jpg")){  
            contentType = "image/jpeg";  
        } else if (returnFileName.equals("js")){  
            contentType = "application/x-javascript";  
        } else if (returnFileName.equals("atom")){  
            contentType = "application/atom+xml";  
        } else if (returnFileName.equals("rss")){  
            contentType = "application/rss+xml";  
        } else if (returnFileName.equals("mml")){  
            contentType = "text/mathml";   
        } else if (returnFileName.equals("txt")){  
            contentType = "text/plain";  
        } else if (returnFileName.equals("jad")){  
            contentType = "text/vnd.sun.j2me.app-descriptor";  
        } else if (returnFileName.equals("wml")){  
            contentType = "text/vnd.wap.wml";  
        } else if (returnFileName.equals("htc")){  
            contentType = "text/x-component";  
        } else if (returnFileName.equals("png")){  
            contentType = "image/png";  
        } else if (returnFileName.equals("tif") || returnFileName.equals("tiff")){  
            contentType = "image/tiff";  
        } else if (returnFileName.equals("wbmp")){  
            contentType = "image/vnd.wap.wbmp";  
        } else if (returnFileName.equals("ico")){  
            contentType = "image/x-icon";  
        } else if (returnFileName.equals("jng")){  
            contentType = "image/x-jng";  
        } else if (returnFileName.equals("bmp")){  
            contentType = "image/x-ms-bmp";  
        } else if (returnFileName.equals("svg")){  
            contentType = "image/svg+xml";  
        } else if (returnFileName.equals("jar") || returnFileName.equals("var") || returnFileName.equals("ear")){  
            contentType = "application/java-archive";  
        } else if (returnFileName.equals("doc")){  
            contentType = "application/msword";  
        } else if (returnFileName.equals("pdf")){  
            contentType = "application/pdf";  
        } else if (returnFileName.equals("rtf")){  
            contentType = "application/rtf";  
        } else if (returnFileName.equals("xls")){  
            contentType = "application/vnd.ms-excel";   
        } else if (returnFileName.equals("ppt")){  
            contentType = "application/vnd.ms-powerpoint";  
        } else if (returnFileName.equals("7z")){  
            contentType = "application/x-7z-compressed";  
        } else if (returnFileName.equals("rar")){  
            contentType = "application/x-rar-compressed";  
        } else if (returnFileName.equals("swf")){  
            contentType = "application/x-shockwave-flash";  
        } else if (returnFileName.equals("rpm")){  
            contentType = "application/x-redhat-package-manager";  
        } else if (returnFileName.equals("der") || returnFileName.equals("pem") || returnFileName.equals("crt")){  
            contentType = "application/x-x509-ca-cert";  
        } else if (returnFileName.equals("xhtml")){  
            contentType = "application/xhtml+xml";  
        } else if (returnFileName.equals("zip")){  
            contentType = "application/zip";  
        } else if (returnFileName.equals("mid") || returnFileName.equals("midi") || returnFileName.equals("kar")){ 
            contentType = "audio/midi";  
        } else if (returnFileName.equals("mp3")){  
            contentType = "audio/mpeg";  
        } else if (returnFileName.equals("ogg")){  
            contentType = "audio/ogg";  
        } else if (returnFileName.equals("m4a")){  
            contentType = "audio/x-m4a";  
        } else if (returnFileName.equals("ra")){  
            contentType = "audio/x-realaudio";  
        } else if (returnFileName.equals("3gpp") || returnFileName.equals("3gp")){  
            contentType = "video/3gpp";  
        } else if (returnFileName.equals("mp4") ){  
            contentType = "video/mp4";  
        } else if (returnFileName.equals("mpeg") || returnFileName.equals("mpg") ){  
            contentType = "video/mpeg";  
        } else if (returnFileName.equals("mov")){  
            contentType = "video/quicktime";  
        } else if (returnFileName.equals("flv")){  
            contentType = "video/x-flv";  
        } else if (returnFileName.equals("m4v")){  
            contentType = "video/x-m4v";  
        } else if (returnFileName.equals("mng")){  
            contentType = "video/x-mng";  
        } else if (returnFileName.equals("asx") || returnFileName.equals("asf")){  
            contentType = "video/x-ms-asf";  
        } else if (returnFileName.equals("wmv")){  
            contentType = "video/x-ms-wmv";  
        } else if (returnFileName.equals("avi")){  
            contentType = "video/x-msvideo";  
        }  else if(returnFileName.equals("docx")){
        	contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
		}
          
        return contentType;  
    }  
	/**
	 * 将毫秒值格式化为 rfc1123定义的日期格式
	 * @param modified
	 * @return
	 */
	private static String toRfc1123(long modified) {
		String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT,
				Locale.ENGLISH);
		return sdf.format(Long.valueOf(modified));
	}
	

	/**
	 * 解析 http 请求头中的参数，将文件写出到 http 输出流
	 * @param file 写出到http的文件
	 * @param filename 文件名
	 * @param request http请求
	 * @param response http响应
	 * @throws IOException
	 */
	public static void outputWithRange(File file, String filename, long rangeByteStart, long rangeByteEnd, HttpServletRequest request,
			HttpServletResponse response, int isInline) throws IOException{
		
		if(!file.isFile())
			throw new FileNotFoundException();
		
		byte[] keys = Crypt.encryptKey(file);
		
		long contentLength = file.length();
		int enLen = 0;
		if (keys != null) {
			contentLength = Crypt.realLength(file);
			enLen = Crypt.EncryptLen;
		}
				
		long byteStart = 0, byteEnd = contentLength;
		if ((rangeByteStart >= 0L)
				&& (rangeByteStart < contentLength)) {
			byteStart = rangeByteStart;
		}

		if ((rangeByteEnd >= 0L) && (rangeByteEnd < contentLength)) {
			byteEnd = rangeByteEnd + 1;
		}

		long total = byteEnd - byteStart;
		
		response.reset();
		response.setHeader("Content-Length", "" + total);
		response.addHeader("Accept-Ranges", "bytes");
		response.addHeader("Last-Modified", toRfc1123(file.lastModified()));
		
		if (total == contentLength) {
			response.setStatus(HttpServletResponse.SC_OK);
		} else {
			response.addHeader("Content-Range", "bytes " + byteStart + "-" + (byteEnd - 1)
					+ "/" + contentLength);

			response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
		}
		String contentType = setContentType(filename);
		response.setContentType(contentType+"; charset=utf-8");
		
		//response.setContentType("application/octet-stream");
		if(filename != null){
			String toFileName = filename;
			try {
				toFileName = FileNameEncoder.encode(filename, request.getHeader("User-agent"));
			} catch (Exception e) {
				toFileName=" filename=" + new String(filename.getBytes("gbk"), "iso-8859-1");
			}
			if (isInline == 1) {
				response.setHeader("Content-Disposition",
		                "inline; " + toFileName);
			} else {
				response.setHeader("Content-Disposition",
		                "attachment; " + toFileName);
			}
		}
		/*String contentType = file.getContentType();
		if (contentType != null) {
			response.setContentType(contentType);
		}*/
		InputStream in = new BufferedInputStream(new FileInputStream(file));
		OutputStream out = response.getOutputStream();
		
		
		//InputStream in = new ByteArrayInputStream(data); 
		in.skip(byteStart+enLen);
		byte[] buffer = new byte[1024*1024];
		int read = -1;
		try{
			while((read = in.read(buffer)) > 0){
				if(keys != null) {
					long start = byteStart%(long)enLen;
					Crypt.en(buffer, keys, (int)start);
				}	
				if(total < read){
					out.write(buffer, 0, (int)total);
					break;
				}else{
					out.write(buffer, 0, read);
					total -= read;
				}
			}			
		}catch(Exception e){
			Throwable te = e.getCause();
			if(!(te instanceof SocketException))
				if(e instanceof IOException)
					throw (IOException)e;
				else
					throw new IOException(e.getLocalizedMessage());
		}finally{
			try{
				in.close();
			}catch(Exception fe){
				//IGNORE
			}
			try{
	            out.flush();
	            out.close();
			}catch(Exception fe){
				//IGNORE
			}
		}
	}

}
