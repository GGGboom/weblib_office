package com.dcampus.weblib.web.action;

import com.dcampus.common.config.PropertyUtil;
import com.dcampus.common.config.ResourceProperty;
import com.dcampus.common.generic.GenericDao;
import com.dcampus.common.paging.*;
import com.dcampus.common.util.HTML;
import com.dcampus.common.util.JS;
import com.dcampus.common.web.BaseController;
import com.dcampus.sys.entity.User;
import com.dcampus.sys.entity.keys.UserSearchItemKey;
import com.dcampus.sys.entity.keys.UserSortItemKey;
import com.dcampus.sys.service.UserService;
import com.dcampus.sys.util.UserUtils;
import com.dcampus.weblib.dao.CategoryDao;
import com.dcampus.weblib.entity.*;
import com.dcampus.weblib.entity.keys.LogSearchItemKey;
import com.dcampus.weblib.entity.keys.LogSortItemKey;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.service.*;
import com.dcampus.weblib.service.permission.IPermission;
import com.dcampus.weblib.service.permission.IPermission.CategoryPerm;
import com.dcampus.weblib.service.permission.IPermission.GroupPerm;
import com.dcampus.weblib.service.permission.PermCollection;
import com.dcampus.weblib.service.permission.impl.PermUtil;
import com.dcampus.weblib.util.*;
import com.dcampus.weblib.util.excelAdaptor.IExcelReader;
import com.dcampus.weblib.util.excelAdaptor.SheetReader;
import com.dcampus.weblib.util.excelAdaptor.SheetReader07;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.jws.Oneway;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.Timestamp;
import java.util.*;

@Controller
@RequestMapping(value="/user")
public class UserController extends BaseController {
	private static final Logger logger = Logger.getLogger(UserController.class);

	@Autowired
	private GrouperService grouperService;
	@Autowired
	private UserService userService;
	@Autowired
	private GroupService groupService;
	@Autowired
	private PermissionService permService;
	@Autowired
	private ApplicationService appService;
	@Autowired
	private ResourceService resourceService;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private LogService logService;
	@Autowired
	private ContactService contactService;
	@Autowired
	private DomainService domainService;
	@Autowired
	private RoleService roleService;

	@Autowired
	private IconService iconService;

    @Autowired
	private CategoryDao categoryDao;


	/**
	 * 获取用户柜子信息
 	 * @param memberId    用户id
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/getPersonalGroupInfo", produces = "application/json; charset=UTF-8")
	public String getPersonalGroupInfo(Long memberId){

		Member member=grouperService.getMemberById(memberId);
		if(member==null){
			throw  new GroupsException("输入的用户不存在");
		}
		Category category=categoryService.getCategoryByName("#person");
		Group groupBean=groupService.getGroupByDisplyName(category.getId(),member.getAccount());
		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		if(groupBean!=null){
			GroupIcon groupIcon=iconService.getIconByGroupId(groupBean.getId());
			buffer.append("\"group\":[");
			buffer.append("{");
			buffer.append("\"id\":").append(groupBean.getId()).append(",");
			buffer.append("\"categoryId\":").append(groupBean.getCategory().getId()).append(",");

			buffer.append("\"desc\":\"").append(JS.quote(HTML.escape(groupBean.getDesc() == null ? "" : groupBean.getDesc()))).append("\",");
			buffer.append("\"displayName\":\"").append(groupBean.getDisplayName()).append("\",");

			buffer.append("\"documentType\":").append(groupBean.getDocumentTypeValue()).append(",");
			buffer.append("\"iconId\":").append(groupBean.getGroupIcon()).append(",");
			buffer.append("\"icon\":\"").append(groupIcon==null?"":groupIcon.getFileName()).append("\",");
			buffer.append("\"iconName\":\"").append(groupIcon==null?"":groupIcon.getName()).append("\",");



			if(groupBean.getCategory().getId()==0)
				buffer.append("\"isVirtualGroup\":").append(true).append(",");
			else
				buffer.append("\"isVirtualGroup\":").append(false).append(",");
			buffer.append("\"name\":").append(groupBean.getName()).append(",");
			buffer.append("\"order\":").append(groupBean.getOrder()).append(",");

			buffer.append("\"paiban\":\"").append(groupBean.getExtendField1()==null?"":groupBean.getExtendField1()).append("\",");

			buffer.append("\"singleFileSize\":").append(resourceService.getResourceSingleFileSize(groupBean.getId())).append(",");
			buffer.append("\"availableCapacity\":").append(groupBean.getAvailableCapacity()).append(",");
			buffer.append("\"totalSize\":").append(groupBean.getTotalFileSize()).append(",");
			buffer.append("\"creatorName\":\"").append(groupBean.getCreatorName()).append("\",");
			buffer.append("\"topCategoryId\":").append(
					groupBean.getTopCategoryId()==null ? null :groupBean.getTopCategoryId())
					.append(",");
			buffer.append("\"creationDate\":\"").append(groupBean.getCreateDate().toString()
					.substring(0,groupBean.getCreateDate().toString().indexOf("."))).append("\"");


			buffer.append("}");
		}else {
			throw  new GroupsException("不存在个人柜子");
		}
		buffer.append("]}");

		return buffer.toString();
	}

	/**
	 * 搜索域内用户
	 * @param account
	 * @param name
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/domainUserSearch", produces = "application/json; charset=UTF-8")
	public String domainUserSearch(String[] account, String name)throws Exception{
		if(!PropertyUtil.getEnableDomainModule()) {
			throw new GroupsException("未开启多域模块");
		}
		if((account==null||account.length==0||isEmpty(account[0]))&&isEmpty(name)) {
			throw new GroupsException("查询条件不可为空");
		}
		//判断是否是总域管理员、域管理员、普通用户
		boolean isRootDomainAdmin = domainService.isRootDomainAdmin(UserUtils.getCurrentMemberId());
		boolean isDomainAdmin = domainService.isDomainAdmin(UserUtils.getCurrentMemberId());

		List<Member> roots = null;

		if(isRootDomainAdmin) {
			//TODO roots=根域下一级所有组织节点
			Member[] arr = grouperService.getFolderAndTeamByParent_v2(0,true);
			roots = new ArrayList<Member>();
			for(Member m:arr) {
				roots.add((Member) m);
			}

		}else if(isDomainAdmin) {
			List<DomainFolder> dfs = domainService.getMyManageDomainRoots();
			roots = new ArrayList<Member>();
			for(DomainFolder df:dfs) {
				roots.add(df.getFolder());
			}
		}else {
			roots = domainService.getMyDomainTreeRoots();
		}

		//获得所在范围的用户列表
		Map<Member, List<Member>> structMap= new HashMap<Member, List<Member>>();

		if(roots!=null) {
			for(Member folder:roots) {
				Stack<Member> structPath = new Stack<Member>();
				grouperService.searchMemberInFolderReturnStruct(folder.getId(),(account==null||account.length==0)?"":account[0],name,structPath,structMap);
			}
		}

		List<User> userbases = new ArrayList<User>();
		Member[] members = structMap.keySet().toArray(new Member[0]);
		for(Member m:members) {
			userbases.add(userService.getUserByAccount(m.getAccount()));
		}

		Map<String, Object> infoMap = new HashMap<String, Object>();
		infoMap.put("totalCount", members.length);

		List<JSONObject> memberVo = new ArrayList<JSONObject>();
		for(int i=0;i<members.length;i++){
			Map<String, Object> mem = new HashMap<String, Object>();
			Member m = members[i];
			mem.put("id", m.getId());
			mem.put("account", m.getAccount());
			User ub = userbases.get(i);
			mem.put("name", ub.getName() == null ? "" : ub.getName());
			mem.put("mobile",ub.getMobile() == null ? "" : ub.getMobile());
			List<Member> pathList = structMap.get(m);
			List<JSONObject> pathVo = new ArrayList<JSONObject>();
			for(Member p:pathList){
				Map<String, Object> path = new HashMap<String, Object>();
				path.put("memberId", p.getId());
				String _name = p.getSignature();
				_name = _name.substring(_name.lastIndexOf(':')+1);
				path.put("name", _name);
				path.put("type", p.getMemberType());
				pathVo.add(JSONObject.fromObject(path));
			}
			mem.put("path",pathVo);
			memberVo.add(JSONObject.fromObject(mem));
		}
		infoMap.put("result", memberVo);
		JSONObject json = JSONObject.fromObject(infoMap);
		return json.toString();
	}

	/**
	 * 注册用户
	 *
	 * @return
	 * @throws GroupsException
	 */
//	@RequiresUser
//	@ResponseBody
//	@RequestMapping(value="/register", produces = "application/json; charset=UTF-8")
//	public String register(HttpServletRequest request, HttpServletResponse response){
//		String[] accounts = ServletRequestUtils.getStringParameters(request, "account");
//		String mobile = ServletRequestUtils.getStringParameter(request, "mobile","");
//		String password = ServletRequestUtils.getStringParameter(request, "password","");
//		String name = ServletRequestUtils.getStringParameter(request, "name","");
//		String company = ServletRequestUtils.getStringParameter(request, "company","");
//		String department = ServletRequestUtils.getStringParameter(request, "department","");
//		String position = ServletRequestUtils.getStringParameter(request, "position","");
//		String email = ServletRequestUtils.getStringParameter(request, "email","");
//		String im = ServletRequestUtils.getStringParameter(request, "im","");
//		String phone = ServletRequestUtils.getStringParameter(request, "phone","" );
//		String from = ServletRequestUtils.getStringParameter(request, "from","" );
//		String creator = ServletRequestUtils.getStringParameter(request, "creator","" );
//		long teamId = ServletRequestUtils.getLongParameter(request, "teamId", 0L);
//		long domainId = ServletRequestUtils.getLongParameter(request, "domainId", 0L);
//		StringBuffer returnStr = new StringBuffer();
//		if (accounts == null || accounts.length == 0)
//			return "";
//		if (PropertyUtil.getUserMaxLimit() != null) {
//			long size = userService.getNumberOfUser()
//					- userService.countAdmin(Admin.SUPER_ADMIN);
//			if (size >= PropertyUtil.getUserMaxLimit().longValue()) {
//				throw new GroupsException(ResourceProperty.getOverUserMaxLimitException(
//						""+PropertyUtil.getUserMaxLimit().longValue()));
//			}
//		}
//		String account = accounts[0];
//		if(account != null &&!"".equals(account.trim())){
//			User existAccount = userService.getUserByAccount(account);
//			if(existAccount!=null&&!existAccount.getAccount().equals(account)){
//
//				throw new GroupsException("账号"+account+"已经存在");
//			}
//		}
//
//		User bean = new User();
//		bean.setAccount(account);
//		bean.setPassword(password);
//		bean.setName(name);
//		bean.setCompany(company);
//		bean.setDepartment(department);
//		bean.setEmail(email);
//		bean.setPosition(position);
//		bean.setIm(im);
//		//bean.setMobile(mobile);
//		if (mobile != null&&!"".equals(mobile.trim())) {
//			User  existAccount =userService.getUserByAccountOrMobile(mobile);
//			if(existAccount!=null&&!existAccount.getAccount().equals(account)){
//				throw new GroupsException("账号"+account+"的手机号: "+mobile+" 已经绑定到账号"+existAccount.getAccount());
//			}
//			bean.setMobile(mobile);
//		}
//		bean.setPhone(phone);
//		bean.setUserCreateType(User.USER_CREATE_TYPE_ADD);
//		bean.setUserbaseStatus(User.USER_STATUS_NORMAL);
//		if(from!=null&&!"".equals(from.trim())) {
//			bean.setRegisterFrom(from);
//		}
//		if(creator!=null&&!"".equals(creator.trim())) {
//			bean.setCreator(creator);
//		}
//		try{
//			userService.createAccount(bean);
//		} catch (GroupsException e) {
//			throw new GroupsException(e.getMessage());
//		}
//
//
//		// 默认注册一个同名马甲
//		Member memberBean = new Member();
//		memberBean.setAccount(account);
//		memberBean.setName(account);
//		memberBean.setMemberStatus(Member.STATUS_NORMAL);
//		memberBean.setMemberType(Member.MEMBER_TYPE_PERSON);
//		grouperService.createMember(memberBean);
//		if(PropertyUtil.getEnableDomainModule()) {
//			//如果开启来多域模块，需判断创建用户来自哪个域，并加入到这个域的“所有用户组”
//			if(domainId!=0) {
//				Domain domain = domainService.getDomainById(domainId);
//				if (domain == null) {
//					throw new GroupsException("请输入正确的domianId");
//				}
//				if(!PropertyUtil.getRootDomainName().equals(domain.getDomainName())) {
//					//如果创建的新用户不是属于总域，则需要加如某个域内的所有用户组
//					Member allMemberTeam = domainService.createOrGetDomainAllMemberTeam(domain);
//					grouperService.addMemberToTeam(memberBean.getId(), allMemberTeam.getId());
//				}
//			}
//		}
//		if (teamId > 0) {
//			grouperService.addMemberToTeam(memberBean.getId(),teamId);
//			grouperService.shiftUpLastModified(teamId, new Date());
//		}
//		returnStr.append("{\"account\":\"");
//		returnStr.append(account);
//		returnStr.append("\",\"memberId\":");
//		returnStr.append(memberBean.getId());
//		returnStr.append(",\"memberName\":\"");
//		returnStr.append(memberBean.getName());
//		returnStr.append("\"}");
//		return returnStr.toString();
//	}
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/register", produces = "application/json; charset=UTF-8")
	public String register(HttpServletRequest request, HttpServletResponse response){
		boolean autoCreateGroup=true;
		String[] accounts = ServletRequestUtils.getStringParameters(request, "account");
		String mobile = ServletRequestUtils.getStringParameter(request, "mobile","");
		String password = ServletRequestUtils.getStringParameter(request, "password","");
		String name = ServletRequestUtils.getStringParameter(request, "name","");
		String company = ServletRequestUtils.getStringParameter(request, "company","");
		String department = ServletRequestUtils.getStringParameter(request, "department","");
		String position = ServletRequestUtils.getStringParameter(request, "position","");
		String email = ServletRequestUtils.getStringParameter(request, "email","");
		String im = ServletRequestUtils.getStringParameter(request, "im","");
		String phone = ServletRequestUtils.getStringParameter(request, "phone","" );
		String from = ServletRequestUtils.getStringParameter(request, "from","" );
		String creator = ServletRequestUtils.getStringParameter(request, "creator","" );
		long teamId = ServletRequestUtils.getLongParameter(request, "teamId", 0L);
		long domainId = ServletRequestUtils.getLongParameter(request, "domainId", 0L);
		StringBuffer returnStr = new StringBuffer();
		if (accounts == null || accounts.length == 0)
			return "";
		if (PropertyUtil.getUserMaxLimit() != null) {
			long size = userService.getNumberOfUser()
					- userService.countAdmin(Admin.SUPER_ADMIN);
			if (size >= PropertyUtil.getUserMaxLimit().longValue()) {
				throw new GroupsException(ResourceProperty.getOverUserMaxLimitException(
						""+PropertyUtil.getUserMaxLimit().longValue()));
			}
		}
		String account = accounts[0];
		if(account != null &&!"".equals(account.trim())){
			User existAccount = userService.getUserByAccount(account);
			if(existAccount!=null&&!existAccount.getAccount().equals(account)){

				throw new GroupsException("账号"+account+"已经存在");
			}
		}

		User bean = new User();
		bean.setAccount(account);
		bean.setPassword(password);
		bean.setName(name);
		bean.setCompany(company);
		bean.setDepartment(department);
		bean.setEmail(email);
		bean.setPosition(position);
		bean.setIm(im);
		//bean.setMobile(mobile);
		if (mobile != null&&!"".equals(mobile.trim())) {
			User  existAccount =userService.getUserByAccountOrMobile(mobile);
			if(existAccount!=null&&!existAccount.getAccount().equals(account)){
				throw new GroupsException("账号"+account+"的手机号: "+mobile+" 已经绑定到账号"+existAccount.getAccount());
			}
			bean.setMobile(mobile);
		}
		bean.setPhone(phone);
		bean.setUserCreateType(User.USER_CREATE_TYPE_ADD);
		bean.setUserbaseStatus(User.USER_STATUS_NORMAL);
		if(from!=null&&!"".equals(from.trim())) {
			bean.setRegisterFrom(from);
		}
		if(creator!=null&&!"".equals(creator.trim())) {
			bean.setCreator(creator);
		}
		try{
			userService.createAccount(bean);
		} catch (GroupsException e) {
			throw new GroupsException(e.getMessage());
		}

		GroupType groupType=groupService.getGroupTypeByName("个人");
		Category categoryPerson=categoryService.getCategoryByName("#person");
		if(categoryPerson.getAvailableCapacity()<groupType.getTotalFileSize()){
			userService.deleteUser(bean);
			throw new  GroupsException("个人资源库的可用容量不足");
		}


		// 默认注册一个同名马甲
		Member memberBean = new Member();
		memberBean.setAccount(account);
		memberBean.setName(account);
		memberBean.setMemberStatus(Member.STATUS_NORMAL);
		memberBean.setMemberType(Member.MEMBER_TYPE_PERSON);
		grouperService.createMember(memberBean);
		if(PropertyUtil.getEnableDomainModule()) {
			//如果开启来多域模块，需判断创建用户来自哪个域，并加入到这个域的“所有用户组”
			if(domainId!=0) {
				Domain domain = domainService.getDomainById(domainId);
				if (domain == null) {
					throw new GroupsException("请输入正确的domianId");
				}
				if(!PropertyUtil.getRootDomainName().equals(domain.getDomainName())) {
					//如果创建的新用户不是属于总域，则需要加如某个域内的所有用户组
					Member allMemberTeam = domainService.createOrGetDomainAllMemberTeam(domain);
					grouperService.addMemberToTeam(memberBean.getId(), allMemberTeam.getId());
				}
			}
		}
		if (autoCreateGroup) {
			String[] categoryNames = PropertyUtil.getDefaultGroupCategory()[0]
					.split(":");
			String categoryName = categoryNames[0];
			String categoryDisplayName = categoryNames[1];
			Category category = null;
			List<Category> categories= categoryService.getCategoriesByName(categoryName);
			if (categories == null || categories.size() <= 0) {
				category = new Category();
				category.setName(categoryName);
				category.setDisplayName(categoryDisplayName);
				category.setParentId(0);
				category.setCategoryStatus(Category.STATUS_NORMAL);
				category.setCreateDate(new Timestamp(System
						.currentTimeMillis()));
				categoryService.createCategory(category, false, OldPerm.SYSTEMADMIN_MEMBER_ID);
			} else {
				category = categories.get(0);
			}
			Group group = groupService.getGroupByName(memberBean.getId().toString());
			if (group == null || group.getId() < 0) {
				Group groupBean = new Group();
				groupBean.setName("" + memberBean.getId());
				groupBean.setCreatorName(memberBean.getAccount());
				groupBean.setDisplayName(memberBean.getAccount());
				groupBean.setAddr("" + memberBean.getId());
				groupBean.setCategory(category);
				groupBean.setCreateDate(new Timestamp(System
						.currentTimeMillis()));
				groupBean.setCreatorId(memberBean.getId());
				groupBean.setGroupStatus(Group.STATUS_NORMAL);
				groupBean.setGroupUsage(Group.USAGE_PRIVATE);
				groupBean.setOwner(memberBean.getAccount());

				GroupType typeBean = groupService.getGroupTypeByName(PropertyUtil
						.getPersonalGroupType());
				groupBean.setGroupType(typeBean);
				//修改导入的用户柜子大小
//				GroupType groupType=groupService.getGroupTypeByName("个人");
				groupBean.setTotalFileSize(groupType.getTotalFileSize());
				groupBean.setAvailableCapacity(groupType.getTotalFileSize());
				groupBean.setUsedCapacity(0L);
//				初始化topCtegoryId
//				Category category1=categoryService.getCategoryByName("#person");

				groupService.createGroup_v1(bean,groupBean, false, account, OldPerm.SYSTEMADMIN_MEMBER_ID);

				PermCollection pc = new PermCollection();

				try {
					permService.modifyPermission(groupBean.getId(), memberBean.getId(), OldPerm.PERM_TYPE_GROUP, pc);
				} catch (Exception e) {
					//todo nothing
				}


				resourceService.createResourceDir(groupBean.getId(), PropertyUtil
						.getRecyclerName(), 0, memberBean.getId(), true);
				resourceService.createResourceDir(groupBean.getId(), "待办事项", 0,
						memberBean.getId(), true, GroupResource.DOCUMENT_TYPE_UNKNOWN, 0);
				resourceService.createResourceDir(groupBean.getId(), "我的文档", 0,
						memberBean.getId(), true, GroupResource.DOCUMENT_TYPE_UNKNOWN, 0);
				resourceService.createResourceDir(groupBean.getId(), "我的图片", 0,
						memberBean.getId(), true, GroupResource.DOCUMENT_TYPE_UNKNOWN, 0);
				resourceService.createResourceDir(groupBean.getId(), "我的视频", 0,
						memberBean.getId(), true, GroupResource.DOCUMENT_TYPE_UNKNOWN, 0);
				//创建邮件大附件文件夹
				resourceService.getPersonEmailAttachFolder(memberBean.getId());
			}
		}
		if (teamId > 0) {
			grouperService.addMemberToTeam(memberBean.getId(),teamId);
			grouperService.shiftUpLastModified(teamId, new Date());
		}
		returnStr.append("{\"account\":\"");
		returnStr.append(account);
		returnStr.append("\",\"memberId\":");
		returnStr.append(memberBean.getId());
		returnStr.append(",\"memberName\":\"");
		returnStr.append(memberBean.getName());
		returnStr.append("\"}");
		return returnStr.toString();
	}

