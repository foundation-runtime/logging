/*
 * Copyright 2015 Cisco Systems, Inc.
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

package org.slf4j.impl;

import org.slf4j.spi.MDCAdapter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Log4jMDCAdapter implements MDCAdapter {

	public void clear() {
		Map map = org.apache.log4j.MDC.getContext();
		if (map != null) {
			map.clear();
		}
	}

	public String get(String key) {
		return (String) org.apache.log4j.MDC.get(key);
	}

	/**
	 * Put a context value (the <code>val</code> parameter) as identified with the <code>key</code> parameter into the current thread's context map. The <code>key</code> parameter cannot be null. Log4j does <em>not</em> support null for the <code>val</code> parameter.
	 * 
	 * <p>
	 * This method delegates all work to log4j's MDC.
	 * 
	 * @throws IllegalArgumentException
	 *             in case the "key" or <b>"val"</b> parameter is null
	 */
	public void put(String key, String val) {
		org.apache.log4j.MDC.put(key, val);
	}

	public void remove(String key) {
		org.apache.log4j.MDC.remove(key);
	}

	public Map getCopyOfContextMap() {
		Map old = org.apache.log4j.MDC.getContext();
		if (old != null) {
			return new HashMap(old);
		} else {
			return null;
		}
	}

	public void setContextMap(Map contextMap) {
		Map old = org.apache.log4j.MDC.getContext();
		if (old == null) {
			Iterator entrySetIterator = contextMap.entrySet().iterator();
			while (entrySetIterator.hasNext()) {
				Map.Entry mapEntry = (Map.Entry) entrySetIterator.next();
				org.apache.log4j.MDC.put((String) mapEntry.getKey(), mapEntry.getValue());
			}
		} else {
			old.clear();
			old.putAll(contextMap);
		}
	}
}
