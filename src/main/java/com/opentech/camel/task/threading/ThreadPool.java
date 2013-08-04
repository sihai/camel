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
package com.opentech.camel.task.threading;

import java.util.concurrent.Future;

import com.opentech.camel.task.lifecycle.LifeCycle;


/**
 * 权重线程池
 * @author sihai
 *
 */
public interface ThreadPool extends LifeCycle {

	int DEFAULT_CORE_THREAD_COUNT = 2;
	
	int DEFAULT_MAX_THREAD_COUNT = 8;
	
	long DEFAULT_KEEP_ALIVE_TIME = 60;
	
	/**
	 * Return count of core thread
	 * @return
	 */
	int getCoreThreadCount();
	
	/**
	 * Return max thread count
	 * @return
	 */
	int getMaxThreadCount();
	
	/**
	 * Return keep alive time of idle thread, unit ms
	 * @return
	 */
	long getThreadKeepAliveTime();
	
	/**
	 * Execute one task
	 * @param runnable
	 */
	void execute(Runnable runnable);
	
	/**
	 * Execute one task and return cancellble future
	 * @param runnable
	 * @return 
	 */
	Future submit(Runnable runnable);
}
