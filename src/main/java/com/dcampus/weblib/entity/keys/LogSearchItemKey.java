package com.dcampus.weblib.entity.keys;


import com.dcampus.common.paging.SimpleSearchItemKey;

import java.sql.Timestamp;

public class LogSearchItemKey<T> extends SimpleSearchItemKey<T> {

	public static final LogSearchItemKey<String> Account = new LogSearchItemKey<String>("account",String.class);
	public static final LogSearchItemKey<String> MemberName = new LogSearchItemKey<String>("memberName",String.class);
	public static final LogSearchItemKey<String> TargetObject = new LogSearchItemKey<String>("targetObject",String.class);
	public static final LogSearchItemKey<String> Action = new LogSearchItemKey<String>("action",String.class);
	public static final LogSearchItemKey<String> Result = new LogSearchItemKey<String>("result",String.class);
	public static final LogSearchItemKey<Timestamp> CreateDate = new LogSearchItemKey<Timestamp>("createDate",Timestamp.class);
	public static final LogSearchItemKey<String> Ip = new LogSearchItemKey<String>("ip",String.class);
	public static final LogSearchItemKey<String> Terminal = new LogSearchItemKey<String>("terminal",String.class);
	public static final LogSearchItemKey<String> GroupName = new LogSearchItemKey<String>("groupName",String.class);

	public LogSearchItemKey(String name, Class<T> valueClass) {
		super(name, valueClass);
	}


}
