package com.cisco.oss.foundation.logging;

/**
 * Use this class when you are using LoggignHelper and you need an entry point to modify the object var-args sent to the LoggingHelper API.
 * The "converter" method will only be invoked if the requested log level is enabled
 * @author yogen
 *
 */
public abstract class AbstractLoggingHelperConverter {
	
	public abstract Object[] convert(Object... args);

}
