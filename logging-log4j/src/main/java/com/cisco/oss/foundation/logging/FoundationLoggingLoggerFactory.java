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

package com.cisco.oss.foundation.logging;

import org.apache.log4j.LogManager;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FoundationLoggingLoggerFactory implements ILoggerFactory {

	private Map<String, Logger> loggerMap;

	public FoundationLoggingLoggerFactory() {
		//protect against concurrent access of loggerMap
		loggerMap = new ConcurrentHashMap<String, Logger>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.slf4j.ILoggerFactory#getLogger(java.lang.String)
	 */
	public Logger getLogger(String name) {

		Logger slf4jLogger = null;
		

		slf4jLogger = (Logger) loggerMap.get(name);

		if (slf4jLogger == null) {

			org.apache.log4j.Logger log4jLogger;

			if (name.equalsIgnoreCase(Logger.ROOT_LOGGER_NAME)) {
				log4jLogger = LogManager.getRootLogger();
			} else {
				log4jLogger = LogManager.getLogger(name);
			}

			//if the log4j logger also implement slf4j logger (which CBLogger does) we can use that as the slf4j logger
			if (log4jLogger instanceof Logger) {
				slf4jLogger = (Logger) log4jLogger;
			} else {
				slf4jLogger = new Log4jLoggerAdapter(log4jLogger);
			}
			
			loggerMap.put(name, slf4jLogger);
		}
		
		return slf4jLogger;
	}
}
