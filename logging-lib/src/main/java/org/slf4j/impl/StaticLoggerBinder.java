package org.slf4j.impl;

import com.cisco.vss.foundation.logging.CabLoggingLoggerFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

public class StaticLoggerBinder implements LoggerFactoryBinder {

	  /**
	   * The unique instance of this class.
	   * 
	   */
	  private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();
	  
	  private static final String loggerFactoryClassStr = CabLoggingLoggerFactory.class.getName();

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
	    loggerFactory = new CabLoggingLoggerFactory();	    
	  }

	  public ILoggerFactory getLoggerFactory() {
	    return loggerFactory;
	  }

	  public String getLoggerFactoryClassStr() {
	    return loggerFactoryClassStr;
	  }
}
