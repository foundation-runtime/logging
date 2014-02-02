package com.cisco.vss.foundation.logging;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.text.MessageFormat;

/**
 * Helper class for formatting log messages.
 *
 * @author Yair Ogen
 * @author Jethro Revill
 */
/**
 * @author Yair Ogen
 *
 */

/**
 * @author Yair Ogen
 */
public final class LoggingHelper { // NOPMD

	/**
	 * Formatter for communication messages.
	 */
	private static final MessageFormat COMM_MESSAGE_FORMAT = new MessageFormat("protocol[{0}] source[{1}] destination[{2}] {3}");

	/**
	 * Formatter for communication messages with addition of in out mode.
	 */
	private static final MessageFormat COMM_MESSAGE_FORMAT_IN_OUT = new MessageFormat("{0}: protocol[{1}] source[{2}] destination[{3}] {4}"); // NOPMD

	/**
	 * Formatter for connection establishment messages.
	 */
	private static final MessageFormat CON_ESTABLISHMENT_FORMAT = new MessageFormat("[{0}] remote host[{1}] {2}");

	/**
	 * Formatter for connection termination messages.
	 */
	private static final MessageFormat CON_TERMINATION_FORMAT = new MessageFormat("[{0}] remote host[{1}] {2} - {3}");

	/**
	 * Private constructor ensures LoggingHelper cannot be instantiated.
	 */
	private LoggingHelper() {
	}

	/**
	 * Enable logging using String.format internally only if debug level is
	 * enabled.
	 * 
	 * @param logger
	 *            the logger that will be used to log the message
	 * @param format
	 *            the format string (the template string)
	 * @param converter
	 *            the converter used to convert the param arguments in case the
	 *            trace level is enabled
	 * @param params
	 *            the parameters to be formatted into it the string format
	 */
	public static void trace(final Logger logger, final String format, final AbstractLoggingHelperConverter converter, final Object... params) {
		trace(logger, format, null, converter, params);
	}

	/**
	 * Enable logging using String.format internally only if debug level is
	 * enabled.
	 * 
	 * @param logger
	 *            the logger that will be used to log the message
	 * @param format
	 *            the format string (the template string)
	 * @param params
	 *            the parameters to be formatted into it the string format
	 */
	public static void trace(final Logger logger, final String format, final Object... params) {
		trace(logger, format, null, null, params);
	}

	/**
	 * Enable logging using String.format internally only if debug level is
	 * enabled.
	 * 
	 * @param logger
	 *            the logger that will be used to log the message
	 * @param format
	 *            the format string (the template string)
	 * @param throwable
	 *            a throwable object that holds the throwable information
	 * @param converter
	 *            the converter used to convert the param arguments in case the
	 *            trace level is enabled
	 * @param params
	 *            the parameters to be formatted into it the string format
	 */
	public static void trace(final Logger logger, final String format, final Throwable throwable, final AbstractLoggingHelperConverter converter, final Object... params) {
		if (logger.isTraceEnabled()) {
			Object[] formatParams = params;
			if (converter != null) {
				formatParams = converter.convert(params);
			}
			final String message = String.format(format, formatParams);
			logger.trace(message, throwable);
		}
	}

	/**
	 * Enable logging using String.format internally only if debug level is
	 * enabled.
	 * 
	 * @param logger
	 *            the logger that will be used to log the message
	 * @param format
	 *            the format string (the template string)
	 * @param throwable
	 *            a throwable object that holds the throable information
	 * @param params
	 *            the parameters to be formatted into it the string format
	 */
	public static void trace(final Logger logger, final String format, final Throwable throwable, final Object... params) {
		trace(logger, format, throwable, null, params);
	}

	/**
	 * Enable logging using String.format internally only if debug level is
	 * enabled.
	 * 
	 * @param logger
	 *            the logger that will be used to log the message
	 * @param format
	 *            the format string (the template string)
	 * @param params
	 *            the parameters to be formatted into it the string format
	 */
	public static void debug(final Logger logger, final String format, final Object... params) {
		debug(logger, format, null, null, params);
	}

	/**
	 * Enable logging using String.format internally only if debug level is
	 * enabled.
	 * 
	 * @param logger
	 *            the logger that will be used to log the message
	 * @param format
	 *            the format string (the template string)
	 * @param converter
	 *            the converter used to convert the param arguments in case the
	 *            debug level is enabled
	 * @param params
	 *            the parameters to be formatted into it the string format
	 */
	public static void debug(final Logger logger, final String format, final AbstractLoggingHelperConverter converter, final Object... params) {
		debug(logger, format, null, converter, params);
	}

	/**
	 * Enable logging using String.format internally only if debug level is
	 * enabled.
	 * 
	 * @param logger
	 *            the logger that will be used to log the message
	 * @param format
	 *            the format string (the template string)
	 * @param throwable
	 *            a throwable object that holds the throable information
	 * @param params
	 *            the parameters to be formatted into it the string format
	 */
	public static void debug(final Logger logger, final String format, final Throwable throwable, final AbstractLoggingHelperConverter converter, final Object... params) {
		if (logger.isDebugEnabled()) {
			Object[] formatParams = params;
			if (converter != null) {
				formatParams = converter.convert(params);
			}
			final String message = String.format(format, formatParams);
			logger.debug(message, throwable);
		}
	}

