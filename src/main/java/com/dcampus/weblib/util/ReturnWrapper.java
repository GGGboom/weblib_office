package com.dcampus.weblib.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import com.dcampus.common.config.PropertyUtil;
import com.dcampus.common.util.HTML;
import com.dcampus.common.util.JS;
import com.dcampus.sys.entity.User;
import com.dcampus.weblib.entity.Domain;
import com.dcampus.weblib.entity.DomainFolder;
import com.dcampus.weblib.entity.DomainPermission;
import com.dcampus.weblib.entity.Member;
import com.dcampus.weblib.vo.FolderVo;
import com.dcampus.weblib.vo.ManagerVo;

/**
 * 复杂反复调用的返回数据包装
 * @author patrick
 *
 */
public class ReturnWrapper {
	/**
	 * getUsers返回数据包装器
	 * @param userBaseBeans
	 * @param memberBeans
	 * @param exceptionList
	 * @return
	 */
	public static String getUsersWrapper(User[] userBaseBeans, Member[] memberBeans,List<String> exceptionList) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("{\"members\":[");
		for (int i = 0; i < memberBeans.length; i++) {
			User bean = userBaseBeans[i];
			String userName = bean.getName();
			Member memberBean = memberBeans[i];
			String name0 = null;
			String uuid = memberBean.getName();
			name0 = memberBean.getName();
			buffer.append("{");
			buffer.append("\"id\":").append(memberBean.getId()).append(",");
			buffer.append("\"text\":\"").append(JS.quote(HTML.escape(name0))).append("\",");
			buffer.append("\"type\":\"").append(memberBean.getMemberType()).append("\",");
			buffer.append("\"company\":\"").append(JS.quote(HTML.escape(bean.getCompany()))).append("\",");
			buffer.append("\"department\":\"").append(JS.quote(HTML.escape(bean.getDepartment()))).append("\",");
			buffer.append("\"position\":\"").append(JS.quote(HTML.escape(bean.getPosition()))).append("\",");
			buffer.append("\"email\":\"").append(JS.quote(HTML.escape(bean.getEmail()))).append("\",");
			buffer.append("\"im\":\"").append(JS.quote(HTML.escape(bean.getIm()))).append("\",");
			buffer.append("\"phone\":\"").append(JS.quote(HTML.escape(bean.getPhone()))).append("\",");
			buffer.append("\"mobile\":\"").append(JS.quote(HTML.escape(bean.getMobile()))).append("\",");
			String url = bean.getPhoto();
			if(url==null||url.length()==0){
				url = "";
			}
			else{
				url=PropertyUtil.getMemberPicFolderPath()+url;
			}
			
			buffer.append("\"picUrl\":\"").append(JS.quote(HTML.escape(url))).append("\",");
			buffer.append("\"status\":\"").append(bean.getUserbaseStatus()).append("\",");
			buffer.append("\"fullName\":\"").append(JS.quote(HTML.escape(userName))).append("\"");
			buffer.append("},");
		}
		if (memberBeans.length > 0)
			buffer.setLength(buffer.length() - 1);
		buffer.append("],");
		buffer.append("\"exceptionList\":[");
		if (exceptionList != null) {
			for (int i = 0; i < exceptionList.size(); i++) {
				buffer.append("\"").append(exceptionList.get(i)).append("\",");
			}

			if (exceptionList.size()> 0)
				buffer.setLength(buffer.length() - 1);
		}
		buffer.append("]}");
		return buffer.toString();
	}

	/**
	 * getUsers返回带分页功能的数据包装器
	 * @param userBaseBeans
	 * @param memberBeans
	 * @param exceptionList
	 * @return
	 */
	public static String getUsersWrapperwithLimit(User[] userBaseBeans, Member[] memberBeans,int start,int limit,List<String> exceptionList) {
		int count = 0;
		StringBuffer buffer = new StringBuffer();
		buffer.append("{\"totalMemberCount\":").append(memberBeans.length).append(",");
		buffer.append("\"members\":[");
		if(start>=memberBeans.length){
			buffer.append("]}");
			return buffer.toString();
		}
		for (int i = start; i < (memberBeans.length>(start+limit)?(start+limit):memberBeans.length); i++) {
			User bean = userBaseBeans[i];
			String userName = bean.getName();
			Member memberBean = memberBeans[i];
			String name0 = null;
			String uuid = memberBean.getName();
			name0 = memberBean.getName();
			buffer.append("{");
			buffer.append("\"id\":").append(memberBean.getId()).append(",");
			buffer.append("\"text\":\"").append(JS.quote(HTML.escape(name0))).append("\",");
			buffer.append("\"type\":\"").append(memberBean.getMemberType()).append("\",");
			buffer.append("\"company\":\"").append(JS.quote(HTML.escape(bean.getCompany()))).append("\",");
			buffer.append("\"department\":\"").append(JS.quote(HTML.escape(bean.getDepartment()))).append("\",");
			buffer.append("\"position\":\"").append(JS.quote(HTML.escape(bean.getPosition()))).append("\",");
			buffer.append("\"email\":\"").append(JS.quote(HTML.escape(bean.getEmail()))).append("\",");
			buffer.append("\"im\":\"").append(JS.quote(HTML.escape(bean.getIm()))).append("\",");
			buffer.append("\"phone\":\"").append(JS.quote(HTML.escape(bean.getPhone()))).append("\",");
			buffer.append("\"mobile\":\"").append(JS.quote(HTML.escape(bean.getMobile()))).append("\",");
			String url = bean.getPhoto();
			if(url==null||url.length()==0){
				url = "";
			}
			else{
				url=PropertyUtil.getMemberPicFolderPath()+url;
			}

			buffer.append("\"picUrl\":\"").append(JS.quote(HTML.escape(url))).append("\",");
			buffer.append("\"status\":\"").append(bean.getUserbaseStatus()).append("\",");
			buffer.append("\"fullName\":\"").append(JS.quote(HTML.escape(userName))).append("\"");
			buffer.append("},");
			count++;
		}
		if (count > 0)
			buffer.setLength(buffer.length() - 1);
		buffer.append("],");
		buffer.append("\"exceptionList\":[");
		if (exceptionList != null) {
			for (int i = 0; i < exceptionList.size(); i++) {
				buffer.append("\"").append(exceptionList.get(i)).append("\",");
			}

			if (exceptionList.size()> 0)
				buffer.setLength(buffer.length() - 1);
		}
		buffer.append("]}");
		return buffer.toString();
	}
	
	/**
	 * getMembersTree返回数据包装器
	 * @param memberBeans
	 * @param isLeafs
	 * @param isSystem
	 * @return
	 */
