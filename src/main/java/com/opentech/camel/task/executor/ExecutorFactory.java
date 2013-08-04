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
package com.opentech.camel.task.executor;

import java.util.Map;

import com.opentech.camel.task.TaskDomainRuntime;
import com.opentech.camel.task.threading.ThreadPool;
import com.opentech.camel.task.watchdog.Watchdog;

/**
 * 
 * @author sihai
 *
 */
public class ExecutorFactory {
	
	/**
	 * Timeout of forced, unit ms
	 */
	private volatile long forcedTimeout = Executor.DEFAULT_FORCED_TIMEOUT;
	
	/**
	 * Thread pool for execute task
	 */
	private ThreadPool threadpool;
	
	/**
	 * Total capacity of all queue
	 */
	private int queueCapacity = Executor.MAX_ALL_QUEUE_CAPACITY;
	
	/**
	 * Task domain name -> runtime map
	 */
	private Map<String, TaskDomainRuntime> runtimeMap;
	
	/**
	 * 
	 */
	private Watchdog watchdog;
	
	/**
	 * 
	 */
	private ExecutorFactory() {
		
	}
	
	/**
	 * 
	 * @return
	 */
	public static ExecutorFactory newInstance() {
		return new ExecutorFactory();
	}
	
	/**
	 * 
	 * @param forcedTimeout
	 * @return
	 */
	public ExecutorFactory withForcedTimeout(long forcedTimeout) {
		this.forcedTimeout = forcedTimeout;
		return this;
	}
	
	/**
	 * 
	 * @param threadpool
	 * @return
	 */
	public ExecutorFactory withThreadpool(ThreadPool threadpool) {
		this.threadpool = threadpool;
		return this;
	}
	
	/**
	 * 
	 * @param queueCapacity
	 * @return
	 */
	public ExecutorFactory withQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
		return this;
	}
	
	/**
	 * 
	 * @param runtimeMap
	 * @return
	 */
	public ExecutorFactory withRuntimeMap(Map<String, TaskDomainRuntime> runtimeMap) {
		this.runtimeMap = runtimeMap;
		return this;
	}
	
	/**
	 * 
	 * @param watchdog
	 * @return
	 */
	public ExecutorFactory withWatchdog(Watchdog watchdog) {
		this.watchdog = watchdog;
		return this;
	}
	
	/**
	 * 
	 * @return
	 */
	public Executor build() {
		DefaultExecutor executor = new DefaultExecutor(forcedTimeout, threadpool, queueCapacity, runtimeMap, watchdog);
		executor.initialize();
		return executor;
	}
}