	/**
	 * Enable logging using String.format internally only if info level is
	 * enabled.
	 * 
	 * @param logger
	 *            the logger that will be used to log the message
	 * @param format
	 *            the format string (the template string)
	 * @param throwable
	 *            the throwable object to be logged
	 * @param params
	 *            the parameters to be formatted into it the string format
	 */
	public static void debug(final Logger logger, final String format, final Throwable throwable, final Object... params) {
		debug(logger, format, throwable, null, params);
	}

	/**
	 * Enable logging using String.format internally only if info level is
	 * enabled.
	 * 
	 * @param logger
	 *            the logger that will be used to log the message
	 * @param format
	 *            the format string (the template string)
	 * @param params
	 *            the parameters to be formatted into it the string format
	 */
	public static void info(final Logger logger, final String format, final Object... params) {
		info(logger, format, null, params);
	}

	/**
	 * Enable logging using String.format internally only if info level is
	 * enabled.
	 * 
	 * @param logger
	 *            the logger that will be used to log the message
	 * @param format
	 *            the format string (the template string)
	 * @param throwable
	 *            a throwable object that holds the throable information
	 * @param params
	 *            the parameters to be formatted into it the string format
	 */
	public static void info(final Logger logger, final String format, final Throwable throwable, final Object... params) {
		if (logger.isInfoEnabled()) {
			final String message = String.format(format, params);
			logger.info(message, throwable);
		}
	}

	/**
	 * log message using the String.format API
	 * 
	 * @param logger
	 *            the logger that will be used to log the message
	 * @param format
	 *            the format string (the template string)
	 * @param params
	 *            the parameters to be formatted into it the string format
	 */
	public static void warn(final Logger logger, final String format, final Object... params) {
		warn(logger, format, null, params);
	}

	/**
	 * log message using the String.format API
	 * 
	 * @param logger
	 *            the logger that will be used to log the message
	 * @param format
	 *            the format string (the template string)
	 * @param throwable
	 *            a throwable object that holds the throable information
	 * @param params
	 *            the parameters to be formatted into it the string format
	 */
	public static void warn(final Logger logger, final String format, final Throwable throwable, final Object... params) {
		final String message = String.format(format, params);
		logger.warn(message, throwable);
	}

	/**
	 * log message using the String.format API
	 * 
	 * @param logger
	 *            the logger that will be used to log the message
	 * @param format
	 *            the format string (the template string)
	 * @param params
	 *            the parameters to be formatted into it the string format
	 */
	public static void error(final Logger logger, final String format, final Object... params) {
		error(logger, format, null, params);
	}

	/**
	 * log message using the String.format API
	 * 
	 * @param logger
	 *            the logger that will be used to log the message
	 * @param format
	 *            the format string (the template string)
	 * @param throwable
	 *            a throwable object that holds the throable information
	 * @param params
	 *            the parameters to be formatted into it the string format
	 */
	public static void error(final Logger logger, final String format, final Throwable throwable, final Object... params) {
		final String message = String.format(format, params);
		logger.error(message, throwable);
	}

	/**
	 * log message using the String.format API.
	 * 
	 * @param logger
	 *            the logger that will be used to log the message
	 * @param format
	 *            the format string (the template string)
	 * @param params
	 *            the parameters to be formatted into it the string format
	 */
	public static void fatal(final Logger logger, final String format, final Object... params) {
		fatal(logger, format, null, params);
	}

	/**
	 * log message using the String.format API.
	 * 
	 * @param logger
	 *            the logger that will be used to log the message
	 * @param format
	 *            the format string (the template string)
	 * @param throwable
	 *            a throwable object that holds the throable information
	 * @param params
	 *            the parameters to be formatted into it the string format
	 */
	public static void fatal(final Logger logger, final String format, final Throwable throwable, final Object... params) {
		final String message = String.format(format, params);
		logger.fatal(message, throwable);
	}

	/**
	 * log message using the String.format API.
	 * 
	 * @param logger
	 *            a logger object that will be used for the actual log.
	 * @param level
	 *            the level of the requested log
	 * @param throwable
	 *            a throwable object if applicable
	 * @param format
	 *            the format string (the template string)
	 * @param params
	 *            the parameters to be formatted into it the string format
	 */
	public static void log(final Logger logger, final Level level, final Throwable throwable, final String format, final Object... params) {
		if (level.isGreaterOrEqual(logger.getEffectiveLevel())) {
			logger.log(level, String.format(format, params), throwable);
		}

	}

	/**
	 * log message using the String.format API.
	 * 
	 * @param logger
	 *            a logger object that will be used for the actual log.
	 * @param level
	 *            the level of the requested log
	 * @param format
	 *            the format string (the template string)
	 * @param params
	 *            the parameters to be formatted into it the string format
	 */
	public static void log(final Logger logger, final Level level, final String format, final Object... params) {
		if (level.isGreaterOrEqual(logger.getEffectiveLevel())) {
			logger.log(level, String.format(format, params));
		}
	}

