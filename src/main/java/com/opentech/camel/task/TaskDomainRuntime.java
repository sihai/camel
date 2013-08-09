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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.opentech.camel.task.exception.ResourceLimitException;
import com.opentech.camel.task.exception.TaskException;
import com.opentech.camel.task.executor.Executor;
import com.opentech.camel.task.executor.WrapedTask;
import com.opentech.camel.task.lifecycle.LifeCycle;
import com.opentech.camel.task.resource.ResourceConfiguration;
import com.opentech.camel.task.resource.ResourceControlMode;
import com.opentech.camel.task.resource.ResourceHolder;
import com.opentech.camel.task.resource.ResourceType;
import com.opentech.camel.task.resource.TaskDomainResource;
import com.opentech.camel.task.resource.TaskDomainResourceController;
import com.opentech.camel.task.threading.ThreadPool;
import com.opentech.camel.task.watchdog.Watchdog;
import com.opentech.camel.task.watchdog.WatchedTask;

/**
 * Runtime of task domain
 * @author sihai
 *
 */
public class TaskDomainRuntime implements LifeCycle, TaskDomainResourceController, Executor {

	public static final String DISPATCHER_THREAD_NAME = "Dispatcher-Thread";
	
	private static final Log logger = LogFactory.getLog(TaskDomainRuntime.class);
	
	/**
	 * Task domain of this runtime
	 */
	private TaskDomain taskDomain;
	
	/**
	 * Timeout of task domain level
	 */
	private volatile long timeout = Executor.NONE_TIMEOUT;
	
	/**
	 * Resource controller use by this task domain.
	 */
	private TaskDomainResourceController resourceController;
	
	/**
	 * Thread pool for execute task
	 */
	private ThreadPool threadpool;
	
	/**
	 * Total request count requestTasks equals totalTasks + rejectTasks
	 */
	private AtomicLong requestTasks;
	
	/**
	 * Total tasks, equals succeedTasks + failedTasks + executingTask + timeoutTasks
	 */
	private AtomicLong totalTasks;
	
	/**
	 * Count of tasks succeed
	 */
	private AtomicLong succeedTasks;
	
	/**
	 * Count of failed tasks
	 */
	private AtomicLong failedTasks;
	
	/**
	 * Count of tasks executing
	 */
	private AtomicLong executingTasks;
	
	/**
	 * Count of tasks timeout
	 */
	private AtomicLong timeoutTasks;
	
	/**
	 * Default runtime
	 */
	private TaskDomainRuntime defaultRuntime;
	
	/**
	 * Watchdog for timeout
	 */
	private Watchdog watchdog;

	/**
	 * Thread for dispatcher
	 */
	private Thread dispatcherThread;
	
	//===========================================================================
	//				
	//===========================================================================
	/**
	 * 
	 */
	private Lock threadAvailableLock;
	
	/**
	 * 
	 */
	private Condition threadAvailableCondition;
	
	/**
	 * 
	 * @param taskDomain
	 * @param resourceControllera
	 */
	public TaskDomainRuntime(TaskDomain taskDomain, TaskDomainResourceController resourceController) {
		this(taskDomain, Executor.NONE_TIMEOUT, resourceController);
	}
	
	/**
	 * 
	 * @param taskDomain
	 * @param timeout
	 * @param resourceController
	 */
	public TaskDomainRuntime(TaskDomain taskDomain, long timeout, TaskDomainResourceController resourceController) {
		this(taskDomain, Executor.NONE_TIMEOUT, resourceController, null, null);
	}
	
	/**
	 * 
	 * @param taskDomain
	 * @param timeout
	 * @param resourceController
	 * @param defaultRuntime
	 * @param watchdog
	 */
	public TaskDomainRuntime(TaskDomain taskDomain, long timeout, TaskDomainResourceController resourceController, TaskDomainRuntime defaultRuntime, Watchdog watchdog) {
		this.taskDomain = taskDomain;
		this.timeout = timeout;
		this.resourceController = resourceController;
		this.defaultRuntime = defaultRuntime;
		this.watchdog = watchdog;
		requestTasks = new AtomicLong(0L);
		totalTasks = new AtomicLong(0L);
		succeedTasks = new AtomicLong(0L);
		failedTasks = new AtomicLong(0L);
		executingTasks = new AtomicLong(0L);
		timeoutTasks = new AtomicLong(0L);
	}
	
	//=========================================================
	//			Life cycle
	//=========================================================
	@Override
	public void initialize() {
		logger.info(String.format("try to initialize task domain runtime: %s", taskDomain.getName()));
		initializeDispatcher();
		logger.info(String.format("task domain runtime: %s resourceController: %s", taskDomain.getName(), resourceController.toString()));
		logger.info(String.format("task domain runtime: %s initialized", taskDomain.getName()));
	}

