package com.dcampus.common.config;


import com.dcampus.sys.security.LoginRealm;
import com.dcampus.sys.security.ShiroLoginFilter;
import com.dcampus.sys.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;

import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.servlet.Filter;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {


    @Bean(name = "shiroCacheManager")
    public EhCacheManager ehCacheManager(){
        EhCacheManager cacheManager = new EhCacheManager();
        cacheManager.setCacheManagerConfigFile("classpath:ehcache/ehcache-shiro.xml");
        return cacheManager;
    }

    /**
     * 身份认证realm; (这个需要自己写，账号密码校验；权限等)
     *
     * @return
     */
    @Bean
    public LoginRealm loginRealm(UserService userService,EhCacheManager ehCacheManager) {
        LoginRealm myLoginRealm = new LoginRealm();
        myLoginRealm.setUserService(userService);
        myLoginRealm.setCacheManager(ehCacheManager);
        return myLoginRealm;
    }

    @Bean
    public SimpleCookie simpleCookie() {
        SimpleCookie cookie = new SimpleCookie("rememberMe");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(60 * 60 * 24);
        return cookie;
    }

    /**
     * rememberManager
     */
    @Bean
    public RememberMeManager rememberMeManager(SimpleCookie simpleCookie) {
        CookieRememberMeManager manager = new CookieRememberMeManager();
        manager.setCookie(simpleCookie);
        manager.setCipherKey(Base64.decode("6ZmI6I2j5Y+R5aSn5ZOlAA=="));
        return manager;
    }


    @Bean
    public DefaultWebSessionManager sessionManager() {
        DefaultWebSessionManager manager = new DefaultWebSessionManager();
        manager.setGlobalSessionTimeout(21600000);
        manager.setDeleteInvalidSessions(true);
        manager.setSessionValidationSchedulerEnabled(true);
        return manager;
    }
    /**
     * securityManager
     * @return
     */
    @Bean(name = "securityManager")
    public DefaultWebSecurityManager securityManager(LoginRealm loginRealm, RememberMeManager rememberMeManager, DefaultWebSessionManager sessionManager) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        // 设置realm.
        securityManager.setRealm(loginRealm);
        // 设置rememberMeManager
        securityManager.setRememberMeManager(rememberMeManager);
        // 设置SessionManager
        securityManager.setSessionManager(sessionManager);
        SecurityUtils.setSecurityManager(securityManager);
//        ThreadContext.bind(securityManager);
        return securityManager;
    }

    @Bean(name = "shiroFilter")
    public ShiroFilterFactoryBean shiroFilter(@Qualifier("securityManager") DefaultWebSecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        // 必须设置 SecurityManager
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        // 拦截器.
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();
        // 配置不会被拦截的链接 顺序判断
        filterChainDefinitionMap.put("/js/**", "anon");
        filterChainDefinitionMap.put("/themes/**", "anon");
        filterChainDefinitionMap.put("/pages/**", "anon");
        filterChainDefinitionMap.put("/logout", "logout");
        filterChainDefinitionMap.put("/login/**", "anon");
        filterChainDefinitionMap.put("/status", "anon");
        filterChainDefinitionMap.put("/getSystemVersion", "anon");
//        //配置某个url需要某个权限码
//        // 过滤链定义，从上向下顺序执行，一般将 /**放在最为下边
//        filterChainDefinitionMap.put("/**", "authc");
        Map<String, Filter> filters = new LinkedHashMap<>();
        //配置了自定义的ShiroLoginFilter，设置不关于页面，返回json
        filters.put("authc", new ShiroLoginFilter());
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        shiroFilterFactoryBean.setFilters(filters);
        return shiroFilterFactoryBean;
    }






    @Bean
    public FilterRegistrationBean delegatingFilterProxy(){
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        DelegatingFilterProxy proxy = new DelegatingFilterProxy();
        proxy.setTargetFilterLifecycle(true);
        proxy.setTargetBeanName("shiroFilter");
        filterRegistrationBean.setFilter(proxy);
        return filterRegistrationBean;
    }

    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    @DependsOn({"lifecycleBeanPostProcessor"})
    public DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        advisorAutoProxyCreator.setProxyTargetClass(true);
        return advisorAutoProxyCreator;
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(DefaultWebSecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }



}