//	public static String getMembersTreeWrapper(Member[] memberBeans, boolean[] isLeafs, boolean[] isSystem) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("{\"members\":[");
//		for (int i = 0; i < memberBeans.length; i++) {
//			Member memberBean = memberBeans[i];
//			String name = null;
//			String uuid = memberBean.getName();
//			if (memberBean.getMemberType().equals(Member.MEMBER_TYPE_PERSON)) {
//				name = memberBean.getName();
//			} else {
//				name = memberBean.getSignature();
//			}
//			String[] spiltDisplayName = name.split(":");
//			String shortName = spiltDisplayName[spiltDisplayName.length - 1];
//			sb.append("{");
//			sb.append("\"id\":").append(memberBean.getId()).append(",");
//			sb.append("\"text\":\"").append(JS.quote(HTML.escape(shortName))).append("\",");
//			sb.append("\"leaf\":").append(memberBean.getIsLeaf()).append(",");
//			sb.append("\"type\":\"").append(memberBean.getMemberType()).append("\",");
//			sb.append("\"fullName\":\"").append(JS.quote(HTML.escape(uuid))).append("\",");
//			sb.append("\"system\":").append(isSystem[i]);
//			sb.append("},");
//		}
//		if (memberBeans.length > 0)
//			sb.setLength(sb.length() - 1);
//		sb.append("]}");
//	return sb.toString();
//	}
	
	
	/**
	 * getMembers返回数据包装器
	 * @param memberBeans
	 * @param isLeafs
	 * @param isSystem
	 * @return
	 */
	public static String getMembersTreeWrapper(Member[] memberBeans, boolean[] isLeafs, boolean[] isSystem) {
		StringBuffer text = new StringBuffer();
		StringBuffer folder = new StringBuffer();
		StringBuffer team = new StringBuffer();
		StringBuffer member = new StringBuffer();

		if(memberBeans!=null) {
			for (int i = 0; i < memberBeans.length; i++) {
				Member memberBean = memberBeans[i];
				if (memberBean.getMemberType().equals(Member.MEMBER_TYPE_FOLDER)) {
					String name = null;
					String uuid = memberBean.getName();
					name = memberBean.getSignature();
					String[] spiltDisplayName = name.split(":");
					String shortName = spiltDisplayName[spiltDisplayName.length - 1];
					folder.append("{");
					folder.append("\"id\":").append(memberBean.getId()).append(",");
					folder.append("\"text\":\"").append(JS.quote(HTML.escape(shortName))).append("\",");
					folder.append("\"leaf\":").append(memberBean.getIsLeaf()).append(",");
					folder.append("\"type\":\"").append(memberBean.getMemberType()).append("\",");
					folder.append("\"fullName\":\"").append(JS.quote(HTML.escape(uuid))).append("\",");
					folder.append("\"system\":").append(isSystem[i]).append(",");
					folder.append("\"lastModified\":\"").append(memberBean.getLastModified()).append("\"");
					folder.append("},");

				} else if (memberBean.getMemberType().equals(Member.MEMBER_TYPE_TEAM)) {
					String name = null;
					String uuid = memberBean.getName();
					name = memberBean.getSignature();
					String[] spiltDisplayName = name.split(":");
					String shortName = spiltDisplayName[spiltDisplayName.length - 1];
					team.append("{");
					team.append("\"id\":").append(memberBean.getId()).append(",");
					team.append("\"text\":\"").append(JS.quote(HTML.escape(shortName))).append("\",");
					/* team.append("\"leaf\":").append(memberBean.getIsLeaf()).append(","); */
					team.append("\"type\":\"").append(memberBean.getMemberType()).append("\",");
					team.append("\"fullName\":\"").append(JS.quote(HTML.escape(uuid))).append("\",");
					team.append("\"system\":").append(isSystem[i]).append(",");
					;
					team.append("\"lastModified\":\"").append(memberBean.getLastModified()).append("\"");
					team.append("},");
				} else {
					String name = null;
					String uuid = memberBean.getName();
					if (memberBean.getMemberType().equals(Member.MEMBER_TYPE_PERSON)) {
						name = memberBean.getName();
					} else {
						name = memberBean.getSignature();
					}
					String[] spiltDisplayName = name.split(":");
					String shortName = spiltDisplayName[spiltDisplayName.length - 1];
					member.append("{");
					member.append("\"id\":").append(memberBean.getId()).append(",");
					member.append("\"text\":\"").append(JS.quote(HTML.escape(shortName))).append("\",");
					member.append("\"leaf\":").append(memberBean.getIsLeaf()).append(",");
					member.append("\"type\":\"").append(memberBean.getMemberType()).append("\",");
					member.append("\"fullName\":\"").append(JS.quote(HTML.escape(uuid))).append("\",");
					member.append("\"system\":").append(isSystem[i]);
					member.append("},");
				}


			}
		}
		if (folder.length() > 0)
			folder.setLength(folder.length() - 1);
		if (team.length() > 0)
			team.setLength(team.length() - 1);
		if (member.length() > 0)
			member.setLength(member.length() - 1);
		
		text.append("{\"folder\":[").append(folder).append("],");
		text.append("\"team\":[").append(team).append("],");
		text.append("\"member\":[").append(member).append("]}");
	return text.toString();
	}
	
	/**
	 * getPermissions返回数据包装
	 * @param permissions
	 * @param checkArr
	 * @return
	 */
	public static String getPermissionsByRoleIdWrapper(List<DomainPermission> permissions, boolean[] checkArr) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		buffer.append("\"permissions\":[");
		if (permissions != null) {
			for (int i=0;i<permissions.size();i++) {
				DomainPermission p = permissions.get(i);
				boolean isChecked = checkArr[i];
				buffer.append("{");
				buffer.append("\"id\":").append(p.getId()).append(",");

				
				buffer.append("\"name\":\"").append(p.getName()).append("\",");

				buffer.append("\"desc\":\"").append(p.getDesc()).append("\",");
				buffer.append("\"isChecked\":").append(isChecked);

				buffer.append("},");
			}
			if (permissions.size() > 0) {
				buffer.setLength(buffer.length() - 1);
			}
		}
		buffer.append("]}");
		return buffer.toString();
	}
	
	/**
	 * 新建域返回信息
	 * @param domain 
	 * @param dfs
	 * @param members
	 * @param managers
	 * @param creator
	 * @return
	 */
	public  static String createDomainOnFolderWrapper(Domain domain, List<DomainFolder> dfs, List<Member> members, List<User> managers, Member creator) {
		Map<String, Object> infoMap = new HashMap<String, Object>();
		infoMap.put("type", "success");
		infoMap.put("code", "200");
		infoMap.put("detail", "ok");
		infoMap.put("success", true);
		infoMap.put("domainId", domain.getId());
		infoMap.put("domainName", domain.getDomainName());
		infoMap.put("desc", domain.getDesc());
		infoMap.put("creatorId", creator==null?0:creator.getId());
		infoMap.put("creatorAccount", creator==null?"系统":creator.getAccount());
		infoMap.put("createDate", domain.getCreateDate());
		if(dfs!=null){
			List<JSONObject> folderVos = new ArrayList<JSONObject>();
			for(DomainFolder df: dfs){
				FolderVo vo = new FolderVo(df.getFolder().getId(),df.getFolder().getSignature());
				JSONObject json = JSONObject.fromObject(vo);
				folderVos.add(json);
			}
			infoMap.put("associatedFolders",folderVos);
		}
		if(managers!=null&&members!=null){
			List<JSONObject> managerVos = new ArrayList<JSONObject>();
			for(int i = 0;i<managers.size();i++){
				User user = managers.get(i);
				Member member = members.get(i);
				ManagerVo vo = new ManagerVo(member.getId(),user.getAccount(),user.getName());
				JSONObject json = JSONObject.fromObject(vo);
				managerVos.add(json);
			}
			infoMap.put("managers",managerVos);
		}

		JSONObject json = JSONObject.fromObject(infoMap);
		return json.toString();
	}
	
	/**
	 * 获取所管理的域所包含的模块结构返回包装
	 * @param memberBeans
	 * @param isSystem
	 * @param domainMap
	 * @return
	 */
	public  static String getManagerDomainWrapper(Member[] memberBeans, boolean[] isSystem, Map<Long,Domain> domainMap) {
		StringBuffer text = new StringBuffer();
		StringBuffer folder = new StringBuffer();
		StringBuffer team = new StringBuffer();
		StringBuffer member = new StringBuffer();
		int totalFolderCount = 0;
		int totalGroupCount = 0;
		int totalMemberCount = 0;
		for (int i = 0; i < memberBeans.length; i++) {
			Member memberBean = memberBeans[i];	
			if(memberBean.getMemberType().equals(Member.MEMBER_TYPE_FOLDER)){
				totalFolderCount++;
				String name = null;
				String uuid = memberBean.getName();
				name = memberBean.getSignature();
				String[] spiltDisplayName = name.split(":");
				String shortName = spiltDisplayName[spiltDisplayName.length - 1];
				folder.append("{");
				folder.append("\"id\":").append(memberBean.getId()).append(",");
				folder.append("\"text\":\"").append(JS.quote(HTML.escape(shortName))).append("\",");
				folder.append("\"leaf\":").append(memberBean.getIsLeaf()==null?false:memberBean.getIsLeaf()).append(",");
				folder.append("\"from\":\"").append(memberBean.getModifyFrom()==null?"":memberBean.getModifyFrom()).append("\",");
				folder.append("\"type\":\"").append(memberBean.getMemberType()).append("\",");
				folder.append("\"fullName\":\"").append(JS.quote(HTML.escape(uuid))).append("\","); 
				folder.append("\"system\":").append(isSystem[i]);
				Domain domain = domainMap.get(memberBean.getId());
				if(domain!=null){
					folder.append(",\"associated\":true");
					folder.append(",\"domainId\":").append(domain.getId());
					folder.append(",\"domainName\":\"").append(domain.getDomainName()+"\"");
					folder.append(",\"desc\":\"").append(domain.getDesc()+"\"");
				}else{
					folder.append(",\"associated\":false");
				}
				folder.append("},");	
			}else if(memberBean.getMemberType().equals(Member.MEMBER_TYPE_TEAM)){
				totalGroupCount++;
				String name = null;
				String uuid = memberBean.getName();
				name = memberBean.getSignature();
				String[] spiltDisplayName = name.split(":");
				String shortName = spiltDisplayName[spiltDisplayName.length - 1];
				team.append("{");
				team.append("\"id\":").append(memberBean.getId()).append(",");
				team.append("\"text\":\"").append(JS.quote(HTML.escape(shortName))).append("\",");
				team.append("\"from\":\"").append(memberBean.getModifyFrom()==null?"":memberBean.getModifyFrom()).append("\",");
				team.append("\"type\":\"").append(memberBean.getMemberType()).append("\",");
				team.append("\"fullName\":\"").append(JS.quote(HTML.escape(uuid))).append("\","); 
				team.append("\"system\":").append(isSystem[i]);
				team.append("},");
			}else{
				totalMemberCount++;
				String name = null;
				String uuid = memberBean.getName();
				if (memberBean.getMemberType().equals(Member.MEMBER_TYPE_PERSON)) {
					name = memberBean.getName();
				} else {
					name = memberBean.getSignature();
				}
				String[] spiltDisplayName = name.split(":");
				String shortName = spiltDisplayName[spiltDisplayName.length - 1];
				member.append("{");
				member.append("\"id\":").append(memberBean.getId()).append(",");
				member.append("\"text\":\"").append(JS.quote(HTML.escape(shortName))).append("\",");
				member.append("\"leaf\":").append(memberBean.getIsLeaf()).append(",");
				member.append("\"type\":\"").append(memberBean.getMemberType()).append("\",");
				member.append("\"fullName\":\"").append(JS.quote(HTML.escape(uuid))).append("\",");
				member.append("\"system\":").append(isSystem[i]);
				member.append("},");
			}
		}
		if (folder.length() > 0)
			folder.setLength(folder.length() - 1);
		if (team.length() > 0)
			team.setLength(team.length() - 1);
		if (member.length() > 0)
			member.setLength(member.length() - 1);

		text.append("{\"totalFolderCount\":").append(totalFolderCount).append(",");
		text.append("\"folder\":[").append(folder).append("],");
		text.append("\"totalTeamCount\":").append(totalGroupCount).append(",");
		text.append("\"team\":[").append(team).append("],");
		text.append("\"totalMemberCount\":").append(totalMemberCount).append(",");
		text.append("\"member\":[").append(member).append("]}");
		return text.toString();
	}

	/**
	 * 获取所管理的域所包含的模块结构返回包装,带有分页功能
	 * @param memberBeans
	 * @param isSystem
	 * @param domainMap
	 * @return
	 */
	public  static String getManagerDomainWrapperwithLimit(Member[] memberBeans, boolean[] isSystem, Map<Long,Domain> domainMap,int start,int limit) {
		StringBuffer text = new StringBuffer();
		StringBuffer folder = new StringBuffer();
		StringBuffer team = new StringBuffer();
		StringBuffer member = new StringBuffer();
		int count = 0;
		int totalFolderCount = 0;
		int totalGroupCount = 0;
		int totalMemberCount = 0;
		for (int i = 0; i < memberBeans.length; i++){
			Member memberBean = memberBeans[i];
			if(memberBean.getMemberType().equals(Member.MEMBER_TYPE_FOLDER)){
				totalFolderCount++;
				if(count<start){
					count++;
					continue;
				}
				if(count>=start+limit){
					continue;
				}
				count++;
				String name = null;
				String uuid = memberBean.getName();
				name = memberBean.getSignature();
				String[] spiltDisplayName = name.split(":");
				String shortName = spiltDisplayName[spiltDisplayName.length - 1];
				folder.append("{");
				folder.append("\"id\":").append(memberBean.getId()).append(",");
				folder.append("\"text\":\"").append(JS.quote(HTML.escape(shortName))).append("\",");
				folder.append("\"leaf\":").append(memberBean.getIsLeaf()==null?false:memberBean.getIsLeaf()).append(",");
				folder.append("\"from\":\"").append(memberBean.getModifyFrom()==null?"":memberBean.getModifyFrom()).append("\",");
				folder.append("\"type\":\"").append(memberBean.getMemberType()).append("\",");
				folder.append("\"fullName\":\"").append(JS.quote(HTML.escape(uuid))).append("\",");
				folder.append("\"system\":").append(isSystem[i]);
				Domain domain = domainMap.get(memberBean.getId());
				if(domain!=null){
					folder.append(",\"associated\":true");
					folder.append(",\"domainId\":").append(domain.getId());
					folder.append(",\"domainName\":\"").append(domain.getDomainName()+"\"");
					folder.append(",\"desc\":\"").append(domain.getDesc()+"\"");
				}else{
					folder.append(",\"associated\":false");
				}
				folder.append("},");
			}
		}
		for (int i = 0; i < memberBeans.length; i++) {
			Member memberBean = memberBeans[i];
			if(memberBean.getMemberType().equals(Member.MEMBER_TYPE_TEAM)){
				totalGroupCount++;
				if(count<start){
					count++;
					continue;
				}
				if(count>=start+limit){
					continue;
				}
				count++;
				String name = null;
				String uuid = memberBean.getName();
				name = memberBean.getSignature();
				String[] spiltDisplayName = name.split(":");
				String shortName = spiltDisplayName[spiltDisplayName.length - 1];
				team.append("{");
				team.append("\"id\":").append(memberBean.getId()).append(",");
				team.append("\"text\":\"").append(JS.quote(HTML.escape(shortName))).append("\",");
				team.append("\"from\":\"").append(memberBean.getModifyFrom()==null?"":memberBean.getModifyFrom()).append("\",");
				team.append("\"type\":\"").append(memberBean.getMemberType()).append("\",");
				team.append("\"fullName\":\"").append(JS.quote(HTML.escape(uuid))).append("\",");
				team.append("\"system\":").append(isSystem[i]);
				team.append("},");
			}else if(!memberBean.getMemberType().equals(Member.MEMBER_TYPE_FOLDER)){
				totalMemberCount++;
				String name = null;
				String uuid = memberBean.getName();
				if (memberBean.getMemberType().equals(Member.MEMBER_TYPE_PERSON)) {
					name = memberBean.getName();
				} else {
					name = memberBean.getSignature();
				}
				String[] spiltDisplayName = name.split(":");
				String shortName = spiltDisplayName[spiltDisplayName.length - 1];
				member.append("{");
				member.append("\"id\":").append(memberBean.getId()).append(",");
				member.append("\"text\":\"").append(JS.quote(HTML.escape(shortName))).append("\",");
				member.append("\"leaf\":").append(memberBean.getIsLeaf()).append(",");
				member.append("\"type\":\"").append(memberBean.getMemberType()).append("\",");
				member.append("\"fullName\":\"").append(JS.quote(HTML.escape(uuid))).append("\",");
				member.append("\"system\":").append(isSystem[i]);
				member.append("},");
			}
		}
		if (folder.length() > 0)
			folder.setLength(folder.length() - 1);
		if (team.length() > 0)
			team.setLength(team.length() - 1);
		if (member.length() > 0)
			member.setLength(member.length() - 1);

		text.append("{\"totalFolderCount\":").append(totalFolderCount).append(",");
		text.append("\"folder\":[").append(folder).append("],");
		text.append("\"totalTeamCount\":").append(totalGroupCount).append(",");
		text.append("\"team\":[").append(team).append("],");
		text.append("\"totalMemberCount\":").append(totalMemberCount).append(",");
		text.append("\"member\":[").append(member).append("]}");

		return text.toString();
	}
}
