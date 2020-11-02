package com.dcampus.weblib.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.persistence.Query;

import com.dcampus.weblib.entity.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.ArrayUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dcampus.common.config.LogMessage;
import com.dcampus.common.config.PropertyUtil;
import com.dcampus.common.config.ResourceProperty;
import com.dcampus.common.generic.GenericDao;
import com.dcampus.common.util.CacheUtil;
import com.dcampus.common.util.SpringApplicationContextHelper;
import com.dcampus.sys.entity.User;
import com.dcampus.sys.service.UserService;
import com.dcampus.sys.util.UserUtils;
import com.dcampus.weblib.dao.AdminDao;
import com.dcampus.weblib.dao.GroupDao;
import com.dcampus.weblib.dao.GroupManagerDao;
import com.dcampus.weblib.dao.MemberDao;
import com.dcampus.weblib.dao.OldPermDao;
import com.dcampus.weblib.exception.PermissionsException;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.service.permission.IPermission.GroupPerm;
import com.dcampus.weblib.service.permission.RolePermissionUtils;
import com.dcampus.weblib.service.permission.impl.Permission;
import com.dcampus.weblib.util.FileUtil;
import com.dcampus.weblib.util.SheetWriter;
import com.dcampus.weblib.util.adaptor.ITeamAdaptor;
import com.dcampus.weblib.util.userutil.UserRemoteServiceUtil;
import com.dcampus.weblib.util.userutil.models.RemoteItems;
import com.dcampus.weblib.util.userutil.models.RemoteUser;


/**
 * 用户，用户组，组织相关的service
 * member表中的person, team, folder
 * @author patrick
 *
 */
@Service
@Transactional(readOnly = false)
public class GrouperService {
	
	@Autowired
	private MemberDao memberDao;
	
	@Autowired
	private GenericDao genericDao;
	@Autowired
	private GroupManagerDao groupManagerDao;
	@Autowired
	private GroupDao groupDao;
	@Autowired
	private OldPermDao oldPermDao;
	@Autowired
	private AdminDao adminDao;
	@Autowired
	private UserService userService;

	@Autowired
	@Lazy
	private GroupService groupService;
	@Autowired
	private LogService logService;
	
	@Autowired
	private Permission permission;

	
	
	
	private ApplicationContext context = SpringApplicationContextHelper
			.getInstance().getApplicationContext();
	
	/**
	 * group中所有member的id
	 * @param groupId
	 * @param start
	 * @param limit
	 * @return
	 */
	public long[] getMembersInGroup(long groupId, int start, int limit) {
		this.checkViewGroupPerm(groupId);
		List<GroupMemberBinding> bindings = memberDao.getBingdingsByGroup(groupId, start, limit);
		int length = bindings.size();
		long[] memberIds = new long[length];
		for (int i = 0; i <length; ++i) {
			memberIds[i] = bindings.get(i).getMemberId();
		}
		return memberIds;
	}
	
	/**
	 * member所在的group的id
	 * 
	 * @param memberId
	 * @return
	 */
	public long[] getGroupsForMember(final long memberId) {
		List<GroupMemberBinding> bindings = memberDao.getBingdingsByMember(memberId);
		long[] groupIds = new long[bindings.size()];
		for (int i = 0; i < bindings.size(); ++i) {
			groupIds[i] = bindings.get(i).getGroupId();
		}
		return groupIds;
	}


	/**
	 * 创建小组分区
	 * 
	 * @param id
	 *            父路径Id
	 * @param name
	 *            路径简称
	 */
	public Member createFolder(long id, String name, long memberId, String from) {
		//当id为-1（即虚拟用户根节点），将id设置为0；
		if (id == -1) {
			id = 0;
		}
		this.checkAdminPerm(memberId);
		this.checkDefaultFolder(id);
		String parentId = null;
		if (id != 0) {
			Member iMemberBean = this.getFolder(id);
			if (!iMemberBean.getMemberType().equals(Member.MEMBER_TYPE_FOLDER)) {
				return null;
			}
			parentId = iMemberBean.getName();
		}

		String folderId = null;
		folderId = UserRemoteServiceUtil.stemSave(name, "", parentId);

		// 将folder作为用户添加到用户表中
		Member iMemberBean = mapping(folderId, name, Member.MEMBER_TYPE_FOLDER);
		if (from != null) {
			iMemberBean.setModifyFrom(from);
			memberDao.saveOrUpdateMember(iMemberBean);
		}
		return iMemberBean;
	}
	
	public Member createFolder(long id, String name, long memberId) {
		return this.createFolder(id, name, memberId, null);
	}
	
	public Member createFolder(long id, String name) {
		//当id为-1（即虚拟用户根节点），将id设置为0；
		if (id == -1) {
			id = 0;
		}
		long memberId = UserUtils.getCurrentMemberId();
		return this.createFolder(id, name, memberId,null);
	}
	
	public Member createFolder(long id, String name, String from) {
		//当id为-1（即虚拟用户根节点），将id设置为0；
		if (id == -1) {
			id = 0;
		}
		long memberId = UserUtils.getCurrentMemberId();
		return this.createFolder(id, name, memberId,from);
	}

	private Member mapping(String id, String name, String type) {
		Member member = null;
		member = memberDao.getMemberByNameAccount(id, id, type);
		if (member == null) {
			Member bean = new Member();
			bean.setAccount(id);
			bean.setName(id);
			bean.setMemberStatus(Member.STATUS_NORMAL);
			bean.setMemberType(type);
			bean.setSignature(name);
			memberDao.saveOrUpdateMember(bean);
			member = bean;
		} else {
			member.setSignature(name);
			memberDao.saveOrUpdateMember(member);
		}
		return member;
	}

	/**
	 * 创建member用户
	 * @param memberBean
	 */
	public void createMember(Member memberBean) {
		memberBean.setMemberType(Member.MEMBER_TYPE_PERSON);
		Member temp = memberDao.getMemberByNameAccount(memberBean.getName(), memberBean.getAccount(), Member.MEMBER_TYPE_PERSON);
		if (temp == null || temp.getId() <0) {
			memberDao.saveOrUpdateMember(memberBean);
//			// 记录日志
//			String desc = LogMessage.getMemberAdd(memberBean.getName());
//			logService.addOperateLog(Log.ACTION_TYPE_ADD, desc, memberBean.getId(), memberBean.getName());
		}
		
	}
	
	/**
	 * 创建member用户,初始化系统用
	 * @param memberBean
	 */
	public void createMemberWithoutLog(Member memberBean) {
		memberBean.setMemberType(Member.MEMBER_TYPE_PERSON);
		Member temp = memberDao.getMemberByNameAccount(memberBean.getName(), memberBean.getAccount(), Member.MEMBER_TYPE_PERSON);
		if (temp == null || temp.getId() <0) {
			memberDao.saveOrUpdateMember(memberBean);
		}
		
	}

	/**
	 * 创建小组
	 * 
	 * @param id
	 *            父路径id
	 * @param name
	 *            小组简称
	 * @param mark
	 * @return
	 */
	public Member createTeam(long id, String name, boolean mark) {
		long memberId =UserUtils.getCurrentMemberId();
		return this.createTeam(id, name, mark, memberId);
	}
	public Member createTeam(long id, String name, boolean mark, long memberId) {
		return this.createTeam(id, name, mark, memberId, false);
	}
	
	public Member createTeam(long id, String name, boolean mark, boolean rename) {
		long memberId =UserUtils.getCurrentMemberId();
		return this.createTeam(id, name, mark, memberId, rename);
	}

	/**
	 * 创建小组
	 * 
	 * @param id
	 *            父路径id
	 * @param name
	 *            小组简称
	 * @param mark 是否检查是系统内建默认组
	 * @param rename 是否重命名
	 * @return
	 */
	public Member createTeam(long id, String name, boolean mark,  boolean rename, String from) {
		long memberId = UserUtils.getCurrentMemberId();
		this.checkAdminPerm(memberId);
		if (mark == true) {
			this.checkDefaultFolder(id);
		}
		Member iMemberBean = this.getFolder(id, memberId);// member表
		if (!iMemberBean.getMemberType().equals(Member.MEMBER_TYPE_FOLDER)) {
			return null;
		}

		String folderId = null;
		folderId = UserRemoteServiceUtil.groupSave(name, "", memberDao
				.getMemberById(id).getAccount(), rename);

		// 将folder作为用户添加到用户表中
		Member iMemberBeanTeam = mapping(folderId, name,
				Member.MEMBER_TYPE_TEAM);
		if (from != null) {
			iMemberBeanTeam.setModifyFrom(from);
			memberDao.saveOrUpdateMember(iMemberBeanTeam);
		}
		return iMemberBeanTeam;
	}
	
