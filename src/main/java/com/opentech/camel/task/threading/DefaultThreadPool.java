/**
 * Copyright 2013 OpenTech
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.opentech.camel.task.threading;

import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.opentech.camel.task.executor.WrapedTask;
import com.opentech.camel.task.lifecycle.AbstractLifeCycle;
import com.opentech.camel.task.resource.ResourceType;

/**
 * 
 * @author sihai
 *
 */
public class DefaultThreadPool extends AbstractLifeCycle implements ThreadPool {

	/**
	 * 
	 */
	private int coreThreadCount;
	
	/**
	 * 
	 */
	private int maxThreadCount;
	
	/**
	 * 
	 */
	private long keepAliveTime;
	
	/**
	 * 
	 */
	private ThreadFactory threadFactory;
	
	/**
	 * 
	 */
	private ThreadPoolExecutor innerThreadPool;
	
	/**
	 * 
	 * @param coreThreadCount
	 * @param maxThreadCount
	 * @param keepAliveTime
	 * @param workQueue
	 * @param threadFactory
	 */
	public DefaultThreadPool(int coreThreadCount, int maxThreadCount, long keepAliveTime, ThreadFactory threadFactory) {
		this.coreThreadCount = coreThreadCount;
		this.maxThreadCount = maxThreadCount;
		this.keepAliveTime = keepAliveTime;
		this.threadFactory = threadFactory;
	}
	
	@Override
	public void initialize() {
		super.initialize();
		innerThreadPool = new ThreadPoolExecutor(coreThreadCount, maxThreadCount, keepAliveTime, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>(), threadFactory) {

			@Override
			protected void beforeExecute(Thread t, Runnable r) {
				super.beforeExecute(t, r);
				if(r instanceof WrapedTask) {
					WrapedTask wt = ((WrapedTask)r);
					wt.getToken().getRuntime().release(wt.getToken(), ResourceType.QUEUE);
					wt.getToken().getRuntime().acquired(wt.getToken(), ResourceType.THREAD);
				}
			}

			@Override
			protected void afterExecute(Runnable r, Throwable t) {
				super.afterExecute(r, t);
				if(r instanceof WrapedTask) {
					WrapedTask wt = ((WrapedTask)r);
					wt.getToken().getRuntime().release(wt.getToken(), ResourceType.THREAD);
				}
			}
			
		};
	}

	@Override
	public void shutdown() {
		super.shutdown();
		innerThreadPool.shutdown();
	}

	@Override
	public int getCoreThreadCount() {
		return coreThreadCount;
	}

	@Override
	public int getMaxThreadCount() {
		return maxThreadCount;
	}

	@Override
	public long getThreadKeepAliveTime() {
		return keepAliveTime;
	}

	@Override
	public void execute(Runnable runnable) {
		innerThreadPool.execute(runnable);
	}

	@Override
	public Future submit(Runnable runnable) {
		return innerThreadPool.submit(runnable);
	}
}
