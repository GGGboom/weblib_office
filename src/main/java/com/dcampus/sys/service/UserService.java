package com.dcampus.sys.service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dcampus.sys.security.LoginRealm;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.shiro.SecurityUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dcampus.common.config.LogMessage;
import com.dcampus.common.config.ResourceProperty;
import com.dcampus.common.generic.GenericDao;
import com.dcampus.common.paging.PageNavigater;
import com.dcampus.common.paging.PageTerm;
import com.dcampus.common.paging.SearchTerm;
import com.dcampus.common.paging.SortTerm;
import com.dcampus.common.service.BaseService;
import com.dcampus.common.util.MD5;
import com.dcampus.common.util.SpringApplicationContextHelper;
import com.dcampus.sys.dao.IUserDao;
import com.dcampus.sys.entity.Perm;
import com.dcampus.sys.entity.Role;
import com.dcampus.sys.entity.RolePerm;
import com.dcampus.sys.entity.User;
import com.dcampus.sys.entity.UserRole;
import com.dcampus.sys.util.UserUtils;
import com.dcampus.weblib.dao.AdminDao;
import com.dcampus.weblib.dao.MemberDao;
import com.dcampus.weblib.entity.Admin;
import com.dcampus.weblib.entity.Log;
import com.dcampus.weblib.entity.Member;
import com.dcampus.weblib.entity.OldPerm;
import com.dcampus.weblib.exception.PermissionsException;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.service.LogService;
import com.dcampus.weblib.service.permission.impl.Permission;
import com.dcampus.weblib.util.CheckUtil;
import com.dcampus.weblib.util.FileUtil;
import com.dcampus.weblib.util.SheetWriter;
import com.dcampus.weblib.util.userutil.UserRemoteServiceUtil;
import com.dcampus.weblib.util.userutil.models.RemoteUser;

/**
 * UserService 权限用户信息service 包含member，userbase , admin的操作
 */
@Service
@Transactional(readOnly = false)
public class UserService extends BaseService {

	public static final String HASH_ALGORITHM = "SHA-1";
	public static final int HASH_INTERATIONS = 1024;
	public static final int SALT_SIZE = 8;

	@Autowired
	private GenericDao genericDao;
	@Autowired
	@Lazy
	private LoginRealm loginRealm;
	@Autowired
	private MemberDao memberDao;
	@Autowired
	private AdminDao adminDao;
	@Autowired
	private IUserDao userDao;
	@Autowired
	@Lazy
	private LogService logService;
	@Autowired
	private Permission permission;

	private ApplicationContext context = SpringApplicationContextHelper
			.getInstance().getApplicationContext();

	/**
	 * 验证MD5密码加密解密
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(UserService.entryptPassword("guochao"));
		System.out.println(UserService.validatePassword("mis",
				"9bd3f346a49663ff9c2ff9862d5698d2"));
//        long time1 = System.currentTimeMillis();
//		for(int i=0; i<50; i++){
//		    System.out.println(getRandomPassword(8));
//        }
//        long time2 = System.currentTimeMillis();
//        System.out.println("当前程序耗时："+(time2-time1)+"ms");
	}

	/**
	 * 生成安全的密码，MD5加密，与之前weblib加密方式一致性
	 */
	public static String entryptPassword(String plainPassword) {
		return MD5.hash(plainPassword);
	}

	/**
	 * 验证密码
	 * 
	 * @param plainPassword
	 *            明文密码
	 * @param password
	 *            密文密码
	 * @return 验证成功返回true
	 */
	public static boolean validatePassword(String plainPassword, String password) {
		String encryptionPassword = MD5.hash(plainPassword);
		return password.equals(encryptionPassword);
	}

	// ------------------------------------------------------------
	// Role
	// -----------------------------------------------------------
	public void saveRole(Role role) {
		genericDao.save(role);
	}

	public void saveRoles(Collection<Role> collection) {
		for (Role item : collection) {
			saveRole(item);
		}
	}

	public void deleteRole(Role role) {
		Role deleteOne = genericDao.get(Role.class, role.getId());
		genericDao.delete(deleteOne);
	}

