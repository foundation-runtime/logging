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
