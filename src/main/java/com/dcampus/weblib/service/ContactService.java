package com.dcampus.weblib.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dcampus.common.generic.GenericDao;
import com.dcampus.common.service.BaseService;
import com.dcampus.sys.util.UserUtils;
import com.dcampus.weblib.dao.CategoryDao;
import com.dcampus.weblib.dao.MemberDao;
import com.dcampus.weblib.entity.Contact;
import com.dcampus.weblib.entity.ContactSubject;
import com.dcampus.weblib.entity.Member;
import com.dcampus.weblib.exception.PermissionsException;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.service.permission.RolePermissionUtils;
import com.dcampus.weblib.util.userutil.UserRemoteServiceUtil;
import com.dcampus.weblib.util.userutil.models.RemoteItems;

/**
 * 通讯录service
 * @author patrick
 *
 */
@Service
@Transactional(readOnly=false)
public class ContactService extends BaseService{

	@Autowired
	private GenericDao genericDao;
	
	@Autowired
	private MemberDao memberDao;
	
	@Autowired
	private RolePermissionUtils roleUtils;
	
	@Autowired
	private GrouperService grouperService;
	
	/**
	 * 新建或者修改contactSubject
	 * @param contactSubject
	 */
	public void saveOrModifyContactSubject(ContactSubject contactSubject) {
		if(contactSubject.getId() != null && contactSubject.getId() > 0){
			genericDao.update(contactSubject);
		} else {
			genericDao.save(contactSubject);
		}
	}

	/**
	 * 根据id查找contactSubject
	 * @param id
	 * @return
	 */
	public ContactSubject getContactSubjectById(long id) {
		return genericDao.get(ContactSubject.class, id);
	}
	
	
	/**
	 * 根据id删除contactSubject
	 * @param id
	 */
	public void deleteContactSubjectById(long id) {
		ContactSubject contactSubject = this.getContactSubjectById(id);
		genericDao.delete(contactSubject);
	}
	
	/**
	 * 根据contactId和subjectId查找contactSubject
	 * @param cid
	 * @param sid
	 * @return
	 */
	public List<ContactSubject> getContactSubjectByContactIdAndSubjectId(long cid, long sid) {
		return genericDao.findAll("from ContactSubject cs where cs.contact.id = ?1 and cs.subjectId = ?2", cid , sid);
	}
	
	/**
	 * 根据contactId查找contactSubject
	 * @param id
	 * @return
	 */
	public List<ContactSubject> getContactSubjectByContactId(long id) {
		return genericDao.findAll("from ContactSubject cs where cs.contact.id = ?1", id);
	}
	
	/**z
	 * 根据contactId和subjectType查找contactSubject
	 * @param id
	 * @return
	 */
	private List<ContactSubject> getContactSubjectByContactIdAndSubjectType(long cid, String subjectType) {
		return genericDao.findAll("from ContactSubject cs where cs.contact.id = ?1 and cs.subjectType = ?2", cid , subjectType);
	}
	
	/**
	 * 根据contactId和subjectId删除contactSubject
	 * @param sid
	 * @param cid
	 */
	public void removeContactSubjectByContactIdAndSubjectId(long sid, long cid) {
		this.checkContactPerm();
		List<ContactSubject> cs = this.getContactSubjectByContactIdAndSubjectId(cid, sid);
		if (cs != null && cs.size() > 0) {
			for (ContactSubject c : cs) {
				genericDao.delete(c);
			}
		}
	}

	/**
	 * 获取所有ContactSubject
	 * @return
	 * 
	 */
	public List<ContactSubject> getAllContactSubject() {
		return genericDao.findAll("from ContactSubject order by createDate desc");
	}

	/**
	 * 创建或者修改通讯录
	 * @param contact
	 */
	public void saveOrModifyContact(Contact contact) {
		this.checkContactPerm();
		if(contact.getId() != null && contact.getId() > 0){
			genericDao.update(contact);
		} else {
			genericDao.save(contact);
		}
	}

