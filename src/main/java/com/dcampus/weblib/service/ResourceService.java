package com.dcampus.weblib.service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dcampus.common.config.LogMessage;
import com.dcampus.common.config.PropertyUtil;
import com.dcampus.common.config.ResourceProperty;
import com.dcampus.common.generic.GenericDao;
import com.dcampus.common.paging.AndSearchTerm;
import com.dcampus.common.paging.AscSortItem;
import com.dcampus.common.paging.DescSortItem;
import com.dcampus.common.paging.OrSearchTerm;
import com.dcampus.common.paging.PageNavigater;
import com.dcampus.common.paging.PageTerm;
import com.dcampus.common.paging.SearchItem;
import com.dcampus.common.paging.SearchTerm;
import com.dcampus.common.paging.SortTerm;
import com.dcampus.sys.util.UserUtils;
import com.dcampus.weblib.dao.CategoryDao;
import com.dcampus.weblib.dao.GroupDao;
import com.dcampus.weblib.dao.GroupResourceDao;
import com.dcampus.weblib.dao.GroupResourceReceiveDao;
import com.dcampus.weblib.dao.GroupResourceShareDao;
import com.dcampus.weblib.dao.GroupTypeDao;
import com.dcampus.weblib.dao.MemberDao;
import com.dcampus.weblib.entity.AppMember;
import com.dcampus.weblib.entity.Application;
import com.dcampus.weblib.entity.Category;
import com.dcampus.weblib.entity.Group;
import com.dcampus.weblib.entity.GroupExtern;
import com.dcampus.weblib.entity.GroupResource;
import com.dcampus.weblib.entity.GroupResourceReceive;
import com.dcampus.weblib.entity.GroupResourceShare;
import com.dcampus.weblib.entity.GroupType;
import com.dcampus.weblib.entity.Log;
import com.dcampus.weblib.entity.Member;
import com.dcampus.weblib.entity.ShareResponse;
import com.dcampus.weblib.entity.ShareWrap;
import com.dcampus.weblib.entity.keys.ResourceReceiveSearchItemKey;
import com.dcampus.weblib.entity.keys.ResourceReceiveSortItemKey;
import com.dcampus.weblib.entity.keys.ResourceSearchItemKey;
import com.dcampus.weblib.entity.keys.ResourceSortItemKey;
import com.dcampus.weblib.exception.PermissionsException;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.service.permission.IPermission.GroupPerm;
import com.dcampus.weblib.service.permission.RolePermissionUtils;
import com.dcampus.weblib.service.permission.impl.Permission;
import com.dcampus.weblib.util.CheckUtil;
import com.dcampus.weblib.util.FilePathUtil;
import com.dcampus.weblib.util.FileUtil;
import com.dcampus.weblib.util.ThreadPoolService;
import com.dcampus.weblib.util.Thumbnail;
import com.dcampus.weblib.util.ZipFile;

/**
 * 与资源有关的service
 * @author patrick
 *
 */
@Service
@Transactional(readOnly=false)
public class ResourceService {
	@Autowired
	GenericDao genericDao;
	
	@Autowired
	GroupResourceShareDao resourceShareDao;
	
	@Autowired
	GroupResourceReceiveDao resourceReceiveDao;
	
	@Autowired
	GroupResourceDao resourceDao;
	
	@Autowired
	GroupDao groupDao;
	
	@Autowired
	GroupTypeDao groupTypeDao;
	
	@Autowired
	MemberDao memberDao;
	@Autowired
	CategoryDao categoryDao;
	
	@Autowired
	GrouperService grouperService;
	
	@Autowired
	private LogService logService;
	@Autowired
	private ApplicationService appService;
	
	@Autowired
	private RolePermissionUtils roleUtils;
	
	@Autowired
	private Permission permission;

	@Autowired
	@Lazy
	private helpAsyncService helpAsyncService;
	///////////////////////////////////
	public long getNumberOfDirectoriessByParent(long parentId) {
		return resourceDao.getNumberOfResourcesByParent(parentId, GroupResource.RESOURCE_TYPE_DIRECTORY, GroupResource.RESOURCE_STATUS_NORMAL);
	}
	
	/**
	 * 获取顶级normal资源总数
	 * @param parentId
	 * @return
	 */
	public long getResourcesAmountByParent(long parentId) {
		Long amount = resourceDao.getNumberOfResourcesByParent(parentId);
		//减去回收站
		if (amount != null) {
			return amount;
		}
		return 0L;
	}

	/**
	 *
	 * @param parentId
	 * @param status
	 * @return
	 */
	public long getResourcesAmountByParent(long parentId, String status) {
		Long amount = resourceDao.getNumberOfResourcesByParent(parentId, status);
		//减去回收站
		if (amount != null) {
			return amount;
		}
		return 0L;
	}