	public Member createTeam(long id, String name, boolean mark, long memberId, boolean rename) {
		this.checkAdminPerm(memberId);
		if (mark == true) {
			this.checkDefaultFolder(id);
		}
		Member iMemberBean = this.getFolder(id, memberId);// member表
		if (!iMemberBean.getMemberType().equals(Member.MEMBER_TYPE_FOLDER)) {
			return null;
		}

		String folderId = null;
		folderId = UserRemoteServiceUtil.groupSave(name, "", memberDao
				.getMemberById(id).getAccount(), rename);

		// 将folder作为用户添加到用户表中
		Member iMemberBeanTeam = mapping(folderId, name,
				Member.MEMBER_TYPE_TEAM);
		return iMemberBeanTeam;
	}

	/**
	 * 删除小组分区 需要记录日志
	 * 
	 * @param id
	 */
	public boolean deleteTeam(long id) {
		this.checkAdminPerm();
		this.checkDefaultTeam(id);
		Member bean = memberDao.getMemberById(id);
		String name = bean.getName();
		long memberId = bean.getId();
		long[] groupIds = this.getGroupsForMember(memberId);
		int length = groupIds.length;
		for (int i = 0; i < length; i++) {
			memberDao.deleteMemberGroupBinding(memberId, groupIds[i]);
		}
		memberDao.deleteMember(bean);
		// 记录日志
		String desc = LogMessage.getTeamDelete(name);
		logService.addOperateLog(Log.ACTION_TYPE_DELETE, desc, id, name);
		
		// 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		CacheUtil.removeAll(CacheUtil.myVisualGroupCache);
		CacheUtil.removeAll(CacheUtil.memberTeamCache);
		return UserRemoteServiceUtil.groupDelete(name);
	}

	/**
	 * 
	 * 将用户添加到用户小组 需要记录日志
	 * 
	 * @param memberId
	 *            member中类型为person的id
	 * @param id
	 *            member中类型为team的id
	 * @return
	 * @throws GroupsException
	 */
	public boolean addMemberToTeam(long memberId, long id)
			throws GroupsException {
		Member memberBean = memberDao.getMemberById(memberId);
		if (!Member.MEMBER_TYPE_PERSON.equals(memberBean.getMemberType())) {
			// || userBaseBean.getStatus().equals(User.Status.DELETE))
			throw new GroupsException(
					ResourceProperty.getNoSuchAccountString());
		}
		Member iMemberBean = memberDao.getMemberById(id);
		String[] membersId = { memberBean.getAccount() };
		boolean mark = UserRemoteServiceUtil.addMember(membersId,
				iMemberBean.getName());
		
		// 记录日志
		String desc = LogMessage.getMemberAddToTeam(memberBean.getName(), (iMemberBean.getSignature()==null?"":iMemberBean.getSignature())+"("+iMemberBean.getName()+")");
		logService.addOperateLog(Log.ACTION_TYPE_ADD, desc, memberBean.getId(), memberBean.getName());
		
		// 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		CacheUtil.removeAll(CacheUtil.myVisualGroupCache);
		CacheUtil.removeAll(CacheUtil.memberTeamCache);
		return mark;
	}

	/**
	 * 获取小组中的所有用户
	 * 
	 * @param id
	 * @return
	 */
	public Member[] getMembersInTeam(long id) {
		Member iMemberBean = memberDao.getMemberById(id);
		Date ts = ((Member)iMemberBean).getLastModified();
		String key = id+":"+(ts==null?"":ts.getTime());//id:timestamp 作为key
		Member[] children = (Member[]) CacheUtil.getCache(CacheUtil.memberChildrenCache, key);
		if(children!=null) {
			return children;
		}
		RemoteItems group = UserRemoteServiceUtil.findGroup(iMemberBean
				.getName());
		RemoteUser[] users = UserRemoteServiceUtil.getGroupMembers(group
				.getId());
		if (users == null || users.length < 1)
			return new Member[0];
		Member[] memberBeans = new Member[users.length];
		int length = users.length;
		for (int i = 0; i < length; i++) {
			try {
				memberBeans[i] = memberDao.getMembersByName(users[i].getUsername())
						.get(0);
			} catch (java.lang.IndexOutOfBoundsException e) {
				Member bean = new Member();
				bean.setName(users[i].getUsername());
				memberBeans[i] = bean;
			}
		}
		CacheUtil.setCache(CacheUtil.memberChildrenCache, key, memberBeans);
		return memberBeans;
	}

	/**
	 * @author zf
	 * @param uuid
	 * @return
	 */
	public Member[] getMembersInTeam(String uuid) {
		RemoteUser[] users = UserRemoteServiceUtil.getGroupMembers(uuid);
		if (users == null || users.length < 1)
			return new Member[0];
		Member[] memberBeans = new Member[users.length];
		int length = users.length;
		for (int i = 0; i < length; i++) {
			try {
				memberBeans[i] = memberDao.getMembersByName(users[i].getUsername())
						.get(0);
			} catch (java.lang.ArrayIndexOutOfBoundsException e) {
				Member bean = new Member();
				bean.setName(users[i].getUsername());
				memberBeans[i] = bean;
			}
		}

		return memberBeans;
	}

	/**
	 * 获取一个用户所在的组
	 * 
	 * @param memberId
	 * @return
	 */
	public Member[] getTeamsOfMember(long memberId) {
		String key = ""+memberId;
		Member[] myTeams = (Member[]) CacheUtil.getCache(CacheUtil.memberTeamCache, key);
		if(myTeams!=null) {
			return myTeams;
		}
		RemoteItems[] groups = UserRemoteServiceUtil.getGroupsOfUser(memberDao
				.getMemberById(memberId).getAccount());
		int length = groups.length;
		Member[] memberBeans = new Member[length];
		for (int i = 0; i < length; i++) {
			memberBeans[i] = mapping(groups[i].getId(), groups[i].getName(),
					Member.MEMBER_TYPE_TEAM);
		}
		CacheUtil.setCache(CacheUtil.memberTeamCache, key,memberBeans);
		return memberBeans;
	}


	/**
	 * 获取用户小组信息
	 * 
	 * @param id
	 * @return
	 */
	public Member getTeam(long id) {
		Member iMemberBean = memberDao.getMemberById(id);
		RemoteItems group = UserRemoteServiceUtil.findGroup(iMemberBean
				.getName());

		if (iMemberBean.getMemberType().equals(Member.MEMBER_TYPE_TEAM)) {
			iMemberBean.setSignature(group.getFullname());
			return iMemberBean;
		}
		return null;
	}
	

	/**
	 * 删除用户组中的成员 
	 * 
	 * @param memberId
	 * @param id
	 */
	public boolean removeMemberFromTeam(long memberId, long id) {
		Member iMemberBean = memberDao.getMemberById(id);
		Member bean = memberDao.getMemberById(memberId);
		String desc = LogMessage.getMemberDeleteFromTeam((iMemberBean.getSignature()==null?"":iMemberBean.getSignature())+"("+iMemberBean.getName()+")", bean.getName());
		String[] member = new String[] { bean.getAccount() };
		boolean mark = UserRemoteServiceUtil.deleteMember(member,
				iMemberBean.getName());
		// 记录日志
		logService.addOperateLog(Log.ACTION_TYPE_DELETE, desc, memberId, bean.getName());
		
		// 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		CacheUtil.removeAll(CacheUtil.myVisualGroupCache);
		CacheUtil.removeAll(CacheUtil.memberTeamCache);
		return mark;
	}

	/**
	 * 复制用户小组 
	 * 
	 * @param id
	 *            小组Id
	 * @param destId
	 *            目标父路径Id
	 * @throws GroupsException
	 */
	public boolean copyTeam(long id, long destId) throws GroupsException {
		this.checkDefaultTeam(id);
		this.checkDefaultFolder(destId);
		Member iMemberBeanTeam = memberDao.getMemberById(id);
		Member iMemberBeanFolder = memberDao.getMemberById(destId);

		UserRemoteServiceUtil.groupCopy(iMemberBeanTeam.getName(),
				iMemberBeanFolder.getName());
		
		// 记录日志
		String desc = LogMessage.getTeamCopy(iMemberBeanTeam.getName(), iMemberBeanFolder.getName());
		logService.addOperateLog(Log.ACTION_TYPE_COPY, desc, id, iMemberBeanTeam.getName());
		
		// 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		CacheUtil.removeAll(CacheUtil.myVisualGroupCache);
		CacheUtil.removeAll(CacheUtil.memberTeamCache);
		return true;
	}

