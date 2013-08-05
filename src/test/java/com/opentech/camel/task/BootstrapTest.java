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

import org.junit.Test;

import com.opentech.camel.task.exception.ResourceLimitException;
import com.opentech.camel.task.executor.Executor;
import com.opentech.camel.task.queue.QueuingConfiguration;
import com.opentech.camel.task.resource.ResourceControlMode;
import com.opentech.camel.task.resource.ResourceConfiguration;
import com.opentech.camel.task.threading.ThreadingConfiguration;

/**
 * 
 * @author sihai
 *
 */
public class BootstrapTest {

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
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread t, Throwable e) {
				System.out.println(String.format("Exception Thread:%s", t.getName()));
				e.printStackTrace();
			}
			
		});
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
		executor.execute(createTask(domain.getName(), "0"));
		executor.execute(createTask(domain.getName(), "1"));
		executor.execute(createTask(domain.getName(), "2"));
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
		executor.execute(createTask(domain.getName(), "0"));
		executor.execute(createTask(domain.getName(), "1"));
		executor.execute(createTask(domain.getName(), "2"));
		executor.execute(createTask(domain.getName(), "3"));
		executor.execute(createTask(domain.getName(), "4"));
		executor.execute(createTask(domain.getName(), "5"));
		Thread.sleep(100000);
		executor.shutdown();
	}
	
	
	
	private Task createTask(final String domainName, final String name) {
		return new AbstractTask() {

			@Override
			public String getTaskDomain() {
				return domainName;
			}

			@Override
			public void execute() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					Thread.currentThread().interrupt();
				}
				System.out.println(String.format("Task %s executed", name));
			}
			
		};
	}
}