	/**
	 * 根据parentId获取所有类型的资源，不包括删除状态的
	 * @param parentId
	 * @return
	 * @throws Exception 
	 */
	public List<GroupResource> getResourcesByParent(long parentId, int start, int limit) throws Exception {
		if (parentId == 0)
			throw PermissionsException.GroupException;

		GroupResource bean = resourceDao.getResourceById(parentId);
		if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), bean.getGroup().getId(),
				GroupPerm.VIEW_RESOURCE)) {
			
			if(!permission.isReceivedResourceToMember(parentId, UserUtils.getCurrentMemberId())) {
				throw PermissionsException.GroupException;
			}
		}
		return this.getResourcesByParent(parentId, null, start, limit, null);
	}
	public List<GroupResource> getResourcesByParent(long parentId, String status,int start, int limit, String orderBy) throws Exception {
		if (parentId == 0)
			throw PermissionsException.GroupException;

		GroupResource bean = resourceDao.getResourceById(parentId);
		if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), bean.getGroup().getId(),
				GroupPerm.VIEW_RESOURCE)) {
			
			if(!permission.isReceivedResourceToMember(parentId, UserUtils.getCurrentMemberId())) {
				throw PermissionsException.GroupException;
			}
		}
		final PageNavigater<GroupResource> pageNavigater = new PageNavigater<GroupResource>(
				new AndSearchTerm(), new SortTerm(), new PageTerm(), resourceDao);

		// 重置searchTerm
		pageNavigater.getSearchTerm().clear();
		pageNavigater.getSearchTerm()
				.add(new SearchItem<Long>(ResourceSearchItemKey.ParentId, SearchItem.Comparison.EQ, parentId));
		if (status != null) {
			pageNavigater.getSearchTerm().add(new SearchItem<String>(ResourceSearchItemKey.Status, SearchItem.Comparison.EQ,
					status));
		}

		// 重置sortTerm，先按优先级排序，再按创建时间排序
		pageNavigater.getSortTerm().clear();
		pageNavigater.getSortTerm().add(new DescSortItem(ResourceSortItemKey.DefaultFolder));
		pageNavigater.getSortTerm().add(new AscSortItem(ResourceSortItemKey.Type));
		if (orderBy != null && orderBy.trim().length() > 0) {
			String[] bys = orderBy.split(",");
			for (String order : bys) {
				String[] o = order.split(" ");
				if (o.length == 1) {
					pageNavigater.getSortTerm().add(new AscSortItem(new ResourceSortItemKey(o[0])));

				} else if (o.length == 2) {
					if ("DESC".equals(o[1].toUpperCase())) {
						pageNavigater.getSortTerm().add(new DescSortItem(new ResourceSortItemKey(o[0])));
					} else {
						pageNavigater.getSortTerm().add(new AscSortItem(new ResourceSortItemKey(o[0])));
					}
				}
			}
		}

		pageNavigater.getSortTerm().add(new AscSortItem(ResourceSortItemKey.Priority));
		pageNavigater.getSortTerm().add(new DescSortItem(ResourceSortItemKey.CreationDate));

		// 重置pageTerm
		pageNavigater.getPageTerm().setBeginIndex(CheckUtil.reviseStart(start));
		pageNavigater.getPageTerm().setPageSize(CheckUtil.reviseLimit(limit, PropertyUtil.getResourceDefaultPageSize()));
		GroupResource[] result = pageNavigater.getContent();
		return Arrays.asList(result);
	}
	
	private List<GroupResource> getResourcesByParent(long parentId) {
		if (parentId == 0)
			throw PermissionsException.GroupException;
		
		GroupResource bean = resourceDao.getResourceById(parentId);
		if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), bean.getGroup().getId(),
				GroupPerm.VIEW_RESOURCE)) {
			
			if(!permission.isReceivedResourceToMember(parentId, UserUtils.getCurrentMemberId())) {
				throw PermissionsException.GroupException;
			}
		}
		return resourceDao.getResourcesByParent(parentId, GroupResource.RESOURCE_STATUS_NORMAL);
	}
	
	private List<GroupResource> getResourcesByParentWithoutPermCheck(long parentId) {
		return resourceDao.getResourcesByParent(parentId, GroupResource.RESOURCE_STATUS_NORMAL);
	}
	
	/**
//	 * 根据parentId获取所有类型的资源，包括删除状态的
//	 * @param parentId
//	 * @param start
//	 * @param limit
//	 * @return
//	 */
//	private List<GroupResource> getResourcesByParent(long parentId, int start, int limit) {
//		if (parentId == 0)
//			throw PermissionsException.GroupException;
//
//		GroupResource bean = resourceDao.getResourceById(parentId);
//		if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), bean.getGroup().getId(),
//				GroupPerm.VIEW_RESOURCE)) {
//			
//			if(!permission.isReceivedResourceToMember(parentId, UserUtils.getCurrentMemberId())) {
//				throw PermissionsException.GroupException;
//			}
//		}
//		return resourceDao.getResourcesByParent(parentId, start, limit);
//	}
	
	
	
	
	
	/////////////////////////////////////
	
	/**
	 * 检查资源名字是否已经存在
	 * 同一位置不允许文件或者文件夹重名
	 * @return
	 */
	public boolean checkResourceName(GroupResource resource){
		resourceDao.clear();
		Boolean flag = false;
		long groupId = resource.getGroup().getId();
		String name = resource.getName();
		long parentId = resource.getParentId();
		int type = resource.getResourceType();
		GroupResource r = resourceDao.getResourceByDetails(groupId, name, parentId, type);
		if (r != null) {
			flag = true;
		}
		return flag;
	}
	/**
	 * 创建资源文件夹
	 *
	 * @param groupId 柜子id
	 * @param name 资源名字
	 * @param parentId 父亲id
	 * @param ownerId 拥有者memberId
	 * @param ingoreCheckName 忽略检查文件名
	 * @return
	 * @throws GroupsException
	 */
	public long createResourceDir(long groupId, String name, long parentId,
			long ownerId, boolean ingoreCheckName) {
		if (!hasPesonalGroupAppPerm(groupId)) {
			if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), groupId,
					GroupPerm.ADD_FOLDER))
				throw PermissionsException.GroupException;
		}
		 return this.createResourceDir(groupId, name, parentId, ownerId,
				ingoreCheckName, GroupResource.DOCUMENT_TYPE_UNKNOWN);
	}
		
	/**
	 * 创建资源文件夹
	 *
	 * @param groupId 柜子id
	 * @param name 资源名字
	 * @param parentId 父亲id
	 * @param ownerId 拥有者memberId
	 * @param ingoreCheckName 忽略检查文件名
	 * @param documentType 文件夹类型
	 * @return
	 * @throws GroupsException
	 */
	public long createResourceDir(long groupId, String name, long parentId,
			long ownerId, boolean ingoreCheckName, int documentType) {
		 return this.createResourceDir(groupId, name, parentId, ownerId,
				ingoreCheckName, documentType, 0);
	}
	
	public long createResourceDir(long groupId, String name, long parentId,
			long ownerId, boolean ingoreCheckName, int documentType, double priority) {	
		if (!hasPesonalGroupAppPerm(groupId)) {
			if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), groupId,
					GroupPerm.ADD_FOLDER))
				throw PermissionsException.GroupException;
		}
		Group group = groupDao.getGroupById(groupId);
		Member owner = memberDao.getMemberById(ownerId);
		if (group == null || group.getId() <= 0) {
			throw new GroupsException(ResourceProperty.getAlternateKeyNotFoundString("Group", "id", groupId));
		}
		if (owner == null || owner.getId() <= 0) {
			throw new GroupsException(ResourceProperty.getAlternateKeyNotFoundString("Member", "id", ownerId));
		}
		GroupResource bean = new GroupResource();
		bean.setGroup(group);
		bean.setName(name);
		bean.setParentId(parentId);
		bean.setCreatorId(ownerId);
		bean.setDocumentTypeValue(documentType);
		bean.setResourceType(GroupResource.RESOURCE_TYPE_DIRECTORY);
		bean.setPriority(priority);
		return this.createResourceDir(bean, ingoreCheckName);
	}

	/**
	 * 创建资源文件夹,需要记录日志
	 * @param bean 资源(要有group，owner)
	 * @param ingoreCheckName 是否忽略检查名字
	 * @return
	 */
	public long createResourceDir(GroupResource bean, boolean ingoreCheckName) {
		if (!hasPesonalGroupAppPerm(bean.getGroup().getId())) {
			if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(),
					bean.getGroup().getId(), GroupPerm.ADD_FOLDER))
				throw PermissionsException.GroupException;
		}
		//==========更新path==========		
		String path = null;
		GroupResource temp;
		if (bean.getParentId() <= 0) {
			path = "/";
		} else {
			temp = resourceDao.getResourceById(bean.getParentId());
			if (temp.getPath() == null || temp.getPath().equals("")) {
				// rebuilt resource path 
				path = this.rebuiltPath(temp.getId());				
			} else {
				path = temp.getPath() + bean.getParentId() + "/";
			}
		}		
		bean.setPath(path);	
		//==========更新path==========

		// 若柜子已关闭，不可操作
		Group group = bean.getGroup();
		long ownerId = bean.getCreatorId();
		CheckUtil.checkNormalGroup(group);


		if (!ingoreCheckName) {
			// 检查资源文件夹名字是否合法
			CheckUtil.checkDirName(bean.getName());
		}
		

		// 检查资源文件夹深度
		this.checkDirectoryDepth(bean.getParentId());		

		bean.setCreateDate(new Timestamp(System.currentTimeMillis()));
		bean.setGroup(group);
		bean.setGroupName(group.getName());
		if (ownerId <= 0) {
			bean.setCreatorId(UserUtils.getCurrentMemberId());
			bean.setMemberName(UserUtils.getAccount());
		} else {
			Member memberBean = memberDao.getMemberById(ownerId);
			bean.setCreatorId(memberBean.getId());
			bean.setMemberName(memberBean.getName());
		}	
		String name = bean.getName();
		String filePreName = name;
		bean.setOriginalName(name);
		bean.setFilePreName(name);
		bean.setParentId(bean.getParentId());
		bean.setSize(0L);
		if (bean.getResourceType() == 0) {
			bean.setResourceType(GroupResource.RESOURCE_TYPE_DIRECTORY);
		}
		bean.setResourceStatus(GroupResource.RESOURCE_STATUS_NORMAL);
		bean.setDocumentTypeValue(GroupResource.DOCUMENT_TYPE_UNKNOWN);
		int i = 1;
		boolean isExist = this.checkResourceName(bean);
		while (isExist) {
			bean.setName(filePreName + "(" + i + ")");
			++i;
			if (i >= 100)
				throw new GroupsException(ResourceProperty
						.getCannotCreateResourceDirString());
			isExist = this.checkResourceName(bean);
		}
		resourceDao.saveOrUpdateResource(bean);
		
		// 记录日志
		String desc = LogMessage.getResourceDirAdd(group.getDisplayName());
		logService.addOperateLog(Log.ACTION_TYPE_ADD, desc, bean.getId(), bean.getName());
		return bean.getId();
		
	}
	
	/**
	 * 修改资源文件夹，只允许修改文件夹名字和描述
	 * @param id
	 * @param newName
	 * @param newDesc
	 * @throws GroupsException
	 */
	public void modifyResource(long id, String newName, String newDesc){
		GroupResource bean = resourceDao.getResourceById(id);
		if (!hasPesonalGroupAppPerm(bean.getGroup().getId())) {
			if (!permission.isAdmin(UserUtils.getCurrentMemberId())
					&& !permission.isGroupManager(UserUtils.getCurrentMemberId(),
							bean.getGroup().getId())
					&& bean.getCreatorId() != UserUtils.getCurrentMemberId()
					&& !permission.hasGroupPerm(UserUtils.getCurrentMemberId(),
							bean.getGroup().getId(), GroupPerm.MODIFY_RESOURCE)){
				throw PermissionsException.GroupException;
			}
		}
		CheckUtil.checkNormalGroup(bean.getGroup());
		CheckUtil.checkDirName(newName);

		if (bean.getResourceType() == GroupResource.RESOURCE_TYPE_FILE) {
			// 若是文件，先取出后缀名，后缀名不允许更改
			String suffix = this.split(bean.getName())[1];
			if (suffix.length() > 0)
				suffix = "." + suffix;
			bean.setName(newName + suffix);
			bean.setFilePreName(newName);
			bean.setFileExt(suffix);
			bean.setOriginalName(newName + suffix);

		} else {
			// 文件夹直接设置名字
			bean.setName(newName);
			bean.setFilePreName(newName);
			bean.setOriginalName(newName);
		}
		bean.setDesc(newDesc);
//		boolean  flag = this.checkResourceName(bean);
//		
//		if (flag) {
//			throw new GroupsException("此位置已存在同名项！");
//		}
		try {
			resourceDao.saveOrUpdateResource(bean);
		} catch (DataIntegrityViolationException e) {
			throw new GroupsException("此位置已存在同名项！");
		}
		
		
		// 记录日志
		Group groupBean = bean.getGroup();
		String desc="";
		if(bean.getResourceType() == GroupResource.RESOURCE_TYPE_DIRECTORY)
		   desc = LogMessage.getResourceDirMod(groupBean.getDisplayName(), bean.getName());

		//modify by mi
		if(bean.getResourceType() == GroupResource.RESOURCE_TYPE_FILE)
		     desc=LogMessage.getResourceMod(groupBean.getDisplayName(), bean.getName());
		logService.addOperateLog(Log.ACTION_TYPE_MODIFY, desc, id, bean.getName());
	}
	

	/**
	 * 修改资源文件的状态
	 * @param id 资源id
	 * @param status 正常或者删除,resourceStatus
	 */
	public void modifyResourceStatus(long id, String status) {
		GroupResource bean = resourceDao.getResourceById(id);
		if (!permission.isAdmin(UserUtils.getCurrentMemberId())
				&& !permission.hasGroupPerm(UserUtils.getCurrentMemberId(), bean
						.getGroup().getId(), GroupPerm.MANAGE_GROUP))
			throw PermissionsException.GroupException;
		CheckUtil.checkNormalGroup(bean.getGroup());

		bean.setResourceStatus(status);
		resourceDao.saveOrUpdateResource(bean);
		
		// 记录日志
		Group groupBean = bean.getGroup();
		String desc = LogMessage.getResourceStateMod(groupBean.getDisplayName(), bean.getName());
		logService.addOperateLog(Log.ACTION_TYPE_MODIFY, desc, id, bean.getName());
	}

	/**
	 * 删除资源文件夹，将会递归地将文件夹下的子文件夹和资源全部删除
	 * @param id 资源文件夹id
	 */
	public void deleteResourceDir(long id) {
		long groupId = 0;
		Group agroup = resourceDao.getResourceById(id).getGroup();
		if (agroup != null) {
			groupId = agroup.getId();
		}
		if (!hasPesonalGroupAppPerm(groupId)) {
			if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), groupId,
					GroupPerm.DELETE_RESOURCE))
				throw PermissionsException.GroupException;
		}
		GroupResource resourceBean = resourceDao.getResourceById(id);
		CheckUtil.checkNormalGroup(resourceBean.getGroup());
		try {
			System.out.println(" Using new delete method!");
			deleteReresourceDir_batch(id);
			return;
		} catch (Exception ee) {
			System.out.println("Catch Exception "+ee.toString()+ ", Using old delete method!");
			List<Long> idList = new ArrayList<Long>();
			idList.add(id);
			StringBuffer hql = new StringBuffer();
			hql.append("from GroupResource where id = ")
					.append(resourceBean.getId()).append(" or ").append(" path like '")
					.append(resourceBean.getPath() + resourceBean.getId()).append("/%'");

			final List<GroupResource> beans = genericDao.findAll(hql.toString());

			for (GroupResource bean : beans) {
				// 若是文件则删除文件
				if (bean.getResourceType() == GroupResource.RESOURCE_TYPE_FILE) {
					deleteResource(bean.getId());
				}

			}

			hql = new StringBuffer();
			hql.append("delete from GroupResource where id = ").append(resourceBean.getId());
			javax.persistence.Query query = genericDao.createQuery(hql.toString());
			query.executeUpdate();

		}
		// 记录日志
		Group groupBean = resourceBean.getGroup();
		//新增容量by mi
		groupBean.setAvailableCapacity(groupBean.getAvailableCapacity()+resourceBean.getSize());
		groupBean.setUsedCapacity(groupBean.getTotalFileSize()-groupBean.getAvailableCapacity());
		groupDao.saveOrUpdateGroup(groupBean);
		String desc = LogMessage.getResourceDirDelete(groupBean.getDisplayName(), resourceBean.getOriginalName());
		logService.addOperateLog(Log.ACTION_TYPE_DELETE, desc, id, resourceBean.getOriginalName());

	}

	
	/**
	 * 移动资源文件
	 * @param id
	 * @param parentId
	 * @param groupId 移入的柜子
	 * @param recycler 是否放到回收站，null忽略判断false表示还原
	 * @throws GroupsException
	 */
	public void moveResource(long id, long parentId, long groupId,Boolean recycler) throws GroupsException {
		//权限检查
		this.checkMoveResourcePerm(id, parentId, groupId, recycler);
		if (parentId < 0) {
			parentId = 0;
		}
		Group targetGroup = groupDao.getGroupById(groupId);	
		CheckUtil.checkNormalGroup(targetGroup);
		List<GroupResource> toIndexBeanList = new ArrayList<GroupResource>();
		boolean restore = recycler != null && !recycler.booleanValue();// 是否回收站还原操作
		GroupResource bean = resourceDao.getResourceById(id);
		if(bean.getResourceType()==GroupResource.RESOURCE_TYPE_DIRECTORY&&!checkCopyOrMoveable(bean,groupId,parentId)){
			throw new GroupsException("不能移动到到源文件夹的子文件夹中");
		}
		
		if (restore) {
			parentId = bean.getPreParentId() == null ? 0L : bean.getPreParentId().longValue();
			groupId = bean.getGroup().getId();
		}
		// parentId必须是一个目录
		if (parentId > 0) { 
			GroupResource parentBean = null;
			parentBean = resourceDao.getResourceById(parentId);
			if (parentBean == null || parentBean.getId() <= 0) {
				throw new GroupsException(ResourceProperty.getResourceNotExistsString());
			}
			if (parentBean.getResourceType() != GroupResource.RESOURCE_TYPE_DIRECTORY)
				throw new GroupsException(ResourceProperty.getNotResourceDirectoryString());
		}
		// 若圈子已关闭，不可操作
		Group preGroup = bean.getGroup();
		long preGroupId = preGroup.getId();
		CheckUtil.checkNormalGroup(preGroup);
		if (preGroupId != groupId) {
			if (bean.getResourceType() == GroupResource.RESOURCE_TYPE_FILE) {
				this.checkSingleFileSize(bean, groupId);
				this.checkGroupResourceSpace(bean, groupId);
			}
		}

		// 如果是回收站恢复 ---20150316
		if (preGroupId == groupId && recycler != null && !recycler.booleanValue()) {
			if (bean.getResourceType() == GroupResource.RESOURCE_TYPE_FILE) {
				this.checkGroupResourceSpace(bean, groupId);
			}
		}

		long resourceTotalSize = 0;
		// 不能移动到该资源的子孙文件夹中，检查parentId是否是id的子孙
		if (id == parentId)
			throw new GroupsException(ResourceProperty.getCannotMoveResourceString());

		if (preGroupId == groupId && bean.getParentId() == parentId) {
			return;
		}
		// if (parentId > 0 || bean.getGroupId() != groupId) {
		List<Long> list = new ArrayList<Long>();
		list.add(id);

		for (int i = 0; i < list.size(); ++i) {
			// 如果是目录，取出其子文件夹，若是文件则肯定childrenBeans.length==0
			List<GroupResource> childrenBeans = this.getResourcesByParent(list.get(i));

			for (GroupResource resourceBean : childrenBeans) {
				if (parentId == resourceBean.getId())
					throw new GroupsException(ResourceProperty.getCannotMoveResourceString());

				// 如果是跨柜移动，要修改子文件所在groupId
				if (resourceBean.getGroup().getId() != groupId) {
					// 如果是文件
					if (bean.getResourceType() == GroupResource.RESOURCE_TYPE_FILE) {
						// 检查文件大小是否超出限制
						checkSingleFileSize(resourceBean, groupId);
						resourceTotalSize = resourceTotalSize + resourceBean.getSize();
					}
					resourceBean.setGroup(targetGroup);
					resourceDao.saveOrUpdateResource(resourceBean);
					toIndexBeanList.add(resourceBean);
				} else {
					this.setResourceStatus(toIndexBeanList, resourceBean, recycler);
				}
				// 根据是否放到回收站设置资源的删除状态
				list.add(resourceBean.getId());

			}
		}
		// }
		// 跨圈子移动文件夹，检查空间是否足够
		if (preGroupId != groupId && bean.getResourceType() == GroupResource.RESOURCE_TYPE_DIRECTORY) {
			long totalSize = getResourceSpaceSize(groupId);
			long currentSize = getResourcesSize(groupId);
			if (currentSize + resourceTotalSize > totalSize)
				throw new GroupsException(ResourceProperty.getNotEnoughResourceRoomString());
		}

		// 判断是否重名
		// 取得原父id
		long orignalParentId = bean.getParentId();
		String beanName = bean.getName();
		String origin=bean.getName();
		String filePreName = bean.getFilePreName();
		String originalName = bean.getOriginalName();
		String ext = bean.getFileExt();
		// 如何使放到回收站，忽略重复名称，直接改成随机
		if (recycler != null && recycler.booleanValue()) {
			String uid = java.util.UUID.randomUUID().toString();
			beanName = uid + ext;
			bean.setPreParentId(orignalParentId);
			bean.setOriginalName(bean.getName());
		} else {
			// 从回收站还原
			if (recycler != null && !recycler.booleanValue()) {
				beanName = originalName;
				bean.setPreParentId(null);
			}
			
			int i = 1;
			while (true) {
				GroupResource existResource = resourceDao.getResourceByDetails(groupId, beanName, parentId, bean.getResourceType());
				if (existResource != null && bean.getResourceType()== GroupResource.RESOURCE_TYPE_FILE) {
					beanName = filePreName + "(" + i + ")" + ext;
					++i;
					if (i >= 100) {
						String uid = java.util.UUID.randomUUID().toString();
						beanName = uid + ext;
					}
				}
				if (existResource != null && bean.getResourceType()== GroupResource.RESOURCE_TYPE_DIRECTORY) {
					beanName = filePreName + "(" + i + ")";
					++i;
					if (i >= 100) {
						String uid = java.util.UUID.randomUUID().toString();
						beanName = uid;
					}
				}else {
					break;
				}
			}
		}

		// 更新父id
		bean.setParentId(parentId);
		bean.setName(beanName);
		bean.setGroup(targetGroup);
		// ==========更新path==========
		String path = null;
		if (parentId <= 0) {
			path = "/";
		} else {
			GroupResource temp = getResourceById(parentId);
			if (temp.getPath() == null || temp.getPath().equals("")) {
				// rebuilt resource path
				path = this.rebuiltPath(temp.getId());
			} else {
				path = temp.getPath() + parentId + "/";
			}
		}
		bean.setPath(path);
		

		// ==========更新path==========
		this.setResourceStatus(toIndexBeanList, bean, recycler);
		resourceDao.saveOrUpdateResource(bean);
		
		try {
			this.rebuiltChildPath(id);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new GroupsException("子文件夹或者子文件重建path失败");
		}
		// 记录日志
		Group source = bean.getGroup();
		Group target = groupDao.getGroupById(groupId);
//		String desc = LogMessage.getResourceMove(source.getDisplayName(), bean.getName(), target.getDisplayName());
		//modify by mi
		String desc = LogMessage.getResourceMove(source.getDisplayName(), origin, target.getDisplayName());
		logService.addOperateLog(Log.ACTION_TYPE_MOVE, desc, bean.getId(), origin);
//		logService.addOperateLog(Log.ACTION_TYPE_MOVE, desc, bean.getId(), bean.getName());
		// 更新文件夹统计

		 if (orignalParentId > 0) {
			 this.statResourceDirSize(orignalParentId); 
		 } 
		 if (parentId > 0) { 
			 this.statResourceDirSize(parentId); 
		 }
		 
		long Parent_tempId = orignalParentId;
		GroupResource parentBean;
		while (Parent_tempId > 0) {
			parentBean = resourceDao.getResourceById(Parent_tempId);
			this.statResourceDirSize(Parent_tempId);
			Parent_tempId = parentBean.getParentId();
		}
		Parent_tempId = parentId;
		while (Parent_tempId > 0) {
			parentBean = resourceDao.getResourceById(Parent_tempId);
			this.statResourceDirSize(Parent_tempId);
			Parent_tempId = parentBean.getParentId();
		}
		//修改可用容量
		System.out.println("之前柜子的id:"+preGroup.getId());
		System.out.println("目标柜子的id1:"+targetGroup.getId());
		if(preGroup.getId()!=targetGroup.getId()){
			long beansize=bean.getSize();
			long pregroupavai=preGroup.getAvailableCapacity();
			long targetgroupavai=targetGroup.getAvailableCapacity();
			preGroup.setAvailableCapacity(pregroupavai+beansize);
			preGroup.setUsedCapacity(preGroup.getTotalFileSize()-preGroup.getAvailableCapacity());
			targetGroup.setAvailableCapacity(targetgroupavai-beansize);
			targetGroup.setUsedCapacity(targetGroup.getTotalFileSize()-targetGroup.getAvailableCapacity());
			groupDao.saveOrUpdateGroup(preGroup);
			groupDao.saveOrUpdateGroup(targetGroup);
		}


	}


	private void checkMoveResourcePerm(long id, long parentId, long groupId, Boolean recycler) {
		GroupResource bean = resourceDao.getResourceById(id);
		//如果不是系统管理员
		if (!permission.isAdmin(UserUtils.getCurrentMemberId()) || !hasPesonalGroupAppPerm(bean.getGroup().getId())) {
			if (recycler != null && !recycler.booleanValue()) {
				groupId = bean.getGroup().getId();
			}
			//删除
			if (recycler != null && recycler.booleanValue()) {
				if (!permission.isGroupManager(UserUtils.getCurrentMemberId(), bean
						.getGroup().getId())
					&& !permission.hasGroupPerm(UserUtils.getCurrentMemberId(), bean
							.getGroup().getId(), GroupPerm.DELETE_RESOURCE))
					throw PermissionsException.GroupException;
			}
			//如果不是圈子管理员并且没有修改权限，报错
			if (!permission.isGroupManager(UserUtils.getCurrentMemberId(), bean
					.getGroup().getId())
				&& bean.getCreatorId() != UserUtils.getCurrentMemberId()
					&& !permission.hasGroupPerm(UserUtils.getCurrentMemberId(), bean
						.getGroup().getId(), GroupPerm.MODIFY_RESOURCE))
				throw PermissionsException.GroupException;
			//如果是跨柜移动，并且不是目标柜子管理员
			if (bean.getGroup().getId() != groupId && !permission.isGroupManager(UserUtils.getCurrentMemberId(), groupId)) {
				if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(),groupId, GroupPerm.UPLOAD_RESOURCE))
					throw PermissionsException.GroupException;

				// 确保用户在目标圈子有共享资源的权限
				if (bean.getResourceType() == GroupResource.RESOURCE_TYPE_DIRECTORY) {
					if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), groupId,
							GroupPerm.ADD_FOLDER)) {
						throw PermissionsException.GroupException;
					}
				}
			}
		}
	}
		
	/**
	 * 根据是否放到回收站设置资源的删除状态
	 *
	 * @param toIndexBeanList
	 * @param resourceBean
	 * @param recycler
	 */
	private void setResourceStatus(List<GroupResource> toIndexBeanList, GroupResource resourceBean,
			Boolean recycler) {
		if (recycler != null) {
			// 如果是放到回收站
			if (recycler.booleanValue()) {
				if (GroupResource.RESOURCE_STATUS_NORMAL.equals(resourceBean.getResourceStatus())) {
					resourceBean.setResourceStatus(GroupResource.RESOURCE_STATUS_DELETE);
					toIndexBeanList.add(resourceBean);
				}

			} else {
				// 如果不是放到回收站，判断
				if (GroupResource.RESOURCE_STATUS_DELETE.equals(resourceBean.getResourceStatus())) {
					resourceBean.setResourceStatus(GroupResource.RESOURCE_STATUS_NORMAL);
					toIndexBeanList.add(resourceBean);
				}
			}
		}
	}
	/**
	 * 检查是否可以复制或者移动
	 * @param resource 待操作资源
	 * @param groupId 目标柜子id
	 * @param parentId 目标父亲文件夹id
	 * @return
	 */
	private boolean checkCopyOrMoveable(GroupResource resource,Long groupId,Long parentId){
		if(resource==null){
			throw new GroupsException("找不到资源文件");
		}
		if(resource.getResourceType()==GroupResource.RESOURCE_TYPE_DIRECTORY){
			//如果被复制的是文件夹（源文件夹），则需要判断目标位置是否是源文件夹本身或子文件夹
			if(resource.getGroup().getId().longValue()==groupId){
				if(resource.getId().longValue()==parentId){
					//目标是本身
					return false;
				}
				if(parentId==0){
					return true;//目标是柜子根目录
				}
				if(parentId>0){
					//目标是柜子中的文件夹
					GroupResource targetFolder = this.getResourceById(parentId);
					if(targetFolder==null){
						throw new GroupsException("找不到目标文件夹");
					}
					long[] parentPathIds = targetFolder.getParentIdsByPath();
					if(parentPathIds!=null){
						for(long pid:parentPathIds){
							if(pid==resource.getId()){
								return false;
							}
						}
					}
					
				}	
			}
		}
		return true;
	}

	/**
	 * 上传资源
	 *
	 * @param file
	 * @param resourceBean
	 * @throws GroupsException
	 */
	public void uploadResource(File file, GroupResource resourceBean) throws GroupsException {
		if (!hasPesonalGroupAppPerm(resourceBean.getGroup().getId())) {
			if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(),
					resourceBean.getGroup().getId(), GroupPerm.UPLOAD_RESOURCE))
				throw PermissionsException.GroupException;
		}
		this.uploadResource(file, resourceBean, (Boolean) null);
	}

	
	private void uploadResource(File file, GroupResource resourceBean, Boolean encrypt) throws GroupsException {
		if (resourceBean == null) {
			throw new GroupsException(ResourceProperty.getCannotUploadFileString());
		}
		resourceBean.setRate(1);
		resourceBean.setFinishSign(GroupResource.UPLOAD_FINISHED);
		this.createResource(resourceBean);
		if (file != null) {
			String ext = resourceBean.getFileExt();
			boolean en = PropertyUtil.isIngoreEncrypt(ext);
			if (!en && encrypt != null && !encrypt.booleanValue()) {
				en = true;
			}
			String filePath = FilePathUtil.getFileFullPath(resourceBean);
			try {
				FileUtil.copyFileToServer(file, filePath, en);
				// 记录日志
				Group groupBean = resourceBean.getGroup();
				logService.addUploadLog(groupBean.getId(), groupBean.getDisplayName(),
						resourceBean.getId(), resourceBean.getOriginalName());
			} catch (Exception ex) {
                System.out.println("资源ID："+resourceBean.getId());
                System.out.println("资源柜子ID："+resourceBean.getGroup().getId());
				// 上传文件失败，删除记录
				this.deleteResource(resourceBean.getId());
			}
		}
	}

	/**
	 * 上传资源
	 *
	 * @throws GroupsException
	 */
	public void uploadResource(byte[] data, GroupResource resourceBean) throws GroupsException {
		if (!hasPesonalGroupAppPerm(resourceBean.getGroup().getId())) {
			if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(),
					resourceBean.getGroup().getId(), GroupPerm.UPLOAD_RESOURCE))
				throw PermissionsException.GroupException;
		}
		if (data == null || resourceBean == null) {
			throw new GroupsException(ResourceProperty.getCannotUploadFileString());
		}
		resourceBean.setRate(1);
		resourceBean.setFinishSign(GroupResource.UPLOAD_FINISHED);
		this.createResource(resourceBean);
		if (data != null) {
			String ext = resourceBean.getFileExt();
			boolean en = PropertyUtil.isIngoreEncrypt(ext);
			String filePath = FilePathUtil.getFileFullPath(resourceBean);
			try {
				FileUtil.writeToServer(data, filePath, en);
				// 记录日志
				Group groupBean = resourceBean.getGroup();
				logService.addUploadLog(groupBean.getId(), groupBean.getDisplayName(),
						resourceBean.getId(), resourceBean.getOriginalName());
			} catch (Exception ex) {
				// 上传文件失败，删除记录
				this.deleteResource(resourceBean.getId());
			}
		}
	}
