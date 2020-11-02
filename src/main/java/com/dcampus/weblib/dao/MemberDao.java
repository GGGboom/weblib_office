package com.dcampus.weblib.dao;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.dcampus.common.generic.GenericDao;
import com.dcampus.weblib.entity.GroupMemberBinding;
import com.dcampus.weblib.entity.Member;
import com.dcampus.weblib.entity.ShareTeamInfo;
import com.dcampus.weblib.entity.ShareTeamMember;

@Repository
public class MemberDao {
	@Autowired
	private GenericDao genericDao;
	

	/**
	 * 创建或者更新用户
	 * 
	 * @param member
	 */
	public void saveOrUpdateMember(Member member) {
		if (member.getId() != null && member.getId() > 0)
			genericDao.update(member);
		else
			genericDao.save(member);
	}

	/**
	 * 删除用户，同时将所有用户关联的数据清除
	 * 
	 * @param member
	 */
	public void deleteMember(Member member) {
		Member m = genericDao.get(Member.class, member.getId());
		genericDao.delete(m);
	}

	/**
	 * 批量删除用户及用户相关柜子信息等
	 * 
	 * @param members
	 */
	public void deleteMembers(Collection<Member> members) {
		for (Member m : members) {
			this.deleteMember(m);
		}
	}

	/**
	 * 根据用户帐号获取用户列表
	 * 
	 * @param account
	 * @return
	 */
	public List<Member> getMembersByAccount(String account) {

		return genericDao.findAll("from Member m where m.account = ?1 and m.memberStatus = 1 and m.memberType = ?2",
						account, Member.MEMBER_TYPE_PERSON);
	}

	/**
	 * 获取所有用户列表
	 * 
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<Member> getAllMembers(int start, int limit) {
		return genericDao.findAll(start, limit,
				"from Member m order by m.id desc");

	}

	/**
	 * 获取用户信息
	 * 
	 * @param id
	 * @return
	 */
	public Member getMemberById(long id) {
		return genericDao.get(Member.class, id);
	}

	/**
	 * 根据member名字和账号获取member信息
	 *
	 * @param name
	 * @param account
	 * @param type
	 * @return Member
	 */
	public Member getMemberByNameAccount(String name, String account,
			String type) {
		return genericDao
				.findFirst(
						"from Member m where m.name = ?1 and m.account = ?2 and m.memberType = ?3",
						name, account, type);
	}

	/**
	 * 根据姓名查找member
	 *
	 * @param name
	 * @return ListMember members
	 */
	public List<Member> getMembersByName(String name) {
		return genericDao.findAll(
				"from Member m where m.name = ?1 order by m.name asc",
				name);
	}

	public Member getMembersByNameandType(String name,String type) {
		try {
			return genericDao.findFirst(
					"from Member m where m.signature = ?1 and m.memberType=?2 order by m.signature asc",
					name,type);
		}catch (Exception e){
			return  null;
		}
	}
	/**
	 * 获取所有member总数
	 *
	 * @return
	 */
	public long getNumberOfAllMembers() {
		Long result = genericDao.findFirst("select count(m) from Member m");
		return result == null ? 0L : result.longValue();
	}
	
	/**
	 * 查找member与group绑定的记录
	 * 
	 * @param memberId
	 * @param groupId
	 * @return
	 */
	public GroupMemberBinding getBindingByGroupAndMember(long memberId, long groupId) {
		return genericDao.findFirst(
				"from GroupMemberBinding g where g.memberId = ?1 "
						+ "and g.groupId = ?2", memberId, groupId);
	}

	/**
	 * 创建member与group的绑定
	 * 
	 * @param memberId
	 * @param groupId
	 * @param bind 是否绑定
	 */
	public void createMemberGroupBinding(long memberId, long groupId,
			boolean bind) {
		GroupMemberBinding binding = new GroupMemberBinding();
		binding.setGroupId(groupId);
		binding.setMemberId(memberId);
		binding.setStatus(bind ? GroupMemberBinding.STATUS_PASS
				: GroupMemberBinding.STATUS_REQUEST);
		genericDao.save(binding);
	}

