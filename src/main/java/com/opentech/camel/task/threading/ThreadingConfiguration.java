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

/**
 * Configuration of threading
 * 
 * @author sihai
 *
 */
public class ThreadingConfiguration {

	public static final int DEFAULT_MAX_THREAD_COUNT = 32;
	
	/**
	 * Mode of threading control
	 */
	private ThreadingControlMode mode = ThreadingControlMode.MAX;
	
	/**
	 * Value of thread count (MAX, RESERVED)
	 */
	private int threadCount = DEFAULT_MAX_THREAD_COUNT;
	
	/**
	 * 
	 */
	public ThreadingConfiguration() {
		this(ThreadingControlMode.MAX, DEFAULT_MAX_THREAD_COUNT);
	}
	
	/**
	 * 
	 * @param threadCount
	 */
	public ThreadingConfiguration(int threadCount) {
		this(ThreadingControlMode.MAX, threadCount);
	}
	
	/**
	 * 
	 * @param mode
	 * @param threadCount
	 */
	public ThreadingConfiguration(ThreadingControlMode mode, int threadCount) {
		this.mode = mode;
		this.threadCount = threadCount;
	}

	public ThreadingControlMode getMode() {
		return mode;
	}

	public int getThreadCount() {
		return threadCount;
	}
}