//	/**
//	 * 下载资源
//	 *
//	 * @param id
//	 * @param os
//	 * @throws ServiceException
//	 */
//	void downloadResource(long[] ids, OutputStream os) throws ServiceException;
	/**
	 * 不用判断权限直接下载资源，分享外链下载用
	 *
	 * @param os
	 * @throws GroupsException
	 */
	public void directDownloadResource4Web(Long[] ids, OutputStream os) throws GroupsException {
		if (os == null || ids == null || ids.length == 0) {
			throw new GroupsException(ResourceProperty.getCannotDownloadFileString());
		}

		GroupResource[] beans = new GroupResource[ids.length];
		for (int i = 0; i < beans.length; ++i) {
			beans[i] = resourceDao.getResourceById(ids[i]);

			// 若圈子已关闭，不可操作
			CheckUtil.checkNormalGroup(beans[i].getGroup());
		}

		// 多个文件，打包下载
		if (beans.length > 1) {
			this.zipDownload(beans, os);
		}

		// 单个文件时
		if (beans.length == 1) {
			// 若为目录，打包下载
			if (beans[0].getResourceType() == GroupResource.RESOURCE_TYPE_DIRECTORY) {
				this.zipDownload(beans, os);
			}
			// 若为文件，则直接下载
			if (beans[0].getResourceType() == GroupResource.RESOURCE_TYPE_FILE) {
				FileUtil.downloadFile(FilePathUtil.getFileFullPath(beans[0]), os);
			}
			// 记录日志
			Group groupBean = beans[0].getGroup();
			logService.addDownloadLog4System(beans[0].getGroup().getId(), groupBean.getDisplayName(),
					beans[0].getId(), beans[0].getName());
		}
	}
	

	/**
	 * 获得待下载资源,分享外链下载用
	 * @param ids
	 * @return
	 * @throws GroupsException
	 */
	public File getDownloadResource(long[] ids) throws GroupsException {
		if (ids == null || ids.length == 0) {
			throw new GroupsException(ResourceProperty.getCannotDownloadFileString());
		}
		File zipFile = null;
		GroupResource[] beans = new GroupResource[ids.length];
		String zipName = "";
		for (int i = 0; i < beans.length; ++i) {
			beans[i] = resourceDao.getResourceById(ids[i]);
			zipName = zipName + "_" + ids[i];
			// 若圈子已关闭，不可操作
			CheckUtil.checkNormalGroup(beans[i].getGroup());
		}
		zipName = zipName + ".zip";
		if (zipName.length() > 255) {
			throw new GroupsException("File name too long");
		}
		// 多个文件，打包下载
		if (beans.length > 1 || (beans.length == 1 && beans[0].getResourceType() == GroupResource.RESOURCE_TYPE_DIRECTORY)) {
			if (FileUtil.isTempDownFileExists(zipName)) {
				zipFile = FileUtil.getTempDownFile(zipName);
			} else {
				try {
					zipFile = FileUtil.createTempDownFile(zipName);
				} catch (IOException e) {
					throw new GroupsException("Create temp file fail");
				}
				zipDownload(beans, zipFile);
				zipFile = FileUtil.getTempDownFile(zipName);
			}
		}
		// 单个文件时
		if (beans.length == 1) {
			// 若为文件，则直接下载
			if (beans[0].getResourceType() == GroupResource.RESOURCE_TYPE_FILE) {
				try {
					zipFile = new File(new URI(FilePathUtil.getFileFullPath(beans[0])));
					
					// 记录日志
					Group groupBean = beans[0].getGroup();
					logService.addDownloadLog(groupBean.getId(), groupBean.getDisplayName(),
							beans[0].getId(), beans[0].getName());
				} catch (URISyntaxException e) {
					throw new GroupsException(e);
				}
			}
		}
		return zipFile;
	}
//	/**
//	 * 查看缩略图
//	 * @param id
//	 * @param os
//	 * @throws ServiceException
//	 */
//	void viewThumbnail(long id, OutputStream os) throws ServiceException;
//	/**
//	 * 下载缩略图
//	 * @param resourceBean
//	 * @param os
//	 * @throws ServiceException
//	 */
//	public void downloadThumbnail(GroupResource resourceBean,OutputStream os) throws ServiceException;


	/**
	 * 
	 * 根据柜子获取资源
	 * @param groupId 柜子id
	 * @param start 分页开始
	 * @param limit 分页大小
	 * @return
	 * @throws Exception 
	 */
	public List<GroupResource> getResourcesByGroup(long groupId, int start, int limit, boolean top) throws Exception {
		return this.getResourcesByGroup(groupId, start, limit, top, null);
	}
	
	/**
	 * 根据柜子获取资源
	 * @param groupId 柜子id
	 * @param start 分页开始
	 * @param limit 分页大小
	 * @param top 是否只获取第一级资源列表
	 * @param orderBy 排序
	 * @return
	 */
	public List<GroupResource> getResourcesByGroup(long groupId, int start, int limit, boolean top, String orderBy) throws Exception {
		if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), groupId, GroupPerm.VIEW_RESOURCE))
			throw PermissionsException.GroupException;
		// 若圈子已关闭，不可访问，除非是管理员
		if (!permission.isAdmin(UserUtils.getCurrentMemberId())) {
			CheckUtil.checkNormalGroup(groupDao.getGroupById(groupId));
		}
		final PageNavigater<GroupResource> pageNavigater = new PageNavigater<GroupResource>(
				new AndSearchTerm(), new SortTerm(), new PageTerm(), resourceDao);

		// 重置searchTerm
		pageNavigater.getSearchTerm().clear();
		pageNavigater.getSearchTerm()
				.add(new SearchItem<Long>(ResourceSearchItemKey.GroupId, SearchItem.Comparison.EQ, groupId));
		pageNavigater.getSearchTerm().add(new SearchItem<String>(ResourceSearchItemKey.Status, SearchItem.Comparison.EQ,
				GroupResource.RESOURCE_STATUS_NORMAL));
		if (top) {
			pageNavigater.getSearchTerm()
					.add(new SearchItem<Long>(ResourceSearchItemKey.ParentId, SearchItem.Comparison.EQ, 0L));
			// 获得非回收站资源
			pageNavigater.getSearchTerm().add(new SearchItem<String>(ResourceSearchItemKey.Name,
					SearchItem.Comparison.NE, PropertyUtil.getRecyclerName()));
		}

		// 重置sortTerm，先按优先级排序，再按创建时间排序
		pageNavigater.getSortTerm().clear();
		pageNavigater.getSortTerm().add(new DescSortItem(ResourceSortItemKey.DefaultFolder));
		pageNavigater.getSortTerm().add(new AscSortItem(ResourceSortItemKey.Type));
		if (orderBy != null && orderBy.trim().length() > 0) {
			String[] bys = orderBy.split(",");
			for (String order : bys) {
				String[] o = order.split(" ");
				if (o.length == 1) {
					pageNavigater.getSortTerm().add(new AscSortItem(new ResourceSortItemKey(o[0])));

				} else if (o.length == 2) {
					if ("DESC".equals(o[1].toUpperCase())) {
						pageNavigater.getSortTerm().add(new DescSortItem(new ResourceSortItemKey(o[0])));
					} else {
						pageNavigater.getSortTerm().add(new AscSortItem(new ResourceSortItemKey(o[0])));
					}
				}
			}
		}

		pageNavigater.getSortTerm().add(new AscSortItem(ResourceSortItemKey.Priority));
		pageNavigater.getSortTerm().add(new DescSortItem(ResourceSortItemKey.CreationDate));

		// 重置pageTerm
		pageNavigater.getPageTerm().setBeginIndex(CheckUtil.reviseStart(start));
		pageNavigater.getPageTerm().setPageSize(CheckUtil.reviseLimit(limit, PropertyUtil.getResourceDefaultPageSize()));
		GroupResource[] result = pageNavigater.getContent();
		return Arrays.asList(result);
	}
	
	/**
	 * 获取顶级normal资源总数
	 * @param groupId
	 * @return
	 */
	public long getTopResourcesAmountByGroup(long groupId) {
		Long amount = resourceDao.getTopResourcesAmountByGroup(groupId, GroupResource.RESOURCE_STATUS_NORMAL);
		//减去回收站
		if (amount != null) {
			return amount-1;
		}
		return 0L;
	}
	
	


