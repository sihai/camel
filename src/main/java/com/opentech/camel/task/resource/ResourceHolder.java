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

import com.opentech.camel.task.TaskDomainRuntime;

/**
 * Resource holder, only hold one type resource one time
 * @author sihai
 *
 */
public class ResourceHolder {
	
	/**
	 * Type of resource
	 */
	private ResourceType type;
	
	/**
	 * 
	 */
	private TaskDomainRuntime runtime;
	
	/**
	 * 
	 * @param type
	 */
	public ResourceHolder(ResourceType type) {
		this(type, null);
	}
	
	/**
	 * 
	 * @param type
	 * @param runtime
	 */
	public ResourceHolder(ResourceType type, TaskDomainRuntime runtime) {
		this.type = type;
		this.runtime = runtime;
	}
	
	/**
	 * 
	 * @return
	 */
	public ResourceType getType() {
		return type;
	}
	
	/**
	 * 
	 * @param type
	 */
	public void setType(ResourceType type) {
		this.type = type;
	}

	/**
	 * 
	 * @return
	 */
	public TaskDomainRuntime getRuntime() {
		return runtime;
	}
	
	/**
	 * 
	 * @param runtime
	 */
	public void setRuntime(TaskDomainRuntime runtime) {
		this.runtime = runtime;
	}
	
	@Override
	public String toString() {
		return String.format("{type: %s, runtime: %s}", type, runtime.getTaskDomain().getName());
	}
}
