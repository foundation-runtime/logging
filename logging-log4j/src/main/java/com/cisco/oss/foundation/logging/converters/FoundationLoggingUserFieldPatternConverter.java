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

import com.cisco.oss.foundation.logging.FoundationLof4jLoggingEvent;
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
		
		if(event instanceof FoundationLof4jLoggingEvent){
			FoundationLof4jLoggingEvent foundationLof4jLoggingEvent = (FoundationLof4jLoggingEvent)event;
			
			Marker marker = foundationLof4jLoggingEvent.getSlf4jMarker();
			
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
