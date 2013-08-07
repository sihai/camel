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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.opentech.camel.task.exception.ResourceLimitException;
import com.opentech.camel.task.executor.Executor;
import com.opentech.camel.task.queue.QueuingConfiguration;
import com.opentech.camel.task.resource.ResourceConfiguration;
import com.opentech.camel.task.resource.ResourceControlMode;
import com.opentech.camel.task.threading.ThreadingConfiguration;

/**
 * 
 * @author sihai
 *
 */
public class BootstrapTest {
	
	private static final Log logger = LogFactory.getLog(BootstrapTest.class);
	
	@Before
	public void before() {
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread t, Throwable e) {
				System.out.println(String.format("Exception Thread:%s", t.getName()));
				e.printStackTrace();
			}
			
		});
	}
	
	@After
	public void after() {
		
	}

	@Test
	public void testOnlyDefaultDomain() throws Exception {
		Bootstrap bootstrap = new Bootstrap();
		Executor executor = bootstrap.bootstrap();
		executor.execute(new AbstractTask() {

			@Override
			public void execute() {
				System.out.println("Task executed");
			}
			
		});
		Thread.sleep(10000);
		executor.shutdown();
	}
	
	@Test
	public void testDomainMax() throws Exception {
		ThreadingConfiguration threadingConfiguration = new ThreadingConfiguration(ResourceControlMode.MAX, 1);
		QueuingConfiguration queuingConfiguration = new QueuingConfiguration(ResourceControlMode.MAX, 1);
		ResourceConfiguration configuration = new ResourceConfiguration();
		configuration.setQueuingConfiguration(queuingConfiguration);
		configuration.setThreadingConfiguration(threadingConfiguration);
		TaskDomain domain = new TaskDomain("test", configuration);
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.setCoreThreadCount(2);
		bootstrap.setMaxThreadCount(2);
		bootstrap.setQueueCapacity(2);
		bootstrap.register(domain);
		
		Executor executor = bootstrap.bootstrap();
		executor.execute(createTask(domain.getName(), "0", 10000));
		executor.execute(createTask(domain.getName(), "1", 10000));
		executor.execute(createTask(domain.getName(), "2", 10000));
		Thread.sleep(100000);
		executor.shutdown();
	}
	
	@Test(expected = ResourceLimitException.class)
	public void testDomainMax_ResourceLimitException() throws Exception {
		ThreadingConfiguration threadingConfiguration = new ThreadingConfiguration(ResourceControlMode.MAX, 1);
		QueuingConfiguration queuingConfiguration = new QueuingConfiguration(ResourceControlMode.MAX, 1);
		ResourceConfiguration configuration = new ResourceConfiguration();
		configuration.setQueuingConfiguration(queuingConfiguration);
		configuration.setThreadingConfiguration(threadingConfiguration);
		TaskDomain domain = new TaskDomain("test", configuration);
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.setCoreThreadCount(2);
		bootstrap.setMaxThreadCount(2);
		bootstrap.setQueueCapacity(2);
		bootstrap.register(domain);
		
		Executor executor = bootstrap.bootstrap();
		executor.execute(createTask(domain.getName(), "0", 10000));
		executor.execute(createTask(domain.getName(), "1", 10000));
		executor.execute(createTask(domain.getName(), "2", 10000));
		executor.execute(createTask(domain.getName(), "3", 10000));
		executor.execute(createTask(domain.getName(), "4", 10000));
		executor.execute(createTask(domain.getName(), "5", 10000));
		Thread.sleep(100000);
		executor.shutdown();
	}
	
	
	@Test
	public void testDomainThreadReservedQueueMax() throws Exception {
		ThreadingConfiguration threadingConfiguration = new ThreadingConfiguration(ResourceControlMode.RESERVED, 1);
		QueuingConfiguration queuingConfiguration = new QueuingConfiguration(ResourceControlMode.MAX, 1);
		ResourceConfiguration configuration = new ResourceConfiguration();
		configuration.setQueuingConfiguration(queuingConfiguration);
		configuration.setThreadingConfiguration(threadingConfiguration);
		TaskDomain domain = new TaskDomain("test", configuration);
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.setCoreThreadCount(2);
		bootstrap.setMaxThreadCount(2);
		bootstrap.setQueueCapacity(2);
		bootstrap.register(domain);
		
		Executor executor = bootstrap.bootstrap();
		executor.execute(createTask(domain.getName(), "0", 10000));
		executor.execute(createTask(domain.getName(), "1", 10000));
		executor.execute(createTask(domain.getName(), "2", 10000));
		//executor.execute(createTask(domain.getName(), "3"));
		//executor.execute(createTask(domain.getName(), "4"));
		//executor.execute(createTask(domain.getName(), "5"));
		Thread.sleep(100000);
		executor.shutdown();
	}
	
	@Test(expected = ResourceLimitException.class)
	public void testDomainThreadReservedQueueMax_ResourceLimitException() throws Exception {
		ThreadingConfiguration threadingConfiguration = new ThreadingConfiguration(ResourceControlMode.RESERVED, 1);
		QueuingConfiguration queuingConfiguration = new QueuingConfiguration(ResourceControlMode.MAX, 1);
		ResourceConfiguration configuration = new ResourceConfiguration();
		configuration.setQueuingConfiguration(queuingConfiguration);
		configuration.setThreadingConfiguration(threadingConfiguration);
		TaskDomain domain = new TaskDomain("test", configuration);
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.setCoreThreadCount(2);
		bootstrap.setMaxThreadCount(2);
		bootstrap.setQueueCapacity(2);
		bootstrap.register(domain);
		
		Executor executor = bootstrap.bootstrap();
		executor.execute(createTask(domain.getName(), "0", 10000));
		executor.execute(createTask(domain.getName(), "1", 10000));
		executor.execute(createTask(domain.getName(), "2", 10000));
		executor.execute(createTask(domain.getName(), "3", 10000));
		//executor.execute(createTask(domain.getName(), "4"));
		//executor.execute(createTask(domain.getName(), "5"));
		Thread.sleep(100000);
		executor.shutdown();
	}
	
	@Test
	public void testDomainThreadReservedQueueReserved() throws Exception {
		ThreadingConfiguration threadingConfiguration = new ThreadingConfiguration(ResourceControlMode.RESERVED, 1);
		QueuingConfiguration queuingConfiguration = new QueuingConfiguration(ResourceControlMode.RESERVED, 1);
		ResourceConfiguration configuration = new ResourceConfiguration();
		configuration.setQueuingConfiguration(queuingConfiguration);
		configuration.setThreadingConfiguration(threadingConfiguration);
		TaskDomain domain = new TaskDomain("test", configuration);
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.setCoreThreadCount(2);
		bootstrap.setMaxThreadCount(2);
		bootstrap.setQueueCapacity(2);
		bootstrap.register(domain);
		
		Executor executor = bootstrap.bootstrap();
		executor.execute(createTask(domain.getName(), "0", 10000));
		executor.execute(createTask(domain.getName(), "1", 10000));
		executor.execute(createTask(domain.getName(), "2", 10000));
		executor.execute(createTask(domain.getName(), "3", 10000));
		//executor.execute(createTask(domain.getName(), "4"));
		//executor.execute(createTask(domain.getName(), "5"));
		Thread.sleep(100000);
		executor.shutdown();
	}
	
	@Test
	public void testDomainThreadReservedQueueReserved_ResourceLimitException() throws Exception {
		ThreadingConfiguration threadingConfiguration = new ThreadingConfiguration(ResourceControlMode.RESERVED, 1);
		QueuingConfiguration queuingConfiguration = new QueuingConfiguration(ResourceControlMode.RESERVED, 1);
		ResourceConfiguration configuration = new ResourceConfiguration();
		configuration.setQueuingConfiguration(queuingConfiguration);
		configuration.setThreadingConfiguration(threadingConfiguration);
		TaskDomain domain = new TaskDomain("test", configuration);
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.setCoreThreadCount(2);
		bootstrap.setMaxThreadCount(2);
		bootstrap.setQueueCapacity(2);
		bootstrap.register(domain);
		
		Executor executor = bootstrap.bootstrap();
		logger.debug("Try to execute task 0");
		executor.execute(createTask(domain.getName(), "0", 10000));
		logger.debug("Try to execute task 1");
		executor.execute(createTask(domain.getName(), "1", 10000));
		logger.debug("Try to execute task 2");
		executor.execute(createTask(domain.getName(), "2", 10000));
		logger.debug("Try to execute task 3");
		executor.execute(createTask(domain.getName(), "3", 10000));
		logger.debug("Try to execute task 4");
		executor.execute(createTask(domain.getName(), "4", 10000));
		logger.debug("End");
		//executor.execute(createTask(domain.getName(), "5"));
		Thread.sleep(100000);
		executor.shutdown();
	}
	
	@Test
	// 实测 TPS: 7.960661
	public void testDomainThreadReservedQueueReserved_TPS_8() throws Exception {
		ThreadingConfiguration threadingConfiguration = new ThreadingConfiguration(ResourceControlMode.RESERVED, 4);
		QueuingConfiguration queuingConfiguration = new QueuingConfiguration(ResourceControlMode.RESERVED, 32);
		ResourceConfiguration configuration = new ResourceConfiguration();
		configuration.setQueuingConfiguration(queuingConfiguration);
		configuration.setThreadingConfiguration(threadingConfiguration);
		TaskDomain domain = new TaskDomain("test", configuration);
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.setCoreThreadCount(8);
		bootstrap.setMaxThreadCount(8);
		bootstrap.setQueueCapacity(64);
		bootstrap.register(domain);
		
		Executor executor = bootstrap.bootstrap();
		
		int i = 600;
		CountDownLatch latch = new CountDownLatch(600 * 8);
		long start = System.currentTimeMillis();
		while(i-- > 0) {
			for(int j = 0; j < 8; j++) {
				executor.execute(createCounterTask(domain.getName(), String.format("%d-%d", i, j), 1000, latch));
				Thread.sleep(125);
			}
		}
		latch.await();
		System.out.println(String.format("TPS: %f", (600 * 8) / ((System.currentTimeMillis() - start) / (1000 + 0.0D))));
		executor.shutdown();
	}

	@Test
	// 实测 TPS: 15.360492
	public void testDomainThreadReservedQueueReserved_TPS_16() throws Exception {
		ThreadingConfiguration threadingConfiguration = new ThreadingConfiguration(ResourceControlMode.RESERVED, 8);
		QueuingConfiguration queuingConfiguration = new QueuingConfiguration(ResourceControlMode.RESERVED, 32);
		ResourceConfiguration configuration = new ResourceConfiguration();
		configuration.setQueuingConfiguration(queuingConfiguration);
		configuration.setThreadingConfiguration(threadingConfiguration);
		TaskDomain domain = new TaskDomain("test", configuration);
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.setCoreThreadCount(16);
		bootstrap.setMaxThreadCount(16);
		bootstrap.setQueueCapacity(64);
		bootstrap.register(domain);
		
		Executor executor = bootstrap.bootstrap();
		
		int i = 60;
		CountDownLatch latch = new CountDownLatch(60 * 16);
		long start = System.currentTimeMillis();
		while(i-- > 0) {
			for(int j = 0; j < 8; j++) {
				for(int n = 0; n < 2; n++) {
					executor.execute(createCounterTask(domain.getName(), String.format("%d-%d-%n", i, j, n), 1000, latch));
				}
				Thread.sleep(125);
			}
		}
		latch.await();
		System.out.println(String.format("TPS: %f", (60 * 16) / ((System.currentTimeMillis() - start) / (1000 + 0.0D))));
		executor.shutdown();
	}
	
	@Test
	// 实测 122.750376
	public void testDomainThreadReservedQueueReserved_TPS_128() throws Exception {
		ThreadingConfiguration threadingConfiguration = new ThreadingConfiguration(ResourceControlMode.RESERVED, 64);
		QueuingConfiguration queuingConfiguration = new QueuingConfiguration(ResourceControlMode.RESERVED, 256);
		ResourceConfiguration configuration = new ResourceConfiguration();
		configuration.setQueuingConfiguration(queuingConfiguration);
		configuration.setThreadingConfiguration(threadingConfiguration);
		TaskDomain domain = new TaskDomain("test", configuration);
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.setCoreThreadCount(128);
		bootstrap.setMaxThreadCount(128);
		bootstrap.setQueueCapacity(512);
		bootstrap.register(domain);
		
		Executor executor = bootstrap.bootstrap();
		
		int i = 60;
		CountDownLatch latch = new CountDownLatch(60 * 128);
		long start = System.currentTimeMillis();
		while(i-- > 0) {
			for(int j = 0; j < 8; j++) {
				for(int n = 0; n < 16; n++) {
					executor.execute(createCounterTask(domain.getName(), String.format("%d-%d-%n", i, j, n), 1000, latch));
				}
				Thread.sleep(125);
			}
		}
		latch.await();
		System.out.println(String.format("TPS: %f", (60 * 128) / ((System.currentTimeMillis() - start) / (1000 + 0.0D))));
		executor.shutdown();
	}
	
	@Test
	public void testDomainThreadReservedQueueReserved_TPS_128_ResourceLimitException_Send_Speed_Big_Then_128() throws Exception {
		ThreadingConfiguration threadingConfiguration = new ThreadingConfiguration(ResourceControlMode.RESERVED, 64);
		QueuingConfiguration queuingConfiguration = new QueuingConfiguration(ResourceControlMode.RESERVED, 256);
		ResourceConfiguration configuration = new ResourceConfiguration();
		configuration.setQueuingConfiguration(queuingConfiguration);
		configuration.setThreadingConfiguration(threadingConfiguration);
		TaskDomain domain = new TaskDomain("test", configuration);
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.setCoreThreadCount(128);
		bootstrap.setMaxThreadCount(128);
		bootstrap.setQueueCapacity(512);
		bootstrap.register(domain);
		
		Executor executor = bootstrap.bootstrap();
		
		int i = 60;
		CountDownLatch latch = new CountDownLatch(60 * 128);
		long start = System.currentTimeMillis();
		while(i-- > 0) {
			for(int j = 0; j < 8; j++) {
				for(int n = 0; n < 16; n++) {
					executor.execute(createCounterTask(domain.getName(), String.format("%d-%d-%n", i, j, n), 1000, latch));
				}
				Thread.sleep(100);
			}
		}
		latch.await();
		System.out.println(String.format("TPS: %f", (60 * 128) / ((System.currentTimeMillis() - start) / (1000 + 0.0D))));
		executor.shutdown();
	}
	
	@Test
	// 实测 TPS: 125.404134
	public void testDomainThreadReservedQueueReserved_TPS_128_Dealwith_ResourceLimitException_Send_Speed_Big_Then_128() throws Exception {
		ThreadingConfiguration threadingConfiguration = new ThreadingConfiguration(ResourceControlMode.RESERVED, 64);
		QueuingConfiguration queuingConfiguration = new QueuingConfiguration(ResourceControlMode.RESERVED, 256);
		ResourceConfiguration configuration = new ResourceConfiguration();
		configuration.setQueuingConfiguration(queuingConfiguration);
		configuration.setThreadingConfiguration(threadingConfiguration);
		TaskDomain domain = new TaskDomain("test", configuration);
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.setCoreThreadCount(128);
		bootstrap.setMaxThreadCount(128);
		bootstrap.setQueueCapacity(512);
		bootstrap.register(domain);
		
		Executor executor = bootstrap.bootstrap();
		
		int i = 60;
		CountDownLatch latch = new CountDownLatch(60 * 128);
		long start = System.currentTimeMillis();
		while(i-- > 0) {
			for(int j = 0; j < 8; j++) {
				for(int n = 0; n < 16; n++) {
					Task task = createCounterTask(domain.getName(), String.format("%d-%d-%n", i, j, n), 1000, latch);
					for(;;) {
						try {
							executor.execute(task);
							break;
						} catch (ResourceLimitException e) {
							e.printStackTrace();
							logger.error("Resource Limited", e);
							Thread.yield();
						}
					}
				}
				Thread.sleep(100);
			}
		}
		latch.await();
		System.out.println(String.format("TPS: %f", (60 * 128) / ((System.currentTimeMillis() - start) / (1000 + 0.0D))));
		executor.shutdown();
	}
	
	private Task createTask(final String domainName, final String name, final int sleepTime) {
		return new AbstractTask() {

			@Override
			public String getTaskDomain() {
				return domainName;
			}

			@Override
			public void execute() {
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
					Thread.currentThread().interrupt();
				}
				System.out.println(String.format("Task %s executed", name));
			}
			
		};
	}
	
	/**
	 * 
	 * @param domainName
	 * @param name
	 * @param sleepTime
	 * @param latch
	 * @return
	 */
	private Task createCounterTask(final String domainName, final String name, final int sleepTime, final CountDownLatch latch) {
		return new AbstractTask() {

			@Override
			public String getTaskDomain() {
				return domainName;
			}

			@Override
			public void execute() {
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
					Thread.currentThread().interrupt();
				}
				latch.countDown();
			}
			
		};
	}
}
