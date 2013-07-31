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

import java.util.concurrent.atomic.AtomicLong;

import com.opentech.camel.task.executor.Executor;
import com.opentech.camel.task.resource.TaskDomainResource;
import com.opentech.camel.task.resource.TaskDomainResourceController;
import com.opentech.camel.task.threading.exception.ResourceLimitException;

/**
 * Runtime of task domain
 * @author sihai
 *
 */
public class TaskDomainRuntime implements TaskDomainResourceController {

	/**
	 * Timeout of task domain level
	 */
	private volatile long timeout = Executor.NONE_TIMEOUT;
	
	/**
	 * Resource use by this task domain.
	 */
	private TaskDomainResource resource;
	
	/**
	 * Resource controller use by this task domain.
	 */
	private TaskDomainResourceController resourceController;
	
	/**
	 * Total tasks, equals succeedTasks + failedTasks + executingTask + timeoutTasks
	 */
	private AtomicLong totalTasks;
	
	/**
	 * Count of tasks succeed
	 */
	private AtomicLong succeedTasks;
	
	/**
	 * Count of failed tasks
	 */
	private AtomicLong failedTasks;
	
	/**
	 * Count of tasks executing
	 */
	private AtomicLong executingTasks;
	
	/**
	 * Count of tasks timeout
	 */
	private AtomicLong timeoutTasks;
	
	/**
	 * 
	 * @param timeout
	 * @param resource
	 * @param resourceController
	 */
	public TaskDomainRuntime(long timeout, TaskDomainResource resource, TaskDomainResourceController resourceController) {
		this.timeout = timeout;
		this.resource = resource;
		this.resourceController = resourceController;
		totalTasks = new AtomicLong(0L);
		succeedTasks = new AtomicLong(0L);
		failedTasks = new AtomicLong(0L);
		executingTasks = new AtomicLong(0L);
		timeoutTasks = new AtomicLong(0L);
	}
	
	//=========================================================
	//			TaskDomainResourceController
	//=========================================================
	public void acquire() throws ResourceLimitException {
		resourceController.acquire();
	}

	public void release() {
		resourceController.release();
	}
	
	/**
	 * 
	 * @param task
	 */
	public void taskSubmited(Task task) {
		Status status = task.getStatus();
		assert(Status.NEW == status);
		totalTasks.incrementAndGet();
		executingTasks.incrementAndGet();
		// TODO time out watchdog
	}
	
	/**
	 * 
	 * @param task
	 */
	public void taskCompleted(Task task) {
		Status status = task.getStatus();
		assert(Status.SUCCEED == status || Status.FAILED == status || Status.TIMEOUT == status);
		if(Status.SUCCEED == status) {
			succeedTasks.incrementAndGet();
		} else if(Status.FAILED == status) {
			failedTasks.incrementAndGet();
		} else if(Status.TIMEOUT == status) {
			timeoutTasks.incrementAndGet();
		}
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public long getTimeout() {
		return timeout;
	}

	public long getTotalTasks() {
		return totalTasks.get();
	}

	public long getSucceedTasks() {
		return succeedTasks.get();
	}

	public long getFailedTasks() {
		return failedTasks.get();
	}

	public long getExecutingTasks() {
		return executingTasks.get();
	}

	public long getTimeoutTasks() {
		return timeoutTasks.get();
	}
}
