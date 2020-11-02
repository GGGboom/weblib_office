package com.dcampus.weblib.web.action;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.Future;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.dcampus.common.generic.GenericDao;
import com.dcampus.common.paging.*;
import com.dcampus.sys.entity.keys.UserSearchItemKey;
import com.dcampus.weblib.dao.CategoryDao;
import com.dcampus.weblib.dao.GroupDao;
import com.dcampus.weblib.entity.*;
import com.dcampus.weblib.exception.PermissionsException;
import com.dcampus.weblib.service.*;
import com.dcampus.weblib.service.permission.impl.Permission;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dcampus.common.config.PropertyUtil;
import com.dcampus.common.util.HTML;
import com.dcampus.common.util.JS;
import com.dcampus.common.web.BaseController;
import com.dcampus.sys.entity.User;
import com.dcampus.sys.service.UserService;
import com.dcampus.sys.util.UserUtils;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.service.permission.IPermission;
import com.dcampus.weblib.service.permission.IPermission.GroupPerm;
import com.dcampus.weblib.service.permission.PermCollection;
import com.dcampus.weblib.service.permission.impl.PermProperty;
import com.dcampus.weblib.service.permission.impl.PermUtil;
import com.dcampus.weblib.util.CurrentUserWrap;
import com.dcampus.weblib.util.Filter;

/**
 * 所有跟柜子和资源有关的处理类
 * @author patrick
 *
 */
