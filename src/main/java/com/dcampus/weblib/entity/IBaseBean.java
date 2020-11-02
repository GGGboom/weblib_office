package com.dcampus.weblib.entity;

import java.io.Serializable;

/**
 * 为了新的树结构可以缓存
 * @author patrick
 *
 * @param <T>
 */
public interface IBaseBean<T> extends Serializable {
	public static enum Order {
		ASC("ASC"), DESC("DESC");
		private String name;

		private Order(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	/**
	 * 获取bean的id
	 *
	 * @return
	 */
	T getId();
}

