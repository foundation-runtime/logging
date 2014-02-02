package com.cisco.vss.foundation.logging.stuctured;

import org.slf4j.Marker;

public interface CABLoggingMarker extends Marker {
	
	 static final String NO_OPERATION = "NOP";

	String valueOf(String userFieldName);
	
	String getName();
	
	CABLoggingMarkerFormatter getFormatter();
}
