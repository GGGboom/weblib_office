package com.dcampus.weblib.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.dcampus.common.generic.GenericDao;
import com.dcampus.weblib.entity.Admin;
@Repository
public class AdminDao {
	@Autowired
	private GenericDao genericDao;

	/**
	 * 创建管理员项
	 * 
	 * @param admin
	 */
	public void createAdmin(Admin admin) {
		genericDao.save(admin);
	}

	/**
	 * 删除管理员项
	 * 
	 * @param admin
	 */
	public void deleteAdmin(Admin admin) {
		Admin bean = genericDao.get(Admin.class, admin.getId());
		genericDao.delete(bean);
	}

	/**
	 * 根据memberId删除管理员
	 * 
	 * @param memberId
	 */
	public void deleteAdminByMember(long memberId) {
		Admin a = genericDao.findFirst("from Admin a where a.member_id = ?1",
				memberId);
		deleteAdmin(a);

	}

	/**
	 * 批量删除管理员项
	 * 
	 * @param admins
	 */
	public void deleteAdmins(List<Admin> admins) {
		for (Admin a : admins) {
			deleteAdmin(a);
		}
	}

	/**
	 * 更新管理员项
	 * 
	 * @param admin
	 */
	public void updateAdmin(Admin admin) {
		genericDao.update(admin);
	}

	/**
	 * 根据memberid查找管理员
	 * 
	 * @param memberId
	 * @return
	 */
	public Admin getAdminByMember(long memberId) {
		return genericDao.findFirst("from Admin a where a.member.id = ?1", memberId);
	}
	
	/**
	 * 获取所有管理员
	 * 
	 * @return
	 */
	public List<Admin> getAdmins() {
		return genericDao.findAll("from Admin order by id desc");
	}

	/**
	 * 获取某种类型的管理员信息 只是查询数据库，把其他的判断放到service层去判断 例如type为空时返回所有记录
	 * 
	 * @param type
	 * @return
	 */
	public List<Admin> getAdminByType(int type) {
		return genericDao.findAll("from Admin a where a.type = ?1", type);
	}

	/**
	 * 获取某种类型的管理员数量 只是查询数据库，把其他的判断放到service层去判断 例如type为空时返回所有记录 查询出来的数据包括一个系统机器人
	 * 
	 * @param type
	 * @return
	 */
	public long countAdmin(int type) {
		return genericDao.findFirst(
				"select count(a) from Admin a where a.type = ?1", type);
	}

}
