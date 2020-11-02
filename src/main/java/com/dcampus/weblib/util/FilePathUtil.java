package com.dcampus.weblib.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.dcampus.common.config.PropertyUtil;
import com.dcampus.common.config.ResourceProperty;
import com.dcampus.common.util.Log;
import com.dcampus.weblib.entity.Domain;
import com.dcampus.weblib.entity.GroupResource;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.service.DomainService;

@Service
@Lazy(false)
public class FilePathUtil implements ApplicationContextAware{
	
	private static Log logger = Log.getLog(FilePathUtil.class);
	@Override
	public void setApplicationContext(ApplicationContext applicationContext){
		domainService = (DomainService) applicationContext.getBean("domainService");
	}

	private static DomainService domainService;
	/**
	 * 获取文件的绝对路径，这里返回的绝对路径必须是一个URI地址
	 * @param resourceBean
	 * @return
	 * @throws GroupsException
	 */
	public static String getFileFullPath(GroupResource resourceBean) throws GroupsException {
		
		Domain domain = domainService.isDomainGroup(resourceBean.getGroup().getId());
		if(domain==null) {
			//个人柜子内，上传到域空间的资源路径
			long domainId = resourceBean.getDomainTag();
			if(domainId!=0) {
				domain = domainService.getDomainById(domainId);
			}
		}
		if(domain!=null) {
			String relativePath = domain.getDomainCategory().getRelativePath();
			return getFileDomainFullPath(resourceBean.getCreatorId(), resourceBean.getFilePath(),relativePath);
		}
		return getFileFullPath(resourceBean.getCreatorId(), resourceBean.getFilePath());
	}
	
	/**
	 * 获取资源域的完整路径
	 * @param memberId
	 * @param fileName
	 * @param relativePath
	 * @return
	 */
	public static String getFileDomainFullPath(long memberId, String fileName,String relativePath) {
		StringBuffer buffer = new StringBuffer();

		String path = PropertyUtil.getDomainResourceRootPath();

		// 非绝对路径，需加上classpath
		if (!isAbsolute(path)) {
			buffer.append(getRootPath()).append(path);
			// if (!path.endsWith(File.separator))
			// buffer.append(File.separator);
			if (!path.endsWith("/"))
				buffer.append("/");
		} else {
			String subPath = new File(path).toURI().toString();
			buffer.append(subPath);
			if (!subPath.endsWith("/")) // toURI()之后已是正斜杠，因此此处从File.separator改为“/”，FZD
										// 20140812
				buffer.append("/");
		}
		buffer.append(relativePath).append("/");
		buffer.append(memberId).append("/");

		// 创建用户文件夹路径
		createDir(buffer.toString());

		buffer.append(fileName);

		return buffer.toString();
	}
	
	/**
	 * 获取文件的绝对路径，这里返回的绝对路径必须是一个URI地址
	 * @param memberId
	 * @param fileName
	 * @return
	 * @throws GroupsException
	 */
	public static String getFileFullPath(long memberId, String fileName) throws GroupsException {
		StringBuffer buffer = new StringBuffer();

		String path = PropertyUtil.getGroupResourceRootPath();

		// 非绝对路径，需加上classpath
		if (!isAbsolute(path)) {
			buffer.append(FilePathUtil.getRootPath()).append(path);
			// if (!path.endsWith(File.separator))
			// buffer.append(File.separator);
			if (!path.endsWith("/")){
				buffer.append("/");
			}
		} else {
			String subPath = new File(path).toURI().toString();
			buffer.append(subPath);
			if (!subPath.endsWith("/")) {
				buffer.append("/");
			}
		}
		buffer.append(memberId).append("/");

		// 创建用户文件夹路径
		FilePathUtil.createDir(buffer.toString());

		buffer.append(fileName);

		return buffer.toString();
	}
	
	/**
	 * 创建并返回文件断点续传的临时路径，只是URI文件目录 2017/4/19 guochao
	 *
	 * @param resourceBean
	 * @return
	 * @throws GroupsException
	 */
	public static String getFileTempPath(GroupResource resourceBean) throws GroupsException {
		return getFileTempPath(resourceBean.getCreatorId(), resourceBean.getFilePath(), resourceBean.getId());
	}
	
	public static String getFileTempPath(long memberId, String fileName, long resourceId) throws GroupsException {
		StringBuffer buffer = new StringBuffer();

		String path = PropertyUtil.getMtuploadResourceRootPath();

		// 非绝对路径，需加上classpath
		if (!isAbsolute(path)) {
			buffer.append(getRootPath()).append(path);
			// if (!path.endsWith(File.separator))
			// buffer.append(File.separator);
			if (!path.endsWith("/"))
				buffer.append("/");
		} else {
			String subPath = new File(path).toURI().toString();
			buffer.append(subPath);
			if (!subPath.endsWith("/")) // toURI()之后已是正斜杠，因此此处从File.separator改为“/”，FZD
										// 20140812
				buffer.append("/");
		}
		buffer.append(memberId).append("/");
		buffer.append(resourceId).append("/");
		try {
			File file = new File(new URI(buffer.toString()));
			// 如果文件夹不存在则创建
			if (!file.exists()) {
				file.mkdirs();
			}
		} catch (URISyntaxException e) {
			logger.error(e.getStackTrace());
		}

		return buffer.toString();
	}

	/**
	 * 是否绝对路径
	 * @param path
	 * @return
	 */
	public static boolean isAbsolute(String path) {
		File file = new File(path);
		return file.isAbsolute();
	}
	/**
	 * 获取classPath的uri地址
	 *
	 * @return
	 */
	public static String getRootPath() {
		return FileUtil.class.getClassLoader().getResource("").toString();
	}
	
	/**
	 * 传入的路径必须是一个uri路径
	 *
	 * @param uriPath
	 * @throws GroupsException
	 */
	public static void createDir(String uriPath) throws GroupsException {
		try {
			File file = new File(new URI(uriPath));
			if (!(file.exists() || file.mkdirs())) {
				throw new GroupsException(
						ResourceProperty.getCannotUploadFileString());
			}
		} catch (URISyntaxException e) {
			throw new GroupsException(e);
		}
	}
}
