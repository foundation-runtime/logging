/**
 * 
 */
package com.cisco.vss.foundation.logging;

import com.cisco.vss.foundation.logging.stuctured.CABLoggingMarker;
import org.apache.log4j.pattern.LoggingEventPatternConverter;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.Marker;

/**
 * @author Yair Ogen
 * 
 */
public final class CABLoggingUserFieldPatternConverter extends LoggingEventPatternConverter {
	
	private String key = null;

	/**
	 * Private constructor.
	 * 
	 * @param key the converter options key, may be null.
	 */
	private CABLoggingUserFieldPatternConverter(String key) {
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
	public static CABLoggingUserFieldPatternConverter newInstance(final String[] options) {
		return new CABLoggingUserFieldPatternConverter(options.length > 0 ? options[0] : null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void format(final LoggingEvent event, final StringBuffer toAppendTo) {
		
		if(event instanceof CabLoggingEvent){
			CabLoggingEvent cabLoggingEvent = (CabLoggingEvent)event;
			
			Marker marker = cabLoggingEvent.getMarker();
			
			marker.getName();
			
			if(marker instanceof CABLoggingMarker){
				CABLoggingMarker cabLoggingMarker = (CABLoggingMarker)marker;
				String userFieldValue = cabLoggingMarker.valueOf(key);
				if(CABLoggingMarker.NO_OPERATION.equals(userFieldValue)){
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
