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

import org.slf4j.Marker;

@SuppressWarnings("serial")
/**
 * common interface for logging events. used by implementations for the structured logging binding
 */
public interface FoundationLoggingEvent{


    /**
     * get the marker implementation
     * @return
     */
	Marker getMarker();

    /**
     * set the appender name correlated to this appender
     * @param name
     */
	void setAppenderName(String name);

    /**
     * get the appender name. used by generated source for teh marker fomratter
     * @return
     */
	String getAppenderName();
	

}
