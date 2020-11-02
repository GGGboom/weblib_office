package com.dcampus.weblib.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dcampus.common.config.PropertyUtil;
import com.dcampus.common.generic.GenericDao;
import com.dcampus.common.service.BaseService;
import com.dcampus.sys.util.UserUtils;
import com.dcampus.weblib.dao.CategoryDao;
import com.dcampus.weblib.dao.GroupDao;
import com.dcampus.weblib.dao.GroupResourceDao;
import com.dcampus.weblib.dao.MemberDao;
import com.dcampus.weblib.entity.AppMember;
import com.dcampus.weblib.entity.Application;
import com.dcampus.weblib.entity.Category;
import com.dcampus.weblib.entity.Group;
import com.dcampus.weblib.entity.Member;
import com.dcampus.weblib.entity.OldPerm;
import com.dcampus.weblib.exception.NotAuthorizedException;
import com.dcampus.weblib.exception.PermissionsException;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.service.permission.IPermission.CategoryPerm;
import com.dcampus.weblib.service.permission.IPermission.GlobalPerm;
import com.dcampus.weblib.service.permission.IPermission.GroupPerm;
import com.dcampus.weblib.service.permission.PermCollection;
import com.dcampus.weblib.service.permission.RolePermissionUtils;
import com.dcampus.weblib.service.permission.impl.Permission;
import com.dcampus.weblib.util.CurrentUserWrap;

/**
 * 应用处理service
 * @author patrick
 *
 */
@Service
@Transactional(readOnly=false)
public class ApplicationService extends BaseService {

	@Autowired
	GenericDao genericDao;
	
	@Autowired
	CategoryDao categoryDao;
	
	@Autowired
	MemberDao memberDao;
	
	@Autowired
	GroupDao groupDao;
	
//	@Autowired
//	GroupResourceDao groupResourceDao;
	
	@Autowired
	private PermissionService permService;
	
	@Autowired
	private RolePermissionUtils roleUtils;
	
	@Autowired
	private Permission permission;


	public Long getCategoryByParentIdAndDisplayName(Long parentId, String displayName){
		Category category=categoryDao.getCategoryByParentIdAndDisplayName(parentId, displayName);
	    if(category!=null){
	    	return category.getId();
		}
	    else return null;
	}


	/**
	 * 创建应用，同时创建同名分类用以存放应用的柜子
	 * @param application
	 */
	public void createApplication(Application application) {
		if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_app_m")){
			throw new PermissionsException("无应用管理权限");
		}
		this.checkApplicationPerm();
		List<Category> c = categoryDao.getCategoriesByName("#application");
		long categoryId = c.get(0).getId();

		// 创建分类
		Category categoryBean = new Category();
		categoryBean.setCreateDate(new Timestamp(System.currentTimeMillis()));
		categoryBean.setDesc(application.getDesc() == null ? "" : application.getDesc());
		categoryBean.setName(application.getName());
		categoryBean.setDisplayName(application.getName());
		categoryBean.setParentId(categoryId);
		categoryBean.setCategoryStatus(Category.STATUS_NORMAL);
		categoryDao.saveOrUpdateCategory(categoryBean);

