package com.dcampus.weblib.entity.keys;

import com.dcampus.common.paging.SimpleSearchItemKey;

public class ResourceReceiveSearchItemKey<T> extends SimpleSearchItemKey<T> {

	public static final ResourceReceiveSearchItemKey<Long> ResourceId = new ResourceReceiveSearchItemKey<Long>(
			"resourceId", "resource.id", Long.class);

	public static final ResourceReceiveSearchItemKey<Long> ProviderId = new ResourceReceiveSearchItemKey<Long>(
			"providerId", "share.provider.id", Long.class);
	
	public static final ResourceReceiveSearchItemKey<Long> RecipientId = new ResourceReceiveSearchItemKey<Long>(
			"recipientId", "recipient.id", Long.class);
	
	public static final ResourceReceiveSearchItemKey<String> ResourcePath = new ResourceReceiveSearchItemKey<String>(
			"resourcePath", "resource.path", String.class);
	
	protected ResourceReceiveSearchItemKey(String name, String oringalName, Class<T> valueClass){
        super(name, oringalName, valueClass);
    }
	
	protected ResourceReceiveSearchItemKey(String name, Class<T> valueClass){
        super(name, valueClass);
    }

}
