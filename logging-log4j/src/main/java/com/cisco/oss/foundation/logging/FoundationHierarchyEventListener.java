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

/**
 * 
 */
package com.cisco.oss.foundation.logging;

import com.cisco.oss.foundation.logging.appender.FoundationFileRollingAppender;
import com.cisco.oss.foundation.logging.appender.FoundationRollEventListener;
import org.apache.log4j.*;
import org.apache.log4j.appender.TimeAndSizeRollingAppender;
import org.apache.log4j.spi.HierarchyEventListener;

import java.util.Enumeration;
import java.util.HashMap;

/**
 * Override conversion pattern to the Foundation default if pattern was not speicified in the log4j.properties file. This applies only if the appender has a Layout of PatternLayout or EnhancedPatternLayout.
 * 
 * @author Yair Ogen
 */
public class FoundationHierarchyEventListener implements HierarchyEventListener {

	private static HashMap<String,AsyncAppender> asyncAppenders =  new HashMap<String,AsyncAppender>();

	/**
	 * the default for max files to be rolled.
	 */
	private static final int Foundation_MAX_ROLL_COUNT = 100;

	/**
	 * The log4j appender default max rolling files number.
	 */
	private static final int LOG4J_MAX_ROLL_COUNT = 10;

	/**
	 * In this method perform the actual override in runtime.
	 * 
	 * @see org.apache.log4j.spi.HierarchyEventListener#addAppenderEvent(org.apache.log4j.Category, org.apache.log4j.Appender)
	 */
	public void addAppenderEvent(final Category cat, final Appender appender) {

		updateDefaultLayout(appender);

		if (appender instanceof FoundationFileRollingAppender) {
			final FoundationFileRollingAppender timeSizeRollingAppender = (FoundationFileRollingAppender) appender;

			// update the appender with default vales such as logging pattern, file size etc.
			//updateDefaultTimeAndSizeRollingAppender(timeSizeRollingAppender);

			// read teh proeprties and determine if archiving should be enabled.
			updateArchivingSupport(timeSizeRollingAppender);

			// by default add the rolling file listener to enable application
			// state.
			timeSizeRollingAppender.setFileRollEventListener(FoundationRollEventListener.class.getName());

			boolean rollOnStartup = true;

			if (FoundationLogger.log4jConfigProps != null && FoundationLogger.log4jConfigProps.containsKey(FoundationLoggerConstants.Foundation_ROLL_ON_STARTUP.toString())) {
				rollOnStartup = Boolean.valueOf(FoundationLogger.log4jConfigProps.getProperty(FoundationLoggerConstants.Foundation_ROLL_ON_STARTUP.toString()));
			}

			timeSizeRollingAppender.setRollOnStartup(rollOnStartup);

			// refresh the appender
			timeSizeRollingAppender.activateOptions();

			
		//	timeSizeRollingAppender.setOriginalLayout(); //So application state will not make any problems

		}else if(!(appender instanceof FoundationFileRollingAppender) && (appender instanceof TimeAndSizeRollingAppender)){ //TimeAndSizeRollingAppender
			final TimeAndSizeRollingAppender timeSizeRollingAppender = (TimeAndSizeRollingAppender) appender;

			// update the appender with default vales such as logging pattern, file size etc.
			updateDefaultTimeAndSizeRollingAppender(timeSizeRollingAppender);

			// read teh proeprties and determine if archiving should be enabled.
			updateArchivingSupport(timeSizeRollingAppender);

			// by default add the rolling file listener to enable application
			// state.
			timeSizeRollingAppender.setFileRollEventListener(FoundationRollEventListener.class.getName());

			boolean rollOnStartup = true;

			if (FoundationLogger.log4jConfigProps != null && FoundationLogger.log4jConfigProps.containsKey(FoundationLoggerConstants.Foundation_ROLL_ON_STARTUP.toString())) {
				rollOnStartup = Boolean.valueOf(FoundationLogger.log4jConfigProps.getProperty(FoundationLoggerConstants.Foundation_ROLL_ON_STARTUP.toString()));
			}

			timeSizeRollingAppender.setRollOnStartup(rollOnStartup);

			// refresh the appender
			timeSizeRollingAppender.activateOptions();

			
		//	timeSizeRollingAppender.setOriginalLayout();
		}
		if ( ! (appender instanceof org.apache.log4j.AsyncAppender))
            initiateAsyncSupport(appender);

	}

