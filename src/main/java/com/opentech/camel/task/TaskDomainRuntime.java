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
	 * @param resourceController
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
		logger.info(String.format("try to initialize %s's runtime", taskDomain.getName()));
		initializeDispatcher();
		logger.info(String.format("%s's runtime initialize", taskDomain.getName()));
	}

	@Override
	public void shutdown() {
		logger.info(String.format("try to shutdown %s's runtime", taskDomain.getName()));
		resourceController.shutdown();
		shutdownDispatcher();
		logger.info(String.format("%s's runtime shutdowned", taskDomain.getName()));
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
	public ResourceHolder acquire() throws ResourceLimitException {
		ResourceHolder holder = null;
		logger.debug(String.format("Try to acquire one resource from runtime of task domain:%s", taskDomain.getName()));
		try {
			holder = resourceController.acquire();
			holder.setRuntime(this);
			return holder;
		} catch (ResourceLimitException e) {
			logger.debug(String.format("There is no resource of runtime of task domain:%s", taskDomain.getName()));
			if(null != defaultRuntime) {
				logger.debug(String.format("Try to acquire one resource from default runtime, original task domain:%s", taskDomain.getName()));
				holder = defaultRuntime.acquire();
				holder.setRuntime(defaultRuntime);
				return holder;
			} else {
				throw e;
			}
		}
	}
	
	@Override
	public ResourceHolder acquire(ResourceType type) throws ResourceLimitException {
		ResourceHolder holder = null;
		logger.debug(String.format("Try to acquire one resource type:%s from runtime of task domain:%s", type, taskDomain.getName()));
		try {
			holder = resourceController.acquire(type);
			holder.setRuntime(this);
			return holder;
		} catch (ResourceLimitException e) {
			logger.debug(String.format("There is no resource type:%s of runtime of task domain:%s", type, taskDomain.getName()));
			if(null != defaultRuntime) {
				logger.debug(String.format("Try to acquire one resource type:%s from default runtime, original task domain:%s", type, taskDomain.getName()));
				holder = defaultRuntime.acquire(type);
				holder.setRuntime(defaultRuntime);
				return holder;
			} else {
				throw e;
			}
		}
	}

	/**
	 * 
	 */
	public void release(ResourceHolder holder) {
		if(this == holder.getRuntime()) {
			resourceController.release(holder);
			if(ResourceType.THREAD == holder.getType()) {
				threadAvailable();
			}
		} else if(defaultRuntime == holder.getRuntime()) {
			defaultRuntime.release(holder);
		}
	}
	
	@Override
	public void release(ResourceHolder holder, ResourceType type) {
		if(this == holder.getRuntime()) {
			resourceController.release(holder, type);
			if(ResourceType.THREAD == type) {
				threadAvailable();
			}
		} else if(defaultRuntime == holder.getRuntime()) {
			defaultRuntime.release(holder, type);
		}
	}

	/**
	 * 
	 * @param holder
	 * @param type
	 */
	public void acquired(ResourceHolder holder, ResourceType type) {
		if(this == holder.getRuntime()) {
			resourceController.acquired(holder, type);
		} else if(defaultRuntime == holder.getRuntime()) {
			defaultRuntime.acquired(holder, type);
		}
	}
	
	//=========================================================
	//			Executor
	//=========================================================
	@Override
	public void execute(Task task) throws ResourceLimitException, TaskException {
		requestTasks.incrementAndGet();
		ResourceHolder holder = acquire();
		ResourceType type = holder.getType();
		if(ResourceType.THREAD == type) {
			_execute(task, holder);
		} else if(ResourceType.QUEUE == type) {
			_queue(task, holder);
		} else {
			throw new RuntimeException(String.format("Unknown resource type:%s", type));
		}
		totalTasks.incrementAndGet();
	}
	
	/**
	 * 
	 * @param task
	 * @param holder
	 */
	private void _execute(final Task task, ResourceHolder holder) {
		assert(null != holder.getRuntime());
		logger.debug(String.format("Try execute one task, task runtime:%s", holder.getRuntime().getTaskDomain().getName()));
		long timeout = getTaskTimeout(task);
		try {
			// new runnable
			Runnable runnable = new WrapedTask(task, holder) {

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
			if(Executor.NONE_TIMEOUT == timeout) {
				// XXX
				threadpool.execute(runnable);
			} else {
				// XXX
				watchdog.watch(new WatchedTask(threadpool.submit(runnable), task, System.currentTimeMillis() + timeout));
			}
		} finally {
		}
	}
	
	/**
	 * 
	 * @param task
	 * @param holder
	 * @throws TaskException
	 */
	private void _queue(Task task, ResourceHolder holder) throws TaskException {
		assert(null != holder.getRuntime());
		logger.debug(String.format("Try queue one task, task runtime:%s", holder.getRuntime().getTaskDomain().getName()));
		try {
			holder.getRuntime().getResource().getQueue().put(task);
		} catch (InterruptedException e) {
			release(holder, ResourceType.QUEUE);
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
				Task task = null;
				ResourceHolder holder = null;
				logger.info(String.format("%s's runtime's dispacher started", taskDomain.getName()));
				while(!Thread.currentThread().isInterrupted()) {
					try {
						task = getResource().getQueue().take();
						while(null == (holder = takeThread())) {
							threadAvailableLock.lock();
							threadAvailableCondition.await();
						}
						logger.info("Try to dispatch one task");
						_execute(task ,holder);
					} catch (InterruptedException e) {
						logger.error(String.format("%s interrupted", DISPATCHER_THREAD_NAME), e);
					} catch (Throwable t) {
						logger.error("Disptache one task failed", t);
					} finally {
						// XXX
					}
				}
				logger.info(String.format("%s's runtime's dispacher stoped", taskDomain.getName()));
			}
			
		});
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
