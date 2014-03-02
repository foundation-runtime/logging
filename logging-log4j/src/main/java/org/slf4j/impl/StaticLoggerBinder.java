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

package org.slf4j.impl;

import com.cisco.oss.foundation.logging.FoundationLoggingLoggerFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

public class StaticLoggerBinder implements LoggerFactoryBinder {

	  /**
	   * The unique instance of this class.
	   * 
	   */
	  private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();
	  
	  private static final String loggerFactoryClassStr = FoundationLoggingLoggerFactory.class.getName();

	  /**
	   * The ILoggerFactory instance returned by the {@link #getLoggerFactory}
	   * method should always be the same object
	   */
	  private final ILoggerFactory loggerFactory;

	  /**
	   * Return the singleton of this class.
	   * 
	   * @return the StaticLoggerBinder singleton
	   */
	  public static final StaticLoggerBinder getSingleton() {
	    return SINGLETON;
	  }

	  private StaticLoggerBinder() {
	    loggerFactory = new FoundationLoggingLoggerFactory();
	  }

	  public ILoggerFactory getLoggerFactory() {
	    return loggerFactory;
	  }

	  public String getLoggerFactoryClassStr() {
	    return loggerFactoryClassStr;
	  }
}
