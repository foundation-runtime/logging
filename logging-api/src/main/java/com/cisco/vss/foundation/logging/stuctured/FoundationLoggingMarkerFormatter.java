package com.cisco.vss.foundation.logging.stuctured;


import com.cisco.vss.foundation.logging.FoundationLoggingEvent;

/**
 * A formatter helper class to support formatting based on annotations present on the Marker
 * This class should not have any user defined implementations
 * @author Yair Ogen
 *
 */
public interface FoundationLoggingMarkerFormatter {
	String getFormat(FoundationLoggingEvent foundationLoggingEvent);
	void setMarker(FoundationLoggingMarker marker);
}
