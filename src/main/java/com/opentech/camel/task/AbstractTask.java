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
 * 
 * @author sihai
 *
 */
public abstract class AbstractTask implements Task {

	private volatile Status status = Status.NEW;
	
	@Override
	public String getTaskDomain() {
		return null;
	}

	@Override
	public void before() {
		status = Status.RUNING;
	}

	@Override
	public void succeed() {
		status = Status.SUCCEED;
	}

	@Override
	public void failed(Throwable t) {
		status = Status.FAILED;
	}

	@Override
	public void timeout() {
		status = Status.TIMEOUT;
	}

	@Override
	public void after() {

	}

	@Override
	public long getTimeout() {
		return 0;
	}

	@Override
	public Status getStatus() {
		return status;
	}

}
