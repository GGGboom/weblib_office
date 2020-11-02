package com.dcampus.weblib.web.action;

import java.util.*;

import javax.servlet.http.HttpServletResponse;

import com.dcampus.weblib.entity.*;
import com.dcampus.weblib.service.*;
import com.dcampus.weblib.service.permission.IPermission;
import com.dcampus.weblib.service.permission.PermCollection;
import com.dcampus.weblib.util.PermissionParseUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dcampus.common.config.PropertyUtil;
import com.dcampus.common.config.ResourceProperty;
import com.dcampus.common.util.HTML;
import com.dcampus.common.util.JS;
import com.dcampus.common.web.BaseController;
import com.dcampus.sys.entity.User;
import com.dcampus.sys.service.UserService;
import com.dcampus.sys.util.UserUtils;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.util.CurrentUserWrap;
import com.dcampus.weblib.util.ReturnWrapper;
import com.sun.org.glassfish.gmbal.NameValue;

/**
 *
 * @author patrick
 *
 */
@Controller
@RequestMapping(value = "/grouper")
public class GrouperController extends BaseController{


	@Autowired
	private UserService userService;

	@Autowired
	private PermissionService permService;

	@Autowired
	private ResourceService resourceService;

	@Autowired
	private GrouperService grouperService;

	@Autowired
	private LogService logService;
	@Autowired
	private DomainService domainService;
	@Autowired
	private RoleService roleService;

	@Autowired
	private GroupService groupService;

	private boolean getGrouperRootBySystemOrRootDomainManager = false;

