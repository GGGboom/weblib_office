package com.dcampus.weblib.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import javax.imageio.*;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;


public class Thumbnail {
	
	private static final long MAX_LEN = 4*1024*1024;
	private String srcFile;
	private String destFile;
	private int width;
	private int height;
	private long length;
	private Image img;

	public static void main(String[] args) throws Exception {
		System.out.println(1024*1024);
		String src = "D:/ccnl001.jpg";
		//C:\Documents and Settings\scut\My Documents\My Pictures
		String path = "D:/data/";
		String despath = "D:/Jccnl001_thum.jpg";
		File srcs = new File(src);
		//srcs.get
		
		int dot = srcs.getName().lastIndexOf('.');
		if (dot > 0) {
			System.out.println(srcs.getName().substring(dot));
		}
		System.out.println(srcs.exists());
		File fpath = new File(path);
		System.out.println(fpath.exists());
		File despaths = new File(despath);
		try {
		Thumbnail thum = new Thumbnail(srcs, path, "a32_temp.jpg");
		//System.out.println(thum.resizeByQuality(50));
		//System.out.println(thum.resizeByMaxStandard(200, 200,
				//5000000, PropertyUtil.getThumbnailProportion(), 100));

		thum.resizeAndCut_newnew(256, 256, 5242880, 100);
		} catch (Exception e) {
			System.out.println("-----------------------");
		}
		
		//thum.resizeFix(1024, 768, 524288, 50);
	}

	public static boolean isOverLimit(long size){
		if (size > MAX_LEN) {
			return true;
		}
		return false;
	}

	public Thumbnail(File file, String destFilePath, String destFileName) throws IOException {
		this.srcFile = file.getName();
		this.length = file.length();
		this.destFile = destFilePath + File.separator + destFileName;
		
		System.out.println("Thumbnail path : " + this.destFile);
		/*
		this.destFile = this.srcFile
				.substring(0, this.srcFile.lastIndexOf("."))
				+ "_s.jpg";*/
		/*
		byte[] buf = Crypt.fileDecrypt(file);
		InputStream sbs = new ByteArrayInputStream(buf); 
		img = javax.imageio.ImageIO.read(sbs); // 构造Image对象*/
		img = javax.imageio.ImageIO.read(file); // 构造Image对象
		width = img.getWidth(null); // 得到源图宽
		height = img.getHeight(null); // 得到源图长
	}

	/**
	 * 强制压缩/放大图片到固定的大小
	 *
	 * @param w
	 *            int 新宽度
	 * @param h
	 *            int 新高度
	 * @param quality
	 * 			  int 质量(0-100)
	 * @return 图片地址
	 * @throws IOException
	 */
	public String resize(int w, int h, int quality) throws IOException {
		BufferedImage _image = new BufferedImage(w, h,
				BufferedImage.TYPE_INT_RGB);
		_image.getGraphics().drawImage(img, 0, 0, w, h, null); // 绘制缩小后的图
		File file = new File(destFile);
//		FileOutputStream out = new FileOutputStream(file); // 输出到文件流
//		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
//
//		JPEGEncodeParam param = encoder
//
//		.getDefaultJPEGEncodeParam(_image);
//
//		quality = Math.max(0, Math.min(quality, 100));
//
//		param.setQuality((float) quality / 100.0f, false);
//
//		encoder.setJPEGEncodeParam(param);
//
//		encoder.encode(_image);// 近JPEG编码
//
//		out.close();
		
		//Crypt.fileEncrypt(file, file);
		
		return file.getName();
	}

	public String resizeByQuality(int quality) throws IOException {
		return resize(width, height, quality);
	}

	/**
	 * 按照固定的比例缩放图片
	 *
	 * @param t
	 *            double 比例
	 * @param quality
	 * 			  int 质量(0-100)
	 * @return 图片地址
	 * @throws IOException
	 */
	public String resize(double t, int quality) throws IOException {
		int w = (int) (width * t);
		int h = (int) (height * t);
		return resize(w, h, quality);
	}

	/**
	 * 以宽度为基准，等比例放缩图片
	 *
	 * @param w
	 *            int 新宽度
	 * @param quality
	 * 			  int 质量(0-100)
	 * @return 图片地址
	 * @throws IOException
	 */
	public String resizeByWidth(int w, int quality) throws IOException {
		int h = (int) (height * w / width);
		return resize(w, h, quality);
	}

