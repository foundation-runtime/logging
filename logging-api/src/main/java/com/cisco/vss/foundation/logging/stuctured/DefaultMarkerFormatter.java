package com.cisco.vss.foundation.logging.stuctured;

import com.cisco.vss.foundation.logging.FoundationLoggingEvent;

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