@Controller
@RequestMapping(value = "/group")
public class GroupController extends BaseController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private PermissionService permService;
	
	@Autowired
	private ResourceService resourceService;
	
	@Autowired
	private GroupService groupService;
	
	@Autowired
	private ApplicationService appService;
	
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private GrouperService grouperService;

	@Autowired
	private IconService iconService;

	@Autowired
	private Permission permission;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private CategoryDao categoryDao;

	@Autowired
	private GroupDao groupDao;

    @Autowired
	private GenericDao genericDao;



	private boolean isEmpty(String value) {
		if (value == null || value.trim().length() == 0) {
			return true;
		}
		return false;
	}

	/**
	 *
	 * @param account  用户账号
	 * @param createDate_begin  个人柜子创建日期开始位置
	 * @param createDate_end  个人柜子创建日期截止位置
	 * @param start  搜索索引开始位置
	 * @param limit  搜索索引截止位置
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getTopUsedCapacity",produces = "application/json; charset=UTF-8")
	public String getTopUsedCapacity(String account, Timestamp createDate_begin, Timestamp createDate_end,
									 Integer start, Integer limit){
		Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
		Long memberId = currentUserWrap.getMemberId();
		String hql= "from Category where name=?1";
		Category category=genericDao.findFirst(hql,"#person");
		Long categoryId=category.getId();
		List<Group>  groupsAll=new ArrayList<>();
		List<Group>  groups=new ArrayList<>();
		//过滤掉时间，获取到group信息
		start=start==null?0:start;
		limit=limit==null? Integer.MAX_VALUE:limit;
		if(createDate_begin==null ||createDate_begin.equals("")){
                createDate_begin=new Timestamp(0000, 00, 00, 00, 00, 00, 00);
		}

		if(createDate_end==null || createDate_end.equals(""))
			createDate_end =new Timestamp(System.currentTimeMillis());
		int totalCount=0;

		if(account == null|| account.equals("")){
               hql="from "+Group.class.getName()+" as g where g.category.id=?1 and g.createDate >=?2 and g.createDate<=?3 order by usedCapacity DESC";
			   groupsAll=genericDao.findAll(hql, categoryId, createDate_begin, createDate_end);
			   totalCount=groupsAll.size();
			   if(start>=totalCount){
				StringBuffer stringBuffer=new StringBuffer();
				stringBuffer.append("{\"totalCount\":").append(totalCount).append(",");
				stringBuffer.append("\"groups\":[").append("]}");
				return stringBuffer.toString();
			   }
               groups=genericDao.findAll(start, limit, hql, categoryId, createDate_begin, createDate_end);
			   totalCount=groupsAll.size();
		} else {
			hql="from "+Group.class.getName()+" as g where g.displayName like '%"+account+"%' and g.category.id=?1 and g.createDate >=?2 and g.createDate<=?3 order by usedCapacity DESC";
			groupsAll=genericDao.findAll(hql, categoryId, createDate_begin, createDate_end);
			totalCount=groupsAll.size();
			if(start>=totalCount){
				StringBuffer stringBuffer=new StringBuffer();
				stringBuffer.append("{\"totalCount\":").append(totalCount).append(",");
				stringBuffer.append("\"groups\":[").append("]}");
				return stringBuffer.toString();
			}
			groups=genericDao.findAll(start, limit, hql, categoryId, createDate_begin, createDate_end);
		}

        if(groups==null && groups.size()==0)
        	throw new GroupsException("返回列表为空！");
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		sb.append("\"totalCount\":").append(totalCount).append(",");
		sb.append("\"groups\":[");
		for (Group bean : groups) {
//			if(bean.getMember()==null){
//				groupService.deleteGroup(bean.getId());
//			}else {
				sb.append("{");
				sb.append("\"groupId\":").append(bean.getId()).append(",");
				sb.append("\"account\":\"").append(bean.getDisplayName()).append("\",");
				sb.append("\"name\":\"").append(bean.getUser()== null ? "" : JS.quote(HTML.escape(bean.getUser().getName()))).append("\",");

				sb.append("\"company\":\"").append(bean.getUser()==null?"":bean.getUser().getCompany()).append("\",");
				sb.append("\"department\":\"").append(bean.getUser()==null?"":bean.getUser().getDepartment()).append("\",");
				sb.append("\"position\":\"").append(bean.getUser()==null?"":bean.getUser().getPosition()).append("\",");
				sb.append("\"email\":\"").append(bean.getUser()==null?"":bean.getUser().getEmail()).append("\",");
				sb.append("\"im\":\"").append(bean.getUser()==null?"":bean.getUser().getIm()).append("\",");
				sb.append("\"mobile\":\"").append(bean.getUser()==null?"":bean.getUser().getMobile()).append("\",");
				sb.append("\"phone\":\"").append(bean.getUser()==null?"":bean.getUser().getPhone()).append("\",");
				sb.append("\"status\":\"").append(bean.getUser()==null?"":bean.getUser().getUserbaseStatus()).append("\",");

				sb.append("\"usedCapacity\":").append(bean.getUsedCapacity()).append(",");
				sb.append("\"totalCapacity\":").append(bean.getTotalFileSize()).append(",");
//				sb.append("\"createDate\":\"").append(bean.getCreateDate().toString()
//								.substring(0,bean.getCreateDate().toString().indexOf("."))).append("\",");
//				sb.append("\"creator\":\"").append(bean.getCreatorName()).append("\"");
				sb.append("},");
//			}
		}
		if (groups.size() > 0)
			sb.setLength(sb.length() - 1);
		sb.append("]}");
		return sb.toString();
	}

	/**
	 * 创建个人柜子接口
	 * @param memberId  用户ID
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/createPersonalGroup",produces = "application/json; charset=UTF-8")
	public String createPersonalGroup(Long memberId) {
		GroupType groupType = groupService.getGroupTypeByName("个人");
		Category categoryPerson = categoryService.getCategoryByName("#person");
		if (categoryPerson.getAvailableCapacity() < groupType.getTotalFileSize()) {
			throw new GroupsException("个人资源库的可用容量不足");
		}
		Member memberBean =grouperService.getMemberById(memberId);
		String[] categoryNames = PropertyUtil.getDefaultGroupCategory()[0]
				.split(":");
		String categoryName = categoryNames[0];
		String categoryDisplayName = categoryNames[1];
		Category category = null;
		List<Category> categories = categoryService.getCategoriesByName(categoryName);
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
			String hql="from User where account=?1";
			User bean=genericDao.findFirst(hql,memberBean.getAccount());
			groupService.createGroup_v1(bean,groupBean, false, memberBean.getAccount(), OldPerm.SYSTEMADMIN_MEMBER_ID);
		}
		return  "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
	/**
	 * 获取柜子的顶层分类信息
	 * @param groupId
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getTopCategoryInfo",produces = "application/json; charset=UTF-8")
	public String getTopCategoryInfo(Long groupId){
		Group group=groupService.getGroupById(groupId);
		Category categoryBean=group.getCategory();//914
		if(categoryBean!=null){
			while (true){
				Category category=categoryService.getCategoryById(categoryBean.getParentId());//24
				if(category==null||category.getParentId()==0)
					break;
				Long parentId=categoryBean.getParentId();//24
				categoryBean=categoryService.getCategoryById(parentId);
				if(categoryBean==null){
					break;
				}

			}
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		buffer.append("\"category\":[");
		if(categoryBean!=null){
			buffer.append("{");
			buffer.append("\"id\":").append(categoryBean.getId()).append(",");

			buffer.append("\"desc\":\"").append(JS.quote(HTML.escape(categoryBean.getDesc() == null ? "" : categoryBean.getDesc()))).append("\",");
			buffer.append("\"displayName\":\"").append(categoryBean.getDisplayName()).append("\",");


			Subject currentUser = SecurityUtils.getSubject();
			Session session = currentUser.getSession();
			CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
			Long memberId = currentUserWrap.getMemberId();

			PermCollection pc = permissionService.getPermission(categoryBean.getId(), memberId, OldPerm.PERM_TYPE_CATEGORY);
			buffer.append("\"addcategory\":").append(PermUtil.containCategoryPermission(pc.getCategoryPerm(), IPermission.CategoryPerm.CREATE_CATEGORY)).append(",");
			buffer.append("\"addgroup\":").append(PermUtil.containCategoryPermission(pc.getCategoryPerm(), IPermission.CategoryPerm.CREATE_GROUP)).append(",");
			buffer.append("\"managecategory\":").append(PermUtil.containCategoryPermission(pc.getCategoryPerm(), IPermission.CategoryPerm.MANAGE_CATEGORY)).append(",");

			List<Category> childs = categoryService.getCategorieByParent(categoryBean.getId());
			buffer.append("\"leaf\":").append(((childs != null || childs.size() > 0) ? true : false)).append(",");

			buffer.append("\"name\":\"").append(categoryBean.getName()).append("\",");
			buffer.append("\"order\":").append((int)categoryBean.getOrder()).append(",");
			buffer.append("\"parentId\":").append(categoryBean.getParentId()).append(",");
			buffer.append("\"status\":").append(categoryBean.getCategoryStatus()).append(",");
			buffer.append("\"availableCapacity\":").append(categoryBean.getAvailableCapacity()).append(",");
			buffer.append("\"totalCapacity\":").append(categoryBean.getTotalCapacity()).append(",");
			buffer.append("\"creatorName\":\"").append(categoryBean.getCreatorName()).append("\",");
			buffer.append("\"creationDate\":\"").append(categoryBean.getCreateDate().toString()
					.substring(0,categoryBean.getCreateDate().toString().indexOf("."))).append("\"");

			buffer.append("}");
		}
		buffer.append("]}");

		return buffer.toString();
	}
	/**
	 * 获取柜子的父分类信息
	 * @param groupId
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getParentCategoryInfo",produces = "application/json; charset=UTF-8")
	public String getParentCategoryInfo(Long groupId){
		Group group=groupService.getGroupById(groupId);
		Category categoryBean=group.getCategory();
		if(categoryBean!=null){
			while (categoryBean.getTotalCapacity()<=0){
				Long parentId=categoryBean.getParentId();
				categoryBean=categoryService.getCategoryById(parentId);
				if(categoryBean==null){
					break;
				}

			}
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		buffer.append("\"category\":[");
		if(categoryBean!=null){
			buffer.append("{");
			buffer.append("\"id\":").append(categoryBean.getId()).append(",");

			buffer.append("\"desc\":\"").append(JS.quote(HTML.escape(categoryBean.getDesc() == null ? "" : categoryBean.getDesc()))).append("\",");
			buffer.append("\"displayName\":\"").append(categoryBean.getDisplayName()).append("\",");


			Subject currentUser = SecurityUtils.getSubject();
			Session session = currentUser.getSession();
			CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
			Long memberId = currentUserWrap.getMemberId();

			PermCollection pc = permissionService.getPermission(categoryBean.getId(), memberId, OldPerm.PERM_TYPE_CATEGORY);
			buffer.append("\"addcategory\":").append(PermUtil.containCategoryPermission(pc.getCategoryPerm(), IPermission.CategoryPerm.CREATE_CATEGORY)).append(",");
			buffer.append("\"addgroup\":").append(PermUtil.containCategoryPermission(pc.getCategoryPerm(), IPermission.CategoryPerm.CREATE_GROUP)).append(",");
			buffer.append("\"managecategory\":").append(PermUtil.containCategoryPermission(pc.getCategoryPerm(), IPermission.CategoryPerm.MANAGE_CATEGORY)).append(",");

			List<Category> childs = categoryService.getCategorieByParent(categoryBean.getId());
			buffer.append("\"leaf\":").append(((childs != null || childs.size() > 0) ? true : false)).append(",");

			buffer.append("\"name\":\"").append(categoryBean.getName()).append("\",");
			buffer.append("\"order\":").append((int)categoryBean.getOrder()).append(",");
			buffer.append("\"parentId\":").append(categoryBean.getParentId()).append(",");
			buffer.append("\"status\":").append(categoryBean.getCategoryStatus()).append(",");
			buffer.append("\"availableCapacity\":").append(categoryBean.getAvailableCapacity()).append(",");
			buffer.append("\"totalCapacity\":").append(categoryBean.getTotalCapacity()).append(",");
			buffer.append("\"creatorName\":\"").append(categoryBean.getCreatorName()).append("\",");
			buffer.append("\"creationDate\":\"").append(categoryBean.getCreateDate().toString()
					.substring(0,categoryBean.getCreateDate().toString().indexOf("."))).append("\"");

			buffer.append("}");
		}
		buffer.append("]}");

		return buffer.toString();
	}

	/**
	 * 获取文件柜信息接口
	 * @param groupId
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getGroupInfo_v2",produces = "application/json; charset=UTF-8")
	public String getGroupInfo_v2(Long groupId){
		Group groupBean=groupService.getGroupById(groupId);
		if(groupBean.getAvailableCapacity()==null){
			String tt=this.getResourceSize_v2(groupId);
		}
		System.out.println("柜子的可用容量是"+groupBean.getAvailableCapacity());
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
			buffer.append("\"name\":\"").append(groupBean.getName()).append("\",");
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
	 * 根据柜子id获取总空间和已用空间，kb
	 * @param groupId
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getResourceSize", produces = "application/json; charset=UTF-8")
	public String getResourceSize(Long groupId){
		if (groupId == null || groupId == 0) {
			throw new GroupsException("柜子不能为空！");
		}
		long resourcesSize = resourceService.getResourcesSize(groupId);
//		long totalSize = resourceService.getResourceSpaceSize(groupId);
		//modify by mi
		Group group=groupService.getGroupById(groupId);
		long totalSize = group.getTotalFileSize();
		StringBuffer sb = new StringBuffer();
		sb.append("{\"resourcesSize\":").append(resourcesSize).append(",");
		sb.append("\"totalSize\":").append(totalSize).append("}");
		return sb.toString();
	}


	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getResourceSize_v2", produces = "application/json; charset=UTF-8")
	public String getResourceSize_v2(Long groupId){
		if (groupId == null || groupId == 0) {
			throw new GroupsException("柜子不能为空！");
		}
		//判断权限问题
		if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), groupId,
				GroupPerm.VIEW_RESOURCE))
			throw PermissionsException.GroupException;

//		long resourcesSize = resourceService.getResourcesSize(groupId);
//		long totalSize = resourceService.getResourceSpaceSize(groupId);
		Group groupbean=groupService.getGroupById(groupId);
		long totalSize=groupbean.getTotalFileSize();
		long resourcesSize=0L;
		if(groupbean.getAvailableCapacity()==null){
			resourcesSize = resourceService.getResourcesSize(groupId);
			groupbean.setAvailableCapacity(totalSize-resourcesSize);
			groupbean.setUsedCapacity(groupbean.getTotalFileSize()-groupbean.getAvailableCapacity());
		}
		else
			resourcesSize=totalSize-groupbean.getAvailableCapacity();
		groupbean.setAvailableCapacity(totalSize-resourcesSize);
		groupbean.setUsedCapacity(groupbean.getTotalFileSize()-groupbean.getAvailableCapacity());
		groupService.saveOrUpdateGroup(groupbean);
		StringBuffer sb = new StringBuffer();
		sb.append("{\"resourcesSize\":").append(resourcesSize).append(",");
		sb.append("\"totalSize\":").append(totalSize).append("}");
		return sb.toString();
	}
	/**
	 * 移到回收站
	 * @param id 放到回收站的资源id数组
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/recycleResource", produces = "application/json; charset=UTF-8")
	public String recycleResource(Long[] id) throws Exception {
		if (id == null || id.length == 0) {
			throw new GroupsException("资源id不能为空！");
		}
		Map<Long, Long> recycleMap = new HashMap<Long, Long>();
		for (long _id : id) {
			GroupResource res = resourceService.getResourceById(_id);
			Group group = res.getGroup();
			long groupId = group.getId();
			long recycleId = 0L;
			if (!recycleMap.containsKey(groupId)) {
				GroupResource recycle = resourceService.getResource(
						groupId, PropertyUtil.getRecyclerName(), 0,
						GroupResource.RESOURCE_TYPE_DIRECTORY);
				if (recycle == null || recycle.getId() <= 0) {
					resourceService.createResourceDir(groupId,PropertyUtil.getRecyclerName(), 0,group.getCreatorId(), true);
				}
				recycle = resourceService.getResource(groupId, PropertyUtil.getRecyclerName(), 0,GroupResource.RESOURCE_TYPE_DIRECTORY);
				recycleId = recycle.getId();
				recycleMap.put(groupId, recycleId);
			} else {
				recycleId = recycleMap.get(groupId);
			}
			resourceService.moveResource(_id, recycleId, groupId, true);
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/** 
	 * 根据分类获取可见柜子或分类
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/trees", produces = "application/json; charset=UTF-8")
	public String trees(Long categoryId, Boolean containAblumCategory, Boolean containPersonGroup,Boolean withoutLeaf){
		if (containAblumCategory == null) {
			containAblumCategory = true;
		}
		if (containPersonGroup == null) {
			containPersonGroup = true;
		}
		if (categoryId == null) {
			categoryId = 0L;
		}
		if (withoutLeaf == null) {
			withoutLeaf = true;
		}
		
		Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
		Long memberId = currentUserWrap.getMemberId();
		
		IBaseBean<Long>[] beans = groupService.trees(memberId,categoryId, false);
		String personalGroupName = ""+memberId;
		Group personalGroup = null;
		if(containPersonGroup){
			personalGroup = groupService.getGroupByName(personalGroupName);
		}
		
//		//对返回的结果进行排序 目录在前文件柜在后并且分别按顺序号排序
//		// patrick 20160812
//		List<IBaseBean<Long>> beanList0 = new ArrayList<IBaseBean<Long>>();	
//		List<Category> categoryBeanList0 = new ArrayList<Category>();
//		List<Group> groupBeanList0 = new ArrayList<Group>();
//
//		for (IBaseBean<Long> bean : beans) {
//			if (bean instanceof Category) {
//				Category categoryBean = (Category) bean;
//				categoryBeanList0.add(categoryBean);
//
//			}else{
//				Group groupBean = (Group) bean;
//				groupBeanList0.add(groupBean);
//
//			}
//		}
//		Collections.sort(categoryBeanList0, Category.COMPARE_SORT_ORDER);
//		Collections.sort(groupBeanList0, Group.COMPARE_SORT_ORDER);
//		beanList0.addAll(categoryBeanList0);
//		beanList0.addAll(groupBeanList0);
//		beans = beanList0.toArray(new IBaseBean[0]);

		if (!containAblumCategory && categoryId == 0) {
			List<IBaseBean<Long>> beanList = new ArrayList<IBaseBean<Long>>();		
			for (IBaseBean<Long> bean : beans) {
				// 只对柜子进行权限设置，分类不需要
				if (bean instanceof Category) {
					Category categoryBean = (Category) bean;
					if (PropertyUtil.getDefaultAblumCategory().equals(categoryBean.getName())) {
						continue;
					}
				}
				beanList.add(bean);
			}
			beans = beanList.toArray(new IBaseBean[0]);
		}
		// 判断是否有根节点         ///////todo20141202fzd
		boolean[] leaf = new boolean[beans.length];
		if (!withoutLeaf) {
			for (int i = 0; i < leaf.length; ++i) {
				IBaseBean<Long> bean = beans[i];
				if (bean instanceof Group) {
					leaf[i] = true;
				}
				if (bean instanceof Category) {
					IBaseBean<Long>[] children = groupService.trees(memberId, bean.getId(), containPersonGroup);
					if (children.length > 0)
						leaf[i] = false;
					else
						leaf[i] = true;
				}
			}
		}
		// 获取用户的权限
		Map<Long, GroupPerm[]> map = new TreeMap<Long, GroupPerm[]>();
		//是否柜子管理员
		Map<Long, Boolean> groupManagerMap = new TreeMap<Long, Boolean>();
		for (IBaseBean<Long> bean : beans) {
			// 只对柜子进行权限设置，分类不需要
			if (bean instanceof Group) {
				//System.out.println("=========bean instanceof Group====");
				PermCollection pc = permService.getPermission(bean.getId(),memberId,OldPerm.PERM_TYPE_GROUP);
				//System.out.println("=========bean===="+bean.getId()+"   pc.getGroupPerm:"+pc.getGroupPerm());
				map.put(bean.getId(), pc.getGroupPerm());
				boolean isGroupManager = PermUtil.containPermission(pc.getGroupPerm(), GroupPerm.MANAGE_GROUP);
				groupManagerMap.put(bean.getId(), isGroupManager);
			}
		}
		
//		是否柜子管理员
		Map<Long, Long> watchMap = new TreeMap<Long, Long>();

		List<Watch> watches = groupService.getWatchesByMember(memberId.longValue(), Watch.WATCH_TYPE_GROUP, 0, 500);
		for (Watch watch : watches) {
			watchMap.put(watch.getTargetId(), watch.getId());
		}
		List<GroupIcon> iconBeans = groupService.getAllGroupIcons();
		Map<Long, GroupIcon> iconMap = new HashMap<Long, GroupIcon>();
		for (GroupIcon icon : iconBeans) {
			iconMap.put(icon.getId(), icon);
		}
		//////////////////////////////////////////////////
		StringBuffer sb = new StringBuffer();
		StringBuffer sb1 = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();	
		sb.append("{\"children\":[");
		for (int i = 0; i < beans.length; ++i) {
			IBaseBean<Long> bean = beans[i];
			boolean isLeaf = leaf[i];
			if (bean instanceof Group) {
				sb1.append("{");
				sb1.append("\"id\":").append(bean.getId()).append(",");
				Group groupBean = (Group) bean;
				String name = JS.quote(HTML.escape(groupBean.getName()));
				sb1.append("\"name\":\"").append(name).append("\",");
				sb1.append("\"documentType\":").append(groupBean.getDocumentTypeValue()).append(",");
				sb1.append("\"displayName\":\"").append(JS.quote(HTML.escape(groupBean.getDisplayName() == null ? "" : groupBean.getDisplayName()))).append("\",");
				sb1.append("\"desc\":\"").append(JS.quote(HTML.escape(groupBean.getDesc() == null ? "" : groupBean.getDesc()))).append("\",");				
				sb1.append("\"type\":\"group\",");
				sb1.append("\"paiban\":\"").append(groupBean.getExtendField1()==null?"":groupBean.getExtendField1()).append("\",");
				sb1.append("\"isGroupManager\":").append(groupManagerMap.get(bean.getId())).append(",");
				long watchId = watchMap.containsKey(bean.getId()) ? watchMap.get(bean.getId()) : 0L;
				sb1.append("\"watchId\":").append(watchId).append(",");
				IPermission.GroupPerm[] gps = map.get(bean.getId());
				sb1.append("\"upload\":").append(hasPerm(gps,IPermission.GroupPerm.UPLOAD_RESOURCE)).append(",");
				sb1.append("\"delete\":").append(hasPerm(gps,IPermission.GroupPerm.DELETE_RESOURCE)).append(",");
				sb1.append("\"addDir\":").append(hasPerm(gps, IPermission.GroupPerm.ADD_FOLDER)).append(",");
				sb1.append("\"modify\":").append(hasPerm(gps,IPermission.GroupPerm.MODIFY_RESOURCE)).append(",");
				sb1.append("\"leaf\":").append(true).append(",");
				sb1.append("\"order\":").append((int)groupBean.getOrder());
				sb1.append("},");
			}
			if (bean instanceof Category) {
				sb2.append("{");
				sb2.append("\"id\":").append(bean.getId()).append(",");
				Category categoryBean = (Category) bean;
				String name = JS.quote(HTML.escape(categoryBean.getName()));
				sb2.append("\"name\":\"").append(name).append("\",");
				if (categoryBean.getDisplayName() == null || (categoryBean.getDisplayName().equals(""))) {
					sb2.append("\"displayName\":\"").append(name).append("\",");
				} else {
					sb2.append("\"displayName\":\"").append(JS.quote(HTML.escape(categoryBean.getDisplayName()))).append("\",");
				}
				sb2.append("\"desc\":\"").append(JS.quote(HTML.escape(categoryBean.getDesc()==null?"":categoryBean.getDesc()))).append("\",");
				sb2.append("\"type\":\"category\",");
				sb2.append("\"upload\":false, \"delete\":false, \"modify\":false,");
				sb2.append("\"leaf\":").append(isLeaf).append(",");
				sb2.append("\"order\":").append((int)categoryBean.getOrder());
				sb2.append("},");
			}
		}
		if(sb2.length() > 0 ){
			sb.append(sb2);
		}
		if(sb1.length() > 0 ){
			sb.append(sb1);
		}
		if (beans.length > 0) {
			sb.setLength(sb.length() - 1);
		}
		sb.append("],");
		sb.append("\"personalGroup\":");
		if(personalGroup!=null){
			sb.append("{");
			sb.append("\"id\":").append(personalGroup.getId()).append(",");
			sb.append("\"categoryId\":").append(personalGroup.getCategory().getId()).append(",");
			sb.append("\"name\":\"").append(personalGroup.getName()).append("\",");
			sb.append("\"displayName\":\"").append(JS.quote(Filter.convertHtmlBody(personalGroup.getDisplayName() == null ? "" : personalGroup.getDisplayName()))).append("\",");			
			if (iconMap.containsKey(personalGroup.getGroupIcon())) {
				GroupIcon icon = iconMap.get(personalGroup.getGroupIcon());
				sb.append("\"icon\":\"").append(PropertyUtil.getGroupIconFolderPath()+icon.getFileName()).append("\",");
				sb.append("\"iconName\":\"").append(JS.quote(Filter.convertHtmlBody(icon.getName() == null ? "" : icon.getName()))).append("\",");
				sb.append("\"iconId\":").append(personalGroup.getGroupIcon()).append(",");	
			} else {
				sb.append("\"icon\":\"").append(PropertyUtil.getGroupDefaultIcon()).append("\",");
				sb.append("\"iconName\":\"").append("").append("\",");
				sb.append("\"iconId\":").append(0).append(",");
			}	
			sb.append("\"order\":").append((int)personalGroup.getOrder()).append(",");						
			sb.append("\"desc\":\"").append(JS.quote(Filter.convertHtmlBody(personalGroup.getDesc()))).append("\",");
			sb.append("\"paiban\":\"").append(personalGroup.getExtendField1()==null?"":personalGroup.getExtendField1()).append("\",");
			sb.append("\"documentType\":").append(personalGroup.getDocumentTypeValue());
			sb.append("}");
		}else{
			sb.append("{}");
		}
		sb.append("}");
		return sb.toString();
	}
	
	/** 
	 * 根据分类获取可见柜子或分类
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/trees_v2", produces = "application/json; charset=UTF-8")
	public String trees_v2(Long categoryId, Boolean containAblumCategory, Boolean containPersonGroup,Boolean withoutLeaf){
		if (containAblumCategory == null) {
			containAblumCategory = true;
		}
		if (containPersonGroup == null) {
			containPersonGroup = true;
		}
		if (categoryId == null) {
			categoryId = 0L;
		}
		if (withoutLeaf == null) {
			withoutLeaf = true;
		}
		
		Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
		Long memberId = currentUserWrap.getMemberId();
		
		IBaseBean<Long>[] beans = groupService.trees(memberId,categoryId, false);
		String personalGroupName = ""+memberId;
		Group personalGroup = null;
		if(containPersonGroup){
			personalGroup = groupService.getGroupByName(personalGroupName);
		}
		
		//对返回的结果进行排序 目录在前文件柜在后并且分别按顺序号排序
		// patrick 20160812
		List<IBaseBean<Long>> beanList0 = new ArrayList<IBaseBean<Long>>();	
		List<Category> categoryBeanList0 = new ArrayList<Category>();
		List<Group> groupBeanList0 = new ArrayList<Group>();

		for (IBaseBean<Long> bean : beans) {
			if (bean instanceof Category) {
				Category categoryBean = (Category) bean;
				categoryBeanList0.add(categoryBean);

			}else{
				Group groupBean = (Group) bean;
				groupBeanList0.add(groupBean);

			}
		}
		Collections.sort(categoryBeanList0, Category.COMPARE_SORT_ORDER);
		Collections.sort(groupBeanList0, Group.COMPARE_SORT_ORDER);
		beanList0.addAll(categoryBeanList0);
		beanList0.addAll(groupBeanList0);
		beans = beanList0.toArray(new IBaseBean[0]);

		if (!containAblumCategory && categoryId == 0) {
			List<IBaseBean<Long>> beanList = new ArrayList<IBaseBean<Long>>();		
			for (IBaseBean<Long> bean : beans) {
				// 只对柜子进行权限设置，分类不需要
				if (bean instanceof Category) {
					Category categoryBean = (Category) bean;
					if (PropertyUtil.getDefaultAblumCategory().equals(categoryBean.getName())) {
						continue;
					}
				}
				beanList.add(bean);
			}
			beans = beanList.toArray(new IBaseBean[0]);
		}
		// 判断是否有根节点         ///////todo20141202fzd
		boolean[] leaf = new boolean[beans.length];
		if (!withoutLeaf) {
			for (int i = 0; i < leaf.length; ++i) {
				IBaseBean<Long> bean = beans[i];
				if (bean instanceof Group) {
					leaf[i] = true;
				}
				if (bean instanceof Category) {
					IBaseBean<Long>[] children = groupService.trees(memberId, bean.getId(), containPersonGroup);
					if (children.length > 0)
						leaf[i] = false;
					else
						leaf[i] = true;
				}
			}
		}
		// 获取用户的权限
		Map<Long, GroupPerm[]> map = new TreeMap<Long, GroupPerm[]>();
		//是否柜子管理员
		Map<Long, Boolean> groupManagerMap = new TreeMap<Long, Boolean>();
		for (IBaseBean<Long> bean : beans) {
			// 只对柜子进行权限设置，分类不需要
			if (bean instanceof Group) {
				//System.out.println("=========bean instanceof Group====");
				PermCollection pc = permService.getPermission(bean.getId(),memberId,OldPerm.PERM_TYPE_GROUP);
				//System.out.println("=========bean===="+bean.getId()+"   pc.getGroupPerm:"+pc.getGroupPerm());
				map.put(bean.getId(), pc.getGroupPerm());
				boolean isGroupManager = PermUtil.containPermission(pc.getGroupPerm(), GroupPerm.MANAGE_GROUP);
				groupManagerMap.put(bean.getId(), isGroupManager);
			}
		}
		
//		是否柜子管理员
		Map<Long, Long> watchMap = new TreeMap<Long, Long>();

		List<Watch> watches = groupService.getWatchesByMember(memberId.longValue(), Watch.WATCH_TYPE_GROUP, 0, 500);
		for (Watch watch : watches) {
			watchMap.put(watch.getTargetId(), watch.getId());
		}
		List<GroupIcon> iconBeans = groupService.getAllGroupIcons();
		Map<Long, GroupIcon> iconMap = new HashMap<Long, GroupIcon>();
		for (GroupIcon icon : iconBeans) {
			iconMap.put(icon.getId(), icon);
		}
		//////////////////////////////////////////////////
		StringBuffer sb = new StringBuffer();
		StringBuffer sb1 = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();
		for (int i = 0; i < beans.length; ++i) {
			IBaseBean<Long> bean = beans[i];
			boolean isLeaf = leaf[i];
			if (bean instanceof Group) {
				sb1.append("{");
				sb1.append("\"id\":").append(bean.getId()).append(",");
				Group groupBean = (Group) bean;
				String name = JS.quote(HTML.escape(groupBean.getName()));
				sb1.append("\"name\":\"").append(name).append("\",");
				sb1.append("\"documentType\":").append(groupBean.getDocumentTypeValue()).append(",");
				sb1.append("\"displayName\":\"").append(JS.quote(HTML.escape(groupBean.getDisplayName() == null ? "" : groupBean.getDisplayName()))).append("\",");
				sb1.append("\"desc\":\"").append(JS.quote(HTML.escape(groupBean.getDesc() == null ? "" : groupBean.getDesc()))).append("\",");				
				sb1.append("\"type\":\"group\",");
				sb1.append("\"layout\":\"").append(groupBean.getExtendField1()==null?"":groupBean.getExtendField1()).append("\",");
				long watchId = watchMap.containsKey(bean.getId()) ? watchMap.get(bean.getId()) : 0L;
				sb1.append("\"watchId\":").append(watchId).append(",");
				IPermission.GroupPerm[] gps = map.get(bean.getId());
				sb1.append("\"upload\":").append(hasPerm(gps,IPermission.GroupPerm.UPLOAD_RESOURCE)).append(",");
				sb1.append("\"delete\":").append(hasPerm(gps,IPermission.GroupPerm.DELETE_RESOURCE)).append(",");
				sb1.append("\"addDir\":").append(hasPerm(gps, IPermission.GroupPerm.ADD_FOLDER)).append(",");
				sb1.append("\"modify\":").append(hasPerm(gps,IPermission.GroupPerm.MODIFY_RESOURCE)).append(",");
				sb1.append("\"leaf\":").append(true).append(",");
				sb1.append("\"order\":").append((int)groupBean.getOrder());
				sb1.append("},");
			}
			if (bean instanceof Category) {
				sb2.append("{");
				sb2.append("\"id\":").append(bean.getId()).append(",");
				Category categoryBean = (Category) bean;
				String name = JS.quote(HTML.escape(categoryBean.getName()));
				sb2.append("\"name\":\"").append(name).append("\",");
				if (categoryBean.getDisplayName() == null || (categoryBean.getDisplayName().equals(""))) {
					sb2.append("\"displayName\":\"").append(name).append("\",");
				} else {
					sb2.append("\"displayName\":\"").append(JS.quote(HTML.escape(categoryBean.getDisplayName()))).append("\",");
				}
				sb2.append("\"desc\":\"").append(JS.quote(HTML.escape(categoryBean.getDesc()==null?"":categoryBean.getDesc()))).append("\",");
				sb2.append("\"type\":\"category\",");
				sb2.append("\"upload\":false, \"delete\":false, \"modify\":false,");
				sb2.append("\"leaf\":").append(isLeaf).append(",");
				sb2.append("\"order\":").append((int)categoryBean.getOrder());
				sb2.append("},");
			}
		}
		sb.append("{\"category\":[");
		if(sb2.length() > 0 ){
			sb2.setLength(sb2.length() - 1);
			sb.append(sb2);
		}
		sb.append("],\"group\":[");
		if(sb1.length() > 0 ){
			sb1.setLength(sb1.length() - 1);	
			sb.append(sb1);
		}
		
	sb.append("],");
		
		sb.append("\"personalGroup\":");
		if(personalGroup!=null){
			sb.append("{");
			sb.append("\"id\":").append(personalGroup.getId()).append(",");
			sb.append("\"categoryId\":").append(personalGroup.getCategory().getId()).append(",");
			sb.append("\"name\":\"").append(personalGroup.getName()).append("\",");
			sb.append("\"displayName\":\"").append(JS.quote(Filter.convertHtmlBody(personalGroup.getDisplayName() == null ? "" : personalGroup.getDisplayName()))).append("\",");
			if (iconMap.containsKey(personalGroup.getGroupIcon())) {
				GroupIcon icon = iconMap.get(personalGroup.getGroupIcon());
				sb.append("\"icon\":\"").append(PropertyUtil.getGroupIconFolderPath()+icon.getFileName()).append("\",");
				sb.append("\"iconName\":\"").append(JS.quote(Filter.convertHtmlBody(icon.getName() == null ? "" : icon.getName()))).append("\",");
				sb.append("\"iconId\":").append(personalGroup.getGroupIcon()).append(",");	
			} else {
				sb.append("\"icon\":\"").append(PropertyUtil.getGroupDefaultIcon()).append("\",");
				sb.append("\"iconName\":\"").append("").append("\",");
				sb.append("\"iconId\":").append(0).append(",");
			}				
			sb.append("\"order\":").append((int)personalGroup.getOrder()).append(",");						
			sb.append("\"desc\":\"").append(JS.quote(Filter.convertHtmlBody(personalGroup.getDesc()))).append("\",");
			sb.append("\"paiban\":\"").append(personalGroup.getExtendField1()==null?"":personalGroup.getExtendField1()).append("\",");
			sb.append("\"documentType\":").append(
					personalGroup.getDocumentTypeValue());
			sb.append("}");
		}else{
			sb.append("{}");
		}
		sb.append("}");
		return sb.toString();
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
	 * 新建时检查文件柜名是否存在
	 * @param categoryId 分类id
	 * @param displayName 文件柜显示的名字
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/checkGroupDisplayNameExists", produces = "application/json; charset=UTF-8")
	public String checkGroupDisplayNameExists(Long categoryId, String displayName) {
		if (categoryId == null || displayName == null) {
			throw new GroupsException("参数不能为空！");
		}
		boolean exists = true;
		Group group = groupService.getGroupByDisplyName(categoryId, displayName);;
		if (group == null || group.getId() <= 0) {
			exists = false;
		}
		return "{\"exists\":"+exists +"}";
	}
	
	/**
	 * 新建柜子
	 * @param categoryId
	 * @param displayName
	 * @param desc
	 * @param totalSize
	 * @param singleFileSize
	 * @param name
	 * @param addr
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/createGroup", produces = "application/json; charset=UTF-8")
	public String createGroup(Long categoryId, String displayName, String desc, Long totalSize, Long singleFileSize, 
			String name, String addr, Integer documentType, Long iconId, String paiban, Long applicationId, String creatorName) {

		Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
		Long memberId = currentUserWrap.getMemberId();
		String memberName = currentUserWrap.getMemberName();
		Member creator = grouperService.getMemberById(memberId);
		
		Category category = categoryService.getCategoryById(categoryId);
		if (category == null) {
			return "{\"result\":\"failed\", \"message\": \"分类不存在！\"}"; 
		}
		if (name == null || displayName ==null) {
			return "{\"result\":\"failed\", \"message\": \"柜子名字不能为空！\"}"; 
		}
		
		// 创建柜子
		Group groupBean = new Group();
		groupBean.setCategory(category);
		groupBean.setDocumentTypeValue(documentType == null ? GroupResource.DOCUMENT_TYPE_UNKNOWN : documentType.intValue());
		groupBean.setName(name);
		groupBean.setAddr(addr);
		groupBean.setDesc(desc == null ? "" : desc);
		groupBean.setDisplayName(displayName);
		groupBean.setGroupIcon(iconId == null ? 0L : iconId.longValue());
		groupBean.setCreateDate(new Timestamp(System.currentTimeMillis()));
		groupBean.setExtendField1(paiban);
		//===添加应用===
		if (applicationId != null) {
			groupBean.setApplicationId(applicationId);			
			Application applicationBean = appService.getApplicationById(applicationId);			
			if (Long.parseLong(applicationBean.getAvailableSpace())<totalSize) throw  new GroupsException("应用剩余可用空间不足");			
			applicationBean.setAvailableSpace(Long.toString(Long.parseLong(applicationBean.getAvailableSpace())-totalSize));
			appService.modifyApplication(applicationBean);
		}
		//===========		
		double _order = groupService.getGroupMaxOrder(categoryId);
		_order++;
		groupBean.setOrder(_order);
		if (creatorName == null
				|| creatorName.trim().length() == 0
				|| !permService.isAdmin(memberId)) {
			groupBean.setCreatorId(memberId);
			groupBean.setCreatorName(memberName);
		} else {
			List<Member> memberBeans = grouperService.getMembersByName(creatorName);
			if (memberBeans == null || memberBeans.size() == 0) {
				throw new GroupsException("用户 [" + creatorName
						+ "]没有存在记录，他/她还未登录过？");
			}
			groupBean.setCreatorId(memberBeans.get(0).getId());
			groupBean.setCreatorName(memberBeans.get(0).getName());
		}
		if(totalSize>0) {
			groupBean.setTotalFileSize(totalSize);
		}
		groupBean.setGroupStatus(Group.STATUS_NORMAL);
		groupBean.setGroupUsage(Group.USAGE_NORMAL);
		groupBean.setMemberAudit(Group.AUDITMEMBER_NOT_AUDIT);
		groupService.createGroup(groupBean, true , null);

		// 设置groupId，给chain中的createForum使用
		long groupId = groupBean.getId();

		// 创建柜子权限
		this.modifyGroupPermission(null, groupId);

		// 修改柜子容量和单文件大小
		if (singleFileSize > 0) {
			groupService.modifySingleFileSize(groupId, singleFileSize);
		}
		if (totalSize > 0) {
			groupService.modifyTotalFileSize(groupId, totalSize);
		}
		//创建回收站
		resourceService.createResourceDir(groupId,PropertyUtil.getRecyclerName(),0,groupBean.getCreatorId(), true);
		return "{\"type\":\"success\",\"code\":\"200\",\"groupId\":" +groupId+"}";
	}

	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/createGroup_v2", produces = "application/json; charset=UTF-8")
	public String createGroup_v2(Long categoryId, String displayName, String desc, Long totalSize, Long singleFileSize,
							  String name, String addr, Integer documentType, Long iconId, String paiban, Long applicationId,
								 String creatorName,Long topCategoryId) {

		Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
		Long memberId = currentUserWrap.getMemberId();
		String memberName = currentUserWrap.getMemberName();

		Category category = categoryService.getCategoryById(categoryId);
		if (category == null) {
			return "{\"result\":\"failed\", \"message\": \"分类不存在！\"}";
		}
		if (name == null || displayName ==null) {
			return "{\"result\":\"failed\", \"message\": \"柜子名字不能为空！\"}";
		}

		// 创建柜子
		Group groupBean = new Group();
		groupBean.setCategory(category);
		groupBean.setDocumentTypeValue(documentType == null ? GroupResource.DOCUMENT_TYPE_UNKNOWN : documentType.intValue());
		groupBean.setName(name);
		groupBean.setAddr(addr);
		groupBean.setDesc(desc == null ? "" : desc);
		groupBean.setDisplayName(displayName);
		groupBean.setGroupIcon(iconId == null ? 0L : iconId.longValue());
		groupBean.setCreateDate(new Timestamp(System.currentTimeMillis()));

		groupBean.setExtendField1(paiban);
		//===添加应用===
		if (applicationId != null) {
			groupBean.setApplicationId(applicationId);
			Application applicationBean = appService.getApplicationById(applicationId);
			if (Long.parseLong(applicationBean.getAvailableSpace())<totalSize) throw  new GroupsException("应用剩余可用空间不足");
			applicationBean.setAvailableSpace(Long.toString(Long.parseLong(applicationBean.getAvailableSpace())-totalSize));
			appService.modifyApplication(applicationBean);
		}
		//===========
		double _order = groupService.getGroupMaxOrder(categoryId);
		_order++;
		groupBean.setOrder(_order);
		if (creatorName == null
				|| creatorName.trim().length() == 0
				|| !permService.isAdmin(memberId)) {
			groupBean.setCreatorId(memberId);
			groupBean.setCreatorName(memberName);
		} else {
			List<Member> memberBeans = grouperService.getMembersByName(creatorName);
			if (memberBeans == null || memberBeans.size() == 0) {
				throw new GroupsException("用户 [" + creatorName
						+ "]没有存在记录，他/她还未登录过？");
			}
			groupBean.setCreatorId(memberBeans.get(0).getId());
			groupBean.setCreatorName(memberBeans.get(0).getName());
		}
//		if(totalSize>0) {
//			groupBean.setTotalFileSize(totalSize);
//			groupBean.setAvailableCapacity(totalSize);
//		}
		groupBean.setGroupStatus(Group.STATUS_NORMAL);
		groupBean.setGroupUsage(Group.USAGE_NORMAL);
		groupBean.setMemberAudit(Group.AUDITMEMBER_NOT_AUDIT);
//		groupBean.setAvailableCapacity(totalSize);

		//初始化底层柜子id

//		if(topCategoryId!=null){
//			Category category1=categoryService.getCategoryById(topCategoryId);
//			if (category1 == null) {
//				return "{\"result\":\"failed\", \"message\": \"分类不存在！\"}";
//			}else {
//				if(category1.getAvailableCapacity()>=totalSize){
//					groupBean.setTopCategoryId(topCategoryId);
//					category1.setAvailableCapacity(category1.getAvailableCapacity()-totalSize);
//					categoryDao.saveOrUpdateCategory(category1);
//				}
//				else
//					throw new GroupsException("id为"+topCategoryId+"的分类的可用容量不足\n");
//			}
//		}
		long memberIdd = UserUtils.getCurrentMemberId();
		groupService.createGroup_v2(groupBean, true , null,memberIdd,topCategoryId,totalSize);

		// 设置groupId，给chain中的createForum使用
		long groupId = groupBean.getId();

		// 创建柜子权限
		this.modifyGroupPermission(null, groupId);

		// 修改柜子容量和单文件大小
		if (singleFileSize > 0) {
			groupService.modifySingleFileSize(groupId, singleFileSize);
		}
		if (totalSize > 0) {
			groupService.modifyTotalFileSize(groupId, totalSize);
		}
		//创建回收站
		resourceService.createResourceDir(groupId,PropertyUtil.getRecyclerName(),0,groupBean.getCreatorId(), true);
		return "{\"type\":\"success\",\"code\":\"200\",\"groupId\":" +groupId+"}";
	}
	/**
	 * 修改柜子权限,暂时不需要?
	 * @param permissionType 权限类型,1公开，2私有，3留言板
	 * @param groupId 柜子id
	 * @return
	 */
	@Deprecated
	public String modifyGroupPermission(Integer permissionType, Long groupId) {
		if(permissionType == null) {
			permissionType = 0;
		}

		PermCollection gpc = new PermCollection();
		PermCollection mpc = new PermCollection();
		if (permissionType == 1) {
			// 完全公开
			gpc.addGroupPerm(PermProperty.getPublicGroupNonmemberPerm());
			mpc.addGroupPerm(PermProperty.getPublicGroupMemberPerm());
			// 不需审核即可加入柜子
			groupService.setAuditMember(groupId, Group.AUDITMEMBER_NOT_AUDIT);
			// 需要对帖子进行审核
			groupService.setAuditPost(groupId, Group.AUDITPOST_AUDIT);
			// 柜子用途
			groupService.setGroupUsage(groupId, Group.USAGE_PUBLIC);
		} else if (permissionType == 2) {
			// 仅对会员公开
			gpc.addGroupPerm(PermProperty.getPrivateGroupNonmemberPerm());
			mpc.addGroupPerm(PermProperty.getPrivateGroupMemberPerm());
			// 需要对帖子进行审核
			groupService.setAuditPost(groupId, Group.AUDITPOST_AUDIT);
			// 柜子用途
			groupService.setGroupUsage(groupId, Group.USAGE_PRIVATE);
		} else if (permissionType == 3) {
			// 留言板
			gpc.addGroupPerm(PermProperty.getMessageboardGroupNonmemberPerm());
			mpc.addGroupPerm(PermProperty.getMessageboardGroupMemberPerm());
			// 需要对帖子进行审核
			groupService.setAuditPost(groupId, Group.AUDITPOST_AUDIT_ALL);
			// 柜子用途
			groupService.setGroupUsage(groupId, Group.USAGE_GUESTBOOK);
		} else {
			// 系统默认
			gpc.addGroupPerm(PermProperty.getDefaultGroupNonmemberPerm());
			mpc.addGroupPerm(PermProperty.getDefaultGroupMemberPerm());
			// 需要审核才能加入柜子
			groupService.setAuditMember(groupId, Group.AUDITMEMBER_AUDIT);
			// 需要对帖子进行审核
			groupService.setAuditPost(groupId,Group.AUDITPOST_AUDIT);
			// 柜子用途
			groupService.setGroupUsage(groupId, Group.USAGE_NORMAL);
		}

		permService.modifyPermission(groupId, 1, OldPerm.PERM_TYPE_GROUP, gpc);
		permService.modifyPermission(groupId,1 ,OldPerm.PERM_TYPE_GROUP, mpc);
		permService.modifyPermission(groupId, OldPerm.GLOBAL_NONMEMBER_ID, OldPerm.PERM_TYPE_GROUP, gpc);
		permService.modifyPermission(groupId, OldPerm.GLOBAL_MEMBER_ID,OldPerm.PERM_TYPE_GROUP, mpc);

		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
	

	
	
	/********************************************************文件柜图标**********************************************************/
	/**
	 * 获取所有文件柜图标
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/getGroupIcons", produces = "application/json; charset=UTF-8")
	public String getGroupIcons()throws Exception {
		List<GroupIcon> iconBeans = groupService.getAllGroupIcons();
		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		buffer.append("\"totalCount\":").append(iconBeans.size()).append(",");
		buffer.append("\"icons\":[");
		for (GroupIcon iconBean : iconBeans) {
			buffer.append("{");
			buffer.append("\"id\":").append(iconBean.getId()).append(",");
			String name = JS.quote(HTML.escape(iconBean.getName()));
			buffer.append("\"name\":\"").append(name).append(
					"\",");
			buffer.append("\"fileName\":\"").append(iconBean.getFileName()).append(
					"\",");
			buffer.append("\"filePath\":\"").append(PropertyUtil.getGroupIconFolderPath()+iconBean.getFileName()).append(
					"\",");		
			String desc = JS.quote(HTML.escape(iconBean.getDescription()));
			buffer.append("\"desc\":\"").append(desc == null ? "" : desc)
					.append("\",");
			buffer.append("\"sequence\":").append(iconBean.getSequence())
					.append(",");
			buffer.append("\"enable\":").append(iconBean.isEnable());
			buffer.append("},");
		}
		if (iconBeans.size() > 0) {
			buffer.setLength(buffer.length() - 1);
		}
		buffer.append("]}");
		return buffer.toString();
	}
	
	/**
	 * 获取柜子权限信息
	 * @param groupId
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/getGroupPermission", produces = "application/json; charset=UTF-8")
	public String getGroupPermission(Long groupId) throws Exception {
		if (groupId == null || groupId == 0) {
			throw new GroupsException("柜子id不能为空！");
		}
		Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
		Long memberId = currentUserWrap.getMemberId();
		Group group = groupService.getGroupById(groupId);
		PermCollection pc = permService.getPermission(groupId,memberId,OldPerm.PERM_TYPE_GROUP);
		boolean isGroupManager = permService.isGroupManager(memberId, groupId);
		
		StringBuffer sb = new StringBuffer();
		sb.append("{\"groupId\":").append(group.getId()).append(",");
		String name = JS.quote(HTML.escape(group.getName()));
//		sb.append("\"GroupName\":\"").append(name).append("\",");
		sb.append("\"displayName\":\"").append(JS.quote(HTML.escape(group.getDisplayName() == null ? "" : group.getDisplayName()))).append("\",");
		sb.append("\"isGroupManager\":").append(isGroupManager).append(",");				
		sb.append("\"upload\":").append(PermUtil.hasPermission(pc.getGroupPerm(),IPermission.GroupPerm.UPLOAD_RESOURCE)).append(",");
		sb.append("\"delete\":").append(PermUtil.hasPermission(pc.getGroupPerm(),IPermission.GroupPerm.DELETE_RESOURCE)).append(",");
		sb.append("\"addDir\":").append(PermUtil.hasPermission(pc.getGroupPerm(), IPermission.GroupPerm.ADD_FOLDER)).append(",");
		sb.append("\"modify\":").append(PermUtil.hasPermission(pc.getGroupPerm(),IPermission.GroupPerm.MODIFY_RESOURCE)).append("");
		sb.append("}");
		return sb.toString();
	}
	
	/**
	 * 根据分类和名字搜索柜子
	 * @param displayName 柜子名字
	 * @param categoryId 所在分类id
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value={"/searchMyGroups_v2","/searchMyGroups"}, produces = "application/json; charset=UTF-8")
	public String searchMyGroups(String displayName, Long categoryId) throws Exception {
		if (displayName == null || displayName == "") {
			throw new GroupsException("搜索字段不能为空！");
		}
		if (categoryId == null ) {
			categoryId = 0L; 
		}
		Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
		Long memberId = currentUserWrap.getMemberId();
		List<Group> groups = groupService.getMyVisualGroupsWithoutPersonGroup(categoryId,memberId);
		Group[] beans = (Group[])groups.toArray(new Group[groups.size()]);
		List<Group> list = new ArrayList<Group>();
		//获取用户的权限
		Map<Long, GroupPerm[]> map = new TreeMap<Long, GroupPerm[]>();
		//是否柜子管理员
		Map<Long, Boolean> groupManagerMap = new TreeMap<Long, Boolean>();
		if (displayName != null && displayName.trim().length() != 0) {
			for (Group bean : beans) {
				String displayName1 = bean.getDisplayName();
				if (displayName1 == null) {
					displayName1 = bean.getName();
				}
				String displayName2 = displayName1.toLowerCase();
				if (displayName1 != null && displayName2.indexOf(displayName.toLowerCase()) != -1 ) {
					PermCollection pc = permService.getPermission(bean.getId(),memberId,OldPerm.PERM_TYPE_GROUP);
					map.put(bean.getId(), pc.getGroupPerm());
					boolean isGroupManager = permService.isGroupManager(memberId, bean.getId());
					groupManagerMap.put(bean.getId(), isGroupManager);
					list.add(bean);
				}
			}
		}
		List<GroupIcon> iconBeans = groupService.getAllGroupIcons();
		Map<Long, GroupIcon> iconMap = new HashMap<Long, GroupIcon>();
		for (GroupIcon icon : iconBeans) {
			iconMap.put(icon.getId(), icon);
		}
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("{\"totalCount\":").append(list.size()).append(",");
		buffer.append("\"groups\":[");
		for (int i = 0; i < list.size(); ++i) {
			Group groupBean = list.get(i);
			buffer.append("{");
			buffer.append("\"id\":").append(groupBean.getId()).append(",");
			String displayNameNow = groupBean.getDisplayName();
			if (displayNameNow == null) {
				displayNameNow = groupBean.getName();
			}	
			buffer.append("\"displayName\":\"").append(JS.quote(HTML.escape(displayName == null ? "" :displayNameNow))).append("\",");
			buffer.append("\"desc\":\"").append(JS.quote(HTML.escape(groupBean.getDesc() == null ? "" : groupBean.getDesc()))).append("\",");		
			IPermission.GroupPerm[] gps = map.get(groupBean.getId());
			buffer.append("\"upload\":").append(hasPerm(gps,IPermission.GroupPerm.UPLOAD_RESOURCE)).append(",");
			buffer.append("\"delete\":").append(hasPerm(gps,IPermission.GroupPerm.DELETE_RESOURCE)).append(",");
			buffer.append("\"addDir\":").append(hasPerm(gps, IPermission.GroupPerm.ADD_FOLDER)).append(",");
			buffer.append("\"modify\":").append(hasPerm(gps,IPermission.GroupPerm.MODIFY_RESOURCE)).append("");		
			buffer.append("},");
		}
		if (list.size() > 0)
			buffer.setLength(buffer.length() - 1);
		buffer.append("]}");
		return buffer.toString();
	}

	/**
	 * 根据分类和名字搜索柜子
	 * @param displayName 柜子名字
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value={"/searchGroups"}, produces = "application/json; charset=UTF-8")
	public String searchGroups(String displayName, Integer start,Integer limit) throws Exception {
		start = start == null ? 0 : start;
		limit = limit == null ? Integer.MAX_VALUE : limit;
		if (displayName == null || displayName == "") {
			throw new GroupsException("搜索字段不能为空！");
		}

		Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
		Long memberId = currentUserWrap.getMemberId();
		List<Group> groups = groupService.getGroupByDisplyName(displayName);
		long totalCount=groups.size();
		List<Group> list2 = new ArrayList<Group>();
		list2=groupService.getGroupByDisplyNamePart(displayName,start,limit);
		Group[] beans = (Group[])list2.toArray(new Group[list2.size()]);
		List<Group> list = new ArrayList<Group>();
		//获取用户的权限
		Map<Long, GroupPerm[]> map = new TreeMap<Long, GroupPerm[]>();
		//是否柜子管理员
		Map<Long, Boolean> groupManagerMap = new TreeMap<Long, Boolean>();
		if (displayName != null && displayName.trim().length() != 0) {
			for (Group bean : beans) {
				String displayName1 = bean.getDisplayName();
				if (displayName1 == null) {
					displayName1 = bean.getName();
				}
				String displayName2 = displayName1.toLowerCase();
				if (displayName1 != null && displayName2.indexOf(displayName.toLowerCase()) != -1 ) {
					//查看当前用户对这个柜子有没有权限，没有权限就不可以查找柜子
					PermCollection pc = permService.getPermission(bean.getId(),memberId,OldPerm.PERM_TYPE_GROUP);
					if(pc!=null){
						map.put(bean.getId(), pc.getGroupPerm());
						boolean isGroupManager = permService.isGroupManager(memberId, bean.getId());
						groupManagerMap.put(bean.getId(), isGroupManager);
						list.add(bean);
					}

				}
			}
		}
		List<GroupIcon> iconBeans = groupService.getAllGroupIcons();
		Map<Long, GroupIcon> iconMap = new HashMap<Long, GroupIcon>();
		for (GroupIcon icon : iconBeans) {
			iconMap.put(icon.getId(), icon);
		}

		//获取Icon的信息
		List<GroupIcon> icons=new ArrayList<GroupIcon>();
		List  virtualGroup=new ArrayList();
		for(int i = 0; i < list.size(); ++i){
			icons.add(iconService.getIconByGroupId(list.get(i).getId()));
			if(list.get(i).getCategory().getId()==0)
				virtualGroup.add(true);
			else
				virtualGroup.add(false);
		}

		if(start>=totalCount){
			StringBuffer stringBuffer=new StringBuffer();
			stringBuffer.append("{\"totalCount\":").append(totalCount).append(",");
			stringBuffer.append("\"groups\":[").append("]}");
			return stringBuffer.toString();
		}

		StringBuffer buffer = new StringBuffer();
		buffer.append("{\"totalCount\":").append(totalCount).append(",");
		buffer.append("\"groups\":[");
		for (int i = 0; i < list.size(); ++i) {
			Group groupBean = list.get(i);
			buffer.append("{");
			buffer.append("\"categoryId\":").append(groupBean.getCategory().getId()).append(",");

			String displayNameNow = groupBean.getDisplayName();
			if (displayNameNow == null) {
				displayNameNow = groupBean.getName();
			}
			buffer.append("\"desc\":\"").append(JS.quote(HTML.escape(groupBean.getDesc() == null ? "" : groupBean.getDesc()))).append("\",");
			buffer.append("\"displayName\":\"").append(JS.quote(HTML.escape(displayName == null ? "" :displayNameNow))).append("\",");

			buffer.append("\"documentType\":").append(groupBean.getDocumentTypeValue()).append(",");
			buffer.append("\"iconId\":").append(groupBean.getGroupIcon()).append(",");
			buffer.append("\"icon\":\"").append(JS.quote(HTML.escape(icons.get(i) == null ? "" : icons.get(i).getFileName()))).append("\",");
			buffer.append("\"iconName\":\"").append(JS.quote(HTML.escape(icons.get(i) == null ? "" : icons.get(i).getName()))).append("\",");


			buffer.append("\"id\":").append(groupBean.getId()).append(",");
			buffer.append("\"isVirtualGroup\":").append(virtualGroup.get(i)).append(",");
			buffer.append("\"name\":\"").append(JS.quote(HTML.escape(groupBean.getName() == null ? "" :groupBean.getName()))).append("\",");
			buffer.append("\"order\":").append((int)groupBean.getOrder()).append(",");

			buffer.append("\"paiban\":\"").append(JS.quote(HTML.escape(groupBean.getExtendField1() ==null ? "" :groupBean.getExtendField1()))).append("\",");

			buffer.append("\"singleFileSize\":").append(resourceService.getResourceSingleFileSize(groupBean.getId())).append(",");
			buffer.append("\"availableCapacity\":").append(groupBean.getAvailableCapacity()).append(",");
			buffer.append("\"totalSize\":").append(groupBean.getTotalFileSize()).append(",");
			buffer.append("\"creatorName\":\"").append(groupBean.getCreatorName()).append("\",");
			buffer.append("\"creationDate\":\"").append(groupBean.getCreateDate().toString()
					.substring(0,groupBean.getCreateDate().toString().indexOf("."))).append("\",");
			buffer.append("\"topCategoryId\":").append(
					groupBean.getTopCategoryId()==null ? null:groupBean.getTopCategoryId())
					.append(",");

			IPermission.GroupPerm[] gps = map.get(groupBean.getId());
			buffer.append("\"upload\":").append(hasPerm(gps,IPermission.GroupPerm.UPLOAD_RESOURCE)).append(",");
			buffer.append("\"delete\":").append(hasPerm(gps,IPermission.GroupPerm.DELETE_RESOURCE)).append(",");
			buffer.append("\"addDir\":").append(hasPerm(gps, IPermission.GroupPerm.ADD_FOLDER)).append(",");
			buffer.append("\"modify\":").append(hasPerm(gps,IPermission.GroupPerm.MODIFY_RESOURCE)).append("");
			buffer.append("},");
		}
		if (list.size() > 0)
			buffer.setLength(buffer.length() - 1);
		buffer.append("]}");
		return buffer.toString();
	}
	/**
	 * 修改柜子
	 * @param groupId 柜子id
	 * @param name 名字
	 * @param desc 描述
	 * @param iconId 柜子图标id，默认为0
	 * @param paiban 默认为Landscape
	 * @param displayName 显示的名字
	 * @param singleFileSize 单文件限制，为0则不修改
	 * @param totalSize 总大小，为0则不修改
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/modifyGroup", produces = "application/json; charset=UTF-8")
	public String modifyGroup(Long groupId, String name, String desc, Long iconId,String paiban, String displayName,Long singleFileSize, Long totalSize) {
		if (groupId == null || groupId == 0) {
			throw new GroupsException("柜子id不能为空！");
		}
		if (name == null || name == "") {
			throw new GroupsException("柜子name不能为空！");
		}
		if (displayName == null || displayName == "") { 
			throw new GroupsException("柜子displayName不能为空！");
		}

		iconId = iconId == null ? 0L : iconId.longValue();
		paiban = paiban == null ? "Landscape" : paiban;
		
		// 修改柜子资源空间
		if (singleFileSize != null && singleFileSize > 0) {
			groupService.modifySingleFileSize(groupId, singleFileSize);
		}
		if (totalSize != null && totalSize > 0) {
			groupService.modifyTotalFileSize(groupId, totalSize);
		}
		// 修改柜子名字和描述
		groupService.modifyGroup(groupId, name, desc, iconId, displayName,true,paiban);

		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	@RequiresUser
	@Transactional
	@ResponseBody
	@RequestMapping(value="/modifyGroup_v2", produces = "application/json; charset=UTF-8")
	public String modifyGroup_v2(Long groupId, String name, String desc, Long iconId,String paiban, String displayName,
								 Long singleFileSize, Long totalSize,String creatorName,Long topCategoryId) {
		if (groupId == null || groupId == 0) {
			throw new GroupsException("柜子id不能为空！");
		}
		if (name == null || name == "") {
			throw new GroupsException("柜子name不能为空！");
		}
		if (displayName == null || displayName == "") {
			throw new GroupsException("柜子displayName不能为空！");
		}
		long resourcesSize=0L;
		resourcesSize = resourceService.getResourcesSize(groupId);
		if(totalSize<resourcesSize){
			throw new GroupsException("柜子的总容量不能小于已使用容量");
		}
//		if(availableCapacity==null){

//			if(availableCapacity<0){
//				throw new GroupsException("柜子可用容量不能小于0！");
//			}
//
//			if(availableCapacity>totalSize){
//				throw new GroupsException("新设置的柜子可用容量不能大于柜子的总容量！");
//			}
//			if(groupbean.getAvailableCapacity()==null){
//				throw new GroupsException("请先调用调用getResourceSize进行可用容量的设置");
//			}
//		}

        if(creatorName==null || creatorName ==""){
			throw new GroupsException("柜子creatorName不能为空！");
		}
		iconId = iconId == null ? 0L : iconId.longValue();
		paiban = paiban == null ? "Landscape" : paiban;

		// 修改柜子资源空间
		if (singleFileSize != null && singleFileSize > 0) {
			groupService.modifySingleFileSize(groupId, singleFileSize);
		}
//		Long  size=groupService.getGroupById(groupId).getTotalFileSize();
//		if (totalSize != null && totalSize > 0) {
////			groupService.modifyTotalFileSize(groupId, totalSize);
////		}
		// 修改柜子名字和描述
		groupService.modifyGroup_v2(groupId, name, desc, iconId, displayName,true,paiban,totalSize,resourcesSize,creatorName,topCategoryId);

		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 删除柜子，与柜子相关的数据将一并删除
	 * @param id
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/deleteGroup", produces = "application/json; charset=UTF-8")
	public String deleteGroup(Long[] id) {
		if (id != null && id.length > 0) {
			for (long _id : id) {
				groupService.deleteGroup_v2(_id);
			}
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/deleteGroup_v2", produces = "application/json; charset=UTF-8")
	public String deleteGroup_v2(Long[] id) {
		if (id != null && id.length > 0) {
			for (long _id : id) {
				groupService.deleteGroup_v3(_id);
			}
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
	/**
	 * 复制柜子权限。<br>
	 * 复制时将做两件事：<br>
	 * 1、将用户绑定到指定的柜子中<br>
	 * 2、将用户的总体权限，添加到新的柜子权限中
	 * @param groupId 权限来源柜子id
	 * @param id 待复制柜子id
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/copyPerm", produces = "application/json; charset=UTF-8")
	public String copyPerm(Long groupId, Long[] id){
		if (groupId == null || groupId == 0 || id == null) {
			throw new GroupsException("待复制和权限来源柜子id不能为空！");
		}
		int start = 0;
		int limit = 100;

		while (true) {
			List<Member> beans = groupService.getMembersInGroup(groupId, start, limit);
			if (beans == null || beans.size() == 0)
				break;

			for (long _id : id) {
				for (Member bean : beans) {
					// 把用户绑定到柜子
					groupService.bindMemberToGroup(bean.getId(),_id);

					// 复制用户的权限到新的柜子中
					PermCollection pc = permService.getPermission(groupId, bean.getId(),OldPerm.PERM_TYPE_GROUP);
					permService.modifyPermission(_id,bean.getId(), OldPerm.PERM_TYPE_GROUP, pc);
				}
			}
			start += limit;
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
	/**
	 * 创建柜子图标
	 * 不必再实现
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/createGroupIcon", produces = "application/json; charset=UTF-8")
	@Deprecated
	public String createGroupIcon() throws Exception {
		throw new GroupsException("此功能不再实现！");
	}
	
	/**
	 * 通过memberId 查询该member最近分享资源给了谁，构造出最近联系人
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/getRecentContacts", produces = "application/json; charset=UTF-8")
	public String getRecentContacts(Long memberId, Integer limit) throws Exception{
		limit = limit == null ? 10 : limit;
		if(memberId == null || memberId == 0){
			memberId = UserUtils.getCurrentMemberId();
		}
		Member[] memberOrTeam = groupService.getRecentContacts(memberId,limit);

		List<User> userBases = new ArrayList<User>();
		List<Member>	teams = new ArrayList<Member>();
		int count =0;
		for(Member mOt:memberOrTeam){
			if(count>=limit){
				break;
			}
			if("person".equals(mOt.getMemberType())){
				User user = userService.getUserByAccount(mOt.getAccount());
				userBases.add(user);
				count ++;
			}
			if("team".equals(mOt.getMemberType())){
				teams.add(mOt);
				count++;
			}
			
		}
		
		Map<String, List<Member>> map = new HashMap<String,List<Member>>();//存放每个Account对应那几个member（马甲），默认是同名一个
		//IMemberManager mm = ServiceManager.getService().getMemberManager();
		for(User user:userBases){
			String account = user.getAccount();
			List<Member> temp = grouperService.getMembersByAccount(account);
			map.put(account, temp);
		}
		long totalCount = userBases.size()+teams.size();
		StringBuffer sb = new StringBuffer();
		sb.append("{\"totalCount\":").append(totalCount).append(",\"accounts\":[");
		if(userBases!=null){
			for (User bean : userBases) {
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
				sb.append("\"picUrl\":\"").append(
						JS.quote(HTML.escape(url))).append("\",");
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
				if (memberBeans.size() > 0)
					sb.setLength(sb.length() - 1);
				sb.append("]");
				sb.append("},");
			}
			if (userBases.size() > 0)
				sb.setLength(sb.length() - 1);
		}
		
		sb.append("],\"teams\":[");
		if(teams!=null){
			for(Member team:teams){
				sb.append("{\"id\":").append(team.getId()).append(",");
				sb.append("\"signature\":\"").append(team.getSignature()).append("\"},");
			}
			if (teams.size() > 0)
				sb.setLength(sb.length() - 1);
		}
		
		sb.append("]}");
		
		return sb.toString();
	}
	
	
	/**
	 * 获取柜子信息
	 *
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/getGroupInfo", produces = "application/json; charset=UTF-8")
	public String getGroupInfo(Long groupId) throws Exception {

		// 获取圈子信息
		Group groupBean = groupService.getGroupById(groupId);

		// 获取圈子装饰信息
		GroupDecoration decorationBean = groupService.getGroupDecoration(groupId);

		// 获取圈子管理员
		Member[] memberBeans = groupService.getGroupManagers(groupId);

		// 获取圈子分类
		Category subCategoryBean = groupBean.getCategory();
		Category categoryBean = null;
		if (subCategoryBean.getParentId() != 0) {
			categoryBean = categoryService.getCategoryById(subCategoryBean.getParentId());
		}
		// 如果圈子是属于一级分类而不是二级分类的情况
		if (categoryBean == null) {
			categoryBean = subCategoryBean;
			subCategoryBean = null;
		}

		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		buffer.append("\"groupId\":").append(groupBean.getId()).append(",");
		buffer.append("\"groupName\":\"").append(groupBean.getName()).append(
				"\",");
		buffer.append("\"groupAddr\":\"").append(groupBean.getAddr()).append(
				"\",");
		String desc = JS.quote(HTML.escape(groupBean.getDesc()));
		buffer.append("\"groupDesc\":\"").append(desc == null ? "" : desc)
				.append("\",");
		buffer.append("\"creatorName\":\"").append(groupBean.getCreatorName())
				.append("\",");
		String creationDate = groupBean.getCreateDate().toString();
		buffer.append("\"creationDate\":\"").append(
				creationDate.substring(0, 10)).append("\",");
		buffer.append("\"status\":").append(groupBean.getGroupStatus())
				.append(",");
		buffer.append("\"usage\":\"").append(groupBean.getGroupUsage())
				.append("\",");
		buffer.append("\"memberCount\":").append(groupBean.getMemberCount())
				.append(",");
		if (decorationBean != null) {
			buffer.append("\"logoIcon\":\"").append(decorationBean.getLogoIcon()).append("\",");
			buffer.append("\"groupIcon\":\"").append(decorationBean.getGroupIcon()).append("\",");
			String inform = JS.quote(HTML.escape(decorationBean.getInform()));
			buffer.append("\"inform\":\"").append(inform == null ? "" : inform).append("\",");
		} else {
			buffer.append("\"logoIcon\":\"").append("").append("\",");
			buffer.append("\"groupIcon\":\"").append("").append("\",");
			buffer.append("\"inform\":\"").append("").append("\",");
		}

		buffer.append("\"managers\":[");
		for (Member memberBean : memberBeans) {
			buffer.append("{");
			buffer.append("\"memberId\":").append(memberBean.getId()).append(",");
			buffer.append("\"memberName\":\"").append(memberBean.getName()).append("\",");
			if (memberBean.getIcon() != null&& memberBean.getIcon().length() != 0) {
				buffer.append("\"memberIcon\":\"").append(PropertyUtil.getIconPrefix()).append(
						memberBean.getIcon()).append("\"");
			} else {
				buffer.append("\"memberIcon\":\"").append(PropertyUtil.getDefaultIcon()).append("\"");
			}
			buffer.append("},");
		}
		if (memberBeans.length > 0)
			buffer.setLength(buffer.length() - 1);
		buffer.append("],");
		buffer.append("\"category\":{\"id\":").append(categoryBean.getId());
		buffer.append(",\"name\":\"").append(categoryBean.getName())
				.append("\"}");
		if (subCategoryBean != null) {
			buffer.append(",\"subCategory\":{\"id\":").append(subCategoryBean.getId());
			buffer.append(",\"name\":\"").append(subCategoryBean.getName()).append("\"}");
		}

		buffer.append("}");
		return buffer.toString();
	}
	
	/**
	 * 修改柜子优先级
	 *
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/modifyGroupOrder", produces = "application/json; charset=UTF-8")
	public String modifyGroupOrder(Long[] id, Double[] orders) throws Exception {
		int ii = 0;
		for (long _id : id) {
			groupService.modifyGroup(_id, orders[ii++]);
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
}