	/**
	 * 以高度为基准，等比例缩放图片
	 *
	 * @param h
	 *            int 新高度
	 * @param quality
	 * 			  int 质量(0-100)
	 * @return 图片地址
	 * @throws IOException
	 */
	public String resizeByHeight(int h, int quality) throws IOException {
		int w = (int) (width * h / height);
		return resize(w, h, quality);
	}

	/**
	 * 按照最大高度限制，生成最大的等比例缩略图
	 *
	 * @param w
	 *            int 最大宽度
	 * @param h
	 *            int 最大高度
	 * @param quality
	 * 			  int 质量(0-100)
	 * @return 图片地址
	 * @throws IOException
	 */
	public String resizeFix(int w, int h, int quality) throws IOException {
		if (width / height > w / h) {
			return resizeByWidth(w, quality);
		} else {
			return resizeByHeight(h, quality);
		}
	}
	/**
	 * 根据最大标准进行压缩，如果文件大小或宽长超过标准，则进行压缩
	 * @param maxWith 最大宽度
	 * @param maxHeight 最大长度
	 * @param maxLength 最大大小
	 * @param percent 压缩比例
	 * @param quality
	 * 			  int 压缩质量(0-100)
	 * @return 图片地址
	 * @throws IOException
	 */
	public String resizeByMaxStandard(int maxWith, int maxHeight, long maxLength, double percent, int quality) throws IOException {
		if (this.length > maxLength) {
			if (this.height > maxHeight || this.width > maxWith) {
				return resize(percent, quality);
			} else {
				return resizeByQuality(quality);
			}
		} else {
			if (this.height > maxHeight || this.width > maxWith) {
				return resize(percent, quality);
			} else {
				return null;
			}
		}
	}
	public String resizeFix(int maxWith, int maxHeight, long maxLength, int quality) throws IOException {
		if (this.length > maxLength) {
			if (this.height > maxHeight || this.width > maxWith) {
				return resizeFix(maxWith, maxHeight, quality);
			} else {
				return resizeByQuality(quality);
			}
		} else {
			if (this.height > maxHeight || this.width > maxWith) {
				return resizeFix(maxWith, maxHeight, 100);
			} else {
				return null;
			}
		}
	}
	//按等宽、等高中的较大比例缩小之后剪切
	public void resizeAndCut(int maxWidth, int maxHeight, int maxLength) throws IOException{
		int destW = this.width;
		int destH = this.height;
		BufferedImage _image = (BufferedImage) img;
		BufferedImage bi = null;

		javax.imageio.stream.ImageInputStream iis = null;
		ByteArrayOutputStream os = null;
		InputStream is = null;
		if(destW <= maxWidth && destH <= maxHeight){
			if(this.length > maxLength){
				float quality = (float)maxLength/this.length;
				//降低质量
				bi = resetQuality(_image,quality);
			}else{
				bi = _image;
			}
		}else{
			if(destW > maxWidth && destH > maxHeight){
				//按等宽、等高中的较大比例缩小
				double scaleW = (double)this.width/maxWidth;
				double scaleH = (double)this.height/maxHeight;
				double scale = scaleW;
				if(scaleW > scaleH){
					scale = scaleH;
				}
				destW = (int)(this.width/scale);
				destH = (int)(this.height/scale);
				_image = new BufferedImage(destW, destH,BufferedImage.TYPE_INT_RGB);
				_image.getGraphics().drawImage(img, 0, 0, destW, destH, null); // 绘制缩小后的图
			}

			//计算压缩后的大小
			os = new ByteArrayOutputStream();
			ImageIO.write(_image, "jpg", os);
			int _imageSize = os.size();

			if(destW > maxWidth || destH > maxHeight){
				//剪切
				is = new ByteArrayInputStream(os.toByteArray());
				iis = javax.imageio.ImageIO.createImageInputStream(is);
				Iterator<javax.imageio.ImageReader> it = javax.imageio.ImageIO.getImageReadersByFormatName("jpg");
				javax.imageio.ImageReader reader = it.next();

				reader.setInput(iis,true) ;
				javax.imageio.ImageReadParam param = reader.getDefaultReadParam();

				int x = 0;
				int y = 0;
				if(destW > maxWidth){
					x = destW/2- maxWidth/2;
					destW = maxWidth;
				}
				if(destH > maxHeight){
					y = destH/2 - maxHeight/2;
					destH = maxHeight;
				}
				Rectangle rect = new Rectangle(x,y,destW,destH);
				param.setSourceRegion(rect);

				bi = reader.read(0,param);

				//计算剪切后的大小
				float cutSacle = (float)(destW*destH)/(_image.getWidth()*_image.getHeight());
				_imageSize = (int)(cutSacle*_imageSize);

			}else{
				bi = _image;
			}

			if(_imageSize > maxLength){
				//降低质量
				float quality = (float)maxLength/_imageSize;
				bi = resetQuality(_image,quality);
			}
		}
		String suffix = "jpg";
		int dot = srcFile.lastIndexOf('.');
		if (dot > 0) {
			suffix = srcFile.substring(dot+1);
		}
		File file = new File(destFile);
		javax.imageio.ImageIO.write(bi, suffix, file);
		if(os != null){
			os.close();
		}
		if(is != null){
			is.close();
		}
		if(iis != null){
			iis.close();
		}
		//Crypt.fileEncrypt(file, file);
	}
	//生成缩略图并写到磁盘
	public void resizeAndCut_new(int Width, int Height, int maxLength, int quality) throws IOException{
		int destW = this.width;
		int destH = this.height;
		BufferedImage _image = (BufferedImage) img;
		BufferedImage bi = null;
		javax.imageio.stream.ImageInputStream iis = null;
		ByteArrayOutputStream os = null;
		InputStream is = null;
		double scaleW = (double)this.width/Width;
		double scaleH = (double)this.height/Height;
		double scale = scaleW;
		if(scaleW > scaleH){
			scale = scaleH;
		}
		destW = (int)(this.width/scale);
		destH = (int)(this.height/scale);
		_image = new BufferedImage(destW, destH,BufferedImage.TYPE_INT_RGB);
		_image.getGraphics().drawImage(img.getScaledInstance(destW, destH, Image.SCALE_SMOOTH), 0, 0, null); // 绘制缩小后的图\
		os = new ByteArrayOutputStream();
		try {
			ImageIO.write(_image, "jpg", os);
		} catch (Exception e)
		{
			System.out.println(e.toString());
		}
		int _imageSize = os.size();
		
		if(destW > Width || destH > Height){
			
			is = new ByteArrayInputStream(os.toByteArray());
			iis = javax.imageio.ImageIO.createImageInputStream(is);
			Iterator<javax.imageio.ImageReader> it = javax.imageio.ImageIO.getImageReadersByFormatName("jpg");
			javax.imageio.ImageReader reader = it.next();
			reader.setInput(iis,true) ;
			javax.imageio.ImageReadParam param = reader.getDefaultReadParam();
			int x = 0;
			int y = 0;
			if(destW > Width){
				x = destW/2- Width/2;
				destW = Width;
			}
			if(destH > Height){
				y = destH/2 - Height/2;
				destH = Height;
			}
			Rectangle rect = new Rectangle(x,y,destW,destH);
			param.setSourceRegion(rect);
			bi = reader.read(0,param);
			float cutSacle = (float)(destW*destH)/(_image.getWidth()*_image.getHeight());
			_imageSize = (int)(cutSacle*_imageSize);
		}else{
			bi = _image;
		}
		bi = resetQuality(bi,quality/(float)100);
		/*if(_imageSize > maxLength){
			float quality_max = (float)maxLength/_imageSize;
			bi = resetQuality(_image,quality_max);
		}*/
		String suffix = "jpg";
		int dot = srcFile.lastIndexOf('.');
		if (dot > 0) {
			suffix = srcFile.substring(dot+1);
		}
		File destFiles = new File(destFile);
		if (!destFiles.exists())
			destFiles.mkdirs();
		try{
		    javax.imageio.ImageIO.write(bi, suffix, destFiles);
		}catch (Exception e){
		    e.printStackTrace();
        }

		if(os != null){
			os.close();
		}
		if(is != null){
			is.close();
		}
		if(iis != null){
			iis.close();
		}
		//Crypt.fileEncrypt(destFiles, destFiles);
	}
	
