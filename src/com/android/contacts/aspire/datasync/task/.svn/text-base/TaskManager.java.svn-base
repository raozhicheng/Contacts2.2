package com.android.contacts.aspire.datasync.task;

import java.util.HashMap;

/**  
 * Filename:    TaskManager.java  
 * Description:   
 * Copyright:   Copyright (c)2009  
 * Company:    company 
 * @author:     liangbo  
 * @version:    1.0  
 * Create at:   2010-9-2 上午09:36:01  
 *  
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2010-9-2    liangbo             1.0        1.0 Version  
 */

public class TaskManager {

	//
	private static HashMap<String,BaseTask> taskMap= new HashMap<String,BaseTask>();
	
	
	
	
	//添加一个任务到Map中
	public static void put(BaseTask baseTask)
	{
		taskMap.put(baseTask.getTaskName(), baseTask);
	}
	
	
	//得到一个任务
	public static BaseTask getTask(String taskName)
	{
		return taskMap.get(taskName);
	}
	
	/**
	 * 强制启动任务，如果发现已经有一个任务了(taskName)，则停止原来的，然后执行新的
	 * @param newBaseTask
	 */
	public static synchronized void compelStartTask(BaseTask newBaseTask)
	{
		privateCompelStartTask(newBaseTask);
	}
	
	
	/**
	 * 没有同步块的 强制启动任务，如果发现已经有一个任务了(taskName)，则停止原来的，然后执行新的
	 * 方便提供给  compelStartTask  tryStartTask这两个同步函数使用
	 * @param newBaseTask
	 */
	private static void privateCompelStartTask(BaseTask newBaseTask)
	{
		//在原 Map中找看有没有旧的任务
		
		BaseTask oldBaseTask= getTask(newBaseTask.getTaskName());
		//如果有 则判断旧的任务的状态
		if(oldBaseTask!=null)
		{
			if(oldBaseTask.getTaskState() ==BaseTask.TASK_STATE_RUN )
			{
				System.out.println("CompelStartTask  stop oldTask   "+"TASK_MGR --> oldtask="+oldBaseTask.getTaskAccount()+" -->   stop is wait");
				
				//如果是旧任务正在运行  则停止旧任务				
				oldBaseTask.stop();//stop是个阻塞函数，等待任务停止
				//System.out.println("TASK_MGR --> oldtask="+oldBaseTask.getTaskAccount()+" -->   stop is wait ok");
			}
			else if(oldBaseTask.getTaskState() ==BaseTask.TASK_STATE_STOP )
			{
				System.out.println("CompelStartTask wait stop oldTask  "+"TASK_MGR --> oldtask="+oldBaseTask.getTaskAccount()+" -->   waitWentState is wait");
				//如果旧任务已经停止 但还没有准备好 则等待准备好
				//stop是个阻塞函数，等待任务停止
				oldBaseTask.waitWentState();
				//System.out.println("TASK_MGR --> oldtask="+oldBaseTask.getTaskAccount()+" -->   waitWentState is wait ok");
			}
			

//			privateCompelStartTask(newBaseTask);
			//将新任务压入Map
			put(newBaseTask);
			//启动新任务  启动最后na
			System.out.println("CompelStartTask start new task  "+"TASK_MGR --> newBaseTask="+newBaseTask.getTaskAccount()+" -->   start");
			newBaseTask.start();
			
		}
		else
		{
			//说明我们以前没有类似任务
			
			//将新任务压入Map
			put(newBaseTask);
			//启动新任务
			//System.out.println("TASK_MGR --> newBaseTask="+newBaseTask.getTaskAccount()+" -->   start");
			newBaseTask.start();
		}
	}
	
	
	/**
	 * 尝试启动任务，如果发现已经有一个任务了(taskName和 taskAccount相同)，则本任务不再执行
	 * @param newBaseTask
	 */
	public static synchronized void tryStartTask(BaseTask newBaseTask)
	{
		if(newBaseTask==null)
		{
			return ;
		}
		//在原 Map中找看有没有旧的任务
		
		BaseTask oldbaseTask= getTask(newBaseTask.getTaskName());
		//如果有 则判断旧的任务的状态
		if(oldbaseTask!=null )
		{
			//说明是完全相同的任务
			if(oldbaseTask.getTaskAccount().equals(newBaseTask.getTaskAccount()))
			{
				//System.out.println(newBaseTask.getTaskAccount()+"   ---    " + oldbaseTask.getTaskAccount()+" -->  oldbaseTask.getTaskState()="+oldbaseTask.getTaskState());
				if(oldbaseTask.getTaskState() ==BaseTask.TASK_STATE_RUN )
				{
					//如果是旧任务正在运行 说明已经有重复任务了				
				    //则本任务不需要执行	
					System.out.println("tryStartTask，oldTask is run，newTask not start    "+newBaseTask.getTaskAccount()+"   ---    " + oldbaseTask.getTaskAccount()+" -->   FIND_OLD_RUN taskExecute()");
				}
				else if(oldbaseTask.getTaskState() ==BaseTask.TASK_STATE_STOP )
				{
					//如果旧任务已经停止 但还没有准备好 则等待准备好
					//则本任务不需要执行	
					
					//stop是个阻塞函数，等待任务停止
					//baseTask.waitWentState();
					System.out.println("tryStartTask，oldTask is stoping，newTask not start    "+newBaseTask.getTaskAccount()+"   ---    " + oldbaseTask.getTaskAccount()+" -->   FIND_OLD_STOP taskExecute()");
				}
				else
				{
					System.out.println("tryStartTask，oldTask is stoped，newTask not start    "+newBaseTask.getTaskAccount()+"   ---    " + oldbaseTask.getTaskAccount()+" -->   FIND_OLD_WENT taskExecute() ok");
					//将新任务压入Map
					put(newBaseTask);
					//启动新任务
					newBaseTask.start();
				}				
			}
			else//说明是 相同任务  但是是不同的账号 所以要使用强制启动 
			{
				System.out.println("tryStartTask，new account，newTask CompelStart    "+newBaseTask.getTaskAccount()+"   ---     -->   NOT_FIND_OLD_WENT taskExecute() ok");
				privateCompelStartTask(newBaseTask);
			}						
		}
		else
		{
			System.out.println("tryStartTask，new task，newTask start    "+newBaseTask.getTaskAccount()+"   ---     -->   NOT_FIND_OLD_WENT taskExecute() ok");
			//将新任务压入Map
			put(newBaseTask);
			//启动新任务
			newBaseTask.start();
		}
	}
	
	
	
