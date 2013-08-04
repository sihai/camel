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

/**
 * Factory for watchdog
 * @author sihai
 *
 */
public class WatchdogFactory {

	/**
	 * Thread count used by watch dog
	 */
	private int threadCount = Watchdog.DEFAULT_WATCHDOG_THREAD_COUNT;
	
	/**
	 * Total capacity of all queue
	 */
	private int queueCapacity = Watchdog.DEFAULT_WATCHDOG_QUEUE_CAPACITY;
	
	/**
	 * Private
	 */
	private WatchdogFactory() {
		
	}
	
	/**
	 * 
	 * @return
	 */
	public static WatchdogFactory newIntance() {
		return new WatchdogFactory();
	}
	
	/**
	 * 
	 * @param threadCount
	 * @return
	 */
	public WatchdogFactory withThreadCount(int threadCount) {
		this.threadCount = threadCount;
		return this;
	}
	
	/**
	 * 
	 * @param queueCapacity
	 * @return
	 */
	public WatchdogFactory withQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
		return this;
	}
	
	/**
	 * 
	 * @return
	 */
	public Watchdog build() {
		Watchdog watchdog = new DefaultWatchdog(threadCount, queueCapacity);
		watchdog.initialize();
		return watchdog;
	}
}
