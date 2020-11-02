package com.dcampus.weblib.entity.keys;

import com.dcampus.common.paging.SearchItemKey;
import com.dcampus.common.paging.SimpleSearchItemKey;

import java.lang.reflect.Field;
import java.sql.Timestamp;

public class ResourceSearchItemKey<T> extends SimpleSearchItemKey<T> {

	public static final ResourceSearchItemKey<Long> GroupId = new ResourceSearchItemKey<Long>(
			"groupId", "agroup.id", Long.class);

	public static final ResourceSearchItemKey<Long> ParentId = new ResourceSearchItemKey<Long>(
			"parentId", Long.class);

	public static final ResourceSearchItemKey<String> Status = new ResourceSearchItemKey<String>(
			"resourceStatus", String.class);
	
	public static final ResourceSearchItemKey<Integer> Type = new ResourceSearchItemKey<Integer>(
			"resourceType", Integer.class);

	public static final ResourceSearchItemKey<String> Name = new ResourceSearchItemKey<String>(
			"name", String.class);
	
	public static final ResourceSearchItemKey<Integer> DocumentType = new ResourceSearchItemKey<Integer>(
			"documentTypeValue", Integer.class);
	
	public static final ResourceSearchItemKey<String> MemberName = new ResourceSearchItemKey<String>(
			"memberName", String.class);
	
	public static final ResourceSearchItemKey<Long> CreatorId = new ResourceSearchItemKey<Long>(
			"creatorId", "creatorId", Long.class);
	
	public static final ResourceSearchItemKey<String> Desc = new ResourceSearchItemKey<String>(
			"desc", String.class);
	
	public static final ResourceSearchItemKey<Timestamp> CreationDate = new ResourceSearchItemKey<Timestamp>(
			"createDate", Timestamp.class);
	
	public static final ResourceSearchItemKey<String> Path = new ResourceSearchItemKey<String>(
			"path", String.class);
	
	private ResourceSearchItemKey(String name, Class<T> valueClass) {
		super(name, valueClass);
	}
	protected ResourceSearchItemKey(String name, String oringalName, Class<T> valueClass){
        super(name, oringalName, valueClass);
    }

	public static SearchItemKey getSearchItemKey(Class cls, String fieldname){
		for(Field field : cls.getFields()){
			try{
				SearchItemKey sik = (SearchItemKey)field.get(null);
				if (fieldname.indexOf("/") != -1)
					fieldname = fieldname.replaceAll("/", "_");
				if(sik.getName().equalsIgnoreCase(fieldname))
					return sik;
			}catch(Exception e){
				throw new RuntimeException(e);
			}
		}
		throw new IllegalArgumentException(
	            "Field undefined:" + fieldname);
	}

}
