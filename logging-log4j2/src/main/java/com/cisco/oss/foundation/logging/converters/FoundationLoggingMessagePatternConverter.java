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
package com.cisco.oss.foundation.logging.converters;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.NamePatternConverter;

/**
 * @author Yair Ogen
 * 
 */
@Plugin(name = "FoundationLoggingMessagePatternConverter", category = "Converter")
@ConverterKeys({ "pm", "prettymessage" })
public final class FoundationLoggingMessagePatternConverter  extends NamePatternConverter {

	private static final int DEFAULT_INDENTATION = 5;
	private static final String DEFAULT_INDENTATION_SPACE = "     ";

	private int option;

	/**
	 * Private constructor.
	 */
	private FoundationLoggingMessagePatternConverter(final String[] options) {
		super("PrettyMessage", "prettymessage",options);
		if ((options != null) && (options.length > 0)) {
			if (isNumeric(options[0])) {
				option = Integer.parseInt(options[0]);
				if (option < 0) {
					option = DEFAULT_INDENTATION;
				}
			} else {
				option = DEFAULT_INDENTATION;
			}
		} else {
			option = DEFAULT_INDENTATION;
		}
	}

	/**
	 * Obtains an instance of pattern converter.
	 * 
	 * @param options
	 *            options, may be null.
	 * @return instance of pattern converter.
	 */
	public static FoundationLoggingMessagePatternConverter newInstance(final String[] options) {
		return new FoundationLoggingMessagePatternConverter(options);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
    public void format(final LogEvent event, final StringBuilder toAppendTo) {

		String renderedMessage = event.getMessage().getFormattedMessage();

		if (renderedMessage!= null && renderedMessage.contains("\n")) {

			toAppendTo.append("\n");

			String[] messageParts = renderedMessage.split("\n");

			String spacing = getSpacing();

			for (int i = 0; i < messageParts.length; i++) {

				String messagePart = messageParts[i];
				toAppendTo.append(spacing);
				toAppendTo.append(messagePart);

				if (i != (messageParts.length - 1)) {
					toAppendTo.append("\n");
				}
			}

		} else {
			toAppendTo.append(renderedMessage);
		}

	}

	/**
	 * @return
	 */
	private String getSpacing() {
		if (option == DEFAULT_INDENTATION) {
			return DEFAULT_INDENTATION_SPACE;
		} else {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < option; i++) {
				builder.append(' ');
			}

			return builder.toString();
		}
	}

	private boolean isNumeric(String str) {
		if (str == null) {
			return false;
		}
		int sz = str.length();
		for (int i = 0; i < sz; i++) {
			if (Character.isDigit(str.charAt(i)) == false) {
				return false;
			}
		}
		return true;
	}
}
