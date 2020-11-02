package com.dcampus.common.config;

import com.dcampus.common.config.interceptor.AuthorizedInteceptor;
import com.dcampus.common.config.interceptor.OverallExceptionResolver;
import com.dcampus.weblib.mail.MailSenderInfo;
import com.dcampus.weblib.robot.system.SystemRobot;
import org.springframework.beans.factory.config.PropertyPathFactoryBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import java.util.Properties;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Scope(value = "singleton")
    @Lazy(value = false)
    @Bean(initMethod = "run",destroyMethod = "shutdown")
    public SystemRobot SystemRobot(){
        SystemRobot systemRobot=new SystemRobot();
        return systemRobot;
    }
    @Scope(value = "singleton")
    @Lazy(value = false)
    @Bean(initMethod = "init")
    public MailSenderInfo MailSenderInfo(){
        MailSenderInfo mailSenderInfo=new MailSenderInfo();
        return mailSenderInfo;
    }

    @Scope(value = "singleton")
    @Bean
    public JavaMailSenderImpl MailSender(){
        JavaMailSenderImpl javaMailSender=new JavaMailSenderImpl();
        MailSenderInfo mailSenderInfo=MailSenderInfo();
        javaMailSender.setUsername(mailSenderInfo.getUsername());
        javaMailSender.setPassword(mailSenderInfo.getPassword());
        javaMailSender.setJavaMailProperties(mailSenderInfo.getMailProperties());
        javaMailSender.setHost(mailSenderInfo.getHost());
        javaMailSender.setPort(mailSenderInfo.getPort());
        return javaMailSender;

    }


    @Bean(name={"multipartResolver"})
    public CommonsMultipartResolver multipartResolver(){
        CommonsMultipartResolver multipartResolver=new CommonsMultipartResolver();
        multipartResolver.setDefaultEncoding("utf-8");
        multipartResolver.setMaxUploadSize(10737418240L);
//        multipartResolver.setUploadTempDir("/tmp");
        return multipartResolver;
    }

    @Bean(name="exceptionResolver")
    public OverallExceptionResolver exceptionResolver(){
        OverallExceptionResolver  exceptionResolver=new OverallExceptionResolver();
        return exceptionResolver;
    }
//
//    @Bean
//    public AuthorizedInteceptor getAuthorizedInteceptor(){
//
//        AuthorizedInteceptor auth=new AuthorizedInteceptor();
//        return auth;
//    }



    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/","redirect:/");
    }

//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(getAuthorizedInteceptor()).addPathPatterns("/**");
//    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
//        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/js/");
//        registry.addResourceHandler("/pages/**").addResourceLocations("classpath:/pages/");
//        registry.addResourceHandler("/themes/**").addResourceLocations("classpath:/themes/");
//        registry.addResourceHandler("/WEB-INF/**").addResourceLocations("classpath:/WEB-INF/");
    }

    @Bean
    public ViewResolver viewResolver(){
        InternalResourceViewResolver inter=new InternalResourceViewResolver();
        inter.setPrefix("/WEB-INF/jsp/");
        inter.setSuffix(".jsp");
        inter.setViewClass(JstlView.class);
        return inter;
    }


}
