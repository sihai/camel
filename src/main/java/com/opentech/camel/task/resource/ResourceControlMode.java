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

/**
 * <b>资源控制模式</b><p>
 * 目前支持如下模式：<p>
 * <ul>
 * 	<li>
 * 		MAX - 限制模式
 * 	</li>
 * <li>
 * 		RESERVED - 预留模式, 其实就是MIN
 * 	</li>
 * </ul>
 * @author sihai
 *
 */
public enum ResourceControlMode {

	MAX,
	RESERVED;
}
