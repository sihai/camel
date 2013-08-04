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
 * Resource used by this task domain, such as queue etc.
 * @author sihai
 *
 */
public class TaskDomainResource {

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
	 * @param minThreadCount
	 * @param maxThreadCount
	 * @param queue
	 */
	public TaskDomainResource(int minThreadCount, int maxThreadCount, BlockingQueue<Task> queue) {
		this.minThreadCount = minThreadCount;
		this.maxThreadCount = maxThreadCount;
		this.queue = queue;
	}
	
	/**
	 * Get queue used by this task domain
	 * @return
	 */
	public BlockingQueue<Task> getQueue() {
		return queue;
	}
	
	/**
	 * Release resource
	 */
	public void release() {
		queue.clear();
	}
}
