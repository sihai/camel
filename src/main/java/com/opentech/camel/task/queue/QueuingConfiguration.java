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
package com.opentech.camel.task.queue;

import com.opentech.camel.task.resource.ResourceControlMode;

/**
 * Configuration of queuing
 * @author sihai
 *
 */
public class QueuingConfiguration {

	/**
	 * Default capacity of queue
	 */
	public static final int DEFAULT_QUEUE_CAPACITY = 1024;
	
	/**
	 * Mode of threading control
	 */
	private ResourceControlMode mode = ResourceControlMode.MAX;
	
	/**
	 * Value of thread count (MAX, RESERVED)
	 */
	private int queueCapacity = DEFAULT_QUEUE_CAPACITY;
	
	/**
	 * 
	 */
	public QueuingConfiguration() {
		this(ResourceControlMode.MAX, DEFAULT_QUEUE_CAPACITY);
	}
	
	/**
	 * 
	 * @param queueCapacity
	 */
	public QueuingConfiguration(int threadCount) {
		this(ResourceControlMode.MAX, threadCount);
	}
	
	/**
	 * 
	 * @param mode
	 * @param queueCapacity
	 */
	public QueuingConfiguration(ResourceControlMode mode, int queueCapacity) {
		this.mode = mode;
		this.queueCapacity = queueCapacity;
	}

	public ResourceControlMode getMode() {
		return mode;
	}

	public int getQueueCapacity() {
		return queueCapacity;
	}
}
