package com.dcampus.weblib.web.action;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dcampus.sys.util.UserUtils;
import com.dcampus.weblib.dao.CategoryDao;
import com.dcampus.weblib.entity.*;
import com.dcampus.weblib.service.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dcampus.common.config.PropertyUtil;
import com.dcampus.common.util.JS;
import com.dcampus.common.web.BaseController;
import com.dcampus.sys.entity.User;
import com.dcampus.sys.service.UserService;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.service.permission.IPermission.CategoryPerm;
import com.dcampus.weblib.service.permission.IPermission.GroupPerm;
import com.dcampus.weblib.service.permission.PermCollection;
import com.dcampus.weblib.service.permission.impl.PermProperty;
import com.dcampus.weblib.util.CurrentUserWrap;
import com.dcampus.weblib.util.Filter;

import javax.persistence.criteria.CriteriaBuilder;

@Controller
@RequestMapping(value = "/group")
public class ApplicationController extends BaseController{
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
	private  DomainService domainService;

	@Autowired
	private CategoryDao categoryDao;

	
	/**
	 * 获取所有应用或者根据id获取应用
	 * @param id 应用id,为空则获取所有应用
	 * @return
	 */
//	@RequiresUser
//	@ResponseBody
//	@RequestMapping(value="/getApplication", produces = "application/json; charset=UTF-8")
//	public String getApplication(Long id) {
//
//		List<Application> applications = new ArrayList<Application> ();
//		long totalCount;
//		if (id != null) {
//			applications.add(appService.getApplicationById(id));
//			totalCount = 1;
//		} else {
//			applications = appService.getAllApplications();
//			if (applications != null && applications.size() > 0) {
//				totalCount = applications.size();
//			} else {
//				totalCount = 0L;
//			}
//		}
//
//
//		StringBuffer buffer = new StringBuffer();
//		buffer.append("{");
//		buffer.append("\"totalCount\":").append(totalCount).append(",");
//		buffer.append("\"applications\":[");
//		if (totalCount >= 1) {
//			for (int i = 0; i < totalCount; ++i) {
//				buffer.append("{");
//				buffer.append("\"id\":").append(applications.get(i).getId()).append(",");
//				buffer.append("\"name\":\"").append(applications.get(i).getName()).append("\",");
//				buffer.append("\"desc\":\"").append(applications.get(i).getDesc()).append("\",");
//				buffer.append("\"creatorId\":\"").append(applications.get(i).getCreatorId()).append("\",");
//				buffer.append("\"creatorName\":\"").append(applications.get(i).getCreatorName()).append("\",");
//				buffer.append("\"totalSpace\":\"").append(applications.get(i).getTotalSpace()).append("\",");
//				buffer.append("\"availableSpace\":\"").append(applications.get(i).getAvailableSpace()).append("\",");
//				buffer.append("\"creatorDate\":\"").append(applications.get(i).getCreateDate()).append("\"");
//				buffer.append("},");
//			}
//			if (totalCount > 0) {
//				buffer.setLength(buffer.length() - 1);
//			}
//		}
//		buffer.append("]");
//		buffer.append("}");
//		return buffer.toString();
//	}

