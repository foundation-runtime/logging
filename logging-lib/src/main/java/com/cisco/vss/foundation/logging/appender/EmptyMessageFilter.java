package com.cisco.vss.foundation.logging.appender;

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
