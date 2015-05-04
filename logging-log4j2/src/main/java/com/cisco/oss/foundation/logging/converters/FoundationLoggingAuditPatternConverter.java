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

/**
 * 
 */
package com.cisco.oss.foundation.logging.converters;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.NamePatternConverter;

/**
 * @author Yair Ogen
 * 
 */
@Plugin(name = "FoundationLoggingAuditPatternConverter", category = "Converter")
@ConverterKeys({ "audit" })
public final class FoundationLoggingAuditPatternConverter extends NamePatternConverter {

	/**
	 * Private constructor.
	 * 
	 * @param options
	 *            options, may be null.
	 */
	private FoundationLoggingAuditPatternConverter(final String[] options) {
		super("Audit", "audit", options);

	}

	/**
	 * Gets an instance of the class.
	 * 
	 * @param options
	 *            pattern options, may be null. If first element is "short",
	 *            only the first line of the throwable will be formatted.
	 * @return instance of class.
	 */
	public static FoundationLoggingAuditPatternConverter newInstance(final String[] options) {
		return new FoundationLoggingAuditPatternConverter(options);
	}

	/**
	 * {@inheritDoc}
	 */
    @Override
    public void format(final LogEvent event, final StringBuilder toAppendTo) {
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
