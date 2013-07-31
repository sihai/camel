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

import java.util.concurrent.Delayed;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * Watched by watchdog
 * @author sihai
 *
 */
public class WatchedTask implements Delayed {

	/**
	 * Cancellable task
	 */
	private FutureTask future;
	
	/**
	 * End timestamp of this task
	 */
	private long endTime;
	
	/**
	 * 
	 * @param future
	 * @param endTime
	 */
	public WatchedTask(FutureTask future, long endTime) {
		this.future = future;
		this.endTime = endTime;
	}
	
	@Override
	public int compareTo(Delayed o) {
		if(this == o) {
			return 0;
		}
		return (int)(getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(endTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}

}
