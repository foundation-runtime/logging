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

package com.cisco.oss.foundation.logging.appender;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * A class that represent a filter to filter empty messages from log file. 
 * @author ykasten
 *
 */
public class EmptyMessageFilter extends Filter {

	@Override
	public int decide(LoggingEvent event) {
		if(event.getMessage() instanceof String){
			if(StringUtils.isEmpty((String)event.getMessage())){
				return Filter.DENY;
			}
		}
		return Filter.NEUTRAL;
	}

}
