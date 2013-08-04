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

import com.opentech.camel.task.executor.Executor;
import com.opentech.camel.task.resource.TaskDomainResource;
import com.opentech.camel.task.resource.TaskDomainResourceController;

/**
 * 
 * @author sihai
 *
 */
public class TaskDomainRuntimeFactory {

	/**
	 * Task domain of this runtime
	 */
	private TaskDomain taskDomain;
	
	/**
	 * Timeout of task domain level
	 */
	private volatile long timeout = Executor.NONE_TIMEOUT;
	
	/**
	 * Resource controller use by this task domain.
	 */
	private TaskDomainResourceController resourceController;
	
	/**
	 * Private 
	 */
	private TaskDomainRuntimeFactory() {
		
	}
	
	/**
	 * 
	 * @return
	 */
	public static TaskDomainRuntimeFactory newInstance() {
		return new TaskDomainRuntimeFactory();
	}
	
	/**
	 * 
	 * @param taskDomain
	 * @return
	 */
	public TaskDomainRuntimeFactory withTaskDomain(TaskDomain taskDomain) {
		this.taskDomain = taskDomain;
		return this;
	}
	
	/**
	 * 
	 * @param timeout
	 * @return
	 */
	public TaskDomainRuntimeFactory withTimeout(long timeout) {
		this.timeout = timeout;
		return this;
	}
	
	/**
	 * 
	 * @param resourceController
	 * @return
	 */
	public TaskDomainRuntimeFactory withResourceController(TaskDomainResourceController resourceController) {
		this.resourceController = resourceController;
		return this;
	}
	
	/**
	 * 
	 * @return
	 */
	public TaskDomainRuntime build() {
		TaskDomainRuntime runtime = new TaskDomainRuntime(taskDomain, timeout, resourceController);
		runtime.initialize();
		return runtime;
	}
}