	/**
	 * 删除binding
	 * 
	 * @param memberId
	 * @param groupId
	 */
	public void deleteMemberGroupBinding(long memberId, long groupId) {
		GroupMemberBinding g = this.getBindingByGroupAndMember(memberId, groupId);
		genericDao.delete(g);
	}
	
	/**
	 * 删除binding
	 * 
	 * @param groupId
	 */
	public void deleteMemberGroupBinding(long groupId) {
		List<GroupMemberBinding> gs = this.getBingdingsByGroup(groupId);
		if(gs != null && gs.size() >0) {
			for(GroupMemberBinding g : gs) {
				genericDao.delete(g);
			}
		}
	}
	
	/**
	 * 删除binding
	 * 
	 * @param memberId
	 */
	public void deleteMemberGroupBindingByMember(long memberId) {
		List<GroupMemberBinding> gs = this.getBingdingsByMember(memberId);
		if(gs != null && gs.size() >0) {
			for(GroupMemberBinding g : gs) {
				genericDao.delete(g);
			}
		}
	}
	
	/**
	 * 获取柜子中所有成员
	 * @param groupId
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<GroupMemberBinding> getBingdingsByGroup(long groupId, int start, int limit) {
		return genericDao.findAll(start, limit,"from GroupMemberBinding g where g.groupId = ?1", groupId);
	}
	
	/**
	 * 获取柜子中所有成员
	 * @param groupId
	 * @return
	 */
	public List<GroupMemberBinding> getBingdingsByGroup(long groupId) {
		return genericDao.findAll("from GroupMemberBinding g where g.groupId = ?1", groupId);
	}
	
	/**
	 * 获取用户所在的所有柜子
	 * @param memberId
	 * @return
	 */
	public List<GroupMemberBinding> getBingdingsByMember(long memberId) {
		return genericDao.findAll("from GroupMemberBinding g where g.memberId = ?1", memberId);
	}


	/**
	 * group中所有member的数目
	 * 
	 * @param groupId
	 * @return
	 */
	public long getNumberOfAllMembersInGroup(long groupId) {
		Long result = genericDao
				.findFirst(
						"select count(b) from GroupMemberBinding b where b.groupId = ?1",
						groupId);
		return result == null ? 0L : result.longValue();
	}
	
	/**
	 * 根据uuid找team
	 * @param uuid
	 * @return
	 */
	public Member getTeamByName(String uuid) {
		return genericDao.findFirst("from Member m where m.memberType = 'team' and m.name= ?1", uuid);
	}

	/**
	 * 根据uuid找folder
	 * @param uuid
	 * @return
	 */
	public Member getFolderByName(String uuid) {
		return genericDao.findFirst("from Member m where m.memberType = 'folder' and m.name= ?1", uuid);
	}
	
	/**
	 * 获取所有的bindings(table GroupMemberBinding of the database)
	 * @return
	 */
	public List<GroupMemberBinding> getAllBindings() {
		// TODO Auto-generated method stub
		return genericDao.findAll("from GroupMemberBinding where status = ?1 order by id desc", GroupMemberBinding.STATUS_PASS);
	}

	/**
	 * 根据memberId获取所在柜子
	 * @param memberId
	 * @return
	 */
	public long[] getGroupsForMember(long memberId) {
		// TODO Auto-generated method stub
		List <GroupMemberBinding> result = genericDao.findAll("from GroupMemberBinding where memberId = ?1 "
				+ "and status = ?2 order by id desc", memberId, GroupMemberBinding.STATUS_PASS);
		if (result != null && result.size() > 0) {
			long[] groupIds = new long[result.size()];
			for (int i = 0; i < groupIds.length; ++i) {
				groupIds[i] = result.get(i).getGroupId();
			}
			return groupIds;
		} else {
			return null;
		}

	}
	/**
	 * member是否在柜子中
	 * @param memberId
	 * @param groupId
	 * @return
	 */
	public boolean findMemberInGroup(long memberId, long groupId) {
		GroupMemberBinding result = genericDao.findFirst("from GroupMemberBinding where memberId = ?1 "
				+ "and groupId = ?2 and status = ?3 ", memberId, groupId,GroupMemberBinding.STATUS_PASS);
		return result != null;
	}