		application.setCategoryId(categoryBean.getId());
		if(application.getId() == null || application.getId() <= 0){
			genericDao.save(application);
		}
	}

	public void createApplication_v2(Application application) {
		if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_app_m")){
			throw new PermissionsException("无应用管理权限");
		}
		this.checkApplicationPerm();
		List<Category> c = categoryDao.getCategoriesByName("#application");
		long categoryId = c.get(0).getId();

		//判断应用分类的总容量是不是null
		Category cg=c.get(0);
		if(cg.getTotalCapacity()==null || cg.getTotalCapacity()<=0)
			throw new GroupsException("请先为应用分类配置容量");
		//判断应用分类的总容量够不够用
		Long capacity=Long.parseLong(application.getTotalSpace());
		Long avaiableca=cg.getAvailableCapacity();
		if(capacity>avaiableca)
			throw new GroupsException("应用分类可用容量不足");
		// 创建分类
		Category categoryBean = new Category();
		categoryBean.setCreateDate(new Timestamp(System.currentTimeMillis()));
		categoryBean.setDesc(application.getDesc() == null ? "" : application.getDesc());
		categoryBean.setName(application.getName());
		categoryBean.setDisplayName(application.getName());
		categoryBean.setParentId(categoryId);
		categoryBean.setCategoryStatus(Category.STATUS_NORMAL);
		//设置可用容量和总容量
		categoryBean.setAvailableCapacity(capacity);
		categoryBean.setTotalCapacity(capacity);
		categoryDao.saveOrUpdateCategory(categoryBean);
		//更新父分类“应用”分类的可用容
		cg.setAvailableCapacity(avaiableca-capacity);
		categoryDao.saveOrUpdateCategory(cg);

		application.setCategoryId(categoryBean.getId());
		if(application.getId() == null || application.getId() <= 0){
			genericDao.save(application);
		}
	}
	/**
	 * 根据appid删除app用户
	 * @param appid
	 */
	public void deleteAppMemberByApp(long appid) {
		if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_app_m")){
			throw new PermissionsException("无应用管理权限");
		}
		this.checkApplicationPerm();
		List<AppMember> am = genericDao.findAll("from AppMember a where a.applicationId = ?1 ", appid);
		if (am != null || am.size() > 0) {
			for (AppMember a : am) {
				genericDao.delete(a);
			}
		}
	}

	/**
	 * 删除应用和应用用户，应用根分类
	 * @param application
	 */
	public void deleteApplication(Application application) {
		if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_app_m")){
			throw new PermissionsException("无应用管理权限");
		}
		this.checkAdminPerm();
		Application app = genericDao.get(Application.class, application.getId());
		genericDao.delete(app);

		// 删除应用相应的用户关系
		this.deleteAppMemberByApp(app.getId());

		// 删除应用分类
		long cid = app.getCategoryId();
		Category c =categoryDao.getCategoryById(cid);
		categoryDao.deleteCategory(c);
	}

	/**
	 * 删除应用和应用用户，应用根分类
	 * @param application
	 */
	public void deleteApplication_v2(Application application) {
		if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_app_m")){
			throw new PermissionsException("无应用管理权限");
		}
		this.checkAdminPerm();
		Application app = genericDao.get(Application.class, application.getId());
		genericDao.delete(app);

		// 删除应用相应的用户关系
		this.deleteAppMemberByApp(app.getId());

		// 删除应用分类
		long cid = app.getCategoryId();
		Category c =categoryDao.getCategoryById(cid);
		//同步
//		if(c.getAvailableCapacity()!=null&&c.getAvailableCapacity()==Long.parseLong(app.getAvailableSpace())){
		List<Category> cs = categoryDao.getCategoriesByName("#application");
		Category cg=cs.get(0);
		//更新"应用"分类的可用容量
		cg.setAvailableCapacity(cg.getAvailableCapacity()+Long.parseLong(app.getTotalSpace()));
		categoryDao.saveOrUpdateCategory(cg);
//		}
		categoryDao.deleteCategory(c);
	}
	/**
	 * 更新应用，应用根分类
	 * @param application
	 */
	public void modifyApplication(Application application) {
		if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_app_m")){
			throw new PermissionsException("无应用管理权限");
		}
		if(application.getId() != null && application.getId() > 0){
			genericDao.update(application);
		} 
		long cid = application.getCategoryId();
		Category category = categoryDao.getCategoryById(cid);
		category.setName(application.getName());
		category.setDesc(application.getDesc());
		categoryDao.saveOrUpdateCategory(category);
	}

	public void modifyApplication_v2(Application application,String space) {
		if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_app_m")){
			throw new PermissionsException("无应用管理权限");
		}
		List<Category> c = categoryDao.getCategoriesByName("#application");
		Category cg=c.get(0);
		if(cg.getTotalCapacity()==null || cg.getTotalCapacity()<=0)
			throw new GroupsException("请先为应用分类配置容量");

		//修改之前的总容量
		//已经使用的容量
		long usecapacity = Long.parseLong(application.getTotalSpace()) - Long.parseLong(application.getAvailableSpace());
		long capacitycurrent = Long.parseLong(application.getTotalSpace());

		long cid = application.getCategoryId();
		//应用关联的分类
		Category category = categoryDao.getCategoryById(cid);
		category.setName(application.getName());
		category.setDesc(application.getDesc());
		//编辑分类
