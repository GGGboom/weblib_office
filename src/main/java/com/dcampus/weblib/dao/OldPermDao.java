package com.dcampus.weblib.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.dcampus.common.generic.GenericDao;
import com.dcampus.weblib.entity.Group;
import com.dcampus.weblib.entity.OldPerm;

@Repository
public class OldPermDao {
	
	@Autowired
	private GenericDao genericDao;
	/**
	 * 创建或者更新权限项
	 *
	 * @param bean
	 */
	public void saveOrUpdatePerm(OldPerm bean) {
		if(bean.getId() != null && bean.getId() > 0){
			genericDao.update(bean);
		} else {
			genericDao.save(bean);
		}
	}
	
	/**
	 * 根据id获取记录
	 * @param id
	 * @return
	 */
	public OldPerm getOldPermById(long id) {
		return genericDao.get(OldPerm.class, id);
	}

	/**
	 * 删除权限项
	 * @param bean
	 */
	public void deletePerm(OldPerm bean) {
		OldPerm perm = genericDao.get(OldPerm.class, bean.getId());
		genericDao.delete(perm);
	}
	
	/**
	 * 批量删除权限项
	 * @param perms
	 */
	public void deletePerms(List<OldPerm> perms) {
		for (OldPerm p : perms) {
			this.deletePerm(p);
		}
	}

	/**
	 * 删除权限记录
	 *
	 * @param memberId
	 * @param typeId
	 * @param type
	 */
	public void deletePermsByMemberAndType(long memberId, long typeId, int type) {
		List<OldPerm> perms = genericDao.findAll("from OldPerm where memberId = ?1 and typeId = ?2"
				+ "and permType = ?3", memberId, typeId, type);
		this.deletePerms(perms);
	}

	/**
	 * 删除用户的所有权限记录
	 *
	 * @param memberId
	 */
	public void deletePermsByMember(long memberId) {
		List<OldPerm> perms = genericDao.findAll("from OldPerm ol where ol.memberId = ?1", memberId);
		this.deletePerms(perms);
	}

	/**
	 * 删除某作用域的id某类型的所有用户权限
	 *
	 * @param typeId
	 * @param type
	 */
	public void deletePermsByType(long typeId, int type) {
		List<OldPerm> perms = this.getPermsByType(typeId, type);
		this.deletePerms(perms);
	}


	/**
	 * 根据member和被作用域的id获取权限项
	 *
	 * @param memberId
	 * @param typeId
	 * @param type
	 * @return
	 */
	public OldPerm getPermByMemberAndType(long memberId, long typeId, int type) {
		return genericDao.findFirst("from OldPerm  p where p.memberId = ?1 and p.typeId = ?2"
				+ "and p.permType = ?3", memberId, typeId, type);
	}
	
	/**
	 * 获取某作用域的id某类型的所有用户权限
	 * @param typeId
	 * @param type
	 * @return
	 */
	public List<OldPerm> getPermsByType(long typeId, int type) {
		return genericDao.findAll("from OldPerm where typeId = ?1 and permType = ?2", typeId, type);
	}

	public List<OldPerm> getPermsByTypePart(long typeId, int type,int start,int limit) {
		return genericDao.findAll(start,limit,"from OldPerm where typeId = ?1 and permType = ?2", typeId, type);
	}
	/**
	 * 获取所有的权限
	 * @return
	 */
	public List<OldPerm> getAllPerms() {
		return genericDao.findAll("from OldPerm order by id desc");
	}
	/**
	 * 获取用户对应的所有权限
	 * @param memberId
	 * @param type
	 * @return
	 */
	public List<OldPerm> getMemberPerms(long memberId, int type) {
		return genericDao.findAll("from OldPerm p where p.memberId = ?1 and p.permType = ?2", memberId, type);
	}
	
	/**
	 * 获取用户或者用户组对柜子和分类的权限
	 * @param memberIds
	 * @return
	 */
	public List<OldPerm> getMemberCategoryOrGroupPerms(long[] memberIds) {
		StringBuffer hql = new StringBuffer();
		hql.append(" from OldPerm g where g.memberId in (");
		for (long id : memberIds) {
			hql.append(id).append(",");
		}
		hql.deleteCharAt(hql.length() - 1);
		hql.append(") and (permType = ?1 or permType = ?2)");
		return genericDao.findAll(hql.toString(), OldPerm.PERM_TYPE_CATEGORY, OldPerm.PERM_TYPE_GROUP);
	}
}
