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
import java.util.concurrent.DelayQueue;

import com.opentech.camel.task.Task;
import com.opentech.camel.task.TaskDomain;
import com.opentech.camel.task.TaskDomainRuntime;
import com.opentech.camel.task.threading.ThreadPool;
import com.opentech.camel.task.threading.exception.ResourceLimitException;
import com.opentech.camel.task.threading.exception.TaskException;

/**
 * 
 * @author sihai
 *
 */
public class DefaultExecutor implements Executor {

	/**
	 * Timeout of forced, unit ms
	 */
	private volatile long forcedTimeout = DEFAULT_FORCED_TIMEOUT;
	
	/**
	 * Thread pool for execute task
	 */
	private ThreadPool threadpool;
	
	/**
	 * Task domain name -> task domain
	 */
	private Map<String, TaskDomain> domainMap;
	
	/**
	 * Task domain name -> runtime map
	 */
	private Map<String, TaskDomainRuntime> rumtimeMap;
	
	/**
	 * Default task domain runtime
	 */
	private TaskDomainRuntime defaultRuntime;
	
	/**
	 * Queue for watchdog
	 */
	private DelayQueue<WatchedTask> watchdogQueue;
	
	/**
	 * Thread for watchdog
	 */
	private Thread watchdogThread;
	
	/**
	 * 
	 * @param threadpool
	 */
	public DefaultExecutor(long forcedTimeout, ThreadPool threadpool, Map<String, TaskDomain> domainMap, Map<String, TaskDomainRuntime> rumtimeMap) {
		this.forcedTimeout = forcedTimeout;
		this.threadpool = threadpool;
		this.domainMap = domainMap;
		this.rumtimeMap = rumtimeMap;
	}
	
	/**
	 * 
	 */
	public void initialize() {
		// TODO
	}
	
	/**
	 * 
	 */
	public void shutdown() {
		// TODO
	}
	
	@Override
	public void execute(final Task task) throws ResourceLimitException, TaskException {
		final TaskDomainRuntime runtime = getTaskDomainRuntime(task);
		assert(null != runtime);
		long timeout = getTaskTimeout(runtime, task);
		try {
			// acquire resource
			runtime.acquire();
			// new runnable
			Runnable runnable = new Runnable() {
				
				@Override
				public void run() {
					try {
						// before
						task.before();
						// execute
						task.execute();
						// succeed
						task.succeed();
					} catch (Throwable t) {
						// failed
						task.failed(t);
					} finally {
						// completed, succeed or failed (timeout situation will deal with in watchdog)
						task.after();
					}
				}
				
			};
			if(0 == timeout) {
				// XXX
				threadpool.execute(runnable);
			} else {
				// XXX
				watchdogQueue.put(new WatchedTask(threadpool.submit(runnable), System.currentTimeMillis() + timeout));
			}
		} finally {
			// release resource
			runtime.release();
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
		TaskDomainRuntime runtime = rumtimeMap.get(domainName);
		if(null == runtime) {
			throw new TaskException(String.format("Task domain:%s not supported", domainName));
		}
		return runtime;
	}
	
	/**
	 * 
	 * @param runtime
	 * @param task
	 * @return
	 */
	private long getTaskTimeout(TaskDomainRuntime runtime, Task task) {
		long timeout = task.getTimeout();
		if(0 == timeout) {
			timeout = runtime.getTimeout();
		}
		if(0 == timeout) {
			timeout = this.forcedTimeout;
		}
		return timeout;
	}
	
	public long getForcedTimeout() {
		return forcedTimeout;
	}

	public void setForcedTimeout(long forcedTimeout) {
		this.forcedTimeout = forcedTimeout;
	}
}
