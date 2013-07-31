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

import com.opentech.camel.task.threading.exception.ResourceLimitException;

/**
 * Default resource controller of task domain
 * @author sihai
 *
 */
public class DefaultTaskDomainResourceController implements TaskDomainResourceController {

	/**
	 * Resource configuration of this task domain.
	 */
	private ResourceConfiguration resourceConfiguration;
	
	/**
	 * 
	 * @param resourceConfiguration
	 */
	public DefaultTaskDomainResourceController(ResourceConfiguration resourceConfiguration) {
		this.resourceConfiguration = resourceConfiguration;
	}
	
	@Override
	public void acquire() throws ResourceLimitException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		
	}

}