	/**
	 * 查询用户状态
	 *
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value={"/status","/status_v2"}, produces = "application/json; charset=UTF-8")
	public String getStatus(HttpServletRequest request) throws Exception{

		Long memberId = Long.MIN_VALUE;
		boolean flag = false;
		String memberName = "";
		String memberIp = IpGetter.getRemoteAddr(request);
		Subject currentUser = SecurityUtils.getSubject();
		CurrentUserWrap currentUserWrap=null;
		try{
		Session session = currentUser.getSession();
		currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
		}
		catch (Exception ex)
		{
			flag = true;
		}
		if (currentUserWrap != null) {
			memberId = currentUserWrap.getMemberId();
			memberName = currentUserWrap.getMemberName();
		}
		StringBuffer returnStr = new StringBuffer();

		long personGroupId = 0L;
		boolean isAdmin = false;
		boolean isProjectManager = false;
		boolean addGroup = false;
		GroupResource b = null;
		boolean isApplicationAdmin = false;
		String status = "guest";
		String picUrl ="";
		String account = null;
		String name = null;
		String pkString = null;

		if (currentUser.isAuthenticated()) {
			status = "login";
			String personGroupName = memberId.toString();
			Group personGroup = groupService.getGroupByName(personGroupName);
			if (personGroup != null) {
				personGroupId = groupService.getGroupByName(personGroupName).getId();
			}
			isAdmin = permService.isAdmin(memberId);
			isProjectManager = permService.isProjectManager(memberId);
			addGroup = permService.hasCreatGroupPermission(memberId);
			b = resourceService.getPersonEmailAttachFolder(memberId);
			isApplicationAdmin = appService.isApplicationAdmin(memberId);
			account = UserUtils.getAccount();
			User userBase = userService.getUserByAccount(account);
			String url =  userBase.getPhoto();
			if(url==null||url.length()==0){
				picUrl = "";
			} else {
				picUrl = PropertyUtil.getMemberPicFolderPath()+url;
			}
			name = UserUtils.getCommonName();
		} else {
//            //如果没登录，则创建加密登录公钥
//            KeyPair keyPair = Crypt.generateRsaKeyPair();
//            RSAPublicKey pk = (RSAPublicKey) keyPair.getPublic();
//            RSAPrivateKey pri = (RSAPrivateKey)keyPair.getPrivate();
//            pkString = Base64.encode(pk.getEncoded());
//            String priString = Base64.encode(pri.getEncoded());
//            //System.out.println(pkString);
//            //System.out.println(priString);
//            //将私钥存入session
//            request.getSession().setAttribute("privateKey", priString);
//            //将公钥返回给客户端
		}
		if(flag==true)
		{
			JSONObject json=new JSONObject();
			json.element("message","SeeionId 不一致");
			json.element("code","500");
			return json.toString();
		}

		returnStr.append("{\"status\":\"").append(status).append("\",");
		returnStr.append("\"personGroupId\":").append(personGroupId).append(",");
		returnStr.append("\"account\":\"").append(account).append("\",");
		returnStr.append("\"memberId\":").append(memberId).append(",");
		returnStr.append("\"memberName\":\"").append(memberName).append("\",");
		returnStr.append("\"memberIp\":\"").append(memberIp).append("\",");
		returnStr.append("\"isApplicationAdmin\":\"").append(isApplicationAdmin).append("\",");
		returnStr.append("\"isProjectManager\":\"").append(isProjectManager).append("\",");
		returnStr.append("\"largeAttachId\":").append(b == null ? 0L : b.getId().longValue()).append(",");
		returnStr.append("\"isAdmin\":").append(isAdmin).append(",");
		returnStr.append("\"addGroup\":\"").append(addGroup).append("\",");
		returnStr.append("\"picUrl\":\"").append(
				JS.quote(HTML.escape(picUrl==null?"":picUrl))).append("\",");
//        if("guest".equals(status)&&pkString!=null){
//            returnStr.append("\"pk\":\"").append(pkString).append("\",");
//        }
		returnStr.append("\"name\":\"").append(HTML.escape(name)).append("\"}");
		return returnStr.toString();
	}

	/**
	 * 查询用户状态
	 *
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/getAccount", produces = "application/json; charset=UTF-8")
	public String getAccount(){
		Subject currentUser = SecurityUtils.getSubject();
		StringBuffer returnStr = new StringBuffer();
		if (!currentUser.isAuthenticated()) {
			return "{\"result\":\"failed\"}";
		}else{
			User user = UserUtils.getUser();
			returnStr.append("{\"account\":\"").append(user.getAccount()).append("\",");
			returnStr.append("\"name\":\"").append(JS.quote(HTML.escape(user.getName()))).append("\",");
			returnStr.append("\"company\":\"").append(JS.quote(HTML.escape(user.getCompany()))).append("\",");
			returnStr.append("\"department\":\"").append(JS.quote(HTML.escape(user.getDepartment()))).append("\",");
			returnStr.append("\"position\":\"").append(JS.quote(HTML.escape(user.getPosition()))).append("\",");
			returnStr.append("\"email\":\"").append(JS.quote(HTML.escape(user.getEmail()))).append("\",");
			returnStr.append("\"mobile\":\"").append(JS.quote(HTML.escape(user.getMobile()))).append("\",");
			returnStr.append("\"phone\":\"").append(JS.quote(HTML.escape(user.getPhone()))).append("\",");
			returnStr.append("\"status\":\"").append(user.getUserbaseStatus().equalsIgnoreCase(User.USER_STATUS_NORMAL) ? "normal" : "delete" ).append("\"}");
			return returnStr.toString();
		}
	}

	/**
	 * 保活
	 *
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/alive", produces = "application/json; charset=UTF-8")
	public String alive(){
		return "{\"type\":\"success\",\"code\":\"200\",\"detail\":\"ok\",\"success\":true}";
	}

	/**
	 * 修改用户信息
	 *包括密码
	 * @return
	 * @throws GroupsException
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value={"/modifyAccount","/modifyAccount_v2"}, produces = "application/json; charset=UTF-8")
	public String modifyAccount(String[] account, String name, String company, String department, String email,
								String im, String phone, String mobile, String position, String password) {
		User user = UserUtils.getUser();
		if (account == null || account.length == 0) {
			account = new String[] { user.getAccount() };
		}
		Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
		String memberName = currentUserWrap.getMemberName();
		System.out.println("当前操作者为"+memberName);
		for (String ac : account) {
			User userBaseBean = new User();
			System.out.println("修改的账号是"+ac);
			userBaseBean.setAccount(ac);
			if (name != null) {
				userBaseBean.setName(name);
			}
			if (company != null) {
				userBaseBean.setCompany(company);
			}
			if (department != null) {
				userBaseBean.setDepartment(department);
			}
			if (email != null) {
				userBaseBean.setEmail(email);
			}
			if (im != null) {
				userBaseBean.setIm(im);
			}
			if (phone != null) {
				userBaseBean.setPhone(phone);
			}
			if (mobile != null) {
				User  existAccount = userService.getUserByAccountOrMobile(mobile);
				if(existAccount!=null&&!existAccount.getAccount().equals(ac)){
					throw new GroupsException("账号"+account+"的手机号: "+mobile+" 已经绑定到账号"+existAccount.getAccount());
				}
				userBaseBean.setMobile(mobile);
			}
			if(position != null){
				userBaseBean.setPosition(position);
			}

			userService.modifyAccount(userBaseBean);

			if (password != null && password.trim().length() != 0) {
				System.out.println("用户修改的新密码是"+password);
				userService.modifyPassword(ac, password);
			}
			long memberId = grouperService.getMemberByNameAndAccount(ac,ac).getId();
			grouperService.shiftUpLastModified(memberId, new Date());
			UserUtils.removeCache("user");
		}
		return "{\"type\":\"success\",\"code\":\"200\",\"detail\":\"ok\",\"success\":true}";
	}

	/**
	 * 修改登录者密码
	 * @param password
	 * @param newPassword
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value={"/modifyPassword","/modifyPassword_v2"}, produces = "application/json; charset=UTF-8")
	public String modifyPassword(String password, String newPassword) throws Exception {
		boolean result = false;
		String message = null;
		String account = UserUtils.getAccount();
		boolean check = false;
		check = userService.checkPassword(account, password);
		//密码检测通过
		if (check) {
			userService.modifyPassword(account, newPassword);
			result = true;
		} else {
			message = ResourceProperty.getPasswordErrorExceptionString();
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		buffer.append("\"result\":").append(result).append(",");
		buffer.append("\"message\":").append("\"").append(message == null ? "操作成功" : message).append("\"");
		buffer.append("}");
		return buffer.toString();
	}

	/**
	 * 重置用户密码并且返回
	 * @param account
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value={"/resetPassword"}, produces = "application/json; charset=UTF-8")
	public String resetPassword(String[] account, HttpServletResponse response) throws Exception {

		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename="+ PropertyUtil.getExportAccountFileName());
		OutputStream outputStream = null;
		try {
			outputStream = response.getOutputStream();
			userService.resetPassword(account, outputStream);
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			if (outputStream != null)
				outputStream.close();
		}
		return null;
	}

	/**********************************************收藏相关*******************************************************/

