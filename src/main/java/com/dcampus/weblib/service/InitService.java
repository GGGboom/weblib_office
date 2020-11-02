package com.dcampus.weblib.service;

import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dcampus.common.generic.GenericDao;
import com.dcampus.sys.util.UserUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dcampus.common.config.PropertyUtil;
import com.dcampus.common.config.ResourceProperty;
import com.dcampus.common.util.Log;
import com.dcampus.common.util.SpringApplicationContextHelper;
import com.dcampus.sys.entity.User;
import com.dcampus.sys.service.UserService;
import com.dcampus.weblib.dao.MemberDao;
import com.dcampus.weblib.entity.Admin;
import com.dcampus.weblib.entity.Category;
import com.dcampus.weblib.entity.Global;
import com.dcampus.weblib.entity.GroupMemberBinding;
import com.dcampus.weblib.entity.GroupType;
import com.dcampus.weblib.entity.Member;
import com.dcampus.weblib.entity.OldPerm;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.util.adaptor.ITeamAdaptor;
import com.dcampus.weblib.util.userutil.UserRemoteServiceUtil;
import com.dcampus.weblib.util.userutil.models.RemoteItems;
import com.dcampus.weblib.util.userutil.models.RemoteUser;


/**
 * 所有的关于初始化用到的方法
 * 由于事物的特性在这里包装
 * @author patrick
 *
 */
@Service
@Transactional(readOnly = false)
public class InitService {
	private Log log = Log.getLog(InitService.class);
	@Autowired
	private GroupService groupService;
	@Autowired
	private UserService userService;
	@Autowired
	private GrouperService grouperService;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private PermissionService permService;
	@Autowired
	private GlobalService globalService;

	@Autowired
	private GenericDao genericDao;
	
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public void initGrouper() {
		log.info("creating buildin folder and teams");
		// 创建默认组织
		Member folder = grouperService.createFolder(0, PropertyUtil.getSystemDefaultOrgName(),OldPerm.SYSTEMADMIN_MEMBER_ID);
		log.info("finish creating folder ["+PropertyUtil.getSystemDefaultOrgName()+"]");
		
		String [] defaultTeam =PropertyUtil.getSystemDefaultManageTeamName().split(";");
		// 创建管理员分组
    	Member admin = grouperService.createTeam(folder.getId(), defaultTeam[0],false, OldPerm.SYSTEMADMIN_MEMBER_ID);
		//initService.createTeamAnd(folder.getId(), defaultTeam[0],false);
		// 添加为admin
		List<Admin> adminBeans = userService.getAdmins();
		if (!find(admin, adminBeans)) {
			Admin adminBean = new Admin();
			adminBean.setCreateDate(new Timestamp(System.currentTimeMillis()));
			adminBean.setMember(admin);
			adminBean.setType(Admin.NORMAL_ADMIN);
			userService.createAdmin(adminBean,  OldPerm.SYSTEMADMIN_MEMBER_ID);
		}

		log.info("finish creating team [管理员]");

		Member everyone = grouperService.createTeam(folder.getId(), defaultTeam[1],false, OldPerm.SYSTEMADMIN_MEMBER_ID);
		PropertyUtil.EVERYONE_TEAM_ID = everyone.getId();
		
		
		
		// 标记已经初始化过了
		Global bean = globalService.getGlobalConfig();
		bean.setInit(true);
		globalService.modifyGlobal(bean);
	}

