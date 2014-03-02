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

/**
 * 
 */
package com.cisco.oss.foundation.logging;

import org.apache.log4j.pattern.LoggingEventPatternConverter;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

/**
 * @author Yair Ogen
 * 
 */
public final class FoundationLoggingThrowableInformationPatternConverter extends LoggingEventPatternConverter {
	/**
	 * If "short", only first line of throwable report will be formatted.
	 */
	private final String option;

	/**
	 * Private constructor.
	 * 
	 * @param options
	 *            options, may be null.
	 */
	private FoundationLoggingThrowableInformationPatternConverter(final String[] options) {
		super("Throwable", "throwable");

		if ((options != null) && (options.length > 0)) {
			option = options[0];
		} else {
			option = null;
		}
	}

	/**
	 * Gets an instance of the class.
	 * 
	 * @param options
	 *            pattern options, may be null. If first element is "short",
	 *            only the first line of the throwable will be formatted.
	 * @return instance of class.
	 */
	public static FoundationLoggingThrowableInformationPatternConverter newInstance(final String[] options) {
		return new FoundationLoggingThrowableInformationPatternConverter(options);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void format(final LoggingEvent event, final StringBuffer toAppendTo) {
		final ThrowableInformation information = event.getThrowableInformation();

		if (information != null) {
			final String[] stringRep = information.getThrowableStrRep();

			int length = 0;

			if (option == null) {
				length = stringRep.length;
			} else if ("full".equals(option)) {
				length = stringRep.length;
			} else if ("short".equals(option)) {
				length = -1;
			} else if ("none".equals(option)) {
				return;
			} else {
				length = stringRep.length;
			}

			toAppendTo.append("\n[Exception: ");

			if (length == -1) {
				toAppendTo.append(generateAbbreviatedExceptionMessage(information.getThrowable()));
			} else {
				for (int i = 0; i < length; i++) {
					final String string = stringRep[i];
					toAppendTo.append(string).append('\n');
				}
			}

			toAppendTo.append("]");
		}
	}

	/**
	 * This converter obviously handles throwables.
	 * 
	 * @return true.
	 */
	@Override
	public boolean handlesThrowable() {
		return true;
	}

	/**
	 * Method generates abbreviated exception message.
	 * 
	 * @param message
	 *            Original log message
	 * @param throwable
	 *            The attached throwable
	 * @return Abbreviated exception message
	 */
	private String generateAbbreviatedExceptionMessage(final Throwable throwable) {

		final StringBuilder builder = new StringBuilder();
		builder.append(": ");
		builder.append(throwable.getClass().getCanonicalName());
		builder.append(": ");
		builder.append(throwable.getMessage());

		Throwable cause = throwable.getCause();
		while (cause != null) {
			builder.append('\n');
			builder.append("Caused by: ");
			builder.append(cause.getClass().getCanonicalName());
			builder.append(": ");
			builder.append(cause.getMessage());
			// make sure the exception cause is not itself to prevent infinite
			// looping
			assert (cause != cause.getCause());
			cause = (cause == cause.getCause() ? null : cause.getCause());
		}
		return builder.toString();
	}

}
