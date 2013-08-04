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
import com.opentech.camel.task.resource.ResourceConfiguration;

/**
 * 
 * @author sihai
 *
 */
public class TaskDomain {

	public static final String DEFAULT_TASK_DOMAIN_NAME = "_DEFAULT_";
	
	/**
	 * Name of this domain
	 */
	private String name;
	
	/**
	 * Timout of this domain
	 */
	private long timeout = Executor.NONE_TIMEOUT;
	
	/**
	 * Resource configuration of this domain
	 */
	private ResourceConfiguration resourceConfiguration;
	
	//================================================================
	//		Runtime info
	//================================================================
	
	/**
	 * Default
	 */
	public TaskDomain() {
		
	}
	
	/**
	 * 
	 * @param name
	 * @param resourceConfiguration
	 */
	public TaskDomain(String name, ResourceConfiguration resourceConfiguration) {
		this(name, Executor.NONE_TIMEOUT, resourceConfiguration);
	}

	
	/**
	 * 
	 * @param name
	 * @param timeout
	 * @param resourceConfiguration
	 */
	public TaskDomain(String name, long timeout, ResourceConfiguration resourceConfiguration) {
		this.name = name;
		this.resourceConfiguration = resourceConfiguration;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public long getTimeout() {
		return timeout;
	}


	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}


	public ResourceConfiguration getResourceConfiguration() {
		return resourceConfiguration;
	}


	public void setResourceConfiguration(ResourceConfiguration resourceConfiguration) {
		this.resourceConfiguration = resourceConfiguration;
	}
}
