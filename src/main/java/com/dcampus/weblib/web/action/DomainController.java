package com.dcampus.weblib.web.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import net.sf.json.JsonConfig;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dcampus.common.config.PropertyUtil;
import com.dcampus.common.web.BaseController;
import com.dcampus.sys.entity.User;
import com.dcampus.sys.service.UserService;
import com.dcampus.sys.util.UserUtils;
import com.dcampus.weblib.entity.CapacityDistribution;
import com.dcampus.weblib.entity.CapacityDistribution.FromType;
import com.dcampus.weblib.entity.Domain;
import com.dcampus.weblib.entity.DomainCategory;
import com.dcampus.weblib.entity.DomainFolder;
import com.dcampus.weblib.entity.Member;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.service.DomainService;
import com.dcampus.weblib.service.GroupService;
import com.dcampus.weblib.service.GrouperService;
import com.dcampus.weblib.util.ReturnWrapper;
import com.dcampus.weblib.vo.FolderVo;
import com.dcampus.weblib.vo.ManagerVo;

/**
 * 域相关的操作
 * @author patrick
 *
 */
@Controller
@RequestMapping(value = "/domain")

public class DomainController extends BaseController{
	@Autowired
	DomainService domainService;
	@Autowired
	GrouperService grouperService;
	@Autowired
	GroupService groupService;
	@Autowired
	UserService userService;

	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/createDomainOnFolder_v2", produces = "application/json; charset=UTF-8")
	public String createDomainOnFolder_v2(Long[] folderIds, String name,String desc, Long fromDomainId, Long capacity,
			String managerAccount, String managerPass, String managerName, Long[] memberId) throws Exception{  
		if(folderIds==null||name==null){
			throw new GroupsException("参数错误");
		}
		if(name.equals(PropertyUtil.getRootDomainName())) {
			throw new GroupsException("非法用户域名");
		}
		for(long fid:folderIds){
			//检查每个目标folder是否存在
			Member targetFolder = grouperService.getFolder(fid);
			if(targetFolder==null){
				throw new GroupsException("目标组织不存在");
			}
			if(!targetFolder.getMemberType().equals(Member.MEMBER_TYPE_FOLDER)){
				throw new GroupsException("目标节点类型不正确");
			}
			//检查目标组织是否已与某个已有域关联
			domainService.checkFolderAvailable(fid);
		}
		Domain newDomain = domainService.createDomainOnFolder(folderIds,name,desc,fromDomainId,capacity);
		//创建域同时设置域管理员
		if(managerAccount != null && managerPass != null && !managerAccount.trim().equals("")
				&& !managerPass.trim().equals(""))  {
			Member manager = domainService.registNewManagerAccount(managerAccount,managerName==null?managerAccount:managerName,managerPass);
			domainService.addDomainManager(newDomain.getId(), new Long[] {manager.getId()});
		}
		if(memberId!=null) {
			domainService.addDomainManager(newDomain.getId(), memberId);
		}
		List<DomainFolder> dfs = domainService.getDomainFoldersByDomainId(newDomain.getId());
		List<Member> members = domainService.getManagersByDomainId(newDomain.getId());
		List<User> managers = new ArrayList<User>();
		for(Member m:members){
			managers.add(userService.getUserByAccount(m.getAccount()));
		}
		
		return ReturnWrapper.createDomainOnFolderWrapper(newDomain, dfs, members, managers, null);
	}

	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/createDomainWithNewFolder", produces = "application/json; charset=UTF-8")
	public String createDomainWithNewFolder(Long parentFolderId, String name,String desc, Long fromDomainId, Long capacity,
											String managerAccount, String managerPass, String managerName, Long[] memberId)throws Exception{
		if(parentFolderId==null||name==null){
			throw new GroupsException("参数错误");
		}
		if(name.equals(PropertyUtil.getRootDomainName())) {
			throw new GroupsException("非法用户域名");
		}
		if (parentFolderId != 0 && parentFolderId != -1) {
			// 检查folder是否存在
			Member parent;
			try {
				parent = grouperService.getFolder(parentFolderId);
			} catch (Exception e) {
				throw new GroupsException("父组织不存在");
			}
			if (!parent.getMemberType().equals(Member.MEMBER_TYPE_FOLDER)) {
				throw new GroupsException("父组织节点类型不正确");
			}
		}
		//创建域的同名组织
		Member newFolder = grouperService.createFolder(parentFolderId, name);
		//更新lastModified
		grouperService.shiftUpLastModified(newFolder.getId(), new Date());
		//创建域
		Domain newDomain = domainService.createDomainOnFolder(new Long[] {newFolder.getId()},name,desc,fromDomainId,capacity);
		// 创建域同时设置域管理员
		if (managerAccount != null && managerPass != null && !managerAccount.trim().equals("") && !managerPass.trim().equals("")) {
			Member manager = domainService.registNewManagerAccount(managerAccount,
					managerName == null ? managerAccount : managerName, managerPass);
			domainService.addDomainManager(newDomain.getId(), new Long[] { manager.getId() });
		}
		if (memberId != null) {
			domainService.addDomainManager(newDomain.getId(), memberId);
		}
		List<DomainFolder> dfs = domainService.getDomainFoldersByDomainId(newDomain.getId());
		List<Member> members = domainService.getManagersByDomainId(newDomain.getId());
		List<User> managers = new ArrayList<User>();
		for(Member m:members){
			managers.add(userService.getUserByAccount(m.getAccount()));
		}
		Member creator = grouperService.getMemberById(newDomain.getCreator().getId());
		return ReturnWrapper.createDomainOnFolderWrapper(newDomain, dfs, members, managers, creator);
	}
	

	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/getAvailableCapacitySource", produces = "application/json; charset=UTF-8")
	public String getAvailableCapacitySource(Long[] folderIds) throws Exception{
		if(folderIds==null||folderIds.length==0){
			throw new GroupsException("参数错误");
		}
		long  targetFolderId = folderIds[0];
		List<Domain> sources = domainService.getAvailableCapacitySource(targetFolderId);
		List<Map> list = new ArrayList<Map>();
		if(domainService.isRootDomainAdmin(UserUtils.getCurrentMemberId())) {
			Long systemTotalCapacity = PropertyUtil.getTotalGroupSpaceMaxLimit()*1024*1024;
			Long systemAvailableCapacity = groupService.getAvailableCapacity();
			if(systemAvailableCapacity<0)
			    throw new GroupsException("可用容量是一个负数，当前可用容量为"+systemAvailableCapacity);
			Map<String ,Object> domainInfo = new HashMap<String,Object>();
			domainInfo.put("totalCapacity",systemTotalCapacity);
			domainInfo.put("availableCapacity", systemAvailableCapacity);
			list.add(domainInfo);
		}
		for(Domain domain:sources){
			Map<String ,Object> domainInfo = new HashMap<String,Object>();
			domainInfo.put("domainId", domain.getId());
			domainInfo.put("domainName", domain.getDomainName());
			domainInfo.put("totalCapacity",domain.getDomainCategory().getTotalCapacity());
			domainInfo.put("availableCapacity", domain.getDomainCategory().getAvailableCapacity());
			list.add(domainInfo);
		}
		
		Map<String ,Object> map = new HashMap<String,Object>();
		map.put("availableSource", list);
		JSONObject json = JSONObject.fromObject(map);
		return json.toString();
	}

