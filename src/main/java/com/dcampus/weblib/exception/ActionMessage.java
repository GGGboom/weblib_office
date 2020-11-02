package com.dcampus.weblib.exception;

import com.dcampus.common.Identifiable;
import com.dcampus.common.util.Strings;

/**
 * action出错信息列表<br>
 * FIXME 几乎还未使用，暂时保留
 *
 * @author zim
 *
 */
public class ActionMessage extends Exception implements Identifiable {
	private static final long serialVersionUID = -8443139338283380921L;

	// 系统定义编号 0～1000，用户自定义编号 1001～
	public static final String CODE_INTERNAL_SERVER_ERROR = "500";
	public static final String CODE_USER_ALREADY_EXIST = "100";
	public static final String CODE_NOT_LOGIN = "480";
	public static final String CODE_USER_NOT_FOUND = "450";
	public static final String CODE_WRONG_PASSWORD = "350";
	public static final String CODE_NOT_FOUND = "404";
	public static final String CODE_FORBIDDEN = "403";
	public static final String CODE_UNAUTHORIZED = "401";
	public static final String CODE_OK = "200";
	public static final String CODE_UNKNOWN = "0";
	public static final String CODE_BAD_INPUT = "300";
	public static final String CODE_FORUM_ALREADY_EXIST = "405";
	public static final String CODE_DUPLICATE_BADWORD = "301";
	public static final String CODE_REGISTER_TOO_MANY_TIMES = "406";
	public static final String CODE_INVALID_ICON = "550";
	public static final String CODE_MAX_ICON = "510";
	public static final String CODE_AUDIT_ICON = "515";
	public static final String CODE_LOAD_PROPERTIES_FAIL = "520";
	public static final String CODE_FILE_OVERSIZE = "540";
	public static final String CODE_ILLEGAL_FILE_EXTENSION = "542";
	public static final String CODE_UPLOADFILE_FAIL = "541";
	public static final String CODE_POPEDOM = "407";
	public static final String CODE_SYSTEM_STOPED = "1001";
	public static final String CODE_NO_PERMISSION = "1002";

	public static final String DETAIL_INTERNAL_SERVER_ERROR = "服务器内部错误！";
	public static final String DETAIL_USER_ALREADY_EXIST = "用户已经存在！";
	public static final String DETAIL_NOT_LOGIN = "尚未登陆！";
	public static final String DETAIL_USER_NOT_FOUND = "没有找到此用户！";
	public static final String DETAIL_WRONG_PASSWORD = "密码错误！";
	public static final String DETAIL_NOT_FOUND = "查找失败！";
	public static final String DETAIL_FORBIDDEN = "禁止操作！";
	public static final String DETAIL_UNAUTHORIZED = "不能获得授权！";
	public static final String DETAIL_OK = "成功！";
	public static final String DETAIL_UNKNOWN = "未知错误！";
	public static final String DETAIL_BAD_INPUT = "非法输入！";
	public static final String DETAIL_FORUM_ALREADY_EXIST = "此板块已经存在!";
	public static final String DETAIL_DUPLICATE_BADWORD = "该过滤字已存在";
	public static final String DETAIL_REGISTER_TOO_MANY_TIMES = "请不要一次注册过多的马甲!";
	public static final String DETAIL_INVALID_ICON = "非法的图像大小或格式!";
	public static final String DETAIL_MAX_ICON = "自定义图像数目已达最大值!";
	public static final String DETAIL_AUDIT_ICON = "图像正在审核中！";
	public static final String DETAIL_LOAD_PROPERTIES_FAIL = "配置文件读取错误";
	public static final String DETAIL_FILE_OVERSIZE = "文件大小超过限制";
	public static final String DETAIL_UPLOADFILE_FAIL = "文件上传失败";
	public static final String DETAIL_ILLEGAL_FILE_EXTENSION = "不支持此类文件上传";
	public static final String DETAIL_POPEDOM = "用户没有权限";
	public static final String DETAIL_SYSTEM_STOPED = "系统备份或还原中，暂停使用。";

	private Type type;
	private String code;
	private String detail;

	private String uid = Strings.random(10);

	public ActionMessage(Type type, String code, String detail) {
		this.type = type;
		this.code = code;
		this.detail = detail;
	}

	public ActionMessage(Type type, String code) {
		this(type, code, null);
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getUid() {
		return uid;
	}

	public static enum Type {
		DEBUG(0), INFO(1), WARN(2), ERROR(3), SUCCESS(4);
		int type = 0;

		private Type(int type) {
			this.type = type;
		}

		@Override
		public String toString() {
			switch (type) {
			case 0:
				return "debug";
			case 1:
				return "info";
			case 2:
				return "warn";
			case 3:
				return "error";
			case 4:
				return "success";
			default:
				return "none";
			}
		}
	};
}