	/**
	 * 返回所有加入柜子的用户的memberId
	 * @param groupId
	 * @param start
	 * @param limit
	 * @return
	 */
	public long[] getMembersInGroup(long groupId, int start, int limit) {
		List<GroupMemberBinding> result = genericDao.findAll(start, limit,"from GroupMemberBinding where groupId = ?1 and status = ?2 ", groupId, GroupMemberBinding.STATUS_PASS);
		if (result != null && result.size() >0) {
			long[] memberIds = new long[result.size()];
			for (int i = 0; i < result.size(); ++i) {
				memberIds[i] = result.get(i).getMemberId();
			}
			return memberIds;
		}
		return null;
	}
	public long[] getMembersInGroupTotal(long groupId) {
		List<GroupMemberBinding> result = genericDao.findAll("from GroupMemberBinding where groupId = ?1 and status = ?2 ", groupId, GroupMemberBinding.STATUS_PASS);
		if (result != null && result.size() >0) {
			long[] memberIds = new long[result.size()];
			for (int i = 0; i < result.size(); ++i) {
				memberIds[i] = result.get(i).getMemberId();
			}
			return memberIds;
		}
		return null;
	}
	/**
	 * 根据创建者查询自建分享组
	 * @param creator
	 * @return
	 */
	public List<ShareTeamInfo> getShareTeamOfCreator(Member creator) {
		return genericDao.findAll("from ShareTeamInfo as sti where sti.creator.id= ?1", creator.getId());
	}
	
	/**
	 * 创建自建分享组和自建分享组信息
	 * @param name 分享组名字
	 * @param creator 创建者
	 * @param desc 描述
	 * @return
	 */
	public ShareTeamInfo createShareTeam(String name, Member creator,
			String desc) {
		// TODO Auto-generated method stub
		Member st = new Member();
		st.setAccount(creator.getAccount());// 创建者的account
		st.setName(name);
		st.setMemberType(Member.MEMBER_TYPE_SHARETEAM);
		st.setSignature(name);
		st.setMemberStatus(Member.STATUS_NORMAL);
		genericDao.save(st);

		ShareTeamInfo sti = new ShareTeamInfo();
		Timestamp now = new Timestamp(System.currentTimeMillis());
		sti.setCreateDate(now);
		sti.setLastModified(now);
		sti.setCreator((Member) creator);
		sti.setDesc(desc);
		sti.setShareTeam(st);
		genericDao.save(sti);
		return sti;
	}

	/**
	 * 添加用户到自建分享组
	 * @param shareTeam 自建分享组
	 * @param member 用户
	 */
	public void addMemberToShareTeam(Member shareTeam, Member member) {
		// TODO Auto-generated method stub
		String hql = "from " + ShareTeamMember.class.getName() + " as s where s.shareTeam.id=" + shareTeam.getId()
				+ " and s.member.id=" + member.getId();
		Query query = genericDao.createQuery(hql, null);
		ShareTeamMember stm;
		try {
			stm = (ShareTeamMember) query.getSingleResult();
		} catch (NoResultException e) {
			stm = new ShareTeamMember();
			stm.setMember((Member) member);
			stm.setShareTeam((Member) shareTeam);
			stm.setLastModified(new Timestamp(System.currentTimeMillis()));
			genericDao.save(stm);
		}

		stm.setLastModified(new Timestamp(System.currentTimeMillis()));
	}

	/**
	 * 根据分享组id获取分享组成员
	 * @param shareTeamId
	 * @return
	 */
	public List<Member> getShareTeamMember(long shareTeamId) {
		// TODO Auto-generated method stub
		String hql = "select sm.member from " + ShareTeamMember.class.getName() + " as sm where sm.shareTeam.id="
				+ shareTeamId;
		Query q = genericDao.createQuery(hql, null);
		return q.getResultList();
	}

	/**
	 * 根据id删除自建分享组中指定成员
	 * @param shareTeamId
	 * @param ids
	 */
	public void removeMemberFromShareTeam(long shareTeamId, long[] ids) {
		// TODO Auto-generated method stub
		String hql = "delete from  "+ ShareTeamMember.class.getName() + " as sm where sm.shareTeam.id="
				+ shareTeamId+" and ( 0=1";
		for(long id:ids){
			hql+=" or sm.member.id="+id;
		}
		hql+=" ) ";
		Query q = genericDao.createQuery(hql, null);
		q.executeUpdate();
	}

}
