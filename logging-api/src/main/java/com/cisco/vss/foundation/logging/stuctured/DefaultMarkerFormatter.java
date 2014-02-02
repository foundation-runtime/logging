package com.cisco.vss.foundation.logging.stuctured;

import com.cisco.vss.foundation.logging.CabLoggingEvent;

/**
 * Default formatter for use when User Marker doesn't have any annotations or configuration overrides
 * @author Yair Ogen
 */
public class DefaultMarkerFormatter implements CABLoggingMarkerFormatter {

	@Override
	public String getFormat(CabLoggingEvent cabLoggingEvent) {
		return null;
	}

	@Override
	public void setMarker(CABLoggingMarker marker) {
	}

}