	public void initiateAsyncSupport(final Appender appender) {

		final String asyncAppenderReferencesKey = FoundationLoggerConstants.Foundation_ASYNC_REF.toString();

		// first check if we have some appender references. if not - do nothing.
		if (FoundationLogger.log4jConfigProps != null && FoundationLogger.log4jConfigProps.containsKey(asyncAppenderReferencesKey)) {

			// if the async appender is null or it was closed by log4j as part of the log4j reload we need to create a new instance.
	//		if (asyncAppenders == null || isAsyncAppenderClosed()) {
				
	//		}

			String asyncAppenderReferences = FoundationLogger.log4jConfigProps.getProperty(asyncAppenderReferencesKey);
			String[] asyncAppenderReferencesList = asyncAppenderReferences.split(",");

			// for each appender ref, we need to check if should be added to async appender.
			for (String asyncAppenderRef : asyncAppenderReferencesList) {

				// validate the appender ref matches: "[appender_name]"
				if (!(asyncAppenderRef.startsWith("[") && asyncAppenderRef.endsWith("]"))) {

					// write to error stream as we do can not yet use the logging API
					System.err.println("could not parse async appender ref. Illegal format. " + asyncAppenderRef);
					// ignore
					continue;

				} else {

					// get the "real" appender ref name
					asyncAppenderRef = asyncAppenderRef.substring(1, asyncAppenderRef.lastIndexOf("]"));

					if (appender.getName().equals(asyncAppenderRef)) {

						// check if the appender already exists in the async appender
						Appender appenderRefInAsync = asyncAppenders.get(appender.getName());

						if (appenderRefInAsync != null) {
							asyncAppenders.remove(appender.getName());
						}
						AsyncAppender async=createAsyncAppender(); 
						async.addAppender(appender);
						async.activateOptions();
						asyncAppenders.put(appender.getName(),async);
						

						Enumeration currentLoggers = LogManager.getLoggerRepository().getCurrentLoggers();
						while (currentLoggers.hasMoreElements()) {
							Logger logger = (Logger) currentLoggers.nextElement();
							if (logger.getAppender(appender.getName()) != null) {
								logger.removeAppender(appender.getName());
								logger.addAppender(async);
							}
						}
						
						if(LogManager.getRootLogger().getAppender(appender.getName()) != null) {
							LogManager.getRootLogger().removeAppender(appender.getName());
							LogManager.getRootLogger().addAppender(async);
						}

						// Logger.getRootLogger().removeAppender(asyncAppender);
						// Logger.getRootLogger().addAppender(asyncAppender);
						//
						// //remove the appender from the root logger so we don't have 2 appenders write to the same file.
						// Logger.getRootLogger().removeAppender(appender);

					}

				}
			}
		}
	}

	private AsyncAppender createAsyncAppender() {
		AsyncAppender asyncAppender = new AsyncAppender();
		String bufferSize = FoundationLogger.log4jConfigProps.getProperty(FoundationLoggerConstants.Foundation_ASYNC_BUFFER_SIZE.toString(), "2500");
		try {
			asyncAppender.setBufferSize(Integer.parseInt(bufferSize));
		} catch (NumberFormatException e) {
			asyncAppender.setBufferSize(2500);
		}

	//	updateDispatcher(asyncAppender);

		return asyncAppender;
	}

//	private void updateDispatcher(AsyncAppender asyncAppender) {
//		try {
//			String fieldName = "dispatcher";
//			Field dispatcherField = getField(fieldName);
//			Thread thread = (Thread) dispatcherField.get(asyncAppender);
//			if (thread != null) {
//				thread.stop();
//			}
//
//			fieldName = "buffer";
//			Field bufferField = getField(fieldName);
//
//			fieldName = "discardMap";
//			Field discardMapField = getField(fieldName);
//
//			fieldName = "appenders";
//			Field appendersField = getField(fieldName);
//
//			FoundationLoggingDispatcher dispatcher = new FoundationLoggingDispatcher(asyncAppender, (List<LoggingEvent>) bufferField.get(asyncAppender), (Map<String, DiscardSummary>) discardMapField.get(asyncAppender), (AppenderAttachableImpl) appendersField.get(asyncAppender));
//			Thread dispatcherThread = new Thread(dispatcher);
//			dispatcherThread.setDaemon(true);
//
//			dispatcherThread.setName("AsyncAppender-Dispatcher-" + dispatcherThread.getName());
//			dispatcherThread.start();
//			
//			dispatcherField.set(asyncAppender, dispatcherThread);
//			
//		} catch (IllegalArgumentException e) {
//			throw new IllegalArgumentException(e);
//		} catch (NoSuchFieldException e) {
//			throw new IllegalArgumentException(e);
//		} catch (IllegalAccessException e) {
//			throw new IllegalArgumentException(e);
//		}
//	}

//	private Field getField(String fieldName) throws NoSuchFieldException {
//		Field dispatcherField = AsyncAppender.class.getDeclaredField(fieldName);
//		dispatcherField.setAccessible(true);
//		return dispatcherField;
//	}
/*does not needed anymore since we create an Async appender every time.
	private boolean isAsyncAppenderClosed() {
		// no other way than use reflection to validate the state of the appender.
		// NOTE: this code will break if java SecurityManager is used.
		try {
			Field closedField = AppenderSkeleton.class.getDeclaredField("closed");
			closedField.setAccessible(true);
			boolean closed = (Boolean) closedField.get(asyncAppender);

			return closed;
		} catch (Exception e) {
			System.err.println("Cannot check AsyncAppender state. Error is: " + e);
			// assume closed in case of error.
			return true;
		}
	}
*/
	/**
	 * @param timeSizeRollingAppender
	 */
	private void updateArchivingSupport(final FoundationFileRollingAppender timeSizeRollingAppender) {

		boolean shouldArchive = true;

		final String FoundationFileArchiveKey = FoundationLoggerConstants.Foundation_FILE_ARCHIVE.toString();

		if (FoundationLogger.log4jConfigProps != null && FoundationLogger.log4jConfigProps.containsKey(FoundationFileArchiveKey)) {
			shouldArchive = Boolean.valueOf(FoundationLogger.log4jConfigProps.getProperty(FoundationFileArchiveKey));
		}

		if (shouldArchive) {
			if (OperatingSystem.getOperatingSystem().equals(OperatingSystem.Windows)) {
				timeSizeRollingAppender.setCompressionAlgorithm(FoundationLoggerConstants.ZIP.toString());
			} else {
				timeSizeRollingAppender.setCompressionAlgorithm(FoundationLoggerConstants.GZIP.toString());
			}
		}
	}

