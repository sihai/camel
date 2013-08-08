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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.opentech.camel.task.Task;
import com.opentech.camel.task.resource.ResourceHolder;

/**
 * 
 * @author sihai
 *
 */
public abstract class WrapedTask implements Runnable {

	private static final Log logger = LogFactory.getLog(WrapedTask.class);
	
	/**
	 * 
	 */
	private Task task;
	
	/**
	 * 
	 */
	private ResourceHolder holder;
	
	/**
	 * 
	 * @param task
	 * @param holder
	 */
	public WrapedTask(Task task, ResourceHolder holder) {
		this.task = task;
		this.holder = holder;
	}
	
	/*@Override
	public void run() {
		try {
			// 
			before();
			
			// before
			task.before();
			// execute
			task.execute();
			// succeed
			task.succeed();
		} catch (Throwable t) {
			// failed
			task.failed(t);
		} finally {
			//
			try {
				after();
			} catch (Throwable t) {
				logger.error("Execute WrapedTask.after failed", t);
			}
			// completed, succeed or failed (timeout situation will deal with in watchdog)
			task.after();
		}
	}*/
	
	public Task getTask() {
		return task;
	}

	public ResourceHolder getResourceHolder() {
		return holder;
	}

	public void setResourceHolder(ResourceHolder holder) {
		this.holder = holder;
	}
}
