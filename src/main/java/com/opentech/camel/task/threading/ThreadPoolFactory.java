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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;

/**
 * Factory of thread pool
 * @author sihai
 *
 */
public class ThreadPoolFactory {

	/**
	 * 
	 */
	private int coreThreadCount = ThreadPool.DEFAULT_CORE_THREAD_COUNT;
	
	/**
	 * 
	 */
	private int maxThreadCount = ThreadPool.DEFAULT_MAX_THREAD_COUNT;
	
	/**
	 * 
	 */
	private long keepAliveTime = ThreadPool.DEFAULT_KEEP_ALIVE_TIME;
	
	/**
	 * 
	 */
	private ThreadFactory threadFactory;
	
	/**
	 * Private
	 */
	private ThreadPoolFactory() {
		
	}
	
	/**
	 * 
	 * @return
	 */
	public static ThreadPoolFactory newInstance() {
		return new ThreadPoolFactory();
	}
	
	/**
	 * 
	 * @param coreThreadCount
	 * @return
	 */
	public ThreadPoolFactory withCoreThreadCount(int coreThreadCount) {
		this.coreThreadCount = coreThreadCount;
		return this;
	}
	
	/**
	 * 
	 * @param maxThreadCount
	 * @return
	 */
	public ThreadPoolFactory withMaxThreadCount(int maxThreadCount) {
		this.maxThreadCount = maxThreadCount;
		return this;
	}
	
	/**
	 * 
	 * @param keepAliveTime
	 * @return
	 */
	public ThreadPoolFactory withKeepAliveTime(long keepAliveTime) {
		this.keepAliveTime = keepAliveTime;
		return this;
	}
	
	/**
	 * 
	 * @param threadFactory
	 * @return
	 */
	public ThreadPoolFactory withThreadFactory(ThreadFactory threadFactory) {
		this.threadFactory = threadFactory;
		return this;
	}
	
	/**
	 * 
	 * @return
	 */
	public ThreadPool build() {
		ThreadPool threadpool = new DefaultThreadPool(coreThreadCount, maxThreadCount, keepAliveTime, threadFactory);
		threadpool.initialize();
		return threadpool;
	}
}
