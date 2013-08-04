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

/**
 * 
 * @author sihai
 *
 */
public class TaskDomainResourceControllerFactory {

	/**
	 * Resource configuration of this controller.
	 */
	private ResourceConfiguration resourceConfiguration;
	
	/**
	 * Resource of this controller
	 */
	private TaskDomainResource resource;
	
	/**
	 * Private
	 */
	private TaskDomainResourceControllerFactory() {
		
	}
	
	/**
	 * 
	 * @return
	 */
	public static TaskDomainResourceControllerFactory newInstance() {
		return new TaskDomainResourceControllerFactory();
	}
	
	/**
	 * 
	 * @param resourceConfiguration
	 * @return
	 */
	public TaskDomainResourceControllerFactory withResourceConfiguration(ResourceConfiguration resourceConfiguration) {
		this.resourceConfiguration = resourceConfiguration;
		return this;
	}
	
	/**
	 * 
	 * @param resource
	 * @return
	 */
	public TaskDomainResourceControllerFactory withResource(TaskDomainResource resource) {
		this.resource = resource;
		return this;
	}
	
	/**
	 * 
	 * @return
	 */
	public TaskDomainResourceController build() {
		TaskDomainResourceController controller = new DefaultTaskDomainResourceController(resourceConfiguration, resource);
		controller.initialize();
		return controller;
	}
}
