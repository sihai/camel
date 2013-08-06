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

import com.opentech.camel.task.queue.QueuingConfiguration;
import com.opentech.camel.task.threading.ThreadingConfiguration;

/**
 * <b>资源配置, 涉及如下配置:</b><p>
 * <table>
 * 		<tr>
 * 			<td>队列最大容量</td>
 * 			<td>默认DEFAULT_QUEUE_CAPACITY</td>
 * 		</tr>
 * 		<tr>
 * 			<td>线程模式</td>
 * 			<td>默认{@link com.opentech.camel.task.resource.ResourceControlMode}</td>
 * 		</tr>
 * </table>
 * 
 * @author sihai
 *
 */
public class ResourceConfiguration {
	
	public static final ThreadingConfiguration DEFAULT_MAX_THREADING_CONFIGURATION = new ThreadingConfiguration();
	
	public static final QueuingConfiguration DEFAULT_MAX_QUEUING_CONFIGURATION = new QueuingConfiguration();
	
	/**
	 * Threading Configuration
	 */
	private ThreadingConfiguration threadingConfiguration = DEFAULT_MAX_THREADING_CONFIGURATION;
	
	/**
	 * Queuing Configuration
	 */
	private QueuingConfiguration queuingConfiguration = DEFAULT_MAX_QUEUING_CONFIGURATION;

	/**
	 * 
	 */
	public ResourceConfiguration() {
		
	}
	
	/**
	 * 
	 * @param threadingConfiguration
	 * @param queuingConfiguration
	 */
	public ResourceConfiguration(ThreadingConfiguration threadingConfiguration, QueuingConfiguration queuingConfiguration) {
		this.threadingConfiguration = threadingConfiguration;
		this.queuingConfiguration = queuingConfiguration;
	}

	public ThreadingConfiguration getThreadingConfiguration() {
		return threadingConfiguration;
	}

	public void setThreadingConfiguration(
			ThreadingConfiguration threadingConfiguration) {
		this.threadingConfiguration = threadingConfiguration;
	}

	public QueuingConfiguration getQueuingConfiguration() {
		return queuingConfiguration;
	}

	public void setQueuingConfiguration(QueuingConfiguration queuingConfiguration) {
		this.queuingConfiguration = queuingConfiguration;
	}
	
	@Override
	public String toString() {
		return String.format("{threadingConfiguration: %s, queuingConfiguration: %s}", threadingConfiguration.toString(), queuingConfiguration.toString());
	}
}