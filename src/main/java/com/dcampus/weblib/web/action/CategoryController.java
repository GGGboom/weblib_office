package com.dcampus.weblib.web.action;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dcampus.common.generic.GenericDao;
import com.dcampus.common.util.HTML;
import com.dcampus.sys.util.UserUtils;
import com.dcampus.weblib.dao.CategoryDao;
import com.dcampus.weblib.entity.*;
import com.dcampus.weblib.service.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dcampus.common.config.PropertyUtil;
import com.dcampus.common.util.JS;
import com.dcampus.sys.service.UserService;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.service.permission.IPermission.CategoryPerm;
import com.dcampus.weblib.service.permission.PermCollection;
import com.dcampus.weblib.service.permission.impl.PermUtil;
import com.dcampus.weblib.util.CurrentUserWrap;
import com.dcampus.weblib.util.Filter;
import com.dcampus.weblib.service.permission.IPermission;

import javax.persistence.criteria.CriteriaBuilder;

@Controller
@RequestMapping(value = "/category")
public class CategoryController {
	
	@Autowired
	private UserService userService;
	@Autowired
	private ResourceService resourceService;
	@Autowired
	private PermissionService permissionService;
	@Autowired
	private GroupService groupService;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private DomainService domainService;

	@Autowired
	private DomainCategoryService domainCategoryService;


	@Autowired
	private CategoryDao categoryDao;