	public static void testTaskManager()
	{
//		for(int i=0;i<5;i++)
//		{
//			//compelStartTask(new BaseTask("test", "TASK "+i){
//			//tryStartTask(new BaseTask("test", "TASK ")  {
//			tryStartTask(new BaseTask("test", "TASK "+i)  {
//				
//				
//				@Override
//				public void taskExecute() {
//					
//					System.out.println(getTaskAccount()+" -->   begin into taskExecute()");
//					
//					for(int j=0;j<10;j++)
//					{
//						System.out.println(getTaskAccount()+" -->   begin into taskExecute() getTaskState()="+getTaskState());
//						if(getTaskState() == BaseTask.TASK_STATE_STOP)
//						{
//							System.out.println(getTaskAccount()+" --> chick stop  ps="+j);
//							return;
//						}
//						try {
//							Thread.sleep(1000);
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//					
//					System.out.println(getTaskAccount()+" -->   end taskExecute()");
//					
//				}});
//			
//		}
		
		
		
		for(i=0;i<5;i++)
		{
			new Thread(){
				
				public void run()
				{
//					//compelStartTask(new BaseTask("test", "TASK "+i){
//					//tryStartTask(new BaseTask("test", "TASK ")  {
					tryStartTask(new BaseTask("test", "TASK "+i)  {
						
						
						@Override
						public void taskExecute() {
							
							System.out.println(getTaskAccount()+" -->   begin into taskExecute()");
							
							for(int j=0;j<10;j++)
							{
								System.out.println(getTaskAccount()+" -->   begin into taskExecute() getTaskState()="+getTaskState());
								if(getTaskState() == BaseTask.TASK_STATE_STOP)
								{
									System.out.println(getTaskAccount()+" --> chick stop  ps="+j);
									return;
								}
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							
							System.out.println(getTaskAccount()+" -->   end taskExecute()");
							
						}});
				}
				
				
			}.start();
			
			
			
		}
		
	}
	static int i;
}
