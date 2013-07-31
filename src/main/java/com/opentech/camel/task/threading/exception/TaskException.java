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
package com.opentech.camel.task.threading.exception;

/**
 * Root exception of task
 * @author sihai
 *
 */
public class TaskException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2340609296223380759L;

	/**
	 * 
	 * @param msg
	 * @param t
	 */
	public TaskException(String msg, Throwable t) {
		super(msg, t);
	}
	
	/**
	 * 
	 * @param msg
	 */
	public TaskException(String msg) {
		super(msg);
	}
	
	/**
	 * 
	 * @param t
	 */
	public TaskException(Throwable t) {
		super(t);
	}
}
