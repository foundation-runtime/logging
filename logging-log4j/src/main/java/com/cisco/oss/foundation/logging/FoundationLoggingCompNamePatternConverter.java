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
public final class FoundationLoggingCompNamePatternConverter extends LoggingEventPatternConverter {
	
	static String componentName = getComponentName();

	/**
	 * Private constructor.
	 * 
	 */
	private FoundationLoggingCompNamePatternConverter() {
		super("compName", "compName");

	}

	/**
	 * Gets an instance of the class.
	 * 
	 * @param options
	 *            pattern options, may be null. If first element is "short",
	 *            only the first line of the throwable will be formatted.
	 * @return instance of class.
	 */
	public static FoundationLoggingCompNamePatternConverter newInstance(final String[] options) {
		return new FoundationLoggingCompNamePatternConverter();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void format(final LoggingEvent event, final StringBuffer toAppendTo) {
		toAppendTo.append(componentName);
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
	
	public static String getComponentName() {
		String compName = System.getenv("_RPM_SOFTWARE_NAME");
		
		if(StringUtils.isBlank(compName)){
			compName = System.getProperty("app.name");
		}
		
		
		if(StringUtils.isBlank(compName)){
			compName = "UNKNOWN";
		}

		return compName;
	}
	public static int getHashedComponentName() {
		return getComponentName().hashCode();
	}

}
