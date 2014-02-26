/**
 * 
 */
package com.cisco.oss.foundation.logging.appender;

import com.cisco.oss.foundation.logging.ApplicationState;
import org.apache.log4j.Layout;
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
				    if(entry.getLevel().isGreaterOrEqual(ApplicationState.LOGGER.getEffectiveLevel())) {				       
						final org.apache.log4j.spi.LoggingEvent loggingEvent = new org.apache.log4j.spi.LoggingEvent(ApplicationState.FQCN, ApplicationState.LOGGER, entry.getLevel(), entry.getMessage(), null);

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
