package com.dcampus.weblib.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 随机串生成工具类，生成的随机串只包含数字和字母
 *
 * @author zim
 *
 */
public class RandomStringGenerater {
	private static Random random = new Random();

	private static Character[] charSet;

	static {
		List<Character> list = new ArrayList<Character>();
		for (char i = '0'; i <= '9'; ++i) {
			list.add(i);
		}
		for (char i = 'a'; i <= 'z'; ++i) {
			list.add(i);
		}
		for (char i = 'A'; i <= 'Z'; ++i) {
			list.add(i);
		}

		charSet = list.toArray(new Character[list.size()]);
	}

	/**
	 * 生成随机串
	 *
	 * @param length
	 *            随机串长度
	 * @return
	 */
	public static String generate(int length) {
		if (length <= 0)
			length = 10;

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; ++i) {
			int rand = random.nextInt(charSet.length);
			sb.append(charSet[rand]);
		}

		return sb.toString();
	}

}
