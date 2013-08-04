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
package com.opentech.camel.task.resource;

import java.util.concurrent.BlockingQueue;

import com.opentech.camel.task.Task;

/**
 * 
 * @author sihai
 *
 */
public class TaskDomainResourceFactory {

	//======================================================
	//			Thread resource
	//======================================================
	
	/**
	 * 
	 */
	private int minThreadCount;
	
	/**
	 * 
	 */
	private int maxThreadCount;
	
	//======================================================
	//			Queue
	//======================================================
	/**
	 * Queue used by this task domain
	 */
	private BlockingQueue<Task> queue;
	
	/**
	 * 
	 */
	private TaskDomainResourceFactory() {
		
	}
	
	/**
	 * 
	 * @return
	 */
	public static TaskDomainResourceFactory newInstance() {
		return new TaskDomainResourceFactory();
	}
	
	/**
	 * 
	 * @param minThreadCount
	 * @return
	 */
	public TaskDomainResourceFactory withMinThreadCount(int minThreadCount) {
		this.minThreadCount = minThreadCount;
		return this;
	}
	
	/**
	 * 
	 * @param maxThreadCount
	 * @return
	 */
	public TaskDomainResourceFactory withMaxThreadCount(int maxThreadCount) {
		this.maxThreadCount = maxThreadCount;
		return this;
	}
	
	/**
	 * 
	 * @param queue
	 * @return
	 */
	public TaskDomainResourceFactory withQueue(BlockingQueue<Task> queue) {
		this.queue = queue;
		return this;
	}
	
	/**
	 * 
	 * @return
	 */
	public TaskDomainResource build() {
		return new TaskDomainResource(minThreadCount, maxThreadCount, queue);
	}
}