	@RequiresUser
	@ResponseBody
	@RequestMapping(value={"/getWatchByGroup","/getWatchByGroup_v2"}, produces = "application/json; charset=UTF-8")
	public String getWatchByGroup(Long groupId) throws Exception {
		long memberId = UserUtils.getCurrentMemberId();
		Watch watchBean = groupService.getWatch(memberId, groupId, Watch.WATCH_TYPE_GROUP);
		long id = watchBean == null ? 0L : watchBean.getId();
		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		buffer.append("\"watchId\":").append(id).append("}");
		return buffer.toString();
	}
	/**
	 * 获得我的收藏
	 * @param watchType 收藏类型 group
	 * @param start
	 * @param limit
	 * @param withPath 是否带路径
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value={"/getWatches","/getWatches_v2"}, produces = "application/json; charset=UTF-8")
	public String getWatches(String watchType, Integer start, Integer limit, Boolean withPath){
		if (watchType == null || watchType == "") {
			watchType = Watch.WATCH_TYPE_GROUP;
		}
		start = start == null ? 0 : start;
		limit = limit == null ? Integer.MAX_VALUE : limit;
		withPath = withPath == null ?false : withPath;

		Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
		Long memberId = currentUserWrap.getMemberId();
		StringBuffer buffer = new StringBuffer();

		List<Watch> watches = groupService.getWatchesByMember(memberId.longValue(), watchType, start, limit);
		int totalCount = watches.size();
		if (Watch.WATCH_TYPE_GROUP.equalsIgnoreCase(watchType)) {
			Group[] groupBeans = new Group[totalCount];
			String[] paths = new String[totalCount];


//            获取用户的权限
			Map<Long, GroupPerm[]> map = new TreeMap<Long, GroupPerm[]>();
			//是否圈子管理员
			Map<Long, Boolean> groupManagerMap = new TreeMap<Long, Boolean>();

			for (int i = 0; i < totalCount; ++i) {
				groupBeans[i] = groupService.getGroupById(watches.get(i).getTargetId());
				PermCollection pc = permService.getPermission(groupBeans[i].getId(), memberId, OldPerm.PERM_TYPE_GROUP);
				map.put(groupBeans[i].getId(), pc.getGroupPerm());
				boolean isGroupManager = permService.isGroupManager(memberId, groupBeans[i].getId());
				groupManagerMap.put(groupBeans[i].getId(), isGroupManager);
				//path
				if(withPath){
					long cid = groupBeans[i].getCategory().getId();//柜子所属分类ID
					List<Category> categorys = categoryService.tracedCategoryList(cid);
					StringBuffer sb =new StringBuffer();
					for(Category c:categorys){
						sb.insert(0, c.getId()+":");
					}
					paths[i] = sb.toString();
				}
			}
			buffer.append("{");
			buffer.append("\"totalCount\":").append(totalCount).append(",");
			buffer.append("\"watches\":[");
			for (int i = 0; i < totalCount; ++i) {
				buffer.append("{");
				buffer.append("\"displayName\":\"").append(JS.quote(Filter.convertHtmlBody(groupBeans[i].getDisplayName() == null ? "" : groupBeans[i].getDisplayName()))).append("\",");
				buffer.append("\"desc\":\"").append(JS.quote(Filter.convertHtmlBody(groupBeans[i].getDesc() == null ? "" : groupBeans[i].getDesc()))).append("\",");
				buffer.append("\"watchId\":").append(watches.get(i).getId()).append(",");
				Group groupBean = groupBeans[i];
				if (map != null) {
					IPermission.GroupPerm[] gps = map.get(groupBeans[i].getId());
					buffer.append("\"upload\":").append(hasPerm(gps,IPermission.GroupPerm.UPLOAD_RESOURCE)).append(",");
					buffer.append("\"delete\":").append(hasPerm(gps,IPermission.GroupPerm.DELETE_RESOURCE)).append(",");
					buffer.append("\"addDir\":").append(hasPerm(gps, IPermission.GroupPerm.ADD_FOLDER)).append(",");
					buffer.append("\"modify\":").append(hasPerm(gps,IPermission.GroupPerm.MODIFY_RESOURCE)).append(",");
				}
				buffer.append("\"documentType\":").append(
						groupBean.getDocumentTypeValue()).append(",");
				if(i<paths.length&&paths[i]!=null){
					buffer.append("\"path\":\"").append(paths[i]).append("\",");
				}else{
					buffer.append("\"path\":\"").append("").append("\",");
				}
				buffer.append("\"groupId\":").append(groupBeans[i].getId());
				buffer.append("},");
			}
			if (totalCount > 0) {
				buffer.setLength(buffer.length() - 1);
			}
			buffer.append("]");
			buffer.append("}");

		}
		return buffer.toString();
	}

	private static boolean hasPerm(IPermission.GroupPerm[] perms,
								   IPermission.GroupPerm perm) {
		if (perms == null)
			return false;
		for (IPermission.GroupPerm p : perms) {
			if (p == perm)
				return true;
		}
		return false;
	}

	/**
	 * 用户高级搜索
	 *
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/advancedSearch", produces = "application/json; charset=UTF-8")
	public String advancedSearch(String[] account, HttpServletRequest request) throws Exception {
		String name = ServletRequestUtils.getStringParameter(request, "name", "");
		String company = ServletRequestUtils.getStringParameter(request, "company", "");
		String department = ServletRequestUtils.getStringParameter(request, "department", "");
		String email = ServletRequestUtils.getStringParameter(request, "email", "");
		String mobile = ServletRequestUtils.getStringParameter(request, "mobile", "");
		String im = ServletRequestUtils.getStringParameter(request, "im", "");
		String status = ServletRequestUtils.getStringParameter(request, "status", "");
		String userCreateType = ServletRequestUtils.getStringParameter(request, "userCreateType", "");
		int start = ServletRequestUtils.getIntParameter(request, "start", 0);
		int limit = ServletRequestUtils.getIntParameter(request, "limit", 10);

		Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
		Long memberId = currentUserWrap.getMemberId();
		StringBuffer buffer = new StringBuffer();

		SearchTerm searchTerm = new AndSearchTerm();
		boolean isAdmin = permService.isAdmin(memberId);

		if (account != null) {
			SearchTerm orsearchTerm = new OrSearchTerm();
			boolean f = false;
			for (String ac : account) {
				if (!isEmpty(ac)) {
					orsearchTerm.add(new SearchItem<String>(
							UserSearchItemKey.ACCOUNT,
							SearchItem.Comparison.LK, ac));

					orsearchTerm.add(new SearchItem<String>(UserSearchItemKey.NAME,
							SearchItem.Comparison.LK, ac));
					orsearchTerm.add(new SearchItem<String>(UserSearchItemKey.MOBILE,
							SearchItem.Comparison.LK, ac));
					f = true;
				}
			}
			if (f) {
				searchTerm.add(orsearchTerm);
			}
		}
		if (!isEmpty(name)) {
			searchTerm.add(new SearchItem<String>(UserSearchItemKey.NAME,
					SearchItem.Comparison.LK, name));
		}
		if (!isEmpty(company)) {
			searchTerm.add(new SearchItem<String>(
					UserSearchItemKey.COMPANY, SearchItem.Comparison.LK,
					company));
		}
		if (!isEmpty(department)) {
			searchTerm.add(new SearchItem<String>(
					UserSearchItemKey.DEPARTMENT, SearchItem.Comparison.LK,
					department));
		}
		if (!isEmpty(email)) {
			searchTerm.add(new SearchItem<String>(UserSearchItemKey.EMAIL,
					SearchItem.Comparison.LK, email));
		}
		if (!isEmpty(mobile)) {
			searchTerm.add(new SearchItem<String>(UserSearchItemKey.MOBILE,
					SearchItem.Comparison.LK, mobile));
		}
		if (!isEmpty(im)) {
			searchTerm.add(new SearchItem<String>(UserSearchItemKey.IM,
					SearchItem.Comparison.LK, im));
		}
		if (!isEmpty(status)) {
			searchTerm.add(new SearchItem<String>(UserSearchItemKey.STATUS,
					SearchItem.Comparison.EQ, status));
		}
		if (!isEmpty(userCreateType)) {
			searchTerm.add(new SearchItem<String>(UserSearchItemKey.CREATETYPE,
					SearchItem.Comparison.EQ, userCreateType));
		}

		// 搜索排序
		SortTerm sortTerm = new SortTerm();
		sortTerm.add(new AscSortItem(UserSortItemKey.ACCOUNT));
		sortTerm.add(new AscSortItem(UserSortItemKey.ID));

		// 搜索分页
		PageTerm pageTerm = new PageTerm();
		pageTerm.setBeginIndex(start);
		pageTerm.setPageSize(limit);

		PageNavigater<User> result = userService.searchAccount(searchTerm, sortTerm,
				pageTerm);

		User[] array = result.getContent();
		long totalCount = result.getItemsCount();
		List<User> list = Arrays.asList(array);
		Map<String, List<Member>> map = new HashMap<String, List<Member>>();
		for (User bean : array) {
			List<Member> memberBeans = map.get(bean.getAccount());
			if (memberBeans == null) {
				memberBeans = grouperService.getMembersByAccount(bean.getAccount());
				map.put(bean.getAccount(), memberBeans);
			}
		}

		StringBuffer sb = new StringBuffer();
		sb.append("{");
		sb.append("\"totalCount\":").append(totalCount).append(",");
		sb.append("\"accounts\":[");
		for (User bean : list) {
			sb.append("{");
			sb.append("\"account\":\"").append(bean.getAccount()).append("\",");
			sb.append("\"password\":\"").append("\",");
			sb.append("\"name\":\"").append(bean.getName() == null ? "" : JS.quote(HTML.escape(bean.getName()))).append("\",");
			sb.append("\"position\":\"").append(JS.quote(HTML.escape(bean.getPosition()))).append("\",");
			sb.append("\"company\":\"").append(bean.getCompany() == null ? "" : JS.quote(HTML.escape(bean.getCompany()))).append("\",");
			sb.append("\"department\":\"").append(bean.getDepartment() == null ? "" : JS.quote(HTML.escape(bean.getDepartment()))).append("\",");
			sb.append("\"email\":\"").append(bean.getEmail() == null ? "" : JS.quote(HTML.escape(bean.getEmail()))).append("\",");
			sb.append("\"phone\":\"").append(bean.getPhone() == null ? "" : JS.quote(HTML.escape(bean.getPhone()))).append("\",");
			sb.append("\"mobile\":\"").append(bean.getMobile() == null ? "" : JS.quote(HTML.escape(bean.getMobile()))).append("\",");
			sb.append("\"im\":\"").append(bean.getIm() == null ? "" : JS.quote(HTML.escape(bean.getIm()))).append("\",");
			sb.append("\"status\":\"").append(bean.getUserbaseStatus())
					.append("\",");

			List<Member> memberBeans = map.get(bean.getAccount());
			sb.append("\"members\":[");
			for (Member memberBean : memberBeans) {
				sb.append("{");
				sb.append("\"id\":").append(memberBean.getId()).append(",");
				sb.append("\"name\":\"").append(memberBean.getName()).append("\"");
				sb.append("},");
			}
			if (memberBeans.size() > 0)
				sb.setLength(sb.length() - 1);
			sb.append("]");
			sb.append("},");
		}
		if (list.size() > 0)
			sb.setLength(sb.length() - 1);
		sb.append("]}");
		return sb.toString();
	}

	private boolean isEmpty(String value) {
		if (value == null || value.trim().length() == 0) {
			return true;
		}
		return false;
	}

	/**
	 * 对柜子添加收藏
	 * @param id 柜子id
	 * @param type 收藏类型group或者thread(已弃用thread)
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value={"/addWatch","/addWatch_v2"}, produces = "application/json; charset=UTF-8")
	public String addWatch(Long[] id, String type) throws Exception {
		if (id == null || id.length == 0) {
			throw new GroupsException("收藏对象id不能为空");
		}
		Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
		Long memberId = currentUserWrap.getMemberId();
		String watchType = null;
		if ("group".equalsIgnoreCase(type)) {
			watchType = Watch.WATCH_TYPE_GROUP;
		} else {
			watchType = Watch.WATCH_TYPE_THREAD;
		}

		for (long _id : id) {
			groupService.addWatch(memberId, _id,watchType);
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 删除收藏
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value={"/deleteWatch","/deleteWatch_v2"}, produces = "application/json; charset=UTF-8")
	public String deleteWatch(Long[] id) throws Exception {
		if (id ==null || id.length <=0) {
			throw new GroupsException("收藏id不可为空");
		}
		groupService.deleteWatch(id);
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 注销用户
	 * @param account 账号数组
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/expiredAccount", produces = "application/json; charset=UTF-8")
	public String expiredAccount(String[] account) {
		if (account != null && account.length > 0) {
			for (String ac : account) {
				userService.expiredAccount(ac);
			}
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
	/**
	 * 注销用户
	 * @param account 账号数组
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/deleteAccount", produces = "application/json; charset=UTF-8")
	public String deleteAccount(String[] account) {
		StringBuffer stringBuffer=new StringBuffer();
		stringBuffer.append("{");
		int i=0;
		if (account != null && account.length > 0) {
			for (String ac : account) {
				String aa=null;
				if(!(aa=grouperService.deleteAccount(ac)).equals("")){
					i+=1;
					stringBuffer.append("\"detail\":\"").append(aa).append("\",");
				}

			}
		}
		if(i>1){
			stringBuffer.append("\"end\":\"").append("所选用户有"+i+"个管理员，请先删除他们的管理员权限").append("\",");
			stringBuffer.append("}");
			return stringBuffer.toString();
		}else if(i==1){
			stringBuffer.append("}");
			return stringBuffer.toString();
		}else
			return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}


	/**
	 * 激活用户
	 * @param account 账号数组
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/restoreAccount", produces = "application/json; charset=UTF-8")
	public String restoreAccount(String[] account) {
		if (account != null && account.length > 0) {
			for (String ac : account) {
				userService.restoreAccount(ac);
			}
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

    /**
     * 修改用户权限
     * @param memberId 账号数组
	 * @param targetId 分类或文件柜id
	 * @param modifyType 分类或文件柜类型
	 * @param perm 权限数组
     * @return
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value="/modifyPermissionOfMember", produces = "application/json; charset=UTF-8")
    public String modifyPermissionOfMember(Long memberId,Long[] targetId,String[] modifyType,String[] perm) {
        if (memberId == null || memberId<=0) {
            throw new GroupsException("memberId不合法");
        }
        if(targetId == null || targetId.length == 0){
            throw new GroupsException("targetId为空");
        }
        Long[] memberIdArray = {memberId};
        for(int i = 0;i<modifyType.length;i++){
        	String[] permArray = {perm[i]};
        	if("category".equals(modifyType[i])){
        		modifyMemberCategoryPermission(targetId[i],memberIdArray,permArray);
			}
        	if("group".equals(modifyType[i])){
        		modifyMemberGroupPermission(targetId[i],memberIdArray,permArray);
			}
		}
        return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
    }

	/**
	 * 导入用户
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = {"/importAccount","/importAccountToTeam"}, produces = "application/json; charset=UTF-8")
	public String importAccount(String from, String creator, Long teamId, HttpServletRequest request) throws Exception {
		Boolean autoCreateGroup = true;
		ArrayList<Long> list = new ArrayList<Long>();
		//上传文件
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		MultipartFile multipartFile =  null;
		multipartFile = multipartRequest.getFile("filedata");
		if (multipartFile == null) {
			multipartFile = multipartRequest.getFile("Filedata");
		}
		if (multipartFile == null) {
			throw new GroupsException("文件为空，请选择文件");
		}
		String filedataFileName = multipartFile.getOriginalFilename();
		String fileType = filedataFileName.substring(filedataFileName.lastIndexOf(".")+1, filedataFileName.length());
		File file = null;
		try {
			file = File.createTempFile("tmp", null);
			multipartFile.transferTo(file);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		FileInputStream fis = new FileInputStream(file);


		IExcelReader reader = null;
		if (fileType.equalsIgnoreCase("xlsx")) {
			reader = new SheetReader07(fis);
		} else if(fileType.equalsIgnoreCase("xls")) {
			reader = new SheetReader(fis);
		} else {
			throw new GroupsException("读取的不是Excel文件！");
		}
		List<Integer> exceptionList = new ArrayList<Integer>();
		Map<Integer,String> messageMap  = new HashMap<Integer,String>();
		int importNum = 0;
		int updateNum = 0;
		// 跳过第一行，因为第一行是标题
		reader.readRow();

		long sizeLimit = 0L;
		boolean limit = false;

		if (PropertyUtil.getUserMaxLimit() != null) {
			limit = true;
			long size = userService.getNumberOfUser() - userService.countAdmin(Admin.SUPER_ADMIN);
			if (size >= PropertyUtil.getUserMaxLimit().longValue()) {
				return "{\"type\":\"error\",\"code\":\"300\", \"detail\": \""+ ResourceProperty.getOverUserMaxLimitException(
						String.valueOf(PropertyUtil.getUserMaxLimit().longValue()))+ "\"}";
			}
			sizeLimit = PropertyUtil.getUserMaxLimit().longValue() - size;
		}

		while (true) {
			int currRow = reader.getCurRow() + 1;
			String[] cells = reader.readRow();

			if (cells.length < 1) {
				break;
			}

			if (cells.length == 1 ||cells[0] == null || cells[0].equals("") || cells[1] == null
					|| cells[1].equals("")) {
				exceptionList.add(currRow);
				continue;
			}

			String account = cells[0];
			String name = cells[1];
			String status = cells.length > 2 ? cells[2] : "";
			String company = cells.length > 3 ? cells[3] : null;
			String department = cells.length > 4 ? cells[4] : null;
			String email = cells.length > 5 ? cells[5] : null;
			String phone = cells.length > 6 ? cells[6] : null;
			String mobile = cells.length > 7 ? cells[7] : null;
			String IM = cells.length > 8 ? cells[8] : null;
			String positon = cells.length > 9 ? cells[9] : null;
			String pwd = cells.length>10?cells[10]:null;
			// 写入数据库
			User bean = null;
			bean = userService.getUserByAccount(account);
			if (bean == null || bean.getId() <= 0) {
				// 无此用户，创建新用户
				if (account != null&&!"".equals(account.trim())) {
					User  existAccount = userService.getUserByAccountOrMobile(account);
					if(existAccount!=null&&!existAccount.getAccount().equals(account)){
						messageMap.put(currRow, "账号"+account+" 已经绑定到账号"+existAccount.getAccount()+"的手机");
						exceptionList.add(currRow);
						continue;
					}
				}
				bean = new User();
				bean.setAccount(account);
				bean.setName(name);

				//密码强度太低的拒绝
				if (pwd != null && pwd.length() > 0) {
					try{
						userService.checkPasswordStrength(account,pwd);
					} catch (GroupsException e) {
						exceptionList.add(currRow);
						continue;
					}
					bean.setPassword(pwd);
				} else {
					//密码为空时随机生成
					bean.setPassword(UserService.getRandomPassword(8));
				}
				bean.setUserbaseStatus("delete".equals(status)?User.USER_STATUS_DELETE:User.USER_STATUS_NORMAL);
				bean.setCompany(company);
				bean.setDepartment(department);
				bean.setEmail(email);
				bean.setPhone(phone);
				//bean.setMobile(mobile);
				if (mobile != null&&!"".equals(mobile.trim())) {
					User  existAccount = userService.getUserByAccountOrMobile(mobile);
					if(existAccount!=null&&!existAccount.getAccount().equals(account)){
						exceptionList.add(currRow);
						messageMap.put(currRow, "账号"+account+"的手机号: "+mobile+" 已经绑定到账号"+existAccount.getAccount());
						continue;
					}
					bean.setMobile(mobile);
				}
				bean.setIm(IM);
				bean.setPosition(positon);
				if(from!=null&&!"".equals(from.trim())) {
					bean.setRegisterFrom(from);
				}
				if(creator!=null&&!"".equals(creator.trim())) {
					bean.setCreator(creator);
				}
				try {
					if (!limit || (limit && sizeLimit > 0)) {
						userService.createAccount(bean);
						sizeLimit--;
						importNum++;
					} else {
						exceptionList.add(currRow);
						continue;
					}
				} catch (GroupsException exception) {
					// 创建用户失败，不返回错误信息，添加到错误列表中
					exceptionList.add(currRow);
					continue;
				}
			} else {
				// 若有此用户，更新该用户信息
				bean.setName(name);
				bean.setUserbaseStatus(status);
				bean.setCompany(company);
				bean.setDepartment(department);
				bean.setEmail(email);
				bean.setPhone(phone);
				//bean.setMobile(mobile);
				if (mobile != null&&!"".equals(mobile.trim())) {
					User  existAccount = userService.getUserByAccountOrMobile(mobile);
					if(existAccount!=null&&!existAccount.getAccount().equals(account)){
						exceptionList.add(currRow);
						messageMap.put(currRow, "账号"+account+"的手机号: "+mobile+" 已经绑定到账号"+existAccount.getAccount());
						continue;
					}
					bean.setMobile(mobile);
				}
				bean.setIm(IM);
				bean.setPosition(positon);
				if(from!=null&&!"".equals(from.trim())) {
					bean.setRegisterFrom(from);
				}
				if(creator!=null&&!"".equals(creator.trim())) {
					bean.setCreator(creator);
				}
				try {
					userService.modifyAccount(bean);
				} catch (GroupsException exception) {
					exceptionList.add(currRow);
					continue;
				}
				updateNum++;
			}

			GroupType groupType=groupService.getGroupTypeByName("个人");
			Category categoryPerson=categoryService.getCategoryByName("#person");
			if(categoryPerson.getAvailableCapacity()<groupType.getTotalFileSize())
				throw new  GroupsException("个人资源库的可用容量不足");
			// 该用户没有马甲，则创建一个默认马甲
			List<Member> beans = grouperService.getMembersByAccount(account);
			if (beans.size() == 0) {
				Member memberBean = new Member();
				memberBean.setAccount(account);
				memberBean.setEmail(bean.getEmail());
				memberBean.setName(account);
				memberBean.setMemberStatus(Member.STATUS_NORMAL);
				memberBean.setMemberType(Member.MEMBER_TYPE_PERSON);

				grouperService.createMember(memberBean);
				list.add(memberBean.getId());
				//创建用户时自动创建个人文件柜
				if (autoCreateGroup) {
					String[] categoryNames = PropertyUtil.getDefaultGroupCategory()[0]
							.split(":");
					String categoryName = categoryNames[0];
					String categoryDisplayName = categoryNames[1];
					Category category = null;
					List<Category> categories= categoryService.getCategoriesByName(categoryName);
					if (categories == null || categories.size() <= 0) {
						category = new Category();
						category.setName(categoryName);
						category.setDisplayName(categoryDisplayName);
						category.setParentId(0);
						category.setCategoryStatus(Category.STATUS_NORMAL);
						category.setCreateDate(new Timestamp(System
								.currentTimeMillis()));
						categoryService.createCategory(category, false, OldPerm.SYSTEMADMIN_MEMBER_ID);
					} else {
						category = categories.get(0);
					}
					Group group = groupService.getGroupByName(memberBean.getId().toString());
					if (group == null || group.getId() < 0) {
						Group groupBean = new Group();
						groupBean.setName("" + memberBean.getId());
						groupBean.setCreatorName(memberBean.getAccount());
						groupBean.setDisplayName(memberBean.getAccount());
						groupBean.setAddr("" + memberBean.getId());
						groupBean.setCategory(category);
						groupBean.setCreateDate(new Timestamp(System
								.currentTimeMillis()));
						groupBean.setCreatorId(memberBean.getId());
						groupBean.setGroupStatus(Group.STATUS_NORMAL);
						groupBean.setGroupUsage(Group.USAGE_PRIVATE);
						groupBean.setOwner(memberBean.getAccount());

						GroupType typeBean = groupService.getGroupTypeByName(PropertyUtil
								.getPersonalGroupType());
						groupBean.setGroupType(typeBean);
						//修改导入的用户柜子大小

						groupBean.setTotalFileSize(groupType.getTotalFileSize());
						groupBean.setAvailableCapacity(groupType.getTotalFileSize());
						groupBean.setUsedCapacity(0L);

//						初始化topCtegoryId
						if(categoryPerson.getAvailableCapacity()>groupType.getTotalFileSize()){
							Long total=groupType.getTotalFileSize();
							groupBean.setTopCategoryId(categoryPerson.getId());
							categoryPerson.setAvailableCapacity(categoryPerson.getAvailableCapacity()-total);
						    categoryService.saveOrUpdateCategory(categoryPerson);
						}else {
							throw new GroupsException("个人资源库的可用容量不足\n");
						}
						groupService.createGroup_v1(bean,groupBean, false, null, OldPerm.SYSTEMADMIN_MEMBER_ID);

						PermCollection pc = new PermCollection();

						try {
							permService.modifyPermission(groupBean.getId(), memberBean.getId(), OldPerm.PERM_TYPE_GROUP, pc);
						} catch (Exception e) {
							//todo nothing
						}


						resourceService.createResourceDir(groupBean.getId(), PropertyUtil
								.getRecyclerName(), 0, memberBean.getId(), true);
						resourceService.createResourceDir(groupBean.getId(), "待办事项", 0,
								memberBean.getId(), true, GroupResource.DOCUMENT_TYPE_UNKNOWN, 0);
						resourceService.createResourceDir(groupBean.getId(), "我的文档", 0,
								memberBean.getId(), true, GroupResource.DOCUMENT_TYPE_UNKNOWN, 0);
						resourceService.createResourceDir(groupBean.getId(), "我的图片", 0,
								memberBean.getId(), true, GroupResource.DOCUMENT_TYPE_UNKNOWN, 0);
						resourceService.createResourceDir(groupBean.getId(), "我的视频", 0,
								memberBean.getId(), true, GroupResource.DOCUMENT_TYPE_UNKNOWN, 0);
						//创建邮件大附件文件夹
						resourceService.getPersonEmailAttachFolder(memberBean.getId());
					}
				}
			} else {
				list.add(beans.get(0).getId());
			}
		}
		fis.close();


		if(teamId != null && teamId>0&&list.size()>0){
			long[] memberIds = new long[list.size()];
			for(int i = 0;i<list.size();i++){
				memberIds[i] = list.get(i);
			}
			grouperService.addMemberToTeam(memberIds,teamId);
			//更新lastModified
			grouperService.shiftUpLastModified(teamId, new Date());
		}
		//删除multipartFile转换成File的临时文件
		if(file.exists()){
			file.delete();
		}
		StringBuffer sb = new StringBuffer();
		sb.append("{\"detail\":\"上传成功\",").append("\"import\":").append(importNum);
		sb.append(",\"update\":").append(updateNum);
		sb.append(",\"importExceptionRow\":").append("[");
		for (int i = 0; i < exceptionList.size(); i++) {
			sb.append(exceptionList.get(i)).append(",");
		}
		if (exceptionList.size() > 0)
			sb.setLength(sb.length() - 1);
		sb.append("]}");

		return sb.toString();
	}

	/**
	 * 导出用户
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/exportAccount", produces = "application/json; charset=UTF-8")
	public String exportAccount(HttpServletResponse response) throws Exception {
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename="+ PropertyUtil.getExportAccountFileName());
		OutputStream outputStream = null;
		try {
			outputStream = response.getOutputStream();
			userService.exportAccount(outputStream);
			if (outputStream != null)
				outputStream.close();
		} catch (Exception e) {
			if (outputStream != null)
				outputStream.close();
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 导出用户
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/exportAccountFromTeam", produces = "application/json; charset=UTF-8")
	public String exportAccountFromTeam(Long teamId, HttpServletResponse response) throws Exception{
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename="
				+ PropertyUtil.getExportAccountFileName());

		OutputStream outputStream = null;
		try {
			Member team = grouperService.getMemberById(teamId);
			if(!Member.MEMBER_TYPE_TEAM.equals(team.getMemberType())){
				throw new GroupsException("组id参数错误");
			}
			outputStream = response.getOutputStream();
			grouperService.exportAccountFromTeam(outputStream,teamId);
			if (outputStream != null)
				outputStream.close();
		} finally{
			if (outputStream != null)
				outputStream.close();
		}

		return null;
	}


	/**
	 * 获取分类权限
	 * @param categoryId 分类id
	 * @return
	 */
//	@RequiresUser
//	@ResponseBody
//	@RequestMapping(value = "/getCategoryPermssion", produces = "application/json; charset=UTF-8")
//	public String getCategoryPermssion(Long categoryId) {
//		if (categoryId == null || categoryId == 0) {
//			throw new GroupsException("分类id不能为空");
//		}
//		Set<Long> memberIds = new HashSet<Long>();
//		List<Member> memberList = new ArrayList<Member>();
//		List<String> memberNameList = new ArrayList<String>();
//		//获得分类的分类类型权限
//		List<OldPerm> cbeans = permService.getPerms(categoryId, OldPerm.PERM_TYPE_CATEGORY);
//		//获得分类的柜子类型权限
//		List<OldPerm> beans = permService.getPerms(categoryId, OldPerm.PERM_TYPE_GROUP_OF_CATEGORY);
//
//		List<OldPerm> allBeans = new ArrayList<OldPerm>();
//		allBeans.addAll(cbeans);
//		allBeans.addAll(beans);
//
//		Map<Long, OldPerm> groupPermBeanMap = new HashMap<Long, OldPerm>();//key is memberId
//		Map<Long, OldPerm> cateogryPermBeanMap = new HashMap<Long, OldPerm>();//key is memberId
//		Map<Long, PermCollection> permissionMap = new HashMap<Long, PermCollection>();
//		//index = 0;
//		for (OldPerm bean : allBeans) {
//			long memberId_tmp = bean.getMemberId();
//			Member member = grouperService.getMemberById(memberId_tmp);
//			String displayName = member.getName();
//
//			if (member.getMemberType().equalsIgnoreCase(Member.MEMBER_TYPE_FOLDER)) {
//				Member folder = grouperService.getFolder(member.getId());
//				displayName = folder.getSignature();
//			}
//			if (member.getMemberType().equalsIgnoreCase(Member.MEMBER_TYPE_TEAM)) {
//				Member team = grouperService
//						.getTeam(member.getId());
//				displayName = team.getSignature().replace(":", "/");
//			}
//			if (!memberIds.contains(member.getId())) {
//				memberIds.add(member.getId());
//				memberList.add(member);
//				memberNameList.add(displayName);
//			}
//			if (bean.getPermType() == OldPerm.PERM_TYPE_CATEGORY) {
//				cateogryPermBeanMap.put(member.getId(), bean);
//				PermCollection pp = permService.getPermission(categoryId, member.getId(), OldPerm.PERM_TYPE_CATEGORY);
//				if (permissionMap.containsKey(member.getId())) {
//					PermCollection pc = permissionMap.get(member.getId());
//					pc.addCategoryPerm(pp.getCategoryPerm());
//				} else {
//					permissionMap.put(member.getId(), pp);
//				}
//
//
//			} else {
//				groupPermBeanMap.put(member.getId(), bean);
//
//				PermCollection pp = permService.getGroupPermissionToCategory(categoryId, member.getId());
//				if (permissionMap.containsKey(member.getId())) {
//					PermCollection pc = permissionMap.get(member.getId());
//					pc.addInheritedPerm(pp.getGroupInheritedCategoryPerm());
//					pc.addGroupPerm(pp.getGroupPerm());
//				} else {
//					permissionMap.put(member.getId(), pp);
//				}
//			}
//			//index++;
//		}
//
//		String[] idens = new String[memberList.size()];
//		for (int i = 0; i < memberList.size(); ++i) {
//			if (permService.isAdmin(memberList.get(i).getId())) {
//				idens[i] = "admin";
//			} else {
//				idens[i] = "member";
//			}
//		}
//
//		int totalCount = memberList.size();
//		StringBuffer buffer = new StringBuffer();
//		buffer.append("{");
//		buffer.append("\"totalCount\":").append(totalCount).append(",");
//		buffer.append("\"members\":[");
//		for (int i = 0; i < totalCount; ++i) {
//			Member member = memberList.get(i);
//			long memberId = member.getId();
//			buffer.append("{");
//			buffer.append("\"signatrue\":\"").append(JS.quote(Filter.convertHtmlBody(member.getSignature()))).append("\"").append(",");
//			if (member.getIcon() != null
//					&& member.getIcon().length() != 0) {
//				buffer.append("\"icon\":\"").append(PropertyUtil.getIconPrefix()).append(member.getIcon()).append("\"").append(",");
//			} else {
//				buffer.append("\"icon\":\"").append(PropertyUtil.getDefaultIcon()).append("\",");
//			}
//
//			long groupTypeId = groupPermBeanMap.containsKey(memberId) ? groupPermBeanMap.get(memberId).getId() : 0;
//			long categoryTypeId = cateogryPermBeanMap.containsKey(memberId) ? cateogryPermBeanMap.get(memberId).getId() : 0;
//
//			buffer.append("\"groupTypePermId\":").append(groupTypeId).append(",");
//			buffer.append("\"categoryTypePermId\":").append(categoryTypeId).append(",");
//			buffer.append("\"id\":").append(member.getId()).append(",");
//			buffer.append("\"name\":\"").append(memberNameList.get(i)).append("\",");
//			buffer.append("\"iden\":\"").append(idens[i]).append("\",");
//			PermCollection pc = permissionMap.get(member.getId());
//			IPermission.GroupPerm[] gps = pc.getGroupPerm();
//			IPermission.GroupPerm[] inheritedGps = pc.getGroupInheritedCategoryPerm();
//			buffer.append("\"addcategory\":").append(PermUtil.containCategoryPermission(pc.getCategoryPerm(), IPermission.CategoryPerm.CREATE_CATEGORY)).append(",");
//			buffer.append("\"managecategory\":").append(PermUtil.containCategoryPermission(pc.getCategoryPerm(), IPermission.CategoryPerm.MANAGE_CATEGORY)).append(",");
//			buffer.append("\"addgroup\":").append(PermUtil.containCategoryPermission(pc.getCategoryPerm(), IPermission.CategoryPerm.CREATE_GROUP)).append(",");
//			buffer.append("\"managegroup\":").append(PermUtil.containPermission(gps, IPermission.GroupPerm.MANAGE_GROUP)).append(",");
//			buffer.append("\"managegroupInherit\":").append(PermUtil.containPermission(inheritedGps, IPermission.GroupPerm.MANAGE_GROUP)).append(",");
//			buffer.append("\"upload\":").append(PermUtil.containPermission(gps, IPermission.GroupPerm.UPLOAD_RESOURCE)).append(",");
//			buffer.append("\"uploadInherit\":").append(PermUtil.containPermission(inheritedGps, IPermission.GroupPerm.UPLOAD_RESOURCE)).append(",");
//			buffer.append("\"delete\":").append(PermUtil.containPermission(gps, IPermission.GroupPerm.DELETE_RESOURCE)).append(",");
//			buffer.append("\"deleteInherit\":").append(PermUtil.containPermission(inheritedGps, IPermission.GroupPerm.DELETE_RESOURCE)).append(",");
//			buffer.append("\"addDir\":").append(PermUtil.containPermission(gps, IPermission.GroupPerm.ADD_FOLDER)).append(",");
//			buffer.append("\"addDirInherit\":").append(PermUtil.containPermission(inheritedGps, IPermission.GroupPerm.ADD_FOLDER)).append(",");
//			buffer.append("\"modify\":").append(PermUtil.containPermission(gps, IPermission.GroupPerm.MODIFY_RESOURCE)).append(",");
//			buffer.append("\"modifyInherit\":").append(PermUtil.containPermission(inheritedGps, IPermission.GroupPerm.MODIFY_RESOURCE));
//			buffer.append("},");
//		}
//		if (totalCount > 0) {
//			buffer.setLength(buffer.length() - 1);
//		}
//		buffer.append("]");
//		buffer.append("}");
//		return buffer.toString();
//	}

	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getCategoryPermssion", produces = "application/json; charset=UTF-8")
	public String getCategoryPermssion(Long categoryId,int start,int limit) {
		if (categoryId == null || categoryId == 0) {
			throw new GroupsException("分类id不能为空");
		}
		Set<Long> memberIds = new HashSet<Long>();
		List<Member> memberList = new ArrayList<Member>();
		List<String> memberNameList = new ArrayList<String>();
		//获得分类的分类类型权限
		List<OldPerm> cbeans = permService.getPermsPart(categoryId, OldPerm.PERM_TYPE_CATEGORY,start,limit);
		//获得分类的柜子类型权限
		List<OldPerm> beans = permService.getPermsPart(categoryId, OldPerm.PERM_TYPE_GROUP_OF_CATEGORY,start,limit);

		List<OldPerm> allBeans = new ArrayList<OldPerm>();
		allBeans.addAll(cbeans);
		allBeans.addAll(beans);

		Map<Long, OldPerm> groupPermBeanMap = new HashMap<Long, OldPerm>();//key is memberId
		Map<Long, OldPerm> cateogryPermBeanMap = new HashMap<Long, OldPerm>();//key is memberId
		Map<Long, PermCollection> permissionMap = new HashMap<Long, PermCollection>();
		//index = 0;
		for (OldPerm bean : allBeans) {
			long memberId_tmp = bean.getMemberId();
			Member member = grouperService.getMemberById(memberId_tmp);
			if(member==null)
				continue;
			String displayName = member.getName();

			if (member.getMemberType().equalsIgnoreCase(Member.MEMBER_TYPE_FOLDER)) {
				Member folder = grouperService.getFolder(member.getId());
				displayName = folder.getSignature();
			}
			if (member.getMemberType().equalsIgnoreCase(Member.MEMBER_TYPE_TEAM)) {
				Member team = grouperService.getTeam(member.getId());
				displayName = team.getSignature().replace(":", "/");
			}
			if (!memberIds.contains(member.getId())) {
				memberIds.add(member.getId());
				memberList.add(member);
				memberNameList.add(displayName);
			}
			if (bean.getPermType() == OldPerm.PERM_TYPE_CATEGORY) {
				cateogryPermBeanMap.put(member.getId(), bean);
				PermCollection pp = permService.getPermission(categoryId, member.getId(), OldPerm.PERM_TYPE_CATEGORY);
				if (permissionMap.containsKey(member.getId())) {
					PermCollection pc = permissionMap.get(member.getId());
					pc.addCategoryPerm(pp.getCategoryPerm());
				} else {
					permissionMap.put(member.getId(), pp);
				}


			} else {
				groupPermBeanMap.put(member.getId(), bean);
				PermCollection pp = permService.getGroupPermissionToCategory(categoryId, member.getId());
				if (permissionMap.containsKey(member.getId())) {
					PermCollection pc = permissionMap.get(member.getId());
					pc.addInheritedPerm(pp.getGroupInheritedCategoryPerm());
					pc.addGroupPerm(pp.getGroupPerm());
				} else {
					permissionMap.put(member.getId(), pp);
				}
			}
			//index++;
		}

		String[] idens = new String[memberList.size()];
		for (int i = 0; i < memberList.size(); ++i) {
			if (permService.isAdmin(memberList.get(i).getId())) {
				idens[i] = "admin";
			} else {
				idens[i] = "member";
			}
		}
//获得分类的分类类型权限
        List<OldPerm> cbeanall = permService.getPerms(categoryId, OldPerm.PERM_TYPE_CATEGORY);
        //获得分类的柜子类型权限
        List<OldPerm> beanall = permService.getPerms(categoryId, OldPerm.PERM_TYPE_GROUP_OF_CATEGORY);

		Set<Long> memberIdsAll = new HashSet<Long>();
		List<Member> memberListAll = new ArrayList<Member>();
        List<OldPerm>  all =new ArrayList<>();
        all.addAll(cbeanall);
        all.addAll(beanall);
		for (OldPerm bean : all) {
			long memberId_tmp = bean.getMemberId();
			Member member = grouperService.getMemberById(memberId_tmp);
			if(member==null)
				continue;

			if (!memberIdsAll.contains(member.getId())) {
				memberIdsAll.add(member.getId());
				memberListAll.add(member);
			}
		}

//		int totalCount =cbeanall.size()+beanall.size();
		int totalCount = memberListAll.size();
		if(start>=totalCount || totalCount==0){
			StringBuffer stringBuffer=new StringBuffer();
			stringBuffer.append("{\"totalCount\":").append(totalCount).append(",");
			stringBuffer.append("\"members\":[ ]").append("}");
			return stringBuffer.toString();
		}
		if(start<0){
			StringBuffer stringBuffer=new StringBuffer();
			stringBuffer.append("{\"totalCount\":").append(totalCount).append(",");
			stringBuffer.append("\"message\":").append("\"parameter start is illegal\"").append("}");
			return stringBuffer.toString();
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		buffer.append("\"totalCount\":").append(totalCount).append(",");
		buffer.append("\"members\":[");
		for (int i = 0; i < memberList.size(); ++i) {
			Member member = memberList.get(i);
			long memberId = member.getId();
			buffer.append("{");
			buffer.append("\"signatrue\":\"").append(JS.quote(Filter.convertHtmlBody(member.getSignature()))).append("\"").append(",");
			if (member.getIcon() != null
					&& member.getIcon().length() != 0) {
				buffer.append("\"icon\":\"").append(PropertyUtil.getIconPrefix()).append(member.getIcon()).append("\"").append(",");
			} else {
				buffer.append("\"icon\":\"").append(PropertyUtil.getDefaultIcon()).append("\",");
			}

			long groupTypeId = groupPermBeanMap.containsKey(memberId) ? groupPermBeanMap.get(memberId).getId() : 0;
			long categoryTypeId = cateogryPermBeanMap.containsKey(memberId) ? cateogryPermBeanMap.get(memberId).getId() : 0;

			buffer.append("\"groupTypePermId\":").append(groupTypeId).append(",");
			buffer.append("\"categoryTypePermId\":").append(categoryTypeId).append(",");
			buffer.append("\"id\":").append(member.getId()).append(",");
			buffer.append("\"name\":\"").append(memberNameList.get(i)).append("\",");
			buffer.append("\"iden\":\"").append(idens[i]).append("\",");
			PermCollection pc = permissionMap.get(member.getId());
			IPermission.GroupPerm[] gps = pc.getGroupPerm();
			IPermission.GroupPerm[] inheritedGps = pc.getGroupInheritedCategoryPerm();
			buffer.append("\"addcategory\":").append(PermUtil.containCategoryPermission(pc.getCategoryPerm(), IPermission.CategoryPerm.CREATE_CATEGORY)).append(",");
			buffer.append("\"managecategory\":").append(PermUtil.containCategoryPermission(pc.getCategoryPerm(), IPermission.CategoryPerm.MANAGE_CATEGORY)).append(",");
			buffer.append("\"addgroup\":").append(PermUtil.containCategoryPermission(pc.getCategoryPerm(), IPermission.CategoryPerm.CREATE_GROUP)).append(",");
			buffer.append("\"managegroup\":").append(PermUtil.containPermission(gps, IPermission.GroupPerm.MANAGE_GROUP)).append(",");
			buffer.append("\"managegroupInherit\":").append(PermUtil.containPermission(inheritedGps, IPermission.GroupPerm.MANAGE_GROUP)).append(",");
			buffer.append("\"upload\":").append(PermUtil.containPermission(gps, IPermission.GroupPerm.UPLOAD_RESOURCE)).append(",");
			buffer.append("\"uploadInherit\":").append(PermUtil.containPermission(inheritedGps, IPermission.GroupPerm.UPLOAD_RESOURCE)).append(",");
			buffer.append("\"delete\":").append(PermUtil.containPermission(gps, IPermission.GroupPerm.DELETE_RESOURCE)).append(",");
			buffer.append("\"deleteInherit\":").append(PermUtil.containPermission(inheritedGps, IPermission.GroupPerm.DELETE_RESOURCE)).append(",");
			buffer.append("\"addDir\":").append(PermUtil.containPermission(gps, IPermission.GroupPerm.ADD_FOLDER)).append(",");
			buffer.append("\"addDirInherit\":").append(PermUtil.containPermission(inheritedGps, IPermission.GroupPerm.ADD_FOLDER)).append(",");
			buffer.append("\"modify\":").append(PermUtil.containPermission(gps, IPermission.GroupPerm.MODIFY_RESOURCE)).append(",");
			buffer.append("\"modifyInherit\":").append(PermUtil.containPermission(inheritedGps, IPermission.GroupPerm.MODIFY_RESOURCE));
			buffer.append("},");
		}
		if (memberList.size() > 0) {
			buffer.setLength(buffer.length() - 1);
		}
		buffer.append("]");
		buffer.append("}");
		return buffer.toString();
	}


	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getCategoryPermission", produces = "application/json; charset=UTF-8")
	public String getCategoryPermission(Long categoryId, @RequestParam(required = false	,defaultValue = "0")Integer start, @RequestParam(required = false	,defaultValue = "40")Integer limit) {
		if (categoryId == null || categoryId == 0) {
			throw new GroupsException("分类id不能为空");
		}
		Set<Long> memberIds = new HashSet<Long>();
		List<Member> memberList = new ArrayList<Member>();
		List<String> memberNameList = new ArrayList<String>();
		//获得分类的分类类型权限
		List<OldPerm> cbeans = permService.getPermsPart(categoryId, OldPerm.PERM_TYPE_CATEGORY,start,limit);
		//获得分类的柜子类型权限
		List<OldPerm> beans = permService.getPermsPart(categoryId, OldPerm.PERM_TYPE_GROUP_OF_CATEGORY,start,limit);

		List<OldPerm> allBeans = new ArrayList<OldPerm>();
		allBeans.addAll(cbeans);
		allBeans.addAll(beans);

		List<Member> list=new ArrayList<Member>();
		Set<Long> memberIdss = new HashSet<Long>();
		List<OldPerm> cbeansAll=permService.getPerms(categoryId, OldPerm.PERM_TYPE_CATEGORY);
		List<OldPerm> beansAll=permService.getPerms(categoryId, OldPerm.PERM_TYPE_GROUP_OF_CATEGORY);
		List<OldPerm> allBeansall = new ArrayList<OldPerm>();
		allBeansall.addAll(cbeansAll);
		allBeansall.addAll(beansAll);

		Map<Long, OldPerm> groupPermBeanMap = new HashMap<Long, OldPerm>();//key is memberId
		Map<Long, OldPerm> cateogryPermBeanMap = new HashMap<Long, OldPerm>();//key is memberId
		Map<Long, PermCollection> permissionMap = new HashMap<Long, PermCollection>();
		//index = 0;
		for (OldPerm bean : allBeans) {
			long memberId_tmp = bean.getMemberId();
			Member member = grouperService.getMemberById(memberId_tmp);
			if(member==null)
				continue;
			String displayName = member.getName();

			if (member.getMemberType().equalsIgnoreCase(Member.MEMBER_TYPE_FOLDER)) {
				Member folder = grouperService.getFolder(member.getId());
				displayName = folder.getSignature();
			}
			if (member.getMemberType().equalsIgnoreCase(Member.MEMBER_TYPE_TEAM)) {
				Member team = grouperService.getTeam(member.getId());
				displayName = team.getSignature().replace(":", "/");
			}
			if (!memberIds.contains(member.getId())) {
				memberIds.add(member.getId());
				memberList.add(member);
				memberNameList.add(displayName);
			}
			if (bean.getPermType() == OldPerm.PERM_TYPE_CATEGORY) {
				cateogryPermBeanMap.put(member.getId(), bean);
				PermCollection pp = permService.getPermission(categoryId, member.getId(), OldPerm.PERM_TYPE_CATEGORY);
				if (permissionMap.containsKey(member.getId())) {
					PermCollection pc = permissionMap.get(member.getId());
					pc.addCategoryPerm(pp.getCategoryPerm());
				} else {
					permissionMap.put(member.getId(), pp);
				}

			} else {
				groupPermBeanMap.put(member.getId(), bean);

				PermCollection pp = permService.getGroupPermissionToCategory(categoryId, member.getId());
				if (permissionMap.containsKey(member.getId())) {
					PermCollection pc = permissionMap.get(member.getId());
					pc.addInheritedPerm(pp.getGroupInheritedCategoryPerm());
					pc.addGroupPerm(pp.getGroupPerm());
				} else {
					permissionMap.put(member.getId(), pp);
				}
			}
			//index++;
		}


		for (OldPerm bean : allBeansall) {
			long memberId_tmp = bean.getMemberId();
			Member member = grouperService.getMemberById(memberId_tmp);
			if(member==null)
				continue;
			if (!memberIdss.contains(member.getId())) {
				memberIdss.add(member.getId());
				list.add(member);
			}
			//index++;
		}
		int total=list.size();
		String[] idens = new String[memberList.size()];
		for (int i = 0; i < memberList.size(); ++i) {
			if (permService.isAdmin(memberList.get(i).getId())) {
				idens[i] = "admin";
			} else {
				idens[i] = "member";
			}
		}



		int totalCount = memberList.size();
		if(start>=totalCount || totalCount==0){
			StringBuffer stringBuffer=new StringBuffer();
//			stringBuffer.append("{\"totalCount\":").append(totalCount).append(",");
			stringBuffer.append("{\"totalCount\":").append(total).append(",");
			stringBuffer.append("\"members\":[ ]").append("}");
			return stringBuffer.toString();
		}
		if(start<0){
			StringBuffer stringBuffer=new StringBuffer();
//			stringBuffer.append("{\"totalCount\":").append(totalCount).append(",");
			stringBuffer.append("{\"totalCount\":").append(total).append(",");
			stringBuffer.append("\"message\":").append("\"parameter start is illegal\"").append("}");
			return stringBuffer.toString();
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		buffer.append("\"totalCount\":").append(total).append(",");
//		buffer.append("\"totalCount\":").append(totalCount).append(",");
		buffer.append("\"members\":[");
		for (int i = 0; i < memberList.size(); ++i) {
			Member member = memberList.get(i);
			long memberId = member.getId();
			buffer.append("{");
			buffer.append("\"signatrue\":\"").append(JS.quote(Filter.convertHtmlBody(member.getSignature()))).append("\"").append(",");
			if (member.getIcon() != null
					&& member.getIcon().length() != 0) {
				buffer.append("\"icon\":\"").append(PropertyUtil.getIconPrefix()).append(member.getIcon()).append("\"").append(",");
			} else {
				buffer.append("\"icon\":\"").append(PropertyUtil.getDefaultIcon()).append("\",");
			}

			long groupTypeId = groupPermBeanMap.containsKey(memberId) ? groupPermBeanMap.get(memberId).getId() : 0;
			long categoryTypeId = cateogryPermBeanMap.containsKey(memberId) ? cateogryPermBeanMap.get(memberId).getId() : 0;

			buffer.append("\"groupTypePermId\":").append(groupTypeId).append(",");
			buffer.append("\"categoryTypePermId\":").append(categoryTypeId).append(",");
			buffer.append("\"id\":").append(member.getId()).append(",");
			buffer.append("\"name\":\"").append(memberNameList.get(i)).append("\",");
			buffer.append("\"iden\":\"").append(idens[i]).append("\",");
			PermCollection pc = permissionMap.get(member.getId());
			IPermission.GroupPerm[] gps = pc.getGroupPerm();
			IPermission.GroupPerm[] inheritedGps = pc.getGroupInheritedCategoryPerm();
			buffer.append("\"addcategory\":").append(PermUtil.containCategoryPermission(pc.getCategoryPerm(), IPermission.CategoryPerm.CREATE_CATEGORY)).append(",");
			buffer.append("\"managecategory\":").append(PermUtil.containCategoryPermission(pc.getCategoryPerm(), IPermission.CategoryPerm.MANAGE_CATEGORY)).append(",");
			buffer.append("\"addgroup\":").append(PermUtil.containCategoryPermission(pc.getCategoryPerm(), IPermission.CategoryPerm.CREATE_GROUP)).append(",");
			buffer.append("\"managegroup\":").append(PermUtil.containPermission(gps, IPermission.GroupPerm.MANAGE_GROUP)).append(",");
			buffer.append("\"managegroupInherit\":").append(PermUtil.containPermission(inheritedGps, IPermission.GroupPerm.MANAGE_GROUP)).append(",");
			buffer.append("\"upload\":").append(PermUtil.containPermission(gps, IPermission.GroupPerm.UPLOAD_RESOURCE)).append(",");
			buffer.append("\"uploadInherit\":").append(PermUtil.containPermission(inheritedGps, IPermission.GroupPerm.UPLOAD_RESOURCE)).append(",");
			buffer.append("\"delete\":").append(PermUtil.containPermission(gps, IPermission.GroupPerm.DELETE_RESOURCE)).append(",");
			buffer.append("\"deleteInherit\":").append(PermUtil.containPermission(inheritedGps, IPermission.GroupPerm.DELETE_RESOURCE)).append(",");
			buffer.append("\"addDir\":").append(PermUtil.containPermission(gps, IPermission.GroupPerm.ADD_FOLDER)).append(",");
			buffer.append("\"addDirInherit\":").append(PermUtil.containPermission(inheritedGps, IPermission.GroupPerm.ADD_FOLDER)).append(",");
			buffer.append("\"modify\":").append(PermUtil.containPermission(gps, IPermission.GroupPerm.MODIFY_RESOURCE)).append(",");
			buffer.append("\"modifyInherit\":").append(PermUtil.containPermission(inheritedGps, IPermission.GroupPerm.MODIFY_RESOURCE));
			buffer.append("},");
		}
		if (memberList.size() > 0) {
			buffer.setLength(buffer.length() - 1);
		}
		buffer.append("]");
		buffer.append("}");
		return buffer.toString();
	}

	/**
	 * 修改分类权限
	 * @param categoryId 分类id
	 * @param memberId 用户或者team 的memberId数组
	 * @param perm 权限数组，与memberId对应
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/modifyMemberCategoryPermission", produces = "application/json; charset=UTF-8")
	public String modifyMemberCategoryPermission(Long categoryId, Long[] memberId, String[] perm){
		if (categoryId == null || categoryId == 0) {
			throw new GroupsException("分类id为空");
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

		int length = Math.min(memberId.length, perms.size());
		for (int i = 0; i < length; ++i) {
			long _memberId = memberId[i];
			String p = perms.get(i);

			PermCollection pc = permService.getPermission(categoryId,_memberId, OldPerm.PERM_TYPE_CATEGORY);

			// 清空权限
			pc.removeCategoryPerm(new CategoryPerm[] { CategoryPerm.CREATE_CATEGORY,
					CategoryPerm.CREATE_GROUP, CategoryPerm.MANAGE_CATEGORY});

			// 解析需要设置的权限，并设置
			List<CategoryPerm> list = new ArrayList<CategoryPerm>();
			PermissionParseUtil.parseCategoryPermission(list, p);

			pc.addCategoryPerm(list.toArray(new CategoryPerm[list.size()]));
			// 重新设置权限
			permService.modifyPermission(categoryId, _memberId,OldPerm.PERM_TYPE_CATEGORY, pc);
			//设置对圈子内容的权限
			pc = permService.getPermission(categoryId,_memberId, OldPerm.PERM_TYPE_GROUP_OF_CATEGORY);
			// 清空四种权限
			pc.removeGroupSelfPerm(new GroupPerm[] { GroupPerm.VIEW_RESOURCE,GroupPerm.UPLOAD_RESOURCE,
					GroupPerm.DELETE_RESOURCE, GroupPerm.MODIFY_RESOURCE,GroupPerm.DOWNLOAD_RESOURCE,
					GroupPerm.ADD_FOLDER,GroupPerm.MANAGE_GROUP});

			// 解析需要设置的权限，并设置
			List<GroupPerm> glist = new ArrayList<GroupPerm>();
			PermissionParseUtil.parseGroupPermission(glist, p);
			glist.add(GroupPerm.VIEW_GROUP);
			glist.add(GroupPerm.VIEW_RESOURCE);
			glist.add(GroupPerm.DOWNLOAD_RESOURCE);
			if (list.contains(CategoryPerm.CREATE_GROUP) ||
					list.contains(CategoryPerm.MANAGE_CATEGORY)) {
				glist.add(GroupPerm.MANAGE_GROUP);
			}

			pc.addGroupSelfPerm(glist.toArray(new GroupPerm[glist.size()]));

			// 重新设置权限
			permService.modifyPermission(categoryId, _memberId,
					OldPerm.PERM_TYPE_GROUP_OF_CATEGORY, pc);
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 根据权限id删除分类权限
	 * @param permBeansId 权限id数组
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/deleteMemberPermssion", produces = "application/json; charset=UTF-8")
	public String deleteMemberPermssion(Long[] permBeansId) throws Exception {
		if (permBeansId != null && permBeansId.length > 0) {
			for(long _id : permBeansId) {
				permService.deletePerm(_id);
			}
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 将用户加入柜子
	 *
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/joinGroup", produces = "application/json; charset=UTF-8")
	public String joinGroup(String type, String[] name, Long groupId) throws Exception {
		if (groupId == null || groupId == 0) {
			throw new GroupsException("柜子id为空");
		}
		if (name == null || name.length <= 0) {
			throw new GroupsException("用户列表为空");
		}
		Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
		Long memberId = currentUserWrap.getMemberId();
		// 管理员强制某人加入某圈子
		if ("god".equalsIgnoreCase(type)
				&& (permService.isAdmin(memberId) || permService.hasGroupPermission(memberId, groupId, GroupPerm.MANAGE_GROUP))) {
			for (String n : name) {
				List<Member> memberBeans = grouperService.getMembersByName(n);
				for (Member memberBean : memberBeans) {
					groupService.bindMemberToGroup(memberBean.getId(), groupId);
				}
			}
			return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
		} else {
			throw new GroupsException("操作者权限不够");
		}
	}

	/**
	 * 将用户加入柜子的同时赋予权限
	 *
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/joinGroupPermission", produces = "application/json; charset=UTF-8")
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
		int length = Math.min(memberId.length,perm.length);
		// 管理员强制某人加入某圈子
		if ((permService.isAdmin(thisMemberId) || permService.hasGroupPermission(thisMemberId, groupId, GroupPerm.MANAGE_GROUP))) {
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

//	/**
//	 * 获取加入柜子的用户
//	 * @param groupId 柜子id
//	 * @param start
//	 * @param limit
//	 * @return
//	 */
//	@RequiresUser
//	@ResponseBody
//	@RequestMapping(value = "/getMembersInGroup", produces = "application/json; charset=UTF-8")
//	public String getMembersInGroup(Long groupId, Integer start, Integer limit) {
//		if (groupId == null || groupId == 0) {
//			throw new GroupsException("柜子id为空");
//		}
//		start = start == null ? 0 : start;
//		limit = limit == null ? Integer.MAX_VALUE : limit;
//
//		List<Member> memberBeans = groupService.getMembersInGroup(groupId, start, limit);
//		if (memberBeans == null || memberBeans.size() <= 0) {
//			throw new GroupsException("柜子尚未加入任何用户或者用户组");
//		}
//		int totalCount = memberBeans.size();
//
//		String[] displayNames = new String[totalCount];
//		for (int i = 0; i < displayNames.length; ++i) {
//			if (memberBeans.get(i).getMemberType().equalsIgnoreCase(Member.MEMBER_TYPE_PERSON)) {
//				displayNames[i] = memberBeans.get(i).getName();
//			}
//			if (memberBeans.get(i).getMemberType().equalsIgnoreCase(Member.MEMBER_TYPE_FOLDER)) {
//				Member folder = grouperService.getFolder(memberBeans.get(i).getId());
//				displayNames[i] = folder.getSignature();
//			}
//			if (memberBeans.get(i).getMemberType().equalsIgnoreCase(Member.MEMBER_TYPE_TEAM)) {
//				Member team = grouperService.getTeam(memberBeans.get(i).getId());
//				displayNames[i] = team.getSignature().replace(":", "/");
//			}
//		}
//
//		List<GroupPerm[]> permList = new ArrayList<GroupPerm[]>();
//
//		for (Member bean : memberBeans) {
//			PermCollection pc = permService.getPermission(
//					groupId, bean.getId(), OldPerm.PERM_TYPE_GROUP);
//			permList.add(pc.getGroupPerm());
//		}
//		String[] idens = new String[totalCount];
//		for (int i = 0; i < totalCount; ++i) {
//			if (permService.isAdmin(memberBeans.get(i).getId())) {
//				idens[i] = "admin";
//			} else if (permService.isGroupManager(
//					memberBeans.get(i).getId(), groupId)) {
//				idens[i] = "manager";
//			} else {
//				idens[i] = "member";
//			}
//		}
//
//		StringBuffer buffer = new StringBuffer();
//		buffer.append("{");
//		buffer.append("\"totalCount\":").append(totalCount).append(",");
//		buffer.append("\"members\":[");
//		for (int i = 0; i < totalCount; ++i) {
//			buffer.append("{");
//			buffer.append("\"signatrue\":\"").append(JS.quote(Filter.convertHtmlBody(memberBeans.get(i).getSignature()))).append("\"").append(",");
//			if (memberBeans.get(i).getIcon() != null && memberBeans.get(i).getIcon().length() != 0) {
//				buffer.append("\"icon\":\"").append(PropertyUtil.getIconPrefix()).append(memberBeans.get(i).getIcon()).append("\"").append(",");
//			} else {
//				buffer.append("\"icon\":\"").append(PropertyUtil.getDefaultIcon()).append("\",");
//			}
//			buffer.append("\"id\":").append(memberBeans.get(i).getId()).append(",");
//			buffer.append("\"name\":\"").append(displayNames[i]).append("\",");
//			buffer.append("\"iden\":\"").append(idens[i]).append("\",");
//			IPermission.GroupPerm[] gps = permList.get(i);
//			buffer.append("\"upload\":").append(hasPerm(gps, IPermission.GroupPerm.UPLOAD_RESOURCE)).append(",");
//			buffer.append("\"delete\":").append(hasPerm(gps, IPermission.GroupPerm.DELETE_RESOURCE)).append(",");
//			buffer.append("\"addDir\":").append(hasPerm(gps, IPermission.GroupPerm.ADD_FOLDER)).append(",");
//			buffer.append("\"modify\":").append(hasPerm(gps, IPermission.GroupPerm.MODIFY_RESOURCE));
//			buffer.append("},");
//		}
//		if (totalCount > 0) {
//			buffer.setLength(buffer.length() - 1);
//		}
//		buffer.append("]");
//		buffer.append("}");
//		return buffer.toString();
//	}

	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getMembersInGroup", produces = "application/json; charset=UTF-8")
	public String getMembersInGroup(Long groupId, Integer start, Integer limit) {
		if (groupId == null || groupId == 0) {
			throw new GroupsException("柜子id为空");
		}
		start = start == null ? 0 : start;
		limit = limit == null ? Integer.MAX_VALUE : limit;

		List<Member> memberBeans = groupService.getMembersInGroup(groupId, start, limit);
		List<Member> members=groupService.getMembersInGroupTotal(groupId);
		int totalCount=0;
		if(members!=null)
			totalCount= members.size();
		if (memberBeans == null || memberBeans.size() <= 0) {
			StringBuffer stringBuffer=new StringBuffer();
			stringBuffer.append("{\"totalCount\":").append(totalCount).append(",");
			stringBuffer.append("\"members\":[ ]").append("}");
			return stringBuffer.toString();

		}
		//获取到的所有的用户或用户组
//		int totalCount = memberBeans.size();
		int sizebeans=memberBeans.size();

		String[] displayNames = new String[sizebeans];
		if(start>=totalCount || totalCount==0){
			StringBuffer stringBuffer=new StringBuffer();
			stringBuffer.append("{\"totalCount\":").append(totalCount).append(",");
			stringBuffer.append("\"members\":[ ]").append("}");
			return stringBuffer.toString();
		}
		if(start<0){
			StringBuffer stringBuffer=new StringBuffer();
			stringBuffer.append("{\"totalCount\":").append(totalCount).append(",");
			stringBuffer.append("\"message\":").append("\"parameter start is illegal\"").append("}");
			return stringBuffer.toString();
		}

			for (int i = 0; i < sizebeans; i++) {
				if (memberBeans.get(i).getMemberType().equalsIgnoreCase(Member.MEMBER_TYPE_PERSON)) {
					displayNames[i] = memberBeans.get(i).getName();
				}
				if (memberBeans.get(i).getMemberType().equalsIgnoreCase(Member.MEMBER_TYPE_FOLDER)) {
					Member folder = grouperService.getFolder(memberBeans.get(i).getId());
					displayNames[i] = folder.getSignature();
				}
				if (memberBeans.get(i).getMemberType().equalsIgnoreCase(Member.MEMBER_TYPE_TEAM)) {
					Member team = grouperService.getTeam(memberBeans.get(i).getId());
					displayNames[i] = team.getSignature().replace(":", "/");
				}
			}
		List<GroupPerm[]> permList = new ArrayList<GroupPerm[]>();

		for (Member bean : memberBeans) {
			PermCollection pc = permService.getPermission(
					groupId, bean.getId(), OldPerm.PERM_TYPE_GROUP);
			permList.add(pc.getGroupPerm());
		}
		//判断每位用户的身份
		String[] idens = new String[sizebeans];
		for (int i = 0; i < sizebeans; i++) {
			if (permService.isAdmin(memberBeans.get(i).getId())) {
				idens[i] = "admin";
			} else if (permService.isGroupManager(
					memberBeans.get(i).getId(), groupId)) {
				idens[i] = "manager";
			} else {
				idens[i] = "member";
			}
		}

		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		buffer.append("\"totalCount\":").append(totalCount).append(",");
		buffer.append("\"members\":[");
		for (int i = 0; i < sizebeans; i++) {
			buffer.append("{");
			buffer.append("\"signatrue\":\"").append(JS.quote(Filter.convertHtmlBody(memberBeans.get(i).getSignature()))).append("\"").append(",");
			if (memberBeans.get(i).getIcon() != null && memberBeans.get(i).getIcon().length() != 0) {
				buffer.append("\"icon\":\"").append(PropertyUtil.getIconPrefix()).append(memberBeans.get(i).getIcon()).append("\"").append(",");
			} else {
				buffer.append("\"icon\":\"").append(PropertyUtil.getDefaultIcon()).append("\",");
			}
			buffer.append("\"id\":").append(memberBeans.get(i).getId()).append(",");
			buffer.append("\"name\":\"").append(displayNames[i]).append("\",");
			buffer.append("\"iden\":\"").append(idens[i]).append("\",");
			IPermission.GroupPerm[] gps = permList.get(i);
			buffer.append("\"upload\":").append(hasPerm(gps, IPermission.GroupPerm.UPLOAD_RESOURCE)).append(",");
			buffer.append("\"delete\":").append(hasPerm(gps, IPermission.GroupPerm.DELETE_RESOURCE)).append(",");
			buffer.append("\"addDir\":").append(hasPerm(gps, IPermission.GroupPerm.ADD_FOLDER)).append(",");
			buffer.append("\"modify\":").append(hasPerm(gps, IPermission.GroupPerm.MODIFY_RESOURCE));
			buffer.append("},");
		}
		if (totalCount > 0) {
			buffer.setLength(buffer.length() - 1);
		}
		buffer.append("]");
		buffer.append("}");
		return buffer.toString();
	}



	/**
	 *
	 * 修改柜子权限
	 * @param groupId 柜子id
	 * @param memberId 用户或者team 的memberId数组
	 * @param perm 权限数组，与memberId对应
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/modifyMemberGroupPermission", produces = "application/json; charset=UTF-8")
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
		int length = Math.min(memberId.length, perms.size());
		for (int i = 0; i < length; ++i) {
			long _memberId = memberId[i];
			String p = perms.get(i);
			PermCollection pc = permService.getPermission(groupId,_memberId, OldPerm.PERM_TYPE_GROUP);
			// 清空四种权限
			pc.removeGroupPerm(new GroupPerm[] { GroupPerm.UPLOAD_RESOURCE,
					GroupPerm.DELETE_RESOURCE, GroupPerm.MODIFY_RESOURCE,GroupPerm.DOWNLOAD_RESOURCE,
					GroupPerm.ADD_FOLDER});

			// 解析需要设置的权限，并设置
			List<GroupPerm> list = new ArrayList<GroupPerm>();
			PermissionParseUtil.parseGroupPermission(list, p);
			list.add(GroupPerm.VIEW_GROUP);
			list.add(GroupPerm.VIEW_RESOURCE);
			list.add(GroupPerm.DOWNLOAD_RESOURCE);
			pc.addGroupPerm(list.toArray(new GroupPerm[list.size()]));

			// 重新设置权限
			permService.modifyPermission(groupId, _memberId,OldPerm.PERM_TYPE_GROUP, pc);
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 将用户从柜子中删除
	 * @param groupId 柜子id
	 * @param memberId 用户或者用户组id数组
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/deleteMemberFromGroup", produces = "application/json; charset=UTF-8")
	public String deleteMemberFromGroup(Long groupId, Long[] memberId) {
		if (groupId == null || groupId == 0) {
			throw new GroupsException("柜子id为空");
		}
		if (memberId == null || memberId.length <= 0) {
			throw new GroupsException("用户列表id为空");
		}
		for (long _memberId : memberId) {
			groupService.removeMemberGroupBinding(_memberId, groupId);
			OldPerm perm = permService.getPerm(_memberId, groupId,OldPerm.PERM_TYPE_GROUP);
			if (perm != null && perm.getId() > 0) {
				permService.deletePerm(perm.getId());
			}
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 根据账号获取用户头像
	 * @param account
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getProfilePic", produces = "application/json; charset=UTF-8")
	public String getProfilePic(String[] account)throws Exception{
		if(account==null||account.length==0){
			throw new GroupsException("用户account为空");
		}
		Map<String,String> urls = new HashMap<String, String>();
		for(String a:account){
			User userBase = userService.getUserByAccount(a);
			String url =  userBase.getPhoto();
			if(url==null||url.length()==0){
				urls.put(a, "");
			}
			else{
				urls.put(a, PropertyUtil.getMemberPicFolderPath()+url);
			}
		}

		StringBuffer sb = new StringBuffer();
		sb.append("{\"profilePics\":[");
		for(Map.Entry<String,String>url:urls.entrySet()){
			sb.append("{\"account\":\"").append(url.getKey());
			sb.append("\",\"picUrl\":\"").append(url.getValue());
			sb.append("\"},");
		}
		if(urls.size()>0){
			sb.setLength(sb.length()-1);
		}
		sb.append("]}");
		return sb.toString();
	}

	/**上传自己的头像
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/uploadProfilePic", produces = "application/json; charset=UTF-8")
	public String uploadProfilePic(HttpServletRequest request) throws Exception{
		//上传文件
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		MultipartFile multipartFile =  null;
		multipartFile = multipartRequest.getFile("filedata");
		String filedataFileName = multipartFile.getOriginalFilename();
		File file = null;
		try {
			file = File.createTempFile("tmp", null);
			multipartFile.transferTo(file);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(file==null){
			throw new GroupsException("头像文件不能为空");
		}
		String filename = filedataFileName;
		String ext = filename.substring(filename.lastIndexOf(".") + 1);
		String filepath =System.currentTimeMillis() + "." + ext;
		String path = PropertyUtil.getMemberPicRootPath();
		File folder =new File(path);

		if(!folder.exists()) {
			//如果目标文件所在的目录不存在，则创建父目录
			System.out.println("目标文件所在目录不存在，准备创建它！");
			if(!folder.mkdirs()) {
				System.out.println("创建目标文件所在目录失败！");
				throw new GroupsException("头像目录无法创建");
			}
		}
		//File destFile = FileUtils.getFile(path,filepath);
		File destFile = new File(folder,filepath);
		userService.uploadPersonalPic(file,destFile);
		User user = UserUtils.getUser();
		user.setPhoto(filepath);
		userService.saveOrUpdateUser(user);
		//删除multipartFile转换成File的临时文件
		if(file.exists()){
			file.delete();
		}
		String headPicUrl = PropertyUtil.getMemberPicFolderPath()+filepath;
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		sb.append("\"url\":\"").append(headPicUrl).append("\"");
		sb.append("}");
		return sb.toString();
	}

	/*********************************通讯录部分开始***************************************/
	/**
	 * 根据个人所在通讯录获取自己通讯录的用户列表
	 *
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = {"/getAccounts","/getAccounts_v2","/getContacts"}, produces = "application/json; charset=UTF-8")
	public String getContacts(Boolean withoutAdmin) throws Exception {
		withoutAdmin = withoutAdmin == null ? false : withoutAdmin;
		Long memberId = UserUtils.getCurrentMemberId();
		List<Contact> myContacts = contactService.getMyContact(memberId);

		List<Member> rootFolders = contactService.getMyContactTreeRootFolders(myContacts);
		List<User> beans = new ArrayList<User>();
		Map<String, List<Member>> map = new HashMap<String, List<Member>>();
		if (rootFolders != null && rootFolders.size() > 0) {
			for (Member folder : rootFolders) {
				Member[] members = grouperService.getMemberInFolder(folder.getId());
				if (members == null) {
					continue;
				}
				for (Member member : members) {
					String account = member.getAccount();
					if (map.containsKey(account)) {
						continue;
					}
					if (withoutAdmin && permService.isAdmin(
							member.getId())) {
						continue;
					}
					User user = userService.getUserByAccount(account);
					List<Member> temp = grouperService.getMembersByAccount(account);
					if (user != null && temp != null) {
						beans.add(user);
						map.put(account, temp);
					}
				}
			}
		}

		long totalCount = beans.size();
		StringBuffer sb = new StringBuffer();
		sb.append("{\"totalCount\":").append(totalCount).append(",\"accounts\":[");
		for (User bean : beans) {
			sb.append("{");
			sb.append("\"account\":\"").append(bean.getAccount()).append("\",");
			sb.append("\"name\":\"").append(bean.getName()).append("\",");
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
			if (bean.getUserbaseStatus() != null) {
				sb.append("\"status\":\"").append(bean.getUserbaseStatus()).append("\",");
			} else {
				sb.append("\"status\":\"").append("normal").append("\",");
			}

			List<Member> memberBeans = map.get(bean.getAccount());
			sb.append("\"members\":[");
			for (Member memberBean : memberBeans) {
				sb.append("{");
				sb.append("\"id\":").append(memberBean.getId()).append(",");
				sb.append("\"name\":\"").append(memberBean.getName()).append("\"");
				sb.append("},");
			}
			if (memberBeans.size() > 0) {
				sb.setLength(sb.length() - 1);
			}
			sb.append("]");
			sb.append("},");
		}
		if (totalCount > 0)
			sb.setLength(sb.length() - 1);
		sb.append("]}");
		return sb.toString();
	}

	/**
	 * 创建通讯录
	 * @param contactName 通讯录名字
	 * @param desc 描述
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/createContact", produces = "application/json; charset=UTF-8")
	public String createContact(String contactName, String desc) {
		if (contactName == null || "".equals(contactName))
			return "{\"type\":\"error\",\"code\":\"300\", \"detail\": \"通讯录名字不能为空！\"}";
		// 创建bean
		Contact bean = new Contact();
		bean.setContactName(contactName);
		bean.setCreateDate(new Timestamp(System.currentTimeMillis()));
		bean.setCreatorId(UserUtils.getCurrentMemberId());
		bean.setCreatorName(UserUtils.getAccount());
		bean.setDesc(desc);
		bean.setLastModified(new Date());
		contactService.saveOrModifyContact(bean);
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 获取我在的通讯组
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getMyContact", produces = "application/json; charset=UTF-8")
	public String getMyContact() throws Exception {
		long memberId = UserUtils.getCurrentMemberId();
		List<Contact> contacts = contactService.getMyContact(memberId);

		StringBuffer sb = new StringBuffer();
		long totalCount = 0L;
		if (contacts != null) {
			totalCount = contacts.size();
		}
		sb.append("{\"totalCount\":").append(totalCount).append(",\"contacts\":[");
		if (contacts != null) {
			for (Contact c : contacts) {
				sb.append("{");
				sb.append("\"id\":").append(c.getId()).append(",");
				sb.append("\"name\":\"").append(c.getContactName()).append("\",");
				sb.append("\"desc\":\"").append(JS.quote(HTML.escape(c.getDesc()))).append("\",");
				sb.append("\"createDate\":\"").append(c.getCreateDate()).append("\",");
				sb.append("\"creatorId\":").append(c.getCreatorId()).append(",");
				sb.append("\"creatorName\":\"").append(JS.quote(HTML.escape(c.getCreatorName()))).append("\"");
				sb.append("},");
			}
		}
		if (totalCount > 0)
			sb.setLength(sb.length() - 1);
		sb.append("]");
		sb.append("}");

		return sb.toString();
	}

	/**
	 * 根据id删除通讯录
	 * @param id contactId
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/deleteContact", produces = "application/json; charset=UTF-8")
	public String deleteContact(Long id) throws Exception {
		// 找到contact
		if (id == null || id <= 0) {
			return "{\"type\":\"error\",\"code\":\"300\", \"detail\": \"通讯录Id不能为空或者负数！\"}";
		}
		Contact contact = contactService.getContactById(id);
		if (contact == null) {
			return "{\"type\":\"error\",\"code\":\"300\", \"detail\": \"找不到对应的通讯录！\"}";
		}
		// 级联删除contactSubject
		contactService.removeContactSubjectByContactId(id);
		// 删除contact
		contactService.deleteContactById(id);
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 根据id
	 * @param id
	 * @param contactName
	 * @param desc
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/modifyContact", produces = "application/json; charset=UTF-8")
	public String modifyContact(Long id, String contactName, String desc) throws Exception {
		if (id == null || id <= 0) {
			return "{\"type\":\"error\",\"code\":\"300\", \"detail\": \"通讯录Id不能为空或者负数！\"}";
		}
		Contact contact = (Contact) contactService.getContactById(id);
		if (contact == null) {
			return "{\"type\":\"error\",\"code\":\"300\", \"detail\": \"找不到对应的通讯录！\"}";
		}
		contact.setContactName(contactName);
		contact.setDesc(desc);
		contact.setLastModified(new Timestamp(System.currentTimeMillis()));
		contactService.saveOrModifyContact(contact);
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 批量添加folder到通讯录
	 * @param ids 要添加的folder的memberId
	 * @param id 通讯录id
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/addFoldersToContact", produces = "application/json; charset=UTF-8")
	public String addFoldersToContact(Long[] ids, Long id) throws Exception {
		if (id == null || id ==0) {
			return "{\"type\":\"error\", \"detail\": \"通讯录id不能为空！\"}";
		}
		if (ids != null && ids.length >0) {
			for (long fid : ids) {
				contactService.addFoldersToContact(fid, id);
			}
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 根据id批量删除通讯录中的folder
	 * @param ids 要删除的folder的memberid数组
	 * @param id 通讯录id
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/removeFoldersFromContact", produces = "application/json; charset=UTF-8")
	public String removeFoldersFromContact(Long[] ids, Long id) throws Exception {
		if (id == null || id ==0) {
			return "{\"type\":\"error\", \"detail\": \"通讯录id不能为空！\"}";
		}
		if (ids != null && ids.length >0) {
			for (long fid : ids) {
				contactService.removeContactSubjectByContactIdAndSubjectId(fid, id);
			}
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 获取全部通讯录
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getAllContact", produces = "application/json; charset=UTF-8")
	public String getAllContact(Integer start, Integer limit) throws Exception {
		start = start == null ? 0 : start;
		limit = limit == null ? Integer.MAX_VALUE : limit;
		List<Contact> contacts = contactService.getAllContact(start, limit);
		StringBuffer sb = new StringBuffer();
		long totalCount = contactService.getAllContactTotalCount();
		sb.append("{\"totalCount\":").append(totalCount).append(",\"contacts\":[");
		if (contacts != null) {
			for (Contact c : contacts) {
				sb.append("{");
				sb.append("\"id\":").append(c.getId()).append(",");
				sb.append("\"name\":\"").append(c.getContactName()).append("\",");
				sb.append("\"desc\":\"").append(JS.quote(HTML.escape(c.getDesc()))).append("\",");
				sb.append("\"createDate\":\"").append(c.getCreateDate()).append("\",");
				sb.append("\"creatorId\":").append(c.getCreatorId()).append(",");
				sb.append("\"creatorName\":\"").append(JS.quote(HTML.escape(c.getCreatorName()))).append("\"");
				sb.append("},");
			}
		}
		if (totalCount > 0)
			sb.setLength(sb.length() - 1);
		sb.append("]");
		sb.append("}");

		return sb.toString();

	}

	/**
	 * lastmodified更新
	 * @param timestamp
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/validateContactList", produces = "application/json; charset=UTF-8")
	public String validateContactList(String timestamp) throws Exception{
		boolean modified = false;
		long time=0L;
		if(timestamp==null){
			modified = true;
		}
		try{
			time = Long.valueOf(timestamp);
		}catch(NumberFormatException e){
			time = 0L;
		}
		List<Contact> myContacts = contactService.getMyContact(UserUtils.getCurrentMemberId());
		List<Member> rootFolders = contactService.getMyContactTreeRootFolders(myContacts);
		Date compareDate = new Date(time);
		if(rootFolders!=null)
			for (Member folder : rootFolders) {
				Date date = ((Member)folder).getLastModified();
				if(date==null){
					//如果原本lastModified为空，则认为是最新的，并向上更新
					date = new Date();
					grouperService.shiftUpLastModified(folder.getId(), date);
				}
				if(date.compareTo(compareDate)>0){
					//该folder 的lastModified 大于参数值（更新过）
					modified = true;
					compareDate = date;//compareDate用于记录做大的lastmodified
				}

			}

		StringBuffer sb = new StringBuffer();

		sb.append("{");
		sb.append("\"isValid\":\"").append(modified).append("\"");
		//sb.append("\"latestModifiedDate\":").append(latestModifiedDate.getTime());
		sb.append("}");
		return sb.toString();
	}

	/**
	 * 根据id获取我的通讯录树
	 * 有点疑惑
	 * @param id memberId
	 * @param folderOnly
	 * @param withoutLeaf
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getMyContactTree", produces = "application/json; charset=UTF-8")
	public String getMyContactTree(Long id, Boolean folderOnly, Boolean withoutLeaf) throws Exception {
		if (id == null) {
			id =0L;
		}
		if (folderOnly == null) {
			folderOnly = false;
		}
		if(withoutLeaf == null) {
			withoutLeaf = true;
		}
		Member[] memberBeans = null;
		Member memberBean = null;
		// 判断是否有子节点
		boolean[] isLeafs = null;
		// 判断是否是系统生成的组织和组
		boolean[] isSystem = null;

		String type = Member.MEMBER_TYPE_FOLDER;
		String name = null;
		// 先判断是否根节点
		if (id != 0) {
			Member iMemberBean = grouperService.getMemberById(id);
			type = iMemberBean.getMemberType();
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
				if (memberBeans[i].getAccount() != null) {
					userList.add(userService.getUserByAccount(memberBeans[i].getAccount()));
					memberList.add(memberBeans[i]);
				} else {
					System.out.println("Get Account Exception, User donot Exist! : "
							+ memberBeans[i].getName());
					exceptionList.add(memberBeans[i].getName());
				}
			}
			User[] userBaseBeans = userList.toArray(new User[userList.size()]);
			memberBeans = memberList.toArray(new Member[memberList.size()]);
			return ReturnWrapper.getUsersWrapper(userBaseBeans, memberBeans, exceptionList);
		} else if (Member.MEMBER_TYPE_FOLDER.equals(type)) {
			// 判断是否是只显示folder
			if (folderOnly) {
				memberBeans = grouperService.getFolderByParent(id, withoutLeaf);
				isLeafs = new boolean[memberBeans.length];

				isSystem = new boolean[memberBeans.length];
				for (int i = 0; i < memberBeans.length; i++) {
					Member m = memberBeans[i];
					boolean ifLeaf = grouperService.hasSubFolder(m.getId());
					isLeafs[i] = !ifLeaf;
					try {
						grouperService.checkDefaultFolder(m.getId());
					} catch (GroupsException e) {
						isSystem[i] = true;
					}
				}
			} else {
				if (id == 0) {
					List<Contact> myContacts = contactService.getMyContact(UserUtils.getCurrentMemberId());
					List<Member> memberBeansList = contactService.getMyContactTreeRootFolders(myContacts);
					if (memberBeansList == null || memberBeansList.size() == 0) {
						long temp = 0L;
						memberBeans = null;
					} else {
						memberBeans = memberBeansList.toArray(new Member[memberBeansList.size()]);
					}
				} else {
					memberBeans = grouperService.getFolderAndTeamByParent_v2(id,
							withoutLeaf);
				}

				if (memberBeans != null) {
					isLeafs = new boolean[memberBeans.length];
					isSystem = new boolean[memberBeans.length];
					for (int i = 0; i < memberBeans.length; i++) {

						Member m = memberBeans[i];
						if (Member.MEMBER_TYPE_TEAM.equals(m.getMemberType())) {
							try {
								grouperService.checkDefaultTeam(m.getSignature());
							} catch (GroupsException e) {
								isSystem[i] = true;
							}
						}

					}
				}
			}
		}

		return ReturnWrapper.getMembersTreeWrapper(memberBeans, isLeafs, isSystem);

	}

	/**
	 * 根据通讯录id获取通讯录树
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getContactTree", produces = "application/json; charset=UTF-8")
	public String getContactTree(Long id) throws Exception {
		if (id == null || id ==0) {
			return "{\"type\":\"error\", \"detail\": \"通讯录id不能为空！\"}";
		}
		Contact contact = contactService.getContactById(id);
		if (contact == null) {
			return "{\"type\":\"error\", \"detail\": \"通讯录不存在！\"}";
		}
		List<Contact> contacts = new ArrayList<Contact>();
		contacts.add(contact);
		List<Member> rootFolders = contactService.getMyContactTreeRootFolders(contacts);
		List<Member[]> childrenList = new ArrayList<Member[]>();
		for (Member folder : rootFolders) {
			Member[] children = contactService
					.getChildrenByParentFolder(folder.getId());
			childrenList.add(children);
		}

		StringBuffer sb = new StringBuffer();
		sb.append("{\"contactRoot\":[");
		if (rootFolders != null) {
			int index = 0;
			for (Member root : rootFolders) {
				sb.append("{");
				sb.append("\"id\":").append(root.getId()).append(",");
				String name = root.getSignature();
				if(name==null){
					name="null";
				}
				String[] spiltDisplayName = name.split(":");
				String shortName = spiltDisplayName[spiltDisplayName.length - 1];
				sb.append("\"name\":\"").append(shortName).append("\",");
				sb.append("\"type\":\"").append(root.getMemberType()).append("\",");
				StringBuffer folder = new StringBuffer();
				StringBuffer team = new StringBuffer();
				for (Member bean : childrenList.get(index++)) {
					if ("folder".equals(bean.getMemberType())) {

						name = bean.getSignature();
						if(name==null){
							name="null";
						}
						spiltDisplayName = name.split(":");
						shortName = spiltDisplayName[spiltDisplayName.length - 1];
						folder.append("{");
						folder.append("\"id\":").append(bean.getId()).append(",");
						folder.append("\"name\":\"").append(JS.quote(HTML.escape(shortName))).append("\",");
						folder.append("\"type\":\"").append(bean.getMemberType()).append("\"");
						folder.append("},");
					} else {
						name = bean.getSignature();
						spiltDisplayName = name.split(":");
						shortName = spiltDisplayName[spiltDisplayName.length - 1];
						team.append("{");
						team.append("\"id\":").append(bean.getId()).append(",");
						team.append("\"name\":\"").append(JS.quote(HTML.escape(shortName))).append("\",");
						team.append("\"type\":\"").append(bean.getMemberType()).append("\"");
						team.append("},");
					}
				}
				if (folder.length() > 0)
					folder.setLength(folder.length() - 1);
				if (team.length() > 0)
					team.setLength(team.length() - 1);

				sb.append("\"folder\":[").append(folder).append("],");
				sb.append("\"team\":[").append(team).append("]");
				sb.append("},");
			}
			if (rootFolders.size() > 0)
				sb.setLength(sb.length() - 1);
		}
		sb.append("]");
		sb.append("}");
		return sb.toString();
	}

	/*********************************通讯录部分结束***************************************/

	/***********************************角色权限部分开始************************************/
	/**
	 * 给用户赋角色
	 * 角色id为0则设为根域管理员
	 * @param id 角色id
	 * @param memberIds 待赋角色的用户id数组
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/grantRoleToMember", produces = "application/json; charset=UTF-8")
	public String grantRoleToMember(Long id, Long[] memberIds) throws Exception {
		if (memberIds == null || memberIds.length == 0 || id == null) {
			return "{\"type\":\"error\", \"detail\": \"参数不能为空！\"}";
		}
		if(id==0) {
			Domain root = domainService.getRootDomain();
			if(root==null){
				return "{\"type\":\"error\", \"detail\": \"根域创建失败！\"}";
			}
			domainService.addRootDomainManager(root.getId(), memberIds);
			return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
		}
		roleService.grantRoleToMember(id, memberIds);
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 根据id获取用户的角色
	 * 不传或者为0则默认查询当前登陆者
	 * @param memberId 用户id
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getRolesOfMember", produces = "application/json; charset=UTF-8")
	public String getRolesOfMember(Long memberId) throws Exception {
		if (memberId == null || memberId== 0) {
			memberId = UserUtils.getCurrentMemberId();
		}
		List<DomainRole> roles = roleService.getRolesOfMember(memberId);
		StringBuffer sb = new StringBuffer();
		sb.append("{\"roles\":[");
		if (roles != null) {
			for(int i=0;i<roles.size();i++){
				DomainRole role = roles.get(i);
				sb.append("{");
				sb.append("\"id\":").append(role.getId()).append(",");
				sb.append("\"name\":\"").append(role.getName()).append("\",");
				sb.append("\"desc\":\"").append(role.getDesc()).append("\"");
				sb.append("},");
			}
			if(roles.size()>0){
				sb.setLength(sb.length()-1);
			}
		}
		sb.append("]}");
		return sb.toString();
	}

	/**
	 * 回收用户角色
	 * 角色id为0则回收根域管理员
	 * @param id 角色id
	 * @param memberIds 待回收角色的用户id数组
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/revokeRoleFromMember", produces = "application/json; charset=UTF-8")
	public String revokeRoleFromMember(Long id, Long[] memberIds) throws Exception {

		if (memberIds == null || memberIds.length == 0 || id == null) {
			return "{\"type\":\"error\", \"detail\": \"参数不能为空！\"}";
		}
		if(id==0) {
			Domain root = domainService.getRootDomain();
			if(root==null){
				return "{\"type\":\"error\", \"detail\": \"根域创建失败！\"}";
			}
			domainService.revokeRootDomainManager(root.getId(), memberIds);
			return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
		}
		roleService.revokeRoleFromMember(id, memberIds);
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 获取所有角色列表
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getAllRoles", produces = "application/json; charset=UTF-8")
	public String getAllRoles() throws Exception {
		List<DomainRole> roles = roleService.getAllRoles();
		// 添加一个虚拟的总域管理员角色，id为0，名字叫rootDomainAdmin
		DomainRole rootDomainAdminRole = new DomainRole();
		rootDomainAdminRole.setId(0L);
		rootDomainAdminRole.setName("rootDomainAdmin");
		rootDomainAdminRole.setDesc("总域管理员");
		roles.add(rootDomainAdminRole);

		StringBuffer sb = new StringBuffer();
		sb.append("{\"roles\":[");
		if (roles != null) {
			for(int i=0;i<roles.size();i++){
				DomainRole role = roles.get(i);
				sb.append("{");
				sb.append("\"id\":").append(role.getId()).append(",");
				sb.append("\"name\":\"").append(role.getName()).append("\",");
				sb.append("\"desc\":\"").append(role.getDesc()).append("\"");
				sb.append("},");
			}


			if(roles.size()>0){
				sb.setLength(sb.length()-1);
			}
		}
		sb.append("]}");
		return sb.toString();
	}

	/**
	 * 根据角色id获取拥有该角色的用户
	 * 为0表示查总域管理员
	 * @param id 待查询的角色id
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getMembersByRoleId", produces = "application/json; charset=UTF-8")
	public String getMembersByRoleId(Long id) throws Exception {
		if (id == null) {
			return "{\"type\":\"error\", \"detail\": \"参数不能为空！\"}";
		}
		List<Member> members = new ArrayList<Member>();
		if (id == 0) {
			// 查询的是总域管理员
			members = domainService.getRootDomainManagers();
		} else {
			members = roleService.getMembersByRoleId(id);
		}
		List<User> userbases = new ArrayList<User>();
		for (Member m : members) {
			try {
				userbases.add(userService.getUserByAccount(m.getAccount()));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ReturnWrapper.getUsersWrapper(userbases.toArray(new User[0]), members.toArray(new Member[0]), null);
	}

	/**
	 * 根据角色id查询拥有的权限
	 * 默认返回所有权限并且标记此角色拥有的权限
	 * @param id 角色id
	 * @param thisRoleOnly 是否只返回此角色拥有的权限
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getPermissionsByRoleId", produces = "application/json; charset=UTF-8")
	public String getPermissionsByRoleId(Long id, Boolean thisRoleOnly) throws Exception {
		if (id == null) {
			return "{\"type\":\"error\", \"detail\": \"参数不能为空！\"}";
		}
		if (thisRoleOnly == null) {
			thisRoleOnly = false;
		}
		List<DomainPermission> permissions = roleService.getPermissionsByRoleId(id);

		Set<DomainPermission> set = new HashSet<DomainPermission>();
		set.addAll(permissions);
		if (!thisRoleOnly) {
			List<DomainPermission> allPermissions = roleService.getAllPermissions();
			boolean[] checkArr = new boolean[allPermissions.size()];
			for(int i=0;i<allPermissions.size();i++) {
				DomainPermission p = allPermissions.get(i);
				if(set.contains(p)) {
					checkArr[i]  =true;
				}
			}
			return ReturnWrapper.getPermissionsByRoleIdWrapper(allPermissions, checkArr);
		}else {
			boolean[] checkArr = new boolean[permissions.size()];
			Arrays.fill(checkArr, true);
			return ReturnWrapper.getPermissionsByRoleIdWrapper(permissions, checkArr);
		}
	}

	/**
	 * 返回用户的权限
	 * @param memberId
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getPermissionOfMember", produces = "application/json; charset=UTF-8")
	public String getPermissionOfMember(Long[] memberId) throws Exception{
		if(memberId==null||memberId.length==0){
			throw new GroupsException("参数错误");
		}

		Member member = grouperService.getMemberById(memberId[0]);//team or person
		if (member == null) {
			throw new Exception("memberId对应的用户不存在");
		}
		List<OldPerm> globalPerms = permService.getMemberPerms(memberId[0], OldPerm.PERM_TYPE_GLOBAL);
		List<OldPerm> groupPerms = permService.getMemberPerms(memberId[0], OldPerm.PERM_TYPE_GROUP);

		List<OldPerm> categoryPerms = permService.getMemberPerms(memberId[0], OldPerm.PERM_TYPE_CATEGORY);
		List<OldPerm> groupOfCategoryPerms = permService.getMemberPerms(memberId[0], OldPerm.PERM_TYPE_GROUP_OF_CATEGORY);//分类权限中属于柜子部分


		//对柜子的权限
		Map<Group,PermCollection> groupPermMap = new HashMap<Group, PermCollection>();
		PermCollection pc = null;
		for(OldPerm gp:groupPerms){
			long groupId = gp.getTypeId();//柜子id
			try {
				Group group = groupService.getGroupById(groupId);
				if(groupPermMap.containsKey(group)){
					pc = groupPermMap.get(group);

				}else{
					pc = new PermCollection();
					groupPermMap.put(group, pc);
				}
			} catch (Exception e) {
				continue;
			}
			GroupPerm[] perms = PermUtil.convertGroupPerm(gp.getPermCode());
			pc.addGroupPerm(perms);
		}
		//member对分类的权限
		Map<Category,PermCollection> categoryPermMap = new HashMap<Category,PermCollection>();
//		pc = null;
//		for(OldPerm cp:categoryPerms){
//			long cid = cp.getTypeId();//分类id
//			try {
//				Category category = categoryService.getCategoryById(cid);
//				if(categoryPermMap.containsKey(category)){
//					pc = categoryPermMap.get(category);
//
//				}else{
//					pc = new PermCollection();
//					categoryPermMap.put(category, pc);
//				}
//			} catch (Exception e) {
//				continue;
//			}
//			CategoryPerm[] perms = PermUtil.convertCategoryPerm(cp.getPermCode());
//			pc.addCategoryPerm(perms);
//		}
		pc = null;
		for(OldPerm gocp:groupOfCategoryPerms){
			long cid = gocp.getTypeId();//分类id
			try {
				Category category = categoryService.getCategoryById(cid);
				if(categoryPermMap.containsKey(category)){
					pc = categoryPermMap.get(category);

				}else{
					pc = new PermCollection();
					categoryPermMap.put(category, pc);
				}
			} catch (Exception e) {
				continue;
			}
			GroupPerm[] perms = PermUtil.convertGroupPerm(gocp.getPermCode());
			pc.addGroupPerm(perms);
		}
		//查找用户是否是管理员

		boolean isAdmin = false;
		List<DomainRole> roles = new ArrayList<DomainRole>();
		if (member.getMemberType().equalsIgnoreCase(Member.MEMBER_TYPE_PERSON)) {
			isAdmin = permService.isAdmin(memberId[0]);
			roles = roleService.getRolesOfMember(memberId[0]);
		}

		//返回数据
		StringBuffer buffer = new StringBuffer();
		buffer.append("{");

		buffer.append("\"groups\":[");
		int countGroups = 0;
		for (Map.Entry<Group, PermCollection> en : groupPermMap.entrySet()) {
			Group group = en.getKey();
			if(group==null)
				continue;
			countGroups++;
			PermCollection pc1 = en.getValue();
			buffer.append("{");
			buffer.append("\"groupId\":").append(group.getId()).append(",");

			buffer.append("\"categoryId\":").append(group.getCategory().getId()).append(",");
			buffer.append("\"displayName\":\"").append(group.getDisplayName()).append("\",");

			buffer.append("\"name\":\"").append(group.getName()).append("\",");
			buffer.append("\"upload\":")
					.append(PermUtil.containPermission(pc1.getGroupPerm(), IPermission.GroupPerm.UPLOAD_RESOURCE))
					.append(",");

			buffer.append("\"delete\":")
					.append(PermUtil.containPermission(pc1.getGroupPerm(), IPermission.GroupPerm.DELETE_RESOURCE))
					.append(",");

			buffer.append("\"addDir\":")
					.append(PermUtil.containPermission(pc1.getGroupPerm(), IPermission.GroupPerm.ADD_FOLDER))
					.append(",");

			buffer.append("\"modify\":")
					.append(PermUtil.containPermission(pc1.getGroupPerm(), IPermission.GroupPerm.MODIFY_RESOURCE));

			buffer.append("},");
		}
		if (countGroups > 0) {
			buffer.setLength(buffer.length() - 1);
		}
		buffer.append("],");
		buffer.append("\"categorys\":[");
		int countCategory=0;
		for (Map.Entry<Category, PermCollection> en : categoryPermMap.entrySet()) {
			Category category = en.getKey();
			if(category==null)
				continue;
			countCategory++;
			PermCollection pcc = en.getValue();
			buffer.append("{");
			buffer.append("\"id\":").append(category.getId()).append(",");

			buffer.append("\"parentId\":").append(category.getParentId()).append(",");
			buffer.append("\"name\":\"").append(category.getName()).append("\",");
			buffer.append("\"displayName\":\"").append(category.getDisplayName()).append("\",");

			buffer.append("\"addcategory\":").append(PermUtil.containCategoryPermission(pcc.getCategoryPerm(),
					IPermission.CategoryPerm.CREATE_CATEGORY)).append(",");
			buffer.append("\"managecategory\":").append(PermUtil.containCategoryPermission(pcc.getCategoryPerm(),
					IPermission.CategoryPerm.MANAGE_CATEGORY)).append(",");
			buffer.append("\"addgroup\":").append(
					PermUtil.containCategoryPermission(pcc.getCategoryPerm(), IPermission.CategoryPerm.CREATE_GROUP))
					.append(",");

			buffer.append("\"managegroup\":")
					.append(PermUtil.containPermission(pcc.getGroupPerm(), IPermission.GroupPerm.MANAGE_GROUP))
					.append(",");

			buffer.append("\"upload\":")
					.append(PermUtil.containPermission(pcc.getGroupPerm(), IPermission.GroupPerm.UPLOAD_RESOURCE))
					.append(",");
			buffer.append("\"delete\":")
					.append(PermUtil.containPermission(pcc.getGroupPerm(), IPermission.GroupPerm.DELETE_RESOURCE))
					.append(",");

			buffer.append("\"addDir\":")
					.append(PermUtil.containPermission(pcc.getGroupPerm(), IPermission.GroupPerm.ADD_FOLDER))
					.append(",");

			buffer.append("\"modify\":")
					.append(PermUtil.containPermission(pcc.getGroupPerm(), IPermission.GroupPerm.MODIFY_RESOURCE));

			buffer.append("},");
		}
		if (countCategory > 0) {
			buffer.setLength(buffer.length() - 1);
		}
		buffer.append("],");
		buffer.append("\"isAdmin\":").append(isAdmin).append(",");

		buffer.append("\"roles\":[");
		for(int i=0;i<roles.size();i++){
			DomainRole role = roles.get(i);
			buffer.append("{");
			buffer.append("\"id\":").append(role.getId()).append(",");
			buffer.append("\"name\":\"").append(role.getName()).append("\",");
			buffer.append("\"desc\":\"").append(role.getDesc()).append("\"");
			buffer.append("},");
		}

		if(roles.size()>0){
			buffer.setLength(buffer.length()-1);
		}
		buffer.append("]}");
		return buffer.toString();

	}


	/***********************************角色权限部分结束************************************/

	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getMembersByTreeNode", produces = "application/json; charset=UTF-8")
	public String getMembersByTreeNode(Long[] id) throws Exception{
		if(id==null||id.length<1){
			throw new GroupsException("参数错误");
		}
		long nodeId = id[0];
		Member curNode = grouperService.getMemberById(nodeId);
		if(curNode==null){
			throw new GroupsException("找不到对象");
		}
		ArrayList<Member> memberList = new ArrayList<Member>();
		if(Member.MEMBER_TYPE_FOLDER.equals(curNode.getMemberType())){
			Member[] members = grouperService.getMemberInFolder(nodeId);
			memberList.addAll(Arrays.asList(members));
		}else if(Member.MEMBER_TYPE_TEAM.equals(curNode.getMemberType())){
			Member[] members = grouperService.getMembersInTeam(nodeId);
			memberList.addAll(Arrays.asList(members));
		}else{
			memberList.add(curNode);
		}
		StringBuffer sb = new StringBuffer();
		sb.append("{\"members\":[");
		if (memberList !=null && memberList.size()>0) {
			for (Member bean : memberList) {
				sb.append("{");
				sb.append("\"id\":").append(bean.getId()).append(",");
				sb.append("\"name\":\"").append(
						JS.quote(HTML.escape(bean.getName()))).append("\",");
				sb.append("\"type\":\"").append(bean.getMemberType()).append("\",");
				sb.append("\"account\":\"").append(bean.getAccount()).append("\"");
				sb.append("},");
			}
			if (memberList.size() > 0)
				sb.setLength(sb.length() - 1);
		}

		sb.append("]}");
		return sb.toString();
	}

	@ResponseBody
	@RequestMapping(value = "/getLastUploadLog", produces = "application/json; charset=UTF-8")
	public String getLastUploadLog(String account, String password, Integer limit) throws Exception{
		if(account==null||password==null){
			throw new GroupsException("账号或者密码为空");
		}
		Boolean flag = userService.checkPassword(account, password);
		if (!flag) {
			throw new GroupsException("账号不存在或者账号密码错误");
		}
		limit = limit == null ? 1 : limit.intValue();
		List<UploadLog> uploadLogs = logService.getLatestUploadLogByAccount(0, limit, account);
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		sb.append("\"count\":").append(limit).append(",");
		sb.append("\"uploadLogs\":[");
		for(int i=0;i<uploadLogs.size();i++){
			sb.append("{");
			sb.append("\"account\":\"").append(uploadLogs.get(i).getAccount()).append("\",");
			sb.append("\"memberName\":\"").append(uploadLogs.get(i).getMemberName()).append("\",");
			sb.append("\"memberId\":").append(uploadLogs.get(i).getMemberId()).append(",");
			sb.append("\"ip\":\"").append(uploadLogs.get(i).getIp()).append("\",");
			sb.append("\"createDate\":\"").append(DateFormat.format(uploadLogs.get(i).getCreateDate())).append("\",");
			sb.append("\"targetObject\":\"").append(JS.quote(HTML.escape(uploadLogs.get(i).getTargetObject()))).append("\",");
			sb.append("\"targetObjectId\":").append(uploadLogs.get(i).getTargetObjectId()).append(",");
			sb.append("\"groupName\":\"").append(uploadLogs.get(i).getGroupName()).append("\",");
			sb.append("\"groupId\":").append(uploadLogs.get(i).getGroupId()).append(",");
			sb.append("\"terminal\":\"").append(JS.quote(Filter.convertHtmlBody(uploadLogs.get(i).getTerminal()))).append("\"}");
			sb.append(",");
		}
		if(uploadLogs.size() > 0){
			sb.setLength(sb.length() - 1);
		}
		sb.append("]");
		sb.append("}");
		return sb.toString();
	}

	@ResponseBody
	@RequestMapping(value = "/getLastDownloadLog", produces = "application/json; charset=UTF-8")
	public String getLastDownloadLog(String account, String password, Integer limit) throws Exception{
		if(account==null||password==null){
			throw new GroupsException("账号或者密码为空");
		}
		Boolean flag = userService.checkPassword(account, password);
		if (!flag) {
			throw new GroupsException("账号不存在或者账号密码错误");
		}
		limit = limit == null ? 1 : limit.intValue();
		List<DownloadLog> downloadLogs = logService.getLatestDownloadLogByAccount(0, limit, account);
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		sb.append("\"count\":").append(limit).append(",");
		sb.append("\"downloadLogs\":[");
		for(int i=0;i<downloadLogs.size();i++){
			sb.append("{");
			sb.append("\"account\":\"").append(downloadLogs.get(i).getAccount()).append("\",");
			sb.append("\"memberName\":\"").append(downloadLogs.get(i).getMemberName()).append("\",");
			sb.append("\"memberId\":").append(downloadLogs.get(i).getMemberId()).append(",");
			sb.append("\"ip\":\"").append(downloadLogs.get(i).getIp()).append("\",");
			sb.append("\"createDate\":\"").append(DateFormat.format(downloadLogs.get(i).getCreateDate())).append("\",");
			sb.append("\"targetObject\":\"").append(JS.quote(Filter.convertHtmlBody(downloadLogs.get(i).getTargetObject()))).append("\",");
			sb.append("\"targetObjectId\":").append(downloadLogs.get(i).getTargetObjectId()).append(",");
			sb.append("\"groupName\":\"").append(downloadLogs.get(i).getGroupName()).append("\",");
			sb.append("\"groupId\":").append(downloadLogs.get(i).getGroupId()).append(",");
			sb.append("\"terminal\":\"").append(JS.quote(Filter.convertHtmlBody(downloadLogs.get(i).getTerminal()))).append("\"}");
			sb.append(",");
		}
		if(downloadLogs.size() > 0){
			sb.setLength(sb.length() - 1);
		}
		sb.append("]");
		sb.append("}");
		return sb.toString();
	}

	@ResponseBody
	@RequestMapping(value = "/getDisplayInfo", produces = "application/json; charset=UTF-8")
	public String getDisplayInfo() throws Exception{
		//查询接受记录，查最近一条
		Long memberId = UserUtils.getCurrentMemberId();
		List<Object[]> result = resourceService.getMyReceive(memberId, 0, 1);
		ArrayList<Object[]> shareRecords = new ArrayList<Object[]>();
		HashSet<Long> uniqueWrap  = new HashSet<Long>();
		HashMap<Long ,ArrayList<GroupResourceReceive>> shareReceiveMap = new HashMap<Long, ArrayList<GroupResourceReceive>>();
		for(int i=0;i<result.size();i++){
			Object[] curResult = result.get(i);
			GroupResourceReceive r =(GroupResourceReceive) curResult[0];
			GroupResourceShare s = (GroupResourceShare)curResult[1];
			if(s.getShareWrap()!=null){
				//存在wrap的shareBean
				Object[] thisRecord = new Object[6];
				ShareWrap shareWrap = s.getShareWrap();//(ShareWrap) curResult[5];
				if(!uniqueWrap.contains(shareWrap.getId())){//过滤重复warp
					uniqueWrap.add(shareWrap.getId());
					long providerId =  shareWrap.getProviderId();
					Member provider = grouperService.getMemberById(providerId);
					thisRecord[0] = provider;
					String remark = shareWrap.getMessage();
					thisRecord[1] = remark;
					Timestamp shareDate = shareWrap.getCreateDate();
					thisRecord[2] = shareDate;
					Long shareId = shareWrap.getId();
					thisRecord[3] = shareId;
					Integer shareType = s.getShareType();//(Integer) curResult[6]==null?1:(Integer) curResult[6];
					thisRecord[4] = shareType;
					List<ShareResponse> response = resourceService.getShareResponseByShareWrapAndResponder(shareId, memberId);
					ShareResponse uniqueResponse = null;
					if(!response.isEmpty()){
						uniqueResponse = response.get(0);
					}
					thisRecord[5] = uniqueResponse;
					shareRecords.add(thisRecord);
				}
				if(!shareReceiveMap.containsKey(shareWrap.getId())){
					shareReceiveMap.put(shareWrap.getId(), new ArrayList<GroupResourceReceive>());
				}
				ArrayList<GroupResourceReceive> resourceList = shareReceiveMap.get(shareWrap.getId());
				resourceList.add(r);
			}else{
				//对于不存在warp的旧分享数据
				Object[] thisRecord = new Object[6];
				Long shareId =s.getId();//(Long) curResult[0];
				if(!uniqueWrap.contains(shareId)){//过滤重复warp
					uniqueWrap.add(shareId);
					Member provider =  s.getProvider();//(Member) curResult[3];
					thisRecord[0] = provider;
					String remark = s.getRemark();//(String) curResult[2];
					thisRecord[1] = remark;
					Timestamp shareDate = (Timestamp) s.getCreateDate();//(Timestamp) curResult[1];
					thisRecord[2] = shareDate;

					thisRecord[3] = shareId;
					Integer shareType = s.getShareType();//(Integer) curResult[6]==null?1:(Integer) curResult[6];
					thisRecord[4] = shareType;
					Long shareWrapId = shareId, responderId = 0L;//0表示本人
					ShareResponse uniqueResponse = null;
					thisRecord[5] = uniqueResponse;
					shareRecords.add(thisRecord);
				}
				if(!shareReceiveMap.containsKey(shareId)){
					shareReceiveMap.put(shareId, new ArrayList<GroupResourceReceive>());
				}
				ArrayList<GroupResourceReceive> resourceList = shareReceiveMap.get(shareId);
				resourceList.add(r);
			}

		}

		//查日志，取最近一条
		String memberName = UserUtils.getCommonName();
		String account = UserUtils.getAccount();
		SearchTerm searchTerm = new AndSearchTerm();
		if(account != null && !account.trim().equals("")){
			searchTerm.add(new SearchItem<String>(LogSearchItemKey.Account,SearchItem.Comparison.LK,account));
		}
		if(memberName != null && !memberName.trim().equals("")){
			searchTerm.add(new SearchItem<String>(LogSearchItemKey.MemberName,SearchItem.Comparison.LK,memberName));
		}
		SortTerm sortTerm = new SortTerm();
		sortTerm.add(new DescSortItem(LogSortItemKey.CreateDate));
		PageTerm pageTerm = new PageTerm();
		pageTerm.setBeginIndex(0);
		pageTerm.setPageSize(1);
		UploadLog[] uploadLogs = logService.searchUploadLogs(searchTerm, sortTerm, pageTerm);
		StringBuffer buffer = new StringBuffer();
		DateFormat df = new DateFormat();
		String displayInfo = null;
		String createDate = null;
		String flag = null;
		if (shareRecords != null && shareRecords.size() >0 && shareRecords.get(0) != null) {
			Object[] shareRecord = shareRecords.get(0);
			ArrayList<GroupResourceReceive> rList = shareReceiveMap.get((Long)shareRecord[3]);
			if (rList != null && rList.size()>0 && rList.get(0) != null) {
				displayInfo = rList.get(0).getResource().getName();
				createDate = df.format((Date)shareRecord[2]);
				flag = "receive";
			}
		}

		//接受记录不存在就显示上传记录
		if (displayInfo == null && uploadLogs != null && uploadLogs.length>0 && uploadLogs[0]!=null) {
			displayInfo = JS.quote(Filter.convertHtmlBody(uploadLogs[0].getTargetObject()));
			createDate = df.format((Date)uploadLogs[0].getCreateDate());
			flag = "uploadlog";
		}
		if (displayInfo == null ) {
			displayInfo = "该用户没有可以显示的接收记录或者上传记录";
		}

		buffer.append("{");
		buffer.append("\"displayInfo\":\"").append(displayInfo).append("\",");
		buffer.append("\"tag\":\"").append(flag).append("\",");
		buffer.append("\"date\":\"").append(createDate).append("\"}");
		return buffer.toString();
	}

}