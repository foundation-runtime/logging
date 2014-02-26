/**
 * 
 */
package com.cisco.oss.foundation.logging;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.pattern.LoggingEventPatternConverter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * @author Yair Ogen
 * 
 */
public final class FoundationLoggingCompInstPathPatternConverter extends LoggingEventPatternConverter {

	private static String componentInstallPath = getComponentInstallPath();

	/**
	 * Private constructor.
	 * 
	 */
	private FoundationLoggingCompInstPathPatternConverter() {
		super("CompInstallPath", "compInstallPath");

	}

	/**
	 * Gets an instance of the class.
	 * 
	 * @param options
	 *            pattern options, may be null. If first element is "short", only the first line of the throwable will be formatted.
	 * @return instance of class.
	 */
	public static FoundationLoggingCompInstPathPatternConverter newInstance(final String[] options) {
		return new FoundationLoggingCompInstPathPatternConverter();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void format(final LoggingEvent event, final StringBuffer toAppendTo) {

		toAppendTo.append(componentInstallPath);

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

	private static String getComponentInstallPath() {
		String compInstPath = System.getenv("_INSTALL_DIR");

		if (StringUtils.isBlank(compInstPath)) {
			compInstPath = "UNKNOWN";
		}

		return compInstPath;
	}
}