	/**
	 * 根据id查找通讯录
	 * @param id
	 * @return
	 */
	public Contact getContactById(long id) {
		return genericDao.get(Contact.class, id);
	}
	
	
	/**
	 * 根据id删除contact
	 * @param id
	 */
	public void deleteContactById(long id) {
		this.checkContactPerm();
		Contact contact = this.getContactById(id);
		genericDao.delete(contact);
	}
	
	/**
	 * 获取所有通讯录（供web 管理端使用）
	 * @return
	 * 
	 */
	public List<Contact> getAllContact(int start, int limit) {
		this.checkContactPerm();
		return genericDao.findAll(start,limit,"from Contact order by createDate desc");
	}
	/**
	 * 获取通讯录总数（供web 管理端使用）
	 * @return
	 *
	 */
	public long getAllContactTotalCount() {
		return genericDao.findFirst("select count(ll) from Contact ll");
	}
	
	/**
	 * 
	 * @param memberId 我的memberId
	 * @return
	 */
	public List<Contact> getMyContact(long memberId) {
		List<Contact> result = new ArrayList<Contact>();
		List<ContactSubject> cSubjects = this.getAllContactSubject();
		Map<String, Contact> map = new HashMap<String, Contact>();
		for (ContactSubject cs : cSubjects) {
			Member folder = memberDao.getMemberById(cs.getSubjectId());
			if (folder == null || folder.getId() <= 0) {
				this.removeContactSubjectByContactIdAndSubjectId(cs.getContact().getId(), cs.getSubjectId());
				continue;
			} else {
				map.put(folder.getAccount(), cs.getContact());// folder的account是grouper数据库中folder的uuid
			}
		}
		Member member = memberDao.getMemberById(memberId);
		String subjectId = member.getAccount();// grouper DB
												// 中的subjectId对应member中的account或name
		JSONObject json = UserRemoteServiceUtil.getGroupsOfMembers(new String[] { subjectId });
		if(json.isNullObject()||json.isEmpty()){
			return null;
		}
		JSONArray groups = json.getJSONArray("groups");
		
		Set<Long> contactResultSet = new HashSet<Long>();
		if (groups != null) {
			for (int i = 0; i < groups.size(); i++) {
				JSONObject group = groups.getJSONObject(i);
				JSONArray parentFolders = group.getJSONArray("parentFolders");
				if (parentFolders == null || parentFolders.isEmpty()) {
					continue;
				}
				for (int j = 0; j < parentFolders.size(); j++) {
					JSONObject pFolder = parentFolders.getJSONObject(j);
					String uuid = pFolder.getString("uuid");
					Contact c = map.get(uuid);
					if (c != null) {
						if(!contactResultSet.contains(c.getId())){
							result.add(c);
							contactResultSet.add(c.getId());
						}
						
					}
				}
			}
		}
		return result;
	}

//	/**
//	 * @param ids 多个member的id
//	 * @param id Contact的id
//	 */
//	public void addMembersToContact(long[] ids, long id) throws Exception;
	
	public void addFoldersToContact(long[] ids, long cid) throws Exception {
		this.checkContactPerm();
		Contact contact = this.getContactById(cid);
		if(contact == null || contact.getId() <= 0) {
			throw new GroupsException("所加入的通讯录不存在！");
		}

		for (long fid : ids) {
			this.checkFolderIfExist(fid);
			List<ContactSubject> cus = this.getContactSubjectByContactIdAndSubjectId(cid, fid);
			if (cus == null || cus.size() < 1) {
				ContactSubject newContactUser = new ContactSubject();
				newContactUser.setSubjectId(fid);
				newContactUser.setSubjectType(ContactSubject.SUBJECTTYPE_FOLDER);
				newContactUser.setContact(contact);
				newContactUser.setCreateDate(new Timestamp(System.currentTimeMillis()));
				this.saveOrModifyContactSubject(newContactUser);
			}
		}
	}
	