	/**
	 * 复制用户组织 需要记录日志
	 * 
	 * @param id
	 *            组织Id
	 * @param destId
	 *            目标父路径Id
	 * @throws GroupsException
	 */
	public boolean copyFolder(long id, long destId) throws GroupsException {
		this.checkDefaultFolder(destId);
		this.checkDefaultFolder(id);

		Member srcFolder = memberDao.getMemberById(id);
		Member destFolder = memberDao.getMemberById(destId);
		UserRemoteServiceUtil.stemCopy(srcFolder.getName(),
				destFolder.getName());
		
		//记录日志
		String desc ="复制用户组织"+srcFolder.getName()+"到组织"+destFolder.getName();
		logService.addOperateLog(Log.ACTION_TYPE_COPY, desc, id, srcFolder.getName());
		return true;
	}

	/**
	 * 移动用户小组 需要记录日志
	 * 
	 * @param id
	 *            小组Id
	 * @param destId
	 *            目标父路径Id
	 * @throws GroupsException
	 */
	public boolean moveTeam(long id, long destId) throws GroupsException {
		this.checkDefaultTeam(id);
		this.checkDefaultFolder(destId);
		Member iMemberBeanTeam = memberDao.getMemberById(id);
		Member iMemberBeanFolder = memberDao.getMemberById(destId);

		UserRemoteServiceUtil.groupMove(iMemberBeanTeam.getName(),
				iMemberBeanFolder.getName());
		
		// 记录日志
		String desc = LogMessage.getTeamMove(iMemberBeanTeam.getName(), iMemberBeanFolder.getName());
		logService.addOperateLog(Log.ACTION_TYPE_MOVE, desc, id, iMemberBeanTeam.getName());
		
		// 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		CacheUtil.removeAll(CacheUtil.myVisualGroupCache);
		CacheUtil.removeAll(CacheUtil.memberTeamCache);
		return true;
	}

	/**
	 * 移动用户组织
	 * 
	 * @param id
	 *            小组
	 * @param destId
	 *            目标父路径Id
	 * @return
	 * @throws GroupsException
	 */
	public boolean moveFolder(long id, long destId) throws GroupsException {
		this.checkDefaultFolder(destId);
		this.checkDefaultFolder(id);
		Member srcFolder = memberDao.getMemberById(id);
		Member destFolder = memberDao.getMemberById(destId);
		UserRemoteServiceUtil.stemMove(srcFolder.getName(),
				destFolder.getName());
		
		//记录日志
		String desc ="移动用户组织"+srcFolder.getName()+"到组织"+destFolder.getName();
		logService.addOperateLog(Log.ACTION_TYPE_MOVE, desc, id, srcFolder.getName());
		return true;
	}

	/**
	 * 获取小组分区的子分区列表
	 * 
	 * @param id
	 * @return
	 */
	public Member[] getFolderByParent(long id) {
		this.checkAdminAndCreateGroupPerm();
		return this.getFolderByParent(id, false);
	}

	/**
	 * 获取小组分区的子分区列表
	 * 
	 * @param id
	 * @return
	 */
	public Member[] getFolderByParent(long id, boolean withoutLeaf) {
		this.checkAdminAndCreateGroupPerm();
		RemoteItems[] stems = null;
		if (id == 0) {
			stems = UserRemoteServiceUtil.findStemsByParent(null, withoutLeaf);// 跳过叶子检测，提高效率
		} else {
			Member iMemberBean = memberDao.getMemberById(id);
			stems = UserRemoteServiceUtil.findStemsByParent(
					iMemberBean.getName(), withoutLeaf);// 跳过叶子检测，提高效率
		}

		Member[] memberBeans = new Member[stems.length];
		int length = stems.length;
		for (int i = 0; i < length; i++) {
			try {
				memberBeans[i] = mapping(stems[i].getId(), stems[i].getName(),
						Member.MEMBER_TYPE_FOLDER);
				memberBeans[i].setIsLeaf(stems[i].getIsLeaf());
			} catch (java.lang.NullPointerException e) {
				// ignore
			}
		}

		Arrays.sort(memberBeans, new Member());
		return memberBeans;
	}

	/**
	 * 获取用户小组所属分区信息
	 * 
	 * @param id
	 * @return
	 */
	public Member getFolder(long id) {
		long memberId = UserUtils.getCurrentMemberId();
		return this.getFolder(id, memberId);
	}
	
	public Member getFolder(long id, long memberId) {
		this.checkAdminAndCreateGroupPerm(memberId);
		Member iMemberBean = memberDao.getMemberById(id);
		RemoteItems item = UserRemoteServiceUtil
				.findStem(iMemberBean.getName());
		if (iMemberBean.getMemberType().equals(Member.MEMBER_TYPE_FOLDER)) {
			iMemberBean.setSignature(item.getName());
			return iMemberBean;
		}

		return null;
	}

	
	/**
	 * 获取小组分区下的小组列表
	 * 
	 * @param id
	 * @return
	 */
	public Member[] getTeamsByParent(long id) {
		this.checkAdminAndCreateGroupPerm();
		Member[] memberBeans = null;
		if (id == 0) {
			memberBeans = new Member[0];
			return memberBeans;
		}
		Member iMemberBean = memberDao.getMemberById(id);

		RemoteItems[] groups = UserRemoteServiceUtil
				.findGroupsByParent(iMemberBean.getName());
		int length = groups.length;
		memberBeans = new Member[length];
		for (int i = 0; i < length; i++) {
			try {
				memberBeans[i] = mapping(groups[i].getId(),
						groups[i].getFullname(), Member.MEMBER_TYPE_TEAM);
				memberBeans[i].setIsLeaf(groups[i].getIsLeaf());
			} catch (java.lang.NullPointerException e) {
				// ignore
			}

		}

		Arrays.sort(memberBeans, new Member());
		return memberBeans;
	}

	/**
	 * 根据id获得子组织和子组
	 * 跳过叶子检测，提高效率
	 * @param id
	 * @param withoutLeaf  
	 * @return
	 */
	public Member[] getFolderAndTeamByParent_v2(long id,boolean withoutLeaf) {
		Member[] beans = this.getChildrenByParent(id,withoutLeaf);
		return beans;
	}
	
	public Member[] getFolderAndTeamByParent_v2(long id) {
		Member[] beans = this.getChildrenByParent(id,false);
		return beans;
	}
	
	/**
	 * @author zf
	 * @param id
	 * @param withoutLeaf //modified by zf 161210 跳过叶子检测，提高效率
	 * @return
	 */
	private Member[] getChildrenByParent(long id,boolean withoutLeaf) {
		// RemoteItems[] stems = null;
		String key=null;
		RemoteItems[] stemsAndGroups = null;
		if (id == 0) {
			stemsAndGroups = UserRemoteServiceUtil.findStemsByParent(null,withoutLeaf); //modified by zf 161210 跳过叶子检测，提高效率
		} else {
			Member iMemberBean = memberDao.getMemberById(id);
			Date ts = ((Member)iMemberBean).getLastModified();
			key = id+":"+(ts==null?"":ts.getTime());//id:timestamp 作为key
			Member[] children = (Member[]) CacheUtil.getCache(CacheUtil.memberChildrenCache, key);
			if(children!=null) {
				return children;
			}
			stemsAndGroups = UserRemoteServiceUtil.findStemsAndGroupsByParent(iMemberBean.getName(),withoutLeaf); //modified by zf 161210 跳过叶子检测，提高效率
		}
		int length = stemsAndGroups.length;
		Member[] memberBeans = new Member[length];

		for (int i = 0; i < length; i++) {
			try {
				if ("stems".equals(stemsAndGroups[i].getType())) {
					memberBeans[i] = mapping(stemsAndGroups[i].getId(), stemsAndGroups[i].getFullname(),//使用getFullname ,记录全路径
							Member.MEMBER_TYPE_FOLDER);
					memberBeans[i].setIsLeaf(stemsAndGroups[i].getIsLeaf());
				} else if ("groups".equals(stemsAndGroups[i].getType())) {
					memberBeans[i] = mapping(stemsAndGroups[i].getId(), stemsAndGroups[i].getFullname(),
							Member.MEMBER_TYPE_TEAM);
					memberBeans[i].setIsLeaf(stemsAndGroups[i].getIsLeaf());
				}

			} catch (java.lang.NullPointerException e) {
				// ignore
			}
		}

		Arrays.sort(memberBeans, new Member());
		if(id!=0) {
			CacheUtil.setCache(CacheUtil.memberChildrenCache, key, memberBeans);
		}
		return memberBeans;

	}
	
