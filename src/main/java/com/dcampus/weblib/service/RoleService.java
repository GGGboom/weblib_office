package com.dcampus.weblib.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dcampus.common.generic.GenericDao;
import com.dcampus.sys.util.UserUtils;
import com.dcampus.weblib.dao.AdminDao;
import com.dcampus.weblib.entity.Admin;
import com.dcampus.weblib.entity.Domain;
import com.dcampus.weblib.entity.DomainManager;
import com.dcampus.weblib.entity.DomainPermission;
import com.dcampus.weblib.entity.DomainRole;
import com.dcampus.weblib.entity.Member;
import com.dcampus.weblib.entity.MemberRole;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.util.userutil.UserRemoteServiceUtil;
import com.dcampus.weblib.util.userutil.models.RemoteItems;
import com.dcampus.weblib.util.userutil.models.RemoteUser;
/**
 * 域等角色赋权操作 获取
 * @author patrick
 *
 */

@Service
@Transactional(readOnly = false)
public class RoleService {

	@Autowired
	private GenericDao genericDao;
	@Autowired
	private AdminDao adminDao;
	@Autowired
	private GrouperService grouperService;
	@Autowired
	@Lazy
	private DomainService domainService;
	
	public void grantRoleToMember(Long roleId, Long[] memberIds) throws GroupsException {

		DomainRole role = checkRoleExist(roleId);
		Timestamp now = new Timestamp(System.currentTimeMillis());
		for (Long memberId : memberIds) {
			Member member = checkMemberExist(memberId);
			if (isMemberRoleExist(memberId, roleId)) {
				continue;
			}
			MemberRole memberRole = new MemberRole();
			memberRole.setGrantTime(now);
			memberRole.setLastModified(now);
			memberRole.setMember(member);
			memberRole.setRole(role);
			genericDao.save(memberRole);
		}

	}
	
	
	/**
	 * 检查角色是否存在，存在返回
	 * 不存在抛出异常
	 * @param roleId
	 * @return
	 * @throws GroupsException
	 */
	private DomainRole checkRoleExist(Long roleId) throws GroupsException {
		DomainRole role = genericDao.get(DomainRole.class, roleId);
		if (role == null) {
			throw new GroupsException("角色不存在");
		}
		return role;
	}
	
	private Member checkMemberExist(Long memberId) throws GroupsException {
		Member member = genericDao.get(Member.class, memberId);
		if (member == null) {
			throw new GroupsException("找不到id为" + memberId + "的用户");
		}
		return member;
	}
	
	private boolean isMemberRoleExist(long memberId, long roleId) {
		MemberRole result = genericDao.findFirst("from MemberRole as mr where mr.member.id = ?1  and mr.role.id = ?2 ", memberId, roleId);
		if (result != null) {
			return true;
		}
		return false;
	}
	