//		if(category.getAvailableCapacity()!=null&&category.getAvailableCapacity()==Long.parseLong(application.getAvailableSpace())&&
//				category.getTotalCapacity()==Long.parseLong(application.getTotalSpace())){
	  if(Long.parseLong(space)>=capacitycurrent){//增加容量
		if(cg.getAvailableCapacity()>=(Long.parseLong(space)-capacitycurrent)){
			cg.setAvailableCapacity(cg.getAvailableCapacity()-(Long.parseLong(space)-capacitycurrent));
			category.setAvailableCapacity(category.getAvailableCapacity()+(Long.parseLong(space)-capacitycurrent));
			category.setTotalCapacity(Long.parseLong(space));
			categoryDao.saveOrUpdateCategory(category);
			categoryDao.saveOrUpdateCategory(cg);
		}else
			throw new GroupsException("应用分类可用容量不足");
	  }else {//减少容量
		  cg.setAvailableCapacity(cg.getAvailableCapacity()+(capacitycurrent-Long.parseLong(space)));
		  category.setAvailableCapacity(category.getAvailableCapacity()-(capacitycurrent)+Long.parseLong(space));
		  category.setTotalCapacity(Long.parseLong(space));
		  categoryDao.saveOrUpdateCategory(category);
		  categoryDao.saveOrUpdateCategory(cg);
	  }
