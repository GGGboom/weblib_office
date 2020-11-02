package com.dcampus.sys.security;

import java.util.Collection;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.util.ByteSource;

import com.dcampus.common.util.Encodes;
import com.dcampus.sys.entity.User;
import com.dcampus.sys.entity.UserRole;
import com.dcampus.sys.service.UserService;
import com.dcampus.sys.util.UserUtils;

public class HtmlRealm extends AuthorizingRealm {
	private UserService userService;

	/**
	 * 认证回调函数, 登录时调用
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) throws AuthenticationException {
		HtmlUsernamePasswordToken token = (HtmlUsernamePasswordToken) authcToken;
		String username = token.getUsername();
		if (StringUtils.isBlank(username)) {
			throw new UnknownAccountException("用户名不能为空");
		}

	//	System.out.println("at LocalRealm doGetAuthenticationInfo : " + username);
		
		User user = userService.getUserByAccount(username);
		if (user != null) {
			if (user.getUserbaseStatus().equalsIgnoreCase(User.USER_STATUS_NORMAL)) {
				return new SimpleAuthenticationInfo(new Principal(user), user.getPassword(),  getName());
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

	//	Principal principal = (Principal) getAvailablePrincipal(principals);
	//	Principal principal = (Principal) principals.fromRealm(getName()).iterator().next();
		Principal principal = null;
		Collection<Principal> pColl = principals.fromRealm(getName());
		if (pColl != null && !pColl.isEmpty()) {
			principal = pColl.iterator().next();
		}
		if (principal == null) {
	//		System.out.println("at LocalRealm doGetAuthorizationInfo : principal null ");
			return null;
		}

	//	System.out.println("at LocalRealm doGetAuthorizationInfo : " + principal.getUsername());
		
		User user = userService.getUserByAccount(principal.getUsername());
		if (user != null) {
			UserUtils.putCache("user", user);
			SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
			
			//加载基础角色信息,根据UserType定义
		//	info.addRole(user.getUserType());
			
			//加载启用的自定义权限信息
			Set<UserRole> urSet = user.getUserRoles();
			if (urSet != null) {
				for (UserRole ur : urSet){
//					if (ur.getRole().getStatus() == Role.Status_enabled) {
						info.addRole(ur.getRole().getName());
//						Set<RolePerm> rpSet = ur.getRole().getRolePerms();
//						if (rpSet != null) {
//							for (RolePerm rp : rpSet) {
//								if (rp.getPerm().getStatus() == Perm.Status_enabled) {
//									info.addStringPermission(rp.getPerm().getId());
//								}
//							}
//						}
//					}
				}
			}
			// 更新用户登陆信息
			userService.updateUserLoginInfo(user.getId());
			return info;
		} else {
			return null;
		}
	}
	
	/**
	 * 设定密码校验的Hash算法与迭代次数
	 */
	@PostConstruct
	public void initCredentialsMatcher() {
		HashedCredentialsMatcher matcher = new HashedCredentialsMatcher(UserService.HASH_ALGORITHM);
		matcher.setHashIterations(UserService.HASH_INTERATIONS);
		setCredentialsMatcher(matcher);
	}
	
	/**
	 * 清空用户关联权限认证，待下次使用时重新加载
	 */
	public void clearCachedAuthorizationInfo(String principal) {
		SimplePrincipalCollection principals = new SimplePrincipalCollection(principal, getName());
		clearCachedAuthorizationInfo(principals);
		UserUtils.removeCache("user");
	}

	/**
	 * 清空所有关联认证
	 */
	public void clearAllCachedAuthorizationInfo() {
		Cache<Object, AuthorizationInfo> cache = getAuthorizationCache();
		if (cache != null) {
			for (Object key : cache.keys()) {
				cache.remove(key);
			}
		}
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}
}
