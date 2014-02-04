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
public final class FoundationLoggingMessagePatternConverter extends LoggingEventPatternConverter {

	private static final int DEFAULT_INDENTATION = 5;
	private static final String DEFAULT_INDENTATION_SPACE = "     ";

	private int option;

	/**
	 * Private constructor.
	 */
	private FoundationLoggingMessagePatternConverter(final String[] options) {
		super("Message", "message");
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
	public void format(final LoggingEvent event, final StringBuffer toAppendTo) {

		String renderedMessage = event.getRenderedMessage();

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