	public List<DomainRole> getRolesOfMember(Long memberId) throws GroupsException {
		if (memberId == 0L) {
			memberId = UserUtils.getCurrentMemberId();// 查看自身
		}
		List<DomainRole> result = genericDao.findAll("select distinct mr.role from MemberRole as mr where mr.member.id= ?1", memberId);
		boolean isSysAdmin = false;
		boolean isDomainAdmin = false;
		for (int i = 0; i < result.size(); i++) {
			if (DomainRole.SYSTEM_ADMIN.equals(result.get(i).getName())) {
				isSysAdmin = true;
			}
			if (DomainRole.DOMAIN_ADMIN.equals(result.get(i).getName())) {
				isDomainAdmin = true;
			}
		}
		// 兼容原有管理员，判断此Id是不是在Admin表中
		if (!isSysAdmin) {
			Admin admin = adminDao.getAdminByMember(memberId);
			if (admin != null) {
				isSysAdmin = true;
			} else {
				// 判断是否在管理员组里
				Member[] teams = grouperService.getTeamsOfMember(memberId);
				StringBuffer sb = new StringBuffer("from Admin as a where 0=1 ");
				for (Member t : teams) {
					if (t.getMemberType().equals(Member.MEMBER_TYPE_TEAM)) {
						sb.append(" or a.member.id=" + t.getId());
					}
				}
				List<Admin> admins = genericDao.findAll(sb.toString(), null);
				if (!admins.isEmpty()) {
					isSysAdmin = true;
				}
			
			}

			if (isSysAdmin) {
				// 把系统管理员角色添加到结果集中
				DomainRole sysAdminRole = genericDao.findFirst("from DomainRole as r where r.name=?1", DomainRole.SYSTEM_ADMIN);
				if (sysAdminRole != null) {
					result.add(sysAdminRole);
				} else {
					throw new GroupsException("找不到系统管理员角色，请检查数据库角色表");
				}
			}
		}
		// 兼容多域模块中，域管理员的配置方式，判断用户是否是某个域的管理员
		if (!isDomainAdmin) {
			List<DomainManager> dms = genericDao.findAll("from DomainManager as dm where dm.manager.id=?1", memberId);
			if (dms != null && !dms.isEmpty()) {
				isDomainAdmin = true;
			}

			if (isDomainAdmin) {
				// 把域管理员角色添加到结果集中
				DomainRole domainAdminRole = genericDao.findFirst("from DomainRole as r where r.name=?", DomainRole.DOMAIN_ADMIN);;
				if (domainAdminRole != null) {
					result.add(domainAdminRole);
				} else {
					throw new GroupsException("找不到域管理员角色，请检查数据库角色表");
				}
			}
		}
		return result;
	}
	
	/**
	 * 判断用户是否是系统管理员，兼容角色管理的系统管理员和admin表中的系统管理员以及‘管理员’组里的管理员
	 * @param memberId
	 * @return
	 * @throws GroupsException
	 */
	public boolean isSystemAdmin(Long memberId)throws GroupsException{
		if (memberId==null || memberId == 0L) {
			memberId = UserUtils.getCurrentMemberId();// 查看自身
		}
		List<DomainRole> result = genericDao.findAll("select distinct mr.role from MemberRole as mr where mr.member.id=?1", memberId);

		for (int i = 0; i < result.size(); i++) {
			if (DomainRole.SYSTEM_ADMIN.equals(result.get(i).getName())) {
				return true;
			}
		}
		// 兼容原有管理员，判断此Id是不是在Admin表中

		Admin admin = adminDao.getAdminByMember(memberId);
		if (admin != null) {
			return true;
		} else {
			// 判断是否在管理员组里
			Member[] teams = grouperService.getTeamsOfMember(memberId);
			StringBuffer sb = new StringBuffer("from Admin as a where 0=1 ");
			for (Member t : teams) {
				if (t.getMemberType().equals(Member.MEMBER_TYPE_TEAM)) {
					sb.append(" or a.member.id=" + t.getId());
				}
			}
			List<Admin> admins = genericDao.findAll(sb.toString(), null);
			if (!admins.isEmpty()) {
				return true;
			}
		}
		return false;	
	}
	

	/**
	 * 对用户收回角色
	 * @param roleId 待收回角色id
	 * @param memberIds 代收回用户memberids
	 * @throws GroupsException
	 */
	public void revokeRoleFromMember(Long roleId, Long[] memberIds) throws GroupsException {
		DomainRole role = checkRoleExist(roleId);
		if (role == null)
			return;
		StringBuffer hql = new StringBuffer();
		hql.append("delete from ").append(MemberRole.class.getName()).append(" as mr where mr.role.id=" + roleId);
		hql.append(" and (0=1 ");
		for (Long mid : memberIds) {
			hql.append(" or mr.member.id=").append(mid);
		}
		hql.append(" )");
		Query query = genericDao.createQuery(hql.toString(), null);
		query.executeUpdate();
	}

	public List<DomainRole> getAllRoles()  {
		return genericDao.findAll("from DomainRole ", null);
	}

