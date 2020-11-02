package com.dcampus.sys.security;

import java.util.Collection;
import java.util.Date;
import java.util.Set;


import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;

import com.dcampus.sys.entity.RolePerm;
import com.dcampus.sys.entity.User;
import com.dcampus.sys.entity.UserRole;
import com.dcampus.sys.service.UserService;
import com.dcampus.sys.util.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 本地登陆Realm
 */
public class LoginRealm extends AuthorizingRealm {

	@Autowired
	private UserService userService;

	/**
	 * 认证回调函数, 登录时调用
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) throws AuthenticationException {
		UsernamePasswordToken token = (UsernamePasswordToken) authcToken;
		String username = token.getUsername();
		if (StringUtils.isBlank(username)) {
			throw new UnknownAccountException("用户名不能为空");
		}
		User user = userService.getUserByAccount(username);
		if (user != null) {
			if (user.getUserbaseStatus().equalsIgnoreCase(User.USER_STATUS_NORMAL)) {
				//realmName:当前realm对象的name,直接调用父类的getName()方法即可
				String realmName = getName();
				Principal principal = new Principal(user);
				return new SimpleAuthenticationInfo(principal, user.getPassword(),  realmName);
			} else {
				throw new LockedAccountException("用户被禁用！");
			}
		} else {
			throw new UnknownAccountException("用户不存在");
		}
	}

	/**
	 * 授权查询回调函数, 进行鉴权但缓存中无用户的授权信息时调用
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

		Principal principal = null;
		Collection<Principal> pColl = principals.fromRealm(getName());
		if (pColl != null && !pColl.isEmpty()) {
			principal = pColl.iterator().next();
		}
		if (principal == null) {
			return null;
		}
		
		User user = userService.getUserByAccount(principal.getUsername());
		if (user != null) {
			UserUtils.putCache("user", user);
			SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
			
			//加载基础角色信息,根据UserType定义
			//info.addRole(user.getUserType());
			
			//加载启用的自定义权限信息
			Set<UserRole> urSet = user.getUserRoles();
			if (urSet != null) {
				for (UserRole ur : urSet){
					info.addRole(ur.getRole().getName());
					Set<RolePerm> rpSet = ur.getRole().getRolePerms();
					if (rpSet != null) {
						for (RolePerm rp : rpSet) {
							info.addStringPermission(rp.getPerm().getId());
						}
					}
				}
			}
			// 更新用户登陆信息
			user.setLastLoginIp(SecurityUtils.getSubject().getSession().getHost());
			user.setLastLoginTime(new Date());
			userService.updateUserLoginInfo(user.getId());
			return info;
		} else {
			return null;
		}
	}
	
	/**
	 * 清空用户关联权限认证，待下次使用时重新加载
	 */
	public void clearCachedAuthorizationInfo(String principal) {
		SimplePrincipalCollection principals = new SimplePrincipalCollection(principal, getName());
		clearCachedAuthorizationInfo(principals);
		UserUtils.removeCache("user");
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	
}
