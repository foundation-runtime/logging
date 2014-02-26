/**
 * 
 */
package com.cisco.oss.foundation.logging;

import org.apache.log4j.pattern.LoggingEventPatternConverter;
import org.apache.log4j.spi.LoggingEvent;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Yair Ogen
 * 
 */
public final class FoundationLoggingHostPatternConverter extends LoggingEventPatternConverter {

	public static final String hostName = initLocalHost();

	/**
	 * Private constructor.
	 * 
	 * @param options
	 *            options, may be null.
	 */
	private FoundationLoggingHostPatternConverter() {
		super("Host", "host");

	}

	private static String initLocalHost() {
		try {
			InetAddress addr = InetAddress.getLocalHost();
			return addr.getHostName();
		} catch (UnknownHostException e) {
			throw new UnsupportedOperationException("Cannot resolve host name. Please check hosts file.", e);
		}
	}

	/**
	 * Gets an instance of the class.
	 * 
	 * @param options
	 *            pattern options, may be null. If first element is "short", only the first line of the throwable will be formatted.
	 * @return instance of class.
	 */
	public static FoundationLoggingHostPatternConverter newInstance(final String[] options) {
		return new FoundationLoggingHostPatternConverter();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void format(final LoggingEvent event, final StringBuffer toAppendTo) {

		toAppendTo.append(hostName);

	}

	/**
	 * This converter obviously handles throwables.
	 * 
	 * @return true.
	 */
	@Override
	public boolean handlesThrowable() {
		return false;
	}
}
