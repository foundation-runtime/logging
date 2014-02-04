package com.cisco.vss.foundation.logging;

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