	/**
	 * 获得用户组织树
	 * @param id
	 * @param folderOnly
	 * @param withoutLeaf
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/grouperTree_v2", produces = "application/json; charset=UTF-8")
	public String grouperTree_v2(Long id, Boolean folderOnly, Boolean withoutLeaf,HttpServletResponse response) throws Exception {
//        if(id==0&&PropertyUtil.getEnableDomainModule()) {
//            return this.domainTree(id, folderOnly,  withoutLeaf,  response);
//        }
		id = id == null ? 0L : id;
		folderOnly = folderOnly == null ? false : folderOnly;
		withoutLeaf = withoutLeaf == null ? false : withoutLeaf;

		Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
		Long memberId = currentUserWrap.getMemberId();
		Member[] memberBeans = null;
		//判断是否有子节点
		boolean[] isLeafs = null;
		// 判断是否是系统生成的组织和组
		boolean[] isSystem = null;

		String type = Member.MEMBER_TYPE_FOLDER;
		// 先判断是否根节点
		if (id != 0) {
			Member iMember = grouperService.getMemberById(id);
			type = iMember.getMemberType();
		}
		// 判断节点类型为team,还是folder
		if (Member.MEMBER_TYPE_TEAM.equals(type)) {
			if (id == 0) {
				memberBeans = new Member[0];
			} else {
				memberBeans = grouperService.getMembersInTeam(id);
			}
			List<User> userList = new ArrayList<User>();
			List<Member> memberList = new ArrayList<Member>();
			List<String> exceptionList = new ArrayList<String>();
			for (int i = 0; i < memberBeans.length; i++) {
				if (memberBeans[i].getAccount()!=null) {
					userList.add(userService.getUserByAccount(memberBeans[i].getAccount()));
					memberList.add(memberBeans[i]);
				} else {
					System.out.println("Get Account Exception, User donot Exist! : " + memberBeans[i].getName());
					exceptionList.add(memberBeans[i].getName());
				}
			}
			User[] userBaseBeans = userList.toArray(new User[userList.size()]);
			memberBeans = memberList.toArray(new Member[memberList.size()]);
			return ReturnWrapper.getUsersWrapper(userBaseBeans, memberBeans, exceptionList);
		} else if (Member.MEMBER_TYPE_FOLDER.equals(type)) {
			// 判断是否是只显示folder
			if (folderOnly) {
				memberBeans = grouperService.getFolderByParent(id,withoutLeaf);
				isLeafs = new boolean[memberBeans.length];

				isSystem = new boolean[memberBeans.length];
				for (int i = 0; i < memberBeans.length; i++) {
					Member m = memberBeans[i];
					boolean ifLeaf = grouperService.hasSubFolder(m.getId());
					isLeafs[i] = !ifLeaf;
					try {
						grouperService.checkDefaultFolder(m.getId());
					} catch (Exception e) {
						isSystem[i] = true;
					}
				}
			} else {

				if (id == 0) {
					id = grouperService.getMemberById(memberId).getFolderId();
				}
				memberBeans = grouperService.getFolderAndTeamByParent_v2(id,withoutLeaf);
				isLeafs = new boolean[memberBeans.length];
				isSystem = new boolean[memberBeans.length];
				for (int i = 0; i < memberBeans.length; i++) {
					Member m = memberBeans[i];
					if (Member.MEMBER_TYPE_FOLDER.equals(m.getMemberType())) {
						try {
							grouperService.checkDefaultFolder(m.getSignature());
						} catch (Exception e) {
							isSystem[i] = true;
						}
					} else if (Member.MEMBER_TYPE_TEAM.equals(m.getMemberType())) {
						try {
							grouperService.checkDefaultTeam(m.getSignature());
						} catch (Exception e) {
							isSystem[i] = true;
						}
					}
				}
			}
			return ReturnWrapper.getMembersTreeWrapper(memberBeans, isLeafs, isSystem);
		} else {
			throw new GroupsException("该id对应的member不是组或者组织！");
		}
	}

	/**
	 * 获得用户组织树
	 * @param id
	 * @param folderOnly
	 * @param withoutLeaf
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/grouperTree", produces = "application/json; charset=UTF-8")
	public String grouperTree(Long id, Boolean folderOnly, Boolean withoutLeaf, HttpServletResponse response) throws Exception {
//        if(id==0&&!getGrouperRootBySystemOrRootDomainManager&&PropertyUtil.getEnableDomainModule()) {
//            return this.domainTree(id, folderOnly,  withoutLeaf,  response);
//        }
		id = id == null ? 0L : id;
		folderOnly = folderOnly == null ? false : folderOnly;
		withoutLeaf = withoutLeaf == null ? false : withoutLeaf;

		Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
		Long memberId = currentUserWrap.getMemberId();
		StringBuffer buffer = new StringBuffer();

		Member[] memberBeans = null;
		//判断是否有子节点
		boolean[] isLeafs = null;
		// 判断是否是系统生成的组织和组
		boolean[] isSystem = null;

		String type = Member.MEMBER_TYPE_FOLDER;
		// 先判断是否根节点
		if (id != 0) {
			Member iMember = grouperService.getMemberById(id);
			type = iMember.getMemberType();
		}
		// 判断节点类型为team,还是folder
		if (Member.MEMBER_TYPE_TEAM.equals(type)) {
			if (id == 0) {
				memberBeans = new Member[0];
			} else {
				memberBeans = grouperService.getMembersInTeam(id);
			}
			List<User> userList = new ArrayList<User>();
			List<Member> memberList = new ArrayList<Member>();
			List<String> exceptionList = new ArrayList<String>();
			for (int i = 0; i < memberBeans.length; i++) {
				if (memberBeans[i].getAccount()!=null) {
					userList.add(userService.getUserByAccount(memberBeans[i].getAccount()));
					memberList.add(memberBeans[i]);
				} else {
					System.out.println("Get Account Exception, User donot Exist! : " + memberBeans[i].getName());
					exceptionList.add(memberBeans[i].getName());
				}
			}
			User[] userBaseBeans = userList.toArray(new User[userList.size()]);
			memberBeans = memberList.toArray(new Member[memberList.size()]);
			/////return buffer
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
		} else if (Member.MEMBER_TYPE_FOLDER.equals(type)) {
			// 判断是否是只显示folder
			if (folderOnly) {
				memberBeans = grouperService.getFolderByParent(id,withoutLeaf);
				isLeafs = new boolean[memberBeans.length];

				isSystem = new boolean[memberBeans.length];
				for (int i = 0; i < memberBeans.length; i++) {
					Member m = memberBeans[i];
					boolean ifLeaf = grouperService.hasSubFolder(m.getId());
					isLeafs[i] = !ifLeaf;
					try {
						grouperService.checkDefaultFolder(m.getId());
					} catch (Exception e) {
						isSystem[i] = true;
					}
				}
			} else {

				if (id == 0) {
					id = grouperService.getMemberById(memberId).getFolderId();
				}
				memberBeans = grouperService.getFolderAndTeamByParent_v2(id,withoutLeaf);
				isLeafs = new boolean[memberBeans.length];
				isSystem = new boolean[memberBeans.length];
				for (int i = 0; i < memberBeans.length; i++) {
					Member m = memberBeans[i];
					if (Member.MEMBER_TYPE_FOLDER.equals(m.getMemberType())) {
						try {
							grouperService.checkDefaultFolder(m.getSignature());
						} catch (Exception e) {
							isSystem[i] = true;
						}
					} else if (Member.MEMBER_TYPE_TEAM.equals(m.getMemberType())) {
						try {
							grouperService.checkDefaultTeam(m.getSignature());
						} catch (Exception e) {
							isSystem[i] = true;
						}
					}
				}
			}

			buffer.append("{\"members\":[");
			for (int i = 0; i < memberBeans.length; i++) {
				Member memberBean = memberBeans[i];
				String name = null;
				String uuid = memberBean.getName();
				if (memberBean.getMemberType().equals(Member.MEMBER_TYPE_PERSON)) {
					name = memberBean.getName();
				} else {
					name = memberBean.getSignature();
				}
				String[] spiltDisplayName = name.split(":");
				String shortName = spiltDisplayName[spiltDisplayName.length - 1];
				buffer.append("{");
				buffer.append("\"id\":").append(memberBean.getId()).append(",");
				buffer.append("\"text\":\"").append(JS.quote(HTML.escape(shortName))).append("\",");
				buffer.append("\"leaf\":").append(memberBean.getIsLeaf()).append(",");
				buffer.append("\"type\":\"").append(memberBean.getMemberType()).append("\",");
				buffer.append("\"fullName\":\"").append(JS.quote(HTML.escape(uuid))).append("\",");
				buffer.append("\"system\":").append(isSystem[i]);
				buffer.append("},");
			}
			if (memberBeans.length > 0)
				buffer.setLength(buffer.length() - 1);
			buffer.append("]}");
			response.setHeader("Cache_Control", "max-age=10");
		}
		return buffer.toString();
	}

	/**
	 * domaintree返回的数据格式中是member不是members
	 * @param id
	 * @param folderOnly
	 * @param withoutLeaf
	 * @param response
	 * @return
	 * @throws Exception
	 */
	private String grouperTree_domain(Long id, Boolean folderOnly, Boolean withoutLeaf, HttpServletResponse response) throws Exception {
//        if(id==0&&!getGrouperRootBySystemOrRootDomainManager&&PropertyUtil.getEnableDomainModule()) {
//            return this.domainTree(id, folderOnly,  withoutLeaf,  response);
//        }
		id = id == null ? 0L : id;
		folderOnly = folderOnly == null ? false : folderOnly;
		withoutLeaf = withoutLeaf == null ? false : withoutLeaf;

		Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
		Long memberId = currentUserWrap.getMemberId();
		StringBuffer buffer = new StringBuffer();

		Member[] memberBeans = null;
		//判断是否有子节点
		boolean[] isLeafs = null;
		// 判断是否是系统生成的组织和组
		boolean[] isSystem = null;

		String type = Member.MEMBER_TYPE_FOLDER;
		// 先判断是否根节点
		if (id != 0) {
			Member iMember = grouperService.getMemberById(id);
			type = iMember.getMemberType();
		}
		// 判断节点类型为team,还是folder
		if (Member.MEMBER_TYPE_TEAM.equals(type)) {
			if (id == 0) {
				memberBeans = new Member[0];
			} else {
				memberBeans = grouperService.getMembersInTeam(id);
			}
			List<User> userList = new ArrayList<User>();
			List<Member> memberList = new ArrayList<Member>();
			List<String> exceptionList = new ArrayList<String>();
			for (int i = 0; i < memberBeans.length; i++) {
				if (memberBeans[i].getAccount()!=null) {
					userList.add(userService.getUserByAccount(memberBeans[i].getAccount()));
					memberList.add(memberBeans[i]);
				} else {
					System.out.println("Get Account Exception, User donot Exist! : " + memberBeans[i].getName());
					exceptionList.add(memberBeans[i].getName());
				}
			}
			User[] userBaseBeans = userList.toArray(new User[userList.size()]);
			memberBeans = memberList.toArray(new Member[memberList.size()]);
			/////return buffer
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
		} else if (Member.MEMBER_TYPE_FOLDER.equals(type)) {
			// 判断是否是只显示folder
			if (folderOnly) {
				memberBeans = grouperService.getFolderByParent(id,withoutLeaf);
				isLeafs = new boolean[memberBeans.length];

				isSystem = new boolean[memberBeans.length];
				for (int i = 0; i < memberBeans.length; i++) {
					Member m = memberBeans[i];
					boolean ifLeaf = grouperService.hasSubFolder(m.getId());
					isLeafs[i] = !ifLeaf;
					try {
						grouperService.checkDefaultFolder(m.getId());
					} catch (Exception e) {
						isSystem[i] = true;
					}
				}
			} else {

				if (id == 0) {
					id = grouperService.getMemberById(memberId).getFolderId();
				}
				memberBeans = grouperService.getFolderAndTeamByParent_v2(id,withoutLeaf);
				isLeafs = new boolean[memberBeans.length];
				isSystem = new boolean[memberBeans.length];
				for (int i = 0; i < memberBeans.length; i++) {
					Member m = memberBeans[i];
					if (Member.MEMBER_TYPE_FOLDER.equals(m.getMemberType())) {
						try {
							grouperService.checkDefaultFolder(m.getSignature());
						} catch (Exception e) {
							isSystem[i] = true;
						}
					} else if (Member.MEMBER_TYPE_TEAM.equals(m.getMemberType())) {
						try {
							grouperService.checkDefaultTeam(m.getSignature());
						} catch (Exception e) {
							isSystem[i] = true;
						}
					}
				}
			}
			buffer = new StringBuffer(ReturnWrapper.getMembersTreeWrapper(memberBeans, isLeafs, isSystem));
//
//            buffer.append("{\"member\":[");
//            for (int i = 0; i < memberBeans.length; i++) {
//                Member memberBean = memberBeans[i];
//                String name = null;
//                String uuid = memberBean.getName();
//                if (memberBean.getMemberType().equals(Member.MEMBER_TYPE_PERSON)) {
//                    name = memberBean.getName();
//                } else {
//                    name = memberBean.getSignature();
//                }
//                String[] spiltDisplayName = name.split(":");
//                String shortName = spiltDisplayName[spiltDisplayName.length - 1];
//                buffer.append("{");
//                buffer.append("\"id\":").append(memberBean.getId()).append(",");
//                buffer.append("\"text\":\"").append(JS.quote(HTML.escape(shortName))).append("\",");
//                buffer.append("\"leaf\":").append(memberBean.getIsLeaf()).append(",");
//                buffer.append("\"type\":\"").append(memberBean.getMemberType()).append("\",");
//                buffer.append("\"fullName\":\"").append(JS.quote(HTML.escape(uuid))).append("\",");
//                buffer.append("\"system\":").append(isSystem[i]);
//                buffer.append("},");
//            }
//            if (memberBeans.length > 0)
//                buffer.setLength(buffer.length() - 1);
//            buffer.append("]}");
//            response.setHeader("Cache_Control", "max-age=10");
		}
		return buffer.toString();
	}

