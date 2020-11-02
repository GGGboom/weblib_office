package com.dcampus.weblib.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

import com.dcampus.common.util.Crypt;
import com.dcampus.common.util.Log;

/**
 * zip文件工具类
 *
 * @author zim
 *
 */
public class ZipFile {
	private static Log log = Log.getLog(ZipFile.class);

	private ZipOutputStream zipos;

	/**
	 * 构造器，往输出流中构建zip文件
	 *
	 * @param os
	 *            输出流
	 * @throws FileNotFoundException
	 */
	public ZipFile(OutputStream os) throws FileNotFoundException {
		zipos = new ZipOutputStream(os);
//		zipos.setEncoding("gbk");
		zipos.setEncoding("utf-8");
		zipos.setLevel(0);
	}

	public ZipFile(File zipFile) throws FileNotFoundException {
		this(new BufferedOutputStream(new FileOutputStream(zipFile)));
	}

	/**
	 * zip文件中添加文件夹
	 *
	 * @param dirname
	 *            文件夹名字
	 * @throws IOException
	 */
	public void addDirectory(String dirname) throws IOException {
		if (!dirname.endsWith("/"))
			dirname += "/";

		zipos.putNextEntry(new ZipEntry(dirname));
		zipos.closeEntry();
	}

	public void zipFile(File file, String fullPath)throws IOException{
		if(fullPath==null || file==null || !file.exists())
			throw new FileNotFoundException();
		//byte[] dataBuf = new byte[16384];
		FileInputStream fin = null;
		try {
			ZipEntry tempEntry = new ZipEntry(fullPath);
			zipos.putNextEntry(tempEntry);

			fin = new FileInputStream(file);
			Crypt.fileDecrypt(fin, zipos);
			
			/*
			BufferedInputStream bin = new BufferedInputStream(fin);
			int readCount = -1;
			while((readCount = bin.read(dataBuf))!=-1){
				zipos.write(dataBuf, 0, readCount);
			}*/
			zipos.closeEntry();
			//zipos.flush();
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			if(fin!=null)fin.close();
		}
	}
	/**
	 * 关闭zip文件写入流
	 */
	public void close() {
		if (zipos != null) {
			try {
				zipos.close();
			} catch (Exception e) {
				log.error(e, e);
			}
		}
	}

}
