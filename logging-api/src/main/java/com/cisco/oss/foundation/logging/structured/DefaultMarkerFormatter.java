package com.cisco.oss.foundation.logging.structured;

import com.cisco.oss.foundation.logging.FoundationLoggingEvent;

/**
 * Default formatter for use when User Marker doesn't have any annotations or configuration overrides
 * @author Yair Ogen
 */
public class DefaultMarkerFormatter implements FoundationLoggingMarkerFormatter {

	@Override
	public String getFormat(FoundationLoggingEvent foundationLoggingEvent) {
		return null;
	}

	@Override
	public void setMarker(FoundationLoggingMarker marker) {
	}

}