	@Override
	public void shutdown() {
		logger.info(String.format("try to shutdown task domain runtime: %s", taskDomain.getName()));
		logger.info(String.format("task domain runtime: %s resourceController: %s", taskDomain.getName(), resourceController.toString()));
		resourceController.shutdown();
		shutdownDispatcher();
		logger.info(String.format("task domain runtime: %s shutdowned", taskDomain.getName()));
	}
	
	//=========================================================
	//			TaskDomainResourceController
	//=========================================================
	
	@Override
	public ResourceConfiguration getResourceConfiguration() {
		return resourceController.getResourceConfiguration();
	}
	
	@Override
	public TaskDomainResource getResource() {
		return resourceController.getResource();
	}

	/**
	 * 
	 */
	@Override
	public ResourceHolder acquire() throws ResourceLimitException {
		ResourceHolder holder = null;
		if(logger.isDebugEnabled()) {
			logger.debug(String.format("Try to acquire one resource from runtime of task domain:%s", taskDomain.getName()));
		}
		try {
			holder = resourceController.acquire();
			holder.setRuntime(this);
			return holder;
		} catch (ResourceLimitException e) {
			if(logger.isDebugEnabled()) {
				logger.debug(String.format("There is no resource of runtime of task domain:%s", taskDomain.getName()));
			}
			holder = tryBorrowFromDefaultRuntime();
			holder.setRuntime(defaultRuntime);
			return holder;
		}
	}
	
	@Override
	public ResourceHolder acquire(ResourceType type) throws ResourceLimitException {
		ResourceHolder holder = null;
		if(logger.isDebugEnabled()) {
			logger.debug(String.format("Try to acquire one resource type:%s from runtime of task domain:%s", type, taskDomain.getName()));
		}
		try {
			holder = resourceController.acquire(type);
			holder.setRuntime(this);
			return holder;
		} catch (ResourceLimitException e) {
			if(logger.isDebugEnabled()) {
				logger.debug(String.format("There is no resource type:%s of runtime of task domain:%s", type, taskDomain.getName()));
			}
			holder = this.tryBorrowFromDefaultRuntime(type);
			holder.setRuntime(defaultRuntime);
			if(logger.isDebugEnabled()) {
				logger.debug(String.format("Brrow one resuource:%s of runtime of task domain:%s", holder, defaultRuntime.getTaskDomain().getName()));
			}
			return holder;
		}
	}

	/**
	 * 
	 */
	public void release(ResourceHolder holder) {
		if(logger.isDebugEnabled()) {
			logger.debug(String.format("Release resource:%s", holder));
		}
		if(this == holder.getRuntime()) {
			resourceController.release(holder);
			if(ResourceType.THREAD == holder.getType()) {
				threadAvailable();
			}
		} else if(defaultRuntime == holder.getRuntime()) {
			defaultRuntime.release(holder);
		} else {
			throw new IllegalStateException(String.format("null of ResourceHolder.runtime, runtime:%s", holder));
		}
	}

	//=========================================================
	//			Executor
	//=========================================================
	@Override
	public void execute(final Task task) throws ResourceLimitException, TaskException {
		requestTasks.incrementAndGet();
		ResourceHolder holder = acquire();
		if(logger.isDebugEnabled()) {
			logger.debug(String.format("acquired resource:%s", holder.toString()));
		}
		ResourceType type = holder.getType();
		
		// new runnable
		WrapedTask wt = new WrapedTask(task, holder) {

			@Override
			public void run() {
				try {
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
					// completed, succeed or failed (timeout situation will deal with in watchdog)
					task.after();
				}
			}

		};
					
		if(ResourceType.THREAD == type) {
			_execute(wt);
		} else if(ResourceType.QUEUE == type) {
			_queue(wt);
		} else {
			throw new RuntimeException(String.format("Unknown resource type:%s", type));
		}
		totalTasks.incrementAndGet();
	}
	
	@Override
	public void release(String taskDomainName) {
		// TODO Auto-generated method stub
	}

	/**
	 * 
	 * @return
	 * @throws ResourceLimitException
	 */
	private ResourceHolder tryBorrowFromDefaultRuntime() throws ResourceLimitException {
		// Try borrow thread
		try {
			return tryBorrowFromDefaultRuntime(ResourceType.THREAD);
		} catch (ResourceLimitException e) {
			// Try borrow queue
			return tryBorrowFromDefaultRuntime(ResourceType.QUEUE);
		}
	}
	
	/**
	 * 
	 * @param type
	 * @return
	 * @throws ResourceLimitException
	 */
	private ResourceHolder tryBorrowFromDefaultRuntime(ResourceType type) throws ResourceLimitException {
		if(null == defaultRuntime) {
			throw new ResourceLimitException("Default runtime can not borrow resource from others");
		}
		if(logger.isDebugEnabled()) {
			logger.debug(String.format("Try to acquire one resource type:%s from default runtime, original task domain:%s", type, taskDomain.getName()));
		}
		ResourceControlMode mode = null;
		if(ResourceType.THREAD == type) {
			mode = taskDomain.getResourceConfiguration().getThreadingConfiguration().getMode();
		} else if (ResourceType.QUEUE == type) {
			mode = taskDomain.getResourceConfiguration().getQueuingConfiguration().getMode();
		}
		
		if(ResourceControlMode.RESERVED == mode) {
			return defaultRuntime.acquire(type);
		}
		throw new ResourceLimitException(String.format("Task domain runtime can not borrow from default runtime, because of this is not RESERVED"));
	}
	
