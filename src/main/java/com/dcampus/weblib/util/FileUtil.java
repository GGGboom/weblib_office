package com.dcampus.weblib.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dcampus.common.config.OfficeProperty;
import com.dcampus.weblib.entity.office.FileType;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.dcampus.common.config.PropertyUtil;
import com.dcampus.common.config.ResourceProperty;
import com.dcampus.common.util.Crypt;
import com.dcampus.common.util.Log;
import com.dcampus.weblib.entity.Domain;
import com.dcampus.weblib.entity.GroupResource;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.service.DomainService;

/**
 * 文件上传下载，路径检验，创建目录等工具类
 * @author patrick
 *
 */
public class FileUtil {
	private static Log logger = Log.getLog(FileUtil.class);
	
	/**
	 * 删除文件
	 * @param uriPath
	 * @throws GroupsException
	 */
	public static void deleteFile(String uriPath) throws GroupsException {
		try {
			File file = new File(new URI(uriPath));
			if (file.exists()) {
				if (!file.delete())
					throw new GroupsException(
							ResourceProperty.getCannotDeleteFileString());
			}
		} catch (Exception e) {
			throw new GroupsException(e);
		}
	}
	
	/**
	 * 根据路径删除物理文件
	 * @param path
	 * @throws GroupsException
	 */
	public static void deleteResource(String path) throws GroupsException {
		try {
			File file = new File(new URI(path));
			if (file.isFile()) {
				file.delete();
			} else {
				deleteDirectory(path);
			}
		} catch (URISyntaxException e) {
			throw new GroupsException(e);
		}
	}
	
	private static void deleteDirectory(String dirPath) throws GroupsException {
		try {
			File dir = new File(new URI(dirPath));
			File[] files = dir.listFiles();
			for (File file : files) {
				if (file.isFile()) {
					file.delete();
				} else {
					String path = dirPath + File.separator + file.getName();
					deleteDirectory(path);
				}
			}
		} catch (URISyntaxException e) {
			throw new GroupsException(e);
		}
	}
	