	/**
	 * 查询某个组织下所有用户
	 * @param fid
	 * @return
	 * @throws GroupsException
	 */
	public Member[] getMemberInFolder(long fid) {
		if (fid == 0) {
			return this.getMIF(null, "folder");
		}
		Member folderMember = memberDao.getMemberById(fid);	
		Member[] result = this.getMemberInFolder_v2(fid); 
		if(result!=null&&result.length>1){
			return this.sameAccountFilter(result);
		}
		
		return result==null?new Member[0]:result;	
	}
	
	private Member[] sameAccountFilter(Member[] beans) {
		Map<String, Member> map = new HashMap<String, Member>();
		List<Member> uniqueResult = new ArrayList<Member>();
		for (Member bean : beans) {
			if (map.containsKey(bean.getAccount())) {
				continue;
			}
			map.put(bean.getAccount(), bean);
			uniqueResult.add(bean);
		}
		return uniqueResult.toArray(new Member[0]);
	}
	
	/**
	 * @param fid
	 * @return
	 * @throws GroupsException
	 *             与getMemberInFolder()功能相同，但使用了多线程(20161205 zf
	 *             抛弃多线程查询，改用单次查询多个group下的成员)
	 */
	private Member[] getMemberInFolder_v2(long fid) throws GroupsException {
		if (fid == 0) {
			return this.getTIF(null);
		}
		Member folderMember = memberDao.getMemberById(fid);
		// long _1 =System.currentTimeMillis();
		// Member[] result = this.getTIF(folderMember.getName());
		Member[] result = this.getAllSubTreeTeamInFolder(folderMember.getName());// 修改20161205
																						// zf
																						// 修改调用userSystem方式，修改调用Grouper方式，单次请求获得所有subTreeGroup，避免递归查询浪费时间


		// 替换多线程
		String[] groupId = new String[result.length];
		for (int i = 0; i < result.length; i++) {
			groupId[i] = result[i].getName();// group的uuid;
		}
		if (groupId.length == 0) {
			return new Member[0];
		}
		RemoteUser[] users = UserRemoteServiceUtil.getGroupsMembers(groupId);
		if (users == null || users.length < 1)
			return new Member[0];
		Member[] memberBeans = new Member[users.length];
		for (int i = 0; i < users.length; i++) {
			try {
				memberBeans[i] = memberDao.getMembersByName(users[i].getUsername()).get(0);
			} catch (java.lang.ArrayIndexOutOfBoundsException e) {
				// ignore
				// System.out.println("Get Account Exception, User donot Exist!
				// : " + users[i].getUsername());
				Member bean = new Member();
				bean.setName(users[i].getUsername());
				bean.setAccount(users[i].getUsername());
				memberBeans[i] = bean;
			}
		}
		return memberBeans;

	}
	
	private Member[] getAllSubTreeTeamInFolder(String uuid) throws GroupsException {
		RemoteItems[] subTreeGroups = UserRemoteServiceUtil.findSubTreeGroupsByParent(uuid);
		Member[] teams = new Member[0];
		for (RemoteItems item : subTreeGroups) {

			List<Member> m = memberDao.getMembersByName(item.getId());
			teams = (Member[]) ArrayUtils.addAll(teams, m.toArray(new Member[0]));
		}
		return teams;
	}
	
	/**
	 * @param
	 * @return getMemberInFolder() 的递归树遍历方法
	 */
	private Member[] getTIF(String uuid) {
		
		RemoteItems[] stemsAndGroups =  UserRemoteServiceUtil.findStemsAndGroupsByParent(uuid);
		Member[] teams = new Member[0];
		for (RemoteItems item : stemsAndGroups) {
			if ("groups".equals(item.getType())) {
				Member[] m = memberDao.getMembersByName(item.getId()).toArray(new Member[0]);
				//递归进去下一层
				teams = (Member[]) ArrayUtils.addAll(teams, m);
			}else if("stems".equals(item.getType())){
				//递归进去下一层
				teams = (Member[]) ArrayUtils.addAll(teams, this.getTIF(item.getId()));
			}
		}
		return teams;
	}
	
	/**
	 * @param
	 * @return getMemberInFolder() 的递归树遍历方法
	 */
	private Member[] getMIF(String uuid, String type) {
		if("team".equals(type)){
			//如果是用户组，则直接返回组内成员
			return this.getMembersInTeam(uuid);
		}
		RemoteItems[] stemsAndGroups = UserRemoteServiceUtil.findStemsAndGroupsByParent(uuid);
		Member[] members = new Member[0];
		for (RemoteItems item : stemsAndGroups) {
			if ("groups".equals(item.getType())) {
				//递归进去下一层
				members = (Member[]) ArrayUtils.addAll(members, this.getMIF(item.getId(),"team"));
			}else if("stems".equals(item.getType())){
				//递归进去下一层
				members = (Member[]) ArrayUtils.addAll(members, this.getMIF(item.getId(),"folder"));
			}
		}
		return members;
	}

	/**
	 * 搜索组
	 * 
	 * @param name
	 *            搜索字段
	 * @return
	 */
	public Member[] searchTeam(String name) {
		RemoteItems[] groups = UserRemoteServiceUtil.findGroupByName(name, false);
		// 记录符合要求的Group
		List<RemoteItems> groupList = new ArrayList<RemoteItems>();
		// 查找符合条件的用户组
		for (int i = 0; i < groups.length; i++) {
			String groupName = groups[i].getName();
			if (groupName.contains(name)) {
				groupList.add(groups[i]);
			}
		}
		// 查找同名的组
		boolean[] marks = new boolean[groupList.size()];
		for (int i = 0; i < groupList.size(); i++) {
			String groupName = groupList.get(i).getName();
			for (int j = 0; j < i; j++) {
				if (groupName.equals(groupList.get(j).getName())) {
					marks[i] = true;
					marks[j] = true;
					continue;
				}
			}
		}
		Member[] memberBeans = new Member[groupList.size()];
		for (int i = 0; i < groupList.size(); i++) {
			RemoteItems g = groupList.get(i);
			if (marks[i] == false) {
				memberBeans[i] = mapping(g.getId(), g.getName(), Member.MEMBER_TYPE_TEAM);
			} else {
				String fullName = g.getName().replace(":", "/");
				memberBeans[i] = mapping(g.getId(), fullName, Member.MEMBER_TYPE_TEAM);
			}
		}
		return memberBeans;
	}

	/**
	 * 修改用户组名
	 * 需要记录日志
	 * @param id
	 * @param name
	 */
	public boolean editTeam(long id, String name) {
		this.checkAdminPerm();
		this.checkDefaultTeam(id);
		Member memberBean = memberDao.getMemberById(id);

		boolean mark = UserRemoteServiceUtil.groupEdit(name, "", memberBean.getName());
		// 记录日志
		String desc = LogMessage.getTeamNameMod(memberBean.getName(), name);
		logService.addOperateLog(Log.ACTION_TYPE_MODIFY, desc, id, memberBean.getName());
		// 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		return mark;
	}

	/**
	 * 修改路径名
	 * 需要记录日志
	 * @param id
	 * @param name
	 */
	public boolean editFolder(long id, String name) {
		this.checkAdminPerm();
		this.checkDefaultFolder(id);
		Member memberBean = memberDao.getMemberById(id);
		boolean mark = UserRemoteServiceUtil.stemEdit(name, "", memberBean.getName());
		// 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		return mark;
	}