	public void deleteRoles(Collection<Role> collection) {
		for (Role item : collection) {
			deleteRole(item);
		}
	}

	public Role getRole(Long id) {
		return genericDao.get(Role.class, id);
	}

	public Role getRoleByName(String name) {
		return genericDao.findFirst("from Role where name=?1", name);
	}

	// ------------------------------------------------------------
	// Perm
	// -----------------------------------------------------------
	public void savePerm(Perm perm) {
		genericDao.save(perm);
	}

	public void savePerms(Collection<Perm> collection) {
		for (Perm item : collection) {
			savePerm(item);
		}
	}

	public void deletePerm(Perm perm) {
		Perm deleteOne = genericDao.get(Perm.class, perm.getId());
		genericDao.delete(deleteOne);
	}

	public void deletePerms(Collection<Perm> collection) {
		for (Perm item : collection) {
			deletePerm(item);
		}
	}

	public Perm getPerm(String id) {
		return genericDao.get(Perm.class, id);
	}

	public Perm getPermByKey(String key) {
		return genericDao.findFirst("from Perm where key=?1", key);
	}

	// ------------------------------------------------------------
	// UserRole
	// -----------------------------------------------------------
	public void refreshUserRole(Long userId, Collection<Role> newRoles) {
		List<UserRole> urList = genericDao.findAll(
				"from UserRole where user.id=?1", userId);
		if (urList.size() > 0) {
			for (UserRole item : urList) {
				genericDao.delete(item);
			}
		}
		for (Role item : newRoles) {
			genericDao.save(new UserRole(new User(userId), item));
		}
	}

	// ------------------------------------------------------------
	// RolePerm
	// -----------------------------------------------------------
	public void refreshRolePerm(Long roleId, Collection<Perm> newPerms) {
		List<RolePerm> rpList = genericDao.findAll(
				"from RolePerm where role.id=?1", roleId);
		if (rpList.size() > 0) {
			for (RolePerm item : rpList) {
				genericDao.delete(item);
			}
		}
		for (Perm item : newPerms) {
			genericDao.save(new RolePerm(new Role(roleId), item));
		}
	}
	// ------------------------------------------------------------
	// User实现weblib中的userBaseDao中方法
	// -----------------------------------------------------------
	public void saveUser(User user) {
		genericDao.save(user);
	}

	public void saveOrUpdateUser(User user) {
		if (user.getId() != null && user.getId() > 0)
			genericDao.update(user);
		else
			genericDao.save(user);
	}

	public void saveUsers(Collection<User> collection) {
		for (User item : collection) {
			saveUser(item);
		}
	}

	public void deleteUser(User user) {
		User deleteOne = genericDao.get(User.class, user.getId());
		genericDao.delete(deleteOne);
	}

	public void deleteUsers(Collection<User> collection) {
		for (User item : collection) {
			deleteUser(item);
		}
	}

	public User getUser(Long id) {
		return genericDao.get(User.class, id);
	}

	public User getUserByAccount(String account) {
		return genericDao.findFirst("from User where account=?1", account);
	}

	public void updateUserLoginInfo(Long userId) {
		User user = genericDao.get(User.class, userId);
		user.setLastLoginIp(SecurityUtils.getSubject().getSession().getHost());
		user.setLastLoginTime(new Date());
		genericDao.update(user);
	}

	public void updatePasswordById(Long id, String loginName, String newPassword) {
		User user = genericDao.get(User.class, id);
		user.setPassword(UserService.entryptPassword(newPassword));
		genericDao.update(user);
		loginRealm.clearCachedAuthorizationInfo(loginName);
	}

	public void updatePassword(String account, String password) {
		User user = this.getUserByAccount(account);
		user.setPassword(UserService.entryptPassword(password));
		genericDao.update(user);
	}

	/**
	 * 获取用户总数
	 *
	 * @param status
	 * @return
	 */
	public long getNumberOfUser(int status) {
		return genericDao.findFirst(
				"select count(u) from User u where u.status = ?1", status);
	}
	
	/**
	 * 获取用户总数
	 *
	 * @return
	 */
	public long getNumberOfUser() {
		return genericDao.findFirst(
				"select count(*) from User");
	}

