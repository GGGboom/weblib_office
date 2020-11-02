package com.dcampus.weblib.util;

import java.text.DecimalFormat;

public class FileSizeConversion {

	public static long KB_SIZE = 1024;
	
	public static long MB_SIZE = KB_SIZE * 1024;
	
	public static long GB_SIZE = MB_SIZE * 1024;
   
	public static String conversion(long size) {
		DecimalFormat df = new DecimalFormat("###.##");
		float f;
		if (size < KB_SIZE) {
			return size + "KB";
		} else if (size < MB_SIZE) {
			f = (float) ((float) size / (float) KB_SIZE);
		    return (df.format(new Float(f).doubleValue())+"MB");
		} else {
			f = (float) ((float) size / (float) MB_SIZE);
		    return (df.format(new Float(f).doubleValue())+"GB");
		}
	}
	
	public static void main(String[] args) {
		System.out.println(FileSizeConversion.conversion(99900000000L));
	}
}