	/**
	 * 父路径中是否有子路径
	 * 
	 * @param id
	 * @return
	 */
	public boolean hasSubFolder(long id) {
		Member iMemberBean = memberDao.getMemberById(id);
		RemoteItems[] items = UserRemoteServiceUtil.findStemsByParent(iMemberBean.getName());

		if (items != null && items.length > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 父路径中是否有用户组
	 * 
	 * @param id
	 * @return
	 */

	public boolean hasSubTeam(long id) {
		Member iMemberBean = memberDao.getMemberById(id);
		RemoteItems[] items = UserRemoteServiceUtil.findGroupsByParent(iMemberBean.getName());

		if (items != null && items.length > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 用户组中是否有用户
	 * 
	 * @param id
	 * @return
	 */
	public boolean hasSubMember(long id) {
		Member iMemberBean = memberDao.getMemberById(id);
		RemoteUser[] user = UserRemoteServiceUtil.getGroupMembers(iMemberBean.getName());

		if (user.length > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 删除路径，包括子路径和子用户组
	 * 
	 * @param id
	 * @return
	 */
	public boolean deleteFolder(long id) {
		this.checkAdminPerm();
		checkDefaultFolder(id);
		boolean isDomainFolder = checkDomainFolder(id);
		if(isDomainFolder) {
			throw new GroupsException("该组织已关联到域，不可直接删除");
		}
		
		Member bean = memberDao.getMemberById(id);
		String name = bean.getName();
		// 如果有子文件夹递归删除子文件夹
		if (hasSubFolder(id)) {
			Member[] iMemberBean = getFolderByParent(id);
			for (int i = 0; i < iMemberBean.length; i++) {
				deleteFolder(iMemberBean[i].getId());
			}
		}
		// 删除子用户组
		if (hasSubTeam(id)) {
			Member[] iMemberBean = getTeamsByParent(id);
			for (int i = 0; i < iMemberBean.length; i++) {
				deleteTeam(iMemberBean[i].getId());
			}
		}
		// 删除目标文件夹
		if (!hasSubTeam(id) && !hasSubFolder(id)) {
			//memberDao.delete(new long[] { bean.getId() });
			memberDao.deleteMember(bean);
			return UserRemoteServiceUtil.stemDelete(name);
		} else {
			return false;
		}
	}

	public boolean deleteFolder_v2(long id) {
		this.checkAdminPerm();
		checkDefaultFolder(id);
		boolean isDomainFolder = checkDomainFolder(id);
		if(isDomainFolder) {
			throw new GroupsException("该组织已关联到域，不可直接删除");
		}

		Member bean = memberDao.getMemberById(id);
		String name = bean.getName();
		// 如果有子文件夹递归删除子文件夹
		if (hasSubFolder(id)) {
			Member[] iMemberBean = getFolderByParent(id);
			for (int i = 0; i < iMemberBean.length; i++) {
				deleteFolder_v2(iMemberBean[i].getId());
			}
		}
		// 删除子用户组
		if (hasSubTeam(id)) {
			Member[] iMemberBean = getTeamsByParent(id);
			for (int i = 0; i < iMemberBean.length; i++) {
				deleteTeam(iMemberBean[i].getId());
			}
		}
		//删除用户组织与通讯录的关联信息
		List<ContactSubject> cs = genericDao.findAll("from ContactSubject cs where cs.subjectId = ?1", id);
		if (cs != null && cs.size() > 0) {
			for (ContactSubject c : cs) {
				genericDao.delete(c);
			}
		}
		// 删除目标文件夹
		if (!hasSubTeam(id) && !hasSubFolder(id)) {
			//memberDao.delete(new long[] { bean.getId() });
			memberDao.deleteMember(bean);
			return UserRemoteServiceUtil.stemDelete(name);
		} else {
			return false;
		}
	}
	/**
	 * 导入用户组和成员信息 需要记录日志
	 * 
	 * @param is
	 */
	public void importTeam(InputStream is) {
		ITeamAdaptor adaptor = (ITeamAdaptor) context.getBean("TeamAdaptor");
		adaptor.importTeam(is);
		
		// 记录日志
		String desc = LogMessage.getTeamImport();
		logService.addOperateLog(Log.ACTION_TYPE_IMPORT, desc, -1, "");

	}

	/**
	 * 查看该组织是否是系统内建的默认组织
	 * 
	 * @param id
	 *            所在组织stem的id
	 * @return
	 */
	public boolean checkDefaultFolder(long id) {
		if (id == 0) {
			return true;
		}
		RemoteItems item = UserRemoteServiceUtil.findStem(memberDao
				.getMemberById(id).getAccount());

		if (PropertyUtil.getSystemDefaultOrgName().equals(item.getFullname())) {
			throw new GroupsException(ResourceProperty.getCannotOperateOrg());
		}
		return true;
	}
	
	/**
	 * 查看该组织是否是系统内建的默认组织
	 * @param folderName
	 * @return
	 */
	public boolean checkDefaultFolder(String folderName) {
		if (PropertyUtil.getSystemDefaultOrgName().equals(folderName)) {
			throw new GroupsException(ResourceProperty.getCannotOperateOrg());
		}
		return true;
	}

	/**
	 * 查看该用户组是否是系统内建的默认用户组
	 * 
	 * @param id
	 *            所在用户组group的id
	 * @return
	 */
	public boolean checkDefaultTeam(long id) {
		RemoteItems item = UserRemoteServiceUtil.findGroup(memberDao.getMemberById(
				id).getAccount());

		String systemDefaultOrgName = PropertyUtil.getSystemDefaultOrgName();
		String[] systemDefaultManageTeams = PropertyUtil
				.getSystemDefaultManageTeamName().split(";");
		for (int i = 0; i < systemDefaultManageTeams.length; i++) {
			if ((systemDefaultOrgName + ":" + systemDefaultManageTeams[i])
					.equals(item.getFullname())) {
				throw new GroupsException(
						ResourceProperty.getCannotOperateOrg());

			}
		}
		return true;
	}
	
	/**
	 *  查看该用户组是否是系统内建的默认用户组
	 * @param teamName
	 * @return
	 */
	public boolean checkDefaultTeam(String teamName) {
		String systemDefaultOrgName = PropertyUtil.getSystemDefaultOrgName();
		String[] systemDefaultManageTeams = PropertyUtil.getSystemDefaultManageTeamName().split(";");
		for (int i = 0; i < systemDefaultManageTeams.length; i++) {
			if ((systemDefaultOrgName + ":" + systemDefaultManageTeams[i]).equals(teamName)) {
				throw new GroupsException(ResourceProperty.getCannotOperateOrg());

			}
		}
		return true;
	}
	
	public boolean checkDomainFolder(long id) {
		if(id==0||id==-1) {
			return true;
		}
		String hql = "from "+DomainFolder.class.getName()+" as df where df.folder.id=?";
		Query q = genericDao.createQuery(hql, new Object[] {id});
		List list = q.getResultList();
		if(list!=null&&list.size()>0) {
			return true;
		}
		return false;
	}

	/**
	 * 设置member显示优先级
	 * 
	 * @param ids
	 * @param orders
	 */
	public void setMemberPriority(long[] ids, double[] orders) {
		int index = 0;
		for (long id : ids) {
			Member m = memberDao.getMemberById(id);
			m.setPriority(orders[index++]);
			memberDao.saveOrUpdateMember(m);
		}
	}
	
	///////////////////////////////////跟person有关/////////////////////////////
	/**
	 * 根据account获取member
	 * @param account
	 * @return
	 */
	public List<Member> getMembersByAccount(String account) {
		return memberDao.getMembersByAccount(account);
	}
	
	/**
	 * 根据name and account取person类型的member
	 * @param name
	 * @param account
	 * @return
	 */
	public Member getMemberByNameAndAccount(String name, String account) {
		return memberDao.getMemberByNameAccount(name, account, Member.MEMBER_TYPE_PERSON);
	}
	
	/**
	 *  根据name、取folder类型的member
	 * @param name
	 * @param type
	 * @return
	 */
	public Long getMemberByNameAndType(String name,String type) {
		try{
			Member member=memberDao.getMembersByNameandType(name, type);
			if(member!=null)
				return member.getId();
			else return null;
		}catch (Exception e){
			return  null;
		}

	}


	/**
	 *  根据name、account取person类型的member
	 * @param name
	 * @param account
	 * @param type
	 * @return
	 */
	public Member getMemberByNameAndAccount(String name, String account, String type) {
		return memberDao.getMemberByNameAccount(name, account, type);
	}
	/**
	 * 根据id获取member
	 * @param id
	 * @return
	 */
	public Member getMemberById(long id) {
		return memberDao.getMemberById(id);
	}
	
	/**
	 * 根据id获得子组织和子组
	 * 此方法已废弃，请使用getFolderAndTeamByParent_v2
	 * @param id
	 * @return
	 * @throws GroupsException
	 */
	@Deprecated
	public Member[] getFolderAndTeamByParent(long id) throws GroupsException {
		Member[] folderBeans = this.getFolderByParent(id);
		Member[] teamBeans = this.getTeamsByParent(id);

		Member[] beans = new Member[folderBeans.length + teamBeans.length];
		System.arraycopy(folderBeans, 0, beans, 0, folderBeans.length);
		System.arraycopy(teamBeans, 0, beans, folderBeans.length, teamBeans.length);
		return beans;
	}
	
	/**
	 * 此方法已废弃，请使用setMemberPriority
	 * @param ids
	 * @param orders
	 */
	@Deprecated
	public void sortMember(long[] ids, double[] orders) {
		this.setMemberPriority(ids, orders);
	}

	/**
	 * 同步用户,可能不需要，暂未实现
	 * 
	 * @throws GroupsException
	 */
	public void synchronizeMembers() {
		// // 远程获取用户列表
		// Member[] remoteMembers =
		// memberDao.getRowMembers(userData.getAccount());
		//
		// // 圈子本地用户列表
		// Member[] localMembers =
		// memberDao.getMembersByAccount(userData.getAccount());
		//
		// // 比较远程和本地用户列表，如果不一致，将进行同步
		// Map<String, Member[]> map = compared(remoteMembers, localMembers);
		//
		// // 本地需要复活的用户
		// Member[] resurrectionMembers = map.get("resurrection");
		// for (Member bean : resurrectionMembers) {
		// bean.setStatus(Member.Status.NORMAL);
		// memberDao.update(bean);
		// }
		//
		// // 本地需要新增的用户
		// Member[] addMembers = map.get("add");
		// for (Member bean : addMembers) {
		// bean.setAccount(userData.getAccount());
		// bean.setStatus(Member.Status.NORMAL);
		// bean.setType(Member.Type.PERSON);
		// memberDao.create(bean);
		// }
		//
		// // 本地需要删除的用户
		// Member[] removeMembers = map.get("remove");
		// for (Member bean : removeMembers) {
		// bean.setStatus(Member.Status.EXPIRED);
		// memberDao.update(bean);
		// }
	}

	/**
	 * 不知道用来干嘛，数据库里面都是空，暂时不写
	 * 
	 * @param publicKey
	 * @param serialNumber
	 */
	public void importPublicKey(String publicKey, String serialNumber) {

	}

	/**
	 * 不知道用来干嘛，数据库里面都是空，暂时不写
	 * 
	 * @param privateKey
	 */
	public void importPrivateKey(String privateKey) {

	}

	/**
	 * 不知道用来干嘛，数据库里面都是空，暂时不写
	 * 
	 * @param account
	 * @param isAvailable
	 */
	public void changeIsAvailable(String account, boolean isAvailable) {
		if (!permission.isAdmin(UserUtils.getCurrentMemberId()))
			throw PermissionsException.MemberException;
	}
	/**
	 * 向上更新LastModified
	 * @param memberId
	 * @param now
	 */
	public void shiftUpLastModified(long memberId, Date now) {
		Member  modifiedMember = null;
		Set<Long> uniqueMemberSet = new HashSet<Long>();
		modifiedMember = this.getMemberById(memberId);
		if(modifiedMember==null){
			System.out.println("用户组织结构修改lastModified 时，找不到id为"+memberId+"的member对象");
			return;
		}
		if(modifiedMember.getMemberType().equals(Member.MEMBER_TYPE_PERSON)){
			//如果修改的是个人信息，则修改该member所在的所有父级组织结构的lastModified值
			modifiedMember.setLastModified(now);
			memberDao.saveOrUpdateMember(modifiedMember);
			uniqueMemberSet.add(modifiedMember.getId());
			
			JSONObject json =UserRemoteServiceUtil.getGroupsOfMembers(new String[]{modifiedMember.getAccount()});
			if(json.isNullObject()||json.isEmpty()){
				return ;
			}
			JSONArray groups = json.getJSONArray("groups");
			
			//Set<Long> contactResultSet = new HashSet<Long>();
			if (groups != null) {
				for (int i = 0; i < groups.size(); i++) {
					JSONObject group = groups.getJSONObject(i);
					String groupUUID = group.getString("uuid");
					Member team =  memberDao.getTeamByName(groupUUID);
					if (team == null || team.equals(null)) {
						throw new GroupsException("找不到UUID为"+groupUUID+"的组");
					}
					team.setLastModified(now);
					if(!uniqueMemberSet.contains(team.getId())){
						memberDao.saveOrUpdateMember(team);
						uniqueMemberSet.add(team.getId());
					}
					
					JSONArray parentFolders = group.getJSONArray("parentFolders");
					if (parentFolders == null || parentFolders.isEmpty()) {
						continue;
					}
					for (int j = 0; j < parentFolders.size(); j++) {
						JSONObject pFolder = parentFolders.getJSONObject(j);
						String folderUUID = pFolder.getString("uuid");
						Member folder =  memberDao.getFolderByName(folderUUID);
						if (folder == null || folder.equals(null)) {
							throw new GroupsException("找不到UUID为"+folderUUID+"的组织");
						}
						folder.setLastModified(now);
						if(!uniqueMemberSet.contains(folder.getId())){
							memberDao.saveOrUpdateMember(folder);
							uniqueMemberSet.add(folder.getId());
						}
					}
				}
			}
		}else {
			//修改的是某个组或组织，则向上更新lastModified值
//			RemoteItems group = UserRemoteServiceUtil.findGroup(modifiedMember.getAccount());
			modifiedMember.setLastModified(now);
			memberDao.saveOrUpdateMember(modifiedMember);
			uniqueMemberSet.add(modifiedMember.getId());
			String subjectId = modifiedMember.getAccount();
			
			JSONObject json = UserRemoteServiceUtil.getParentsOfFolderOrTeam(new String[]{subjectId}, modifiedMember.getMemberType());
			JSONArray parentFolders = json.getJSONArray("parentFolders");
			if (parentFolders == null || parentFolders.isEmpty()) {
				return;
			}
			for (int j = 0; j < parentFolders.size(); j++) {
				JSONObject pFolder = parentFolders.getJSONObject(j);
				String folderUUID = pFolder.getString("uuid");
				Member folder = memberDao.getFolderByName(folderUUID);
				if (folder == null || folder.equals(null)) {
					throw new GroupsException("找不到UUID为"+folderUUID+"的组织");
				}
				folder.setLastModified(now);
				if(!uniqueMemberSet.contains(folder.getId())){
					memberDao.saveOrUpdateMember(folder);
					uniqueMemberSet.add(folder.getId());
				}
			}
			
		}
		
	
		
	}
	/**
	 * 获取所有的GroupMemberBinding
	 * @return
	 */
	public List<GroupMemberBinding> getAllBindings() {
		return memberDao.getAllBindings();
	}

	/**
	 * 根据名字获取member
	 * @param name
	 * @return
	 */
	public List<Member> getMembersByName(String name) {
		return memberDao.getMembersByName(name);
	}

	/**
	 * 检查用户以及用户所在的组是否在柜子中
	 * @param memberId
	 * @param groupId
	 * @return
	 */
	public boolean checkMemberInGroup(long memberId, long groupId) {
		boolean flag = memberDao.findMemberInGroup(memberId, groupId);
		if (flag == false) {
			// 用户不在圈子，查看所在的组是否在圈子
			Member[] beans = this.getTeamsOfMember(memberId);
			for (Member bean : beans) {
				flag = memberDao.findMemberInGroup(bean.getId(), groupId);
				if (flag == true) {
					return true;
				}
			}
		}
		return false;
	}
	
	
	/**
	 * 获取所有用户组
	 * @return
	 */
	public Member getEveryOneTeam() {
		if (PropertyUtil.EVERYONE_TEAM_ID > 0) {
			return memberDao.getMemberById(PropertyUtil.EVERYONE_TEAM_ID);
		}
		String[] defaultTeam = PropertyUtil.getSystemDefaultManageTeamName().split(";");
		String name = PropertyUtil.getSystemDefaultOrgName() + ":" + defaultTeam[1];

		// grouper1.0
		// Group group = api.findGroupByName(name);

		// grouper2.0
		// WsGroup group = wsFindGroup.findGroupByName(name,
		// true).getGroupResults()[0];

		RemoteItems group = UserRemoteServiceUtil.findGroupByName(name, true)[0];

		if (group != null) {
			Member memberBean = mapping(group.getId(), Member.MEMBER_TYPE_TEAM);
			PropertyUtil.EVERYONE_TEAM_ID = memberBean.getId();
			return memberBean;
		}
		return null;
	}
	
	private Member mapping(String name, String type) {
		Member member = memberDao.getMemberByNameAccount(name, name, type);
		if (member == null || member.getId() <= 0) {
			Member bean = new Member();
			bean.setAccount(name);
			bean.setName(name);
			bean.setMemberStatus(Member.STATUS_NORMAL);
			bean.setMemberType(type);
			memberDao.saveOrUpdateMember(bean);
			member = bean;
		}
		return member;
	}

	private void checkAdminPerm() {
		if (!permission.isAdmin(UserUtils.getCurrentMemberId()))
			throw PermissionsException.MemberException;
	}
	
	private void checkAdminPerm(long memberId) {
		if (memberId != OldPerm.SYSTEMADMIN_MEMBER_ID) {
			if (!permission.isAdmin(memberId))
				throw PermissionsException.MemberException;
		}
	}
	
	private void checkAdminAndCreateGroupPerm(long memberId) {
		if (memberId != OldPerm.SYSTEMADMIN_MEMBER_ID) {
			if (!permission.isAdmin(memberId)
					&& !permission.hasCreatGroupPermission(memberId))
				throw PermissionsException.MemberException;
		}
	}
	private void checkAdminAndCreateGroupPerm() {
		if (!permission.isAdmin(UserUtils.getCurrentMemberId())
				&& !permission.hasCreatGroupPermission(UserUtils.getCurrentMemberId()))
			throw PermissionsException.MemberException;
	}
	
	private void checkViewGroupPerm(long groupId) {
		if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), groupId,
				GroupPerm.VIEW_MEMBER))
			throw PermissionsException.MemberException;
	}
	/**
	 * 获得个人自建分享组
	 * @return
	 */
	public List<ShareTeamInfo> getMyShareTeam() {
		// TODO Auto-generated method stub
		long collectorId = UserUtils.getCurrentMemberId();
		Member creator = memberDao.getMemberById(collectorId);
		if (creator == null) {
			throw new GroupsException("创建者不存在");
		}		
		return memberDao.getShareTeamOfCreator(creator);
	}
	
	/**
	 * 创建自建分享组和组信息
	 * @param name 名字
	 * @param desc
	 * @return
	 */
	public ShareTeamInfo createShareTeam(String name, String desc) {
		// TODO Auto-generated method stub
		long collectorId = UserUtils.getCurrentMemberId();
		Member creator = memberDao.getMemberById(collectorId);
		if (creator == null) {
			throw new GroupsException("创建者不存在");
		}
		
		Member shareTeam =  memberDao.getMemberByNameAccount(name, creator.getAccount(), Member.MEMBER_TYPE_SHARETEAM);	
		if (shareTeam == null) {
			ShareTeamInfo shareTeamInfo = memberDao.createShareTeam(name, creator, desc);
			return shareTeamInfo;
		}
		throw new GroupsException("同名组已经存在");
	}
	
	/**
	 * 批量添加用户到自建分享组
	 * @param shareTeamId 自建分享组id
	 * @param ids 待添加用户id
	 */
	public void addMemberToShareTeam(long shareTeamId, long[] ids) {
		// TODO Auto-generated method stub
		Member shareTeam = memberDao.getMemberById(shareTeamId);
		if (shareTeam == null ) {
			throw new GroupsException("分享组不存在");
		}

		if(!UserUtils.getAccount().equals(shareTeam.getAccount())){
			throw new GroupsException("你不是该分享组创建者");
		}
		for(long id:ids){
			Member member = memberDao.getMemberById(id);
			if (member == null || !member.getMemberType().equals(Member.MEMBER_TYPE_PERSON)) {
				throw new GroupsException("memberId为"+id+"的用户不存在");
			}
			memberDao.addMemberToShareTeam(shareTeam,member);
		}
	}
	
	/**
	 * 添加单个用户到自建分享组
	 * @param shareTeamId 分享组id
	 * @param id 待添加用户id
	 */
	public void addMemberToShareTeam(long shareTeamId, long id) {
		// TODO Auto-generated method stub
		Member shareTeam = memberDao.getMemberById(shareTeamId);
		if (shareTeam == null ) {
			throw new GroupsException("分享组不存在");
		}

		if(!UserUtils.getAccount().equals(shareTeam.getAccount())){
			throw new GroupsException("你不是该分享组创建者");
		}
		Member member = memberDao.getMemberById(id);
		if (member == null || !member.getMemberType().equals(Member.MEMBER_TYPE_PERSON)) {
			throw new GroupsException("memberId为"+id+"的用户不存在");
		}
		memberDao.addMemberToShareTeam(shareTeam,member);
	}

	/**
	 * 根据分享组id获取分享组成员
	 * @param shareTeamId 分享组id
	 * @return
	 */
	public List<Member> getShareTeamMember(long shareTeamId) {
		// TODO Auto-generated method stub
		Member shareTeam = memberDao.getMemberById(shareTeamId);
		if (shareTeam == null) {
			throw new GroupsException("分享组不存在");
		}	
		return memberDao.getShareTeamMember(shareTeamId);
	}

	/**
	 * 根据id删除自建分享组中指定成员
	 * @param shareTeamId 自建分享组id
	 * @param ids 待删除成员id数组
	 */
	public void removeMemberFromShareTeam(long shareTeamId, long[] ids) {
		// TODO Auto-generated method stub
		Member shareTeam = memberDao.getMemberById(shareTeamId);
		if (shareTeam == null) {
			throw new GroupsException("分享组不存在");
		}
		if(!UserUtils.getAccount().equals(shareTeam.getAccount())){
			throw new GroupsException("你不是该分享组创建者");
		}
		memberDao.removeMemberFromShareTeam(shareTeamId,ids);
	}

	/**
	 * 根据id删除自建分享组
	 * @param shareTeamId
	 */
	public void deleteShareTeam(long shareTeamId) {
		// TODO Auto-generated method stub
		Member shareTeam = memberDao.getMemberById(shareTeamId);
		if (shareTeam == null) {
			throw new GroupsException("分享组不存在");
		}
		if(!UserUtils.getAccount().equals(shareTeam.getAccount())){
			throw new GroupsException("你不是该分享组创建者");
		}
		memberDao.deleteMember(shareTeam);
	}
	
	/**
	 * 删除用户
	 * @param ac 用户account
	 */
//	public void deleteAccount(String ac) {
//		// TODO Auto-generated method stub
//		User localUser = userService.getUserByAccount(ac);
//		List<Member> members = memberDao.getMembersByAccount(ac);
//		for (Member member : members) {
//			try {
//				//获得用户所在的所有组，并更新lastModified
//				Member[]  teams = getTeamsOfMember(member.getId());
//				for(Member t:teams) {
//					shiftUpLastModified(t.getId(), new Date());
//				}
//			} catch (Exception e) {
//				System.out.println("更新所在用户组lastModified失败");
//			}
//			this.deleteMember(member.getId());
//			// 删除应用用户
//			List<AppMember> am = genericDao.findAll("from AppMember where memberId = ?", member.getId());
//			if (am != null || am.size() > 0) {
//				for (AppMember a : am) {
//					genericDao.delete(a);
//				}
//			}
//		}
//
//		RemoteUser user = new RemoteUser(ac, null);
//		UserRemoteServiceUtil.userDelete(user);
//
//		String desc = LogMessage.getAccountDelete(ac);
//		// 清除用户
//		userService.deleteAccount(ac);
//
//		// 记录日志
//		logService.addOperateLog(Log.ACTION_TYPE_DELETE, desc, localUser.getId(), ac);
//
//	}

	public String deleteAccount(String ac) {
		// TODO Auto-generated method stub
		User localUser = userService.getUserByAccount(ac);
		List<Member> members = memberDao.getMembersByAccount(ac);
		StringBuffer stringBuffer=new StringBuffer();
		Boolean isAdmin=false;
		for (Member member : members) {
			try {
				//获得用户所在的所有组，并更新lastModified
				Member[]  teams = getTeamsOfMember(member.getId());
				for(Member t:teams) {
					shiftUpLastModified(t.getId(), new Date());
				}
			} catch (Exception e) {
				System.out.println("更新所在用户组lastModified失败");
			}
			List<DomainManager> dms=genericDao.findAll("from DomainManager as dm where dm.manager.id = ?1", member.getId());
			if(dms!=null&&dms.size()>0){
				isAdmin=true;
				stringBuffer.append(member.getAccount()+"是域管理员,请先删除他的管理员权限").append(",");
			}
			else
				this.deleteMember(member.getId());

			// 删除应用用户
			List<AppMember> am = genericDao.findAll("from AppMember where memberId = ?1", member.getId());
			if (am != null || am.size() > 0) {
				for (AppMember a : am) {
					genericDao.delete(a);
				}
			}
		}

		RemoteUser user = new RemoteUser(ac, null);
		UserRemoteServiceUtil.userDelete(user);

		String desc = LogMessage.getAccountDelete(ac);
		// 清除用户

		if(!isAdmin){
			userService.deleteAccount(ac);
		}


		// 记录日志
		logService.addOperateLog(Log.ACTION_TYPE_DELETE, desc, localUser.getId(), ac);
		return stringBuffer.toString()==null?"":stringBuffer.toString();

	}
	/**
	 * 根据memberId删除用户
	 * 同时将所有用户关联的数据清除
	 * @param
	 */
	private void deleteMember(long memberId) {
		// TODO Auto-generated method stub
		// 若是管理员马甲，不允许删除
		
		Admin admin = adminDao.getAdminByMember(memberId);
		if (admin != null) {
			throw new GroupsException(ResourceProperty.getAdminCannotDeleteString());
		}
		// 把用户所加入的组中移除
		Member[] beans = getTeamsOfMember(memberId);
		for (Member bean : beans) {
			removeMemberFromTeam(memberId, bean.getId());
		}
		// 只能删除个人文件柜
		try {
			Group g = groupDao.getGroupByName("" + memberId);
			groupService.deleteGroup(g.getId());
		} catch (Exception e) {

		}
		// 删除柜子管理员
		groupManagerDao.deleteGroupManagerByMember(memberId);
		// 删除圈子成员绑定
		memberDao.deleteMemberGroupBindingByMember(memberId);

		// 删除相关的圈子动态

		// 转移柜子资源
		groupService.modifyResourceOwner(memberId, UserUtils.getCurrentMemberId());
		// service.getGroupManager().deleteResourceByMember(memberId);

		// 删除权限
		oldPermDao.deletePermsByMember(memberId);

		// 删除主题和帖子
		// 记录日志
		Member bean = memberDao.getMemberById(memberId);
		String desc = LogMessage.getMemberDelete(bean.getName());

		// 删除用户
		memberDao.deleteMember(bean);

		// 记录日志
//		logService.addOperateLog(Log.ACTION_TYPE_DELETE, desc, bean.getId(), bean.getName());
	}

	/**
	 * 非递归搜索某个节点下的用户
	 * @param id
	 * @param account
	 * @param name
	 * @param structPath
	 * @param structMap
	 * @return
	 */
	public Member[] searchMemberInFolderReturnStruct(Long id, String account,
			String name, Stack<Member> structPath,
			Map<Member, List<Member>> structMap) {
		// TODO Auto-generated method stub
		this.searchMemberInTreeNodeReturnStruct(id,account,name,structMap);//非递归
		return structMap.keySet().toArray(new Member[0]);
	}

	/**
	 * 非递归搜索某个节点下的用户
	 * @param id
	 * @param account
	 * @param name
	 * @param structMap
	 */
	private void searchMemberInTreeNodeReturnStruct(Long id, String account,
			String name, Map<Member, List<Member>> structMap) {
		// TODO Auto-generated method stub
		Member root = this.getMemberById(id);
		Stack<Member> stack = new Stack<Member>();
		Stack<Member> path = new Stack<Member>();
		stack.push(root);
		while(!stack.isEmpty()) {
			Member member = stack.peek();
			if(!Member.MEMBER_TYPE_PERSON.equals(member.getMemberType())&&!path.isEmpty()&&member.equals(path.peek())) {
				path.pop();
				stack.pop();
				continue;
			}
			if(Member.MEMBER_TYPE_PERSON.equals(member.getMemberType())) {
				//如果是人
				if ((account != null && !"".equals(account.trim()) && member.getAccount().contains(account))
						|| (name != null && !"".equals(name.trim()) && member.getName().contains(name))) {
					List<Member> temp = new ArrayList<Member>();
					temp.addAll(path);
					Member _temp  = new Member(member.getId());
					_temp.setAccount(member.getAccount());
					structMap.put(_temp, temp);
				}
				stack.pop();
			}else if(Member.MEMBER_TYPE_TEAM.equals(member.getMemberType())) {
				//如果是组
				path.push(member);
				Member[] children = this.getMembersInTeam(member.getId());
				for(int i=children.length-1;i>=0;i--) {
					stack.push(children[i]);
				}
			}else if(Member.MEMBER_TYPE_FOLDER.equals(member.getMemberType())) {
				//如果是组织
				path.push(member);
				Member[] children = this.getChildrenByParent(member.getId(), true);
				for(int i=children.length-1;i>=0;i--) {
					stack.push(children[i]);
				}
			}
		}
	}

	public boolean addMemberToTeam(long[] memberId, Long teamId) {
		// TODO Auto-generated method stub
		Member iMemberBean = memberDao.getMemberById(teamId);
		Member memberBean;
		// ArrayList<String> listAccount = new ArrayList<String>();
		ArrayList<Member> list = new ArrayList<Member>();
		for (int i = 0; i < memberId.length; i++) {
			memberBean = memberDao.getMemberById(memberId[i]);
			//User userBaseBean = userService.getUserByAccount(memberBean.getAccount());
			if (Member.MEMBER_TYPE_PERSON.equals(memberBean.getMemberType())) {
				list.add(memberBean);
			}
		}
		String[] membersId = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			memberBean = list.get(i);
			membersId[i] = memberBean.getAccount();

		}

		boolean mark = UserRemoteServiceUtil.addMember(membersId, iMemberBean.getName());

		for (int i = 0; i < list.size(); i++) {
			memberBean = list.get(i);
			// 记录日志
			String desc = LogMessage.getMemberAddToTeam(memberBean.getName(), iMemberBean.getName());
			logService.addOperateLog(Log.ACTION_TYPE_ADD, desc, memberBean.getId(), memberBean.getName());
		}
		// 清除myGroupsCache
		CacheUtil.removeAll(CacheUtil.myGroupsCache);
		CacheUtil.removeAll(CacheUtil.myVisualGroupCache);
		CacheUtil.removeAll(CacheUtil.memberTeamCache);
		return mark;
	}

	public void exportAccountFromTeam(OutputStream os, Long id) {
		// TODO Auto-generated method stub
		Member[] memberBeans = null;
		if (id == 0) {
			memberBeans = new Member[0];
		} else {
			memberBeans = this.getMembersInTeam(id);
		}
		List<User> userList = new ArrayList<User>();
		List<Member> memberList = new ArrayList<Member>();
		List<String> exceptionList = new ArrayList<String>();
		for (int i = 0; i < memberBeans.length; i++) {
			//userBaseBeans[i] = imm.getAccount(memberBeans[i].getAccount());
			
			if (memberBeans[i].getAccount()!=null) {
				userList.add(userService.getUserByAccount(memberBeans[i].getAccount()));
				memberList.add(memberBeans[i]);
			} else {
				System.out.println("Get Account Exception, User donot Exist! : " + memberBeans[i].getName());
				exceptionList.add(memberBeans[i].getName());
			}
		}
		User[] userBaseBeans = userList.toArray(new User[userList.size()]);
		//memberBeans = memberList.toArray(new IMemberBean[memberList.size()]);	
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("用户名单");
		SheetWriter writer = new SheetWriter(sheet);

		// 先写个标题
		writer.append("帐户").append("姓名").append("状态").append("公司").append("部门")
				.append("电子邮箱").append("电话").append("手机").append("IM").append("职位")
				.nextRow();

			if (userBaseBeans.length != 0)
				for (User bean : userBaseBeans) {
					writer.append(bean.getAccount()).append(bean.getName()).append(
							bean.getUserbaseStatus()).append(bean.getCompany())
							.append(bean.getDepartment()).append(bean.getEmail())
							.append(bean.getPhone()).append(bean.getMobile())
							.append(bean.getIm()).append(bean.getPosition()).nextRow();
				}

		// 写入到输出流中
		try {
			workbook.write(os);
		} catch (IOException e) {
			throw new GroupsException(e);
		}
		
	}
	
}
