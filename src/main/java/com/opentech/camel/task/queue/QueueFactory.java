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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.opentech.camel.task.executor.WrapedTask;

/**
 * 
 * @author sihai
 *
 */
public class QueueFactory {

	public static final int DEFAULT_CAPACITY = 1024;
	
	/**
	 * 
	 */
	private int capacity = DEFAULT_CAPACITY;
	
	/**
	 * 
	 */
	private QueueMode mode = QueueMode.UN_THREAD_SAFE;
	
	/**
	 * Private
	 */
	private QueueFactory() {
		
	}
	
	/**
	 * 
	 * @return
	 */
	public static QueueFactory newInstance() {
		return new QueueFactory();
	}
	
	/**
	 * 
	 * @param capacity
	 * @return
	 */
	public QueueFactory withCapacity(int capacity) {
		this.capacity = capacity;
		return this;
	}
	
	/**
	 * 
	 * @param mode
	 * @return
	 */
	public QueueFactory withMode(QueueMode mode) {
		this.mode = mode;
		return this;
	}
	
	/**
	 * 
	 * @return
	 */
	public BlockingQueue<WrapedTask> build() {
		if(QueueMode.THREAD_SAFE == mode) {
			return new ArrayBlockingQueue<WrapedTask>(capacity);
		} else if(QueueMode.UN_THREAD_SAFE == mode) {
			throw new IllegalArgumentException("Not supported un thread safe queue at now");
		} else {
			throw new IllegalArgumentException(String.format("Unknown queue mode:%s", mode));
		}
	}
}
