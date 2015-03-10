package com.android.contacts.aspire.datasync.task;

/**
 * Filename: BaseTask.java Description: Copyright: Copyright (c)2009 Company:
 * company
 * 
 * @author: liangbo
 * @version: 1.0 Create at: 2010-9-2 上午09:35:47
 * 
 *           Modification History: Date Author Version Description
 *           ------------------------------------------------------------------
 *           2010-9-2 liangbo 1.0 1.0 Version
 */

public abstract class BaseTask {

	public static int TASK_STATE_WENT = 0;
	public static int TASK_STATE_RUN = 1;
	public static int TASK_STATE_STOP = 2;

	public static long TASK_STOP_TIMEOUT = 5 * 60 * 1000;

	// Task的任务类型 139同步任务 和SIM卡同步任务
	private String taskName;

	// 标识使用此任务的账号
	private String taskAccount;
	
	
	//public Object parameter;

	// 任务状态
	private int taskState = TASK_STATE_WENT;

	// 同步塞子
	private Object sycObj = new Object();

	// 任务线程
	TaskThread taskThread = new TaskThread();

	public synchronized int getTaskState() {
		return taskState;
	}

	public synchronized void setTaskState(int taskState) {
		this.taskState = taskState;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getTaskAccount() {
		if(taskAccount==null)
		{
			taskAccount= "";
		}
		return taskAccount;
	}

	public void setTaskAccount(String taskAccount) {
		this.taskAccount = taskAccount;
	}

	public BaseTask(String taskName, String taskAccount/*,Object parameter*/) {
		this.taskName = taskName;
		this.taskAccount = taskAccount;
		//this.parameter  = parameter;
	}

	/**
	 * 等待任务执行完成后 做好准备 这是一个阻塞函数
	 */
	public void waitWentState() {
		try {
			// 等待任务停止通知
			synchronized (sycObj) {
				sycObj.wait(TASK_STOP_TIMEOUT);
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// 设置任务状态为 准备
			setTaskState(TASK_STATE_WENT);
		}
	}

	/**
	 * 设置任务执行停止 这是一个阻塞函数，直到停止完成
	 */
	public void stop() {

//		System.out.println("BASE_TASK --> " + getTaskAccount()
//				+ " -->   stop is call");
		// 设置状态为STOP 这样在执行中的任意步骤，用户可以检查这个状态
		setTaskState(TASK_STATE_STOP);

		try {
			// 等待任务停止通知
			synchronized (sycObj) {
//				System.out.println("BASE_TASK --> " + getTaskAccount()
//						+ " -->   stop is wait");
				sycObj.wait(TASK_STOP_TIMEOUT);
//				System.out.println("BASE_TASK --> " + getTaskAccount()
//						+ " -->   stop is wait ok");
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// 设置任务状态为 准备
			setTaskState(TASK_STATE_WENT);
//			System.out.println("BASE_TASK --> " + getTaskAccount()
//					+ " -->   stop is wait ok set TASK_STATE_WENT");
		}
	}

	public void start() {
		setTaskState(TASK_STATE_RUN);
		System.out.println(getTaskName() +" --> " + getTaskAccount()
				+ " --> start ...");
		// 启动线程
		taskThread.start();

	}

	private void execute() {
		// 设置任务状态为 准备
   
		try {
			beforeExecute();
			
			taskExecute();
			
			afterExecute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		setTaskState(TASK_STATE_WENT);
		System.out.println(getTaskName() +" --> " + getTaskAccount()
				+ " --> end ...");
		// 通知所有的等待者继续
		synchronized (sycObj) {
			sycObj.notifyAll();
		}

	}

	/**
	 * 后续的实现类自己实现的 任务执行方法
	 */
	abstract public void taskExecute();
	
	
	/**
	 * 开始执行任务之前
	 */
	public void beforeExecute(){};
	
	/**
	 * 执行任务之后
	 */
	public void afterExecute(){};

	class TaskThread extends Thread {
		public void run() {
			execute();
		}
	}

}