//	
//	/**
//	 * 根据父id获取资源列表
//	 *
//	 * @param parentId
//	 * @param type 资源类型，null表示全部
//	 * @param start
//	 * @param limit
//	 * @return
//	 * @throws ServiceException
//	 */
//	IResult<GroupResource> getResourcesByParent(long parentId, GroupResource.Type type,
//			GroupResource.Status status,int start, int limit) throws ServiceException;
//	
//	IResult<GroupResource> getResourcesByParent(long parentId, GroupResource.Type type,
//			GroupResource.Status status,int start, int limit,String orderBy) throws ServiceException;
//
//	/**
//	 * 根据父id获取资源列表，不做权限控制
//	 *
//	 * @param parentId
//	 * @param type 资源类型，null表示全部
//	 * @param start
//	 * @param limit
//	 * @return
//	 * @throws ServiceException
//	 */
//	IResult<GroupResource> directGetResourcesByParent(long parentId, GroupResource.Type type,
//			GroupResource.Status status,int start, int limit) throws ServiceException;
//
	/**
	 * 删除资源
	 * @param id
	 * @throws GroupsException
	 */
	public void deleteResource(long id) throws GroupsException {
		long groupId = 0;
		GroupResource r = resourceDao.getResourceById(id);
		Group agroup = r.getGroup();
		if (agroup != null) {
			groupId = agroup.getId();
		}
		if (!hasPesonalGroupAppPerm(groupId)) {
			if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), groupId,
					GroupPerm.DELETE_RESOURCE))
				throw PermissionsException.GroupException;
		}
		

		GroupResource resourceBean = null;

		// 确定resource是否存在
		resourceBean = resourceDao.getResourceById(id);
		if (resourceBean == null || resourceBean.getId() <= 0) {
			return;
		}
		// 若圈子已关闭，不可操作
		CheckUtil.checkNormalGroup(resourceBean.getGroup());
		// 删除数据库表项，删除表项需在删除文件之前，因为出现异常的话，数据库可以回滚
		resourceDao.deleteResource(resourceBean);

		long parentId = resourceBean.getParentId();
		if (parentId > 0) {
			this.statResourceDirSize(parentId);
		}

		if (resourceBean.getResourceType() == GroupResource.RESOURCE_TYPE_FILE) {
			// 删除服务器文件
			String file = FilePathUtil.getFileFullPath(resourceBean);
			FileUtil.deleteFile(file);
		}

		if (resourceBean.getResourceType() == GroupResource.RESOURCE_TYPE_FILE) {
			// 删除缩略图
			if (resourceBean.getThumbnail() != null) {
				String thumbnail = FilePathUtil.getFileFullPath(resourceBean.getCreatorId(), resourceBean.getThumbnail());
				FileUtil.deleteFile(thumbnail);
			}
		}

		// 记录日志
		Group groupBean = resourceBean.getGroup();
		//新增容量
		groupBean.setAvailableCapacity(groupBean.getAvailableCapacity()+resourceBean.getSize());
		groupBean.setUsedCapacity(groupBean.getTotalFileSize()-groupBean.getAvailableCapacity());
		groupDao.saveOrUpdateGroup(groupBean);
		String desc = LogMessage.getResourceDelete(groupBean.getDisplayName(), resourceBean.getOriginalName());
		logService.addOperateLog(Log.ACTION_TYPE_DELETE, desc, id, resourceBean.getOriginalName());
	}

	/**
	 * batch删除资源
	 * @param id
	 * @throws GroupsException
	 */
	public void deleteResource_batch(long id) throws GroupsException {

		GroupResource resourceBean = null;

		// 确定resource是否存在
		resourceBean = resourceDao.getResourceById(id);
		if (resourceBean == null || resourceBean.getId() <= 0) {
			return;
		}
		if (resourceBean.getResourceType() == GroupResource.RESOURCE_TYPE_FILE) {
			// 删除服务器文件
			String file = FilePathUtil.getFileFullPath(resourceBean);
			FileUtil.deleteFile(file);
		}

		if (resourceBean.getResourceType() == GroupResource.RESOURCE_TYPE_FILE) {
			// 删除缩略图
			if (resourceBean.getThumbnail() != null) {
				String thumbnail = FilePathUtil.getFileFullPath(resourceBean.getCreatorId(), resourceBean.getThumbnail());
				FileUtil.deleteFile(thumbnail);
			}
		}

		// 记录日志
		Group groupBean = resourceBean.getGroup();
		String desc = LogMessage.getResourceDelete(groupBean.getDisplayName(), resourceBean.getOriginalName());
		logService.addOperateLog(Log.ACTION_TYPE_DELETE, desc, id, resourceBean.getOriginalName());
	}

	/**
	 * 获取资源信息
	 *
	 * @param id
	 * @return
	 */
	public GroupResource getResourceById(long id) throws GroupsException{
		if(!permission.isReceivedResourceToMember(id, UserUtils.getCurrentMemberId())) {
			GroupResource resourceBean = resourceDao.getResourceById(id);
			if(resourceBean == null)
			    throw new GroupsException("id为" +id+"的资源不存在");
			if (!hasPesonalGroupAppPerm(resourceBean.getGroup().getId())) {
				if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(),
						resourceBean.getGroup().getId(), GroupPerm.VIEW_RESOURCE))
					throw PermissionsException.GroupException;
			}
		}
		return resourceDao.getResourceById(id);
	}
	
//	/**
//	 * 直接获取资源信息，不用做权限判断
//	 *
//	 * @param id
//	 * @return
//	 * @throws ServiceException
//	 */
//	GroupResource directGetResource(long id) throws ServiceException;
//	
//
//	/**
//	 * 共享资源到另外一个圈子
//	 *
//	 * @param resourceId
//	 *            资源id
//	 * @param groupId
//	 *            目标圈子id
//	 * @param parentId
//	 *            目标圈子资源父id
//	 * @throws ServiceException
//	 */
//	void shareResource(long resourceId, long groupId, long parentId)
//			throws ServiceException;

	/**
	 * 复制资源到指定位置
	 *	新建了数据库记录和文件
	 * @param resourceId
	 *            资源id
	 * @param groupId
	 *            目标柜子id
	 * @param parentId
	 *            目标柜子资源父id
	 * @param memberId
	 *           操作者id
	 * @throws GroupsException
	 */
	public long copyResource(long resourceId, long groupId, long parentId, long memberId) throws GroupsException {
		GroupResource resourceBean = resourceDao.getResourceById(resourceId);
		//如果是非共享给我的资源，查看权限
		if (!permission.isReceivedResourceToMember(resourceId, UserUtils.getCurrentMemberId())) {
			if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), groupId,
					GroupPerm.UPLOAD_RESOURCE)) {
				throw PermissionsException.GroupException;
			}
		}

		// 确保用户在目标圈子有共享资源的权限
		if (resourceBean.getResourceType() == GroupResource.RESOURCE_TYPE_DIRECTORY) {
			if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), groupId,
					GroupPerm.ADD_FOLDER)) {
				throw PermissionsException.GroupException;
			}
		}

		Group targetGroup = groupDao.getGroupById(groupId);	
		CheckUtil.checkNormalGroup(targetGroup);
		long id = 0L;
		if (resourceBean.getResourceType() == GroupResource.RESOURCE_TYPE_LINK) {
			GroupResource bean = new GroupResource();
			bean.setGroup(targetGroup);
			bean.setName(resourceBean.getName());
			bean.setParentId(parentId);
			bean.setCreatorId(memberId);
			bean.setDocumentTypeValue(GroupResource.DOCUMENT_TYPE_UNKNOWN);
			bean.setLinkPath(resourceBean.getLinkPath());
			bean.setResourceType(GroupResource.RESOURCE_TYPE_LINK);
			this.createResourceDir(bean, false);
			long dirId = bean.getId();
			id = dirId;
		}
		else if (resourceBean.getResourceType() != GroupResource.RESOURCE_TYPE_FILE) {
			if(!checkCopyOrMoveable(resourceBean,groupId,parentId)){
				throw new GroupsException("不能复制到源文件夹的子文件夹中");
			}
			long dirId = this.createResourceDir(groupId, resourceBean.getName(), parentId, memberId, true,resourceBean.getDocumentTypeValue());
			id = dirId;
			// 源文件夹 和 复制后的文件夹ID 映射
			Map<Long, Long> dirIdMap = new HashMap<Long, Long>();
			List<Long> list = new ArrayList<Long>();
			list.add(resourceId);
			dirIdMap.put(resourceId, dirId);
			for (int i = 0; i < list.size(); ++i) {
				List<GroupResource> childrenBeans = this.getResourcesByParent(list.get(i));
				for (GroupResource child : childrenBeans) {
					// 如果是文件，直接复制
					if (child.getResourceType() == GroupResource.RESOURCE_TYPE_FILE) {
						_copyFile(memberId, child.getId(), groupId, dirIdMap.get(list.get(i)));
						//modify by mi
//						_copyFile_v2(memberId, child.getId(), groupId, dirIdMap.get(list.get(i)));
					} else {
						// 如果是文件夹，创建文件夹
						this.createResourceDir(groupId, child.getName(), dirIdMap.get(list.get(i)),
								memberId, true, child.getDocumentTypeValue());
						long tempId = resourceDao.getResourceByDetails(groupId, child.getName(), dirIdMap.get(list.get(i)), GroupResource.RESOURCE_TYPE_DIRECTORY).getId();
						list.add(child.getId());
						dirIdMap.put(child.getId(), tempId);
					}
				}
			}
		} else {
			id = _copyFile(memberId,resourceId, groupId, parentId);
			//modify by mi
//			id = _copyFile_v2(memberId,resourceId, groupId, parentId);
		}
		//减少目标柜子的容量
		long beansize=resourceBean.getSize();
		long targroupavai=targetGroup.getAvailableCapacity();
		System.out.println("复制到的目标柜子id:"+targetGroup.getId());
		targetGroup.setAvailableCapacity(targroupavai-beansize);
		targetGroup.setUsedCapacity(targetGroup.getTotalFileSize()-targetGroup.getAvailableCapacity());
		groupDao.saveOrUpdateGroup(targetGroup);
		// 记录日志
		Group source = resourceBean.getGroup();
		Group groupBean = groupDao.getGroupById(groupId);
		String desc = LogMessage.getResourceCopy(source.getDisplayName(), resourceBean.getOriginalName(),
				groupBean.getDisplayName());
		logService.addOperateLog(Log.ACTION_TYPE_COPY, desc, resourceBean.getId(), resourceBean.getOriginalName());
		return id;

	}
	
	/**
	 * 包括数据库记录和物理文件的copy
	 * @param memberId
	 * @param resourceId
	 * @param groupId
	 * @param parentId
	 * @return
	 * @throws GroupsException
	 */
	private long _copyFile(long memberId, long resourceId, long groupId, long parentId) throws GroupsException {
		GroupResource resourceBean = resourceDao.getResourceById(resourceId);

		if (resourceBean.getResourceType() != GroupResource.RESOURCE_TYPE_FILE) {
			throw new GroupsException(ResourceProperty.getCannotShareResourceDirString());
		}
		Group groupBean = groupDao.getGroupById(groupId);
		Member operator = memberDao.getMemberById(memberId);

		GroupResource sharedBean = new GroupResource();
		sharedBean.setContentType(resourceBean.getContentType());
		sharedBean.setCreateDate(new Timestamp(System.currentTimeMillis()));
		sharedBean.setDesc(resourceBean.getDesc());
		sharedBean.setGroup(groupBean);
		sharedBean.setGroupName(groupBean.getName());
		sharedBean.setCreatorId(memberId);
		sharedBean.setMemberName(operator.getName());
		sharedBean.setName(resourceBean.getName());
		sharedBean.setDetailSize(resourceBean.getDetailSize());
		sharedBean.setParentId(parentId < 0 ? 0 : parentId);
		// ==========更新path==========

		String path_temp = null;
		if (parentId <= 0) {
			path_temp = "/";
		} else {
			GroupResource temp = this.getResourceById(parentId);
			if (temp.getPath() == null || temp.getPath().equals("")) {
				// rebuilt resource path
				path_temp = this.rebuiltPath(temp.getId());
			} else {
				path_temp = temp.getPath() + parentId + "/";
			}
		}
		sharedBean.setPath(path_temp);

		// ==========更新path==========
		sharedBean.setSize(resourceBean.getSize());
		sharedBean.setResourceType(resourceBean.getResourceType());
		sharedBean.setResourceStatus(GroupResource.RESOURCE_STATUS_NORMAL);
		sharedBean.setCheckCode(resourceBean.getCheckCode());
		sharedBean.setReserveInfo(GroupResource.PROP_RESERVE_DOUBLE_FIELD_1,
				resourceBean.getReserveInfo(GroupResource.PROP_RESERVE_DOUBLE_FIELD_1));
		sharedBean.setReserveInfo(GroupResource.PROP_RESERVE_FIELD_1,
				resourceBean.getReserveInfo(GroupResource.PROP_RESERVE_FIELD_1));
		sharedBean.setReserveInfo(GroupResource.PROP_RESERVE_FIELD_2,
				resourceBean.getReserveInfo(GroupResource.PROP_RESERVE_FIELD_2));
		sharedBean.setRemark(resourceBean.getRemark());
		sharedBean.setLinkPath(resourceBean.getLinkPath());

		// 获取需要共享的文件路径
		try {
			String path = FilePathUtil.getFileFullPath(resourceBean);
			File file = new File(new URI(path));

			// 进行共享，即拷贝一份到目标圈子的目录下
			this.uploadResource(file, sharedBean, false);
		} catch (Exception e) {
			if (e instanceof GroupsException)
				throw (GroupsException) e;
			throw new GroupsException(e);
		}
		return sharedBean.getId();

	}

	/**
	 * 包括数据库记录和物理文件的copy
	 * @param memberId
	 * @param resourceId
	 * @param groupId
	 * @param parentId
	 * @return
	 * @throws GroupsException
	 */
	private long _copyFile_v2(long memberId, long resourceId, long groupId, long parentId) throws GroupsException {
		GroupResource resourceBean = resourceDao.getResourceById(resourceId);

		if (resourceBean.getResourceType() != GroupResource.RESOURCE_TYPE_FILE) {
			throw new GroupsException(ResourceProperty.getCannotShareResourceDirString());
		}
		Group groupBean = groupDao.getGroupById(groupId);
		Member operator = memberDao.getMemberById(memberId);

		GroupResource sharedBean = new GroupResource();
		sharedBean.setContentType(resourceBean.getContentType());
		sharedBean.setCreateDate(new Timestamp(System.currentTimeMillis()));
		sharedBean.setDesc(resourceBean.getDesc());
		sharedBean.setGroup(groupBean);
		sharedBean.setGroupName(groupBean.getName());
		sharedBean.setCreatorId(memberId);
		sharedBean.setMemberName(operator.getName());
		sharedBean.setName(resourceBean.getName());
		sharedBean.setDetailSize(resourceBean.getDetailSize());
		sharedBean.setParentId(parentId < 0 ? 0 : parentId);
		// ==========更新path==========

		String path_temp = null;
		if (parentId <= 0) {
			path_temp = "/";
		} else {
			GroupResource temp = this.getResourceById(parentId);
			if (temp.getPath() == null || temp.getPath().equals("")) {
				// rebuilt resource path
				path_temp = this.rebuiltPath(temp.getId());
			} else {
				path_temp = temp.getPath() + parentId + "/";
			}
		}
		sharedBean.setPath(path_temp);

		// ==========更新path==========
		sharedBean.setSize(resourceBean.getSize());
		sharedBean.setResourceType(resourceBean.getResourceType());
		sharedBean.setResourceStatus(GroupResource.RESOURCE_STATUS_NORMAL);
		sharedBean.setCheckCode(resourceBean.getCheckCode());
		sharedBean.setReserveInfo(GroupResource.PROP_RESERVE_DOUBLE_FIELD_1,
				resourceBean.getReserveInfo(GroupResource.PROP_RESERVE_DOUBLE_FIELD_1));
		sharedBean.setReserveInfo(GroupResource.PROP_RESERVE_FIELD_1,
				resourceBean.getReserveInfo(GroupResource.PROP_RESERVE_FIELD_1));
		sharedBean.setReserveInfo(GroupResource.PROP_RESERVE_FIELD_2,
				resourceBean.getReserveInfo(GroupResource.PROP_RESERVE_FIELD_2));
		sharedBean.setRemark(resourceBean.getRemark());
		sharedBean.setLinkPath(resourceBean.getLinkPath());
        sharedBean.setRate(1);
        sharedBean.setFinishSign(GroupResource.UPLOAD_FINISHED);
        this.createResource(sharedBean);
		// 获取需要共享的文件路径
		try {
			String path = FilePathUtil.getFileFullPath(resourceBean);
            System.out.println("_copyFile_v2的文件路径是："+path);
			File file = new File(new URI(path));
            System.out.println("文件是否存在："+(file.exists()?true:false));
			// 进行共享，即拷贝一份到目标圈子的目录下
			helpAsyncService.uploadResource(file, sharedBean, false);
		} catch (Exception e) {
			if (e instanceof GroupsException)
				throw (GroupsException) e;
			throw new GroupsException(e);
		}
		return sharedBean.getId();

	}