	/**
	 * 核算分类的虚拟可用空间
	 * @param categoryId
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@Transactional
	@RequestMapping(value = "/calculateAvailableCapacity", produces = "application/json; charset=UTF-8")
	public String calculateAvailableCapacity(Long categoryId) {
            Category category=categoryService.getCategoryById(categoryId);
            if(category.getTotalCapacity()==null||category.getTotalCapacity()<=0){
                  throw new  GroupsException("该分类没有容量，无法计算");
			}
		    StringBuffer sb = new StringBuffer();
		    sb.append("{");
            if(category.getTotalCapacity()>0){
            	Long totalCapacity =0L;
				totalCapacity =groupService.getAllGroupSpaceSize(categoryId);
				Long availableCapacity=0L;
				availableCapacity=category.getTotalCapacity()-totalCapacity;
				category.setAvailableCapacity(availableCapacity);
				categoryDao.saveOrUpdateCategory(category);
				sb.append("\"categoryId\":").append(categoryId).append(",");
				sb.append("\"totalCapacity\":").append(totalCapacity).append(",");
				sb.append("\"availableCapacity\":").append(availableCapacity);
			}
		   sb.append("}");
//		   List<Group> groups=genericDao.findAll(" from Group g where g.topCategoryId = ? ", categoryId);
//           for(Group group:groups){
//			   System.out.println("柜子id为 ："+group.getId()+"   柜名为："+group.getName()+"   柜子总容量为："+group.getTotalFileSize()+"   柜子的topcategoryId为："+group.getTopCategoryId());
//		   }
//           Long result=genericDao.findFirst("select sum(g.totalFileSize) from Group g where g.topCategoryId = ? ", categoryId);
//		   System.out.println("得到的柜子的总容量为"+result);
		return sb.toString();

	}

	
	/**
	 * 
	 * @param parentId
	 * @param type
	 * @param recursion
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getCategoriesWithManagedGroup", produces = "application/json; charset=UTF-8")
	public String getCategoriesWithManagedGroup(Long parentId, String type, Boolean recursion
			, @RequestParam(required = false,defaultValue = "0")Integer start, @RequestParam(required = false,defaultValue = "40")Integer limit) throws Exception {
		start = start == null ? 0 : start;
		limit = limit == null ? Integer.MAX_VALUE : limit;

		if (parentId == null) {
			throw new GroupsException("parentId不能为空！");
		}
		if (type == null) {
			type ="all";
		}
		if (recursion == null) {
			recursion = false;
		}
		Long memberId = null;
		List<Category> beans = null;
		Category virtualPersonalCategory= null;
		long categoriesCount= 0;
		List<OldPerm> permList = null;
		Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
		memberId = currentUserWrap.getMemberId();

		// 判断当前操作者是否是域管理
		boolean isDomainAdmin = false, isRootDomainAdmin = false;
		isDomainAdmin = domainService.isDomainAdmin(memberId);
		isRootDomainAdmin = domainService.isRootDomainAdmin(memberId);
		beans = null;
		virtualPersonalCategory = null;

		categoriesCount = 0L;
		categoriesCount=categoryService.getCategoriesTotalCount(parentId);
		if("category".equalsIgnoreCase(type)||"all".equalsIgnoreCase(type)||"group".equalsIgnoreCase(type)) {
			if (parentId == 0 && PropertyUtil.getEnableDomainModule() && !isRootDomainAdmin && isDomainAdmin) {
				// 获取存储管理的存储根目录下的分类
				// 如果查询者是域管理员，则返回域的存储根目录
				Category[] beanss = domainService.getManageDomainRootCategory(memberId);
				beans = Arrays.asList(beanss);
			} else {
				beans = categoryService.getCategorieByParent(parentId, start, limit);
				//判断parentId是否是#domain分类的直接节点，如果是，则需要虚拟一个域内的个人资源库
				Domain domain = domainService.isDomainRootCategory(parentId);
				if (domain != null) {
					virtualPersonalCategory = new Category();
					virtualPersonalCategory.setId(domain.getId() * -1);
					virtualPersonalCategory.setDisplayName("个人资源库");
					virtualPersonalCategory.setParentId(parentId);
					virtualPersonalCategory.setDesc("虚拟个人资源库");
					virtualPersonalCategory.setCreateDate(new Timestamp(System.currentTimeMillis()));
					virtualPersonalCategory.setCategoryStatus(Category.STATUS_NORMAL);
					virtualPersonalCategory.setOrder(0D);
				}
			}
		}
		permList = permissionService.getMemberPerms(memberId, OldPerm.PERM_TYPE_CATEGORY);
		Map<Long, Category> catMap = new HashMap<Long, Category>();
		Map<Long, Long> permMap = new HashMap<Long, Long>();
		List<PermCollection> pcList = null;
		Category[] categoryBeans = new Category[0];
		boolean isVirtualGroup = false;
		Long domainId = null;
		Group[] groupBeans = new Group[0];
		long groupsCount = 0;
//问题在这里


		for (OldPerm p : permList) {
			permMap.put(p.getTypeId(), p.getPermCode());

			List<Category> list = null;
            try {
                list = categoryService.tracedCategoryList(p.getTypeId());
            } catch (Exception e1) {
                return "{\"success\":false,\"detail\":\"该用户所管理的分类中有不存在的分类，id为"+p.getTypeId()+"\"}";
            }
			for (Category b : list) {
				catMap.put(b.getId(), b);
			}
		}

		//问题在这里
		List<Category> categoryBeanList = null;

		categoryBeanList = new ArrayList<Category>();
		pcList = new ArrayList<PermCollection>();
		for (Category bean : beans) {

			// ====过滤应用======
			if (bean.getName().equals("#1application1")) {
				continue;
			}
			// ===============

			PermCollection pc = permissionService.getPermission(bean.getId(), memberId, OldPerm.PERM_TYPE_CATEGORY);

			if (hasPerssmison(pc.getCategoryPerm())) {
				pcList.add(pc);
				categoryBeanList.add(bean);
			} else if(catMap.containsKey(bean.getId())) {
				pcList.add(pc);
				categoryBeanList.add(bean);
			}
		}


		categoryBeans = (Category[])categoryBeanList.toArray(new Category[0]);
		isVirtualGroup = false;
		domainId = null;
		groupBeans = null;

		groupsCount = 0L;
		groupsCount = groupService.getGroupsInCategoryTotalCount(parentId);
		if ("all".equalsIgnoreCase(type)||"group".equalsIgnoreCase(type)) {
			List<Group> list = new ArrayList<Group>();
			if (parentId > 0 ) {
				PermCollection pc = permissionService.getPermission(parentId, memberId, OldPerm.PERM_TYPE_CATEGORY);
				if (hasPerssmison(pc.getCategoryPerm())) {
//					IResult<Group> result = groupService.getGroupsInCategory(parentId, 0, Integer.MAX_VALUE,
//									recursion);
//					for (Group bean : result.getBeans()) {
//						list.add(bean);
//					}
					List<Group> result = groupService.getGroupsInCategory(parentId,start,limit);
					for (Group bean : result) {
						list.add(bean);
					}
				}
			} else if(parentId < 0){
				//查询域存储根下的虚拟个人资源库 -parentId为对应的域的domainId
				list = domainService.getAllPersonalGroupByDomain(-1*parentId);
				isVirtualGroup = true;
				domainId = -1*parentId;
			}
			groupBeans = list.toArray(new Group[list.size()]);
		} else {
			groupBeans = new Group[0];
		}

		// 获取节点是否有子节点
		boolean[] leaf = new boolean[categoryBeans.length];
		for (int i = 0; i < leaf.length; ++i) {
			List<Category> childs = categoryService.getCategorieByParent(categoryBeans[i].getId(),start,limit);
			PermCollection pc = pcList.get(i);
			long amount = 0;
			if (hasPerssmison(pc.getCategoryPerm())) {
				List<Group> result = groupService.getGroupsInCategory(categoryBeans[i].getId(),start,limit);
				amount = result == null ? 0L :result.size();
			}
			int ii = 0;
			for (Category bean : childs) {
				if (!permMap.containsKey(bean.getId())) {
					ii++;
					break;
				}
				long permCode = permMap.get(bean.getId());
				CategoryPerm[] perms = PermUtil.convertCategoryPerm(permCode);
				if (hasPerssmison(perms)) {
					ii++;
					break;
				}
				if(catMap.containsKey(bean.getId())) {
					ii++;
					break;
				}
			}
			leaf[i] = (ii == 0 && amount == 0);
		}
		long[] singleFileSize = new long[groupBeans.length];
		long[] totalSize = new long[groupBeans.length];
		long[] groupIds = new long[groupBeans.length];
		for (int i = 0; i < groupBeans.length; ++i) {
//			singleFileSize[i] = resourceService.getResourceSingleFileSize(groupBeans[i].getId());
//			totalSize[i] = resourceService.getResourceSpaceSize(groupBeans[i].getId());
			groupIds[i] = groupBeans[i].getId();
		}

		totalSize = resourceService.getResourceSpaceSize(groupIds);
		singleFileSize = resourceService.getResourceSingleFileSize(groupIds);
		List<GroupIcon> iconBeans = groupService.getAllGroupIcons();
		Map<Long, GroupIcon> iconMap = new HashMap<Long, GroupIcon>();
		for (GroupIcon icon : iconBeans) {
			iconMap.put(icon.getId(), icon);
		}

		StringBuffer sb = new StringBuffer();
		sb.append("{\"categories\":[");
		if("category".equalsIgnoreCase(type)||"all".equalsIgnoreCase(type)) {
			if (virtualPersonalCategory != null) {
				//虚拟个人资源库
				Category bean = virtualPersonalCategory;
				//PermCollection pc = pcs[i];
				boolean le = false;
				sb.append("{");
				sb.append("\"id\":").append(bean.getId()).append(",");
				sb.append("\"name\":\"").append(bean.getName()).append("\",");
				if (bean.getDisplayName() == null
						|| bean.getDisplayName().equals("")) {
					sb.append("\"displayName\":\"").append(bean.getName()).append("\",");
				} else {
					sb.append("\"displayName\":\"").append(JS.quote(Filter.convertHtmlBody(bean.getDisplayName()))).append("\",");
				}
				sb.append("\"addcategory\":").append(false).append(",");
				sb.append("\"managecategory\":").append(false).append(",");
				sb.append("\"addgroup\":").append(false).append(",");
				sb.append("\"desc\":\"").append(JS.quote(Filter.convertHtmlBody(bean.getDesc()))).append("\",");
				sb.append("\"parentId\":").append(bean.getParentId()).append(",");
				sb.append("\"order\":").append((int) bean.getOrder()).append(",");
				String creationDate = bean.getCreateDate().toString();
				sb.append("\"creationDate\":\"").append(creationDate.substring(0, creationDate.length() - 2)).append("\",");
				sb.append("\"status\":").append(bean.getCategoryStatus()).append(",");
				sb.append("\"leaf\":").append(le);
				sb.append("},");
			}

			for (int i = 0; i < leaf.length; ++i) {
				Category bean = categoryBeans[i];
				PermCollection pc = pcList.get(i);
				boolean le = leaf[i];

				sb.append("{");
				sb.append("\"id\":").append(bean.getId()).append(",");
				sb.append("\"name\":\"").append(bean.getName()).append("\",");
				if (bean.getDisplayName() == null
						|| bean.getDisplayName().equals("")) {

					sb.append("\"displayName\":\"").append(bean.getName())
							.append("\",");
				} else {
					sb.append("\"displayName\":\"").append(
							JS.quote(Filter.convertHtmlBody(bean
									.getDisplayName()))).append("\",");
				}
				sb.append("\"addcategory\":").append(PermUtil.containCategoryPermission(pc.getCategoryPerm(), IPermission.CategoryPerm.CREATE_CATEGORY)).append(
						",");
				sb.append("\"managecategory\":").append(PermUtil.containCategoryPermission(pc.getCategoryPerm(), IPermission.CategoryPerm.MANAGE_CATEGORY)).append(
						",");
				sb.append("\"addgroup\":").append(PermUtil.containCategoryPermission(pc.getCategoryPerm(), IPermission.CategoryPerm.CREATE_GROUP)).append(
						",");

				sb.append("\"desc\":\"").append(
						JS.quote(Filter.convertHtmlBody(bean.getDesc())))
						.append("\",");
				sb.append("\"parentId\":").append(bean.getParentId()).append(
						",");
				sb.append("\"order\":").append((int) bean.getOrder()).append(
						",");
				String creationDate = bean.getCreateDate().toString();
				sb.append("\"creationDate\":\"").append(
						creationDate.substring(0, creationDate.length() - 2))
						.append("\",");
				sb.append("\"status\":").append(bean.getCategoryStatus())
						.append(",");
				sb.append("\"leaf\":").append(le);
				sb.append("},");
			}
			if (categoryBeans.length > 0)
				sb.setLength(sb.length() - 1);
		}
		sb.append("],");
		sb.append("\"groupsCount\":").append(groupsCount).append(",");
		sb.append("\"categoriesCount\":").append(categoriesCount).append(",");
		sb.append("\"groups\":[");
		for (int i = 0; i < groupBeans.length; ++i) {
			Group bean = groupBeans[i];
			long sf = singleFileSize[i];
			long tf = totalSize[i];
			sb.append("{");
			sb.append("\"id\":").append(bean.getId()).append(",");
			sb.append("\"categoryId\":").append(bean.getCategory().getId())
					.append(",");
			sb.append("\"name\":\"").append(bean.getName()).append("\",");
			sb.append("\"displayName\":\"").append(
					JS
							.quote(Filter.convertHtmlBody(bean
									.getDisplayName() == null ? "" : bean
									.getDisplayName()))).append("\",");
			if (iconMap.containsKey(bean.getGroupIcon())) {
				GroupIcon icon = iconMap.get(bean.getGroupIcon());
				sb.append("\"icon\":\"").append(PropertyUtil.getGroupIconFolderPath()+icon.getFileName()).append("\",");
				sb.append("\"iconName\":\"").append(
						JS.quote(Filter.convertHtmlBody(icon.getName() == null ? "" : icon.getName()))).append("\",");
				sb.append("\"iconId\":").append(bean.getGroupIcon()).append(",");
			} else {
				sb.append("\"icon\":\"").append(PropertyUtil.getGroupDefaultIcon()).append("\",");
				sb.append("\"iconName\":\"").append("").append("\",");
				sb.append("\"iconId\":").append(0).append(",");
			}
			sb.append("\"order\":").append((int)bean.getOrder()).append(
					",");
			sb.append("\"desc\":\"").append(
					JS.quote(Filter.convertHtmlBody(bean.getDesc())))
					.append("\",");
			sb.append("\"singleFileSize\":").append(sf).append(",");
			sb.append("\"paiban\":\"").append(bean.getExtendField1()==null?"":bean.getExtendField1()).append("\",");
			sb.append("\"documentType\":").append(
					bean.getDocumentTypeValue()).append(",");

			sb.append("\"totalSize\":").append(tf);
			sb.append(",\"isVirtualGroup\":").append(isVirtualGroup);
			if(isVirtualGroup){
				sb.append(",\"domainId\":").append(domainId);
				sb.append(",\"memberId\":").append(bean.getName());//个人柜子的name 为对应memberId
			}
			sb.append("},");
		}
		if (groupBeans.length > 0)
			sb.setLength(sb.length() - 1);

		sb.append("]}");
		return sb.toString();
	}

	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getCategoriesWithManagedGroup_v2", produces = "application/json; charset=UTF-8")
	public String getCategoriesWithManagedGroup_v2(Long parentId,
			              @RequestParam(required = false,defaultValue = "0")Integer start,
							@RequestParam(required = false,defaultValue = "40")Integer limit) throws Exception {
		start = start == null ? 0 : start;
		limit = limit == null ? Integer.MAX_VALUE : limit;

		if (parentId == null) {
			throw new GroupsException("parentId不能为空！");
		}

		Long memberId = null;
		List<Category> beans = null;
		List<Group> result=null;
		Category virtualPersonalCategory= null;
		//获取category的总数
		long categoriesCount= 0;
		long totalCount=0;
		long groupsCount = 0;
		List<OldPerm> permList = null;
		Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
		memberId = currentUserWrap.getMemberId();

		// 判断当前操作者是否是域管理
		boolean isDomainAdmin = false, isRootDomainAdmin = false;
		isDomainAdmin = domainService.isDomainAdmin(memberId);
		isRootDomainAdmin = domainService.isRootDomainAdmin(memberId);
		beans = null;
		virtualPersonalCategory = null;

		categoriesCount=categoryService.getCategoriesTotalCount(parentId);
		groupsCount = groupService.getGroupsInCategoryTotalCount(parentId);
		totalCount=categoriesCount+groupsCount;
		if(start>=totalCount){
			StringBuffer stringBuffer=new StringBuffer();
			stringBuffer.append("{\"totalCount\":").append(totalCount).append(",");
			stringBuffer.append("\"categoriesCount\":").append(categoriesCount).append(",");
			stringBuffer.append("\"groupsCount\":").append(groupsCount).append(",");
			stringBuffer.append("\"categories\":[").append("],");
			stringBuffer.append("\"groups\":[").append("]}");
			return stringBuffer.toString();
		}
		if (parentId == 0 && PropertyUtil.getEnableDomainModule() && !isRootDomainAdmin && isDomainAdmin) {
			// 获取存储管理的存储根目录下的分类
			// 如果查询者是域管理员，则返回域的存储根目录
			Category[] beanss = domainService.getManageDomainRootCategory(memberId);
			beans = Arrays.asList(beanss);
		} else {
			//获取需要的category内容以及需要的group内容
			if (start < totalCount) {
				if (start <= categoriesCount - 1) {
					if (start + limit - 1 <= categoriesCount - 1) {
						beans = categoryService.getCategorieByParent(parentId, start, limit);
					}
					if (start + limit - 1 > categoriesCount - 1) {
						beans = categoryService.getCategorieByParent(parentId, start, (int) categoriesCount - start);
						if (parentId > 0) {
							if (limit - categoriesCount + start >= groupsCount) {
								result = groupService.getGroupsInCategory(parentId, 0, (int) groupsCount);
							} else {
								result = groupService.getGroupsInCategory(parentId, 0, (int) (limit - categoriesCount + start));
							}
						}
					}
				} else if (start > categoriesCount - 1) {
					if (parentId > 0) {
						if (start - categoriesCount + limit - 1 >= groupsCount) {
							result = groupService.getGroupsInCategory(parentId, (int) (start - categoriesCount), (int) (totalCount - start));
						}
						if (start - categoriesCount + limit - 1 < groupsCount) {
							result = groupService.getGroupsInCategory(parentId, (int) (start - categoriesCount), limit);
						}
					}
				}
			}
			//判断parentId是否是#domain分类的直接节点，如果是，则需要虚拟一个域内的个人资源库
			Domain domain = domainService.isDomainRootCategory(parentId);
			if (domain != null) {
				virtualPersonalCategory = new Category();
				virtualPersonalCategory.setId(domain.getId() * -1);
				virtualPersonalCategory.setDisplayName("个人资源库");
				virtualPersonalCategory.setParentId(parentId);
				virtualPersonalCategory.setDesc("虚拟个人资源库");
				virtualPersonalCategory.setCreateDate(new Timestamp(System.currentTimeMillis()));
				virtualPersonalCategory.setCategoryStatus(Category.STATUS_NORMAL);
				virtualPersonalCategory.setOrder(0D);
			}
		}

		permList = permissionService.getMemberPerms(memberId, OldPerm.PERM_TYPE_CATEGORY);
		Map<Long, Category> catMap = new HashMap<Long, Category>();
		Map<Long, Long> permMap = new HashMap<Long, Long>();
		List<PermCollection> pcList =new ArrayList<PermCollection>();
		boolean isVirtualGroup = false;
		Long domainId = null;


//问题在这里

		List<Category> categoryBeanList = new ArrayList<Category>();
		Category[] categoryBeans = new Category[0];
		if (start <= categoriesCount - 1) {
//			List<OldPerm> permLists = permissionService.getMemberPerms(memberId, OldPerm.PERM_TYPE_CATEGORY);
//			Map<Long, Category> catMap = new HashMap<Long, Category>();
//			Map<Long, Long> permMap = new HashMap<Long, Long>();
			for (OldPerm p : permList) {
				permMap.put(p.getTypeId(), p.getPermCode());
				List<Category> list = categoryService.tracedCategoryList(p.getTypeId());
				if (list != null) {
					for (Category b : list) {
						catMap.put(b.getId(), b);
					}
				}
			}

			for (Category bean : beans) {
				if (bean.getName().equals("#1application1")) {
					continue;
				}
				// ===============
				//查看用户对这个分类是否有权限
				PermCollection pc = permissionService.getPermission(bean.getId(), memberId, OldPerm.PERM_TYPE_CATEGORY);
				if (pc != null) {
					if (hasPerssmison(pc.getCategoryPerm())) {
						pcList.add(pc);
						categoryBeanList.add(bean);
					} else if(catMap.containsKey(bean.getId())) {
						pcList.add(pc);
						categoryBeanList.add(bean);
					}
				}

//				categoryBeanList.add(bean);
			}
			categoryBeans = (Category[]) categoryBeanList.toArray(new Category[0]);
		}

		//问题在这里
		//获取到的需要的数据

        //获取到的category来输出
		categoryBeans = (Category[])categoryBeanList.toArray(new Category[0]);
		isVirtualGroup = false;
		domainId = null;

		//获取group的内容
		List<Group> list = new ArrayList<Group>();
		Group[] groupBeans = new Group[0];
		if (parentId > 0 ) {
			PermCollection pc = permissionService.getPermission(parentId, memberId, OldPerm.PERM_TYPE_CATEGORY);
			if (hasPerssmison(pc.getCategoryPerm())) {
//					IResult<Group> result = groupService.getGroupsInCategory(parentId, 0, Integer.MAX_VALUE,
//									recursion);
//					for (Group bean : result.getBeans()) {
//						list.add(bean);
//					}
				if (result != null && result.size() > 0) {
					for (Group bean : result) {
						list.add(bean);
					}
				}

			}
		} else if(parentId < 0){
			//查询域存储根下的虚拟个人资源库 -parentId为对应的域的domainId
			list = domainService.getAllPersonalGroupByDomain(-1*parentId);
			isVirtualGroup = true;
			domainId = -1*parentId;
		}
		//获取到的group用来输出
		groupBeans = list.toArray(new Group[list.size()]);

		// 获取节点是否有子节点
		//判断获取到的每一个category是否存在子节点
		boolean[] leaf = null;
		long[] singleFileSize = null;
		long[] totalSize = null;
		Map<Long, GroupIcon> iconMap = null;
		if (start <= categoriesCount - 1) {
			leaf = new boolean[categoryBeans.length];
			for (int i = 0; i < leaf.length; ++i) {
				List<Category> childs = categoryService.getCategorieByParent(categoryBeans[i].getId());
				leaf[i] = ((childs != null || childs.size() > 0) ? true : false);
			}
		}
		//same
		singleFileSize = new long[groupBeans.length];
		totalSize = new long[groupBeans.length];
		for (int i = 0; i < groupBeans.length; ++i) {
			singleFileSize[i] = resourceService.getResourceSingleFileSize(groupBeans[i].getId());
			totalSize[i] = resourceService.getResourceSpaceSize(
					groupBeans[i].getId());
		}
		//same
		List<GroupIcon> iconBeans = groupService.getAllGroupIcons();
		iconMap = new HashMap<Long, GroupIcon>();
		for (GroupIcon icon : iconBeans) {
			iconMap.put(icon.getId(), icon);
		}

		//输出内容
		StringBuffer sb = new StringBuffer();
        sb.append("{");
		sb.append("\"totalCount\":").append(totalCount).append(",");
		sb.append("\"groupsCount\":").append(groupsCount).append(",");
		sb.append("\"categoriesCount\":").append(categoriesCount).append(",");
        sb.append("\"categories\":[");

		if (virtualPersonalCategory != null) {
			//虚拟个人资源库
			Category bean = virtualPersonalCategory;
			//PermCollection pc = pcs[i];
			boolean le = false;
			sb.append("{");
			sb.append("\"id\":").append(bean.getId()).append(",");
			sb.append("\"name\":\"").append(bean.getName()).append("\",");
			if (bean.getDisplayName() == null
					|| bean.getDisplayName().equals("")) {
				sb.append("\"displayName\":\"").append(bean.getName()).append("\",");
			} else {
				sb.append("\"displayName\":\"").append(JS.quote(Filter.convertHtmlBody(bean.getDisplayName()))).append("\",");
			}
			sb.append("\"addcategory\":").append(false).append(",");
			sb.append("\"managecategory\":").append(false).append(",");
			sb.append("\"addgroup\":").append(false).append(",");
			sb.append("\"desc\":\"").append(JS.quote(Filter.convertHtmlBody(bean.getDesc()))).append("\",");
			sb.append("\"parentId\":").append(bean.getParentId()).append(",");
			sb.append("\"order\":").append((int) bean.getOrder()).append(",");
			String creationDate = bean.getCreateDate().toString();
			sb.append("\"creationDate\":\"").append(creationDate.substring(0, creationDate.length() - 2)).append("\",");
			sb.append("\"status\":").append(bean.getCategoryStatus()).append(",");
//			List<DomainCategory> dcss=domainCategoryService.getDcByCategory(bean.getId());
//			if(dcss.size()!=0&&dcss!=null){
//				DomainCategory dc=dcss.get(0);
//				sb.append("\"totalCapacity\":").append(dc.getTotalCapacity())
//						.append(",");
//				sb.append("\"availableCapacity\":").append(dc.getAvailableCapacity())
//						.append(",");
//			}
//			else {
//				sb.append("\"totalCapacity\":")
//						.append(",");
//				sb.append("\"availableCapacity\":")
//						.append(",");
//			}
			sb.append("\"totalCapacity\":").append(bean.getTotalCapacity())
					.append(",");
			sb.append("\"availableCapacity\":").append(bean.getAvailableCapacity())
					.append(",");
			sb.append("\"creatorName\":\"").append(
					JS.quote(HTML.escape(bean.getCreatorName()==null ? "" :bean.getCreatorName())))
					.append("\",");
			sb.append("\"leaf\":").append(le);
			sb.append("},");
		}
		if (start <= categoriesCount - 1) {
			for (int i = 0; i < leaf.length; ++i) {
				Category bean = categoryBeans[i];
				PermCollection pc = pcList.get(i);
				boolean le = leaf[i];

				sb.append("{");
				sb.append("\"id\":").append(bean.getId()).append(",");
				sb.append("\"name\":\"").append(bean.getName()).append("\",");
				if (bean.getDisplayName() == null
						|| bean.getDisplayName().equals("")) {

					sb.append("\"displayName\":\"").append(bean.getName())
							.append("\",");
				} else {
					sb.append("\"displayName\":\"").append(
							JS.quote(Filter.convertHtmlBody(bean
									.getDisplayName()))).append("\",");
				}
				sb.append("\"addcategory\":").append(PermUtil.containCategoryPermission(pc.getCategoryPerm(), IPermission.CategoryPerm.CREATE_CATEGORY)).append(
						",");
				sb.append("\"managecategory\":").append(PermUtil.containCategoryPermission(pc.getCategoryPerm(), IPermission.CategoryPerm.MANAGE_CATEGORY)).append(
						",");
				sb.append("\"addgroup\":").append(PermUtil.containCategoryPermission(pc.getCategoryPerm(), IPermission.CategoryPerm.CREATE_GROUP)).append(
						",");
				sb.append("\"desc\":\"").append(
						JS.quote(Filter.convertHtmlBody(bean.getDesc())))
						.append("\",");
				sb.append("\"parentId\":").append(bean.getParentId()).append(
						",");
				sb.append("\"order\":").append((int) bean.getOrder()).append(
						",");
				String creationDate = bean.getCreateDate().toString();
				sb.append("\"creationDate\":\"").append(
						creationDate.substring(0, creationDate.length() - 2))
						.append("\",");
				sb.append("\"status\":").append(bean.getCategoryStatus())
						.append(",");
//				List<DomainCategory> dcss=domainCategoryService.getDcByCategory(bean.getId());
//				if(dcss.size()!=0&&dcss!=null){
//					DomainCategory dc=dcss.get(0);
//					sb.append("\"totalCapacity\":").append(dc.getTotalCapacity())
//							.append(",");
//					sb.append("\"availableCapacity\":").append(dc.getAvailableCapacity())
//							.append(",");
//				}
//				else {
//					sb.append("\"totalCapacity\":")
//							.append(",");
//					sb.append("\"availableCapacity\":")
//							.append(",");
//				}
				sb.append("\"totalCapacity\":").append(bean.getTotalCapacity())
						.append(",");
				sb.append("\"availableCapacity\":").append(bean.getAvailableCapacity())
						.append(",");
				sb.append("\"creatorName\":\"").append(
						JS.quote(HTML.escape(bean.getCreatorName()==null ? "" :bean.getCreatorName())))
						.append("\",");

				sb.append("\"leaf\":").append(le);
				sb.append("},");
			}
			if (categoryBeans.length > 0)
				sb.setLength(sb.length() - 1);

		}
        sb.append("],");
		//输出group的内容
		sb.append("\"groups\":[");
		for (int i = 0; i < groupBeans.length; ++i) {
			Group bean = groupBeans[i];
			long sf = singleFileSize[i];
			long tf = totalSize[i];
			sb.append("{");
			sb.append("\"id\":").append(bean.getId()).append(",");
			sb.append("\"categoryId\":").append(bean.getCategory().getId())
					.append(",");
			sb.append("\"name\":\"").append(bean.getName()).append("\",");
			sb.append("\"displayName\":\"").append(
					JS
							.quote(Filter.convertHtmlBody(bean
									.getDisplayName() == null ? "" : bean
									.getDisplayName()))).append("\",");
			if (iconMap.containsKey(bean.getGroupIcon())) {
				GroupIcon icon = iconMap.get(bean.getGroupIcon());
				sb.append("\"icon\":\"").append(JS.quote(HTML.escape(PropertyUtil.getGroupIconFolderPath()+icon.getFileName()))).append("\",");
				sb.append("\"iconName\":\"").append(
						JS.quote(Filter.convertHtmlBody(icon.getName() == null ? "" : icon.getName()))).append("\",");
				sb.append("\"iconId\":").append(bean.getGroupIcon()).append(",");
			} else {
				sb.append("\"icon\":\"").append(JS.quote(HTML.escape(PropertyUtil.getGroupDefaultIcon()))).append("\",");
				sb.append("\"iconName\":\"").append("\",");
				sb.append("\"iconId\":").append(0).append(",");
			}
			sb.append("\"order\":").append((int)bean.getOrder()).append(
					",");
			sb.append("\"desc\":\"").append(
					JS.quote(Filter.convertHtmlBody(bean.getDesc())))
					.append("\",");
			sb.append("\"singleFileSize\":").append(sf).append(",");
			sb.append("\"paiban\":\"").append(bean.getExtendField1()==null?"":bean.getExtendField1()).append("\",");
			sb.append("\"documentType\":").append(
					bean.getDocumentTypeValue()).append(",");
			sb.append("\"availableCapacity\":").append(
					bean.getAvailableCapacity()).append(",");
			sb.append("\"totalSize\":").append(bean.getTotalFileSize()).append(",");
			sb.append("\"creatorName\":\"").append(bean.getCreatorName()).append("\",");;
//			sb.append("\"creationDate\":").append(bean.getCreateDate().toString()
//							.substring(0,bean.getCreateDate().toString().indexOf("."))).append(",");
            sb.append("\"creationDate\":\"").append(bean.getCreateDate().toString().substring(0, bean.getCreateDate().toString().length() - 2)).append("\",");
			sb.append("\"topCategoryId\":").append(
					bean.getTopCategoryId()==null ? null :bean.getTopCategoryId())
					.append(",");
            sb.append("\"isVirtualGroup\":").append(isVirtualGroup);
			if(isVirtualGroup){
				sb.append(",\"domainId\":").append(domainId);
				sb.append(",\"memberId\":").append(bean.getName());//个人柜子的name 为对应memberId
			}
			sb.append("},");
		}
		if (groupBeans.length > 0)
			sb.setLength(sb.length() - 1);
		sb.append("]}");

		return sb.toString();
	}
	private boolean hasPerssmison(CategoryPerm[] perms) {
		boolean flag = PermUtil.containCategoryPermission(perms, CategoryPerm.MANAGE_CATEGORY)
		|| PermUtil.containCategoryPermission(perms, CategoryPerm.CREATE_CATEGORY)
		|| PermUtil.containCategoryPermission(perms, CategoryPerm.CREATE_GROUP) 
		|| PermUtil.containCategoryPermission(perms, CategoryPerm.CATEGORY_APPMANAGER);
		return flag;
	}
	
	/**
	 * 创建分类
	 * 
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/createCategory_v2", produces = "application/json; charset=UTF-8")
	public String createCategory_v2(Long parentId,String displayName, String name, String desc,long totalCapacity) {
		StringBuffer sb = new StringBuffer();
		if (parentId == null) {
			parentId = 0L;
		}
		if (displayName == null || name == null) { 
			throw new GroupsException("分类名不能为空！");
		}
		Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
		String memberName = currentUserWrap.getMemberName();
		Category bean = new Category();
		bean.setCreateDate(new Timestamp(System.currentTimeMillis()));
		bean.setDesc(desc == null ? "" : desc);
		bean.setName(name);
		bean.setDisplayName(displayName);
		bean.setParentId(parentId);
		//设置三个新的字段
		bean.setTotalCapacity(totalCapacity);
		bean.setAvailableCapacity(totalCapacity);
        System.out.println(memberName);
        bean.setCreatorName(memberName);
		double num = categoryService.getMaxOrder(parentId);
		num++;
		bean.setOrder(num);
		bean.setCategoryStatus(Category.STATUS_NORMAL);
		categoryService.createCategory(bean,true);
        long idd=bean.getId();
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true,\"categoryId\":"+idd+"}";
	}

	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/createCategory", produces = "application/json; charset=UTF-8")
	public String createCategory(Long parentId,String displayName, String name, String desc) {
		StringBuffer sb = new StringBuffer();
		if (parentId == null) {
			parentId = 0L;
		}
		if (displayName == null || name == null) {
			throw new GroupsException("分类名不能为空！");
		}
		Category bean = new Category();
		bean.setCreateDate(new Timestamp(System.currentTimeMillis()));
		bean.setDesc(desc == null ? "" : desc);
		bean.setName(name);
		bean.setDisplayName(displayName);
		bean.setParentId(parentId);
		double num = categoryService.getMaxOrder(parentId);
		num++;
		bean.setOrder(num);
		bean.setCategoryStatus(Category.STATUS_NORMAL);

		categoryService.createCategory(bean,true);

		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
	/**
	 * 修改分类的名字和描述
	 * @param id 分类id
	 * @param displayName 分类名字
	 * @param desc 描述
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/modifyCategory", produces = "application/json; charset=UTF-8")
	public String modifyCategory(Long id, String displayName, String desc) {
		if (id == null || id == 0) {
			throw new GroupsException("分类id不能为空！");
		}
		if (displayName == null ) {
			throw new GroupsException("分类名不能为空！");
		}
		categoryService.modifyCategory(id, displayName, desc);
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/modifyCategory_v2", produces = "application/json; charset=UTF-8")
	public String modifyCategory_v2(Long id, String displayName, String desc,Long totalCapacity,
	                               String creatorName) {
		if (id == null || id == 0) {
			throw new GroupsException("分类id不能为空！");
		}
		if (displayName == null ) {
			throw new GroupsException("分类名不能为空！");
		}
        if(creatorName==null ||creatorName ==""){
            throw new GroupsException("分类creatorName不能为空！");
        }
		totalCapacity = totalCapacity == null ? 0 : totalCapacity;
		categoryService.modifyCategory_v2(id, displayName, desc,totalCapacity,creatorName);
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
	/**
	 * 删除分类，分类及其子孙分类和子孙柜子将被删除，相关数据也将被删除
	 * @param id
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/deleteCategory", produces = "application/json; charset=UTF-8")
	public String deleteCategory(Long[] id) {
		if (id == null || id.length <= 0) {
			throw new GroupsException("分类id不能为空！");
		}
		for (long _id : id) {
			categoryService.deleteCategory(_id);
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
	
	/**
	 * 获得某层分类最大的顺序号
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getCategoryMaxOrder", produces = "application/json; charset=UTF-8")
	public String getCategoryMaxOrder(Long parentId) throws Exception {
		double num = categoryService.getMaxOrder(parentId);
		num++;
		return "{\"order\":"+num+"}";
	}
	
	/**
	 * 批量修改分类顺序号
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/modifyCategoryOrders", produces = "application/json; charset=UTF-8")
	public String modifyCategoryOrders(Long[] id, Double[] orders) throws Exception {
		int ii = 0;
		for (long _id : id) {
			double num = orders[ii];
			categoryService.modifyCategoryOrder(_id, num);
			ii++;
		}

		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
	/**
	 * 移动分类
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/moveCategory", produces = "application/json; charset=UTF-8")
	public String moveCategory(Long[] id,Long parentId) throws Exception {
		for (long _id : id) {
			categoryService.moveCategory(_id,parentId);
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 关闭分类后只是无法再看到该分类下的柜子，但其子分类下的还能见到
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/closeCategory", produces = "application/json; charset=UTF-8")
	public String closeCategory(Long[] id) throws Exception {
		for (long _id : id) {
			categoryService.closeCategory(_id);
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}


	/**
	 * 重开分类。对应于closeCategory，关闭之后的分类可通过该action打开
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/restoreCategory", produces = "application/json; charset=UTF-8")
	public String restoreCategory(Long[] id) throws Exception {
		for (long _id : id) {
			categoryService.restoreCategory(_id);
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 获取分类信息
	 * @param categoryId
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getCategoryInfo",produces = "application/json; charset=UTF-8")
	public String getCategoryInfo(Long categoryId){
		Category categoryBean=categoryService.getCategoryById(categoryId);
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


}
