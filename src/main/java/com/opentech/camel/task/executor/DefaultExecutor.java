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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.opentech.camel.task.Task;
import com.opentech.camel.task.TaskDomain;
import com.opentech.camel.task.TaskDomainRuntime;
import com.opentech.camel.task.TaskDomainRuntimeFactory;
import com.opentech.camel.task.exception.ResourceLimitException;
import com.opentech.camel.task.exception.TaskException;
import com.opentech.camel.task.lifecycle.AbstractLifeCycle;
import com.opentech.camel.task.queue.QueueFactory;
import com.opentech.camel.task.queue.QueueMode;
import com.opentech.camel.task.queue.QueuingConfiguration;
import com.opentech.camel.task.resource.ResourceControlMode;
import com.opentech.camel.task.resource.ResourceConfiguration;
import com.opentech.camel.task.resource.TaskDomainResource;
import com.opentech.camel.task.resource.TaskDomainResourceControllerFactory;
import com.opentech.camel.task.resource.TaskDomainResourceFactory;
import com.opentech.camel.task.threading.ThreadPool;
import com.opentech.camel.task.threading.ThreadingConfiguration;
import com.opentech.camel.task.watchdog.Watchdog;

/**
 * Deafult executor
 * @author sihai
 *
 */
public class DefaultExecutor extends AbstractLifeCycle implements Executor {

	private static final Log logger = LogFactory.getLog(DefaultExecutor.class);
	
	/**
	 * Timeout of forced, unit ms
	 */
	private volatile long forcedTimeout = DEFAULT_FORCED_TIMEOUT;
	
	/**
	 * Thread pool for execute task
	 */
	private ThreadPool threadpool;
	
	/**
	 * Total capacity of all queue
	 */
	private int queueCapacity = MAX_ALL_QUEUE_CAPACITY;
	
	/**
	 * Task domain name -> task domain
	 */
	private Map<String, TaskDomain> domainMap;
	
	/**
	 * Task domain name -> runtime map
	 */
	private Map<String, TaskDomainRuntime> runtimeMap;
	
	/**
	 * Default task domain runtime
	 */
	private TaskDomainRuntime defaultRuntime;
	
	/**
	 * 
	 */
	private Watchdog watchdog;
	
	/**
	 * 
	 * @param threadpool
	 * @param queueCapacity
	 * @param rumtimeMap
	 */
	public DefaultExecutor(ThreadPool threadpool, Map<String, TaskDomainRuntime> rumtimeMap, Watchdog watchdog) {
		this(DEFAULT_FORCED_TIMEOUT, threadpool, MAX_ALL_QUEUE_CAPACITY, rumtimeMap, watchdog);
	}
	
	/**
	 * 
	 * @param threadpool
	 * @param queueCapacity
	 * @param rumtimeMap
	 */
	public DefaultExecutor(ThreadPool threadpool, int queueCapacity, Map<String, TaskDomainRuntime> rumtimeMap, Watchdog watchdog) {
		this(DEFAULT_FORCED_TIMEOUT, threadpool, queueCapacity, rumtimeMap, watchdog);
	}
	
	/**
	 * 
	 * @param forcedTimeout
	 * @param threadpool
	 * @param queueCapacity
	 * @param runtimeMap
	 * @param watchdog
	 */
	public DefaultExecutor(long forcedTimeout, ThreadPool threadpool, int queueCapacity, Map<String, TaskDomainRuntime> runtimeMap, Watchdog watchdog) {
		this.forcedTimeout = forcedTimeout;
		this.threadpool = threadpool;
		this.queueCapacity = queueCapacity;
		this.runtimeMap = runtimeMap;
		this.watchdog = watchdog;
	}
	
	/**
	 * 
	 */
	@Override
	public void initialize() {
		// TODO
		assert(null != threadpool);
		assert(null != runtimeMap);
		
		buildRuntime();
	}
	
	/**
	 * 
	 */
	@Override
	public void shutdown() {
		shutdownRuntimes();
		shutdownWatchdog();
	}
	
	@Override
	public void execute(final Task task) throws ResourceLimitException, TaskException {
		TaskDomainRuntime runtime = getTaskDomainRuntime(task);
		assert(null != runtime);
		runtime.execute(task);
	}
	
	@Override
	public void release(String taskDomainName) {
		TaskDomainRuntime runtime = runtimeMap.get(taskDomainName);
		if(null != runtime) {
			// TODO
		}
	}

	//================================================================
	//
	//================================================================
	
