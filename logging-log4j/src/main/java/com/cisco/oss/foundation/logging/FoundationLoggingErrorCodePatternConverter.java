/**
 * 
 */
package com.cisco.oss.foundation.logging;

import com.cisco.oss.foundation.application.exception.ApplicationExceptionInterface;
import org.apache.log4j.pattern.LoggingEventPatternConverter;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

/**
 * @author Yair Ogen
 * 
 */
public final class FoundationLoggingErrorCodePatternConverter extends LoggingEventPatternConverter {

	/**
	 * Private constructor.
	 * 
	 */
	private FoundationLoggingErrorCodePatternConverter() {
		super("ErrorCode", "errorcode");

	}

	/**
	 * Gets an instance of the class.
	 * 
	 * @param options
	 *            pattern options, may be null. If first element is "short",
	 *            only the first line of the throwable will be formatted.
	 * @return instance of class.
	 */
	public static FoundationLoggingErrorCodePatternConverter newInstance(final String[] options) {
		return new FoundationLoggingErrorCodePatternConverter();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void format(final LoggingEvent event, final StringBuffer toAppendTo) {

		ThrowableInformation throwableInformation = event.getThrowableInformation();

		if (throwableInformation != null) {

			Throwable throwable = throwableInformation.getThrowable();

			if (throwable instanceof ApplicationExceptionInterface) {// NOPMD
				final ApplicationExceptionInterface ApplicationExceptionInterface = (ApplicationExceptionInterface) throwable;
				toAppendTo.append("[Error code: ");
				toAppendTo.append(ApplicationExceptionInterface.getErrorCode() == null ? "%NO_ERROR_CODE%" : ApplicationExceptionInterface.getErrorCode().toString());
				toAppendTo.append("] ");

			}
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
