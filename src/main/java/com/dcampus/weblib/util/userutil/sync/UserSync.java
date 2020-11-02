package com.dcampus.weblib.util.userutil.sync;

import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.util.userutil.models.RemoteUser;

public interface UserSync {

	RemoteUser validate(String userName,String pwd_plain) throws GroupsException;
	
	boolean regiesterLocalUser(RemoteUser user,String pwd_plain)throws GroupsException;
	
	void setUrl(String url);
	
}