	private boolean find(Member bean, List<Admin> beans) {
		for (Admin adminBean : beans) {
			if (bean.getId() == adminBean.getMember().getId())
				return true;
		}
		return false;
	}
	
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public void createSuperAdmin() {
		// 创建super admin用户
		String[] superAdmins = PropertyUtil.getSuperAdmin();
		for (String superAdmin : superAdmins) {
			String[] admin = superAdmin.split(":");
			String account = admin[0];
			String name = admin[1];
			this.createSuperAdmin(account, name);
		}

	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public void createSuperAdmin(String account, String name) {
		// 创建super admin用户
		User admin = userService.getUserByAccount(account);
		if (admin == null || admin.getId() <= 0) {
			// 未有该本地帐户，添加该本地帐户
			User bean = new User();
			bean.setAccount(account);
			bean.setPassword("ccnl@123");
			bean.setUserbaseStatus(User.USER_STATUS_NORMAL);
			userService.createAccountWithoutLog(bean);
		}

		Member superBean = null;
		List<Member> beans = grouperService.getMembersByAccount(account);
		for (Member bean : beans) {
			if (name.equals(bean.getName())) {
				superBean = bean;
				break;
			}
		}

		if (superBean == null) {
			// 未找到指定为超级管理员的用户，创建一个用户
			Member memberBean = new Member();
			memberBean.setAccount(account);
			memberBean
					.setEmail(account
							+ (StringUtils
									.isEmpty(PropertyUtil.getMailSuffix()) ? "@scut.edu.cn"
									: PropertyUtil.getMailSuffix()));
			memberBean.setName(name);
			memberBean.setMemberStatus(Member.STATUS_NORMAL);
			memberBean.setMemberType(Member.MEMBER_TYPE_PERSON);
			grouperService.createMemberWithoutLog(memberBean);
		}

		// 登记superBean用户为超级管理员
		Member memberBean = grouperService.getMemberByNameAndAccount(name,account, Member.MEMBER_TYPE_PERSON);
		Admin bean = userService.getAdminByMember(memberBean.getId());
		if (bean == null || bean.getId() <= 0) {
			bean = new Admin();
			bean.setCreateDate(new Timestamp(System.currentTimeMillis()));
			bean.setMember(memberBean);
			bean.setType(Admin.SUPER_ADMIN);
			userService.createAdmin(bean, OldPerm.SYSTEMADMIN_MEMBER_ID);
		}
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public void initGlobalProperties() {
		Global globalConfig = globalService.getGlobalConfig();
		if (globalConfig == null || globalConfig.getId() <= 0 ) {
			// 创建全局配置
			Global bean = new Global();
			bean.setSiteAuditGroup(true);
			bean.setGlobalSiteStatus(Global.STATUS_NORMAL);
			bean.setSiteCloseReason("站点正在维护中...");
			bean.setSiteDomain("www.groups.com");
			bean.setSiteName("Groups");
			bean.setSmtpAuth(true);
			bean.setSmtpHost("smtp-host");
			bean.setSmtpPassword("admin");
			bean.setSmtpSender("admin@admin.com");
			bean.setSmtpUsername("admin");
			bean.setSmtpPort(25);
			bean.setInit(false);
			bean.setWeblib_download_log(false);
			bean.setWeblib_login_log(true);
			bean.setWeblib_operate_log(true);
			bean.setWeblib_upload_log(false);
			bean.setWeblib_error_log(true);
			Long allCapacity=PropertyUtil.getTotalStorageSpaceMaxLimit()*1024*1024;
			bean.setTotalStorageCapacity(allCapacity);
			Long used=globalService.getAllGroupusedCapacity();
			bean.setAvailableStorageCapacity(allCapacity-used);
			globalService.createGlobal(bean);
		}else if(globalConfig.getWeblib_download()==null || globalConfig.getWeblib_download().equalsIgnoreCase("")){
			globalConfig.setWeblib_download_log(false);
			globalConfig.setWeblib_login_log(true);
			globalConfig.setWeblib_operate_log(true);
			globalConfig.setWeblib_upload_log(false);
			globalConfig.setWeblib_error_log(true);
			genericDao.update(globalConfig);
		}
		if(globalConfig!=null&&globalConfig.getId()>0){
			Long allCapacity=PropertyUtil.getTotalStorageSpaceMaxLimit()*1024*1024;
			globalConfig.setTotalStorageCapacity(allCapacity);
			Long used=globalService.getAllGroupusedCapacity();
			globalConfig.setAvailableStorageCapacity(allCapacity-used);
			genericDao.update(globalConfig);
		}

	}

//	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
//	public void initBuildInGroupTypes() {
//		String[] names = PropertyUtil.getBuildinGroupType();
//		for (String name : names) {
//			if (name.trim().length() == 0)
//				continue;
//
//			GroupType bean = groupService.getGroupTypeByName(name);
//			if(bean == null || bean.getId() <= 0) {
//				bean = new GroupType();
//				bean.setMailOnPost(false);
//				bean.setName(name);
//			}
//			if (name.equals(PropertyUtil.getPersonalGroupType())) {
//				bean.setTotalFileSize(PropertyUtil.getPersonalGroupSpaceLimit());
//			} else {
//				bean.setTotalFileSize(PropertyUtil.getGroupResourceSize());
//			}
//			bean.setSingleFileSize(PropertyUtil
//					.getGroupResourceSingleFileSize());
//			bean.setGroupType(GroupType.GROUP_TYPE_BUILDIN);
//			groupService.saveOrUpdateGroupType(bean, OldPerm.SYSTEMADMIN_MEMBER_ID);
//		}
//	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public void initBuildInGroupTypes() {
		String[] names = PropertyUtil.getBuildinGroupType();
		for (String name : names) {
			if (name.trim().length() == 0)
				continue;
			GroupType bean = groupService.getGroupTypeByName(name);
			if(bean == null || bean.getId() <= 0) {
				bean = new GroupType();
				bean.setMailOnPost(false);
				bean.setName(name);
				if (name.equals(PropertyUtil.getPersonalGroupType())) {
					bean.setTotalFileSize(PropertyUtil.getPersonalGroupSpaceLimit());
				} else {
					bean.setTotalFileSize(PropertyUtil.getGroupResourceSize());
				}
				bean.setSingleFileSize((long)10*1024*1024);
				bean.setGroupType(GroupType.GROUP_TYPE_BUILDIN);
				bean.setLastModified(new Date());
				groupService.saveOrUpdateGroupType(bean, OldPerm.SYSTEMADMIN_MEMBER_ID);
			}
		}
	}
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public void initDefaultGroupCategory() {
		String[] categories = PropertyUtil.getDefaultGroupCategory();
		for (int i = 0; i < categories.length; i++) {
			String[] names = categories[i].split(":");
			
			List<Category> categoryBeans = categoryService.getCategoriesByName(names[0]);
			Category categoryBean = null;

			if (categoryBeans == null || categoryBeans.size() == 0) {
				categoryBean = new Category();
				categoryBean.setName(names[0]);
				categoryBean.setDisplayName(names[1]);
				categoryBean.setCreateDate(new Timestamp(System
						.currentTimeMillis()));
				categoryBean.setCategoryStatus(Category.STATUS_NORMAL);
				//设置新加字段的值
				categoryBean.setTotalCapacity(1L);
				categoryBean.setAvailableCapacity(1L);
				categoryBean.setCreatorName(new Member(UserUtils.getCurrentMemberId()).getName());
				categoryService.createCategory4Init(categoryBean, false, OldPerm.SYSTEMADMIN_MEMBER_ID);
			} else {
				categoryBean = categoryBeans.get(0);
				if (!names[1].equals(categoryBean.getDisplayName())) {
					categoryService.modifyCategory4Init(categoryBean.getId(),
							names[1], OldPerm.SYSTEMADMIN_MEMBER_ID);
				}
			}
		}
	}
}
