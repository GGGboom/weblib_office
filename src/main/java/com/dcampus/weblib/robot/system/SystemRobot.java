package com.dcampus.weblib.robot.system;

import org.springframework.beans.factory.annotation.Autowired;

import com.dcampus.common.config.PropertyUtil;
import com.dcampus.common.util.Log;
import com.dcampus.weblib.robot.IRobot;
import com.dcampus.weblib.service.InitService;
import com.itextpdf.text.log.SysoCounter;


/**
 * 该机器人进行圈子数据初始化工作。包括:
 * <p>
 * 1、若没有超级管理员，则创建超级管理员
 * <p>
 * 2、若没有机器人管理员，则创建机器人管理员
 * <p>
 * 3、初始化站点全局配置
 * <p>
 * 4、初始化内置圈子等级
 * 
 * @author zim
 * 
 */

public class SystemRobot implements IRobot {

	@Autowired
	private InitService initService;

	public void run() {
		// 创建超级管理员
		
		try{
			System.out.println("--------------------createSuperAdmin begin");
			initService.createSuperAdmin();
		} catch(Exception e){
			e.printStackTrace();			
			System.out.println("--------------------createSuperAdmin catch");			
		}
		
		// 初始化全局站点配置
		try{
			System.out.println("--------------------initGlobalProperties begin");
			initService.initGlobalProperties();
		} catch(Exception e){
			e.printStackTrace();			
			System.out.println("--------------------initGlobalProperties catch");			
		}
		
		// 初始化圈子等级
		try{
			System.out.println("--------------------initBuildInGroupTypes begin");
			initService.initBuildInGroupTypes();
		} catch(Exception e){
			e.printStackTrace();			
			System.out.println("--------------------initBuildInGroupTypes catch");			
		}
		
		// 初始化分类
		try{
			System.out.println("--------------------initDefaultGroupCategory begin");
			initService.initDefaultGroupCategory();
		} catch(Exception e){
			e.printStackTrace();			
			System.out.println("--------------------initDefaultGroupCategory catch");			
		}
		
		// 初始化grouper
		try{
			System.out.println("--------------------initDefaultGrouper begin");
			initService.initGrouper();
		} catch(Exception e){
			e.printStackTrace();			
			System.out.println("--------------------initDefaultGrouper catch");			
		}

	}

	public void shutdown() {

	}

	
}
