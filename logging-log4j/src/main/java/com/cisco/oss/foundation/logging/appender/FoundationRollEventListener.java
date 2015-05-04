/*
 * Copyright 2015 Cisco Systems, Inc.
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

/**
 * 
 */
package com.cisco.oss.foundation.logging.appender;

import com.cisco.oss.foundation.logging.ApplicationState;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.MDC;

import java.util.Collection;

/**
 * 
 * @author Yair Ogen
 */
public class FoundationRollEventListener implements FileRollEventListener {
	
	public void onFileRoll(final FileRollEvent fileRollEvent) {

//		FoundationLogger.printLoggersInfo(LOGGER);
		logState(fileRollEvent);

	}
	
	
	/**
	 * Write all state items to the log file.
	 * 
	 * @param fileRollEvent    the event to log
	 */
	private void logState(final FileRollEvent fileRollEvent) {

//		if (ApplicationState.isApplicationStateEnabled()) {
			
			synchronized (this) {
				
				final Collection<ApplicationState.ApplicationStateMessage> entries = ApplicationState.getAppStateEntries();
				for (ApplicationState.ApplicationStateMessage entry : entries) {
                    Level level = ApplicationState.getLog4jLevel(entry.getLevel());
				    if(level.isGreaterOrEqual(ApplicationState.LOGGER.getEffectiveLevel())) {
						final org.apache.log4j.spi.LoggingEvent loggingEvent = new org.apache.log4j.spi.LoggingEvent(ApplicationState.FQCN, ApplicationState.LOGGER, level, entry.getMessage(), null);

						//Save the current layout before changing it to the original (relevant for marker cases when the layout was changed)
						Layout current=fileRollEvent.getSource().getLayout();
						//fileRollEvent.getSource().activeOriginalLayout();
						String flowContext = (String) MDC.get("flowCtxt");
						MDC.remove("flowCtxt");
						//Write applicationState:
						if(fileRollEvent.getSource().isAddApplicationState() && fileRollEvent.getSource().getFile().endsWith("log")){
							fileRollEvent.dispatchToAppender(loggingEvent);
						}
						//Set current again.
						fileRollEvent.getSource().setLayout(current);
						if (flowContext != null) {
							MDC.put("flowCtxt", flowContext);
						}
						
					}
				}
				
			}
			
//		}
	}


}