	/**
	 * 根据状态获取所有帐号列表
	 * 
	 *
	 * @param status
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<User> getUsers(int status, int start, int limit) {
		return genericDao.findAll(start, limit,
				"from User u where u.status = ?1", status);

	}
	
	/**
	 * 获取所有帐号列表
	 * 
	 *
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<User> getAllUsers(int start, int limit) {
		return genericDao.findAll(start, limit,"from User u ");

	}

	/**
	 * 检测用户输入密码是否正确
	 * 
	 * @param account
	 * @param password
	 */
	public Boolean checkPassword(String account, String password) {
		User user = null;
		user = getUserByAccount(account);
		if (user == null) {
			throw new GroupsException("账号:"+account+"不存在");
		}
		// 密码哈希
		String encryptionPassword = MD5.hash(password);
		// 返回验证结果
		return encryptionPassword.equals(user.getPassword());
	}

	/**
	 * 根据电话查user
	 * @param mobile
	 * @return
	 */
	public User getUserByMobile(String mobile) {
		return genericDao.findFirst("from User where mobile=?1", mobile);
	}
	/**
	 * 根据电话号码作为acount||moblie查user
	 * @param mobile
	 * @return
	 */
	public User getUserByAccountOrMobile(String mobile) {
		User match = null;
		if(mobile==null||"".equals(mobile.trim())){
			return null;
		}
		match = this.getUserByMobile(mobile);
		if(match!=null){
			return match;
		}
		match =  this.getUserByAccount(mobile);
		return match;
	}

	/****************************************** 用户注册修改密码等稍微复杂的service处理开始 **************************************************/

	/**
	 * 创建用户帐户
	 * 已经包含远程创建
	 * @throws GroupsException
	 */
	public void createAccount(User user) throws GroupsException {
			this.createAccountWithoutLog(user);
			// 记录日志
			String desc = LogMessage.getAccountAdd(user.getAccount());
			logService.addOperateLog(Log.ACTION_TYPE_ADD, desc, user.getId(), user.getAccount());
	}
	
	public void createAccountWithoutLog(User user) throws GroupsException {
		// 检验帐户名是否合法
		CheckUtil.checkEnglishName(user.getAccount());
		this.checkPasswordStrength(user.getAccount(), user.getPassword());
		User u = this.getUserByAccount(user.getAccount());
		if (u != null) {
			// 如果已经有该用户，但是标记为注销，启用该用户
			if (u.getUserbaseStatus().equalsIgnoreCase(User.USER_STATUS_DELETE)) {
				if (user.getName() != null) {
					u.setName(user.getName());
				}
				if (user.getPassword() != null) {
					u.setPassword(entryptPassword(user.getPassword()));
				}
				u.setUserbaseStatus(User.USER_STATUS_NORMAL);
			} else {
				// 如果该用户存在，并且标记正常，则抛出异常
				throw new GroupsException(
						ResourceProperty.getDuplicateAccount());
			}
		} else {
			// usersave
			RemoteUser userGrouper = new RemoteUser(user.getAccount(),
					user.getPassword());
			UserRemoteServiceUtil.userSave(userGrouper);
			// 创建本地用户
			String pwd = user.getPassword();
			if (user.getPassword() == null) {
				pwd = user.getAccount();
			}
			user.setPassword(entryptPassword(pwd));
			this.saveUser(user);
		}
	}

    /**
     * 检查是否弱密码
     * @throws GroupsException
     */
	public void checkPasswordStrength(String account, String pw) throws GroupsException{
        if ((null == pw )|| pw.contains(account)){
            throw new GroupsException("密码不符合规则");
        }
        Pattern Password_Pattern = Pattern.compile("^(?![a-zA-Z]+$)(?![A-Z0-9]+$)(?![A-Z\\W_!@#$%^&*`~()-+=]+$)(?![a-z0-9]+$)(?![a-z\\W_!@#$%^&*`~()-+=]+$)(?![0-9\\W_!@#$%^&*`~()-+=]+$)[a-zA-Z0-9\\W_!@#$%^&*`~()-+=]{6,12}$");
//        Pattern Password_Pattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-zA-Z])(.{6,12})$");
        Matcher matcher = Password_Pattern.matcher(pw);
        if (!matcher.matches()) {
            throw new GroupsException("密码不符合规则");
        }
    }

