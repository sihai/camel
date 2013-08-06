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

import com.opentech.camel.task.exception.ResourceLimitException;
import com.opentech.camel.task.lifecycle.LifeCycle;

/**
 * Resource controller of task domain
 * @author sihai
 *
 */
public interface TaskDomainResourceController extends LifeCycle {
	
	/**
	 * Get resource configuration of this controller
	 * @return
	 */
	ResourceConfiguration getResourceConfiguration();
	
	/**
	 * Get resource of this controller
	 * @return
	 */
	TaskDomainResource getResource();
	
	/**
	 * acquire resource for one task<p>
	 * XXX why not return true or false instead of throw exception, use exception could describe which resource limit
	 * @return 
	 * @throws ResourceLimitException
	 */
	ResourceHolder acquire() throws ResourceLimitException;
	
	/**
	 * acquire resource of type for one task<p>
	 * XXX why not return true or false instead of throw exception, use exception could describe which resource limit
	 * @return 
	 * @throws ResourceLimitException
	 */
	ResourceHolder acquire(ResourceType type) throws ResourceLimitException;
	
	/**
	 * release resource for one task
	 * @param token
	 */
	void release(ResourceHolder token);
	
	/**
	 * Try to release resource of type
	 * @param token
	 * @param type
	 */
	void release(ResourceHolder token, ResourceType type);
}
