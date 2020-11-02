package com.dcampus.weblib.service.permission;

import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;




import com.dcampus.common.config.PropertyUtil;
import com.dcampus.common.generic.GenericDao;
import com.dcampus.weblib.dao.AdminDao;
import com.dcampus.weblib.dao.MemberDao;
import com.dcampus.weblib.entity.Admin;
import com.dcampus.weblib.entity.DomainManager;
import com.dcampus.weblib.entity.Member;
import com.dcampus.weblib.entity.DomainPermission;
import com.dcampus.weblib.entity.DomainRole;
import com.dcampus.weblib.entity.DomainRolePermission;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.util.userutil.UserRemoteServiceUtil;
import com.dcampus.weblib.util.userutil.models.RemoteItems;

@Service
@Transactional(readOnly=false)
public class RolePermissionUtils {
	
	@Autowired
	private GenericDao genericDao;
	
	@Autowired
	private MemberDao memberDao;
	@Autowired
	private AdminDao adminDao;
	

	public  List<DomainPermission> getPermissionsOfMember(long memberId) {

		String hql = "select rp.permissioin from MemberRole as mr , DomainRolePermission as rp " 
		+"where mr.member.id=" + memberId + " and mr.role.id=rp.role.id";
		Query query = genericDao.createQuery(hql, null);
		List<DomainPermission> result = query.getResultList();
		return result;
	}

	public  boolean hasPermission(long memberId, String permissionName) {
		boolean enableRoleModule = PropertyUtil.getEnableRoleModule();
		if(!enableRoleModule){
			return true;//如果role模块没有启用，则一律返回true，按原系统权限规则判断
		}
		String hql = "select count(rp.permission) from MemberRole as mr , DomainRolePermission as rp " 
		+ "where rp.permission.name='"+permissionName.trim()+"'  and mr.member.id=" + memberId
				+ " and mr.role.id=rp.role.id";
		Query query = genericDao.createQuery(hql, null);
		List<Long> result = query.getResultList();
		if(result==null||result.get(0)==0){
			//兼容原有管理员，判断此Id是不是在Admin表中
			boolean isSysAdmin = false;
			Admin admin = adminDao.getAdminByMember(memberId);
			if(admin!=null){
				isSysAdmin = true;
			} else {
				//判断是否在管理员组里
				Member[] teams = this.getTeamsOfMember(memberId);
				StringBuffer sb  =new StringBuffer("from Admin as a where 0=1 ");
				for(Member t:teams){
					if(t.getMemberType().equalsIgnoreCase(Member.MEMBER_TYPE_TEAM)){
						sb.append(" or a.member.id="+t.getId());
					}
				}
				Query checkMemberInAdminTeam = genericDao.createQuery(sb.toString(), null);
				List<Admin> admins = checkMemberInAdminTeam.getResultList();
				if(!admins.isEmpty()){
					isSysAdmin = true;
				}
			}
			if(isSysAdmin){
				String checkSysAdminPerm = "select count(rp.permission) from "
						+ DomainRolePermission.class.getName() + " as rp " 
						+ "where rp.permission.name='"+permissionName.trim()+"'  and rp.role.name='"+DomainRole.SYSTEM_ADMIN+"'";
				query = genericDao.createQuery(checkSysAdminPerm, null);
				result = query.getResultList();
				if(result!=null&&result.get(0)>0){
					return true;
				}else{
					return false;
				}
			}
			//兼容多域模块中，域管理员的配置方式，判断用户是否是某个域的管理员
			Boolean isDomainAdmin = false;
			String checkDomainAdmin = "from "+DomainManager.class.getName()+" as dm where dm.manager.id="+memberId;
			Query checkDomainAdminQuery = genericDao.createQuery(checkDomainAdmin, null);
			List<DomainManager> dms = checkDomainAdminQuery.getResultList();
			if(dms!=null&&!dms.isEmpty()) {
				isDomainAdmin = true;
			}
			if(isDomainAdmin){
				String checkDomainAdminPerm = "select count(rp.permission) from "
						+ DomainRolePermission.class.getName() + " as rp " 
						+ "where rp.permission.name='"+permissionName.trim()+"'  and rp.role.name='"+DomainRole.DOMAIN_ADMIN+"'";
				query = genericDao.createQuery(checkDomainAdminPerm, null);
				result = query.getResultList();
				if(result!=null&&result.get(0)>0){
					return true;
				}
			}
			return false;
		}
		return true;
	}
	
	private Member[] getTeamsOfMember(long memberId) throws GroupsException {
		
		RemoteItems[] groups = UserRemoteServiceUtil.getGroupsOfUser(memberDao.getMemberById(memberId).getAccount());

		Member[] memberBeans = new Member[groups.length];
		for (int i = 0; i < groups.length; i++) {
			memberBeans[i] = mapping(groups[i].getId(), groups[i].getName(), Member.MEMBER_TYPE_TEAM);
		}
		return memberBeans;
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
}