	/**
	 * 注册用户账号（无需权限），与createAccount相比少了创建grouper账号 
	 * 
	 * @param user
	 * @throws GroupsException
	 */
	public void registerAccount(User user) throws GroupsException {
		// 检验帐户名是否合法
		CheckUtil.checkEnglishName(user.getAccount());
		User u = this.getUserByAccount(user.getAccount());
		if (u != null) {
			// 如果该用户存在，则抛出异常
			throw new GroupsException(ResourceProperty.getDuplicateAccount());
		} else {
			// 创建本地用户
			String pwd = user.getPassword();
			if (user.getPassword() == null) {
				pwd = user.getAccount();
			}
			user.setPassword(entryptPassword(pwd));
			this.saveUser(user);
			
			// 记录日志
			String desc = LogMessage.getAccountAdd(user.getAccount());
			logService.addOperateLog(Log.ACTION_TYPE_ADD, desc, user.getId(), user.getAccount());
		}
	}

	/**
	 * 注销用户帐户  将帐户状态设置为close
	 * 
	 * @param account
	 */
	public void expiredAccount(String account) {
		User user = this.getUserByAccount(account);
		if (user == null || user.getId() <= 0) {
			throw new GroupsException("用户不存在！");
		}
		user.setUserbaseStatus(User.USER_STATUS_DELETE);
		this.saveOrUpdateUser(user);
		
		// 记录日志
		String desc = LogMessage.getAccountCancel(account);
		logService.addOperateLog(Log.ACTION_TYPE_CANCEL, desc, user.getId(), user.getAccount());
	}

	/**
	 * 删除用户,将会把用户所有马甲相关的数据清除 删除应用用户尚未实现 
	 * 
	 * @param account
	 */
	public void deleteAccount(String account) {
		// 取出用户马甲
		List<Member> beans = memberDao.getMembersByAccount(account);
		for (Member bean : beans) {
			memberDao.deleteMember(bean);
			
			// 删除应用用户
			//appMemberDao.deleteAppMemberByMember(bean.getId());
		}

		RemoteUser userGrouper = new RemoteUser(account, null);
		UserRemoteServiceUtil.userDelete(userGrouper);
		User user = this.getUserByAccount(account);
		// 清除用户
		if(user!=null)
		     this.deleteUser(user);
		
//		// 记录日志
//		String desc = LogMessage.getAccountDelete(account);
//		logService.addOperateLog(Log.ACTION_TYPE_DELETE, desc, user.getId(), account);
	}

	/**
	 * 修改帐户密码 需要记录日志
	 * 
	 * @param account
	 * @param password
	 */
	public void modifyPassword(String account, String password) {
        this.checkPasswordStrength(account, password);
        RemoteUser user = new RemoteUser(account, password);
//		System.out.println("account : " + account);
//		System.out.println("password : " + password);
		UserRemoteServiceUtil.userEdit(user);

		this.updatePassword(account, password);
	}

	/**
	 * 修改用户信息,该方法不会对密码进行修改
	 *
	 */
	public void modifyAccount(User userBase) {
		User user = this.getUserByAccount(userBase.getAccount());
		if (userBase.getName() != null) {
			user.setName(userBase.getName());
		}
		if (userBase.getCompany() != null) {
			user.setCompany(userBase.getCompany());
		}
		if (userBase.getDepartment() != null) {
			user.setDepartment(userBase.getDepartment());
		}
		if (userBase.getEmail() != null) {
			user.setEmail(userBase.getEmail());
		}
		if (userBase.getIm() != null) {
			user.setIm(userBase.getIm());
		}
		if (userBase.getPhone() != null) {
			user.setPhone(userBase.getPhone());
		}
		if (userBase.getMobile() != null) {
			user.setMobile(userBase.getMobile());
		}
		if (userBase.getAppName() != null) {
			user.setAppName(userBase.getAppName());
		}
		if (userBase.getPosition() != null) {
			user.setPosition(userBase.getPosition());
		}
		genericDao.update(user);
		
		// 记录日志
		String desc = LogMessage.getAccountMod(userBase.getAccount());
		logService.addOperateLog(Log.ACTION_TYPE_MODIFY, desc, user.getId(), user.getName());
	}

//	/**
//	 * 导入帐户列表 需要记录日志
//	 * 
//	 * @param is
//	 * @throws ServiceException
//	 */
//	public Map<String, Integer[]> importAccount(InputStream is)
//			throws ServiceException {
//		IAccountAdaptor adaptor = (IAccountAdaptor) context
//				.getBean("AccountAdaptor");
//		return adaptor.importAccount(is);
//	}

