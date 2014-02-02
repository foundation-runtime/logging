package com.cisco.vss.foundation.logging;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.Marker;

@SuppressWarnings("serial")
public class CabLoggingEvent extends LoggingEvent {
	
	private Marker marker = null;
	private String appenderName;
	
	public CabLoggingEvent(String fqnOfCategoryClass, Category logger, Priority level, Object message, Throwable throwable) {
		super(fqnOfCategoryClass, logger, level, message, throwable);
	}
	
	public CabLoggingEvent(Marker marker, String fqnOfCategoryClass, Category logger, Priority level, Object message, Throwable throwable) {
		super(fqnOfCategoryClass, logger, level, message, throwable);
		this.marker = marker;
	}
	
	public CabLoggingEvent(CabLoggingEvent event) {
		
		super(event.fqnOfCategoryClass,event.getLogger(),event.getLevel(),event.getMessage(),event.getThrowableInformation()!=null? event.getThrowableInformation().getThrowable():null);
		this.marker=event.marker;
	}

	public Marker getMarker() {
		return marker;
	}

	public void setAppenderName(String name) {
		this.appenderName = name;
		
	}

	public String getAppenderName() {
		return appenderName;
	}
	

}
