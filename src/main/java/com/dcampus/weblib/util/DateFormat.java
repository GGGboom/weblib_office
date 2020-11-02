package com.dcampus.weblib.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 进行日期格式化的工具类
 *
 * @author zim
 *
 */
public class DateFormat {
	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	private static SimpleDateFormat dayFormat = new SimpleDateFormat(
			"yyyy-MM-dd");

	public static SimpleDateFormat dateTimeFullFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss SSS");
	
	/**
	 * 时间格式化为以秒为最小单位 yyyy-MM-dd hh:mm:ss
	 *
	 * @param date
	 * @return
	 */
	public static String format(Timestamp date) {
		if (date == null)
			return "";

		return dateFormat.format(date);
	}

	/**
	 * 时间格式化为以秒为最小单位 yyyy-MM-dd HH:mm:ss
	 *
	 * @param date
	 * @return
	 */
	public static String format(Date date) {
		if (date == null)
			return "";

		return dateFormat.format(date);
	}

	/**
	 * 时间格式化为以天为最小单位 yyyy-MM-dd
	 *
	 * @param date
	 * @return
	 */
	public static String dayFormat(Timestamp date) {
		if (date == null)
			return "";

		return dayFormat.format(date);
	}

	/**
	 * 时间格式化为以天为最小单位 yyyy-MM-dd
	 *
	 * @param date
	 * @return
	 */
	public static String dayFormat(Date date) {
		if (date == null)
			return "";

		return dayFormat.format(date);
	}

	public static String dayFormat(long time) {
		Timestamp stamp = new Timestamp(time);
		return dayFormat.format(stamp);
	}

	public static long getTimeMillis(String day) {
		if (day == null || day.length() == 0) {
			return 0;
		}
		try {
			return dayFormat.parse(day).getTime();
		} catch (ParseException e) {
		}
		return 0;
	}

	/** 一分钟毫秒数 **/
	private static final long MINUTE = 60L * 1000;

	/** 一小时毫秒数 **/
	private static final long HOUR = 60L * MINUTE;

	/** 一天毫秒数 **/
	private static final long DAY = 24L * HOUR;

	/** 一个月毫秒数 **/
	private static final long MONTH = 30L * DAY;

	/**
	 * 模糊格式化。 时间与当前时间比较： 1、小于1分钟，返回"刚刚" 2、小于60分钟，返回分钟为最小单位，如"2分钟前"
	 * 3、小于24小时，返回小时为最小单位，如"3小时前" 4、小于30天，返回天为最小单位，如"5天前" 5、大于30天，返回"一个月前"
	 *
	 * @param date
	 * @return
	 */
	public static String fuzzyFormat(Timestamp date) {
		long time = System.currentTimeMillis() - date.getTime();
		if (time < MINUTE) {
			return "刚刚";
		}

		if (time < HOUR) {
			long minutes = time / MINUTE;
			return minutes + "分钟前";
		}

		if (time < DAY) {
			long hours = time / HOUR;
			return hours + "小时前";
		}

		if (time < MONTH) {
			long days = time / DAY;
			return days + "天前";
		}

		return "一个月前";
	}
	
	public static String toRfc1123(Date modified) {
		String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT,
				Locale.ENGLISH);
		return sdf.format(modified);
	}
	
	public static String toRfc1123(long modified) {
		String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT,
				Locale.ENGLISH);
		return sdf.format(Long.valueOf(modified));
	}
	
	public static void main(String[] args) {
		System.out.println(DateFormat.toRfc1123(new Date()));
	}
}