	/**
	 * 导出帐户列表 
	 * 
	 * @param os
	 * @throws GroupsException
	 */
	public void exportAccount(OutputStream os) throws GroupsException {
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("用户名单");
		SheetWriter writer = new SheetWriter(sheet);

		// 先写个标题
		writer.append("帐户").append("姓名").append("状态").append("公司").append("部门")
				.append("电子邮箱").append("电话").append("手机").append("IM").append("职位")
				.nextRow();

		int start = 0;
		int limit = 50;
		while (true) {
			List<User> beans = this.getAllUsers(start, limit);

			if (beans.size() == 0)
				break;

			for (User bean : beans) {
				writer.append(bean.getAccount()).append(bean.getName()).append(
						bean.getUserbaseStatus()).append(bean.getCompany())
						.append(bean.getDepartment()).append(bean.getEmail())
						.append(bean.getPhone()).append(bean.getMobile())
						.append(bean.getIm()).append(bean.getPosition()).nextRow();
			}

			start += limit;
		}

		// 写入到输出流中
		try {
			workbook.write(os);
		} catch (IOException e) {
			throw new GroupsException(e);
		}
		// 记录日志
		String desc = LogMessage.getAccountExport();
		logService.addOperateLog(Log.ACTION_TYPE_EXPORT, desc, -1, "");
	}

    /**
     * 重置密码，并且导出excel
     * 管理员账号才能操作
     * @param ac
     * @param os
     */
    public void resetPassword(String[] ac, OutputStream os) throws GroupsException{
        // TODO Auto-generated method stub
        //管理员才能操作
        if (!permission.isAdmin(UserUtils.getCurrentMemberId())) {
            throw new GroupsException("管理员才能重置密码");
        } else {
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("用户名单");
            SheetWriter writer = new SheetWriter(sheet);

            // 先写个标题
            writer.append("帐户").append("重置后密码")
                    .nextRow();
            if (ac.length != 0) {
                for (String account : ac) {
                    User u = this.getUserByAccount(account);
                    if (u == null) {
                        writer.append(account).append("this account doesn't exist!").nextRow();
                    }else {
                        String pw = getRandomPassword(8);
                        RemoteUser user = new RemoteUser(account, pw);
                        UserRemoteServiceUtil.userEdit(user);
                        this.updatePassword(account, pw);
                        writer.append(account).append(pw).nextRow();
                    }
                }
            }
            // 写入到输出流中
            try {
                workbook.write(os);
            } catch (IOException e) {
                throw new GroupsException(e);
            }
        }
        // 记录日志
        String desc = "重置用户密码";
        logService.addOperateLog(Log.ACTION_TYPE_EXPORT, desc, -1, "");

    }

    /**
     * 生成随机密码
     * @param len
     * @return
     */
    public static String makeRandomPassword(int len) {
        char charr[] = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890~!@#$%^&*.?".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random r = new Random();

        for (int x = 0; x < len; ++x) {
            sb.append(charr[r.nextInt(charr.length)]);
        }
        return sb.toString();
    }

    //获取验证过的随机密码
    public static String getRandomPassword(int len) {
        String result = null;
        result = makeRandomPassword(len);
        if (result.matches(".*[a-z]{1,}.*") && result.matches(".*[A-Z]{1,}.*") && result.matches(".*[0-9]{1,}.*") && result.matches(".*[~!@#$%^&*\\.?]{1,}.*")) {
            return result;
        }
        return getRandomPassword(len);
    }


