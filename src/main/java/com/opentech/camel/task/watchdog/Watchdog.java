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
package com.opentech.camel.task.watchdog;

import com.opentech.camel.task.lifecycle.LifeCycle;

/**
 * 
 * @author sihai
 *
 */
public interface Watchdog extends LifeCycle {
	
	int DEFAULT_WATCHDOG_THREAD_COUNT = 1;
	
	int DEFAULT_WATCHDOG_QUEUE_CAPACITY = 1024;
	
	/**
	 * 
	 * @param wt
	 */
	void watch(WatchedTask wt);
	
	/**
	 * 
	 * @param wt
	 */
	void remove(WatchedTask wt);
}
