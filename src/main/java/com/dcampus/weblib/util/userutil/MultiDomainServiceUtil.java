package com.dcampus.weblib.util.userutil;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;



import net.sf.json.JSONObject;

import com.dcampus.weblib.exception.GroupsException;

/**
 * @author zf at 2016年12月14日
 *
 */
public class MultiDomainServiceUtil {
	static Properties properties = null;

	static {
		try {
			properties = new Properties();

			InputStream in = UserRemoteServiceUtil.class
					.getResourceAsStream("user.properties");
			properties.load(in);
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static String url = properties.getProperty("ws.user.url");

	public static String path = properties.getProperty("ws.user.path");
	
	private static String grouperAdmin = properties.getProperty("ws.user.admin");

	public static void main(String[] args) {

	}

	/**
	 * @param domainName
	 *            创建的域名称
	 * @param folderId
	 *            关联的folderUUID
	 * @param creatorId
	 *            memberid
	 * @param creatorName
	 *            memberName
	 * @return
	 */
	public static Long createDomain(String domainName, String folderId,
			Long creatorId, String creatorName,String actAsSubjectId) throws GroupsException {
		String methods = path + "multiDomain/json/admin/createDomain";

		Map<String, String[]> param = new HashMap<String, String[]>();
		param.put("domainName", new String[] { domainName });
		param.put("creatorId", new String[] { creatorId.toString() });
		param.put("creatorName", new String[] { creatorName });
		if (folderId != null && folderId.length() > 0) {
			param.put("folderId", new String[] { folderId });
		}
		param.put("actAsSubjectId",new String[]{actAsSubjectId==null?grouperAdmin:actAsSubjectId});

		try {
			String result = HttpClientUtil.post_v2(url, methods, param);
			JSONObject json = JSONObject.fromObject(result);
			String code = json.getString("code");
			if ("fail".equals(code)) {
				String detail = json.optString("detail");
				throw new GroupsException(detail);
			}

			long domainId = json.getLong("domainId");
			return domainId;
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			throw new GroupsException(e.getMessage());
		}
	}

	public static void deleteDomain(Long domainId,String actAsSubjectId) throws GroupsException{
		String methods = path + "multiDomain/json/admin/deleteDomain";

		Map<String, String[]> param = new HashMap<String, String[]>();
		param.put("domainId", new String[] { domainId.toString() });
		param.put("actAsSubjectId",new String[]{actAsSubjectId==null?grouperAdmin:actAsSubjectId});
		try {
			String result = HttpClientUtil.post_v2(methods, methods, param);
			JSONObject json = JSONObject.fromObject(result);
			String code = json.getString("code");
			if ("fail".equals(code)) {
				String detail = json.optString("detail");
				throw new GroupsException(detail);
			}
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			throw new GroupsException(e.getMessage());
		}

	}

	public static void addManagerToDomain(Long domainId, String managerName,
			String actAsSubjectId) throws GroupsException{
		
		String methods = path + "multiDomain/json/admin/addManagerToDomain";

		Map<String, String[]> param = new HashMap<String, String[]>();
		param.put("domainId", new String[] { domainId.toString() });
		param.put("managerName", new String[] { managerName });
		param.put("actAsSubjectId",new String[]{actAsSubjectId==null?grouperAdmin:actAsSubjectId});
		try {
			String result = HttpClientUtil.post_v2(methods, methods, param);
			JSONObject json = JSONObject.fromObject(result);
			String code = json.getString("code");
			if ("fail".equals(code)) {
				String detail = json.optString("detail");
				throw new GroupsException(detail);
			}
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			throw new GroupsException(e.getMessage());
		}

		
	}

	public static void removeManagerFromDomain(Long domainId,
			String managerName, String actAsSubjectId) throws GroupsException{
		String methods = path + "multiDomain/json/admin/removeManagerFromDomain";

		Map<String, String[]> param = new HashMap<String, String[]>();
		param.put("domainId", new String[] { domainId.toString() });
		param.put("managerName", new String[] { managerName });
		param.put("actAsSubjectId",new String[]{actAsSubjectId==null?grouperAdmin:actAsSubjectId});
		try {
			String result = HttpClientUtil.post_v2(methods, methods, param);
			JSONObject json = JSONObject.fromObject(result);
			String code = json.getString("code");
			if ("fail".equals(code)) {
				String detail = json.optString("detail");
				throw new GroupsException(detail);
			}
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			throw new GroupsException(e.getMessage());
		}
	}

	public static void addFolderToDomain(Long domainId, String folderId,
			Long creatorId, String actAsSubjectId) throws GroupsException {
		String methods = path + "multiDomain/json/admin/addFolderToDomain";

		Map<String, String[]> param = new HashMap<String, String[]>();
		param.put("domainId", new String[] { domainId.toString() });
		param.put("folderId", new String[] { folderId });
		param.put("creatorId", new String[] { creatorId.toString() });
		param.put("actAsSubjectId",new String[]{actAsSubjectId==null?grouperAdmin:actAsSubjectId});
		try {
			String result = HttpClientUtil.post_v2(methods, methods, param);
			JSONObject json = JSONObject.fromObject(result);
			String code = json.getString("code");
			if ("fail".equals(code)) {
				String detail = json.optString("detail");
				throw new GroupsException(detail);
			}
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			throw new GroupsException(e.getMessage());
		}
	}

	public static void removeFolderFromDomain(Long domainId, String folderId,
			String actAsSubjectId) throws GroupsException {
		String methods = path + "multiDomain/json/admin/removeFolderFromDomain";

		Map<String, String[]> param = new HashMap<String, String[]>();
		param.put("domainId", new String[] { domainId.toString() });
		param.put("folderId", new String[] { folderId });
		param.put("actAsSubjectId",new String[]{actAsSubjectId==null?grouperAdmin:actAsSubjectId});
		try {
			String result = HttpClientUtil.post_v2(methods, methods, param);
			JSONObject json = JSONObject.fromObject(result);
			String code = json.getString("code");
			if ("fail".equals(code)) {
				String detail = json.optString("detail");
				throw new GroupsException(detail);
			}
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			throw new GroupsException(e.getMessage());
		}
	}
	
	public static void stemSave(Long domainId, String folderId,
			String actAsSubjectId) throws GroupsException {
		String methods = path + "multiDomain/json/manager/stemsave";

		Map<String, String[]> param = new HashMap<String, String[]>();
		param.put("domainId", new String[] { domainId.toString() });
		param.put("folderId", new String[] { folderId });
		param.put("actAsSubjectId",new String[]{actAsSubjectId==null?grouperAdmin:actAsSubjectId});
		try {
			String result = HttpClientUtil.post_v2(methods, methods, param);
			JSONObject json = JSONObject.fromObject(result);
			String code = json.getString("code");
			if ("fail".equals(code)) {
				String detail = json.optString("detail");
				throw new GroupsException(detail);
			}
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			throw new GroupsException(e.getMessage());
		}
	}

	public static String stemSave(Long domainId, String folderName,
			String desc, String parentId, String actAsSubjectId) {
		String methods = path + "multiDomain/json/manager/stemsave";

		Map<String, String[]> param = new HashMap<String, String[]>();
		param.put("domainId", new String[] { domainId.toString() });
		param.put("name", new String[] { folderName });
		param.put("desc", new String[] { desc });
		param.put("parentId", new String[] { parentId });
		param.put("actAsSubjectId",new String[]{actAsSubjectId});
		try {
			String result = HttpClientUtil.post_v2(methods, methods, param);
			JSONObject json = JSONObject.fromObject(result);
			String code = json.getString("code");
			if ("fail".equals(code)) {
				String detail = json.optString("detail");
				throw new GroupsException(detail);
			}
			String newFolderId = json.getJSONObject("data").getString("id");
			return newFolderId;
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			throw new GroupsException(e.getMessage());
		}
	}
	
	public static void stemDelete(Long domainId, String folderId, String actAsSubjectId) {
		String methods = path + "multiDomain/json/manager/stemdelete";

		Map<String, String[]> param = new HashMap<String, String[]>();
		param.put("domainId", new String[] { domainId.toString() });
		param.put("stemId", new String[] { folderId });
		param.put("actAsSubjectId",new String[]{actAsSubjectId});
		try {
			String result = HttpClientUtil.post_v2(methods, methods, param);
			JSONObject json = JSONObject.fromObject(result);
			String code = json.getString("code");
			if ("fail".equals(code)) {
				String detail = json.optString("detail");
				throw new GroupsException(detail);
			}

		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			throw new GroupsException(e.getMessage());
		}
	}
	
	public static String groupSave(Long domainId,String groupName,String parentId,String desc,String actAsSubjectId){
		String methods = path + "multiDomain/json/manager/groupsave";

		Map<String, String[]> param = new HashMap<String, String[]>();
		param.put("domainId", new String[] { domainId.toString() });
		param.put("name", new String[] { groupName });
		param.put("desc", new String[] { desc });
		param.put("parentId", new String[] { parentId });
		param.put("actAsSubjectId",new String[]{actAsSubjectId});
		try {
			String result = HttpClientUtil.post_v2(methods, methods, param);
			JSONObject json = JSONObject.fromObject(result);
			String code = json.getString("code");
			if ("fail".equals(code)) {
				String detail = json.optString("detail");
				throw new GroupsException(detail);
			}
			String newGroupId = json.getJSONObject("data").getString("id");
			return newGroupId;
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			throw new GroupsException(e.getMessage());
		}
	}
	
	public static void groupDelete(Long domainId, String groupId, String actAsSubjectId) {
		String methods = path + "multiDomain/json/manager/groupdelete";

		Map<String, String[]> param = new HashMap<String, String[]>();
		param.put("domainId", new String[] { domainId.toString() });
		param.put("groupId", new String[] { groupId });
		param.put("actAsSubjectId",new String[]{actAsSubjectId});
		try {
			String result = HttpClientUtil.post_v2(methods, methods, param);
			JSONObject json = JSONObject.fromObject(result);
			String code = json.getString("code");
			if ("fail".equals(code)) {
				String detail = json.optString("detail");
				throw new GroupsException(detail);
			}

		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			throw new GroupsException(e.getMessage());
		}
	}
	
	public static void addMember(Long domainId,String[] subjectIds, String groupId,String actAsSubjectId){
		String methods = path + "multiDomain/json/manager/addmember";

		Map<String, String[]> param = new HashMap<String, String[]>();
		param.put("domainId", new String[] { domainId.toString() });
		param.put("groupId", new String[] { groupId });
		param.put("subjectIds", subjectIds);
		param.put("actAsSubjectId",new String[]{actAsSubjectId});
		try {
			String result = HttpClientUtil.post_v2(methods, methods, param);
			JSONObject json = JSONObject.fromObject(result);
			String code = json.getString("code");
			if ("fail".equals(code)) {
				String detail = json.optString("detail");
				throw new GroupsException(detail);
			}

		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			throw new GroupsException(e.getMessage());
		}
		
	}
	
	public static void deleteMember(Long domainId,String[] subjectIds, String groupId,String actAsSubjectId){
		String methods = path + "multiDomain/json/manager/deletemember";

		Map<String, String[]> param = new HashMap<String, String[]>();
		param.put("domainId", new String[] { domainId.toString() });
		param.put("groupId", new String[] { groupId });
		param.put("subjectIds", subjectIds);
		param.put("actAsSubjectId",new String[]{actAsSubjectId});
		try {
			String result = HttpClientUtil.post_v2(methods, methods, param);
			JSONObject json = JSONObject.fromObject(result);
			String code = json.getString("code");
			if ("fail".equals(code)) {
				String detail = json.optString("detail");
				throw new GroupsException(detail);
			}

		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			throw new GroupsException(e.getMessage());
		}
	}

}
