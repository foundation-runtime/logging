/*
 * Copyright 2014 Cisco Systems, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.cisco.oss.foundation.logging.structured.test;

public class MessageCounterFactory
{
	private static final ThreadLocal<Long> COUNTER_THREAD_LOCAL = new ThreadLocal<Long>();
	
	public static long getCounter()
	{
		Long counter = COUNTER_THREAD_LOCAL.get();
		if (counter == null)
		{
			COUNTER_THREAD_LOCAL.set(0L);
			counter = 0L;
		}
		//userFields.put("msgCounter", String.valueOf(counter++));
		COUNTER_THREAD_LOCAL.set(counter + 1);
		
		return counter;
	}
}