	/**
	 * 查询拥有某个角色的所有用户； 兼容原有系统管理员（Admin表、管理员组） 兼容域管理员模块
	 */
	@SuppressWarnings("unchecked")
	public List<Member> getMembersByRoleId(Long id) {
		DomainRole role = genericDao.get(DomainRole.class, id);
		if (role == null) {
			throw new GroupsException("角色不存在");
		}
		String roleName = role.getName();
		List<Member> members = genericDao.findAll("select distinct mr.member from MemberRole as mr where mr.role.id=?1", id);
		Set<Long> unique = new HashSet<Long>();
		if (members != null) {
			for (Member m : members) {
				unique.add(m.getId());
			}
		} else {
			members = new ArrayList<Member>();
		}
		if (DomainRole.SYSTEM_ADMIN.equals(roleName)) {
			// 查询具有系统管理员身份的用户(从Admin表、管理员组两个地方查询)
			List<Member> list = genericDao.findAll("select distinct m from Admin as a, Member as m "
					+ "where a.member.id=m.id and m.account!=?1", "system");
			if (list != null) {
				for (Member mem : list) {
					if (Member.MEMBER_TYPE_PERSON.equals(mem.getMemberType()) && !unique.contains(mem.getId())) {
						members.add(mem);
						unique.add(mem.getId());
					} else if (Member.MEMBER_TYPE_TEAM.equals(mem.getMemberType())) {
						// 管理员组，则需要找到组内所有用户
						List<Member> adminFromTeam;
						try {
							RemoteItems group = UserRemoteServiceUtil.findGroup(mem.getName());
							RemoteUser[] users = UserRemoteServiceUtil.getGroupMembers(group.getId());
							StringBuffer adminFromTeamHql = new StringBuffer("select distinct m from Member as m where 0=1");
							for (int i = 0; i < users.length; i++) {
								String name = users[i].getUsername();
								adminFromTeamHql.append(" or m.name='" + name + "' ");
							}
							Query adminFromTeamQuery = genericDao.createQuery(adminFromTeamHql.toString(), null);
							adminFromTeam = adminFromTeamQuery.getResultList();
						} catch (Exception e) {
							adminFromTeam = new ArrayList<Member>();
						}
						for (Member m : adminFromTeam) {
							if (Member.MEMBER_TYPE_PERSON.equals(m.getMemberType()) && !unique.contains(m.getId())) {
								members.add(m);
								unique.add(m.getId());
							}
						}

					}
				}
			}

		} else if (DomainRole.DOMAIN_ADMIN.equals(roleName)) {
			// 查询具有域管理员（非总域）角色的用户
			Domain rootDomain = domainService.getRootDomain();
			long rootDomainId = rootDomain == null ? -1 : rootDomain.getId();
			String domainAdminHql = "select distinct dm.manager from DomainManager" 
					+ " as dm where dm.domain.id=?1";
			List<Member> list = genericDao.findAll(domainAdminHql, rootDomainId);
			if (list != null) {
				for (Member mem : list) {
					if (Member.MEMBER_TYPE_PERSON.equals(mem.getMemberType()) && !unique.contains(mem.getId())) {
						members.add(mem);
						unique.add(mem.getId());
					}
				}
			}

		}
		return members;
	}

	public List<DomainPermission> getPermissionsByRoleId(Long id) {
		DomainRole role;
		if (id!=0) {
			role = genericDao.get(DomainRole.class, id);
		}else {
			//获取总域管理员角色权限时，返回域管理的权限
			role = genericDao.findFirst("select distinct r from DomainRole as r where r.name=?1", DomainRole.DOMAIN_ADMIN);
			id = role.getId();
		}
		if (role == null) {
			throw new GroupsException("角色不存在");
		}
		return genericDao.findAll("select rp.permission from DomainRolePermission as rp where rp.role.id=?1", id);
	}

	public List<DomainPermission> getAllPermissions() {
		return genericDao.findAll("from DomainPermission ", null);
	}
}