	/**
	 * 获取所有应用或者根据id获取应用
	 * @param id 应用id,为空则获取所有应用
	 * @return   string 返回json
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/getApplication_v2", produces = "application/json; charset=UTF-8")
	public String getApplication_v2(Long id) {

		List<Application> applications = new ArrayList<Application> ();
		long totalCount;
		//如果可查询到返回一个应用，否则返回全部的应用
		if (id != null) {
			applications.add(appService.getApplicationById(id));
			totalCount = 1;
		} else {
			applications = appService.getAllApplications();
			if (applications != null && applications.size() > 0) {
				totalCount = applications.size();
			} else {
				totalCount = 0L;
			}
		}

		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		buffer.append("\"totalCount\":").append(totalCount).append(",");
		buffer.append("\"applications\":[");
		if (totalCount >= 1) {
			for (int i = 0; i < totalCount; ++i) {
				buffer.append("{");
				buffer.append("\"id\":").append(applications.get(i).getId()).append(",");
				buffer.append("\"name\":\"").append(applications.get(i).getName()).append("\",");
				buffer.append("\"desc\":\"").append(applications.get(i).getDesc()).append("\",");
				//显示关联分类
				buffer.append("\"associatedCategoryId\":\"").append(applications.get(i).getCategoryId()).append("\",");
				Category cg=categoryService.getCategoryById(applications.get(i).getCategoryId());
				buffer.append("\"associatedCategoryName\":\"").append(cg.getDisplayName()).append("\",");
				if(applications.get(i).getDomain()!=null){
					//显示关联的域信息
					buffer.append("\"associatedDomainId\":\"").append(applications.get(i).getDomain().getId()).append("\",");
					buffer.append("\"associatedDomainName\":\"").append(applications.get(i).getDomain().getDomainName()).append("\",");
					//显示关联域所关联的组织
					List<DomainFolder> dfs=domainService.getDomainFoldersByDomainId(applications.get(i).getDomain().getId());
					if(dfs.size()!=0 && dfs!=null){
						DomainFolder df=dfs.get(0);
						buffer.append("\"associatedFolderId\":\"").append(df.getFolder().getId()).append("\",");
						buffer.append("\"associatedFolderName\":\"").append(df.getFolder().getSignature()).append("\",");
					}else {
						buffer.append("\"associatedFolderId\":\"").append("\",");
						buffer.append("\"associatedFolderName\":\"").append("\",");
					}
				}else {
					buffer.append("\"associatedDomainId\":\"").append("\",");
					buffer.append("\"associatedDomainName\":\"").append("\",");
					//显示关联域所关联的组织
					buffer.append("\"associatedFolderId\":\"").append("\",");
					buffer.append("\"associatedFolderName\":\"").append("\",");
				}

				buffer.append("\"creatorId\":\"").append(applications.get(i).getCreatorId()).append("\",");
				buffer.append("\"creatorName\":\"").append(applications.get(i).getCreatorName()).append("\",");
				buffer.append("\"totalSpace\":\"").append(applications.get(i).getTotalSpace()).append("\",");
				buffer.append("\"availableSpace\":\"").append(applications.get(i).getAvailableSpace()).append("\",");
				buffer.append("\"creatorDate\":\"").append(applications.get(i).getCreateDate().toString()
						.substring(0,applications.get(i).getCreateDate().toString().indexOf("."))).append("\"");
				buffer.append("},");
			}
			if (totalCount > 0) {
				buffer.setLength(buffer.length() - 1);
			}
		}
		buffer.append("]");
		buffer.append("}");
		return buffer.toString();
	}
	
	/**
	 * 创建应用
	 * @param space 应用空间kb
	 * @param name 应用名字
	 * @param desc 应用描述
	 * @return string 返回json
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/createApplication", produces = "application/json; charset=UTF-8")
	public String createApplication(String space, String name, String desc) {
		if (space == null || space == "") {
			throw new GroupsException("应用空间大小不能为空！");
		}
		if (name == null || name == "") {
			throw new GroupsException("应用名字不能为空！");
		}
		
		Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
		Long memberId = currentUserWrap.getMemberId();
		String memberName = currentUserWrap.getMemberName(); 
		Member creator = grouperService.getMemberById(memberId);


		Application application = new Application();
		Timestamp now = new Timestamp(System.currentTimeMillis());
		application.setCreateDate(now);
		application.setName(name);
		application.setDesc(desc == null ? "" : desc);
		application.setCreatorId(memberId);
		application.setCreatorName(memberName);
		application.setTotalSpace(space);
		application.setAvailableSpace(space);

		appService.createApplication(application);

		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/createApplication_v2", produces = "application/json; charset=UTF-8")
	public String createApplication_v2(String space, String name, String desc) {
		if (space == null || space == "") {
			throw new GroupsException("应用空间大小不能为空！");
		}
		if (name == null || name == "") {
			throw new GroupsException("应用名字不能为空！");
		}

		Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
		Long memberId = currentUserWrap.getMemberId();
		String memberName = currentUserWrap.getMemberName();
		Member creator = grouperService.getMemberById(memberId);


		Application application = new Application();
		Timestamp now = new Timestamp(System.currentTimeMillis());
		application.setCreateDate(now);
		application.setName(name);
		application.setDesc(desc == null ? "" : desc);
		application.setCreatorId(memberId);
		application.setCreatorName(memberName);
		application.setTotalSpace(space);
		application.setAvailableSpace(space);

		appService.createApplication_v2(application);

		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
	
	/**
	 * 根据应用id删除应用
	 * @param appId 应用id数组
	 * @return string 返回json
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/deleteApplication", produces = "application/json; charset=UTF-8")
	public String deleteApplication(Long[] appId) {
		if (appId != null ) {
			for (int i = 0; i < appId.length; i++) {
				Application application = appService.getApplicationById(appId[i]);
				appService.deleteApplication(application);
			}
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/deleteApplication_v2", produces = "application/json; charset=UTF-8")
	public String deleteApplication_v2(Long[] appId) {
		if (appId != null ) {
			for (int i = 0; i < appId.length; i++) {
				Application application = appService.getApplicationById(appId[i]);
				appService.deleteApplication_v2(application);
			}
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
	/**
	 * 根据id修改应用
	 * @param appId 应用id
	 * @param domainId  域Id
	 * @param space 空间
	 * @param name 名字
	 * @param desc 描述
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/modifyApplication", produces = "application/json; charset=UTF-8")
//	public String modifyApplication(Long appId, String space, String name, String desc) {
//		if (name == null || name == "") {
//			throw new GroupsException("应用名字不能为空！");
//		}
//
//		Application application = appService.getApplicationById(appId);
//		if (application == null) {
//			throw new GroupsException("应用不存在！");
//		}
//		application.setName(name);
//		application.setDesc(desc == null ? "" : desc);
//		long usecapacity=Long.parseLong(application.getTotalSpace())-Long.parseLong(application.getAvailableSpace());
//		long capacitycurrent=Long.parseLong(space)-usecapacity;
//		if (space != null) {
//			application.setAvailableSpace(String.valueOf(capacitycurrent));
//			application.setTotalSpace(space);
//		}
//		appService.modifyApplication(application);
//		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
//	}

	public String modifyApplication(Long appId, Long domainId, String space, String name, String desc) {
		if (name == null || name == "") {
			throw new GroupsException("应用名字不能为空！");
		}

		Application application = appService.getApplicationById(appId);
		if (application == null) {
			throw new GroupsException("应用不存在！");
		}
		application.setName(name);
		application.setDesc(desc == null ? "" : desc);
		//修改关联域，有传入值就修改关联域，没有传入值关联域设置为空
		if(domainId!=null){
			Domain domain=domainService.getDomainById(domainId);
			if(domain==null){
				throw  new GroupsException("传入的域不存在");
			}
			application.setDomain(domain);
		}else {
			if(application.getDomain()!=null){
				application.setDomain(null);
			}
		}

		long usecapacity=Long.parseLong(application.getTotalSpace())-Long.parseLong(application.getAvailableSpace());

		long capacitycurrent=Long.parseLong(space)-usecapacity;
		if (space != null) {
			if(Long.parseLong(space)<usecapacity)
				throw new GroupsException("应用的总容量不能小于已使用容量");
			else {
				application.setAvailableSpace(String.valueOf(capacitycurrent));
				application.setTotalSpace(space);
			}

		}
		appService.modifyApplication(application);
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/modifyApplication_v2", produces = "application/json; charset=UTF-8")
	public String modifyApplication_v2(Long appId, Long domainId, String space, String name, String desc) {
		if (name == null || name == "") {
			throw new GroupsException("应用名字不能为空！");
		}

		Application application = appService.getApplicationById(appId);
		if (application == null) {
			throw new GroupsException("应用不存在！");
		}
		application.setName(name);
		application.setDesc(desc == null ? "" : desc);
		//修改关联域，有传入值就修改关联域，没有传入值关联域设置为空
		if(domainId!=null){
			Domain domain=domainService.getDomainById(domainId);
			if(domain==null){
				throw  new GroupsException("传入的域不存在");
			}
			application.setDomain(domain);
		}else {
			if(application.getDomain()!=null){
				application.setDomain(null);
			}
		}

		appService.modifyApplication_v2(application,space);
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
	/**
	 * 根据应用获取应用的柜子
	 * @param appId 应用id
	 * @param start  开始个数
	 * @param  limit  结束
	 * @return  string 返回json
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/getGroupByApplication", produces = "application/json; charset=UTF-8")
	public String getGroupByApplication(Long appId,Integer start,Integer limit) {
		if (appId == null || appId == 0) {
			throw new GroupsException("应用id为空");
		}
		start=start==null?0:start;
		limit=limit==null? Integer.MAX_VALUE:limit;
		Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
		Long memberId = currentUserWrap.getMemberId();
		long parentId = appService.getApplicationById(appId).getCategoryId();
		long totalCount=0L;
		long categoriesCount=categoryService.getCategoriesTotalCount(parentId);
		long groupsCount=groupService.getGroupsInCategoryTotalCount(parentId);
		List<Category> beans=null;
		List<Group> result=null;
		totalCount=categoriesCount+groupsCount;
		//优先返回分类
		if(start>=totalCount){
			StringBuffer stringBuffer=new StringBuffer();
			stringBuffer.append("{\"totalCount\":").append(totalCount).append(",");
		    stringBuffer.append("\"categoriesCount\":").append(categoriesCount).append(",");
			stringBuffer.append("\"groupCount\":").append(groupsCount).append(",");
			stringBuffer.append("\"message\":").append("\"parameter start is illegal\"").append("}");
			return stringBuffer.toString();
		}
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

                //这里去数据库查询分类
                //List<Category> beans = categoryService.getCategorieByParent(parentId);
                List<Category> categoryBeanList = new ArrayList<Category>();
                Category[] categoryBeans = null;
                if (start <= categoriesCount - 1) {
                    List<OldPerm> permList = permService.getMemberPerms(memberId, OldPerm.PERM_TYPE_CATEGORY);
                    Map<Long, Category> catMap = new HashMap<Long, Category>();
                    Map<Long, Long> permMap = new HashMap<Long, Long>();
                    for (OldPerm p : permList) {
                        permMap.put(p.getTypeId(), p.getPermCode());
                        List<Category> list = categoryService.tracedCategoryList(p.getTypeId());
                        if (list != null) {
                            for (Category b : list) {
                                catMap.put(b.getId(), b);
                            }
                        }
                    }

                    List<PermCollection> pcList = new ArrayList<PermCollection>();
                    for (Category bean : beans) {
                        categoryBeanList.add(bean);
                    }
                    categoryBeans = (Category[]) categoryBeanList
                            .toArray(new Category[0]);
                }

                Group[] groupBeans = null;
                List<Group> list = new ArrayList<Group>();
                if (parentId > 0) {
                    //这里去数据库查询柜子
                    //List<Group> result = groupService.getGroupsInCategory(parentId);
                    if (result != null && result.size() > 0) {
                        for (Group bean : result) {
                            list.add(bean);
                        }
                    }
                }
                groupBeans = list.toArray(new Group[list.size()]);

                // 获取节点是否有子节点
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
                singleFileSize = new long[groupBeans.length];
                totalSize = new long[groupBeans.length];
                for (int i = 0; i < groupBeans.length; ++i) {
                    singleFileSize[i] = resourceService.getResourceSingleFileSize(groupBeans[i].getId());
                    totalSize[i] = resourceService.getResourceSpaceSize(
                            groupBeans[i].getId());
                }
                List<GroupIcon> iconBeans = groupService.getAllGroupIcons();
                iconMap = new HashMap<Long, GroupIcon>();
                for (GroupIcon icon : iconBeans) {
                    iconMap.put(icon.getId(), icon);
                }

                StringBuffer buffer = new StringBuffer();
                buffer.append("{\"totalCount\":").append(totalCount).append(",");
                buffer.append("\"categoriesCount\":").append(categoriesCount).append(",");
                if (start <= categoriesCount - 1) {
                    buffer.append("\"categories\":[");
                    for (int i = 0; i < leaf.length; ++i) {
                        Category bean = categoryBeans[i];
                        boolean le = leaf[i];

                        buffer.append("{");
                        buffer.append("\"id\":").append(bean.getId()).append(",");
                        buffer.append("\"name\":\"").append(bean.getName()).append("\",");
                        if (bean.getDisplayName() == null || bean.getDisplayName().equals("")) {
                            buffer.append("\"displayName\":\"").append(bean.getName()).append("\",");
                        } else {
                            buffer.append("\"displayName\":\"").append(JS.quote(Filter.convertHtmlBody(bean.getDisplayName()))).append("\",");
                        }
                        buffer.append("\"desc\":\"").append(JS.quote(Filter.convertHtmlBody(bean.getDesc()))).append("\",");
                        buffer.append("\"parentId\":").append(bean.getParentId()).append(",");
                        buffer.append("\"order\":").append((int) bean.getOrder()).append(",");
                        buffer.append("\"status\":").append(bean.getCategoryStatus()).append(",");
                        buffer.append("\"leaf\":").append(le);
                        buffer.append("},");
                    }
                    if (categoryBeans.length > 0)
                        buffer.setLength(buffer.length() - 1);
                    buffer.append("],");
                }
                buffer.append("\"groupsCount\":").append(groupsCount).append(",");
                buffer.append("\"groups\":[");
                for (int i = 0; i < groupBeans.length; ++i) {
                    Group bean = groupBeans[i];
                    long sf = singleFileSize[i];
                    long tf = totalSize[i];
                    buffer.append("{");
                    buffer.append("\"id\":").append(bean.getId()).append(",");
                    buffer.append("\"categoryId\":").append(bean.getCategory().getId()).append(",");
                    buffer.append("\"name\":\"").append(bean.getName()).append("\",");
                    buffer.append("\"displayName\":\"").append(JS.quote(Filter.convertHtmlBody(bean.getDisplayName() == null ? "" : bean.getDisplayName()))).append("\",");
                    if (iconMap.containsKey(bean.getGroupIcon())) {
                        GroupIcon icon = iconMap.get(bean.getGroupIcon());
                        buffer.append("\"icon\":\"").append(PropertyUtil.getGroupIconFolderPath() + icon.getFileName()).append("\",");
                        buffer.append("\"iconName\":\"").append(JS.quote(Filter.convertHtmlBody(icon.getName() == null ? "" : icon.getName()))).append("\",");
                        buffer.append("\"iconId\":").append(bean.getGroupIcon()).append(",");
                    } else {
                        buffer.append("\"icon\":\"").append(PropertyUtil.getGroupDefaultIcon()).append("\",");
                        buffer.append("\"iconName\":\"").append("").append("\",");
                        buffer.append("\"iconId\":").append(0).append(",");
                    }
                    buffer.append("\"order\":").append((int) bean.getOrder()).append(",");
                    buffer.append("\"desc\":\"").append(JS.quote(Filter.convertHtmlBody(bean.getDesc()))).append("\",");
                    buffer.append("\"singleFileSize\":").append(sf).append(",");
                    buffer.append("\"paiban\":\"").append(bean.getExtendField1() == null ? "" : bean.getExtendField1()).append("\",");
                    buffer.append("\"documentType\":").append(bean.getDocumentTypeValue()).append(",");
                    buffer.append("\"totalSize\":").append(tf);
                    buffer.append("},");
                }
                if (groupBeans.length > 0)
                    buffer.setLength(buffer.length() - 1);
                buffer.append("]}");
                return buffer.toString();
            }
	
	/**
	 * 创建应用下的柜子
	 * @param appId   应用Id
	 * @param categoryId  分类Id
	 * @param displayName  显示名字
	 * @param desc    描述
	 * @param totalSize   总容量
	 * @param singleFileSize   单个文件容量
	 * @param name   名字
	 * @param documentType  文档类型
	 * @param iconId   图标Id
	 * @param paiban  paiban
	 * @return string 返回json
	 * @throws Exception  抛出异常
	 */
//	@RequiresUser
//	@ResponseBody
//	@RequestMapping(value="/createApplicationGroup", produces = "application/json; charset=UTF-8")
//	public String createApplicationGroup(Long appId, Long categoryId, String displayName, String desc, Long totalSize, Long singleFileSize,
//			String name, Integer documentType, Long iconId, String paiban) throws Exception {
//		if (appId == null || appId == 0) {
//			throw new GroupsException("应用id为空！");
//		}
//		if (name == null || displayName ==null) {
//			throw new GroupsException("柜子名字不能为空！");
//		}
//		Subject currentUser = SecurityUtils.getSubject();
//		Session session = currentUser.getSession();
//		CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
//		Long memberId = currentUserWrap.getMemberId();
//		String memberName = currentUserWrap.getMemberName();
//		Application applicationBean = appService.getApplicationById(appId);
//		long appcid = applicationBean.getCategoryId();
//		Category appCat = categoryService.getCategoryById(appcid);
//
//
//		// 创建圈子
//		Group groupBean = new Group();
//		if (categoryId == null || categoryId == 0) {
//			groupBean.setCategory(appCat);
//			double _order = groupService.getGroupMaxOrder(appCat.getId());
//			_order++;
//			groupBean.setOrder(_order);
//		} else {
//			Category category = categoryService.getCategoryById(categoryId);
//			groupBean.setCategory(category);
//			double _order = groupService.getGroupMaxOrder(category.getId());
//			_order++;
//			groupBean.setOrder(_order);
//		}
//		groupBean.setDocumentTypeValue(documentType == null ? GroupResource.DOCUMENT_TYPE_UNKNOWN : documentType.intValue());
//		groupBean.setName(Long.toString(System.currentTimeMillis()));
//		groupBean.setAddr(Long.toString(System.currentTimeMillis()));
//		groupBean.setDesc(desc == null ? "" : desc);
//		groupBean.setDisplayName(displayName);
//		groupBean.setGroupIcon(iconId == null ? 0L : iconId.longValue());
//		groupBean.setCreateDate(new Timestamp(System.currentTimeMillis()));
//		groupBean.setExtendField1(paiban);
//		groupBean.setApplicationId(applicationBean.getId());
//
//		groupBean.setCreatorId(memberId);
//		groupBean.setCreatorName(memberName);
//		groupBean.setGroupStatus(Group.STATUS_NORMAL);
//		groupBean.setGroupUsage(Group.USAGE_NORMAL);
//
//		if (Long.parseLong(applicationBean.getAvailableSpace())<totalSize) throw  new GroupsException("应用剩余可用空间不足");
//
//		applicationBean.setAvailableSpace(Long.toString(Long.parseLong(applicationBean.getAvailableSpace())-totalSize));
//		groupService.createGroup(groupBean, true, null);
//
//		appService.modifyApplication(applicationBean);
//
//		long groupId = groupBean.getId();
//
//		this.modifyGroupPermission(null, groupId);
//
//		// 修改圈子容量和单文件大小
//		if (singleFileSize > 0) {
//			groupService.modifySingleFileSize(groupId, singleFileSize);
//		}
//		if (totalSize > 0) {
//			groupService.modifyTotalFileSize(groupId, totalSize);
//		}
//		// 创建回收站
//		resourceService.createResourceDir(groupId, PropertyUtil.getRecyclerName(), 0, memberId, true);
//
//		return "{\"type\":\"success\",\"code\":\"200\",\"success\":true,\"id\":"+groupId+"}";
//
//	}

	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/createApplicationGroup", produces = "application/json; charset=UTF-8")
	public String createApplicationGroup(Long appId, Long categoryId, String displayName, String desc, Long totalSize, Long singleFileSize,
											String name, Integer documentType, Long iconId, String paiban) throws Exception {
		if (appId == null || appId == 0) {
			throw new GroupsException("应用id为空！");
		}
		if (name == null || displayName ==null) {
			throw new GroupsException("柜子名字不能为空！");
		}
		Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
		Long memberId = currentUserWrap.getMemberId();
		String memberName = currentUserWrap.getMemberName();
		Application applicationBean = appService.getApplicationById(appId);
		long appcid = applicationBean.getCategoryId();
		//应用关联的分类
		Category appCat = categoryService.getCategoryById(appcid);


		// 创建圈子
		Group groupBean = new Group();
		if (categoryId == null || categoryId == 0) {
			groupBean.setCategory(appCat);
			double _order = groupService.getGroupMaxOrder(appCat.getId());
			_order++;
			groupBean.setOrder(_order);
		} else {
			Category category = categoryService.getCategoryById(categoryId);
			groupBean.setCategory(category);
			double _order = groupService.getGroupMaxOrder(category.getId());
			_order++;
			groupBean.setOrder(_order);
		}


		groupBean.setDocumentTypeValue(documentType == null ? GroupResource.DOCUMENT_TYPE_UNKNOWN : documentType.intValue());
		groupBean.setName(Long.toString(System.currentTimeMillis()));
		groupBean.setAddr(Long.toString(System.currentTimeMillis()));
		groupBean.setDesc(desc == null ? "" : desc);
		groupBean.setDisplayName(displayName);
		groupBean.setGroupIcon(iconId == null ? 0L : iconId.longValue());
		groupBean.setCreateDate(new Timestamp(System.currentTimeMillis()));
		groupBean.setExtendField1(paiban);
		groupBean.setApplicationId(applicationBean.getId());

		groupBean.setCreatorId(memberId);
		groupBean.setCreatorName(memberName);
		groupBean.setGroupStatus(Group.STATUS_NORMAL);
		groupBean.setGroupUsage(Group.USAGE_NORMAL);
		//赋予柜子的可用容量
		groupBean.setAvailableCapacity(totalSize);
		groupBean.setUsedCapacity(0L);
		Long topCategoryId=0L;
		List<Category> c = categoryDao.getCategoriesByName("#application");
		Category cg=c.get(0);
		if(cg.getTotalCapacity()==null || cg.getTotalCapacity()<=0)
			throw new GroupsException("请先为应用分类配置容量");

		//同步
//		if(appCat.getAvailableCapacity()!=null&&appCat.getAvailableCapacity()==Long.parseLong(applicationBean.getAvailableSpace())&&
//				appCat.getTotalCapacity()==Long.parseLong(applicationBean.getTotalSpace())){
		groupBean.setTopCategoryId(appCat.getId());
		topCategoryId=appCat.getId();
		if (Long.parseLong(applicationBean.getAvailableSpace())<totalSize) throw  new GroupsException("应用的可用容量不足");
		System.out.println(appCat.getAvailableCapacity());
//			appCat.setAvailableCapacity(appCat.getAvailableCapacity()-totalSize);
		System.out.println(appCat.getAvailableCapacity());
		applicationBean.setAvailableSpace(Long.toString(Long.parseLong(applicationBean.getAvailableSpace())-totalSize));
//	    categoryDao.saveOrUpdateCategory(appCat);
//		}else {//不同步
//			topCategoryId=null;
//			Long capacityall=Long.parseLong(applicationBean.getTotalSpace());
//			//先进行同步
//			appCat.setTotalCapacity(capacityall);
//			appCat.setAvailableCapacity(Long.parseLong(applicationBean.getAvailableSpace()));
//			cg.setAvailableCapacity(cg.getAvailableCapacity()-capacityall);
//
//			if(cg.getAvailableCapacity()<capacityall) throw new  GroupsException("应用系统分类可用容量不足");
//			if (Long.parseLong(applicationBean.getAvailableSpace())<totalSize) throw  new GroupsException("应用的可用容量不足");
//			applicationBean.setAvailableSpace(Long.toString(Long.parseLong(applicationBean.getAvailableSpace())-totalSize));
//			appCat.setAvailableCapacity(Long.parseLong(applicationBean.getAvailableSpace())-totalSize);
//			categoryDao.saveOrUpdateCategory(appCat);
//			categoryDao.saveOrUpdateCategory(cg);
//		}

//		if (Long.parseLong(applicationBean.getAvailableSpace())<totalSize) throw  new GroupsException("应用剩余可用空间不足");
//
//		applicationBean.setAvailableSpace(Long.toString(Long.parseLong(applicationBean.getAvailableSpace())-totalSize));
		long memberIdd = UserUtils.getCurrentMemberId();
		groupService.createGroup_v2(groupBean, true, null, memberIdd, topCategoryId, totalSize);

		appService.modifyApplication(applicationBean);

		long groupId = groupBean.getId();

		this.modifyGroupPermission(null, groupId);

		// 修改圈子容量和单文件大小
		if (singleFileSize > 0) {
			groupService.modifySingleFileSize(groupId, singleFileSize);
		}
		if (totalSize > 0) {
			groupService.modifyTotalFileSize(groupId, totalSize);
		}
		// 创建回收站
		resourceService.createResourceDir(groupId, PropertyUtil.getRecyclerName(), 0, memberId, true);

		return "{\"type\":\"success\",\"code\":\"200\",\"success\":true,\"id\":"+groupId+"}";

	}
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/createApplicationGroup_v2", produces = "application/json; charset=UTF-8")
	public String createApplicationGroup_v2(Long appId, Long categoryId, String displayName, String desc, Long totalSize, Long singleFileSize,
										 String name, Integer documentType, Long iconId, String paiban) throws Exception {
		if (appId == null || appId == 0) {
			throw new GroupsException("应用id为空！");
		}
		if (name == null || displayName ==null) {
			throw new GroupsException("柜子名字不能为空！");
		}
		Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
		Long memberId = currentUserWrap.getMemberId();
		String memberName = currentUserWrap.getMemberName();
		Application applicationBean = appService.getApplicationById(appId);
		long appcid = applicationBean.getCategoryId();
		//应用关联的分类
		Category appCat = categoryService.getCategoryById(appcid);


		// 创建圈子
		Group groupBean = new Group();
		if (categoryId == null || categoryId == 0) {
			groupBean.setCategory(appCat);
			double _order = groupService.getGroupMaxOrder(appCat.getId());
			_order++;
			groupBean.setOrder(_order);
		} else {
			Category category = categoryService.getCategoryById(categoryId);
			groupBean.setCategory(category);
			double _order = groupService.getGroupMaxOrder(category.getId());
			_order++;
			groupBean.setOrder(_order);
		}


		groupBean.setDocumentTypeValue(documentType == null ? GroupResource.DOCUMENT_TYPE_UNKNOWN : documentType.intValue());
		groupBean.setName(Long.toString(System.currentTimeMillis()));
		groupBean.setAddr(Long.toString(System.currentTimeMillis()));
		groupBean.setDesc(desc == null ? "" : desc);
		groupBean.setDisplayName(displayName);
		groupBean.setGroupIcon(iconId == null ? 0L : iconId.longValue());
		groupBean.setCreateDate(new Timestamp(System.currentTimeMillis()));
		groupBean.setExtendField1(paiban);
		groupBean.setApplicationId(applicationBean.getId());

		groupBean.setCreatorId(memberId);
		groupBean.setCreatorName(memberName);
		groupBean.setGroupStatus(Group.STATUS_NORMAL);
		groupBean.setGroupUsage(Group.USAGE_NORMAL);
		//赋予柜子的可用容量
		groupBean.setAvailableCapacity(totalSize);
		groupBean.setUsedCapacity(0L);
		Long topCategoryId=0L;
		List<Category> c = categoryDao.getCategoriesByName("#application");
		Category cg=c.get(0);
		if(cg.getTotalCapacity()==null || cg.getTotalCapacity()<=0)
			throw new GroupsException("请先为应用分类配置容量");

		//同步
//		if(appCat.getAvailableCapacity()!=null&&appCat.getAvailableCapacity()==Long.parseLong(applicationBean.getAvailableSpace())&&
//				appCat.getTotalCapacity()==Long.parseLong(applicationBean.getTotalSpace())){
		groupBean.setTopCategoryId(appCat.getId());
		topCategoryId=appCat.getId();
		if (Long.parseLong(applicationBean.getAvailableSpace())<totalSize) throw  new GroupsException("应用的可用容量不足");
		System.out.println(appCat.getAvailableCapacity());
//			appCat.setAvailableCapacity(appCat.getAvailableCapacity()-totalSize);
		System.out.println(appCat.getAvailableCapacity());
		applicationBean.setAvailableSpace(Long.toString(Long.parseLong(applicationBean.getAvailableSpace())-totalSize));
//	    categoryDao.saveOrUpdateCategory(appCat);
//		}else {//不同步
//			topCategoryId=null;
//			Long capacityall=Long.parseLong(applicationBean.getTotalSpace());
//			//先进行同步
//			appCat.setTotalCapacity(capacityall);
//			appCat.setAvailableCapacity(Long.parseLong(applicationBean.getAvailableSpace()));
//			cg.setAvailableCapacity(cg.getAvailableCapacity()-capacityall);
//
//			if(cg.getAvailableCapacity()<capacityall) throw new  GroupsException("应用系统分类可用容量不足");
//			if (Long.parseLong(applicationBean.getAvailableSpace())<totalSize) throw  new GroupsException("应用的可用容量不足");
//			applicationBean.setAvailableSpace(Long.toString(Long.parseLong(applicationBean.getAvailableSpace())-totalSize));
//			appCat.setAvailableCapacity(Long.parseLong(applicationBean.getAvailableSpace())-totalSize);
//			categoryDao.saveOrUpdateCategory(appCat);
//			categoryDao.saveOrUpdateCategory(cg);
//		}

//		if (Long.parseLong(applicationBean.getAvailableSpace())<totalSize) throw  new GroupsException("应用剩余可用空间不足");
//
//		applicationBean.setAvailableSpace(Long.toString(Long.parseLong(applicationBean.getAvailableSpace())-totalSize));
		long memberIdd = UserUtils.getCurrentMemberId();
		groupService.createGroup_v2(groupBean, true, null,memberIdd,topCategoryId,totalSize);

		appService.modifyApplication(applicationBean);

		long groupId = groupBean.getId();

		this.modifyGroupPermission(null, groupId);

		// 修改圈子容量和单文件大小
		if (singleFileSize > 0) {
			groupService.modifySingleFileSize(groupId, singleFileSize);
		}
		if (totalSize > 0) {
			groupService.modifyTotalFileSize(groupId, totalSize);
		}
		// 创建回收站
		resourceService.createResourceDir(groupId, PropertyUtil.getRecyclerName(), 0, memberId, true);

		return "{\"type\":\"success\",\"code\":\"200\",\"success\":true,\"id\":"+groupId+"}";

	}
	/**
	 * 根据应用获取member
	 * @param appId 应用id
	 * @param start  开始个数
	 * @param limit  结束个数
	 * @return string 返回json
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/getMemberByApplication", produces = "application/json; charset=UTF-8")
	public String getMemberByApplication(Long appId, Integer start, Integer limit) throws Exception {
		if (appId == null || appId == 0) {
			throw new GroupsException("应用id为空！");
		}
		start = start == null ? 0 : start;
		limit = limit == null ? Integer.MAX_VALUE : limit;
		List<Member> memberBeans = appService.getMemberByApplication(appId, start, limit);
		List<Boolean> isAdminList = new ArrayList<Boolean>();
		List<User> memberDetailList = new ArrayList<User>();
		long totalCount = 0L;
		if (memberBeans != null) {
			totalCount = memberBeans.size();
			for (Member bean: memberBeans) {
				User user = userService.getUserByAccount(bean.getAccount());
				if (user != null) {
					memberDetailList.add(user);
					isAdminList.add(appService.isApplicationAdmin(bean.getId(), appId));
				}
			}
		}
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		buffer.append("\"totalCount\":").append(totalCount).append(",");
		buffer.append("\"applications\":[");
		if (memberBeans != null) {
			for (int i = 0; i < totalCount; ++i) {
				buffer.append("{");
				buffer.append("\"id\":").append(memberBeans.get(i).getId()).append(",");
				buffer.append("\"name\":\"").append(memberBeans.get(i).getName()).append("\",");
				buffer.append("\"account\":\"").append(memberBeans.get(i).getAccount()).append("\",");
				buffer.append("\"status\":\"").append(memberBeans.get(i).getMemberStatus()).append("\",");
				buffer.append("\"type\":\"").append(memberBeans.get(i).getMemberType()).append("\",");
				buffer.append("\"isAppAdmin\":").append(isAdminList.get(i).toString()).append(",");
				buffer.append("\"company\":\"").append(memberDetailList.get(i).getCompany()).append("\",");
				buffer.append("\"email\":\"").append(memberDetailList.get(i).getEmail()).append("\",");
				buffer.append("\"department\":\"").append(memberDetailList.get(i).getDepartment()).append("\",");
				buffer.append("\"mobile\":\"").append(memberDetailList.get(i).getMobile()).append("\",");
				buffer.append("\"phone\":\"").append(memberDetailList.get(i).getPhone()).append("\",");
				buffer.append("\"position\":\"").append(memberDetailList.get(i).getPosition()).append("\"");			
				buffer.append("},");
			}
			if (totalCount > 0) {
				buffer.setLength(buffer.length() - 1);
			}
		}
		buffer.append("]");
		buffer.append("}");
		return buffer.toString();
	}
	
	/**
	 * 添加member到应用
	 * @param mids memberId数组
	 * @param appId 应用id
	 * @param isManager 是否管理员，为空则否
	 * @return string
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/addMemberToApplication", produces = "application/json; charset=UTF-8")
	public String addMemberToApplication(Long[] mids, Long appId,Boolean isManager) throws Exception {
		if (appId == null || appId == 0) {
			throw new GroupsException("应用id为空！");
		}
		if (mids == null || mids.length <= 0) {
			throw new GroupsException("用户memberId为空！");
		}
		isManager = isManager == null ? false : isManager;
		appService.addMembersToApplication(mids, appId, isManager);
		// 授权
		List<Group> groupBeans = groupService.getGroupsByApp(appId);
		long appCatId = appService.getApplicationById(appId).getCategoryId();
		
		for (long id : mids) {

			// 分类管理员权限
			PermCollection pc_cat = permService.getPermission(appCatId,id, OldPerm.PERM_TYPE_CATEGORY);
			if (isManager == true) {
				pc_cat.addCategoryPerm(new CategoryPerm[] { CategoryPerm.CATEGORY_APPMANAGER });
			} else {
				pc_cat.removeCategoryPerm(new CategoryPerm[] { CategoryPerm.CATEGORY_APPMANAGER });
			}
			permService.modifyPermission(appCatId, id, OldPerm.PERM_TYPE_CATEGORY, pc_cat);
			
			for (Group bean : groupBeans) {

				PermCollection pc = permService.getPermission(bean.getId(), id, OldPerm.PERM_TYPE_GROUP);

				List<GroupPerm> list_GroupPerm = new ArrayList<GroupPerm>();
				list_GroupPerm.add(GroupPerm.UPLOAD_RESOURCE);
				list_GroupPerm.add(GroupPerm.DOWNLOAD_RESOURCE);
				list_GroupPerm.add(GroupPerm.VIEW_RESOURCE);
				list_GroupPerm.add(GroupPerm.MODIFY_RESOURCE);
				list_GroupPerm.add(GroupPerm.DELETE_RESOURCE);
				if (isManager == true) {
					// 解析需要设置的权限，并设置
					groupService.bindMemberToGroup(id, bean.getId());
					list_GroupPerm.add(GroupPerm.GROUP_APPMANAGER);
					list_GroupPerm.add(GroupPerm.ADD_FOLDER);
					pc.addGroupPerm(list_GroupPerm.toArray(new GroupPerm[list_GroupPerm.size()]));
				} else {
					if (bean.getCategory().getId() != categoryService.getCategoryByName(PropertyUtil.getDefaultPersonGroupCategory()).getId()) {
						pc.addGroupPerm(list_GroupPerm.toArray(new GroupPerm[list_GroupPerm.size()]));
					}
				}
				// 重新设置权限
				permService.modifyPermission(bean.getId(), id,OldPerm.PERM_TYPE_GROUP, pc);
			}
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	/**
	 * 修改应用的member
	 * @param mids memberId
	 * @param appId 应用id
	 * @param isManager 是否管理员，为空则否
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/modifyMemberinApplication", produces = "application/json; charset=UTF-8")
	public String modifyMemberinApplication(Long mids, Long appId,Boolean isManager) throws Exception {
		if (appId == null || appId == 0) {
			throw new GroupsException("应用id为空！");
		}
		if (mids == null || mids == 0) {
			throw new GroupsException("用户memberId为空！");
		}
		isManager = isManager == null ? false : isManager;
		appService.modifyMemberinApplication(mids, appId, isManager);
		
		List<Group> groupBeans = groupService.getGroupsByApp(appId);
		long appCatId = appService.getApplicationById(appId).getCategoryId();
		
		for (Group bean : groupBeans) {
			PermCollection pc = permService.getPermission(bean.getId(),
					mids, OldPerm.PERM_TYPE_GROUP);
			if (isManager == true) {
				pc.addGroupPerm(new GroupPerm[] { GroupPerm.GROUP_APPMANAGER });
				// 分类管理员权限
			} else {
				pc.removeGroupPerm(new GroupPerm[] { GroupPerm.GROUP_APPMANAGER });
			}
			// 重新设置权限
			permService.modifyPermission(bean.getId(), mids, OldPerm.PERM_TYPE_GROUP, pc);			
		}
		
		PermCollection pc_cat = permService.getPermission(appCatId, mids, OldPerm.PERM_TYPE_CATEGORY);
		if (isManager == true) {
			// 分类管理员权限
			pc_cat.addCategoryPerm(new CategoryPerm[] { CategoryPerm.CATEGORY_APPMANAGER });
		} else {
			pc_cat.removeCategoryPerm(new CategoryPerm[] { CategoryPerm.CATEGORY_APPMANAGER });
		}
		permService.modifyPermission(appCatId, mids, OldPerm.PERM_TYPE_CATEGORY, pc_cat);
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
	
	/**
	 * 根据memberId将用户从应用中删除
	 * @param mids 用户memberId
	 * @param appId 应用id
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/deletMemberFromApplication", produces = "application/json; charset=UTF-8")
	public String deletMemberFromApplication(Long[] mids, Long appId) throws Exception {
		if (mids != null && appId != null) {
			appService.deletMemberFromApplication(mids, appId);
			List<Group> groupBeans = groupService.getGroupsByApp(appId);
			long appCatId = appService.getApplicationById(appId).getCategoryId();
			// 移除授权
			for (long id : mids) {
				for (Group bean : groupBeans) {
					PermCollection pc = permService.getPermission(bean.getId(), id, OldPerm.PERM_TYPE_GROUP);
					pc.removeGroupPerm(new GroupPerm[] {
							GroupPerm.GROUP_APPMANAGER, GroupPerm.UPLOAD_RESOURCE,
							GroupPerm.DOWNLOAD_RESOURCE, GroupPerm.VIEW_RESOURCE,
							GroupPerm.MODIFY_RESOURCE, GroupPerm.DELETE_RESOURCE });

					// 重新设置权限
					permService.modifyPermission(bean.getId(), id, OldPerm.PERM_TYPE_GROUP, pc);
				}

				PermCollection pc_cat = permService.getPermission(appCatId, id, OldPerm.PERM_TYPE_CATEGORY);
				pc_cat.removeCategoryPerm(new CategoryPerm[] { CategoryPerm.CATEGORY_APPMANAGER });
				permService.modifyPermission(appCatId, id, OldPerm.PERM_TYPE_CATEGORY, pc_cat);
			}			
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
	
	/**
	 * 创建应用下的分类
	 * @param appId 应用id
	 * @param displayName
	 * @return
	 * @throws Exception
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/createApplicationCategory_v2", produces = "application/json; charset=UTF-8")
	public String createApplicationCategory_lms_v2(Long appId,String[] displayName) {
		if (displayName == null) {
			throw new GroupsException("分类名不能为空！");
		}
		int length=displayName.length;
		String  result="";
		Application applicationBean = appService.getApplicationById(appId);
		Long parentId=applicationBean.getCategoryId();
		for(int i=0;i<length;i++){
			Long  isCreated=appService.getCategoryByParentIdAndDisplayName(parentId,displayName[i]);
			if(isCreated!=null){
				parentId=isCreated;
				if(i==length-1){
					result=isCreated.toString();
					break;
				}
				continue;
			}
			Category bean = new Category();
			bean.setCreateDate(new Timestamp(System.currentTimeMillis()));
			bean.setName(String.valueOf(System.currentTimeMillis()));
			bean.setDisplayName(displayName[i]);
			if ( parentId == 0) {
				bean.setParentId(applicationBean.getCategoryId());
				double num = categoryService.getMaxOrder(applicationBean.getCategoryId());
				num++;
				bean.setOrder(num);
				bean.setCategoryStatus(Category.STATUS_NORMAL);
				categoryService.createCategory(bean, true);
			} else {
				try {
					Category category=categoryService.getCategoryById(parentId);
					if(category!=null) {
						bean.setParentId(parentId);
						double num = categoryService.getMaxOrder(parentId);
						num++;
						bean.setOrder(num);
						bean.setCategoryStatus(Category.STATUS_NORMAL);
						categoryService.createCategory(bean, true);
					}
				}catch (Exception e){
					throw new GroupsException("传入的parentId有误!");
				}
			}
			parentId=bean.getId();
			if(i==length-1)
				result=bean.getId().toString();
		}

		StringBuffer buffer = new StringBuffer();
		buffer.append("{\"type\":\"success\",\"code\":\"200\",\"id\":\""+result+"\"}");
		return buffer.toString();
	}


	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/createApplicationCategory", produces = "application/json; charset=UTF-8")
	public String createApplicationCategory(Long appId, Long parentId,String displayName, String name, String desc) throws Exception {
		if (displayName == null || name == null) {
			throw new GroupsException("分类名不能为空！");
		}
		Category bean = new Category();
		bean.setCreateDate(new Timestamp(System.currentTimeMillis()));
		bean.setDesc(desc == null ? "" : desc);
		bean.setName(name);
		bean.setDisplayName(displayName);
		if (parentId == null || parentId == 0) {
			Application applicationBean = appService.getApplicationById(appId);
			bean.setParentId(applicationBean.getCategoryId());
			double num = categoryService.getMaxOrder(applicationBean.getCategoryId());
			num++;
			bean.setOrder(num);
		} else {
			bean.setParentId(parentId);
			double num = categoryService.getMaxOrder(parentId);
			num++;
			bean.setOrder(num);
		}
		bean.setCategoryStatus(Category.STATUS_NORMAL);
		categoryService.createCategory(bean,true);

		StringBuffer buffer = new StringBuffer();
		buffer.append("{\"type\":\"success\",\"code\":\"200\",\"id\":\""+bean.getId().toString()+"\"}");
		return buffer.toString();
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
}
