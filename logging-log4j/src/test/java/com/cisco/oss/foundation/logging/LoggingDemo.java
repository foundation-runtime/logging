package com.cisco.oss.foundation.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingDemo {

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		
		while(true){
			LoggingDemo1.log();
			LoggingDemo2.log();
			Thread.sleep(1000);
		}

	}
	
	private static final class LoggingDemo1{
		private static final Logger LOGGER = LoggerFactory.getLogger(LoggingDemo1.class);
		
		private static void log(){
			LOGGER.info("demo 1 info log");
			LOGGER.warn("demo 1 warn log");
		}
	}
	
	private static final class LoggingDemo2{
		private static final Logger LOGGER = LoggerFactory.getLogger(LoggingDemo2.class);
		
		private static void log(){
			LOGGER.info("demo 2 info log");
			LOGGER.warn("demo 2 warn log");
		}
	}

}