	/**
	 * Set default values for the TimeAndSizeRollingAppender appender
	 * 
	 * @param appender
	 */
	private void updateDefaultTimeAndSizeRollingAppender(final FoundationFileRollingAppender appender) {

		if (appender.getDatePattern().trim().length() == 0) {
			appender.setDatePattern(FoundationLoggerConstants.DEFAULT_DATE_PATTERN.toString());
		}
		
		String maxFileSizeKey = "log4j.appender."+appender.getName()+".MaxFileSize";
		appender.setMaxFileSize(FoundationLogger.log4jConfigProps.getProperty(maxFileSizeKey, FoundationLoggerConstants.Foundation_MAX_FILE_SIZE.toString()));

//		if (appender.getMaxFileSize() == null || appender.getMaxFileSize().equals(FoundationLoggerConstants.DEFAULT_FILE_SIZE.toString())) {
//			appender.setMaxFileSize(FoundationLoggerConstants.Foundation_MAX_FILE_SIZE.toString());
//		}

		String maxRollCountKey = "log4j.appender."+appender.getName()+".MaxRollFileCount";
		appender.setMaxRollFileCount(Integer.parseInt(FoundationLogger.log4jConfigProps.getProperty(maxRollCountKey,"100")));
	}

	private void updateDefaultLayout(final Appender appender) {
		Layout layout = appender.getLayout();

		if (layout == null) {
			layout = new FoundationLoggingPatternLayout();
			appender.setLayout(layout);
		}

		if (layout instanceof EnhancedPatternLayout) {

			final EnhancedPatternLayout enhancedPatternLayout = (EnhancedPatternLayout) layout;
			String conversionPattern = enhancedPatternLayout.getConversionPattern();

			if (shouldUpdatePattern(conversionPattern)) {
				conversionPattern = FoundationLoggerConstants.DEFAULT_CONV_PATTERN.toString();
				enhancedPatternLayout.setConversionPattern(conversionPattern);
			}

		} else if (layout instanceof PatternLayout) {

			final PatternLayout patternLayout = (PatternLayout) layout;
			String conversionPattern = patternLayout.getConversionPattern();

			if (shouldUpdatePattern(conversionPattern)) {
				conversionPattern = FoundationLoggerConstants.DEFAULT_CONV_PATTERN.toString();
				patternLayout.setConversionPattern(conversionPattern);
			}

		}
	}

	private boolean shouldUpdatePattern(final String conversionPattern) {
		return conversionPattern == null || conversionPattern.equals(FoundationLoggingPatternLayout.DEFAULT_CONVERSION_PATTERN) || conversionPattern.equals(PatternLayout.DEFAULT_CONVERSION_PATTERN) || conversionPattern.equals("");
	}

	/**
	 * Not implemented by this listener.
	 * 
	 * @see org.apache.log4j.spi.HierarchyEventListener#removeAppenderEvent(org.apache.log4j.Category, org.apache.log4j.Appender)
	 * 
	 */
	public void removeAppenderEvent(final Category cat, final Appender appender) {
		// Empty method
	}

}
