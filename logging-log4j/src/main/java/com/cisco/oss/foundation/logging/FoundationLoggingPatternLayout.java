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

import org.apache.log4j.EnhancedPatternLayout;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Class extends EnhancedPatternLayout to allow enabling and disabling of stack
 * trace output.
 * 
 * @author Yair Ogen
 * @author Jethro Revill
 */
public final class FoundationLoggingPatternLayout extends EnhancedPatternLayout {

	/**
	 * Initial converter for pattern.
	 */
	private PatternConverter head;

	/**
	 * Conversion pattern.
	 */
	private String conversionPattern;
	

	/**
	 * Constructs a EnhancedPatternLayout using the DEFAULT_LAYOUT_PATTERN.
	 * 
	 * The default pattern just produces the application supplied message.
	 */
	public FoundationLoggingPatternLayout() {
		this(FoundationLoggerConstants.DEFAULT_CONV_PATTERN.toString());
	}

	/**
	 * Constructs a EnhancedPatternLayout using the supplied conversion pattern.
	 * 
	 * @param pattern
	 *            conversion pattern.
	 */
	public FoundationLoggingPatternLayout(final String pattern) {
		this.conversionPattern = pattern;
		head = createPatternParser((pattern == null) ? DEFAULT_CONVERSION_PATTERN : pattern).parse();
	}

	/**
	 * Set the <b>ConversionPattern</b> option. This is the string which
	 * controls formatting and consists of a mix of literal content and
	 * conversion specifiers.
	 * 
	 * @param conversionPattern
	 *            conversion pattern.
	 */
	public void setConversionPattern(final String conversionPattern) {
		this.conversionPattern = OptionConverter.convertSpecialChars(conversionPattern);
		head = createPatternParser(this.conversionPattern).parse();
	}

	/**
	 * Returns the value of the <b>ConversionPattern</b> option.
	 * 
	 * @return conversion pattern.
	 */
	public String getConversionPattern() {
		return conversionPattern;
	}

	/**
	 * Returns PatternParser used to parse the conversion string. Subclasses may
	 * override this to return a subclass of PatternParser which recognize
	 * custom conversion characters.
	 * 
	 * @since 0.9.0
	 */
	protected org.apache.log4j.helpers.PatternParser createPatternParser(final String pattern) {
		return new FoundationLoggingPatternParser(pattern);
	}

	/**
	 * Formats a logging event to a writer.
	 * 
	 * @param event
	 *            logging event to be formatted.
	 */
	public String format(final LoggingEvent event) {
		final StringBuffer buf = new StringBuffer();
		for (PatternConverter c = head; c != null; c = c.next) {
			c.format(buf, event);
		}
		return buf.toString();
	}

	/**
	 * Will return false if any of the conversion specifiers in the pattern
	 * handles {@link Exception Exceptions}.
	 * 
	 * @return true if the pattern formats any information from exceptions.
	 */
	public boolean ignoresThrowable() {
		return false;
	}
}