	public void resizeAndCut_newnew(int Width, int Height, int maxLength, int quality) throws IOException{
		//调换了压缩和切割的前后顺序
		
		int destW = this.width;
		int destH = this.height;
		BufferedImage _image = (BufferedImage) img;
		BufferedImage bi = null;
		javax.imageio.stream.ImageInputStream iis = null;
		ByteArrayOutputStream os = null;
		InputStream is = null;
		double scaleW = (double)this.width/Width;
		double scaleH = (double)this.height/Height;
		double scale = scaleW;
		if(scaleW > scaleH){
			scale = scaleH;
		}
		
		if(destW>Width*scale||destH>Height*scale){
			//先剪切成目标比例
			int _destW = destW;
			int _destH = destH;
			os = new ByteArrayOutputStream();
			try {
				ImageIO.write(_image, "jpg", os);
			} catch (Exception e)
			{
				System.out.println(e.toString());
			}
			is = new ByteArrayInputStream(os.toByteArray());
			iis = javax.imageio.ImageIO.createImageInputStream(is);
			Iterator<javax.imageio.ImageReader> it = javax.imageio.ImageIO.getImageReadersByFormatName("jpg");
			javax.imageio.ImageReader reader = it.next();
			reader.setInput(iis,true) ;
			javax.imageio.ImageReadParam param = reader.getDefaultReadParam();
			int x = 0;
			int y = 0;
			if(_destW > Width*scale){
				x = (int)(_destW/2- Width*scale/2);
				_destW =(int)( Width*scale);
			}
			if(_destH > Height*scale){
				y = (int)(_destH/2 - Height*scale/2);
				_destH = (int)(Height*scale);
			}
			Rectangle rect = new Rectangle(x,y,_destW,_destH);
			param.setSourceRegion(rect);
			bi = reader.read(0,param);
			//float cutSacle = (float)(_destW*_destH)/(_image.getWidth()*_image.getHeight());
			os = new ByteArrayOutputStream();
			try {
				ImageIO.write(bi, "jpg", os);
			} catch (Exception e)
			{
				System.out.println(e.toString());
			}
			is = new ByteArrayInputStream(os.toByteArray());
			img = ImageIO.read(is);
			this.width = _destW;
			this.height = _destH;
		}
		
		
		destW = (int)(this.width/scale);
		destH = (int)(this.height/scale);
		_image = new BufferedImage(destW, destH,BufferedImage.TYPE_INT_RGB);
		_image.getGraphics().drawImage(img, 0, 0,destW, destH, null); // 绘制缩小后的图\
//		
		bi = _image;
		bi = resetQuality(bi,quality/(float)100);
		/*if(_imageSize > maxLength){
			float quality_max = (float)maxLength/_imageSize;
			bi = resetQuality(_image,quality_max);
		}*/
		String suffix = "jpg";
		int dot = srcFile.lastIndexOf('.');
		if (dot > 0) {
			suffix = srcFile.substring(dot+1);
		}
		File destFiles = new File(destFile);
		if (!destFiles.exists())
			destFiles.mkdirs();
		try {
			javax.imageio.ImageIO.write(bi, suffix, destFiles);
		}
		catch (Exception e){
			e.printStackTrace();
		}
		if(os != null){
			os.close();
		}
		if(is != null){
			is.close();
		}
		if(iis != null){
			iis.close();
		}
		//Crypt.fileEncrypt(destFiles, destFiles);
	}
	
	

	private BufferedImage resetQuality(BufferedImage image, float quality) throws IOException{
		ByteArrayOutputStream out = new ByteArrayOutputStream(); // 输出流
//		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
//
//		JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(image);
//
//		quality = Math.max(0, Math.min(quality, 1));
//		param.setQuality(quality, false);
//
//		encoder.setJPEGEncodeParam(param);
//		encoder.encode(image);// 近JPEG编码

		ByteArrayInputStream input = new ByteArrayInputStream(out.toByteArray());
        BufferedImage bi = null;
		try {
            bi = javax.imageio.ImageIO.read(input);
        }catch (Exception e){
		    e.printStackTrace();
        }
		if(out != null){
			out.close();
		}
		if(input != null){
			input.close();
		}
		return image;
	}

	/**
	 * 获取图片原始宽度 getSrcWidth
	 */
	public int getSrcWidth() {
		return width;
	}

	/**
	 * 获取图片原始高度 getSrcHeight
	 */
	public int getSrcHeight() {
		return height;
	}

}