	/**
	 * @return
	 * 获取本人通讯录中的所有根目录
	 * 
	 */
	public List<Member> getMyContactTreeRootFolders(List<Contact> contacts) {
		List<Member> result = new ArrayList<Member>();
		
		Set<Long> uniqueFolderResult = new HashSet<Long>();//用来过滤重复folder
		if (contacts == null || contacts.size() <= 0) {
			return null;
		}
		for (Contact c : contacts) {
			List<ContactSubject> css = this.getContactSubjectByContactIdAndSubjectType(c.getId(), ContactSubject.SUBJECTTYPE_FOLDER);
			for (ContactSubject cs : css) {
				if(uniqueFolderResult.contains(cs.getSubjectId())){
					continue;
				}
				
				Member folder = memberDao.getMemberById(cs.getSubjectId());
				if (folder != null && folder.getId() >0) {
					RemoteItems ri = UserRemoteServiceUtil.findStem(folder.getName());
					folder.setSignature(ri.getFullname());// 由于本地没存foldername,所以从grouper取
				} else {
					this.removeContactSubjectByContactIdAndSubjectId(c.getId(), cs.getSubjectId());
					continue;
				}
				result.add(folder);
				uniqueFolderResult.add(cs.getSubjectId());
			}
		}
		return result;
	}



	/**
	 * @param fid
	 * @return
	 * 
	 * 根据组织的id 查询子组织或组
	 */
	public Member[] getChildrenByParentFolder(long fid)throws Exception {
		return grouperService.getFolderAndTeamByParent_v2(fid);
	}





	/**
	 * 根据contactId 解除contact和subject（组织）关系
	 * @param id
	 */
	public void removeContactSubjectByContactId(long id)throws Exception {
		this.checkContactPerm();
		List<ContactSubject> cs = this.getContactSubjectByContactId(id);
		if (cs != null && cs.size() > 0) {
			for (ContactSubject c : cs) {
				genericDao.delete(c);
			}
		}
	}



	/**
	 * 根据contactId和关联subject（组织）ids数组，解除关联
	 * @param id
	 * @param ids
	 */
	public void removeContactSubjectByContactIdAndSubjectIds(long id, long[] ids) throws Exception {
		for (long sid :ids) {
			this.removeContactSubjectByContactIdAndSubjectId(id, sid);
		}
	}


	private void checkUserIfExist(long mId) throws Exception {
		Member member = memberDao.getMemberById(mId);
		if (member == null) {
			throw new Exception(member.getAccount() + "账号不存在！");
		}
	}

	private void checkFolderIfExist(long fid) throws Exception {
		Member member = memberDao.getMemberById(fid);
		if (!"folder".equals(member.getMemberType())) {
			throw new Exception(member.getId() + "组织不存在！");
		}
	}
	
	private void checkContactPerm() throws PermissionsException{
		if(!roleUtils.hasPermission(UserUtils.getCurrentMemberId(), "perm_contact_m")){
//			throw new PermissionsException("无通讯录管理权限");
			throw new PermissionsException("用户没有加入任何通讯录，请联系管理员！");
		}
	}
	/**
	 * 添加folder到通讯录
	 * @param fid folder对应的 memberId
	 * @param cid 通讯录id
	 * @throws Exception
	 */
	public void addFoldersToContact(long fid, long cid) throws Exception {
		// TODO Auto-generated method stub
		this.checkContactPerm();
		Contact contact = this.getContactById(cid);
		if(contact == null || contact.getId() <= 0) {
			throw new GroupsException("所加入的通讯录不存在！");
		}
		this.checkFolderIfExist(fid);
		List<ContactSubject> cus = this.getContactSubjectByContactIdAndSubjectId(cid, fid);
		if (cus == null || cus.size() < 1) {
			ContactSubject newContactUser = new ContactSubject();
			newContactUser.setSubjectId(fid);
			newContactUser.setSubjectType(ContactSubject.SUBJECTTYPE_FOLDER);
			newContactUser.setContact(contact);
			newContactUser.setCreateDate(new Timestamp(System.currentTimeMillis()));
			this.saveOrModifyContactSubject(newContactUser);
		}
	}
}
