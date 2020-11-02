package com.dcampus.weblib.web.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import com.dcampus.weblib.service.CollectionService;
import com.dcampus.weblib.service.GrouperService;

@Controller
@RequestMapping(value="/user")
public class CollectionController {
	@Autowired
	private CollectionService collectionService;
	@Autowired
	private GrouperService grouperService;
	@Autowired
	private UserService userService;
	
	/**
	 * 添加收藏
	 * @param ids 收藏对象（用户，组等）id数组
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/addCollection", produces = "application/json; charset=UTF-8")
	public String addCollection(Long[] ids) throws Exception {
		if (ids == null || ids.length == 0) {
			throw new GroupsException("memberIds参数错误");
		}
		for (long id : ids) {
			collectionService.addCollection(id);
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
	
	/**
	 * 删除收藏
	 * @param ids 收藏对象（用户，组等）id数组
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/removeCollection", produces = "application/json; charset=UTF-8")
	public String removeCollection(long[] ids) throws Exception {
		if (ids != null && ids.length > 0) {
			for (long id : ids) {
				collectionService.removeCollection(ids);
			}
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 根据收藏类型获取我的收藏
	 * 不加此参数为全部
	 * @param collectionType  收藏类型
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getMyCollections", produces = "application/json; charset=UTF-8")
	public String getMyCollections(String collectionType) throws Exception {
		List<Member> collections = collectionService.getMyCollections(collectionType);
		List<Member> users = new ArrayList<Member>();
		List<Member> teams = new ArrayList<Member>();
		List<Member> shareTeams = new ArrayList<Member>();
		List<ShareTeamInfo> stInfo = grouperService.getMyShareTeam();
		Set<Long> unique = new HashSet<Long>();
		List<User> userBases = new ArrayList<User>();
		for(ShareTeamInfo sti:stInfo){
			if(!unique.contains(sti.getShareTeam().getId())){
				shareTeams.add(sti.getShareTeam());
				unique.add(sti.getShareTeam().getId());
			}
		}
		for (Member c : collections) {
			if (Member.MEMBER_TYPE_PERSON.equals(c.getMemberType())) {
				User ub = userService.getUserByAccount(c.getAccount());
				userBases.add(ub);
				users.add(c);
			} else if (Member.MEMBER_TYPE_TEAM.equals(c.getMemberType())) {
				teams.add(c);
			} else if (Member.MEMBER_TYPE_SHARETEAM.equals(c.getMemberType())&& !unique.contains(c.getId())) {
				shareTeams.add(c);
				unique.add(c.getId());				
				
			} else {
				continue;
			}
		}

		StringBuffer text = new StringBuffer();
		StringBuffer shareTeam = new StringBuffer();
		StringBuffer team = new StringBuffer();
		StringBuffer user = new StringBuffer();
		
		for(int i = 0;i<teams.size();i++){
			Member t = teams.get(i);
			String name = null;
			String uuid = t.getName();
			name = t.getSignature();
			String[] spiltDisplayName = name.split(":");
			String shortName = spiltDisplayName[spiltDisplayName.length - 1];
			team.append("{");
			team.append("\"id\":").append(t.getId()).append(",");
			team.append("\"text\":\"").append(JS.quote(HTML.escape(shortName))).append("\",");
			team.append("\"type\":\"").append(t.getMemberType()).append("\",");
			team.append("\"fullName\":\"").append(JS.quote(HTML.escape(uuid))).append("\""); 
			team.append("},");
		}
		for(int i = 0;i<shareTeams.size();i++){
			Member st = shareTeams.get(i);
			String shortName = st.getName();
			shareTeam.append("{");
			shareTeam.append("\"id\":").append(st.getId()).append(",");
			shareTeam.append("\"text\":\"").append(JS.quote(HTML.escape(shortName))).append("\",");
			shareTeam.append("\"type\":\"").append(st.getMemberType()).append("\"");
			shareTeam.append("},");
		}
		for(int i = 0;i<users.size();i++){
			Member u = users.get(i);
			User ub = userBases.get(i);
			String userName = ub.getName();
			user.append("{");
			user.append("\"id\":").append(u.getId()).append(",");
			user.append("\"text\":\"").append(
			JS.quote(HTML.escape(userName))).append("\",");
			user.append("\"type\":\"").append(u.getMemberType()).append("\",");
			user.append("\"company\":\"").append(JS.quote(HTML.escape(ub.getCompany()))).append("\",");
			user.append("\"department\":\"").append(JS.quote(HTML.escape(ub.getDepartment()))).append("\",");
			user.append("\"position\":\"").append(JS.quote(HTML.escape(ub.getPosition()))).append("\",");
			user.append("\"email\":\"").append(JS.quote(HTML.escape(ub.getEmail()))).append("\",");
			user.append("\"im\":\"").append(JS.quote(HTML.escape(ub.getIm()))).append("\",");
			user.append("\"phone\":\"").append(JS.quote(HTML.escape(ub.getPhone()))).append("\",");
			user.append("\"mobile\":\"").append(JS.quote(HTML.escape(ub.getMobile()))).append("\",");
			String url = ub.getPhoto();
			if(url==null||url.length()==0){
				url = "";
			}
			else{
				url=PropertyUtil.getMemberPicFolderPath()+url;
			}
			user.append("\"picUrl\":\"").append(JS.quote(HTML.escape(url))).append("\",");
			user.append("\"status\":\"").append(ub.getUserbaseStatus()).append("\",");
			user.append("\"fullName\":\"").append(JS.quote(HTML.escape(userName))).append("\"");
			user.append("},");
		}
		if (team.length() > 0)
			team.setLength(team.length() - 1);
		if (shareTeam.length() > 0)
			shareTeam.setLength(shareTeam.length() - 1);
		if (user.length() > 0)
			user.setLength(user.length() - 1);
		
		text.append("{\"shareTeam\":[").append(shareTeam).append("],");
		text.append("\"team\":[").append(team).append("],");
		text.append("\"member\":[").append(user).append("]}");
		
		return text.toString();
	}

}
