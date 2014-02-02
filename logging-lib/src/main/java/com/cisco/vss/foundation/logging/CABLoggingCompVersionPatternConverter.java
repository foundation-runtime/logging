/**
 * 
 */
package com.cisco.vss.foundation.logging;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.pattern.LoggingEventPatternConverter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * @author Yair Ogen
 * 
 */
public final class CABLoggingCompVersionPatternConverter extends LoggingEventPatternConverter {
	
	private static String componentVersion = getComponentVersion();

	/**
	 * Private constructor.
	 * 
	 */
	private CABLoggingCompVersionPatternConverter() {
		super("compVersion", "compVersion");

	}

	/**
	 * Gets an instance of the class.
	 * 
	 * @param options
	 *            pattern options, may be null. If first element is "short",
	 *            only the first line of the throwable will be formatted.
	 * @return instance of class.
	 */
	public static CABLoggingCompVersionPatternConverter newInstance(final String[] options) {
		return new CABLoggingCompVersionPatternConverter();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void format(final LoggingEvent event, final StringBuffer toAppendTo) {
			toAppendTo.append(componentVersion);
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
	
	private static String getComponentVersion() {
		String compVersion = System.getenv("_ARTIFACT_VERSION");
		
		if(StringUtils.isBlank(compVersion)){
			compVersion = "UNKNOWN";
		}
		
		return compVersion;
	}
}