	/**
	 * Get task domain of this task, get default if none
	 * @param task
	 * @return
	 * @throws TaskException
	 */
	private TaskDomainRuntime getTaskDomainRuntime(Task task) throws TaskException {
		String domainName = task.getTaskDomain();
		if(null == domainName) {
			return defaultRuntime;
		}
		TaskDomainRuntime runtime = runtimeMap.get(domainName);
		if(null == runtime) {
			throw new TaskException(String.format("Task domain:%s not supported", domainName));
		}
		return runtime;
	}
	
	/**
	 * Config all runtime and build default task domain runtime
	 */
	private void buildRuntime() {
		//int coreThreadSize = threadpool.getCoreThreadCount();
		int maxThreadSize = threadpool.getMaxThreadCount();
		int maxQueueCapacity = this.queueCapacity;
		int needThread = 0;
		int needQueue = 0;
		String domainName = null;
		TaskDomainRuntime runtime = null;
		ResourceConfiguration resourceConfiguration = null;
		ThreadingConfiguration threadingConfiguration = null;
		domainMap = new HashMap<String, TaskDomain>(runtimeMap.size());
		for(Map.Entry<String, TaskDomainRuntime> e : runtimeMap.entrySet()) {
			domainName = e.getKey();
			runtime = e.getValue();
			domainMap.put(domainName, runtime.getTaskDomain());
			resourceConfiguration = runtime.getResourceConfiguration();
			
			threadingConfiguration = resourceConfiguration.getThreadingConfiguration();
			maxThreadSize -= threadingConfiguration.getThreadCount();
			needThread += threadingConfiguration.getThreadCount();
			
			maxQueueCapacity -= resourceConfiguration.getQueuingConfiguration().getQueueCapacity();
			needQueue += resourceConfiguration.getQueuingConfiguration().getQueueCapacity();
			/*if(threadingConfiguration.getMode() == ThreadingControlMode.RESERVED) {
				maxSize -= threadingConfiguration.getThreadCount();
			} else if (threadingConfiguration.getMode() == ThreadingControlMode.MAX) {
				needMax += threadingConfiguration.getThreadCount();
			}*/
		}
		
		// 
		if(maxThreadSize <= 0) {
			throw new IllegalArgumentException(String.format("Thread need by all task domain big then the max thread of the pool, maxThreadCount:%d, needed:%d", threadpool.getMaxThreadCount(), needThread));
		}
		if(maxQueueCapacity <= 0) {
			throw new IllegalArgumentException(String.format("Queue capacity needed by all task domain big then maxQueueCapacity:%d, needed:%d", queueCapacity, needQueue));
		}
		
		// new default task domain
		// resource configuration
		resourceConfiguration = new ResourceConfiguration();
		resourceConfiguration.setQueuingConfiguration(new QueuingConfiguration(ResourceControlMode.MAX, maxQueueCapacity));
		resourceConfiguration.setThreadingConfiguration(new ThreadingConfiguration(ResourceControlMode.MAX, maxThreadSize));
		
		// 
		TaskDomain defaultTaskDomain = new TaskDomain(TaskDomain.DEFAULT_TASK_DOMAIN_NAME, resourceConfiguration);
		
		TaskDomainResource resource = TaskDomainResourceFactory
				.newInstance()
				.withMaxThreadCount(maxThreadSize)
				.withQueue(
						QueueFactory.newInstance()
								.withMode(QueueMode.THREAD_SAFE)
								.withCapacity(maxQueueCapacity).build())
				.build();
		
		defaultRuntime = TaskDomainRuntimeFactory
				.newInstance()
				.withTaskDomain(defaultTaskDomain)
				.withResourceController(
						TaskDomainResourceControllerFactory
								.newInstance()
								.withResourceConfiguration(
										resourceConfiguration)
								.withResource(resource).build()).build();
		
		defaultRuntime.setThreadpool(threadpool);
		defaultRuntime.setWatchdog(watchdog);
		
		// 
		for(TaskDomainRuntime r : runtimeMap.values()) {
			//if(ReourceControlMode.RESERVED == r.getResourceConfiguration().getThreadingConfiguration().getMode()) {
				runtime.setDefaultRuntime(defaultRuntime);
			//}
			r.setThreadpool(threadpool);
			r.setWatchdog(watchdog);
		}
	}
	
	//================================================================
	//
	//================================================================
	/**
	 * 
	 */
	private void shutdownRuntimes() {
		for(TaskDomainRuntime runtime : runtimeMap.values()) {
			runtime.shutdown();
		}
		defaultRuntime.shutdown();
	}
	
	/**
	 * TODO
	 */
	private void shutdownWatchdog() {
		watchdog.shutdown();
	}
	
	public long getForcedTimeout() {
		return forcedTimeout;
	}

	public void setForcedTimeout(long forcedTimeout) {
		this.forcedTimeout = forcedTimeout;
	}
}
