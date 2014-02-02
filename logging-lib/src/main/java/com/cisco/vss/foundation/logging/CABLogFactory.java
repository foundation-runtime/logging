package com.cisco.vss.foundation.logging;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

/**
 * This factory is used for initializing a custom Logger that extends Log4j. The
 * goal is for the user to use the regular log4j API (Logger LOGGER =
 * Logger.getLogger()) but work in runtime with the NDSLogger transparently.
 * 
 * @author Yair Ogen
 */
public class CABLogFactory implements LoggerFactory {



	/**
	 * the CAB Hierarchy that points to the CABLogFactory.
	 */
	static CABLogHierarchy cabLogHierarchy = new CABLogHierarchy();// NOPMD

	static {
		CABLogger.init();
	}

	@Override
	public Logger makeNewLoggerInstance(final String name) {
		return new CABLogger(name);
	}

	
}
