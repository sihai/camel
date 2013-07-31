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

/**
 * Task, executed by {@link com.opentech.camel.task.threading.ThreadPool}
 * @author sihai
 *
 */
public interface Task {

	/**
	 * Get the domain of this task
	 * @return
	 */
	String getTaskDomain();
	
	/**
	 * Call before call execute
	 */
	void before();
	
	/**
	 * Execute task
	 */
	void execute();
	
	/**
	 * Call when executed succeed
	 */
	void succeed();
	
	/**
	 * Call when executed failed
	 * @param t
	 */
	void failed(Throwable t);
	
	/**
	 * Call when timeout
	 */
	void timeout();

	/**
	 * Call when completed
	 */
	void after();
	
	/**
	 * Get the timeout of this task
	 *
	 * @return
	 * 		0 - never timeout
	 * 		> 0 timeout of this task, unit ms
	 */
	long getTimeout();
	
	/**
	 * Get status of this task
	 * @return
	 */
	Status getStatus();
}