//	
//	
//	/**
//	 * 搜索资源
//	 *
//	 * @param query
//	 * @param sort
//	 * @param page
//	 * @return
//	 * @throws ServiceException
//	 */
//	SearchResult<GroupResource> searchResources(
//			SearchQuery<GroupResource> query, SearchFilter filter,
//			com.dcampus.ztools.zsearch.searcher.Sort sort, Page page)
//			throws ServiceException;
//
//	/**
//	 * 重建资源索引
//	 *
//	 * @param groupId
//	 * @throws ServiceException
//	 */
//	void rebuildResourceIndex(long groupId) throws ServiceException;
//	
//	/**
//	 * 修改资源拥有者
//	 *
//	 * @param oldMemberId
//	 * @param newMemberId
//	 * @throws ServiceException
//	 */
//	void modifyResourceOwner(long oldMemberId, long newMemberId)
//			throws ServiceException;
//	/**
//	 * 撤销删除马甲的资源
//	 * @param memberId
//	 * @throws ServiceException
//	 */
//	void deleteResourceByMember(long memberId) throws ServiceException;
	
	/**
	 * 获取资源
	 * @param groupId 柜子id
	 * @param name 资源名字
	 * @param parentId 父亲id
	 * @param type 资源类型
	 * @return
	 */
	public GroupResource getResource(long groupId, String name, long parentId, int type) {
		return resourceDao.getResourceByDetails(groupId, name, parentId, type);
	}
	
	/**
	 * 获得文件柜的回收站，如果没有，自动创建一个
	 * @param groupId
	 */
	public GroupResource getRecycler(long groupId) {
		Group groupBean = groupDao.getGroupById(groupId);
		GroupResource resource = this.getResource(groupId, PropertyUtil.getRecyclerName(), 0, GroupResource.RESOURCE_TYPE_DIRECTORY);
		if (resource == null || resource.getId() <= 0) {
			this.createResourceDir(groupId, PropertyUtil.getRecyclerName(), 0, groupBean.getCreatorId(), true);
			resource = this.getResource(groupId, PropertyUtil.getRecyclerName(), 0, GroupResource.RESOURCE_TYPE_DIRECTORY);
		}
		return resource;
	}

//	
//	void modifyResource(long id, double fileRate)throws ServiceException;
//
	public void createResource(GroupResource resourceBean)throws GroupsException {
		if (!hasPesonalGroupAppPerm(resourceBean.getGroup().getId())) {
			if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(),
					resourceBean.getGroup().getId(), GroupPerm.UPLOAD_RESOURCE))
				throw PermissionsException.GroupException;
		}
		CheckUtil.checkNormalGroup(resourceBean.getGroup());

		if (resourceBean == null)
		{
			throw new GroupsException(ResourceProperty.getCannotUploadFileString());
		}
		String[] nn = split(resourceBean.getName());
		String suffix = nn[1];
		String filePreName = nn[0];
		String originalName = resourceBean.getName();
		resourceBean.setFileExt("." + suffix);
		resourceBean.setOriginalName(originalName);
		resourceBean.setFilePreName(filePreName);

		// 检查文件后缀名
		checkSuffix(suffix);
		// 检查文件大小是否超出限制
		this.checkSingleFileSize(resourceBean, resourceBean.getGroup().getId());
		// 检查资源空间是否足够
		this.checkGroupResourceSpace(resourceBean, resourceBean.getGroup().getId());
		// 重命名资源
		renameResource(resourceBean);

		int i = 1;
		while (true) {
			boolean f = this.checkResourceName(resourceBean);
			if (f) {
				// 有此名字的资源，进行名字重命名

				// resourceBean.setName(s[0] + "(" + i + ")." + s[1]);
				resourceBean.setName(filePreName + "(" + i + ")." + suffix);
				++i;

				/*
				 * if (i >= 20) throw new ServiceException(ResourceProperty
				 * .getCannotUploadFileString());
				 */
			} else {
				try {
					// 防止多线程保存出错
					resourceDao.saveOrUpdateResource(resourceBean);

				} catch (Exception ee) {
					resourceDao.clear();

					String uid = java.util.UUID.randomUUID().toString();
					String bakName = filePreName + "(" + uid + ")." + suffix;

					resourceBean.setName(bakName);
					resourceDao.saveOrUpdateResource(resourceBean);

				}
				break;
			}

		}
		// 统计资源目录大小
		long parentId = resourceBean.getParentId();
		GroupResource parentBean;
		while (parentId > 0) {
			parentBean = resourceDao.getResourceById(parentId);
			this.statResourceDirSize(parentId);
			parentId = parentBean.getParentId();
		}
	}
//
//	void createShareResource(long groupId, IGroupResourceShareBean share, long[] memberIds, String[] names)throws ServiceException;
//
//	public void revokeShareResourceToMember(long[] ids) throws ServiceException;
//
//	public void deleteSharedMember(long shareId, long[] ids)throws ServiceException;
//
//	public void deleteResourceReceiveBean(long[] receiveId) throws ServiceException;
//
//	public IGroupResourceShareBean getResourceShareBean(long id)throws ServiceException;
//
//	public IGroupResourceReceiveBean getResourceReceiveBean(long id) throws ServiceException;
//
//	public IGroupResourceReceiveBean[] getResourceReceiveBeans(long shareId)throws ServiceException;
//
//	public IResult<IGroupResourceReceiveBean> getMyReceiveResource(
//			final long providerId,final int start, final int limit) throws ServiceException;
//
//
//	public void addSharedMember(long shareBeanId,
//			long[] memberIds, String[] names)throws ServiceException;
//
//	public void modifyShareResource(long shareId,String remark,
//				long[] memberIds, String[] names)throws ServiceException;
//
//	public List<Object[]> getResourcePath(long resourceId) throws ServiceException;
//
//	public void onlinePreview(long resourceId,Writer writer) throws ServiceException;
//
//	/**
//	 * 修改资源
//	 * @param resource
//	 */
//	public void modifyResourceMetaData(GroupResource resource);

	/**
	 * 修改分类顺序号
	 * @param id
	 * @param order
	 */
	public void modifyResourceOrder(long id, double order) {
		GroupResource bean = resourceDao.getResourceById(id);
		bean.setPriority(order);
		resourceDao.saveOrUpdateResource(bean);
	}

	/**
	 * 更新资源
	 * @param resource
	 */
	public void modifyResource(GroupResource resource){
		if (!hasPesonalGroupAppPerm(resource.getGroup().getId())) {
			if (!permission.isAdmin(UserUtils.getCurrentMemberId())
					&& !permission.isGroupManager(UserUtils.getCurrentMemberId(),
							resource.getGroup().getId())
					&& resource.getCreatorId() != UserUtils.getCurrentMemberId()
					&& !permission.hasGroupPerm(UserUtils.getCurrentMemberId(),
							resource.getGroup().getId(), GroupPerm.MODIFY_RESOURCE))
				throw PermissionsException.GroupException;
		}
		resourceDao.saveOrUpdateResource(resource);
	}
//
//	public File getResourceForHtml5(long[] ids) throws ServiceException;
//	
//	public GroupResource[] getImageResources(long groupId, long parentId,int limit) throws ServiceException;
//	
//	GroupResource[] getResourcesByGroup(long groupId,GroupResource.Type type,
//			GroupResource.Status status);
//	
//	public File createCover(GroupResource resourceBean) throws ServiceException;
//	

//
//	String getResourceCheckCode(long id)throws ServiceException;
//	
//	void moveResourceReceiveBean(long[] receiveId, long groupId, long parentId) throws ServiceException;
//
//	void viewThumbnail_new(long id, int width, int height, int quality,
//			OutputStream outputStream);
//	
	public String getThumbnail(long id, int width, int height, int quality) {
		if (!permission.isReceivedResourceToMember(id, UserUtils.getCurrentMemberId())) {
			GroupResource resourceBean = resourceDao.getResourceById(id);
			if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), resourceBean
					.getGroup().getId(), GroupPerm.DOWNLOAD_RESOURCE))
				throw PermissionsException.GroupException;
		}
		GroupResource resourceBean = resourceDao.getResourceById(id);
		String thumbnail = Long.toString(resourceBean.getCreatorId()) + File.separator + "thum_"
				+ Integer.toString(width) + "x" + Integer.toString(height) + "x" + Integer.toString(quality) + "_"
				+ resourceBean.getFilePath();
		File thumbfile = new File(PropertyUtil.getGroupThumbnailRootPath() + File.separator + thumbnail);
		if (thumbfile.exists()) {
			try {
				createThumbnail_new(resourceBean, width, height, quality);
				thumbnail = Long.toString(resourceBean.getCreatorId()) + File.separator + "thum_"
						+ Integer.toString(width) + "x" + Integer.toString(height) + "x" + Integer.toString(quality)
						+ "_" + resourceBean.getFilePath();
			} catch (Exception e) {
				System.out.println(e.toString());
			}
			return thumbnail;
		} else {
			String suffix = split(resourceBean.getName())[1];
			if (isImageType(suffix)) {
				try {
					createThumbnail_new(resourceBean, width, height, quality);
					thumbnail = Long.toString(resourceBean.getCreatorId()) + File.separator + "thum_"
							+ Integer.toString(width) + "x" + Integer.toString(height) + "x" + Integer.toString(quality)
							+ "_" + resourceBean.getFilePath();
				} catch (Exception e) {
					System.out.println(e.toString());
				}
				return thumbnail;
			}
		}
		return null;
	}
	/**
	 * 生成预览图
	 * @param resourceBean
	 */
	public void createThumbnail_new(GroupResource resourceBean) {
		this.createThumbnail_new(resourceBean,PropertyUtil.getThumbnailMaxWith(),
				PropertyUtil.getThumbnailMaxHeight(),PropertyUtil.getThumbnailQuality());
	}
	
	public void createThumbnail_new(GroupResource resourceBean, int width, int height, int quality) {
		String suffix = split(resourceBean.getName())[1];
		// 将资源保存到服务器中
		String thumbnail = null;
		String filePath = FilePathUtil.getFileFullPath(resourceBean);
		// 生成缩略图
		if (isImageType(suffix)) {
			try {
				// 生成缩略图

				File srcFile = new File(new URI(filePath));
				/*
				 * if (Thumbnail.isOverLimit(srcFile.length())) { return; }
				 */
				thumbnail = "thum_" + Integer.toString(width) + "x" + Integer.toString(height) + "x"
						+ Integer.toString(quality) + "_" + srcFile.getName();
				String path = PropertyUtil.getGroupThumbnailRootPath() + File.separator
						+ Long.toString(resourceBean.getCreatorId());

				Thumbnail thum = new Thumbnail(srcFile, path, thumbnail);

				thum.resizeAndCut_new(width, height, 524288, quality);

			} catch (Exception e) {
				// ingore
			}
		}
	}

	/**
	 * 获得所有孩子
	 * @param resourceBeans
	 * @return
	 */
	public List<GroupResource> getAllChildBeans(List<GroupResource> resourceBeans) {
		int length = resourceBeans.size();
		if (length == 0)
			return null;
		String rids = resourceBeans.get(0).getId().toString();
		for (int i = 1; i < length; i++) {
			rids = rids + "," + resourceBeans.get(i).getId().toString();
		}
		String hql = "from " + GroupResource.class.getName() + " where parentId in (" + rids + ")";
		return genericDao.findAll(hql);
	}
	
	
	/**
	 * 重建路径
	 * @param id
	 * @return
	 */
	public String rebuiltPath(long id) {
		if (id == 0)
			return "/";
		GroupResource temp = resourceDao.getResourceById(id);
		String path;
		if (temp.getPath() == null || temp.getPath().equals("")) {
			path = rebuiltPath(temp.getParentId());
			temp.setPath(path);
			resourceDao.saveOrUpdateResource(temp);
			path = path + id + "/";
		} else
			path = temp.getPath() + id + "/";
		return path;
	}
	/**
	 * 更新子文件夹和子文件的path
	 * @param id
	 * @throws Exception 
	 */
	private void rebuiltChildPath(long id) throws Exception {
		// TODO Auto-generated method stub
		int start = 0;
		int size = 40;
		List<GroupResource> list = new ArrayList<>(this.getResourcesByParent(id, null, start, size, null));
		List<GroupResource> list_temp = new ArrayList<>(this.getResourcesByParent(id, null, size, size+size, null));
		if (list_temp != null && list_temp.size() > 0) {
			list.addAll(list_temp);
			start += size;
			list_temp = this.getResourcesByParent(id, null, start, start+size, null);
		}
		for (GroupResource resource : list) {
			String path = null;
			long parentId = resource.getParentId();
			GroupResource temp = getResourceById(parentId);
			if (temp.getPath() == null || temp.getPath().equals("")) {
				// rebuilt resource path
				path = this.rebuiltPath(temp.getId());
			} else {
				path = temp.getPath() + parentId + "/";
			}
			resource.setPath(path);
			resourceDao.saveOrUpdateResource(resource);
			
			if(resource.getResourceType() == GroupResource.RESOURCE_TYPE_DIRECTORY) {
				this.rebuiltChildPath(resource.getId());
			}
		}
	}
	
	/**
	 * 获得个人邮件大附件文件夹
	 * 没有就创建
	 * @param memberId 对应memberId
	 * @return
	 */
	public GroupResource getPersonEmailAttachFolder(long memberId) {
		if (PropertyUtil.getHasLargeAttach() == false)
			return null;
		Member member = memberDao.getMemberById(memberId);
		if (member == null) {
			throw new GroupsException(ResourceProperty.getAlternateKeyNotFoundString("Member","memberId", memberId));
		}
		String personGroupName = "" + memberId;
		Group personGroup = null;
		personGroup = groupDao.getGroupByName(personGroupName);
		GroupResource resource = null;
		if (personGroup != null) {
			long groupId = personGroup.getId();
			resource = this.getResource(groupId, PropertyUtil.getLargeAttachFolderName(), 0,
					GroupResource.RESOURCE_TYPE_DIRECTORY);
			if (resource == null || resource.getId() <= 0) {
				GroupResource bean = new GroupResource();
				bean.setGroup(personGroup);
				bean.setName(PropertyUtil.getLargeAttachFolderName());
				bean.setParentId(0L);
				bean.setCreatorId(memberId);
				bean.setDocumentTypeValue(GroupResource.DOCUMENT_TYPE_UNKNOWN);
				bean.setResourceType(GroupResource.RESOURCE_TYPE_DIRECTORY);

				bean.setCreateDate(new Timestamp(System.currentTimeMillis()));
				bean.setGroupName(personGroup.getName());
				bean.setMemberName(member.getName());

				bean.setResourceStatus(GroupResource.RESOURCE_STATUS_NORMAL);
				bean.setDefaultFolder(true);
				resourceDao.saveOrUpdateResource(bean);
				resource = bean;
			}
		}
		return resource;
	
	}
	