	/**
	 * 保存文件到服务器
	 *
	 * @param file
	 * @param uriPath
	 * @throws GroupsException
	 */
	public static void copyFileToServer(File file, String uriPath,
			boolean ingoreEncrypt) throws GroupsException {
		FileInputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
			inputStream = new FileInputStream(file);
			File targetFile = new File(new URI(uriPath));
			outputStream = new FileOutputStream(targetFile, true);

			if (ingoreEncrypt) {
				IOUtils.copy(inputStream, outputStream);
			} else {
				Crypt.fileEncrypt(inputStream, outputStream);
			}
		} catch (Exception e) {
			if (e instanceof FileNotFoundException)
				throw new GroupsException("找不到待复制文件" );
			else
				throw new GroupsException(e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Exception e) {
					logger.error(e, e);
				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (Exception e) {
					logger.error(e, e);
				}
			}
		}
	}
	
	/**
	 * 保存data到服务器
	 * @param data
	 * @param uriPath
	 * @param ingoreEncrypt
	 * @throws GroupsException
	 */
	public 	static void writeToServer(byte[] data, String uriPath, boolean ingoreEncrypt) throws GroupsException {
		FileOutputStream outputStream = null;
		try {
			File targetFile = new File(new URI(uriPath));
			outputStream = new FileOutputStream(targetFile, true);

			if (ingoreEncrypt)
				IOUtils.write(data, outputStream);
			else {
				Crypt.en(data);
				IOUtils.write(data, outputStream);
			}
		} catch (Exception e) {
			throw new GroupsException(e);
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (Exception e) {
					logger.error(e, e);
				}
			}
		}
	}
	
	/**
	 * 下载文件
	 *
	 * @param uriPath
	 * @param outputStream
	 */
	public static void downloadFile(String uriPath, OutputStream outputStream)
			throws GroupsException {
		FileInputStream inputStream = null;
		try {
			File targetFile = new File(new URI(uriPath));

			inputStream = new FileInputStream(targetFile);
			Crypt.fileDecrypt(inputStream, outputStream);
			// IOUtils.copy(inputStream, outputStream);
			outputStream.flush();
		} catch (Exception e) {
			throw new GroupsException(e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Exception e) {
					logger.error(e, e);
				}
			}
		}
	}
	
	public static String getTempDir() {
		return System.getProperty("java.io.tmpdir");   
	}
	
	public static File getTempDownDir() {
		File tempDir = new File(getTempDir());
		File file = new File(tempDir, "down");
		if (!file.exists()) {
			file.mkdir();
		}
		return file;
	}
	
	public static File createTempDownFile(String fileName) throws IOException {
		File tempDir = getTempDownDir();
		File file = new File(tempDir, fileName);
		if (!file.exists()) {
			file.createNewFile();
		}
		return new File(file.toURI());
	}
	
	public static boolean isTempDownFileExists(String fileName){
		File tempDir = getTempDownDir();
		File file = new File(tempDir, fileName);
		return file.exists();
	}
	
	public static File getTempDownFile(String fileName){
		File tempDir = getTempDownDir();
		File file = new File(tempDir, fileName);
		return new File(file.toURI());
	}
	
	
	/**
	 * 截取content-Range 中的个部分信息，以数组形式返回
	 * @param contentRange
	 * @return
	 */
	public static long[] splitContentRange(String contentRange){
		long rangeByteStart = -1L;
		long rangeByteEnd = -1L;
		long total = -1L;
		String rangeFull = null;
		boolean rf = false;
		if(contentRange == null){
			return null;
		}
		String[] range = contentRange.trim().split("=| ");
		
		if (range.length == 2) {
			rf = true;
			//rangeFull的形式为6777-7888/945996
			rangeFull = range[1].trim();
			range = rangeFull.split("-");
			if (range.length > 0) {

				try {
//					rangeByteStart = Integer.decode(range[0]).intValue();
					rangeByteStart = Long.decode(range[0]).longValue();
				} catch (NumberFormatException ex) {
					
				}
				
				if (range.length > 1) {
					String[] temp = range[1].split("/");
					if(temp.length > 0){
						try {
//							rangeByteEnd = Integer.decode(range[0]).intValue();
							rangeByteEnd = Long.decode(temp[0]).longValue();
						} catch (NumberFormatException ex) {
							
						}
						if(temp.length > 1){
							try {
//								total = Integer.decode(range[1]).intValue();
								total = Long.decode(temp[1]).longValue();
							} catch (NumberFormatException ex) {
								
							}
						}
					}
				}
			}
		}
		return new long[] {rangeByteStart,rangeByteEnd,total};
	}
	
	/**
	 * 返回上传数据块编号
	 * 文件融合时根据顺序融合
	 * @param blockIndex
	 * @return
	 */
	public static String createStrBlockIndex(int blockIndex) {  
	    String strBlockIndex;  
	    if (blockIndex < 10) {  
	        strBlockIndex = "000" + blockIndex;  
	    } else if (10 <= blockIndex && blockIndex < 100) {  
	        strBlockIndex = "00" + blockIndex;  
	    } else if (100 <= blockIndex && blockIndex < 1000) {  
	        strBlockIndex = "0" + blockIndex;  
	    } else {  
	        strBlockIndex = "" + blockIndex;  
	    }  
	    return strBlockIndex;  
	}
	
    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     * @param dir 将要删除的文件目录
     * @return
     */
    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }
    
	/**
	 * 组装文件，并且删除已组装完成的数据块
	 * @param dirPath 待组装文件所在目录
	 * @param assembleFilePath	组装后文件路径
	 */
	public static void assembleFiles( String dirPath, String assembleFilePath) {  
	    // 开始在指定目录组装文件  
	    String uploadedUrl = null;  
	    String[] separatedFiles;  
	    String[][] separatedFilesAndSize;  
	    int fileNum = 0;  
	    File file = new File(dirPath);  
	    separatedFiles = file.list();  
	    separatedFilesAndSize = new String[separatedFiles.length][2];  
	    Arrays.sort(separatedFiles);  
	    fileNum = separatedFiles.length;  
	    for (int i = 0; i < fileNum; i++) {  
	        separatedFilesAndSize[i][0] = separatedFiles[i];  
	        String fileName = dirPath + separatedFiles[i];  
	        File tmpFile = new File(fileName);  
	        long fileSize = tmpFile.length();  
	        separatedFilesAndSize[i][1] = String.valueOf(fileSize);  
	    }  
	  
	    RandomAccessFile fileReader = null;  
	    RandomAccessFile fileWrite = null;  
	    long alreadyWrite = 0;  
	    int len = 0;  
	    byte[] buf = new byte[1024];  
	    try {  
	        uploadedUrl = assembleFilePath;    	
	        fileWrite = new RandomAccessFile(uploadedUrl, "rw");  
	        for (int i = 0; i < fileNum; i++) {  
	            fileWrite.seek(alreadyWrite);  
	            // 读取  
	            fileReader = new RandomAccessFile((dirPath + separatedFilesAndSize[i][0]), "r");  
	            // 写入  
	            while ((len = fileReader.read(buf)) != -1) {  
	                fileWrite.write(buf, 0, len);  
	            }  
	            fileReader.close();  
	            alreadyWrite += Long.parseLong(separatedFilesAndSize[i][1]);  
	        }  
	        fileWrite.close();  
	        //删除待组装目录下所有已经组装的文件
	        if(!deleteDir(new File(dirPath))){;
	        	System.out.println("delete tempDir failed!");
	        	logger.warn("delete tempDir:" + dirPath +"failed!");
	        }
	        
	        //////////////////////////////
	    } catch (IOException e) {  
	        logger.error(e.getMessage(), e);  
	        System.out.println(e.getMessage());
	        try {  
	            if (fileReader != null) {  
	                fileReader.close();  
	            }  
	            if (fileWrite != null) {  
	                fileWrite.close();  
	            }  
	        } catch (IOException ex) {  
	            logger.error(e.getMessage(), e);  
	        }  
	    }  
	  
	} 
	

	
	/**
	 * 上传文件非空验证
	 * @param file
	 * @throws Exception
	 */
    public static void checkEmptyFile(File file) throws Exception {  
        if (file == null || file.getAbsolutePath() == null) {  
        	throw new Exception("there is no file exists!");// 上传文件不存在
        }  
    } 
    
 
	
	/**
	 * 将名字分成两部分，第一部分为文件名，第二部分为后缀名，以.划分。
	 * <p>
	 * 若存在多个.则取最后一部分作为后缀名，前部分作为文件名
	 *
	 * @param name
	 * @return
	 */
	public static String[] splitName(String name) {
		if (name == null)
			return null;

		String[] ts = name.split("\\.");
		if (ts.length == 1)
			return new String[] { ts[0], "" };

		// 拼接第一段
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < ts.length - 1; ++i) {
			if (i != 0)
				b.append(".");
			b.append(ts[i]);
		}

		return new String[] { b.toString(), ts[ts.length - 1] };
	}

}
