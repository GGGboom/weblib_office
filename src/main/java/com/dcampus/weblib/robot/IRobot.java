package com.dcampus.weblib.robot;

/**
 * 机器人接口。
 * <p>
 * 提供该接口的实现，并配置到applicationContext-robot.xml中，可以实现程序的自动化运行。
 * </p>
 * 
 * @author zim
 * 
 */
public interface IRobot extends Runnable {
	/**
	 * 机器人运行入口。
	 * <p>
	 * 注意：请配置到applicationContext-robot中作为bean的init-method方法，
	 * 这样可以允许机器人在web开始时自动运行
	 * </p>
	 * 
	 */
	void run();

	/**
	 * 机器人关闭入口
	 * <p>
	 * 注意：请配置到applicationContext-robot中作为bean的destroy-method方法，
	 * 这样可以允许机器人在web关闭前做清理工作
	 * </p>
	 */
	void shutdown();
}