//		}else {//不同步
//            //对应用的总容量进行编辑
//			if(Long.parseLong(space)>=capacitycurrent){//增加容量
//				if(cg.getAvailableCapacity()>=Long.parseLong(space)){
//					cg.setAvailableCapacity(cg.getAvailableCapacity()-Long.parseLong(space));
//					category.setAvailableCapacity(Long.parseLong(space)-usecapacity);
//					category.setTotalCapacity(Long.parseLong(space));
//					categoryDao.saveOrUpdateCategory(category);
//					categoryDao.saveOrUpdateCategory(cg);
//				}else
//					throw new GroupsException("应用分类可用容量不足");
//		    }
//		}
		if(application.getId() != null && application.getId() > 0) {

			long capacityavailable = Long.parseLong(space) - usecapacity;
			if (space != null&& Long.parseLong(space)>=usecapacity) {
				application.setAvailableSpace(String.valueOf(capacityavailable));
				application.setTotalSpace(space);
			}
			else
				throw new GroupsException("应用的总容量不能小于已使用容量");
			genericDao.update(application);

		}

	}
	/**
	 * 根据appid和memberId查找应用用户
	 * @param mid
	 * @param appid
	 * @return
	 */
	public AppMember getAppMemberByMemberAndApp(long mid,long appid) {
		if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_app_m")){
			throw new PermissionsException("无应用管理权限");
		}
		return genericDao.findFirst("from AppMember where memberId = ?1 and applicationId = ?2", mid, appid);
	}
	
	/**
	 * 新建或或者修改应用用户
	 * @param appMember
	 */
	public void saveOrUpdateAppMember(AppMember appMember) {
		if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_app_m")){
			throw new PermissionsException("无应用管理权限");
		}
		if(appMember.getId() != null && appMember.getId() > 0){
			genericDao.update(appMember);
		} else {
			genericDao.save(appMember);
		}
	}

	/**
	 * 添加用户到应用中,对柜子和分类的赋权放到controller
	 * @param mids memberId 
	 * @param appId applicationId
	 * @param isManager 是否是应用管理员
	 */
	public void addMembersToApplication(Long[] mids, long appId, boolean isManager) {
		if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_app_m")){
			throw new PermissionsException("无应用管理权限");
		}
		this.checkAppIfExist(appId);
		Timestamp now = new Timestamp(System.currentTimeMillis());
		for (long id : mids) {
			if (id <= 0) {
				continue;
			}
			this.checkMemberIfExist(id);
			AppMember bean = this.getAppMemberByMemberAndApp(id, appId);
			if (bean == null || bean.getId() <= 0) {
				bean = new AppMember();
				bean.setMemberId(id);
				bean.setApplicationId(appId);
				bean.setCreateDate(now);
			}
			if (isManager == true) {
				bean.setIsManager(1);
			} else{
				bean.setIsManager(0);
			}
			this.saveOrUpdateAppMember(bean);
		}
	}

	/**
	 * 只做数据库处理
	 * 权限放到controller处理
	 * @param mid
	 * @param appId
	 * @param isManager
	 */
	public void modifyMemberinApplication(long mid, long appId, boolean isManager) {
		if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_app_m")){
			throw new PermissionsException("无应用管理权限");
		}
		this.checkAppIfExist(appId);
		this.checkMemberIfExist(mid);
		AppMember appMemberBean = this.getAppMemberByMemberAndApp(mid, appId);
		if (appMemberBean != null && appMemberBean.getId() > 0) {
			if (isManager == true) {
				appMemberBean.setIsManager(1);
			} else {
				appMemberBean.setIsManager(0);
			}
			this.saveOrUpdateAppMember(appMemberBean);
		}
	}
	/**
	 * 删除应用用户，只做数据库处理
	 * 权限放到controller处理
	 * @param mids
	 * @param appId
	 */
	public void deletMemberFromApplication(Long[] mids, long appId) {
		if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_app_m")){
			throw new PermissionsException("无应用管理权限");
		}
		for (long id : mids) {
			AppMember appMember = this.getAppMemberByMemberAndApp(id, appId);
			genericDao.delete(appMember);
		}
	}
			 
	/**
	 * 
	 * 根据应用id获取所有加入应用的member
	 * @param appId 应用id
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<Member> getMemberByApplication(long appId ,int start, int limit) {
		if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_app_m")){
			throw new PermissionsException("无应用管理权限");
		}
		List<AppMember> list = genericDao.findAll(start, limit, "from AppMember where applicationId = ?1", appId);
		List<Member> members = new ArrayList<Member>();
		if (list != null && list.size() > 0) {
			for (AppMember am : list) {
				Member member = memberDao.getMemberById(am.getMemberId());
				members.add(member);
			}
		}
		return members;
	}
			 
	/**
	 * 根据应用id获取所有属于应用的柜子
	 * @param appId
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<Group> getGroupbyApplication(long appId, int start, int limit) {
		if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_app_m")){
			throw new PermissionsException("无应用管理权限");
		}
		return groupDao.getGroupsByApp(appId, start, limit);
	}
			 
	/**
	 * 获取所有应用
	 * @return
	 */
	public List<Application> getAllApplications() {
		if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_app_m")){
			throw new PermissionsException("无应用管理权限");
		}
		return genericDao.findAll("from Application order by name asc");
	}
			 

	/**
	 * 根据id获取应用
	 * @param applicationId
	 * @return
	 */
	public Application getApplicationById(long applicationId) {
		return genericDao.get(Application.class, applicationId);
	}

	/**
	根据分类id获取应用
	 */
	public Application getApplicaitonByCategoryId(long categoryId){
		if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_app_m")){
			throw new PermissionsException("无应用管理权限");
		}
		String hql="from "+Application.class.getName()+" as app where app.categoryId=?";
		List<Application> result= genericDao.findAll(hql,categoryId);
		if(result!=null && result.size()!=0)
		  return result.get(0);
		else
			return null;
	}

	/**
	 *通过域Id来查找应用，看域是否与应用有关联
	 * @param domainId
	 */
	public Application getApplicationByDomain(Long domainId){
		if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_app_m")){
			throw new PermissionsException("无应用管理权限");
		}
		String  hql="from "+Application.class.getName()+" as app where app.domain.id=?";
		List<Application> result= genericDao.findAll(hql,domainId);
		if(result!=null && result.size()!=0)
			return result.get(0);
		else
			return null;
	}


