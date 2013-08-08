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

import java.util.concurrent.DelayQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.opentech.camel.task.Status;
import com.opentech.camel.task.lifecycle.AbstractLifeCycle;

/**
 * Default implement of watchdog
 * 
 * TODO support multithread and control queue capacity
 * @author sihai
 *
 */
public class DefaultWatchdog extends AbstractLifeCycle implements Watchdog {

	private static final Log logger = LogFactory.getLog(DefaultWatchdog.class);
	
	/**
	 * Queue for watchdog
	 */
	private DelayQueue<WatchedTask> watchdogQueue;
	
	/**
	 * Thread for watchdog
	 */
	private Thread watchdogThread;
	
	/**
	 * 
	 * @param threadCount
	 * @param queueCapacity
	 */
	public DefaultWatchdog(int threadCount, int queueCapacity) {
		// TODO
	}
	
	@Override
	public void initialize() {
		super.initialize();
		watchdogQueue = new DelayQueue<WatchedTask>();
		watchdogThread = new Thread(new Runnable() {

			@Override
			public void run() {
				WatchedTask wt = null;
				while(Thread.currentThread().isInterrupted()) {
					try {
						wt = null;
						// TODO
						wt = watchdogQueue.take();
						// XXX
						if(Status.RUNING == wt.getWt().getTask().getStatus()) {
							wt.getFuture().cancel(true);
							wt.getWt().getTask().timeout();
							wt.getWt().getTask().after();
						}
					} catch (InterruptedException e) {
						logger.error("Watchdog Interrupted", e);
						Thread.currentThread().interrupt();
					} catch (Throwable t) {
						logger.error("Watchdog Error", t);
					} finally {
						// 释放资源
						if(null != wt) {
							// XXX 貌似这里释放资源有问题
							wt.getWt().getResourceHolder().release();
						}
					}
				}
			}
			
		});
		watchdogThread.start();
	}

	@Override
	public void shutdown() {
		super.shutdown();
		watchdogThread.interrupt();
		watchdogQueue.clear();
	}

	@Override
	public void watch(WatchedTask wt) {
		watchdogQueue.put(wt);
	}

	@Override
	public void remove(WatchedTask wt) {
		watchdogQueue.remove(wt);
	}
}