    @RequiresUser
    @ResponseBody
    @RequestMapping(value="/modifyTotalCapacity", produces = "application/json; charset=UTF-8")
    public String modifyTotalCapacity(Long totalCapacity)throws Exception{
        PropertyUtil.setTotalGroupSpaceMaxLimit(totalCapacity);
        return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true,\"totalCapacity\":\""+PropertyUtil.getTotalGroupSpaceMaxLimit()*1024*1024+"\"}";
    }

	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/deleteDomain_v2", produces = "application/json; charset=UTF-8")
	public String deleteDomain_v2(Long domainId, @RequestParam(value = "isDeleteAssociatedFolder",defaultValue = "false") boolean isDeleteAssociatedFolder)throws Exception{
		if(domainId==null){
			throw new GroupsException("参数错误");
		}
		domainService.deleteDomain(domainId,isDeleteAssociatedFolder);
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
	

	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/addRootDomainManager_v2", produces = "application/json; charset=UTF-8")
	public String addRootDomainManager_v2() throws Exception{
		/*DomainManager domainService = ServiceManager.getService().getDomainManager();
		Domain root = domainService.getRootDomain();
		if(root==null||memberId==null){
			throw new ServiceException("参数错误");
		}
		domainService.addRootDomainManager(root.getId(), memberId);*/
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
	

	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/addDomainManager_v2", produces = "application/json; charset=UTF-8")
	public String addDomainManager_v2(Long domainId, Long[] memberId) throws Exception{
		if(domainId==null||memberId==null){
			throw new GroupsException("参数错误");
		}
		domainService.addDomainManager(domainId,memberId);
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
	

	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/revokeRootDomainManager_v2", produces = "application/json; charset=UTF-8")
	public String revokeRootDomainManager_v2()throws Exception{
		/*DomainManager domainService = ServiceManager.getService().getDomainManager();
		Domain root = domainService.getRootDomain();
		if(root==null||memberId==null){
			throw new ServiceException("参数错误");
		}
		domainService.revokeRootDomainManager(root.getId(), memberId);*/
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
	

	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/revokeDomainManager_v2", produces = "application/json; charset=UTF-8")
	public String revokeDomainManager_v2(Long domainId, Long[] memberId) throws Exception{
		if(domainId==null||memberId==null){
			throw new GroupsException("参数错误");
		}
		domainService.revokeDomainManager(domainId,memberId);
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
	

	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/editDomain_v2", produces = "application/json; charset=UTF-8")
	public String editDomain_v2(Long domainId, String name, Long capacity, Long fromDomainId, String desc) throws Exception{
		if(domainId==null){
			throw new GroupsException("参数错误");
		}
//		if(name != null && name.equals(PropertyUtil.getRootDomainName())) {
//			throw new GroupsException("非法用户域名");
//		}
		if(name != null && name.equals(PropertyUtil.getRootDomainName()) && domainId!=1) {
			throw new GroupsException("非法用户域名");
		}
		if(capacity==0) {
			throw new GroupsException("域容量不可为0");
		}
		domainService.editDomain(domainId,name,desc,fromDomainId,capacity);
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
	

	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/addFolderToDomain_v2", produces = "application/json; charset=UTF-8")
	public String addFolderToDomain_v2(Long domainId, Long[] folderIds)throws Exception{
		if(domainId==null||folderIds==null){
			throw new GroupsException("参数错误");
		}
		for(long fid:folderIds){
			//检查每个目标folder是否存在
			Member targetFolder = grouperService.getFolder(fid);
			if(targetFolder==null){
				throw new GroupsException("目标组织不存在");
			}
			if(targetFolder.getMemberType().equals(Member.MEMBER_TYPE_FOLDER)){
				throw new GroupsException("目标节点类型不正确");
			}
			//检查目标组织是否已与某个已有域关联
			domainService.checkFolderAvailable(fid);
		}
		
		domainService.addFolderToDomain(domainId,folderIds);
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/removeFolderFromDomain_v2", produces = "application/json; charset=UTF-8")
	public String removeFolderFromDomain_v2(Long domainId, Long[] folderIds)throws Exception{
		if(domainId==null||folderIds==null){
			throw new GroupsException("参数错误");
		}
		domainService.removeFolderFromDomain(domainId,folderIds);
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/getDomainInfo_v3", produces = "application/json; charset=UTF-8")
	public String getDomainInfo_v3(Long domainId)throws Exception{
		if(domainId==null){
			throw new GroupsException("参数错误");
		}
		Domain  domain = domainService.getDomainById(domainId);
		if(domain==null){
			throw new GroupsException("域不存在");
		}
		List<DomainFolder> dfs = domainService.getDomainFoldersByDomainId(domainId);
		List<Member> members = domainService.getManagersByDomainId(domainId);
		List<User> managers = new ArrayList<User>();
		for(Member m:members){
			managers.add(userService.getUserByAccount(m.getAccount()));
		}
		//实际资源占用容量
		long domainResourceSize = 0L;
		domainResourceSize = domainService.getDomainResourceSize(domain);


		//返回数据
		Map<String, Object> infoMap = new HashMap<String, Object>();
		Map<String,Object> map=new HashMap<String,Object>();
		map=domainService.getParentDomainInfo(domainId);
		infoMap.put("parentDomainId",map.get("父域Id"));
		infoMap.put("parentDomainName",map.get("父域名称"));
		infoMap.put("parentDomainAvailableCapacity",map.get("父域可用容量"));
		infoMap.put("associatedCategoryId",map.get("category Id"));
		infoMap.put("associatedCategoryName",map.get("category name"));
		infoMap.put("domainId", domain.getId());
		infoMap.put("domainName", domain.getDomainName());
		DomainCategory dc = domain.getDomainCategory();
		infoMap.put("totalCapacity", dc==null?"":dc.getTotalCapacity());
		infoMap.put("availableCapacity", dc==null?"":dc.getAvailableCapacity());
		infoMap.put("domainResourceSize", domainResourceSize);
		infoMap.put("desc", domain.getDesc());
		Member creator = domain.getCreator();
		infoMap.put("creatorId", creator==null?0:creator.getId());
		infoMap.put("creatorAccount", creator==null?"系统":creator.getAccount());
		infoMap.put("createDate", domain.getCreateDate().toString()
				.substring(0,domain.getCreateDate().toString().indexOf(".")));

//		infoMap.put("createDate1",domain.getCreateDate());

		if(dfs!=null){
			List<JSONObject> folderVos = new ArrayList<JSONObject>();
			for(DomainFolder df: dfs){
				FolderVo vo = new FolderVo(df.getFolder().getId(),df.getFolder().getSignature());
				JSONObject json = JSONObject.fromObject(vo);
				folderVos.add(json);
			}
			infoMap.put("associatedFolders",folderVos);
		}
		if(managers!=null&&members!=null){
			List<JSONObject> managerVos = new ArrayList<JSONObject>();
			for(int i = 0;i<managers.size();i++){
				User user = managers.get(i);
				Member member = members.get(i);
				ManagerVo vo = new ManagerVo(member.getId(),user.getAccount(),user.getName());
				JSONObject json = JSONObject.fromObject(vo);
				managerVos.add(json);
			}
			infoMap.put("managers",managerVos);
		}

		JSONObject json = JSONObject.fromObject(infoMap);
		
		return json.toString();
	}


	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/getDomainInfo_v2", produces = "application/json; charset=UTF-8")
	public String getDomainInfo_v2(Long domainId)throws Exception{
		if(domainId==null){
			throw new GroupsException("参数错误");
		}
		Domain  domain = domainService.getDomainById(domainId);
		if(domain==null){
			throw new GroupsException("域不存在");
		}
		List<DomainFolder> dfs = domainService.getDomainFoldersByDomainId(domainId);
		List<Member> members = domainService.getManagersByDomainId(domainId);
		List<User> managers = new ArrayList<User>();
		for(Member m:members){
			managers.add(userService.getUserByAccount(m.getAccount()));
		}
		//实际资源占用容量
		long domainResourceSize = 0L;
		domainResourceSize = domainService.getDomainResourceSize(domain);
		//返回数据
		Map<String, Object> infoMap = new HashMap<String, Object>();
		infoMap.put("domainId", domain.getId());
		infoMap.put("domainName", domain.getDomainName());
		DomainCategory dc = domain.getDomainCategory();
		infoMap.put("totalCapacity", dc==null?"":dc.getTotalCapacity());
		infoMap.put("availableCapacity", dc==null?"":dc.getAvailableCapacity());
		infoMap.put("domainResourceSize", domainResourceSize);
		infoMap.put("desc", domain.getDesc());
		Member creator = domain.getCreator();
		infoMap.put("creatorId", creator==null?0:creator.getId());
		infoMap.put("creatorAccount", creator==null?"系统":creator.getAccount());
		infoMap.put("createDate", domain.getCreateDate());
		if(dfs!=null){
			List<JSONObject> folderVos = new ArrayList<JSONObject>();
			for(DomainFolder df: dfs){
				FolderVo vo = new FolderVo(df.getFolder().getId(),df.getFolder().getSignature());
				JSONObject json = JSONObject.fromObject(vo);
				folderVos.add(json);
			}
			infoMap.put("associatedFolders",folderVos);
		}
		if(managers!=null&&members!=null){
			List<JSONObject> managerVos = new ArrayList<JSONObject>();
			for(int i = 0;i<managers.size();i++){
				User user = managers.get(i);
				Member member = members.get(i);
				ManagerVo vo = new ManagerVo(member.getId(),user.getAccount(),user.getName());
				JSONObject json = JSONObject.fromObject(vo);
				managerVos.add(json);
			}
			infoMap.put("managers",managerVos);
		}

		JSONObject json = JSONObject.fromObject(infoMap);

		return json.toString();
	}

	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/isDomainCategory", produces = "application/json; charset=UTF-8")
	public String isDomainCategory(Long id) {
		Domain domain = domainService.isDomainCategory(id);
		
		Map<String, Object> infoMap = new HashMap<String, Object>();
		infoMap.put("isBelongToDomain", domain==null?false:true);
		if(domain!=null){
			infoMap.put("domainId", domain.getId());
			infoMap.put("domainName", domain.getDomainName());
			infoMap.put("desc", domain.getDesc());
			Member creator = domain.getCreator();
			infoMap.put("creatorId", creator==null?0:creator.getId());
			infoMap.put("creatorAccount", creator==null?"系统":creator.getAccount());
			infoMap.put("createDate", domain.getCreateDate());
		}
		JSONObject json = JSONObject.fromObject(infoMap);
		return json.toString();
	}
	

	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/isDomainGroup", produces = "application/json; charset=UTF-8")
	public String isDomainGroup(Long id) {
		Domain domain = domainService.isDomainGroup(id);
		Map<String, Object> infoMap = new HashMap<String, Object>();
		infoMap.put("isBelongToDomain", domain==null?false:true);
		if(domain!=null){
			infoMap.put("domainId", domain.getId());
			infoMap.put("domainName", domain.getDomainName());
			infoMap.put("desc", domain.getDesc());
			Member creator = domain.getCreator();
			infoMap.put("creatorId", creator==null?0:creator.getId());
			infoMap.put("creatorAccount", creator==null?"系统":creator.getAccount());
			infoMap.put("createDate", domain.getCreateDate());
		}
		JSONObject json = JSONObject.fromObject(infoMap);
		return json.toString();
	}

	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/getDomainAvailableCapacity", produces = "application/json; charset=UTF-8")
	public String getDomainAvailableCapacity(Long domainId) {
		if(domainId==null){
			throw new GroupsException("参数错误");
		}
		Domain  domain = domainService.getDomainById(domainId);
		if(domain==null){
			throw new GroupsException("域不存在");
		}
		//DomainCategoryBean dc = domain.getDomainCategory();
		Map<String, Object> infoMap = new HashMap<String, Object>();
		infoMap.put("domainId", domain.getId());
		infoMap.put("domainName", domain.getDomainName());
		DomainCategory dc = domain.getDomainCategory();
		infoMap.put("totalCapacity", dc==null?0:dc.getTotalCapacity());
		infoMap.put("availableCapacity", dc==null?0:dc.getAvailableCapacity());
		JSONObject json = JSONObject.fromObject(infoMap);
		return json.toString();
	}

	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/modifyPersonalGroupCapacity", produces = "application/json; charset=UTF-8")
	public String modifyPersonalGroupCapacity(Long domainId, Long[] memberId, Long capacity) {
		if (domainId == null || memberId == null) {
			throw new GroupsException("参数错误");
		}
		for(Long mid :memberId) {
			domainService.modifyPersonalGroupCapacity(mid,domainId,capacity);
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}

	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/getMyDomainTag", produces = "application/json; charset=UTF-8")
	public String getMyDomainTag()throws GroupsException{
		List<Domain> domains = domainService.getDomainTagOfMember(null);
		Map<String, Object> obj = new HashMap<String, Object>();
		List<Map<String, Object>> taglist = new ArrayList<Map<String, Object>>();
		if(domains!=null){
			for(Domain domain:domains){
				Map<String, Object> tag = new HashMap<String, Object>();
				tag.put("domainId", domain.getId());
				tag.put("domainName", domain.getDomainName());
				tag.put("tagName",domain.getDomainName());
				taglist.add(tag);
			}
		}
		obj.put("tags", taglist);
		
		JSONObject json = JSONObject.fromObject(obj);
		return json.toString();
	}

	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/getMyCreatedDomainManager", produces = "application/json; charset=UTF-8")
	public String getMyCreatedDomainManager(Long domainId)throws GroupsException{
		List<Member> members = domainService.getMyCreatedDomainManager(domainId);
		List<User> managers = new ArrayList<User>();
		for(Member m:members){
			managers.add(userService.getUserByAccount(m.getAccount()));
		}
		
		Map<String, Object> infoMap = new HashMap<String, Object>();	
		if(managers!=null&&members!=null){
			List<JSONObject> managerVos = new ArrayList<JSONObject>();
			for(int i = 0;i<managers.size();i++){
				User user = managers.get(i);
				Member member = members.get(i);
				ManagerVo vo = new ManagerVo(member.getId(),user.getAccount(),user.getName());
				JSONObject json = JSONObject.fromObject(vo);
				managerVos.add(json);
			}
			infoMap.put("managers",managerVos);
		}
		JSONObject json = JSONObject.fromObject(infoMap);
		return json.toString();
	}

	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/getDomainCapacityStruct", produces = "application/json; charset=UTF-8")
	public String getDomainCapacityStruct(Long domainId)throws GroupsException{
		if(domainId==null) {
			throw new GroupsException("参数错误");
		}
		Domain domain = domainService.getDomainById(domainId);
		List<CapacityDistribution> cds = domainService.getDomainCapacityStruct(domain);
		if(cds==null||cds.size()==0) {
			throw new GroupsException("找不到容量来源");
		}
		CapacityDistribution cd  = cds.get(0);
		long total = domain.getDomainCategory().getTotalCapacity();
		long available = domainService.getDomainAvailableCapacity(domain.getId());
		long used = (total-available)<0?total:(total-available);
		FromType fromType = cd.getFromType();
		
		Map<String, Object> infoMap = new HashMap<String, Object>();
		infoMap.put("totalCapacity", total);
		infoMap.put("usedCapacity", used);
		infoMap.put("fromType", fromType.name());
		
		if(FromType.DOMAIN_CAPACITY.equals(fromType)) {
			Domain fromDomain = cd.getFromDomain();
			infoMap.put("fromDomainId",fromDomain.getId());
			infoMap.put("fromDomainName", fromDomain.getDomainName());
			infoMap.put("fromDomainAvailableCapacity", domainService.getDomainAvailableCapacity(fromDomain.getId()));
		}else if(FromType.GLOBAL_CAPACITY.equals(fromType)) {
			infoMap.put("globalAvailableCapacity", groupService.getAvailableCapacity());
		}
		JSONObject json = JSONObject.fromObject(infoMap);
		return json.toString();
	}
	
	/**
	 * 获取当前用户所管理的域，包括总域
	 * @return
	 * @throws GroupsException
	 */

	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/getMyManageDomain", produces = "application/json; charset=UTF-8")
	public String getMyManageDomain() throws GroupsException{
		List<Domain> domains = domainService.getMyManageDomain();
		Map<String, Object> obj = new HashMap<String, Object>();
		List<Map<String, Object>> domainList = new ArrayList<Map<String, Object>>();
		if(domains!=null){
			for(Domain domain:domains){
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("domainId", domain.getId());
				map.put("domainName", domain.getDomainName());
				map.put("desc",domain.getDesc());
				domainList.add(map);
			}
		}
		obj.put("domains", domainList);
		JSONObject json = JSONObject.fromObject(obj);
		return json.toString();
	}

	@RequiresUser
	@ResponseBody
	@RequestMapping(value="/getDomainByTeam", produces = "application/json; charset=UTF-8")
	public String getDomainByTeam(Long id) throws GroupsException{
		if(id==null) {
			throw new GroupsException("参数错误");
		}
		Domain domain = domainService.getDomainByTeamOrFolder(id,"team");
		Map<String, Object> infoMap = new HashMap<String, Object>();
		infoMap.put("isBelongToDomain", domain==null?false:true);
		if(domain!=null){
			infoMap.put("domainId", domain.getId());
			infoMap.put("domainName", domain.getDomainName());
			infoMap.put("desc", domain.getDesc());
			Member creator = domain.getCreator();
			infoMap.put("creatorId", creator==null?0:creator.getId());
			infoMap.put("creatorAccount", creator==null?"系统":creator.getAccount());
			infoMap.put("createDate", domain.getCreateDate());
		}
		JSONObject json = JSONObject.fromObject(infoMap);
		return json.toString();
	}

}
