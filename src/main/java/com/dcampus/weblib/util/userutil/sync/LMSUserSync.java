package com.dcampus.weblib.util.userutil.sync;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.util.userutil.HttpClientUtil;
import com.dcampus.weblib.util.userutil.models.RemoteUser;

public class LMSUserSync implements UserSync{
	
	private String url;
	private final String validatePath = "/lms/htmlLogin";

	@Override
	public RemoteUser validate(String userName, String pwd_plain)
			throws GroupsException {
		Map<String,String[] > param = new HashMap<String, String[]>();
		param.put("username",new String[]{userName});
		param.put("password",new String[]{pwd_plain});
		String json = HttpClientUtil.get_v2(url, validatePath, param);
		JSONObject jo;
		try {
			jo = JSONObject.fromObject(json);
			if("success".equals(jo.getString("result"))){
				RemoteUser user = new RemoteUser(userName, pwd_plain);
				return user;
			}
		} catch (JSONException e) {
			System.out.println("LMS 用户校验接口返回数据格式错误");
		}
		throw new GroupsException("用户名或者密码错误");
		
	}

	@Override
	public boolean regiesterLocalUser(RemoteUser user, String pwd_plain)
			throws GroupsException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setUrl(String url) {
		this.url = url;
	}

}
