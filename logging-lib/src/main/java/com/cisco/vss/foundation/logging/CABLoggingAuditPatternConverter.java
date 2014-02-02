/**
 * 
 */
package com.cisco.vss.foundation.logging;

import org.apache.log4j.pattern.LoggingEventPatternConverter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * @author Yair Ogen
 * 
 */
public final class CABLoggingAuditPatternConverter extends LoggingEventPatternConverter {

	/**
	 * Private constructor.
	 * 
	 * @param options
	 *            options, may be null.
	 */
	private CABLoggingAuditPatternConverter() {
		super("Audit", "audit");

	}

	/**
	 * Gets an instance of the class.
	 * 
	 * @param options
	 *            pattern options, may be null. If first element is "short",
	 *            only the first line of the throwable will be formatted.
	 * @return instance of class.
	 */
	public static CABLoggingAuditPatternConverter newInstance(final String[] options) {
		return new CABLoggingAuditPatternConverter();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void format(final LoggingEvent event, final StringBuffer toAppendTo) {
		String loggerName = event.getLoggerName();
		if (loggerName.startsWith("audit.")) {
			toAppendTo.append("AUDIT-");
		}
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