    /**
	 * 恢复账号状态 
	 * 
	 * @param account
	 */
	public void restoreAccount(String account) {
		User user = this.getUserByAccount(account);
		if (user == null || user.getId() <= 0) {
			throw new GroupsException("用户不存在！");
		}
		if (user.getUserbaseStatus().equalsIgnoreCase(User.USER_STATUS_DELETE)) {
			user.setUserbaseStatus(User.USER_STATUS_NORMAL);
			genericDao.update(user);
			// 记录日志
			String desc = LogMessage.getAccountActivate(account);
			logService.addOperateLog(Log.ACTION_TYPE_ACTIVATE, desc, user.getId(), account);
		}
	}

	/************************************************************ 用户注册修改密码等稍微复杂的service处理结束 ************************************************************/
	/************************************************************ 创建目录组组织等与member有关的操作开始 ************************************************************/


	// -----------------------------------------------
	// Admin操作处理
	// ------------------------------------------------

	/**
	 * 创建管理员项
	 * 
	 * @param admin
	 * @return
	 */
	public void createAdmin(Admin admin) {
		long memberId = UserUtils.getCurrentMemberId();
		this.createAdmin(admin, memberId);
	}
	
	public void createAdmin(Admin admin, long memberId) {
		if (memberId != OldPerm.SYSTEMADMIN_MEMBER_ID) {
			Admin bean = this.getAdminByMember(memberId); 
			if (bean.getType() != Admin.SUPER_ADMIN)
				throw PermissionsException.MemberException;
		}	
		this.saveAdmin(admin); 
	}
	
	private void saveAdmin(Admin admin) {
		adminDao.createAdmin(admin);
	}

	/**
	 * 删除管理员项
	 * 
	 * @param admin
	 * @return
	 */
	public void deleteAdmin(Admin admin) {
		long memberId = UserUtils.getCurrentMemberId();
		this.deleteAdmin(admin, memberId);
	}
	
	public void deleteAdmin(Admin admin, long memberId) {
		if (memberId != OldPerm.SYSTEMADMIN_MEMBER_ID) {
			Admin bean = this.getAdminByMember(memberId); 
			if (bean.getType() != Admin.SUPER_ADMIN)
				throw PermissionsException.MemberException;
		}	
		adminDao.deleteAdmin(admin);
	}



	/**
	 * 根据memberid查找管理员
	 * 
	 * @param memberId
	 * @return
	 */
	public Admin getAdminByMember(long memberId) {
		return adminDao.getAdminByMember(memberId);
	}
	
	/**
	 * 获取所有管理员
	 * 
	 * @return
	 */
	public List<Admin> getAdmins() {
		return adminDao.getAdmins();
	}

	/**
	 * 获取某种类型的管理员信息 只是查询数据库，把其他的判断放到service层去判断 例如type为空时返回所有记录
	 * 
	 * @param type
	 * @return
	 */
	public List<Admin> getAdminByType(int type) {
		return adminDao.getAdminByType(type);
	}

	/**
	 * 获取某种类型的管理员数量 只是查询数据库，把其他的判断放到service层去判断 例如type为空时返回所有记录 查询出来的数据包括一个系统机器人
	 * 
	 * @param type
	 * @return
	 */
	public long countAdmin(int type) {
		Long result = (Long)adminDao.countAdmin(type);
		if (result == null) {
			return 0L;
		}
		return result.longValue();
	}
	

	/**
	 * 上传用户头像
	 * @param file
	 * @param destFile
	 */
	public void uploadPersonalPic(File file, File destFile) {
		FileUtil.copyFileToServer(file, destFile.toURI().toString(), true);
	}
	

	/********************************************** 之前有的方法，但是已经废弃，直接调用 ***************************************************/
	/**
	 * 获取帐户列表 此方法已废弃，请使用getUsers
	 * 
	 * @param status
	 * @param start
	 * @param limit
	 * @return
	 */
	@Deprecated
	public List<User> getAccounts(int status, int start, int limit) {
		return this.getUsers(status, start, limit);
	}

	public PageNavigater<User> searchAccount(SearchTerm searchTerm, SortTerm sortTerm, PageTerm pageTerm) {
		// TODO Auto-generated method stub
		return new PageNavigater(searchTerm, sortTerm, pageTerm, userDao);
	}
	



}
