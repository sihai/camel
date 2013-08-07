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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.opentech.camel.task.executor.WrapedTask;
import com.opentech.camel.task.lifecycle.AbstractLifeCycle;
import com.opentech.camel.task.resource.ResourceType;

/**
 * 
 * @author sihai
 *
 */
public class DefaultThreadPool extends AbstractLifeCycle implements ThreadPool {

	private static final Log logger = LogFactory.getLog(DefaultThreadPool.class);
	
	/**
	 * 
	 */
	private int coreThreadCount;
	
	/**
	 * 
	 */
	private int maxThreadCount;
	
	/**
	 * 
	 */
	private long keepAliveTime;
	
	/**
	 * 
	 */
	private ThreadFactory threadFactory;
	
	/**
	 * 
	 */
	private ThreadPoolExecutor innerThreadPool;
	
	/**
	 * 
	 */
	private ConcurrentHashMap<String, WrapedTask> pengingReleaseThreadRequest;
	
	/**
	 * XXX
	 */
	private BlockingQueue workqueue = new LinkedBlockingQueue() {

		@Override
		public Object take() throws InterruptedException {
			release();
			return super.take();
		}

		@Override
		public Object poll(long timeout, TimeUnit unit) throws InterruptedException {
			release();
			return super.poll(timeout, unit);
		}
		
		/**
		 * 
		 */
		private void release() {
			logger.debug(String
					.format("innerThreadPool: {corePoolSize:%d, maximumPoolSize:%d, poolSize:%d, activeCount:%d, queuedCount:%d}",
							innerThreadPool.getCorePoolSize(),
							innerThreadPool.getMaximumPoolSize(),
							innerThreadPool.getPoolSize(),
							innerThreadPool.getActiveCount(),
							innerThreadPool.getQueue().size()));
			WrapedTask wt = pengingReleaseThreadRequest.remove(Thread.currentThread().getName());
			if(null != wt) {
				wt.getResourceHolder().getRuntime().release(wt.getResourceHolder(), ResourceType.THREAD);
			}
		}
		
	};
	
	/**
	 * 
	 * @param coreThreadCount
	 * @param maxThreadCount
	 * @param keepAliveTime
	 * @param workQueue
	 * @param threadFactory
	 */
	public DefaultThreadPool(int coreThreadCount, int maxThreadCount, long keepAliveTime, ThreadFactory threadFactory) {
		this.coreThreadCount = coreThreadCount;
		this.maxThreadCount = maxThreadCount;
		this.keepAliveTime = keepAliveTime;
		this.threadFactory = threadFactory;
	}
	
	@Override
	public void initialize() {
		super.initialize();
		pengingReleaseThreadRequest = new ConcurrentHashMap<String, WrapedTask>();
		innerThreadPool = new ThreadPoolExecutor(coreThreadCount, maxThreadCount, keepAliveTime, TimeUnit.MILLISECONDS, workqueue, threadFactory) {

			@Override
			protected void beforeExecute(Thread t, Runnable r) {
				super.beforeExecute(t, r);
				if(r instanceof WrapedTask) {
					WrapedTask wt = ((WrapedTask)r);
					//XXX NO NEED
					//wt.getToken().getRuntime().release(wt.getToken(), ResourceType.QUEUE);
				}
			}

			@Override
			protected void afterExecute(Runnable r, Throwable t) {
				super.afterExecute(r, t);
				if(r instanceof WrapedTask) {
					WrapedTask wt = ((WrapedTask)r);
					if(null != pengingReleaseThreadRequest.putIfAbsent(Thread.currentThread().getName(), wt)) {
						throw new IllegalStateException("OMG Bug");
					}
					// XXX Release thread resource here, not safe, because of thread not really released here
					//wt.getResourceHolder().getRuntime().release(wt.getResourceHolder(), ResourceType.THREAD);
				}
			}
			
			
		};
	}

	@Override
	public void shutdown() {
		super.shutdown();
		innerThreadPool.shutdown();
		workqueue.clear();
		pengingReleaseThreadRequest.clear();
	}

	@Override
	public int getCoreThreadCount() {
		return coreThreadCount;
	}

	@Override
	public int getMaxThreadCount() {
		return maxThreadCount;
	}

	@Override
	public long getThreadKeepAliveTime() {
		return keepAliveTime;
	}

	@Override
	public void execute(Runnable runnable) {
		// XXX
		while(!_execute(runnable)) {
			// XXX
			//Thread.yield();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		}
	}
	
	/**
	 * 
	 * @param runnable
	 * @return
	 */
	private boolean _execute(Runnable runnable) {
		try {
			innerThreadPool.execute(runnable);
			return true;
		} catch (RejectedExecutionException e) {
			logger.error("DefaultThreadPool RejectedExecutionException", e);
			//System.exit(1);
			return false;
		}
	}

	@Override
	public Future submit(Runnable runnable) {
		// XXX
		Future f = null;
		while(null != (f = _submit(runnable))) {
			// XXX
			//Thread.yield();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				logger.error("DefaultThreadPool InterruptedException", e);
			}
		}
		return f;
	}
	
	/**
	 * 
	 * @param runnable
	 * @return
	 */
	private Future _submit(Runnable runnable) {
		try {
			return innerThreadPool.submit(runnable);
		} catch (RejectedExecutionException e) {
			logger.error("DefaultThreadPool RejectedExecutionException", e);
			//System.exit(1);
			return null;
		}
	}
}
