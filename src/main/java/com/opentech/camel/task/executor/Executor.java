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
package com.opentech.camel.task.executor;

import com.opentech.camel.task.Task;
import com.opentech.camel.task.threading.exception.ResourceLimitException;
import com.opentech.camel.task.threading.exception.TaskException;

/**
 * Executor of task
 * @author sihai
 *
 */
public interface Executor {
	
	long NONE_TIMEOUT = 0;
	
	/**
	 * Default forced timeout, unit ms
	 */
	long DEFAULT_FORCED_TIMEOUT = 60000;
	
	/**
	 * Execute task
	 * @param task
	 * @throws ResourceLimitException, TaskException
	 */
	void execute(final Task task) throws ResourceLimitException, TaskException;
}
