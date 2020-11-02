package com.dcampus.weblib.web.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dcampus.common.config.PropertyUtil;
import com.dcampus.common.util.HTML;
import com.dcampus.common.util.JS;
import com.dcampus.sys.entity.User;
import com.dcampus.sys.service.UserService;
import com.dcampus.weblib.entity.Member;
import com.dcampus.weblib.entity.ShareTeamInfo;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.service.GrouperService;

/**
 * 自建分享组controller
 * @author patrick
 *
 */
@Controller
@RequestMapping(value="/user")
public class ShareTeamController {
	
	@Autowired
	private GrouperService grouperService;
	@Autowired
	private UserService userService;
	
	/**
	 * 创建自建分享组
	 * 同时添加用户到分享组
	 * @param name 自建分享组名字
	 * @param desc 自建分享组描述
	 * @param ids 添加的用户的memberId
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/createShareTeam", produces = "application/json; charset=UTF-8")
	public String createShareTeam(String name, String desc, long[] ids) throws Exception {
		if(name==null||"".equals(name)){
			throw new GroupsException("组名不能为空");
		}
		ShareTeamInfo sti = grouperService.createShareTeam(name,desc);
		if(ids!=null&&ids.length>0) {
			grouperService.addMemberToShareTeam(sti.getShareTeam().getId(), ids);
		}
		
		StringBuffer sb = new StringBuffer();
		String shortName = sti.getShareTeam().getName();
		sb.append("{");
		sb.append("\"id\":").append(sti.getShareTeam().getId()).append(",");
		sb.append("\"text\":\"").append(JS.quote(HTML.escape(shortName))).append("\",");
		sb.append("\"creatorName\":\"").append(sti.getCreator().getName()).append("\",");
		sb.append("\"creatorId\":").append(sti.getCreator().getId()).append(",");
		sb.append("\"creatorTimeStamp\":").append(sti.getCreateDate().getTime()).append(",");
		sb.append("\"type\":\"shareteam\"");
		sb.append("}");
		return sb.toString();
	}
	
	/**
	 * 批量添加用户到自建分享组
	 * @param shareTeamId 分享组id
	 * @param ids 待添加用户id数组
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/addMemberToShareTeam", produces = "application/json; charset=UTF-8")
	public String addMemberToShareTeam(Long shareTeamId, Long[] ids) throws Exception {
		if(shareTeamId == null || shareTeamId == null) {
			throw new GroupsException("分享组id不能为空！");
		}
		if(ids != null && ids.length > 0) {
			for (long id : ids) {
				grouperService.addMemberToShareTeam(shareTeamId,id);
			}
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 根据自建分享组id获取成员
	 * @param shareTeamId 自建分享组id
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getShareTeamMember", produces = "application/json; charset=UTF-8")
	public String getShareTeamMember(Long shareTeamId) throws Exception {
		if (shareTeamId == null || shareTeamId == 0L) {
			throw new GroupsException("自建分享组id不能为空");
		}
		List<Member> members = grouperService.getShareTeamMember(shareTeamId);
		List<User> userBases = new ArrayList<User>();
		for(Member m:members){
			userBases.add(userService.getUserByAccount(m.getAccount()));
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append("{\"members\":[");
		for (int i = 0; i < members.size(); i++) {
			User bean = userBases.get(i);
			String userName = bean.getName();
			Member memberBean = members.get(i);
			String name = null;
			String uuid = memberBean.getName();
			name = memberBean.getName();
			sb.append("{");
			sb.append("\"id\":").append(memberBean.getId()).append(",");
			sb.append("\"text\":\"").append(JS.quote(HTML.escape(name))).append("\",");
			sb.append("\"type\":\"").append(memberBean.getMemberType()).append("\",");
			sb.append("\"company\":\"").append(JS.quote(HTML.escape(bean.getCompany()))).append("\",");
			sb.append("\"department\":\"").append(JS.quote(HTML.escape(bean.getDepartment()))).append("\",");
			sb.append("\"position\":\"").append(JS.quote(HTML.escape(bean.getPosition()))).append("\",");
			sb.append("\"email\":\"").append(JS.quote(HTML.escape(bean.getEmail()))).append("\",");
			sb.append("\"im\":\"").append(JS.quote(HTML.escape(bean.getIm()))).append("\",");
			sb.append("\"phone\":\"").append(JS.quote(HTML.escape(bean.getPhone()))).append("\",");
			sb.append("\"mobile\":\"").append(JS.quote(HTML.escape(bean.getMobile()))).append("\",");
			String url = bean.getPhoto();
			if(url==null||url.length()==0){
				url = "";
			}
			else{
				url=PropertyUtil.getMemberPicFolderPath()+url;
			}
			sb.append("\"picUrl\":\"").append(JS.quote(HTML.escape(url))).append("\",");
			sb.append("\"status\":\"").append(bean.getUserbaseStatus()).append("\",");
			sb.append("\"fullName\":\"").append(JS.quote(HTML.escape(userName))).append("\"");
			sb.append("},");
		}
		if (members.size() > 0)
			sb.setLength(sb.length() - 1);
		sb.append("]}");
		return sb.toString();
	}

	/**
	 * 根据id从自建分享组删除成员
	 * @param shareTeamId 自建分享组id
	 * @param ids 待删除成员id数组
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/removeMemberFromShareTeam", produces = "application/json; charset=UTF-8")
	public String removeMemberFromShareTeam(Long shareTeamId, long[] ids) throws Exception {
		if(shareTeamId == null || shareTeamId ==0L) {
			throw new GroupsException("自建分享组id不能为空");
		}
		grouperService.removeMemberFromShareTeam(shareTeamId,ids);
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 根据id删除自建分享组
	 * @param shareTeamId 自建分享组id
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/deleteShareTeam", produces = "application/json; charset=UTF-8")
	public String deleteShareTeam(Long shareTeamId) throws Exception {
		if(shareTeamId != null && shareTeamId !=0L) {
			grouperService.deleteShareTeam(shareTeamId);
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 获得当前登陆者的自建分享组
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getMyShareTeam", produces = "application/json; charset=UTF-8")
	public String getMyShareTeam() throws Exception {
		List<ShareTeamInfo> stis = grouperService.getMyShareTeam();
		
		StringBuffer sb = new StringBuffer();
		sb.append("{\"shareTeam\":[");
		for(ShareTeamInfo sti:stis){
			String shortName = sti.getShareTeam().getName();
			sb.append("{");
			sb.append("\"id\":").append(sti.getShareTeam().getId()).append(",");
			sb.append("\"text\":\"").append(
					JS.quote(HTML.escape(shortName))).append(
					"\",");
			sb.append("\"creatorName\":\"").append(sti.getCreator().getName()).append("\",");
			sb.append("\"creatorId\":").append(sti.getCreator().getId()).append(",");
			sb.append("\"creatorTimeStamp\":").append(sti.getCreateDate().getTime()).append(",");
			sb.append("\"type\":\"shareteam\"");
			sb.append("},");
		}
		if(stis.size()>0){
			sb.setLength(sb.length()-1);
		}
		sb.append("]}");
		return sb.toString();
	}
}
