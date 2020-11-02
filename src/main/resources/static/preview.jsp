<%@ page contentType="text/html;  charset=utf8" language="java"
 import="java.net.*,java.io.*,
org.springframework.web.bind.ServletRequestUtils,com.dcampus.groups.service.*,
com.dcampus.groups.manager.IGroupManager,
com.dcampus.groups.beans.IGroupResourceBean,
com.dcampus.groups.exception.GroupsException,
com.dcampus.groups.config.PropertyUtil,org.apache.commons.io.FilenameUtils "%>
<%@ page import="com.dcampus.sys.service.UserService" %><%
	Object o = session.getAttribute(ServiceManager.SERVICE_KEY);
	if(o == null) {
		response.sendRedirect(request.getContextPath()+"/pages/c/login.jsp");
		return;
	}
	IService service = (UserService)o;
	long resourceId = ServletRequestUtils.getLongParameter(request, "resourceId", -1L);
	IGroupManager groupManager = service.getGroupManager();
	IGroupResourceBean resourceBean = null;
	try {
		resourceBean = groupManager.getResource(resourceId);
	} catch (Exception e) {
		out.println("文件不存在");
		return;
	}
	FileInputStream inputStream = null;
	BufferedInputStream bis = null;
	OutputStream fos = null;
	try {
		String uriPath = getFileFullPath(resourceBean);
		File file = new File(new URI(uriPath));
		
		String ext = FilenameUtils.getExtension(resourceBean.getFilePath()).toLowerCase();
		String name = "online."+ext;
		
		inputStream = new FileInputStream(file);
		
		bis = new BufferedInputStream(inputStream);
		response.reset();
		String contentType = getMimeType(ext);
		response.setContentType(contentType);
		
		response.setHeader("Content-Disposition","inline; filename="+name);//online open online用于页面展示
		//response.setHeader("Content-Disposition","attachment;filename=test.pdf");//download   attachment下载
		fos = response.getOutputStream();
		
		byte[] b = new byte[2048];
		int i = 0;
		while ((i = bis.read(b)) != -1) {
			fos.write(b, 0, i);
		}
		fos.flush();
		out.clear();
		out = pageContext.pushBody(); 
	}catch (Exception e) {
		
	}finally {
		if (bis != null)
			bis.close();
		if (inputStream != null)
			inputStream.close();
		if (fos != null)
			fos.close();
	}
	
	
%><%!
	private String getFileFullPath(IGroupResourceBean resourceBean) throws GroupsException {
		long memberId = resourceBean.getMemberId();
		String fileName = resourceBean.getFilePath();
		StringBuffer buffer = new StringBuffer();
	
		String path = PropertyUtil.getGroupResourceRootPath();
	
		String subPath = new File(path).toURI().toString();
		buffer.append(subPath);
		if (!subPath.endsWith(File.separator))
			buffer.append(File.separator);
		buffer.append(memberId).append(File.separator);
		buffer.append(fileName);
		return buffer.toString();
	}
	private String getMimeType(String ext) {
		String type = "application/octet-stream";
		if("ppt".equals(ext) || "pptx".equals(ext)){
			type = "application/vnd.ms-powerpoint";
		} else if("doc".equals(ext) || "docx".equals(ext)){
			type = "application/msword";
		} else if("pdf".equals(ext)){
			type = "application/pdf";
		} else if("txt".equals(ext)){
			type = "text/plain";
		} else if("xls".equals(ext)|| "xlsx".equals(ext)){
			type = "application/vnd.ms-excel";
		} else if("jpg".equals(ext)|| "jpeg".equals(ext)){
			type = "image/jpeg";
		} else if("gif".equals(ext)){
			type = "image/gif";
		} else if("png".equals(ext)){
			type = "image/png";
		} else if("bmp".equals(ext)){
			type = "application/x-bmp";
		} 
		return type;
	}
%>