	/**
	 * 
	 * @param wt
	 */
	private void _execute(final WrapedTask wt) {
		assert(null != wt.getResourceHolder().getRuntime());
		logger.debug(String.format("Try execute one task, task runtime:%s", wt.getResourceHolder().getRuntime().getTaskDomain().getName()));
		long timeout = getTaskTimeout(wt.getTask());
		try {
			if(Executor.NONE_TIMEOUT == timeout) {
				// XXX
				threadpool.execute(wt);
			} else {
				// XXX
				watchdog.watch(new WatchedTask(threadpool.submit(wt), wt, System.currentTimeMillis() + timeout));
			}
		} finally {
		}
	}
	
	/**
	 * 
	 * @param wt
	 * @throws TaskException
	 */
	private void _queue(WrapedTask wt) throws TaskException {
		assert(null != wt.getResourceHolder().getRuntime());
		logger.debug(String.format("Try queue one task, task runtime:%s", wt.getResourceHolder().getRuntime().getTaskDomain().getName()));
		try {
			wt.getResourceHolder().getRuntime().getResource().getQueue().put(wt);
		} catch (InterruptedException e) {
			wt.getResourceHolder().release();
			Thread.currentThread().interrupt();
			throw new TaskException("OMG, not possible, should never be interrupted");
		}
	}
	
	/**
	 * 
	 * @param task
	 * @return
	 */
	private long getTaskTimeout(Task task) {
		long timeout = task.getTimeout();
		if(0 == timeout) {
			timeout = getTimeout();
		}
		if(0 == timeout) {
			//timeout = this.forcedTimeout;
		}
		return timeout;
	}
	
	/**
	 * 
	 */
	private void threadAvailable() {
		try {
			threadAvailableLock.lock();
			threadAvailableCondition.signalAll();
		} finally {
			threadAvailableLock.unlock();
		}
	}
	
	/**
	 * 
	 * @return
	 */
	private ResourceHolder takeThread() {
		try {
			return acquire(ResourceType.THREAD);
		} catch (ResourceLimitException e) {
			logger.error("Take thread filead", e);
			return null;
		}
	}
	
	/**
	 * 
	 */
	private void initializeDispatcher() {
		threadAvailableLock = new ReentrantLock();
		threadAvailableCondition = threadAvailableLock.newCondition();
		dispatcherThread = new Thread(new Runnable() {

			@Override
			public void run() {
				WrapedTask wt = null;
				ResourceHolder holder = null;
				logger.info(String.format("%s's runtime's dispacher started", taskDomain.getName()));
				while(!Thread.currentThread().isInterrupted()) {
					try {
						wt = getResource().getQueue().take();
						// XXX
						wt.getResourceHolder().release();
						while(null == (holder = takeThread())) {
							try {
								threadAvailableLock.lock();
								threadAvailableCondition.await(1000, TimeUnit.MILLISECONDS);
							} finally {
								threadAvailableLock.unlock();
							}
						}
						logger.debug(String.format("%s acquired one resource:%s", Thread.currentThread().getName(), holder));
						logger.debug(String.format("%s try to execute one task", Thread.currentThread().getName()));
						wt.setResourceHolder(holder);
						_execute(wt);
					} catch (InterruptedException e) {
						logger.error(String.format("%s interrupted", Thread.currentThread().getName()), e);
					} catch (Throwable t) {
						logger.error("Disptach one task failed", t);
					} finally {
						// XXX
					}
				}
				logger.info(String.format("%s's runtime's dispacher stoped", taskDomain.getName()));
			}
			
		}, String.format("Task-Domain-Runtime-%s-Dispatcher-Thread", taskDomain.getName()));
		dispatcherThread.start();
	}
	
	/**
	 * 
	 */
	private void shutdownDispatcher() {
		dispatcherThread.interrupt();
	}

	public TaskDomain getTaskDomain() {
		return taskDomain;
	}
	
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public long getTimeout() {
		return timeout;
	}
	
	public void setThreadpool(ThreadPool threadpool) {
		this.threadpool = threadpool;
	}
	
	public void setWatchdog(Watchdog watchdog) {
		this.watchdog = watchdog;
	}

	public long getTotalTasks() {
		return totalTasks.get();
	}

	public long getSucceedTasks() {
		return succeedTasks.get();
	}

	public long getFailedTasks() {
		return failedTasks.get();
	}

	public long getExecutingTasks() {
		return executingTasks.get();
	}

	public long getTimeoutTasks() {
		return timeoutTasks.get();
	}
	
	public void setDefaultRuntime(TaskDomainRuntime defaultRuntime) {
		this.defaultRuntime = defaultRuntime;
	}
}
