package com.dcampus.weblib.util.userutil;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.Properties;
import com.dcampus.common.util.Properties;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.dcampus.common.util.MD5;
import com.dcampus.common.util.Crypt;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.util.userutil.models.RemoteItems;
import com.dcampus.weblib.util.userutil.models.RemoteUser;

/**
 * 对接UserManagement的远程调用工具类
 * 
 * @author feng.zd
 *
 */
public class UserRemoteServiceUtil {

	static Properties properties = null;
	// private static ResourceLoader resourceLoader = new
	// DefaultResourceLoader();

	static {
		try {
//			properties = new Properties();
			// String location = "user.properties";
			// Resource resource = resourceLoader.getResource(location);
			// InputStream in = resource.getInputStream();
//			InputStream in = UserRemoteServiceUtil.class
//					.getResourceAsStream("user.properties");
//			properties.load(in);
//				in.close();
			properties=new Properties(Thread.currentThread().getContextClassLoader().getResourceAsStream(
							"user.properties"));
//

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static String url = properties.getProperty("ws.user.url");

	public static String path = properties.getProperty("ws.user.path");

	public static void main(String[] args) {

		/*
		 * findUsersByGroup("87bc996419f44271ad235fb5e13ed7c9");
		 * 
		 * findStemsByParent("0d06faa20c0e4e87999acf335c358bee");
		 * System.out.println(findStemsByParent(null).length);
		 * findStemsByParent(null);
		 * findGroupsByParent("0d06faa20c0e4e87999acf335c358bee");
		 * System.out.println(findGroupsByParent(null).length);
		 * findGroupsByParent(null);
		 */
		// getGroupsOfUser("ttttt12t");
		// System.out.println(findGroupByName("test213", false).getName());
		// findGroupsByParent("e0bb6d9b265940ecad6746f6583ab12b");

	}

	public static RemoteItems findStem(String stemId) {
		String methods = path + "/admin/json/group/steminfo";
		Map<String, String[]> params = new HashMap<String, String[]>();
		params.put("stemId", new String[] { stemId });
		String result = HttpClientUtil.get(url, methods, params);
		RemoteItems[] items = getStemsFromResult(result);
		if (items.length > 0) {
			return items[0];
		} else {
			return null;
		}
	}
	public static RemoteItems[] findStemsByParent(String parentId){
		return findStemsByParent(parentId,false);
	}

	public static RemoteItems[] findStemsByParent(String parentId,boolean withoutLeaf) {
		String methods = path + "/admin/json/group/list_v2";
		Map<String, String[]> params = new HashMap<String, String[]>();
		if (parentId != null) {
			params.put("parentId", new String[] { parentId });
		}
		if(withoutLeaf){
			params.put("withoutLeaf", new String[] {"true"});
		}
		params.put("type", new String[] { "STEMS" });
		String result = HttpClientUtil.get(url, methods, params);
		RemoteItems[] items = getStemsFromResult(result);
		/*for (RemoteItems s : items) {
			// System.out.println(s.getName() + "==s==" + s.getType());
		}*/
		return items;
	}
	public static RemoteItems[] findStemsAndGroupsByParent(String parentId){
		
		return findStemsAndGroupsByParent(parentId,false);
	}
	/**
	 * @author zf
	 * @param parentId
	 * @return
	 * 同时查询子组织和组   
	 */
	public static RemoteItems[] findStemsAndGroupsByParent(String parentId,boolean withoutLeaf){
		String methods = path + "/admin/json/group/list_v2";
		Map<String, String[]> params = new HashMap<String, String[]>();
		if (parentId != null) {
			params.put("parentId", new String[] { parentId });
		}
		if(withoutLeaf){
			params.put("withoutLeaf", new String[] {"true"});
		}
		params.put("type", new String[] { "ALL" });
		String result = HttpClientUtil.get(url, methods, params);
		RemoteItems[] stems = getStemsFromResult(result);
		RemoteItems[] groups = getGroupsFromResult(result);
		/*for (RemoteItems s : items) {
			// System.out.println(s.getName() + "==s==" + s.getType());
		}*/
		RemoteItems[] items = new RemoteItems[stems.length+groups.length];
		System.arraycopy(stems, 0, items, 0, stems.length);
		System.arraycopy(groups, 0, items, stems.length,
				groups.length);
		return items;
	}

	public static RemoteItems findGroup(String groupId) {
		String methods = path + "/admin/json/group/groupinfo";
		Map<String, String[]> params = new HashMap<String, String[]>();
		params.put("groupId", new String[] { groupId });
		String result = HttpClientUtil.get(url, methods, params);
		RemoteItems[] items = getGroupsFromResult(result);
		if (items.length > 0) {
			return items[0];
		} else {
			return null;
		}
	}
	
	/**
	 * 根据subjectIds（如：zf）来获得所在的组，以及所在的父组织
	 * @author zf
	 * @param subjectIds
	 * @return
	 */
	public static JSONObject getGroupsOfMembers(String[] subjectIds){
		String methods = path+"/admin/json/group/getgroupsofmembers";
		Map<String, String[]> params = new HashMap<String, String[]>();
		params.put("subjectIds", subjectIds);
		String result = HttpClientUtil.get(url, methods, params);
		JSONObject json = JSONObject.fromObject(result);
		return json;
		
	}
	
	public static JSONObject getParentsOfFolderOrTeam(String[] subjectIds,String type){
		String methods = path+"/admin/json/group/getParentsOfFolderOrTeam";
		Map<String, String[]> params = new HashMap<String, String[]>();
		params.put("subjectIds", subjectIds);
		params.put("type", new String[]{type});
		String result = HttpClientUtil.get(url, methods, params);
		JSONObject json = JSONObject.fromObject(result);
		return json;
		
	}

	public static RemoteItems[] findGroupByName(String groupName, boolean exact) {
		String methods = path + "/admin/json/group/groupinfobyname";
		Map<String, String[]> params = new HashMap<String, String[]>();
		params.put("groupName", new String[] { groupName });
		if (exact) {
			params.put("exact", new String[] { "true" });
		} else {
			params.put("exact", new String[] { "false" });
		}
		String result = HttpClientUtil.post(url, methods, params);//modified by zf 161212 get to post 
		RemoteItems[] items = getGroupsFromResult(result);
		if (items.length > 0) {
			return items;
		} else {
			return null;
		}
	}

	public static RemoteItems[] findGroupsByParent(String parentId) {
		String methods = path + "/admin/json/group/list_v2";
		Map<String, String[]> params = new HashMap<String, String[]>();
		if (parentId != null) {
			params.put("parentId", new String[] { parentId });
		}
		params.put("type", new String[] { "GROUP" });
		String result = HttpClientUtil.get(url, methods, params);
		RemoteItems[] items = getGroupsFromResult(result);
		return items;
	}
	public static RemoteItems[] findSubTreeGroupsByParent(String parentId) {
		String methods = path + "/admin/json/group/getSubTreeGroupsInFolder";
		Map<String, String[]> params = new HashMap<String, String[]>();
		if (parentId != null) {
			params.put("parentId", new String[] { parentId });
		}
		//params.put("type", new String[] { "GROUP" });
		String result = HttpClientUtil.get(url, methods, params);
		RemoteItems[] items = getGroupsFromResult(result);
		return items;
	}


	public static RemoteUser[] getGroupMembers(String groupId) {
		String methods = path + "/admin/json/group/getgroupmembers";
		Map<String, String[]> params = new HashMap<String, String[]>();
		if (groupId != null) {
			params.put("groupId", new String[] { groupId });
		}
		String result = HttpClientUtil.get(url, methods, params);
		RemoteUser[] items = getUsersFromResult(result);
		return items;
	}
	/**同时查多个group的member
	 * @param groupId
	 * @return
	 */
	public static RemoteUser[] getGroupsMembers(String[] groupId) {
		String methods = path + "/admin/json/group/getgroupsmembers";
		Map<String, String[]> params = new HashMap<String, String[]>();
		if (groupId != null) {
			params.put("groupId",  groupId );
		}
		String result = HttpClientUtil.get(url, methods, params);
		RemoteUser[] items = getUsersFromResult(result);
		return items;
	}

	public static RemoteItems[] getGroupsOfUser(String username) {
		String methods = path + "/admin/json/group/getgroupsofuser";
		Map<String, String[]> params = new HashMap<String, String[]>();
		params.put("username", new String[] { username });
		String result = HttpClientUtil.get(url, methods, params);
		RemoteItems[] items = getGroupsFromResult(result);
		return items;
	}

	public static String stemSave(String name, String desc, String parentId) {
		String methods = path + "/admin/json/group/stemsave";
		Map<String, String[]> params = new HashMap<String, String[]>();
		try {
			params.put("name",
					new String[] { URLDecoder.decode(name, "UTF-8") });
			params.put("desc",
					new String[] { URLDecoder.decode(desc, "UTF-8") });
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		params.put("parentId", new String[] { parentId });
		String result = HttpClientUtil.post(url, methods, params);
		JSONObject json = JSONObject.fromObject(result);
		return json.getString("id");
	}

	public static boolean stemDelete(String stemId) {
		String methods = path + "/admin/json/group/stemdelete";
		Map<String, String[]> params = new HashMap<String, String[]>();
		params.put("stemId", new String[] { stemId });
		String result = HttpClientUtil.post(url, methods, params);
		return true;
	}

	public static boolean groupDelete(String groupId) {
		String methods = path + "/admin/json/group/groupdelete";
		Map<String, String[]> params = new HashMap<String, String[]>();
		params.put("groupId", new String[] { groupId });
		String result = HttpClientUtil.post(url, methods, params);
		return true;
	}
	public static String groupSave(String name, String desc, String parentId){
		return groupSave(name,desc,parentId,false);
	}

	public static String groupSave(String name, String desc, String parentId,boolean rename) {//rename为true 时，已存在同名则重命名
		String methods = path + "/admin/json/group/groupsave";
		Map<String, String[]> params = new HashMap<String, String[]>();
		try {
			params.put("name",
					new String[] { URLDecoder.decode(name, "UTF-8") });
			params.put("desc",
					new String[] { URLDecoder.decode(desc, "UTF-8") });
			if(rename){
				params.put("rename",
						new String[] { URLDecoder.decode("true", "UTF-8") });
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		params.put("parentId", new String[] { parentId });
		String result = HttpClientUtil.post(url, methods, params);
		JSONObject json = JSONObject.fromObject(result);
		return json.getString("id");
	}
	/**@author zf
	 * @param groupId
	 * @param stemId
	 * @return
	 */
	public static String groupCopy(String groupId, String stemId) {
		String methods = path + "/admin/json/group/copyGroup";
		Map<String, String[]> params = new HashMap<String, String[]>();

		params.put("groupId", new String[] { groupId });
		params.put("stemId", new String[] { stemId });

		String result = HttpClientUtil.post_v2(url, methods, params);
		JSONObject json = JSONObject.fromObject(result);
		String code = json.getString("code");
		if("fail".equals(code)){
			throw new GroupsException(json.getString("detail"));
		}
		JSONObject data = json.getJSONObject("data");
		return data.getString("id");
	}
	/**@author zf
	 * @param srcStemId
	 * @param stemId
	 * @return
	 */
	public static String stemCopy(String srcStemId, String stemId) {
		String methods = path + "/admin/json/group/copyStem";
		Map<String, String[]> params = new HashMap<String, String[]>();

		params.put("srcStemId", new String[] { srcStemId });
		params.put("stemId", new String[] { stemId });

		String result = HttpClientUtil.post_v2(url, methods, params);
		JSONObject json = JSONObject.fromObject(result);
		String code = json.getString("code");
		if("fail".equals(code)){
			throw new GroupsException(json.getString("detail"));
		}
		JSONObject data = json.getJSONObject("data");
		return data.getString("id");
	}
	/**@author zf
	 * @param groupId
	 * @param stemId
	 * @return
	 */
	public static String groupMove(String groupId, String stemId) {
		String methods = path + "/admin/json/group/moveGroup";
		Map<String, String[]> params = new HashMap<String, String[]>();

		params.put("groupId", new String[] { groupId });
		params.put("stemId", new String[] { stemId });

		String result = HttpClientUtil.post_v2(url, methods, params);
		JSONObject json = JSONObject.fromObject(result);
		String code = json.getString("code");
		if("fail".equals(code)){
			throw new GroupsException(json.getString("detail"));
		}
		JSONObject data = json.getJSONObject("data");
		return data.getString("id");
	}
	/**@author zf
	 * @param srcStemId
	 * @param stemId
	 * @return
	 */
	public static String stemMove(String srcStemId, String stemId) {
		String methods = path + "/admin/json/group/moveStem";
		Map<String, String[]> params = new HashMap<String, String[]>();

		params.put("srcStemId", new String[] { srcStemId });
		params.put("stemId", new String[] { stemId });

		String result = HttpClientUtil.post_v2(url, methods, params);
		JSONObject json = JSONObject.fromObject(result);
		String code = json.getString("code");
		if("fail".equals(code)){
			throw new GroupsException(json.getString("detail"));
		}
		JSONObject data = json.getJSONObject("data");
		return data.getString("id");
	}

	public static boolean userSave(RemoteUser user) {
		String methods = path + "/admin/json/group/usersave";
		Map<String, String[]> params = new HashMap<String, String[]>();
		try {
			params.put("username", new String[] { URLDecoder.decode(
					user.getUsername(), "UTF-8") });
			params.put(
					"password",
					new String[] { URLDecoder.decode(
							Crypt.encrypt(user.getPassword()), "UTF-8") });
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String result = HttpClientUtil.post(url, methods, params);
		return true;
	}

	public static boolean userEdit(RemoteUser user) {
		String methods = path + "/admin/json/group/useredit";
		Map<String, String[]> params = new HashMap<String, String[]>();
		try {
			params.put("username", new String[] { URLDecoder.decode(
					user.getUsername(), "UTF-8") });
			if (user.getPassword() != null) {
				params.put(
						"password",
						new String[] { URLDecoder.decode(
								Crypt.encrypt(user.getPassword()), "UTF-8") });
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String result = HttpClientUtil.post(url, methods, params);
		return true;
	}

	public static boolean userDelete(RemoteUser user) {
		String methods = path + "/admin/json/group/userdelete";
		Map<String, String[]> params = new HashMap<String, String[]>();
		try {
			params.put("username", new String[] { URLDecoder.decode(
					user.getUsername(), "UTF-8") });
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		String result = HttpClientUtil.post(url, methods, params);
		return true;
	}

	public static boolean addMember(String[] subjectIds, String groupId) {
		System.out.println("path:"+path);
		//String path1="127.0.0.1:8080";
		String methods = path + "/admin/json/group/addmember";
		System.out.println("method:"+methods);
		Map<String, String[]> params = new HashMap<String, String[]>();
		try {
			params.put("subjectIds", subjectIds);
			params.put("groupId", new String[] { groupId });
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String result = HttpClientUtil.post(url, methods, params);
		System.out.println("result:"+result);
		return true;
	}

	public static boolean deleteMember(String[] subjectIds, String groupId) {
		String methods = path + "/admin/json/group/deletemember";
		Map<String, String[]> params = new HashMap<String, String[]>();
		try {
			params.put("subjectIds", subjectIds);
			params.put("groupId", new String[] { groupId });
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String result = HttpClientUtil.post(url, methods, params);
		return true;
	}

	public static boolean stemEdit(String name, String desc, String stemId) {
		String methods = path + "/admin/json/group/stemedit";
		Map<String, String[]> params = new HashMap<String, String[]>();
		try {
			params.put("name", new String[] { name });
			params.put("desc", new String[] { desc });
			params.put("stemId", new String[] { stemId });
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String result = HttpClientUtil.post(url, methods, params);
		return true;
	}

	public static boolean groupEdit(String name, String desc, String groupId) {
		String methods = path + "/admin/json/group/groupedit";
		Map<String, String[]> params = new HashMap<String, String[]>();
		try {
			params.put("name", new String[] { name });
			params.put("desc", new String[] { desc });
			params.put("groupId", new String[] { groupId });
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String result = HttpClientUtil.post(url, methods, params);
		return true;
	}

	private static RemoteUser[] getUsersFromResult(String data) {
		JSONObject json = JSONObject.fromObject(data);
		List<RemoteUser> result = new ArrayList<RemoteUser>();
		JSONArray users = null;
		try {
			users = json.getJSONArray("users");
		} catch (net.sf.json.JSONException e) {
			// e.printStackTrace();
			return new RemoteUser[0];
		}
		for (Object s : users) {
			JSONObject jtmp = JSONObject.fromObject(s);
			RemoteUser item = new RemoteUser();
			item.setId(jtmp.getString("id"));
			item.setUsername(jtmp.getString("username"));
			item.setName(jtmp.getString("name"));
			item.setEmail(jtmp.getString("email"));
			item.setPhone(jtmp.getString("phone"));
			item.setAddress(jtmp.getString("address"));
			item.setZipcode(jtmp.getString("zipcode"));
			item.setFax(jtmp.getString("fax"));
			result.add(item);
		}
		return (RemoteUser[]) result.toArray(new RemoteUser[result.size()]);
	}

	private static RemoteItems[] getStemsFromResult(String data) {
		JSONObject json = JSONObject.fromObject(data);
		List<RemoteItems> result = new ArrayList<RemoteItems>();
		JSONArray stems = null;
		JSONArray stemIsLeaf = null;
		try {
			stems = json.getJSONArray("stems");
			
			if(json.containsKey("stemIsLeaf"))
				stemIsLeaf = json.getJSONArray("stemIsLeaf");
			else
				stemIsLeaf  = new JSONArray();
		} catch (net.sf.json.JSONException e) {
			// e.printStackTrace();
			return new RemoteItems[0];
		}
		int index = 0;
		// for (Object s : stems) {
		for (; index < stems.size(); index++) {
			Object s = stems.get(index);
			JSONObject jtmp = JSONObject.fromObject(s);
			RemoteItems item = new RemoteItems();
			item.setId(jtmp.getString("uuid"));
			item.setName(jtmp.getString("extension"));
			item.setFullname(jtmp.getString("name"));
			item.setDescription(jtmp.getString("description"));
			if (index < stemIsLeaf.size())
				item.setIsLeaf(stemIsLeaf.getBoolean(index));// 设置是否为叶子节点
			else
				item.setIsLeaf(false);
			item.setType("stems");
			result.add(item);
		}
		return (RemoteItems[]) result.toArray(new RemoteItems[result.size()]);
	}

	private static RemoteItems[] getGroupsFromResult(String data) {
		JSONObject json = JSONObject.fromObject(data);
		List<RemoteItems> result = new ArrayList<RemoteItems>();
		JSONArray groups = null;
		JSONArray groupIsLeaf = null;
		try {
			groups = json.getJSONArray("groups");
			if(json.containsKey("groupIsLeaf"))
				groupIsLeaf = json.getJSONArray("groupIsLeaf");
			else
				groupIsLeaf = new JSONArray();
		} catch (net.sf.json.JSONException e) {
			// e.printStackTrace();
			return new RemoteItems[0];
		}
		// for (Object s : groups) {
		for (int index = 0; index < groups.size(); index++) {
			Object s = groups.get(index);
			JSONObject jtmp = JSONObject.fromObject(s);
			RemoteItems item = new RemoteItems();
			item.setId(jtmp.getString("uuid"));
			item.setName(jtmp.getString("extension"));
			item.setFullname(jtmp.getString("name"));
			item.setDescription(jtmp.getString("description"));
			if (index < groupIsLeaf.size())
				item.setIsLeaf(groupIsLeaf.getBoolean(index));// 设置是否为叶子节点
			else
				item.setIsLeaf(false);
			item.setType("groups");
			result.add(item);
		}
		return (RemoteItems[]) result.toArray(new RemoteItems[result.size()]);
	}

}