//
//	public void addGroupToApplication(long[] gIds, long appId)  
//
//	public void removeGroupFromApplication(long[] gIds, long appId)
//			 
//	
	public List<Application> getApplicationByMember(long mid, int start, int limit, boolean isManager) {
		String hql_applicationId;
		if (isManager == true) {
			hql_applicationId = "SELECT applicationId FROM "
					+ AppMember.class.getName()
					+ " WHERE memberId = " + mid + "AND isManager = 1";
		} else {
			hql_applicationId = "SELECT applicationId FROM "
					+ AppMember.class.getName()
					+ " WHERE memberId = " + mid;
		}
		
		
		String hql = "from " + Application.class.getName()
				+ " where id in (:app) ";

		List<Object> result_applicationId = genericDao
				.findAll(hql_applicationId);

		if (result_applicationId.isEmpty()) return null;
		
		Query query = genericDao.createQuery(hql);
		query.setParameter("app", result_applicationId);
		if (start > 0)
			query.setFirstResult(0);
		if (limit > 0)
			query.setMaxResults(limit);
		List<Application> result = query.getResultList();
		return result;
	}
//	
//	public long getApplicationCountByMember(long mid)  
//	
//	public long getApplicationsCount()  
//	
	public boolean isApplicationAdmin(long id) {
		List<AppMember> beans = null;
		try {
			beans = this.getAppMemberByMember(id);
		} catch (PermissionsException ex) {
			return false;
		}
		if (beans != null) {
			for (AppMember bean : beans) {
				if (bean.getIsManager() == 1)
					return true;
			}
		}
		return false;
	}
	
	/**
	 * 根据memberId查询appMember
	 * @param mid
	 * @return
	 */
	public List<AppMember> getAppMemberByMember(long mid) {	
		if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_app_m")){
			throw new PermissionsException("无应用管理权限");
		}
		return genericDao.findAll("from AppMember where memberId = ?1", mid);
	}
	
	/**
	 * 用户是否是应用管理员
	 * @param mid memberId
	 * @param aid applicationId
	 * @return
	 */
	public boolean isApplicationAdmin(long mid, long aid) {
		List<AppMember> beans = this.getAppMemberByMember(mid);
		for (AppMember bean : beans) {
			if (bean.getIsManager() == 1 && bean.getApplicationId() == aid)
				return true;
		}
		return false;
	}
	
	public void checkAppIfExist(long appId) throws GroupsException {
		Application app = genericDao.get(Application.class, appId);
		if (app == null || app.getId() <= 0) {
			throw new GroupsException("应用不存在！");
		}
	}

	public void checkMemberIfExist(long mId) throws GroupsException {
		Member m = memberDao.getMemberById(mId);
		if (m == null || m.getId() <= 0) {
			throw new GroupsException("用户不存在！");
		}
	}
	
	/**
	 * 当前用户是否是管理员和创分类权限
	 * @throws PermissionsException
	 */
	private void checkApplicationPerm() throws PermissionsException {
		if (!permission.isAdmin(UserUtils.getCurrentMemberId())) {
			throw PermissionsException.ApplicationException;
		}
		if (!permission.hasGlobalPerm(UserUtils.getCurrentMemberId(),
				GlobalPerm.CREATE_CATEGORY)) {
				throw PermissionsException.CategoryException;
		}
	}
	
	/**
	 * 当前用户是否管理员
	 * @throws PermissionsException
	 */
	private void checkAdminPerm() throws PermissionsException {
		if (!permission.isAdmin(UserUtils.getCurrentMemberId())) {
			throw PermissionsException.ApplicationException;
		}
	}

}
