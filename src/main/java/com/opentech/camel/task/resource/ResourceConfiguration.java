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

import com.opentech.camel.task.threading.ThreadingConfiguration;
import com.opentech.camel.task.threading.ThreadingControlMode;

/**
 * <b>资源配置, 涉及如下配置:</b><p>
 * <table>
 * 		<tr>
 * 			<td>队列最大容量</td>
 * 			<td>默认DEFAULT_QUEUE_CAPACITY</td>
 * 		</tr>
 * 		<tr>
 * 			<td>线程模式</td>
 * 			<td>默认{@link com.opentech.camel.task.threading.ThreadingControlMode}</td>
 * 		</tr>
 * </table>
 * 
 * @author sihai
 *
 */
public class ResourceConfiguration {
	
	/**
	 * Default capacity of queue
	 */
	public static final int DEFAULT_QUEUE_CAPACITY = 1024;
	
	public static final ThreadingConfiguration DEFAULT_MAX_THREADING_CONFIGURATION = new ThreadingConfiguration();
	
	/**
	 * capacity of queue
	 */
	private int queueCapacity = DEFAULT_QUEUE_CAPACITY;
	
	/**
	 * Threading Configuration
	 */
	private ThreadingConfiguration threadingConfiguration = DEFAULT_MAX_THREADING_CONFIGURATION;

	public int getQueueCapacity() {
		return queueCapacity;
	}

	public void setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}

	public ThreadingConfiguration getThreadingConfiguration() {
		return threadingConfiguration;
	}

	public void setThreadingConfiguration(
			ThreadingConfiguration threadingConfiguration) {
		this.threadingConfiguration = threadingConfiguration;
	}
}