//	public IResult<GroupResource> getResourcesByClassify(long gid, int classify, int start, int limit) throws ServiceException;
//	
//	public boolean isReceivedResourceToMember(long resourceId, long memberId);
//	
//	public void updateViewUuid(long resourceId, String uuid);

//	/**
//	 * 获取所有没有上传完毕的文件信息
//	 * @param memberId
//	 * @return
//	 */
//	public List<GroupResource> getAllChunckResources(long memberId);

//	/**
//	 * 删除未上传完毕的资源分块
//	 * 临时分块资源与正常资源存放路径不同
//	 * @param resourceId
//	 */
//	public void deleteChunkResource(long resourceId) {
//		
//	}
	
	
	/**
	 * 获取资源空间总大小
	 *
	 * @param groupId
	 * @return
	 * @throws GroupsException
	 */
	public long getResourceSpaceSize(long groupId) throws GroupsException {
		if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), groupId,
				GroupPerm.VIEW_RESOURCE))
			throw PermissionsException.GroupException;
		Group bean = groupDao.getGroupById(groupId);
//		if (bean.getTotalFileSize() != null && bean.getTotalFileSize().longValue() > 0) {
//			return bean.getTotalFileSize();
//		}
		Map<String, String> map = bean.getExternInfo();
		if (map == null) {
			try {
				GroupType typeBean = groupTypeDao.getGroupTypeById(bean.getGroupType().getId());
				return typeBean.getTotalFileSize();
			} catch (Exception e) {
				return PropertyUtil.getGroupResourceSize();
			}
		}

		String filesize = map.get(Group.ExternInfo.TotalFileSize.toString());
		if (filesize == null) {
			try {
				GroupType typeBean = groupTypeDao.getGroupTypeById(bean.getGroupType().getId());
				return typeBean.getTotalFileSize();
			} catch (Exception e) {
				return PropertyUtil.getGroupResourceSize();
			}
		}

		return Long.parseLong(filesize);
	}
	   public long[] getResourceSpaceSize(long[] groupIds) throws GroupsException{
		      if(groupIds==null&&groupIds.length==0) {
		         return new long[] {};
		      }
		      long[] result = new long[groupIds.length];
		      String hql = "select g.id,ge.attrValue from "+Group.class.getName()+" as g , "+GroupExtern.class.getName()
		            +" as ge where g.id=ge.group.id and ge.attrKey='TotalFileSize' and (1=0";
		      ArrayList<Object> param = new ArrayList<Object>();
		      for(long id :groupIds) {
		         hql+=" or g.id=? ";
		         param.add(id);
		      } 
		      hql+=")";
		      Query q = genericDao.createQuery(hql, param.toArray());
		      List<Object[]> list  = q.getResultList();//
		      //构造查询映射
		      Map<Long ,Long> searchMap = new HashMap<Long,Long>();
		      for(Object[] ob:list) {
		         searchMap.put((Long)ob[0], Long.parseLong((String) ob[1]));
		      }
		      int index =0;
		      for(long gid:groupIds) {
		         Long groupTotalFileSize = searchMap.get(gid);
		         if(groupTotalFileSize==null||groupTotalFileSize==0) {
		            //根据类型来获取默认柜子容量大小
			            //根据类型来获取默认单文件大小
			            Group bean = groupDao.getGroupById(gid);
			            GroupType grouptype = bean.getGroupType();
						if (grouptype == null || grouptype.getId() <= 0) {
							groupTotalFileSize = PropertyUtil.getGroupResourceSize();
						} else {
							groupTotalFileSize = grouptype.getTotalFileSize();
						}
//						bean.setTotalFileSize(groupTotalFileSize);
//						groupDao.saveOrUpdateGroup(bean);
		         }
		         result[index] =groupTotalFileSize;
		         index++;
		      }
				
		      return result;
		   }
	public long[] getResourceSingleFileSize(long[] groupIds)throws GroupsException{
	      for(long groupId:groupIds) {
	          if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), groupId,
	                GroupPerm.VIEW_RESOURCE))
	             throw PermissionsException.GroupException;
	      }
	      if(groupIds==null&&groupIds.length==0) {
	         return new long[] {};
	      }
	      long[] result = new long[groupIds.length];
	      String hql = "select g.id,ge.attrValue from "+Group.class.getName()+" as g , "+GroupExtern.class.getName()
	            +" as ge where g.id=ge.group.id and ge.attrKey='SingleFileSize' and (1=0";
	      ArrayList<Object> param = new ArrayList<Object>();
	      for(long id :groupIds) {
	         hql+=" or g.id=? ";
	         param.add(id);
	      } 
	      hql+=")";
	      Query q = genericDao.createQuery(hql, param.toArray());
	      List<Object[]> list  = q.getResultList();//
	      //构造查询映射
	      Map<Long ,Long> searchMap = new HashMap<Long,Long>();
	      for(Object[] ob:list) {
	         searchMap.put((Long)ob[0], Long.parseLong((String) ob[1]));
	      }
	      int index =0;
	      for(long gid:groupIds) {
	         Long groupSingleFileSize = searchMap.get(gid);
	         if(groupSingleFileSize==null||groupSingleFileSize==0) {
	            //根据类型来获取默认单文件大小
	            Group bean = groupDao.getGroupById(gid);
	            GroupType grouptype = bean.getGroupType();
				if (grouptype == null || grouptype.getId() <= 0) {
					groupSingleFileSize = PropertyUtil.getGroupResourceSingleFileSize();
				} else {
					groupSingleFileSize = grouptype.getSingleFileSize();
				}
	         }
	         result[index] =groupSingleFileSize;
	         index++;
	      }
	      return result;
		}  

	/**
	 * 获取资源单文件限制大小
	 *
	 * @param groupId
	 * @return
	 * @throws GroupsException
	 */
	public long getResourceSingleFileSize(long groupId) throws GroupsException {
		if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), groupId,
				GroupPerm.VIEW_RESOURCE))
			throw PermissionsException.GroupException;
		Group bean = groupDao.getGroupById(groupId);
		Map<String, String> map = bean.getExternInfo();
		if (map == null) {
			// 有可能存在无效的圈子类型
			try {
				GroupType typeBean = groupTypeDao.getGroupTypeById(bean.getGroupType().getId());
				return typeBean.getSingleFileSize();
			} catch (Exception e) {
				return PropertyUtil.getGroupResourceSingleFileSize();
			}
		}

		String filesize = map.get(Group.ExternInfo.SingleFileSize.toString());
		if (filesize == null) {
			// 有可能存在无效的圈子类型
			try {
				GroupType typeBean = groupTypeDao.getGroupTypeById(bean.getGroupType().getId());
				return typeBean.getSingleFileSize();
			} catch (Exception e) {
				return PropertyUtil.getGroupResourceSingleFileSize();
			}
		}

		return Long.parseLong(filesize);
	}

	/**
	 * 获取已使用的资源空间大小
	 *
	 * @param groupId
	 * @return
	 * @throws GroupsException
	 */
	public long getResourcesSize(long groupId) throws GroupsException{
		if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), groupId,
				GroupPerm.VIEW_RESOURCE))
			throw PermissionsException.GroupException;
		return resourceDao.getResourcesSizeByGroup(groupId);
	}
	
	/**
	 * 更新资源文件夹的大小
	 * 资源文件夹大小仅包含其子级文件的总大小
	 * @param dirId
	 * @throws GroupsException
	 */
	public  void statResourceDirSize(long dirId) throws GroupsException {
		GroupResource bean = resourceDao.getResourceById(dirId);

		if (bean.getResourceType() != GroupResource.RESOURCE_TYPE_DIRECTORY)
			throw new GroupsException(ResourceProperty
					.getNotResourceDirectoryString());	
		long dirSize = resourceDao.getResourceDirSize_new(bean.getPath(),bean.getId().toString(),bean.getGroup().getId());		
		bean.setSize(dirSize);
		resourceDao.saveOrUpdateResource(bean);

	}


	/**
	 * 检查资源文件夹深度
	 *
	 * @throws GroupsException
	 */
	private void checkDirectoryDepth(long parentId) throws GroupsException {
		int depth = 1;
		while (true) {
			if (parentId == 0)
				break;

			GroupResource bean = resourceDao.getResourceById(parentId);
			parentId = bean.getParentId();
			++depth;
		}

		if (depth > PropertyUtil.getGroupResourceDirDepth())
			throw new GroupsException(ResourceProperty.getMaxResourceDepthString());
	}
	
	/**
	 * 将名字分成两部分，第一部分为文件名，第二部分为后缀名，以.划分。
	 * <p>
	 * 若存在多个.则取最后一部分作为后缀名，前部分作为文件名
	 *
	 * @param name
	 * @return
	 */
	private String[] split(String name) {
		if (name == null)
			return null;

		String[] ts = name.split("\\.");
		if (ts.length == 1)
			return new String[] { ts[0], "" };

		// 拼接第一段
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < ts.length - 1; ++i) {
			if (i != 0)
				b.append(".");
			b.append(ts[i]);
		}

		return new String[] { b.toString(), ts[ts.length - 1] };
	}
	/**
	 * 是否是合法的图片类型
	 * @param suffix
	 * @return
	 * @throws GroupsException
	 */
	private static boolean isImageType(String suffix) throws GroupsException {
		if (suffix == null || suffix.length() == 0)
			return false;

		String[] validSuffix = PropertyUtil.getVolidImageType();
		for (String s : validSuffix) {
			if (s.equalsIgnoreCase(suffix))
				return true;
		}
		return false;
	}
	
	/**
	 * 文件是否合法后缀
	 * @param suffix
	 * @throws GroupsException
	 */
	private static void checkSuffix(String suffix) throws GroupsException {
		if (suffix == null || suffix.length() == 0)
			return;

		String[] validSuffix = PropertyUtil.getVolidResourceType();
		for (String s : validSuffix) {
			if (s.equalsIgnoreCase(suffix) || s.equalsIgnoreCase("*"))
				return;
		}

		throw new GroupsException(ResourceProperty.getInvolidNameString());
	}
	
	/**
	 * 检查柜子是否还有空间
	 * @param resourceBean
	 * @param groupId
	 * @throws GroupsException
	 */
	private void checkGroupResourceSpace(GroupResource resourceBean, long groupId) throws GroupsException {
		long totalSize = getResourceSpaceSize(groupId);

		long currentSize = getResourcesSize(groupId);

		if (currentSize + resourceBean.getSize() > totalSize)
			throw new GroupsException(ResourceProperty.getNotEnoughResourceRoomString());
	}
	
	/**
	 * 检查文件大小是否超出限制
	 * @param resourceBean
	 * @param groupId
	 * @throws GroupsException
	 */
	private void checkSingleFileSize(GroupResource resourceBean, long groupId) throws GroupsException {
		long singleFileSize = getResourceSingleFileSize(groupId);
		if (resourceBean.getSize() > singleFileSize)
			throw new GroupsException(ResourceProperty.getFileSizeTooBigString());
	}
	
	/**
	 * 对文件进行重命名，以当前时间作为其名字，并保留其原本的后缀名
	 *
	 * @param resourceBean
	 */
	private static void renameResource(GroupResource resourceBean) {
		// TODO 改成不要带后缀
		String name = resourceBean.getName();

		String buffer_name = null;

		boolean exist = true;
		while (exist == true) {
			StringBuffer buffer = new StringBuffer();
			Thread current = Thread.currentThread();
			Random random = new Random(100);
			buffer.append(resourceBean.getCreatorId() + "_" + Long.toString(current.getId())
					+ Integer.toString(random.nextInt(10000)) + System.currentTimeMillis());
			// 加入后缀名
			int index = name.lastIndexOf(".");
			if (index != -1) {
				buffer.append(name.substring(name.lastIndexOf("."), name.length()));
			}

			File tmp = new File(FilePathUtil.getFileFullPath(resourceBean.getCreatorId(), buffer.toString()));
			if (!tmp.exists()) {
				exist = false;
				buffer_name = buffer.toString();
			}
		}
		// 设置文件存储路径
		resourceBean.setFilePath(buffer_name);
	}
	
	private void deleteReresourceDir_batch(long id) {
		GroupResource resourceBean = resourceDao.getResourceById(id);
		CheckUtil.checkNormalGroup(resourceBean.getGroup());
		List<Long> idList = new ArrayList<Long>();
		idList.add(id);
		StringBuffer hql = new StringBuffer();
		hql.append("from GroupResource where id = ")
				.append(resourceBean.getId()).append(" or ").append(" path like '")
				.append(resourceBean.getPath() + resourceBean.getId()).append("/%'");

		final List<GroupResource> beans = genericDao.findAll(hql.toString());
		beans.getClass();

		ThreadPoolService t = new ThreadPoolService(2, 500);

		Runnable task = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				for (GroupResource bean : beans) {
					// 若是文件则删除文件
					if (bean.getResourceType() == GroupResource.RESOURCE_TYPE_FILE) {
						deleteResource_batch(bean.getId());
					}
				}
			}
		};
		t.execute(task);
		hql = new StringBuffer();
		hql.append("delete from GroupResource where id = ").append(resourceBean.getId());
		javax.persistence.Query query = genericDao.createQuery(hql.toString());
		query.executeUpdate();
		//新增容量by mi
		Group groupBean = resourceBean.getGroup();
		groupBean.setAvailableCapacity(groupBean.getAvailableCapacity()+resourceBean.getSize());
		groupBean.setUsedCapacity(groupBean.getTotalFileSize()-groupBean.getAvailableCapacity());
		groupDao.saveOrUpdateGroup(groupBean);
	}
	
	/**
	 * 获得待下载资源
	 * @param ids
	 * @return
	 * @throws GroupsException
	 */
	public File getDownloadResource(Long[] ids) throws GroupsException{
		if (ids == null || ids.length == 0) {
			throw new GroupsException(ResourceProperty.getCannotDownloadFileString());
		}
		File zipFile = null;
		GroupResource[] beans = new GroupResource[ids.length];
		String zipName = "";
		for (int i = 0; i < beans.length; ++i) {
			beans[i] = resourceDao.getResourceById(ids[i]);
			zipName = zipName + "_" + ids[i];
			// 若圈子已关闭，不可操作
			CheckUtil.checkNormalGroup(beans[i].getGroup());
		}
		zipName = zipName + ".zip";
		if (zipName.length() > 255) {
			throw new GroupsException("File name too long");
		}
		// 多个文件，打包下载
		if (beans.length > 1 || (beans.length == 1 && beans[0].getResourceType() == GroupResource.RESOURCE_TYPE_DIRECTORY)) {
			if (FileUtil.isTempDownFileExists(zipName)) {
				zipFile = FileUtil.getTempDownFile(zipName);
			} else {
				try {
					zipFile = FileUtil.createTempDownFile(zipName);
				} catch (IOException e) {
					throw new GroupsException("Create temp file fail");
				}
				zipDownload(beans, zipFile);
				zipFile = FileUtil.getTempDownFile(zipName);
			}
		}
		// 单个文件时
		if (beans.length == 1) {
			// 若为文件，则直接下载
			if (beans[0].getResourceType() == GroupResource.RESOURCE_TYPE_FILE) {
				try {
					zipFile = new File(new URI(FilePathUtil.getFileFullPath(beans[0])));
					// 记录日志
					Group groupBean = beans[0].getGroup();
					logService.addDownloadLog(beans[0].getGroup().getId(), groupBean.getDisplayName(),
							beans[0].getId(), beans[0].getName());
				} catch (URISyntaxException e) {
					throw new GroupsException(e);
				}
			}
		}
		return zipFile;
	
	}
	
	/**
	 * 下载资源
	 * @param ids
	 * @param os
	 * @throws GroupsException
	 */
	public void downloadResource(Long[] ids, OutputStream os) throws GroupsException {
		for (long id : ids) {
			if (permission.isReceivedResourceToMember(id, UserUtils.getCurrentMemberId())) {
				continue;
			}
			GroupResource resourceBean = resourceDao.getResourceById(id);

			if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), resourceBean
					.getGroup().getId(), GroupPerm.DOWNLOAD_RESOURCE))
				throw PermissionsException.GroupException;
		}
		
		if (os == null || ids == null || ids.length == 0) {
			throw new GroupsException(ResourceProperty.getCannotDownloadFileString());
		}

		GroupResource[] beans = new GroupResource[ids.length];
		for (int i = 0; i < beans.length; ++i) {
			beans[i] = resourceDao.getResourceById(ids[i]);

			// 若圈子已关闭，不可操作
			CheckUtil.checkNormalGroup(beans[i].getGroup());
		}

		// 多个文件，打包下载
		if (beans.length > 1) {
			this.zipDownload(beans, os);
		}

		// 单个文件时
		if (beans.length == 1) {
			// 若为目录，打包下载
			if (beans[0].getResourceType() == GroupResource.RESOURCE_TYPE_DIRECTORY) {
				this.zipDownload(beans, os);
			}
			// 若为文件，则直接下载
			if (beans[0].getResourceType() == GroupResource.RESOURCE_TYPE_FILE) {
				FileUtil.downloadFile(FilePathUtil.getFileFullPath(beans[0]), os);
				// 记录日志
				Group groupBean = beans[0].getGroup();
				logService.addDownloadLog(beans[0].getGroup().getId(), groupBean.getDisplayName(),
						beans[0].getId(), beans[0].getName());
			}
		}
	}
	
	/**
	 * 多个资源打包下载
	 * @param resourceBeans
	 * @param os
	 * @throws GroupsException
	 */
	private void zipDownload(GroupResource[] resourceBeans, OutputStream os) throws GroupsException {
		ZipFile zip = null;
		try {
			zip = new ZipFile(os);
			zipfile(zip, resourceBeans, "");
		} catch (Exception e) {
			throw new GroupsException("没找到下载文件");
		} finally {
			if (zip != null)
				zip.close();
		}
	}

	private void zipDownload(GroupResource[] resourceBeans, File zipFile) throws GroupsException {
		ZipFile zip = null;
		try {
			zip = new ZipFile(zipFile);
			zipfile(zip, resourceBeans, "");
		} catch (Exception e) {
			throw new GroupsException(e);
		} finally {
			if (zip != null)
				zip.close();
		}
	}
	
	private void zipfile(ZipFile zip, GroupResource[] resourceBeans, String base) throws Exception {
		for (GroupResource resourceBean : resourceBeans) {
			if (resourceBean.getResourceType() == GroupResource.RESOURCE_TYPE_DIRECTORY) {
				// 文件夹关键字
				Map<String, String> keywords = PropertyUtil.getFolderKeyWordMap();
				String zipname = resourceBean.getName();
				if (keywords.containsKey(zipname)) {
					zipname = keywords.get(zipname);
				}
				String nbase = base + zipname + "/";
				zip.addDirectory(nbase);
				List<GroupResource> resourcesByParents = getResourcesByParentWithoutPermCheck(resourceBean.getId());
				GroupResource[] beans = resourcesByParents.toArray(new GroupResource[resourcesByParents.size()]);
				zipfile(zip, beans, nbase);
			} else {
				zip.zipFile(new File(new URI(FilePathUtil.getFileFullPath(resourceBean))), base + resourceBean.getName());
			}
		}
	}
	
	public void downloadResourceReplaceName(Long[] ids, OutputStream os, String[] newFileName) throws GroupsException {
		if (os == null || ids == null || ids.length == 0) {
			throw new GroupsException(ResourceProperty.getCannotDownloadFileString());
		}

		GroupResource[] beans = new GroupResource[ids.length];
		for (int i = 0; i < beans.length; ++i) {
			beans[i] = resourceDao.getResourceById(ids[i]);
			beans[i] = copyTempResource(beans[i]);
			beans[i].setName(newFileName[i] + (beans[i].getFileExt() != null ? beans[i].getFileExt() : ""));
		}

		// 多个文件，打包下载

		zipDownload(beans, os);

	}

	private GroupResource copyTempResource(GroupResource res) {
		if (res == null)
			return null;
		GroupResource copy = new GroupResource();
		BeanUtils.copyProperties(res, copy);
		copy.setId(res.getId());
		return copy;
	}
	public PageNavigater<GroupResource> searchResource(SearchTerm searchTerm, SortTerm sortTerm, PageTerm pageTerm) {
		// TODO Auto-generated method stub
		return new PageNavigater(searchTerm, sortTerm, pageTerm, resourceDao);
	}
	
	/**
	 * 查看预览图
	 * @param id
	 * @param os
	 * @throws GroupsException
	 */
	public void viewThumbnail(long id, OutputStream os) throws GroupsException {
		if (!permission.isReceivedResourceToMember(id, UserUtils.getCurrentMemberId())) {
			GroupResource resourceBean = resourceDao.getResourceById(id);
			if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), resourceBean
					.getGroup().getId(), GroupPerm.DOWNLOAD_RESOURCE))
				throw PermissionsException.GroupException;
		}
		GroupResource resourceBean = resourceDao.getResourceById(id);
		String thumbnail = resourceBean.getThumbnail();
		if (thumbnail != null) {
			FileUtil.downloadFile(FilePathUtil.getFileFullPath(resourceBean.getCreatorId(), thumbnail), os);
		} else {
			String suffix = split(resourceBean.getName())[1];
			if (isImageType(suffix)) {
				// =========
				// 生成缩略图
				this.createThumbnail_new(resourceBean);
				// =========
				// thumbnail = resourceBean.getFilePath();
				thumbnail = resourceBean.getThumbnail();
				// thumbnail = "thum_"+resourceBean.getFilePath();
				if (thumbnail != null) {
					FileUtil.downloadFile(FilePathUtil.getFileFullPath(resourceBean.getCreatorId(), thumbnail), os);
				} else {
					FileUtil.downloadFile(FilePathUtil.getFileFullPath(resourceBean.getCreatorId(), resourceBean.getFilePath()), os);
				}
			}
		}
	}


	public void viewFile(long id,OutputStream os)throws GroupsException{
		if (!permission.isReceivedResourceToMember(id, UserUtils.getCurrentMemberId())) {
			GroupResource resourceBean = resourceDao.getResourceById(id);
			if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), resourceBean
					.getGroup().getId(), GroupPerm.DOWNLOAD_RESOURCE))
				throw PermissionsException.GroupException;
		}
		GroupResource resourceBean = resourceDao.getResourceById(id);
	}
	/**
	 * 获取所有资源总大小
	 * @param resourceIds
	 * @return
	 */
	public long getResourcesSizeSum(Long[] resourceIds) {
		if (resourceIds == null || resourceIds.length == 0) {
			return 0;
		}
		long sum = 0;
		String hql = "select sum(r.size) from " + GroupResource.class.getName() + " as r where ";
		for (int i = 0; i < resourceIds.length; i++) {
			if (resourceIds[i] != 0) {
				hql += " r.id=" + resourceIds[i] + " or ";
			}
		}
		hql += "1=0";
		javax.persistence.Query query = genericDao.createQuery(hql, null);
		try {
			sum = (Long) query.getSingleResult();
		} catch (NoResultException e) {
			sum = 0;
		}
		return sum;
	}
	
	/**
	 * @param shareWrap shareWrap (还处于瞬时态，需要在该方法内持久化)
	 * @param share shareBean (还处于瞬时态，需要在该方法内持久化)
	 */
	public void createShareAndSplitReceive(long groupId, ShareWrap shareWrap, GroupResourceShare share, LinkedHashMap<Long,String> members) {
		if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), groupId, GroupPerm.SHARE_RESOURCE))
			throw PermissionsException.GroupException;
		resourceShareDao.saveOrUpdateShareWrap(shareWrap);
		resourceShareDao.saveOrUpdateResourceShare(share);
		for (Map.Entry<Long, String> en : members.entrySet()) {
			Long mid = en.getKey();
			String mname = en.getValue();
			GroupResourceReceive receive = new GroupResourceReceive();
			Member m = memberDao.getMemberById(mid);
			receive.setRecipient(m);
			receive.setRemark(share.getRemark());
			receive.setResource(share.getResource());
			receive.setShare(share);
			receive.setMemberName(mname);
			receive.setStatus(GroupResourceReceive.NEWSHARE);
			resourceReceiveDao.saveOrUpdateResourceReceive(receive);
		}
	}

	public long getMyNewReceiveCount(long memberId) {
		return resourceReceiveDao.getMyNewReceiveCount(memberId);
	}
	
	public GroupResourceReceive[] getMyReceiveResource(Long providerId,
			Integer start, Integer limit) throws Exception {
		// TODO Auto-generated method stub
		final long recipientId = UserUtils.getCurrentMemberId();
		SearchTerm searchTerm = new AndSearchTerm();
		if (providerId > 0) {
			searchTerm.add(new SearchItem<Long>(ResourceReceiveSearchItemKey.ProviderId, SearchItem.Comparison.EQ,
					providerId));
		} else {
			SearchTerm orSearchTerm = new OrSearchTerm();
			Member[] beans = grouperService.getTeamsOfMember(recipientId);
			Member everyOneTeam = grouperService.getEveryOneTeam();
			orSearchTerm.add(new SearchItem<Long>(ResourceReceiveSearchItemKey.RecipientId, SearchItem.Comparison.EQ,
					recipientId));
			orSearchTerm.add(new SearchItem<Long>(ResourceReceiveSearchItemKey.RecipientId, SearchItem.Comparison.EQ,
					everyOneTeam.getId()));
			for (Member b : beans) {
				orSearchTerm.add(new SearchItem<Long>(ResourceReceiveSearchItemKey.RecipientId,
						SearchItem.Comparison.EQ, b.getId()));
			}
			searchTerm.add(orSearchTerm);
		}

		SortTerm sortTerm = new SortTerm(new DescSortItem(ResourceReceiveSortItemKey.Id));

		PageTerm pageTerm = new PageTerm();
		pageTerm.setBeginIndex(CheckUtil.reviseStart(start));
		pageTerm.setPageSize(CheckUtil.reviseLimit(limit, PropertyUtil.getGroupRecordsDefaultPageSize()));

		final PageNavigater<GroupResourceReceive> pageNavigater = new PageNavigater<GroupResourceReceive>(
				searchTerm, sortTerm, pageTerm, resourceReceiveDao);

		return pageNavigater.getContent();
	}

	/**
	 * 根据id获取我的接收和我所在组的接收
	 * @param recipientId
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<Object[]> getMyReceive(long recipientId, int start, int limit) {
		Member[] beans = grouperService.getTeamsOfMember(recipientId);// 当前用户所在的组
		Member everyOneTeam = grouperService.getEveryOneTeam();// 所有用户组
		List <Long> recipientIds = new ArrayList<Long>();
		recipientIds.add(recipientId);
		if (everyOneTeam == null && everyOneTeam.getId() >= 0) {
			recipientIds.add(everyOneTeam.getId());
		}
		if (beans != null || beans.length > 0) {
			for (Member member : beans) {
				recipientIds.add(member.getId());
			}
		}
		return resourceReceiveDao.getNewReceiveByRecipientIds(recipientIds);
	}
	
	/**
	 * 根据回复人和shareWrap获取回复
	 * @param shareWrapId
	 * @param responderId
	 * @return
	 */
	public List<ShareResponse> getShareResponseByShareWrapAndResponder(long shareWrapId, long responderId) {
		return resourceShareDao.getShareResponseByShareWrapAndResponder(shareWrapId, responderId);
	}

	/**
	 * 根据接收id将接收状态设为已读
	 * @param ids GroupResourceReceive的id数组
	 */
	public void markReceived(Long[] ids) {
		// TODO Auto-generated method stub
		for (long id : ids) {
			GroupResourceReceive receive = resourceReceiveDao.getResourceReceiveById(id);
			if (receive != null && receive.getId() >= 0) {
				receive.setStatus(GroupResourceReceive.RECEIVED);
				resourceReceiveDao.saveOrUpdateResourceReceive(receive);
			}
		}
	}
	
	/**
	 * 根据memberId获取所分享的所有分享记录
	 * @param memberId 分享者memberId
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<GroupResourceShare> getSharedResourceByProvider(long memberId, int start, int limit) {
		return resourceShareDao.getAllResourceShareByProvider(memberId, start, limit);     
	}

	/**
	 * 根据id删除所有上传中间文件 
	 * @param resourceId
	 */
	public void deleteChunkResource(long resourceId) {
		long groupId = 0;
		Group agroup = resourceDao.getResourceById(resourceId).getGroup();
		if (agroup != null) {
			groupId = agroup.getId();
		}
		if (!hasPesonalGroupAppPerm(groupId)) {
			if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), groupId,
					GroupPerm.DELETE_RESOURCE))
				throw PermissionsException.GroupException;
		}

		GroupResource resourceBean = resourceDao.getResourceById(resourceId);
		if (resourceBean == null) {
			return;
		}
		// 若圈子已关闭，不可操作
		CheckUtil.checkNormalGroup(resourceBean.getGroup());
		// 删除数据库表项，删除表项需在删除文件之前，因为出现异常的话，数据库可以回滚
		resourceDao.deleteResource(resourceBean);
		// 统计父资源目录
		long parentId = resourceBean.getParentId();
		if (parentId > 0) {
			this.statResourceDirSize(parentId);
		}

		if (resourceBean.getResourceType() == GroupResource.RESOURCE_TYPE_FILE) {
			// 删除服务器文件
			String dirPath = FilePathUtil.getFileTempPath(resourceBean);
			//删除文件夹下所有文件
			FileUtil.deleteResource(dirPath);
			try {
				File dir = new File(new URI(dirPath));
				//删除文件夹本身
				dir.delete();
			} catch (URISyntaxException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}	
	}
	
	/**
	 * 获取用户所有未上传完成的资源
	 * @param memberId
	 * @return
	 */
	public List<GroupResource> getAllChunckResources(long memberId) {
		return resourceDao.getAllUnfinishedResources(memberId);
	}
	
	private boolean hasPesonalGroupAppPerm(long groupId) {
		// 判断是否个人文件柜
		Group groupBean = groupDao.getGroupById(groupId);
		Category personCategory = categoryDao.getCategoryByName(PropertyUtil.getDefaultPersonGroupCategory());
		if (groupBean.getCategory().getId() == personCategory.getId()) {
			try {
				// 获取该人所属应用
				List<Member> memberBean = memberDao.getMembersByName(groupBean.getDisplayName());
				List<Application> applicationBeans = appService.getApplicationByMember(memberBean.get(0).getId(), 0, 0, false);
				// 判断本sesion是否拥有该应用的管理权限
				for (Application appBean : applicationBeans) {
					AppMember bean =  appService.getAppMemberByMemberAndApp(
									UserUtils.getCurrentMemberId(), appBean.getId());
					if (bean != null && bean.getIsManager() == 1) {
						return true;
					}
				}
			} catch (Exception e) {
				return false;
				//e.printStackTrace();
			}
		}
		return false;
	}

	public List<Object[]> getMyShared(int start, int limit) {
		// TODO Auto-generated method stub
		long memberId = UserUtils.getCurrentMemberId();
		String hql = "from " + GroupResourceShare.class.getName()
				+ " as s left join s.shareWrap as w where s.provider.id=" + memberId + " and s.shareType="
				+ GroupResourceShare.INNER_SHARE// 新版分享只显示站内分享，不显示外链分享
				+ " order by s.createDate DESC";
		javax.persistence.Query qu = genericDao.createQuery(hql, null);
		List<Object[]> result = qu.getResultList();
		return result;
	}

	public void replyShare(Long shareId, Long memberId, String replyContent) {
		// TODO Auto-generated method stub
		String hql = "from " + ShareResponse.class.getName() + " as sr where sr.shareWrap.id=" + shareId
				+ " and sr.responder.id=" + memberId;
		Query q = genericDao.createQuery(hql, null);
		ShareResponse sr;
		try {
			sr = (ShareResponse) q.getSingleResult();
			sr.setContent(replyContent);// 替换更新回复
			sr.setResponseDate(new Timestamp(System.currentTimeMillis()));
			genericDao.update(sr);
		} catch (NoResultException e) {
			sr = new ShareResponse();
			ShareWrap temp = new ShareWrap();
			temp.setId(shareId);
			sr.setShareWrap(temp);
			Member responder = new Member(memberId);
			sr.setResponder(responder);
			sr.setContent(replyContent);
			sr.setResponseDate(new Timestamp(System.currentTimeMillis()));
			genericDao.save(sr);
		}
	}

	public List<Object[]> getMySharedDetials(Long shareId, Boolean isOld,
			Boolean onlyResponse) {
		// TODO Auto-generated method stub
		final Long providerId = UserUtils.getCurrentMemberId();
		if (!isOld) {
			String hql = "select sw,sr from " + ShareResponse.class.getName() + " sr "
					+ "right join sr.shareWrap as sw" + " where sw.providerId=" + providerId + " and sw.id=" + shareId
					+ " order by sr.responseDate DESC";

			javax.persistence.Query query = genericDao.createQuery(hql, null);
			List<Object[]> result = query.getResultList();
			return result;
		} else {
			// 旧版分享数据，不支持回复
			if (onlyResponse) {
				List<Object[]> result = new ArrayList<Object[]>();
				Object[] o = new Object[0];
				result.add(o);
				return result;
			}
			String hql = "from " + GroupResourceShare.class.getName() + " as s where s.provider.id=" + providerId
					+ " and s.id=" + shareId + " order by sr.responseDate DESC";
			javax.persistence.Query query = genericDao.createQuery(hql, null);
			List<Object[]> result = query.getResultList();
			return result;
		}
	}

	public List<GroupResource> getResourcesByIds(Long[] ids) {
		// TODO Auto-generated method stub
		String hql = "from " + GroupResource.class.getName() + " as r where 1=0  ";
		if (ids != null && ids.length > 0) {
			for (Long id : ids) {
				hql += "  or r.id=" + id;
			}
		}
		Query query = genericDao.createQuery(hql, null);

		return query.getResultList();
	}

	/**
	 * 
	 * @param id groupresourceshare 的id
	 */
	public void revokeShareResourceToMember(Long[] id) {
		if (id == null || id.length <= 0)
			return;
		// response

		// receive
		String deleteReceive = "delete from " + GroupResourceReceive.class.getName() + " as receive " + " where 1=0 ";
		for (Long i : id) {
			deleteReceive += (" or receive.share.id=" + i);
		}

		Query q_deleteReceive = genericDao.createQuery(deleteReceive, null);
		// resourceCode

		// share
		String deleteShare = "delete from " + GroupResourceShare.class.getName() + " as share " + " where 1=0 ";
		for (Long i : id) {
			deleteShare += (" or share.id=" + i);
		}
		Query q_deleteShare = genericDao.createQuery(deleteShare, null);

		q_deleteReceive.executeUpdate();
		q_deleteShare.executeUpdate();
	}

	/**
	 * 
	 * @param id shareWrap的id
	 */
	public void revokeShareResource(Long[] id) {
		// TODO Auto-generated method stub
		if (id == null || id.length <= 0)
			return;
		long memberId = UserUtils.getCurrentMemberId();
		for (Long i : id) {
			ShareWrap sw = genericDao.get(ShareWrap.class, i);
			if (sw.getProviderId() != memberId) {
				throw new PermissionsException("无法删除非本人分享");
			}
		}
		// response
		String deleteResp = "delete from " + ShareResponse.class.getName() + " as sr where 1=0 ";
		for (Long i : id) {
			deleteResp += (" or sr.shareWrap.id=" + i);
		}
		Query q_deleteResp = genericDao.createQuery(deleteResp, null);
		// receive
		String getMatchReceive = "from " + GroupResourceReceive.class.getName() + " as receive " + " where 1=0 ";
		for (Long i : id) {
			getMatchReceive += (" or receive.share.shareWrap.id=" + i);
		}

		Query getMatch = genericDao.createQuery(getMatchReceive, null);
		List<GroupResourceReceive> receives = getMatch.getResultList();
		String deleteReceive = "delete from " + GroupResourceReceive.class.getName() + " as receive "
				+ " where 1=0 ";

		for (GroupResourceReceive r : receives) {
			deleteReceive += (" or receive.id=" + r.getId());
		}
		Query q_deleteReceive = genericDao.createQuery(deleteReceive, null);
		// resourceCode
		// share
		String deleteShare = "delete from " + GroupResourceShare.class.getName() + " as share " + " where 1=0 ";
		for (Long i : id) {
			deleteShare += (" or share.shareWrap.id=" + i);
		}
		Query q_deleteShare = genericDao.createQuery(deleteShare, null);
		// shareWrap
		String deleteShareWrap = "delete from " + ShareWrap.class.getName() + " as shareWrap " + " where 1=0 ";
		for (Long i : id) {
			deleteShareWrap += (" or shareWrap.id=" + i);
		}
		Query q_deleteShareWrap = genericDao.createQuery(deleteShareWrap, null);

		q_deleteResp.executeUpdate();
		q_deleteReceive.executeUpdate();
		q_deleteShare.executeUpdate();
		q_deleteShareWrap.executeUpdate();
	}

	public void deleteResourceReceiveBean(Long[] ids) {
		// TODO Auto-generated method stub
		String deleteReceive = "delete from " + GroupResourceReceive.class.getName() + " as receive "
				+ " where 1=0 ";

		for (long id : ids) {
			deleteReceive += (" or receive.id=" + id);
		}
		Query q_deleteReceive = genericDao.createQuery(deleteReceive, null);
		q_deleteReceive.executeUpdate();
	}

	public void modifyResourcePublishDate(Long id, Timestamp timestamp) {
		// TODO Auto-generated method stub
		GroupResource bean = resourceDao.getResourceById(id);
		Group agroup = bean.getGroup();
		// 若圈子已关闭，不可操作
		CheckUtil.checkNormalGroup(agroup);

		bean.setPublishDate(timestamp);
		resourceDao.saveOrUpdateResource(bean);

		// 记录日志		
		logService.addOperateLog(Log.ACTION_TYPE_MODIFY, "修改发文时间", id, bean.getName());
	}

	public void uploadPreviewThumbnail(GroupResource resourceBean, File file,
			Integer width, Integer height, Integer quality, boolean resize) {
		// TODO Auto-generated method stub

		try {
			String ext = split(resourceBean.getName())[1];
			if (!isImageType(ext) || file == null)
				return;
			String resourceBeanPath = FilePathUtil.getFileFullPath(resourceBean);
			File resourceFile = new File(new URI(resourceBeanPath));
			String savePath = resourceFile.getParent();
			String thumbnailName = "thum_" + resourceFile.getName();
			if (resize) {
				// 需要resize

				Thumbnail thum = new Thumbnail(file, savePath, thumbnailName);
				thumbnailName = thum.resizeByMaxStandard(PropertyUtil.getThumbnailMaxWith(),
						PropertyUtil.getThumbnailMaxHeight(), PropertyUtil.getThumbnailMaxLength(),
						PropertyUtil.getThumbnailProportion(), PropertyUtil.getThumbnailQuality());
			} else {
				// 直接存，不resize
				if (resourceFile.exists()) {

					String thumPath = savePath + File.separator + thumbnailName;
					File destFile = new File(thumPath);
					FileUtil.copyFileToServer(file, destFile.toURI().toString(), true);
				}
				resourceBean.setThumbnail(thumbnailName);
				resourceDao.saveOrUpdateResource(resourceBean);
			}
		} catch (Exception e) {

		}
	}

	public void uploadThumbnail(GroupResource resourceBean, File thumbnailFile,
			Integer width, Integer height, Integer quality) {
		// TODO Auto-generated method stub

		String suffix = split(resourceBean.getName())[1];
		// 将资源保存到服务器中
		String thumbnail = null;
		String filePath = FilePathUtil.getFileFullPath(resourceBean);
		// 生成缩略图
		if (isImageType(suffix)) {
			try {
				// 生成缩略图

				File srcFile = new File(new URI(filePath));
				// File srcFile = thumbnailFile;
				/*
				 * if (Thumbnail.isOverLimit(srcFile.length())) { return; }
				 */
				thumbnail = "thum_" + Integer.toString(width) + "x" + Integer.toString(height) + "x"
						+ Integer.toString(quality) + "_" + srcFile.getName();
				String path = PropertyUtil.getGroupThumbnailRootPath() + File.separator
						+ Long.toString(resourceBean.getCreatorId());

				if (thumbnailFile == null) {
					Thumbnail thum = new Thumbnail(srcFile, path, thumbnail);
					thum.resizeAndCut_new(width, height, 524288, quality);
				} else {
					// Thumbnail thum = new Thumbnail(thumbnailFile, path,
					// thumbnail);
					// thum.resizeAndCut_new(width, height, 524288, quality);

					File destFiles = new File(path + File.separator + thumbnail);

					FileUtil.copyFileToServer(thumbnailFile, destFiles.toURI().toString(), true);

				}

			} catch (Exception e) {
				// ingore
				e.printStackTrace();
			}
		}
	
	}

	public GroupResourceShare getResourceShareBean(long shareId) {
		// TODO Auto-generated method stub
		return resourceShareDao.getResourceShareById(shareId);
	}

	public File getDownloadResource4Web(long[] ids) {
		if (ids == null || ids.length == 0) {
			throw new GroupsException(ResourceProperty.getCannotDownloadFileString());
		}
		File zipFile = null;
		GroupResource[] beans = new GroupResource[ids.length];
		String zipName = "";
		for (int i = 0; i < beans.length; ++i) {
			beans[i] = resourceDao.getResourceById(ids[i]);
			zipName = zipName + "_" + ids[i];
			// 若圈子已关闭，不可操作
			CheckUtil.checkNormalGroup(beans[i].getGroup());
		}
		zipName = zipName + ".zip";
		if (zipName.length() > 255) {
			throw new GroupsException("File name too long");
		}
		// 多个文件，打包下载
		if (beans.length > 1 || (beans.length == 1 && beans[0].getResourceType() == GroupResource.RESOURCE_TYPE_DIRECTORY)) {
			if (FileUtil.isTempDownFileExists(zipName)) {
				zipFile = FileUtil.getTempDownFile(zipName);
			} else {
				try {
					zipFile = FileUtil.createTempDownFile(zipName);
				} catch (IOException e) {
					throw new GroupsException("Create temp file fail");
				}
				zipDownload(beans, zipFile);
				zipFile = FileUtil.getTempDownFile(zipName);
			}
		}
		// 单个文件时
		if (beans.length == 1) {
			// 若为文件，则直接下载
			if (beans[0].getResourceType() == GroupResource.RESOURCE_TYPE_FILE) {
				try {
					zipFile = new File(new URI(FilePathUtil.getFileFullPath(beans[0])));
					
					// 记录日志
					Group groupBean = beans[0].getGroup();
					logService.addDownloadLog4System(groupBean.getId(), groupBean.getDisplayName(),
							beans[0].getId(), beans[0].getName());
				} catch (URISyntaxException e) {
					throw new GroupsException(e);
				}
			}
		}
		return zipFile;
	}

	

}
