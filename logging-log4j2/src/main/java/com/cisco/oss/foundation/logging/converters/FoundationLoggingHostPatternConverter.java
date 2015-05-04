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

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Yair Ogen
 * 
 */
@Plugin(name = "FoundationLoggingHostPatternConverter", category = "Converter")
@ConverterKeys({ "host" })
public final class FoundationLoggingHostPatternConverter  extends NamePatternConverter {

	public static final String hostName = initLocalHost();

	/**
	 * Private constructor.
	 */
	private FoundationLoggingHostPatternConverter(final String[] options) {
		super("Host", "host",options);

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
		return new FoundationLoggingHostPatternConverter(options);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
    public void format(final LogEvent event, final StringBuilder toAppendTo) {

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
