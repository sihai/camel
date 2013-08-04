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
package com.opentech.camel.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.opentech.camel.task.executor.Executor;
import com.opentech.camel.task.executor.ExecutorFactory;
import com.opentech.camel.task.queue.QueueFactory;
import com.opentech.camel.task.queue.QueueMode;
import com.opentech.camel.task.resource.TaskDomainResource;
import com.opentech.camel.task.resource.TaskDomainResourceController;
import com.opentech.camel.task.resource.TaskDomainResourceControllerFactory;
import com.opentech.camel.task.resource.TaskDomainResourceFactory;
import com.opentech.camel.task.threading.TaskThreadFactory;
import com.opentech.camel.task.threading.ThreadPool;
import com.opentech.camel.task.threading.ThreadPoolFactory;
import com.opentech.camel.task.watchdog.Watchdog;
import com.opentech.camel.task.watchdog.WatchdogFactory;


/**
 * 
 * @author sihai
 *
 */
public class Bootstrap {

	private static final Log logger = LogFactory.getLog(Bootstrap.class);
	
	/**
	 * s
	 */
	private List<TaskDomain> domains = new ArrayList<TaskDomain>();

	//===================================================================
	//			Executor
	//===================================================================
	/**
	 * 
	 */
	private long forcedTimeout = Executor.DEFAULT_FORCED_TIMEOUT;
	
	/**
	 * Total capacity of all queue
	 */
	private int queueCapacity = Executor.MAX_ALL_QUEUE_CAPACITY;
	
	
	//===================================================================
	//			Thread pool
	//===================================================================
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
	private ThreadFactory threadFactory = new TaskThreadFactory("Task-ThreadPool", null, true);
	
	
	//===================================================================
	//			Watchdog
	//===================================================================
	/**
	 * Thread count used by watch dog
	 */
	private int watchdogThreadCount = Watchdog.DEFAULT_WATCHDOG_THREAD_COUNT;
	
	/**
	 * Total capacity of watch queue
	 */
	private int watchdogQueueCapacity = Watchdog.DEFAULT_WATCHDOG_QUEUE_CAPACITY;
	
	/**
	 * 
	 * @param timeout
	 */
	private void forcedTimeout(long timeout) {
		this.forcedTimeout = timeout;
	}
	
	/**
	 * 
	 * @param domain
	 */
	public void register(TaskDomain domain) {
		domains.add(domain);
	}
	
	/**
	 * 
	 * @return
	 */
	public Executor bootstrap() {
		logger.info("camel bootstrap ...");
		ThreadPool threadpool = ThreadPoolFactory.newInstance()
				.withCoreThreadCount(coreThreadCount)
				.withMaxThreadCount(maxThreadCount)
				.withKeepAliveTime(keepAliveTime)
				.withThreadFactory(threadFactory).build();
		TaskDomainResource resource = null;
		TaskDomainResourceController controller = null;
		Map<String, TaskDomainRuntime> runtimeMap = new HashMap<String, TaskDomainRuntime>(domains.size());
		for(TaskDomain domain : domains) {
			resource = TaskDomainResourceFactory.newInstance()
					.withMinThreadCount(domain.getResourceConfiguration().getThreadingConfiguration().getThreadCount())
					.withMaxThreadCount(domain.getResourceConfiguration().getThreadingConfiguration().getThreadCount())
					.withQueue(QueueFactory.newInstance().withMode(QueueMode.THREAD_SAFE).withCapacity(domain.getResourceConfiguration().getQueueCapacity()).build()).build();
			controller = TaskDomainResourceControllerFactory.newInstance().withResource(resource).withResourceConfiguration(domain.getResourceConfiguration()).build();
			runtimeMap.put(domain.getName(), TaskDomainRuntimeFactory.newInstance().withTaskDomain(domain).withResourceController(controller).withTimeout(domain.getTimeout()).build());
		}
		return ExecutorFactory.newInstance().withForcedTimeout(forcedTimeout).withQueueCapacity(queueCapacity).withRuntimeMap(runtimeMap).withThreadpool(threadpool).withWatchdog(WatchdogFactory.newIntance().withThreadCount(watchdogThreadCount).withQueueCapacity(watchdogQueueCapacity).build()).build();
	}

	public void setDomains(List<TaskDomain> domains) {
		this.domains = domains;
	}

	public void setForcedTimeout(long forcedTimeout) {
		this.forcedTimeout = forcedTimeout;
	}

	public void setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}

	public void setCoreThreadCount(int coreThreadCount) {
		this.coreThreadCount = coreThreadCount;
	}

	public void setMaxThreadCount(int maxThreadCount) {
		this.maxThreadCount = maxThreadCount;
	}

	public void setKeepAliveTime(long keepAliveTime) {
		this.keepAliveTime = keepAliveTime;
	}

	public void setThreadFactory(ThreadFactory threadFactory) {
		this.threadFactory = threadFactory;
	}

	public void setWatchdogThreadCount(int watchdogThreadCount) {
		this.watchdogThreadCount = watchdogThreadCount;
	}

	public void setWatchdogQueueCapacity(int watchdogQueueCapacity) {
		this.watchdogQueueCapacity = watchdogQueueCapacity;
	}
}
