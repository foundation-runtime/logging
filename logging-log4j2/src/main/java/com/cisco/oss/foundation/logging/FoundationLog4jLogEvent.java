/*
 * Copyright 2014 Cisco Systems, Inc.
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

package com.cisco.oss.foundation.logging;

import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.slf4j.Marker;

@SuppressWarnings("serial")
public class FoundationLog4jLogEvent extends Log4jLogEvent implements FoundationLoggingEvent {

	private Marker marker = null;
	private String appenderName;



	public FoundationLog4jLogEvent(FoundationLog4jLogEvent event) {
		super(event.getLoggerName(),(org.apache.logging.log4j.Marker)event.getMarker(),event.getLoggerFqcn(),event.getLevel(),event.getMessage(), event.getThrown());
		this.marker=(Marker)event.marker;
	}

    /**
     * get the marker implementation
     * @return
     */
    @Override
	public Marker getSlf4jMarker() {
		return marker;
	}

    /**
     * set the appender name correlated to this appender
     * @param name
     */
    @Override
	public void setAppenderName(String name) {
		this.appenderName = name;
		
	}

    /**
     * get the appender name. used by generated source for teh marker fomratter
     * @return
     */
    @Override
	public String getAppenderName() {
		return appenderName;
	}
	

}
