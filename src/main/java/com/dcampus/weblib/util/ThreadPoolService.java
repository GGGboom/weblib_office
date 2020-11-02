package com.dcampus.weblib.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池工具类
 *
 * @author zim
 *
 */
public class ThreadPoolService {

	private int poolSize;

	private ThreadPoolExecutor threadPool;

	private BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(
			1000);

	private long aliveTime;

	/**
	 * 根据给定大小创建线程池
	 */
	public ThreadPoolService(int poolSize, long aliveTime) {
		setPoolSize(poolSize);
		setAliveTime(aliveTime);
		// 启动线程池服务
		createService();
	}

	/**
	 * 使用线程池中的线程来执行任务
	 */
	public void execute(Runnable task) {
		threadPool.execute(task);
	}

	/**
	 * 关闭当前threadPool
	 *
	 * @param timeout
	 *            以毫秒为单位的超时时间
	 */
	public void closeService(long timeout) {
		if (threadPool != null && !threadPool.isShutdown()) {
			try {
				threadPool.awaitTermination(timeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			threadPool.shutdown();
		}
	}

	/**
	 * 关闭当前threadPool，随后根据poolSize创建新的threadPool
	 */
	public void createService() {
		closeService(1000);
		threadPool = new ThreadPoolExecutor(poolSize, poolSize, aliveTime,
				TimeUnit.MILLISECONDS, queue);
	}

	/**
	 * 调整线程池大小
	 */
	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

	/**
	 * 设置线程超时时间
	 *
	 * @param aliveTime
	 */
	public void setAliveTime(long aliveTime) {
		this.aliveTime = aliveTime;
	}
}
