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
package com.opentech.camel.task.resource;

import java.util.concurrent.atomic.AtomicInteger;

import com.opentech.camel.task.exception.ResourceLimitException;
import com.opentech.camel.task.lifecycle.AbstractLifeCycle;

/**
 * Default resource controller of task domain
 * @author sihai
 *
 */
public class DefaultTaskDomainResourceController extends AbstractLifeCycle implements TaskDomainResourceController {

	/**
	 * Resource configuration of controller.
	 */
	private ResourceConfiguration resourceConfiguration;
	
	/**
	 * Resource of this controller
	 */
	private TaskDomainResource resource;
	
	/**
	 * 
	 */
	private AtomicInteger usedThreadCount;
	
	/**
	 * 
	 */
	private AtomicInteger queueSize;
	
	private AtomicInteger allocedThreadCount = new AtomicInteger(0);
	
	private AtomicInteger releaseThreadCount = new AtomicInteger(0);
	
	/**
	 * 
	 * @param resourceConfiguration
	 * @param resource
	 */
	public DefaultTaskDomainResourceController(ResourceConfiguration resourceConfiguration, TaskDomainResource resource) {
		this.resourceConfiguration = resourceConfiguration;
		this.resource = resource;
	}
	
	/**
	 * Initialize
	 */
	@Override
	public void initialize() {
		usedThreadCount = new AtomicInteger(0);
		queueSize = new AtomicInteger(0);
	}
	
	/**
	 * Shutdown
	 */
	@Override
	public void shutdown() {
		usedThreadCount.set(0);
		queueSize.set(0);
		// Release resource
		resource.release();
	}
	
	@Override
	public ResourceConfiguration getResourceConfiguration() {
		return resourceConfiguration;
	}

	@Override
	public TaskDomainResource getResource() {
		return resource;
	}

	@Override
	public ResourceHolder acquire() throws ResourceLimitException {
		try {
			return acquire(ResourceType.THREAD);
		} catch (ResourceLimitException e) {
			return acquire(ResourceType.QUEUE);
		}
	}

	@Override
	public ResourceHolder acquire(ResourceType type) throws ResourceLimitException {
		if(ResourceType.THREAD == type) {
			if(usedThreadCount.incrementAndGet() <= resourceConfiguration.getThreadingConfiguration().getThreadCount()) {
				allocedThreadCount.incrementAndGet();
				System.out.println(String.format("allocedThreadCount:%d, releaseThreadCount:%d", allocedThreadCount.intValue(), releaseThreadCount.intValue()));
				System.out.println(String.format("usedThreadCount:%d, threadCount:%d", usedThreadCount.intValue(), resourceConfiguration.getThreadingConfiguration().getThreadCount()));
				return new ResourceHolder(ResourceType.THREAD);
			}
			usedThreadCount.decrementAndGet();
		} else if(ResourceType.QUEUE == type) {
			if(queueSize.incrementAndGet() <= resourceConfiguration.getQueuingConfiguration().getQueueCapacity()) {
				return new ResourceHolder(ResourceType.QUEUE);
			}
			queueSize.decrementAndGet();
		} else {
			throw new IllegalArgumentException(String.format("Unknown resource type:%s", type));
		}
		
		throw new ResourceLimitException(String.format("usedThreadCount:%d, maxThreadCount:%d, queueCapacity:%d, queueSize:%d", usedThreadCount.intValue(), resourceConfiguration.getThreadingConfiguration().getThreadCount(), resourceConfiguration.getQueuingConfiguration().getQueueCapacity(), queueSize.intValue()));
	}

	@Override
	public void release(ResourceHolder holder) {
		release(holder, holder.getType());
	}
	
	@Override
	public void release(ResourceHolder holder, ResourceType type) {
		if(holder.getType() != type) {
			return;
			//throw new IllegalStateException(String.format("ResourceHolder not hold resource type:%s", type));
		}
		
		if(ResourceType.THREAD == type) {
			usedThreadCount.decrementAndGet();
			releaseThreadCount.incrementAndGet();
			System.out.println(String.format("allocedThreadCount:%d, releaseThreadCount:%d", allocedThreadCount.intValue(), releaseThreadCount.intValue()));
			System.out.println(String.format("usedThreadCount:%d, threadCount:%d", usedThreadCount.intValue(), resourceConfiguration.getThreadingConfiguration().getThreadCount()));
		} else if(ResourceType.QUEUE == type) {
			queueSize.decrementAndGet();
		} else {
			throw new IllegalArgumentException(String.format("Unknown resource type:%s", type));
		}
		holder.setType(null);
	}
	
	@Override
	public String toString() {
		return String.format("{resourceConfiguration: %s, resource: %s, usedThreadCount: %d, queueSize: %d}", resourceConfiguration.toString(), resource.toString(), usedThreadCount.get(), queueSize.get());
	}
}
