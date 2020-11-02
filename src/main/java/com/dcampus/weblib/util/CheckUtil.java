package com.dcampus.weblib.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import com.dcampus.common.config.PropertyUtil;
import com.dcampus.common.config.ResourceProperty;
import com.dcampus.common.util.Log;
import com.dcampus.weblib.entity.Category;
import com.dcampus.weblib.entity.Group;
import com.dcampus.weblib.exception.GroupsException;

/**
 * 把之前放在groupmanager中的一些参数检查的静态方法抽出来作为工具类多次使用
 * @author patrick
 *
 */
public class CheckUtil {
	private static Log log = Log.getLog(CheckUtil.class);

	private static Pattern namePattern = Pattern
			.compile("[^\u4e00-\u9fa5|\\w|\\-|\\.|\\[|\\]|\\/|\\）|\\（|\\)|\\(]");
	/**
	 * 检查是否为空或含有中文非法字符
	 * @param name
	 * @throws GroupsException
	 */
	public static void checkName(String name) throws GroupsException {
		if (name == null)
			throw new GroupsException(ResourceProperty.getInvolidNameString());

		Matcher matcher = namePattern.matcher(name);
		if (matcher.find()) {
			throw new GroupsException(ResourceProperty.getInvolidNameString());
		}
	}

	private static Pattern englishNamePattern = Pattern
			.compile("[^.@-_0-9a-zA-Z]");
	/**
	 * 检查是否为空或含有英文非法字符
	 * @param name
	 * @throws GroupsException
	 */
	public static void checkEnglishName(String name) throws GroupsException {
		if (name == null)
			throw new GroupsException(ResourceProperty.getInvolidNameString());

		Matcher matcher = englishNamePattern.matcher(name);
		if (matcher.find()) {
			throw new GroupsException(ResourceProperty.getInvolidNameString());
		}
	}
	/**
	 * 柜子名字是否为空或含有保留名字
	 * @param name
	 * @throws GroupsException
	 */
	public static void checkGroupName(String name) throws GroupsException {
		checkName(name);

		String[] keyWords = PropertyUtil.getGroupNameKeyWord();
		for (String word : keyWords) {
			if (name.equalsIgnoreCase(word))
				throw new GroupsException(ResourceProperty.getInvolidGroupNameString(word));
		}
	}
	/**
	 * 柜子地址是否为空或含有保留地址
	 * @param addr
	 * @throws GroupsException
	 */
	public static void checkGroupAddr(String addr) throws GroupsException {
		if (addr == null || addr.length() == 0)
			throw new GroupsException(ResourceProperty.getGroupAddrDescriptionString());

		checkEnglishName(addr);

		String[] keyWords = PropertyUtil.getGroupAddrKeyWord();
		for (String word : keyWords) {
			if (addr.equalsIgnoreCase(word))
				throw new GroupsException(ResourceProperty.getInvolidGroupAddressString(word));
		}
	}

	private static Pattern involidCharPattern = Pattern.compile("[<>\\|:/\\\\\"\\*\\?]");
	
	/**
	 * 目录地址是否为空或含有保留地址
	 */
	public static void checkDirName(String name) throws GroupsException {
		if (name == null || name.length() == 0)
			throw new GroupsException(ResourceProperty.getGroupAddrDescriptionString());

		Map<String, String> keyWords = PropertyUtil.getFolderKeyWordMap();
		if (keyWords.containsKey(name.toLowerCase())) {
			throw new GroupsException(ResourceProperty.getInvolidFolderNameString(name));
		}
		Matcher matcher = involidCharPattern.matcher(name.toLowerCase());
		if (matcher.find()) {
			throw new GroupsException(
					ResourceProperty.getInvolidFolderNameString(ResourceProperty.getInvolidCharacters()));
		}
	}

	/**
	 * 检查柜子状态
	 * @param group
	 * @throws GroupsException
	 */
	public static void checkNormalGroup(Group group) throws GroupsException {
		if (group == null || group.getId() <= 0) {
			throw new GroupsException(
					ResourceProperty.getDataNotFoundExceptionString());
		}
		if (group.getGroupStatus() != Group.STATUS_NORMAL)
			throw new GroupsException(
					ResourceProperty.getNotNormalGroupString());
	}
	
	/**
	 * 检查分类状态
	 * @param category
	 * @throws GroupsException
	 */
	public static void checkNormalCategory(Category category) throws GroupsException {
		if (category == null || category.getId() <= 0) {
			throw new GroupsException(
					ResourceProperty.getDataNotFoundExceptionString());
		}
		if (category.getCategoryStatus() != Category.STATUS_NORMAL)
			throw new GroupsException(
					ResourceProperty.getNotNormalCategoryString());
	}
	
	/**
	 * 检查查询开始值
	 * @param start
	 * @return
	 */
	public static int reviseStart(int start) {
		if (start < 0)
			return 0;
		return start;
	}

	/**
	 * 检查结束值
	 * @param limit
	 * @param defaultValue
	 * @return
	 */
	public static int reviseLimit(int limit, int defaultValue) {
		if (limit <= 0)
			return defaultValue;
		return limit;
	}

	
}
