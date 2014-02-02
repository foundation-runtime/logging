package com.cisco.vss.foundation.logging.stuctured;


import com.cisco.vss.foundation.logging.CabLoggingEvent;

/**
 * A formatter helper class to support formatting based on annotations present on the Marker
 * This class should not have any user defined implementations
 * @author Yair Ogen
 *
 */
public interface CABLoggingMarkerFormatter {
	String getFormat(CabLoggingEvent cabLoggingEvent);
	void setMarker(CABLoggingMarker marker);
}
