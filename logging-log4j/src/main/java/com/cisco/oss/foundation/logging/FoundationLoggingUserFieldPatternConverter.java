/**
 * 
 */
package com.cisco.oss.foundation.logging;

import com.cisco.oss.foundation.logging.structured.FoundationLoggingMarker;
import org.apache.log4j.pattern.LoggingEventPatternConverter;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.Marker;

/**
 * @author Yair Ogen
 * 
 */
public final class FoundationLoggingUserFieldPatternConverter extends LoggingEventPatternConverter {
	
	private String key = null;

	/**
	 * Private constructor.
	 * 
	 * @param key the converter options key, may be null.
	 */
	private FoundationLoggingUserFieldPatternConverter(String key) {
		super("UserField", "u");
		this.key = key;

	}

	/**
	 * Gets an instance of the class.
	 * 
	 * @param options
	 *            pattern options, may be null. If first element is "short",
	 *            only the first line of the throwable will be formatted.
	 * @return instance of class.
	 */
	public static FoundationLoggingUserFieldPatternConverter newInstance(final String[] options) {
		return new FoundationLoggingUserFieldPatternConverter(options.length > 0 ? options[0] : null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void format(final LoggingEvent event, final StringBuffer toAppendTo) {
		
		if(event instanceof FoundationLoggingEvent){
			FoundationLoggingEvent foundationLoggingEvent = (FoundationLoggingEvent)event;
			
			Marker marker = foundationLoggingEvent.getMarker();
			
			marker.getName();
			
			if(marker instanceof FoundationLoggingMarker){
				FoundationLoggingMarker foundationLoggingMarker = (FoundationLoggingMarker)marker;
				String userFieldValue = foundationLoggingMarker.valueOf(key);
				if(FoundationLoggingMarker.NO_OPERATION.equals(userFieldValue)){
					toAppendTo.append("");
				}else{
					toAppendTo.append(userFieldValue);	
				}
				
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