	/**
	 * 创建路径
	 * id 是该路径的父路径，如果id为0，则在根目录下创建该路径。
	 * name 是该路径的名称，不能包含":"特殊字符
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/createFolder", produces = "application/json; charset=UTF-8")
	public String createFolder(Long id, String name,String from) throws Exception {
		id = id == null ? 0L : id.longValue();
		if(name == null || name == "") {
			throw new GroupsException("组织名字不能为空！");
		}
		if(name.contains(":")) {
			throw new GroupsException("组织名字不能含有冒号！");
		}
		Member memberBean = grouperService.createFolder(id, name,from);
		String signature = memberBean.getSignature();
		String uuid = memberBean.getName();
		String[] spiltDisplayName = signature.split(":");
		String shortName = spiltDisplayName[spiltDisplayName.length - 1];
		StringBuffer buffer = new StringBuffer();
		buffer.append("{\"members\":[");
		buffer.append("{");
		buffer.append("\"id\":").append(memberBean.getId()).append(",");
		buffer.append("\"text\":\"").append(JS.quote(HTML.escape(shortName))).append("\",");
		buffer.append("\"fullName\":\"").append(JS.quote(HTML.escape(uuid))).append("\",");
		buffer.append("\"type\":\"").append(memberBean.getMemberType()).append("\"");
		buffer.append("}");
		buffer.append("]}");

		grouperService.shiftUpLastModified(memberBean.getId(), new Date());
		return buffer.toString();
	}


	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/createFolder_v3", produces = "application/json; charset=UTF-8")
	public String createFolder_v3(Long id, String name,String from) throws Exception {
		id = id == null? getManagerDomainTree(): id.longValue();
		if(id == null)
			throw new GroupsException("传入的id不能为空！");
		if(name == null || name == "") {
			throw new GroupsException("组织名字不能为空！");
		}
		if(name.contains(":")) {
			throw new GroupsException("组织名字不能含有冒号！");
		}
		Member memberBean = grouperService.createFolder(id, name,from);
		String signature = memberBean.getSignature();
		String uuid = memberBean.getName();
		String[] spiltDisplayName = signature.split(":");
		String shortName = spiltDisplayName[spiltDisplayName.length - 1];
		StringBuffer buffer = new StringBuffer();
		buffer.append("{\"members\":[");
		buffer.append("{");
		buffer.append("\"id\":").append(memberBean.getId()).append(",");
		buffer.append("\"text\":\"").append(JS.quote(HTML.escape(shortName))).append("\",");
		buffer.append("\"fullName\":\"").append(JS.quote(HTML.escape(uuid))).append("\",");
		buffer.append("\"type\":\"").append(memberBean.getMemberType()).append("\"");
		buffer.append("}");
		buffer.append("]}");

		grouperService.shiftUpLastModified(memberBean.getId(), new Date());
		return buffer.toString();
	}
	/**
	 * 创建用户组
	 * id 是该路径的父路径，id不能为0
	 * name 是该路径的名称，不能包含":"特殊字符
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/createTeam", produces = "application/json; charset=UTF-8")
	public String createTeam(Long id, String name,String from) throws Exception {
		id = id == null ? 0L : id.longValue();
		if(name == null || name == "") {
			throw new GroupsException("用户组名字不能为空！");
		}
		if(name.contains(":")) {
			throw new GroupsException("用户组名字不能含有冒号！");
		}
		Member memberBean = grouperService.createTeam(id, name, true,true,from);
		String signature = memberBean.getSignature();
		String uuid = memberBean.getName();
		String[] spiltDisplayName = signature.split(":");
		String shortName = spiltDisplayName[spiltDisplayName.length - 1];
		StringBuffer buffer = new StringBuffer();
		buffer.append("{\"members\":[");
		buffer.append("{");
		buffer.append("\"id\":").append(memberBean.getId()).append(",");
		buffer.append("\"text\":\"").append(JS.quote(HTML.escape(shortName))).append("\",");
		buffer.append("\"fullName\":\"").append(JS.quote(HTML.escape(uuid))).append("\",");
		buffer.append("\"type\":\"").append(memberBean.getMemberType()).append("\"");
		buffer.append("}");
		buffer.append("]}");
		grouperService.shiftUpLastModified(memberBean.getId(), new Date());
		return buffer.toString();
	}

	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/createTeam_v2", produces = "application/json; charset=UTF-8")
	public String createTeam_v2(Long id, String[] name,String from,Long groupId,String[] perm )throws Exception {
		id = id == null ? 0L : id.longValue();
		if(name == null || name.length <= 0) {
			throw new GroupsException("用户组名字不能为空！");
		}
		if(groupId == null || groupId < 0){
			throw new GroupsException("传入的groupId无效！");
		}
		if(name.length!=perm.length){
			throw new GroupsException("传入的name和perm长度不一致，无效！");

		}
		//查找id对应的folder是否存在，不存在就直接报错
		try{
			Member member=grouperService.getMemberById(id);
			if(member == null||!member.getMemberType().equals("folder"))
				throw new GroupsException("传入的id无效！");
		}catch (Exception e){
			throw new GroupsException("传入的id无效！");
		}
		int length=name.length;
		Long[] memberId=new Long[length];
		StringBuffer buffer = new StringBuffer();
		buffer.append("{\"members\":[");
		for(int i=0;i<length;i++){
			buffer.append("{");
			if(name[i].contains(":")) {
				throw new GroupsException("用户组名字不能含有冒号！");
			}
			Long idd=grouperService.getMemberByNameAndType(name[i],"team");
			if(idd!=null&&idd>0){
//				if(i==length-1){
//					result=id.toString();
//				}
				buffer.append("\"memberId\":").append(idd).append(",");
				buffer.append("\"name\":\"").append(name[i]).append("\"");
				memberId[i]=idd;
			}else {
				Member memberBean = grouperService.createTeam(id, name[i], true,true,from);
				memberId[i]=memberBean.getId();
				grouperService.shiftUpLastModified(memberBean.getId(), new Date());
				buffer.append("\"memberId\":").append(memberId[i]).append(",");
				buffer.append("\"name\":\"").append(name[i]).append("\"");
			}
			buffer.append("},");
		}
		if (length > 0)
			buffer.setLength(buffer.length() - 1);
		buffer.append("],\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}");
		//创建好了所有的team,将所有的team设置权限
		joinGroupPermission(memberId,groupId,perm);
		return buffer.toString();
	}

	public String joinGroupPermission(Long[] memberId, Long groupId,String[] perm) throws Exception {
		if (groupId == null || groupId == 0) {
			throw new GroupsException("柜子id为空");
		}
		if (memberId == null || memberId.length <= 0) {
			throw new GroupsException("用户列表为空");
		}
		Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
		Long thisMemberId = currentUserWrap.getMemberId();
		int length = memberId.length;
		// 管理员强制某人加入某圈子
		if ((permService.isAdmin(thisMemberId) || permService.hasGroupPermission(thisMemberId, groupId, IPermission.GroupPerm.MANAGE_GROUP))) {
			for (int i = 0; i < length; ++i) {
				long _memberId = memberId[i];
				groupService.bindMemberToGroup(_memberId, groupId);
			}
			modifyMemberGroupPermission(groupId, memberId, perm);
			StringBuffer buffer = new StringBuffer();
			buffer.append("{");
			buffer.append("\"type\":\"success\",");
			buffer.append("\"code\":\"200\",");
			buffer.append("\"detail\": \"ok\",");
			buffer.append("\"success\":true,");
			buffer.append("}");
			return buffer.toString();
		} else {
			throw new GroupsException("操作者权限不够");
		}
	}

	public String modifyMemberGroupPermission(Long groupId, Long[] memberId, String[] perm) {
		if (groupId == null || groupId == 0) {
			throw new GroupsException("柜子id为空");
		}
		if (memberId == null || memberId.length <= 0) {
			throw new GroupsException("memberId为空");
		}
		//perm为空是默认权限，只能下载，但是传过来变成length为0，故作此处理
		List<String> perms = new ArrayList<String>();
		if (perm == null || perm.length <= 0) {
			perms.add("");
		} else {
			perms = new ArrayList<String>(Arrays.asList(perm));
		}
		int length =memberId.length;
		for (int i = 0; i < length; ++i) {
			long _memberId = memberId[i];
			String p = perms.get(i);
			PermCollection pc = permService.getPermission(groupId,_memberId, OldPerm.PERM_TYPE_GROUP);
			// 清空四种权限
			pc.removeGroupPerm(new IPermission.GroupPerm[] { IPermission.GroupPerm.UPLOAD_RESOURCE,
					IPermission.GroupPerm.DELETE_RESOURCE, IPermission.GroupPerm.MODIFY_RESOURCE, IPermission.GroupPerm.DOWNLOAD_RESOURCE,
					IPermission.GroupPerm.ADD_FOLDER});

			// 解析需要设置的权限，并设置
			List<IPermission.GroupPerm> list = new ArrayList<IPermission.GroupPerm>();
			PermissionParseUtil.parseGroupPermission(list, p);
			list.add(IPermission.GroupPerm.VIEW_GROUP);
			list.add(IPermission.GroupPerm.VIEW_RESOURCE);
			list.add(IPermission.GroupPerm.DOWNLOAD_RESOURCE);
			pc.addGroupPerm(list.toArray(new IPermission.GroupPerm[list.size()]));

			// 重新设置权限
			permService.modifyPermission(groupId, _memberId,OldPerm.PERM_TYPE_GROUP, pc);
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
	/**
	 * 修改用户组名
	 * id 用户组id
	 * name 新用户组名
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/editTeam", produces = "application/json; charset=UTF-8")
	public String editTeam(Long id, String name) throws Exception {
		if (id == null || id == 0) {
			throw new GroupsException("用户组id不能为空！");
		}
		if(name == null || name == "") {
			throw new GroupsException("新用户组名字不能为空！");
		}
		if(name.contains(":")) {
			throw new GroupsException("新用户组名字不能含有冒号！");
		}
		try {
			grouperService.editTeam(id, name);
		} catch (GroupsException e) {
			throw new GroupsException(e);
		}

		//更新lastModified
		grouperService.shiftUpLastModified(id, new Date());
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 修改已存在路径的路径名
	 * id 路径id
	 * name 新路径名
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/editFolder", produces = "application/json; charset=UTF-8")
	public String editFolder(Long id, String name) throws Exception {
		if (id == null || id == 0) {
			throw new GroupsException("用户组织id不能为空！");
		}
		if(name == null || name == "") {
			throw new GroupsException("新用户组织名字不能为空！");
		}
		if(name.contains(":")) {
			throw new GroupsException("新用户组织名字不能含有冒号！");
		}
		try {
			grouperService.editFolder(id, name);
		} catch (GroupsException e) {
			throw new GroupsException(e);
		}
		grouperService.shiftUpLastModified(id, new Date());
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 删除用户组
	 * @param id 用户组team的memberid
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/deleteTeam", produces = "application/json; charset=UTF-8")
	public String deleteTeam(Long id){
		if (id == null || id == 0) {
			throw new GroupsException("用户组id不能为空！");
		}
		//更新lastModified
		grouperService.shiftUpLastModified(id, new Date());
		grouperService.deleteTeam(id);
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 删除路径，包括子路径和子用户组
	 * @param id 用户组织的memberId
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/deleteFolder", produces = "application/json; charset=UTF-8")
	public String deleteFolder(Long id) {
		if (id == null || id == 0) {
			throw new GroupsException("用户组织id不能为空");
		}
		//更新lastModified
		grouperService.shiftUpLastModified(id, new Date());
		grouperService.deleteFolder(id);
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/deleteFolder_v2", produces = "application/json; charset=UTF-8")
	public String deleteFolder_v2(Long id) {
		if (id == null || id == 0) {
			throw new GroupsException("用户组织id不能为空");
		}
		//更新lastModified
		grouperService.shiftUpLastModified(id, new Date());
		grouperService.deleteFolder_v2(id);
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
	/**
	 * 把批量用户添加到用户组
	 * @param memberId 批量用户id
	 * @param id 用户组 id
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/addMemberToTeam", produces = "application/json; charset=UTF-8")
	public String addMemberToTeam(Long[] memberId, Long id) {
		if (memberId == null || memberId.length <= 0) {
			throw new GroupsException("用户id不能为空！");
		}
		if (id == null || id == 0) {
			throw new GroupsException("用户组id不能为空！");
		}
		//先判断用户是否激活
		for (long mId : memberId) {
			Member memberBean = grouperService.getMemberById(mId);
			User userBaseBean = null;
			if (memberBean != null && memberBean.getAccount() != null) {
				userBaseBean = userService.getUserByAccount(memberBean.getAccount());
			}
			if (!Member.MEMBER_TYPE_PERSON.equals(memberBean.getMemberType())
					|| userBaseBean == null || userBaseBean.getUserbaseStatus().equals(User.USER_STATUS_DELETE)) {
				throw new GroupsException(ResourceProperty.getInvolidOperationWithDeletedAccount());
			}
		}
		//将member加入team中
		for (long mId : memberId) {
			grouperService.addMemberToTeam(mId,id);
		}
		//更新lastModified
		grouperService.shiftUpLastModified(id, new Date());
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 把批量用户从用户组中删除
	 * @param memberId 批量用户id
	 * @param id 用户组id
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/removeMemberFromTeam", produces = "application/json; charset=UTF-8")
	public String removeMemberFromTeam(Long[] memberId, Long id) {
		if (memberId == null || memberId.length <= 0) {
			throw new GroupsException("用户id不能为空！");
		}
		if (id == null || id == 0) {
			throw new GroupsException("用户组id不能为空！");
		}
		for (long mid : memberId) {
			grouperService.removeMemberFromTeam(mid, id);
		}
		//更新lastModified
		grouperService.shiftUpLastModified(id, new Date());
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}


	/**
	 * 查找用户组
	 * @param name 查找字段
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/searchTeam", produces = "application/json; charset=UTF-8")
	public String searchTeam(String name) throws Exception {
		if (name == null || "".equals(name)) {
			throw new GroupsException("搜索字段为空");
		}
		Member[] memberBeans = null;
		boolean[] isLeafs = null;
		StringBuffer buffer = new StringBuffer();
		try {
			memberBeans = grouperService.searchTeam(name);
		} catch (Exception e) {
			buffer.append("{\"members\":[]}");
			return buffer.toString();
		}
		isLeafs = new boolean[memberBeans.length];
		for (int i = 0; i < memberBeans.length; i++) {
			isLeafs[i] = !grouperService.hasSubMember(memberBeans[i].getId());
		}

		buffer.append("{\"members\":[");
		for (int i = 0; i < memberBeans.length; i++) {
			Member memberBean = memberBeans[i];
			String displayName = null;
			String uuid = memberBean.getName();
			displayName = memberBean.getSignature();
			buffer.append("{");
			buffer.append("\"id\":").append(memberBean.getId()).append(",");
			buffer.append("\"text\":\"").append(JS.quote(HTML.escape(displayName))).append("\",");
			buffer.append("\"leaf\":").append(isLeafs[i]).append(",");
			buffer.append("\"type\":\"").append(memberBean.getMemberType()).append("\",");
			buffer.append("\"fullName\":\"").append(JS.quote(HTML.escape(uuid))).append("\"");
			if(i==memberBeans.length-1)
				buffer.append("}");
			else
				buffer.append("},");
		}
		buffer.append("]}");
		return buffer.toString();
	}
	/**
	 * 该接口用于分域后，用户组织结构的查询 通过用户所在域过滤
	 *
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/domainTree", produces = "application/json; charset=UTF-8")
	public String domainTree(Long id, Boolean folderOnly, Boolean withoutLeaf, HttpServletResponse response) throws Exception {
		id = id == null ? 0L : id;
		folderOnly = folderOnly == null ? false : folderOnly;
		withoutLeaf = withoutLeaf == null ? false : withoutLeaf;
		// 首先判断系统中是否启用了分域功能
		boolean enable = PropertyUtil.getEnableDomainModule();
		if (enable) {
			// 做过滤
			if (id == 0) {
				// 暂时只对查询根结构做过滤
				List<Member> roots = domainService.getMyDomainTreeRoots();
				// 判断是否是系统生成的组织和组
				boolean[] isSystem = null;
				boolean[] isLeafs = null;
				Member[] memberBeans = null;
				if (roots != null) {
					isSystem = new boolean[roots.size()];
					isLeafs = new boolean[roots.size()];
					memberBeans = new Member[roots.size()];
					for (int i = 0; i < roots.size(); i++) {
						Member folder = roots.get(i);
						if (Member.MEMBER_TYPE_FOLDER.equals(folder.getMemberType())) {
							memberBeans[i] = folder;
							try {
								grouperService.checkDefaultFolder(folder.getSignature());
							} catch (GroupsException e) {
								isSystem[i] = true;
							}
						}
					}

				} else {
					memberBeans = new Member[0];
					isSystem = new boolean[0];
					isLeafs = new boolean[0];
				}
				return ReturnWrapper.getMembersTreeWrapper(memberBeans, isLeafs, isSystem);

			} else {
				// TODO 判断是否属于用户所在域
				//
				return this.grouperTree_domain(id, folderOnly, withoutLeaf, response);
			}
		} else {
			throw new GroupsException("未开启多域模块！");
		}

	}

	/**
	 * 获取所管理的域所包含的模块结构
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getManagerDomainTree", produces = "application/json; charset=UTF-8")
	public String getManagerDomainTree(Long id, @RequestParam(required = false	,defaultValue = "0")Integer start, @RequestParam(required = false	,defaultValue = "40")Integer limit, HttpServletResponse response) throws Exception {
		id = id == null ? 0L : id;
		start = start == null? 0 : start;
		limit = limit == null? Integer.MAX_VALUE : limit;
		// 首先判断系统中是否启用了分域功能
		boolean enable = PropertyUtil.getEnableDomainModule();
		if (enable) {
			Map<Long, Domain> domainMap = new HashMap<Long, Domain>();
			boolean isRootDomainManager = domainService.checkRootDomainManager(null);
			boolean isSystemAdmin = roleService.isSystemAdmin(null);
			// 做过滤
			if(id==-1&&!isRootDomainManager&&!isSystemAdmin) {//无权访问虚拟根节点
				id=0L;
			}
			if (id == 0&& !isRootDomainManager&&!isSystemAdmin) {
				// 暂时只对查询根结构做过滤

				List<DomainFolder> dfs = domainService.getMyManageDomainRoots();
				List<Member> roots = new ArrayList<Member>();
				for (DomainFolder df : dfs) {
					roots.add(df.getFolder());
					domainMap.put(df.getFolder().getId(), df.getDomain());
				}
				// 获得所管理的域所关联的根节点
				// 判断是否是系统生成的组织和组
				boolean[] isSystem = null;
				Member[] memberBeans = null;
				if (roots.size() > 0) {
					isSystem = new boolean[roots.size()];
					memberBeans = new Member[roots.size()];
					for (int i = 0; i < roots.size(); i++) {
						Member folder = roots.get(i);
						if (Member.MEMBER_TYPE_FOLDER.equals(folder.getMemberType())) {
							memberBeans[i] = folder;
							try {
								grouperService.checkDefaultFolder(folder.getSignature());
							} catch (GroupsException e) {
								isSystem[i] = true;
							}
						}
					}

				} else {
					memberBeans = new Member[0];
					isSystem = new boolean[0];
				}
				return ReturnWrapper.getManagerDomainWrapper(memberBeans, isSystem, domainMap);
			} else if(id == 0&& (isRootDomainManager||isSystemAdmin)){
				//返回一个虚拟的根域
				Domain rootDomain = domainService.getRootDomain();


				Member root = new Member();
				root.setId(-1L);
				root.setMemberType(Member.MEMBER_TYPE_FOLDER);
				root.setName("");
				root.setSignature("用户根");
				root.setIsLeaf(false);
				Member[] memberBeans = new Member[] {root};
				domainMap.put(root.getId(), rootDomain);
				boolean[] isSystem = new boolean[] {true};
				return ReturnWrapper.getManagerDomainWrapper(memberBeans, isSystem, domainMap);
			}else {
				if(id==-1) {
					id = 0L;
				}
				this.getGrouperRootBySystemOrRootDomainManager = true;
				Member[] memberBeans = null;
				//判断是否有子节点
				boolean[] isLeafs = null;
				// 判断是否是系统生成的组织和组
				boolean[] isSystem = null;
				String type = Member.MEMBER_TYPE_FOLDER;
				// 先判断是否根节点
				if (id != 0) {
					Member iMember = grouperService.getMemberById(id);
					type = iMember.getMemberType();
				}
				// 判断节点类型为team,还是folder
				if (Member.MEMBER_TYPE_TEAM.equals(type)) {
					if (id == 0) {
						memberBeans = new Member[0];
					} else {
						memberBeans = grouperService.getMembersInTeam(id);
					}
					List<User> userList = new ArrayList<User>();
					List<Member> memberList = new ArrayList<Member>();
					List<String> exceptionList = new ArrayList<String>();
					for (int i = 0; i < memberBeans.length; i++) {
						if (memberBeans[i].getAccount()!=null) {
							userList.add(userService.getUserByAccount(memberBeans[i].getAccount()));
							memberList.add(memberBeans[i]);
						} else {
							System.out.println("Get Account Exception, User donot Exist! : " + memberBeans[i].getName());
							exceptionList.add(memberBeans[i].getName());
						}
					}
					User[] userBaseBeans = userList.toArray(new User[userList.size()]);
					memberBeans = memberList.toArray(new Member[memberList.size()]);
					return ReturnWrapper.getUsersWrapperwithLimit(userBaseBeans, memberBeans, start,limit,exceptionList);
				} else if (Member.MEMBER_TYPE_FOLDER.equals(type)) {
					Long memberId = UserUtils.getCurrentMemberId();
					if (id == 0) {
						id = grouperService.getMemberById(memberId).getFolderId();
					}
					memberBeans = grouperService.getFolderAndTeamByParent_v2(id,false);
					isLeafs = new boolean[memberBeans.length];
					isSystem = new boolean[memberBeans.length];
					for (int i = 0; i < memberBeans.length; i++) {
						Member m = memberBeans[i];
						if (Member.MEMBER_TYPE_FOLDER.equals(m.getMemberType())) {
							try {
								grouperService.checkDefaultFolder(m.getSignature());
							} catch (Exception e) {
								isSystem[i] = true;
							}
						} else if (Member.MEMBER_TYPE_TEAM.equals(m.getMemberType())) {
							try {
								grouperService.checkDefaultTeam(m.getSignature());
							} catch (Exception e) {
								isSystem[i] = true;
							}
						}
					}
				}
				this.getGrouperRootBySystemOrRootDomainManager = false;
				domainMap = domainService.getDomainMapFromFolderArray(memberBeans);
				return ReturnWrapper.getManagerDomainWrapperwithLimit(memberBeans, isSystem, domainMap,start,limit);
			}
		} else {
			throw new GroupsException("未开启多域模块！");
		}
	}

	/**
	 * 获得一个用户所在的所有用户组
	 * memberId 用户id
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getTeamsOfMember", produces = "application/json; charset=UTF-8")
	public String getTeamsOfMember(Long[] memberId) throws Exception {
		if (memberId == null || memberId.length <=0) {
			throw new GroupsException("memberId不能为空！");
		}
		Member[] memberBeans = grouperService.getTeamsOfMember(memberId[0]);
		StringBuffer text = new StringBuffer();
		text.append("{\"members\":[");
		if (memberBeans != null && memberBeans.length > 0) {
			for (Member memberBean : memberBeans) {
				String name = null;
				String uuid = memberBean.getName();
				if (memberBean.getMemberType().equals(Member.MEMBER_TYPE_PERSON)) {
					name = memberBean.getName();
				} else {
					name = memberBean.getSignature();
				}
				String[] spiltDisplayName = name.split(":");
				String shortName = spiltDisplayName[spiltDisplayName.length - 1];
				text.append("{");
				text.append("\"id\":").append(memberBean.getId()).append(",");
				text.append("\"text\":\"").append(
						JS.quote(HTML.escape(shortName))).append(
						"\",");
				text.append("\"fullName\":\"").append(
						JS.quote(HTML.escape(uuid))).append("\",");
				text.append("\"type\":\"").append(
						memberBean.getMemberType()).append("\"");
				text.append("},");
			}
			text.setLength(text.length() - 1);
		}
		text.append("]}");
		return text.toString();
	}

	/**
	 * 根据用户组id获得用户组信息
	 * id 用户组id
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getTeam", produces = "application/json; charset=UTF-8")
	public String getTeam(Long id) throws Exception {

		Member memberBean = grouperService.getTeam(id);
		String name = memberBean.getSignature();
		String uuid = memberBean.getName();
		String[] spiltDisplayName = name.split(":");
		String shortName = spiltDisplayName[spiltDisplayName.length - 1];
		StringBuffer text = new StringBuffer();
		text.append("{\"members\":[");
		text.append("{");
		text.append("\"id\":").append(memberBean.getId()).append(",");
		text.append("\"text\":\"").append(JS.quote(HTML.escape(shortName))).append("\",");
		text.append("\"from\":\"").append(memberBean.getModifyFrom()==null?"":memberBean.getModifyFrom()).append("\",");
		text.append("\"fullName\":\"").append(JS.quote(HTML.escape(uuid))).append("\",");
		text.append("\"type\":\"").append(memberBean.getMemberType())
				.append("\"");
		text.append("}");
		text.append("]}");
		return text.toString();
	}

	/**
	 * 移动组
	 * id 需要移动的用户组id
	 * destId 目的父路径id
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/moveTeam", produces = "application/json; charset=UTF-8")
	public String moveTeam(Long id, Long destId) throws Exception {
		if (id == null || destId == null || id == 0 || destId == 0) {
			throw new GroupsException("输入参数参数为空值");
		}
		//更新lastModified
		grouperService.shiftUpLastModified(id, new Date());
		grouperService.moveTeam(id, destId);

		//更新lastModified
		grouperService.shiftUpLastModified(id, new Date());
		grouperService.shiftUpLastModified(destId, new Date());
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 移动组织
	 * @param id 需要移动的组织id
	 * @param destId 目的父路径id
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/moveFolder", produces = "application/json; charset=UTF-8")
	public String moveFolder(Long id, Long destId) throws Exception {
		if (id == null || destId == null || id == 0 || destId == 0) {
			throw new GroupsException("输入参数参数为空值");
		}
		//更新lastModified
		grouperService.shiftUpLastModified(id, new Date());

		grouperService.moveFolder(id, destId);

		//更新lastModified
		grouperService.shiftUpLastModified(destId, new Date());
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 复制组
	 * id 需要复制的用户组id
	 * destId 目的父路径id
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/copyTeam", produces = "application/json; charset=UTF-8")
	public String copyTeam(Long id, Long destId) throws Exception {
		if (id == null || destId == null || id == 0 || destId == 0) {
			throw new GroupsException("输入参数参数为空值");
		}
		grouperService.copyTeam(id, destId);

		//更新lastModified
		grouperService.shiftUpLastModified(destId, new Date());
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 复制组织
	 * @param id 需要复制的组织id
	 * @param destId 目的父路径id
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/copyFolder", produces = "application/json; charset=UTF-8")
	public String copyFolder(Long id, Long destId) throws Exception {
		if (id == null || destId == null || id == 0 || destId == 0) {
			throw new GroupsException("输入参数参数为空值");
		}
		grouperService.copyFolder(id, destId);

		//更新lastModified
		grouperService.shiftUpLastModified(destId, new Date());
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/sameGrouperTree", produces = "application/json; charset=UTF-8")
	public String sameGrouperTree(Long id) throws Exception {
		Member[] memberBeans = null;
		// 判断是否有子节点
		boolean[] isLeafs = null;

		// 先判断是否根节点
		if (id > 0) {
			memberBeans = grouperService.getMembersInTeam(id);
			User[] userBaseBeans = new User[memberBeans.length];
			for (int i = 0; i < memberBeans.length; i++) {
				userBaseBeans[i] = userService.getUserByAccount(memberBeans[i].getAccount());
			}
			return ReturnWrapper.getUsersWrapper(userBaseBeans, memberBeans, null);
		} else {
			long mId = UserUtils.getCurrentMemberId();
			memberBeans = grouperService.getTeamsOfMember(mId);
			isLeafs = new boolean[memberBeans.length];
			for (int i = 0; i < memberBeans.length; i++) {
				Member m = memberBeans[i];
				boolean ifLeaf = grouperService.hasSubMember(m.getId());
				isLeafs[i] = !ifLeaf;
			}
		}
		StringBuffer text = new StringBuffer();
		text.append("{\"members\":[");
		for (int i = 0; i < memberBeans.length; i++) {
			Member memberBean = memberBeans[i];
			String name = null;
			String uuid = memberBean.getName();
			if (memberBean.getMemberType().equals(Member.MEMBER_TYPE_PERSON)) {
				name = memberBean.getName();
			} else {
				name = memberBean.getSignature();
			}
			String[] spiltDisplayName = name.split(":");
			String shortName = spiltDisplayName[spiltDisplayName.length - 1];
			text.append("{");
			text.append("\"id\":").append(memberBean.getId()).append(",");
			text.append("\"text\":\"").append(
					JS.quote(HTML.escape(shortName))).append(
					"\",");
			text.append("\"leaf\":").append(isLeafs[i]).append(",");

			text.append("\"fullName\":\"").append(
					JS.quote(HTML.escape(uuid))).append("\"");
			text.append("},");

		}
		if (memberBeans.length > 0)
			text.setLength(text.length() - 1);
		text.append("]}");
		return text.toString();
	}


	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getManagerDomainTree_v2", produces = "application/json; charset=UTF-8")
	public Long getManagerDomainTree() throws Exception {
		Long id = 0L;

		// 首先判断系统中是否启用了分域功能
		boolean enable = PropertyUtil.getEnableDomainModule();
		if (enable) {
			Map<Long, Domain> domainMap = new HashMap<Long, Domain>();
			boolean isRootDomainManager = domainService.checkRootDomainManager(null);
			boolean isSystemAdmin = roleService.isSystemAdmin(null);
			// 做过滤
			if(id==-1&&!isRootDomainManager&&!isSystemAdmin) {//无权访问虚拟根节点
				id=0L;
			}
			if (id == 0&& !isRootDomainManager&&!isSystemAdmin) {
				// 暂时只对查询根结构做过滤

				List<DomainFolder> dfs = domainService.getMyManageDomainRoots();
				List<Member> roots = new ArrayList<Member>();
				for (DomainFolder df : dfs) {
					roots.add(df.getFolder());
					domainMap.put(df.getFolder().getId(), df.getDomain());
				}
				// 获得所管理的域所关联的根节点
				// 判断是否是系统生成的组织和组
				boolean[] isSystem = null;
				Member[] memberBeans = null;
				if (roots.size() > 0) {
					isSystem = new boolean[roots.size()];
					memberBeans = new Member[roots.size()];
					for (int i = 0; i < roots.size(); i++) {
						Member folder = roots.get(i);
						if (Member.MEMBER_TYPE_FOLDER.equals(folder.getMemberType())) {
							memberBeans[i] = folder;
							try {
								grouperService.checkDefaultFolder(folder.getSignature());
							} catch (GroupsException e) {
								isSystem[i] = true;
							}
						}
					}

				} else {
					memberBeans = new Member[0];
					isSystem = new boolean[0];
				}
//				return memberBeans[0].getId();
				return memberBeans==null?null:memberBeans[0].getId();
//				return ReturnWrapper.getManagerDomainWrapper(memberBeans, isSystem, domainMap);
			} else if(id == 0&& (isRootDomainManager||isSystemAdmin)){
				//返回一个虚拟的根域
				Domain rootDomain = domainService.getRootDomain();


				Member root = new Member();
				root.setId(-1L);
				root.setMemberType(Member.MEMBER_TYPE_FOLDER);
				root.setName("");
				root.setSignature("用户根");
				root.setIsLeaf(false);
				Member[] memberBeans = new Member[] {root};
				domainMap.put(root.getId(), rootDomain);
				boolean[] isSystem = new boolean[] {true};
//				return memberBeans[0].getId();
				return memberBeans==null?null:memberBeans[0].getId();
//				return ReturnWrapper.getManagerDomainWrapper(memberBeans, isSystem, domainMap);
			}else {
				if(id==-1) {
					id = 0L;
				}
				this.getGrouperRootBySystemOrRootDomainManager = true;
				Member[] memberBeans = null;
				//判断是否有子节点
				boolean[] isLeafs = null;
				// 判断是否是系统生成的组织和组
				boolean[] isSystem = null;
				String type = Member.MEMBER_TYPE_FOLDER;
				// 先判断是否根节点
				if (id != 0) {
					Member iMember = grouperService.getMemberById(id);
					type = iMember.getMemberType();
				}
				// 判断节点类型为team,还是folder
				if (Member.MEMBER_TYPE_TEAM.equals(type)) {
					if (id == 0) {
						memberBeans = new Member[0];
					} else {
						memberBeans = grouperService.getMembersInTeam(id);
					}
					List<User> userList = new ArrayList<User>();
					List<Member> memberList = new ArrayList<Member>();
					List<String> exceptionList = new ArrayList<String>();
					for (int i = 0; i < memberBeans.length; i++) {
						if (memberBeans[i].getAccount()!=null) {
							userList.add(userService.getUserByAccount(memberBeans[i].getAccount()));
							memberList.add(memberBeans[i]);
						} else {
							System.out.println("Get Account Exception, User donot Exist! : " + memberBeans[i].getName());
							exceptionList.add(memberBeans[i].getName());
						}
					}
					User[] userBaseBeans = userList.toArray(new User[userList.size()]);
					memberBeans = memberList.toArray(new Member[memberList.size()]);
//					return memberBeans[0].getId();
					return memberBeans==null?null:memberBeans[0].getId();
//					return ReturnWrapper.getUsersWrapperwithLimit(userBaseBeans, memberBeans, start,limit,exceptionList);
				} else if (Member.MEMBER_TYPE_FOLDER.equals(type)) {
					Long memberId = UserUtils.getCurrentMemberId();
					if (id == 0) {
						id = grouperService.getMemberById(memberId).getFolderId();
					}
					memberBeans = grouperService.getFolderAndTeamByParent_v2(id,false);
					isLeafs = new boolean[memberBeans.length];
					isSystem = new boolean[memberBeans.length];
					for (int i = 0; i < memberBeans.length; i++) {
						Member m = memberBeans[i];
						if (Member.MEMBER_TYPE_FOLDER.equals(m.getMemberType())) {
							try {
								grouperService.checkDefaultFolder(m.getSignature());
							} catch (Exception e) {
								isSystem[i] = true;
							}
						} else if (Member.MEMBER_TYPE_TEAM.equals(m.getMemberType())) {
							try {
								grouperService.checkDefaultTeam(m.getSignature());
							} catch (Exception e) {
								isSystem[i] = true;
							}
						}
					}
				}
				this.getGrouperRootBySystemOrRootDomainManager = false;
				domainMap = domainService.getDomainMapFromFolderArray(memberBeans);
				return memberBeans==null?null:memberBeans[0].getId();
//				return ReturnWrapper.getManagerDomainWrapperwithLimit(memberBeans, isSystem, domainMap,start,limit);
			}
		} else {
			throw new GroupsException("未开启多域模块！");
		}
	}

	/**
	 * 创建路径
	 * id 是该路径的父路径，如果id为0，则在根目录下创建该路径。
	 * name 是该路径的名称，不能包含":"特殊字符
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/createFolder_v2", produces = "application/json; charset=UTF-8")
	public String createFolder_v2(String[] name) throws Exception {
		Long id=0L;
		String  result = "";
		if(name == null) {
			throw new GroupsException("组织名字不能为空！");
		}
		id=getManagerDomainTree();
		int length=name.length;
		for(int i=0;i<length;i++){
			if(name[i].contains(":")) {
				throw new GroupsException("组织名字不能含有冒号！");
			}
			Long idd=grouperService.getMemberByNameAndType(name[i],"folder");
			if(idd!=null&&idd>0){
				id=idd;
				if(i==length-1){
					result=id.toString();
				}
				continue;
			}
			Member memberBean = grouperService.createFolder(id, name[i]);
			grouperService.shiftUpLastModified(memberBean.getId(), new Date());
			id=memberBean.getId();
			if(i==length-1)
				result=memberBean.getId().toString();
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append("{\"type\":\"success\",\"code\":\"200\",\"id\":\""+result+"\"}");
		return buffer.toString();
	}
}