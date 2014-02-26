package com.cisco.oss.foundation.logging.structured;

import org.slf4j.Marker;

public interface FoundationLoggingMarker extends Marker {
	
	 static final String NO_OPERATION = "NOP";

	String valueOf(String userFieldName);
	
	String getName();
	
	FoundationLoggingMarkerFormatter getFormatter();
}
