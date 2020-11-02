package com.dcampus.common.util;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import static org.apache.axis2.util.Loader.getResourceAsStream;

/**
 * 进行缓存操作的帮助类
 *
 * @author zim
 *
 */
@Service
@Lazy(false)
public class CacheUtil {

//	public static  CacheManager create = new CacheManager(getResourceAsStream("/ehcache.xml"));
	/** 该cache用户缓存圈子导航树，可参见GroupManager.trees方法 **/
	public static Cache myGroupsCache = CacheManager.getInstance().getCache(
			"mygroup-cache");

	public static Cache myVisualGroupCache = CacheManager.getInstance().getCache(
			"myvisualgroup-cache");

	public static Cache memberTeamCache = CacheManager.getInstance().getCache(
			"memberTeam-cache");

	public static Cache memberChildrenCache = CacheManager.getInstance().getCache(
			"memberChildren-cache");

	public static Cache categoryGroupCache = CacheManager.getInstance().getCache(
			"categoryGroup-cache");
//	public static Cache myGroupsCache = create.getCache(
//			"mygroup-cache");
//
//	public static Cache myVisualGroupCache = create.getCache(
//			"myvisualgroup-cache");
//
//	public static Cache memberTeamCache = create.getCache(
//			"memberTeam-cache");
//
//	public static Cache memberChildrenCache = create.getCache(
//			"memberChildren-cache");
//
//	public static Cache categoryGroupCache = create.getCache(
//			"categoryGroup-cache");
	/**
	 * 获取缓存值
	 *
	 * @param cache
	 *            缓存
	 * @param key
	 *            缓存键
	 * @return
	 */
	public static Object getCache(Cache cache, String key) {
		Element element = cache.get(key);
		if (element == null)
			return null;
		return element.getObjectValue();
	}

	/**
	 * 设置缓存
	 *
	 * @param cache
	 *            缓存
	 * @param key
	 *            缓存键
	 * @param value
	 *            缓存值
	 */
	public static void setCache(Cache cache, String key, Object value) {
		cache.put(new Element(key, value));
	}

	/**
	 * 根据缓存键名前缀移除缓存
	 *
	 * @param cache
	 *            缓存
	 * @param key
	 *            缓存键
	 */
	public static void removeCache(Cache cache, String key) {
		for (Object o : cache.getKeys()) {
			if (((String) o).startsWith(key)) {
				cache.remove(o);
			}
		}
	}

	/**
	 * 移除所有缓存
	 *
	 * @param cache
	 *            缓存
	 */
	public static void removeAll(Cache cache) {
		cache.removeAll();
	}
}