	/**
	 * log message using the String.format API.
	 * 
	 * @param logger
	 *            a logger object that will be used for the actual log.
	 * @param callerFQCN
	 *            The wrapper class' fully qualified class name.
	 * @param level
	 *            the level of the requested log
	 * @param throwable
	 *            a throwable object if applicable
	 * @param format
	 *            the format string (the template string)
	 * @param params
	 *            the parameters to be formatted into it the string format
	 */
	public static void log(final Logger logger, final String callerFQCN, final Level level, final Throwable throwable, final String format, final Object... params) {
		if (level.isGreaterOrEqual(logger.getEffectiveLevel())) {
			logger.log(callerFQCN, level, String.format(format, params), throwable);
		}
	}

	/**
	 * Helper method for formatting transmission and reception messages.
	 * 
	 * @param protocol
	 *            The protocol used
	 * @param source
	 *            Message source
	 * @param destination
	 *            Message destination
	 * @param message
	 *            The message
	 * @return A formatted message in the format:
	 *         "protocol[&lt;protocol&gt;] source[&lt;source&gt;] destination[&lt;destination&gt;] &lt;message&gt;"
	 * <br/>
	 *         e.g. protocol[OpenCAS] source[234.234.234.234:4321]
	 *         destination[123.123.123.123:4567] 0x0a0b0c0d0e0f
	 */
	public static String formatCommunicationMessage(final String protocol, final String source, final String destination, final String message) {
		return COMM_MESSAGE_FORMAT.format(new Object[] { protocol, source, destination, message });
	}

	/**
	 * Helper method for formatting transmission and reception messages.
	 * 
	 * @param protocol
	 *            The protocol used
	 * @param source
	 *            Message source
	 * @param destination
	 *            Message destination
	 * @param message
	 *            The message
	 * @param inOutMODE
	 *            - Enum the designates if this communication protocol is in
	 *            coming (received) or outgoing (transmitted)
	 * @return A formatted message in the format:
	 *         "Rx: / Tx: protocol[&lt;protocol&gt;] source[&lt;source&gt;] destination[&lt;destination&gt;] &lt;message&gt;"
	 * <br/>
	 *         e.g. Rx: protocol[OpenCAS] source[234.234.234.234:4321]
	 *         destination[123.123.123.123:4567] 0x0a0b0c0d0e0f
	 */
	public static String formatCommunicationMessage(final String protocol, final String source, final String destination, final String message, final IN_OUT_MODE inOutMODE) {
		return COMM_MESSAGE_FORMAT_IN_OUT.format(new Object[] { inOutMODE, protocol, source, destination, message });
	}

	/**
	 * Helper method for formatting connection establishment messages.
	 * 
	 * @param connectionName
	 *            The name of the connection
	 * @param host
	 *            The remote host
	 * @param connectionReason
	 *            The reason for establishing the connection
	 * @return A formatted message in the format:
	 *         "[&lt;connectionName&gt;] remote host[&lt;host&gt;] &lt;connectionReason&gt;"
	 * <br/>
	 *         e.g. [con1] remote host[123.123.123.123] connection to ECMG.
	 */
	public static String formatConnectionEstablishmentMessage(final String connectionName, final String host, final String connectionReason) {
		return CON_ESTABLISHMENT_FORMAT.format(new Object[] { connectionName, host, connectionReason });
	}

	/**
	 * Helper method for formatting connection termination messages.
	 * 
	 * @param connectionName
	 *            The name of the connection
	 * @param host
	 *            The remote host
	 * @param connectionReason
	 *            The reason for establishing the connection
	 * @param terminationReason
	 *            The reason for terminating the connection
	 * @return A formatted message in the format:
	 *         "[&lt;connectionName&gt;] remote host[&lt;host&gt;] &lt;connectionReason&gt; - &lt;terminationReason&gt;"
	 * <br/>
	 *         e.g. [con1] remote host[123.123.123.123] connection to ECMG -
	 *         terminated by remote host.
	 */
	public static String formatConnectionTerminationMessage(final String connectionName, final String host, final String connectionReason, final String terminationReason) {
		return CON_TERMINATION_FORMAT.format(new Object[] { connectionName, host, connectionReason, terminationReason });
	}

	/**
	 * Enum defining backword support for Tx and Rx prints in Communication
	 * Logging Formats,
	 * 
	 * @author Yair Ogen
	 */
	public static enum IN_OUT_MODE {

		/**
		 * To be used with receipt of message. Rendered as 'Rx' in the log
		 * entry.
		 */
		IN("Rx"),

		/**
		 * To be used with transmission of message. Rendered as 'Tx' in the log
		 * entry.
		 */
		OUT("Tx");

		private String value;

		private IN_OUT_MODE(final String value) {
			this.value = value;
		}

		/**
		 * @see Enum#toString()
		 */
		@Override
		public String toString() {
			return value;
		}

	}

}
