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

import com.cisco.oss.foundation.flowcontext.FlowContextFactory;
import com.cisco.oss.foundation.logging.structured.AbstractFoundationLoggingMarker;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.*;
import org.apache.log4j.helpers.Loader;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.nt.NTEventLogAppender;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.RepositorySelector;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.slf4j.Marker;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * This Logger implementation is used for applying the Foundation Logging standards on
 * top of log4j. The logger is initialized by the the FoundationLogHierarcy Object. It
 * is not meant to be used in code by developers. In runtime they will use this
 * Logger if they specify the following line in thier log4j.properties file:
 * log4j.loggerFactory=com.nds.foundation.infra.logging.FoundationLogFactory
 * 
 * @author Yair Ogen
 */
class FoundationLogger extends Logger implements org.slf4j.Logger { // NOPMD
	/**
	 * the property key for reloading the log4j properties file.
	 */
	private static final String Foundation_FILE_RELOAD_DELAY = "FoundationfileReloadDelay";
	/**
	 * default delay value for reloading the log4j properties file.
	 */
	private static final int FILE_RELOAD_DELAY = 10000;

	static Properties log4jConfigProps = null; // NOPMD

	private static final String DEFAULT_CONFIGURATION_FILE = "log4j.properties"; // NOPMD

	private static final String DEFAULT_CONFIGURATION_KEY = "log4j.configuration"; // NOPMD
	
	
	private static final String FQCN = FoundationLogger.class.getName();
	private static final String PATTERN_KEY = "messagePattern";

	public static Map<String, Map<String, Layout>> markerAppendersMap = new HashMap<String, Map<String, Layout>>();

	/**
	 * Boolean indicating whether or not NTEventLogAppender is supported.
	 */
	private static boolean ntEventLogSupported = true;

	FoundationLogger(final String name) {
		super(name);
	}

	/**
	 * Initialize that Foundation Logging library.
	 */
	static void init() {// NOPMD

		determineIfNTEventLogIsSupported();

		URL resource = null;

		final String configurationOptionStr = OptionConverter.getSystemProperty(DEFAULT_CONFIGURATION_KEY, null);

		if (configurationOptionStr != null) {
			try {
				resource = new URL(configurationOptionStr);
			} catch (MalformedURLException ex) {
				// so, resource is not a URL:
				// attempt to get the resource from the class path
				resource = Loader.getResource(configurationOptionStr);
			}
		}
		if (resource == null) {
			resource = Loader.getResource(DEFAULT_CONFIGURATION_FILE); // NOPMD
		}

		if (resource == null) {
			System.err.println("[FoundationLogger] Can not find resource: " + DEFAULT_CONFIGURATION_FILE); // NOPMD
			throw new FoundationIOException("Can not find resource: " + DEFAULT_CONFIGURATION_FILE); // NOPMD
		}

		// update the log manager to use the Foundation repository.
		final RepositorySelector foundationRepositorySelector = new FoundationRepositorySelector(FoundationLogFactory.foundationLogHierarchy);
		LogManager.setRepositorySelector(foundationRepositorySelector, null);

		// set logger to info so we always want to see these logs even if root
		// is set to ERROR.
		final Logger logger = getLogger(FoundationLogger.class);

		final String logPropFile = resource.getPath();
		log4jConfigProps = getLogProperties(resource);
		
		// select and configure again so the loggers are created with the right
		// level after the repository selector was updated.
		OptionConverter.selectAndConfigure(resource, null, FoundationLogFactory.foundationLogHierarchy);

		// start watching for property changes
		setUpPropFileReloading(logger, logPropFile, log4jConfigProps);

		// add syslog appender or windows event viewer appender
//		setupOSSystemLog(logger, log4jConfigProps);

		// parseMarkerPatterns(log4jConfigProps);
		// parseMarkerPurePattern(log4jConfigProps);
		udpateMarkerStructuredLogOverrideMap(logger);

//		updateRMISniffingLoggersLevel();

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					AbstractFoundationLoggingMarker.scanClassPathForFormattingAnnotations();
				} catch (Exception e) {
					logger.error("Problem parsing marker annotations. Error is: " + e, e);
				}

			}
		}).start();

        setupJULSupport(resource);

    }

    private static void setupJULSupport(URL resource) {
        boolean julSupportEnabled = Boolean.valueOf(log4jConfigProps.getProperty(FoundationLoggerConstants.Foundation_JUL_SUPPORT_ENABLED.toString(), "false"));
        if (julSupportEnabled) {
            String appenderRef = log4jConfigProps.getProperty(FoundationLoggerConstants.Foundation_JUL_APPENDER_REF.toString());

            if (StringUtils.isBlank(appenderRef)) {

                Enumeration allAppenders = Logger.getRootLogger().getAllAppenders();

                while (allAppenders.hasMoreElements()) {

                    Appender appender = (Appender) allAppenders.nextElement();

                    if (appender instanceof FileAppender) {
                        appenderRef = appender.getName();
                        getLogger(FoundationLogger.class).info("*** Using '" + appenderRef + "' as the Java util logging appender ref ***");
                        System.err.println("*** Using '" + appenderRef + "' as the Java util logging appender ref ***");
                        break;
                    }
                }
            }

            if (StringUtils.isBlank(appenderRef)) {
                throw new IllegalArgumentException("Java util support was enabled but couldn't find a matching appender under the '" + FoundationLoggerConstants.Foundation_JUL_APPENDER_REF.toString() + "' key.");
            }


            Handler handler  = null;

            Appender appender = Logger.getRootLogger().getAppender(appenderRef);
            if(appender == null){

                Enumeration allAppenders = Logger.getRootLogger().getAllAppenders();

                while (allAppenders.hasMoreElements()){

                    Appender tempAppender = (Appender)allAppenders.nextElement();

                    if(tempAppender instanceof AsyncAppender){

                        AsyncAppender asyncAppender = (AsyncAppender)tempAppender;
                        Enumeration asyncAppenderAllAppenders = asyncAppender.getAllAppenders();

                        while (asyncAppenderAllAppenders.hasMoreElements()){

                            Appender asyncTempAppender = (Appender)asyncAppenderAllAppenders.nextElement();

                            if(appenderRef.equals(asyncTempAppender.getName())){
                                appender = asyncTempAppender;
                                break;
                            }
                        }
                        if(appender != null){
                            break;
                        }
                    }
                }
            }

            if(appender instanceof FileAppender){
                try {
                    handler = new FileHandler(((FileAppender)appender).getFile());
                } catch (IOException e) {
                    throw new IllegalArgumentException("IOException encountered when trying to setup jul logging: " + e, e);
                }
            }else if(appender instanceof ConsoleAppender){
                handler = new ConsoleHandler();
            }else{
                getLogger(FoundationLogger.class).error("got a reference to an unsupported appender: " + appenderRef);
            }


            if (handler != null) {

//                System.setProperty("java.util.logging.config.file",resource.getPath());

                java.util.logging.LogManager.getLogManager().reset();
                try {
                    java.util.logging.LogManager.getLogManager().readConfiguration(resource.openStream());
                } catch (IOException e) {
                    throw new IllegalArgumentException("IOException encountered when trying to read log4j properties file: " + e, e);
                }

                handler.setLevel(java.util.logging.Level.FINEST);
                handler.setFormatter(new FoundationLogFormatter());

                java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
                rootLogger.addHandler(handler);
                rootLogger.setLevel(java.util.logging.Level.SEVERE);


                Properties julLoggerSubset = getPropertiesSubset("jul.logger");
                if(!julLoggerSubset.isEmpty()){
                    Set<Object> keySet = julLoggerSubset.keySet();
                    for (Object key : keySet) {
                        java.util.logging.Logger logger = java.util.logging.Logger.getLogger((String)key);
                        logger.setLevel(java.util.logging.Level.parse((String)julLoggerSubset.get(key)));
                    }
                }
            }


        }
    }

    private static Properties getPropertiesSubset(String prefix) {
        Properties subset = new Properties();
        Enumeration<Object> keys = log4jConfigProps.keys();
        boolean validSubset = false;

        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();

            if (key instanceof String && ((String) key).startsWith(prefix)) {
                if (!validSubset) {
                    validSubset = true;
                }

                /*
                 * Check to make sure that subset.subset(prefix) doesn't
                 * blow up when there is only a single property
                 * with the key prefix. This is not a useful
                 * subset but it is a valid subset.
                 */
                String newKey = null;
                if (((String) key).length() == prefix.length()) {
                    newKey = prefix;
                } else {
                    newKey = ((String) key).substring(prefix.length() + 1);
                }

                /*
                 *  use addPropertyDirect() - this will plug the data as
                 *  is into the Map, but will also do the right thing
                 *  re key accounting
                 */
                subset.setProperty(newKey, (String) log4jConfigProps.get(key));
            }
        }

        if (validSubset) {
            return subset;
        } else {
            return new Properties();
        }
    }


	private static void udpateMarkerStructuredLogOverrideMap(Logger logger) {

		InputStream messageFormatIS = FoundationLogger.class.getResourceAsStream("/messageFormat.xml");
		if (messageFormatIS == null) {
			logger.debug("file messageformat.xml not found in classpath");
		} else {
			try {
				SAXBuilder builder = new SAXBuilder();
				Document document = builder.build(messageFormatIS);

				messageFormatIS.close();

				Element rootElement = document.getRootElement();
				List<Element> markers = rootElement.getChildren("marker");
				for (Element marker : markers) {
					AbstractFoundationLoggingMarker.markersXmlMap.put(marker.getAttributeValue("id"), marker);
				}

			} catch (Exception e) {
				logger.error("cannot load the structured log override file. error is: " + e, e);
				throw new IllegalArgumentException("Problem parsing messageformat.xml", e);
			}
		}

	}

	private static void parseMarkerPatterns(Properties properties) {

		Set<String> markerMappingKeySet = new HashSet<String>();

		Set<String> entrySet = properties.stringPropertyNames();
		for (String key : entrySet) {
			if (key.startsWith("logevent")) {
				markerMappingKeySet.add(key);
			}
		}

		for (String key : markerMappingKeySet) {

			String[] split = key.split("\\.");
			if (split.length != 4) {
				throw new IllegalArgumentException("the key " + key + " does not contain a four part mapping.");
			}

			String markerName = split[1];
			String appenderName = split[2];
			String pattern = properties.getProperty(key);

			if (markerAppendersMap.get(markerName) == null) {
				markerAppendersMap.put(markerName, new HashMap<String, Layout>());
			}

			Map<String, Layout> markerAppenderMap = markerAppendersMap.get(markerName);
			Layout patternLayout = new FoundationLoggingPatternLayout(pattern);
			markerAppenderMap.put(appenderName, patternLayout);

		}

	}

	/**
     *
     */
//	private static void updateRMISniffingLoggersLevel() {
//		Logger rmiSnifferClientLogger = getLogger("com.nds.foundation.infra.highavailability.InfraRmiProxyFactoryBean_Sniffer");
//		rmiSnifferClientLogger.setLevel(Level.TRACE);
//
//		Logger rmiSnifferServerLogger = getLogger("com.nds.foundation.infra.highavailability.rmi.exporter.InfraRmiServiceExporter_Sniffer");
//		rmiSnifferServerLogger.setLevel(Level.TRACE);
//	}

	private static void determineIfNTEventLogIsSupported() {
		boolean supported = true;
		try {
			new NTEventLogAppender();
		} catch (Throwable t) {// NOPMD
			supported = false;
		}

		ntEventLogSupported = supported;
	}

//	private static void setupOSSystemLog(final Logger logger, final Properties log4jConfigProps) {
//
//		final Layout layout = new FoundationLoggingPatternLayout(FondationLoggerConstants.DEFAULT_CONV_PATTERN.toString());
//		final Logger rootLogger = LogManager.getRootLogger();
//
//		final OperatingSystem operatingSystem = OperatingSystem.getOperatingSystem();
//		AppenderSkeleton systemLogAppender = null;
//
//		Level defaultThreshold = Level.WARN;
//		if (log4jConfigProps != null && log4jConfigProps.getProperty("FoundationdefaultSystemLoggerThreshold") != null) {
//			defaultThreshold = Level.toLevel(log4jConfigProps.getProperty("FoundationdefaultSystemLoggerThreshold"));
//		}
//
//		if (operatingSystem.equals(OperatingSystem.Windows) && ntEventLogSupported) {
//
//			systemLogAppender = new NTEventLogAppender("Foundation Logging", layout);
//			systemLogAppender.setName("nteventlog");
//			systemLogAppender.setThreshold(defaultThreshold);
//			systemLogAppender.activateOptions();
//
//			rootLogger.addAppender(systemLogAppender);
//
//		} else if (operatingSystem.equals(OperatingSystem.HPUX) || operatingSystem.equals(OperatingSystem.Linux)) {
//
//			systemLogAppender = new SyslogAppender(layout, "localhost", SyslogAppender.LOG_USER);
//			systemLogAppender.setName("systemlog");
//			systemLogAppender.setThreshold(defaultThreshold);
//			systemLogAppender.activateOptions();
//
//			rootLogger.addAppender(systemLogAppender);
//		}
//		if (systemLogAppender == null) {
//			logger.error("System log appender was not initialized! Probably \"NTEventLogAppender.dll\" is not in the computer path.");
//		}
//
//	}

	private static void setUpPropFileReloading(final Logger logger, final String logPropFile, final Properties properties) {

		int fileReloadDelay = FILE_RELOAD_DELAY;
		if (properties.containsKey(Foundation_FILE_RELOAD_DELAY)) {
			final String fileReloadDelayStr = properties.getProperty(Foundation_FILE_RELOAD_DELAY);
			try {
				fileReloadDelay = Integer.parseInt(fileReloadDelayStr);
			} catch (NumberFormatException e) {
				logger.error("Can not format to integer the property: " + Foundation_FILE_RELOAD_DELAY + ". using default of: " + FILE_RELOAD_DELAY);
			}
		}

		PropertyConfigurator.configureAndWatch(logPropFile, fileReloadDelay);
	}

	private static Properties getLogProperties(final URL logPropFileResource) {

		final Properties properties = new Properties();
		InputStream propertiesInStream = null;
		final String log4jFilePath = logPropFileResource.getPath();
		try {
			propertiesInStream = logPropFileResource.openStream();
			properties.load(propertiesInStream);
		} catch (FileNotFoundException e) {
			System.err.println("[FoundationLogger] Can not find the file: " + log4jFilePath); // NOPMD
			throw new FoundationIOException("Can not find the file: " + log4jFilePath, e);
		} catch (IOException e) {
			System.err.println("[FoundationLogger] IO Exception during load of file: " + log4jFilePath + ". Exception is: " + e.toString()); // NOPMD
			throw new FoundationIOException("IO Exception during load of file: " + log4jFilePath + ". Exception is: " + e.toString(), e);
		} finally {
			if (propertiesInStream != null) {
				try {
					propertiesInStream.close();
				} catch (IOException e) {
					System.err.println("[FoundationLogger] IO Exception during close of file: " + log4jFilePath + ". Exception is: " + e.toString()); // NOPMD
				}
			}
		}
		return properties;
	}

	/**
	 * Log a message object at level TRACE.
	 * 
	 * @param msg
	 *            - the message object to be logged
	 */
	public void trace(String msg) {
		log(FQCN, Level.TRACE, msg, null);
	}

	/**
	 * Log a message at level TRACE according to the specified format and
	 * argument.
	 * 
	 * <p>
	 * This form avoids superfluous object creation when the logger is disabled
	 * for level TRACE.
	 * </p>
	 * 
	 * @param format
	 *            the format string
	 * @param arg
	 *            the argument
	 */
	public void trace(String format, Object arg) {
		if (isTraceEnabled()) {
			FormattingTuple ft = MessageFormatter.format(format, arg);
			log(FQCN, Level.TRACE, ft.getMessage(), ft.getThrowable());
		}
	}

	/**
	 * Log a message at level TRACE according to the specified format and
	 * arguments.
	 * 
	 * <p>
	 * This form avoids superfluous object creation when the logger is disabled
	 * for the TRACE level.
	 * </p>
	 * 
	 * @param format
	 *            the format string
	 * @param arg1
	 *            the first argument
	 * @param arg2
	 *            the second argument
	 */
	public void trace(String format, Object arg1, Object arg2) {
		if (isTraceEnabled()) {
			FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
			log(FQCN, Level.TRACE, ft.getMessage(), ft.getThrowable());
		}
	}

	/**
	 * Log a message at level TRACE according to the specified format and
	 * arguments.
	 * 
	 * <p>
	 * This form avoids superfluous object creation when the logger is disabled
	 * for the TRACE level.
	 * </p>
	 * 
	 * @param format
	 *            the format string
	 * @param argArray
	 *            an array of arguments
	 */
	public void trace(String format, Object[] argArray) {
		if (isTraceEnabled()) {
			FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
			log(FQCN, Level.TRACE, ft.getMessage(), ft.getThrowable());
		}
	}

	/**
	 * Log an exception (throwable) at level TRACE with an accompanying message.
	 * 
	 * @param msg
	 *            the message accompanying the exception
	 * @param t
	 *            the exception (throwable) to log
	 */
	public void trace(String msg, Throwable t) {
		log(FQCN, Level.TRACE, msg, t);
	}

	// /**
	// * Is this logger instance enabled for the DEBUG level?
	// *
	// * @return True if this Logger is enabled for level DEBUG, false
	// otherwise.
	// */
	// public boolean isDebugEnabled() {
	// return super.isDebugEnabled();
	// }

	/**
	 * Log a message object at level DEBUG.
	 * 
	 * @param msg
	 *            - the message object to be logged
	 */
	public void debug(String msg) {
		log(FQCN, Level.DEBUG, msg, null);
	}

	/**
	 * Log a message at level DEBUG according to the specified format and
	 * argument.
	 * 
	 * <p>
	 * This form avoids superfluous object creation when the logger is disabled
	 * for level DEBUG.
	 * </p>
	 * 
	 * @param format
	 *            the format string
	 * @param arg
	 *            the argument
	 */
	public void debug(String format, Object arg) {
		if (isDebugEnabled()) {
			FormattingTuple ft = MessageFormatter.format(format, arg);
			log(FQCN, Level.DEBUG, ft.getMessage(), ft.getThrowable());
		}
	}

	/**
	 * Log a message at level DEBUG according to the specified format and
	 * arguments.
	 * 
	 * <p>
	 * This form avoids superfluous object creation when the logger is disabled
	 * for the DEBUG level.
	 * </p>
	 * 
	 * @param format
	 *            the format string
	 * @param arg1
	 *            the first argument
	 * @param arg2
	 *            the second argument
	 */
	public void debug(String format, Object arg1, Object arg2) {
		if (isDebugEnabled()) {
			FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
			log(FQCN, Level.DEBUG, ft.getMessage(), ft.getThrowable());
		}
	}

	/**
	 * Log a message at level DEBUG according to the specified format and
	 * arguments.
	 * 
	 * <p>
	 * This form avoids superfluous object creation when the logger is disabled
	 * for the DEBUG level.
	 * </p>
	 * 
	 * @param format
	 *            the format string
	 * @param argArray
	 *            an array of arguments
	 */
	public void debug(String format, Object[] argArray) {
		if (isDebugEnabled()) {
			FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
			log(FQCN, Level.DEBUG, ft.getMessage(), ft.getThrowable());
		}
	}

	/**
	 * Log an exception (throwable) at level DEBUG with an accompanying message.
	 * 
	 * @param msg
	 *            the message accompanying the exception
	 * @param t
	 *            the exception (throwable) to log
	 */
	public void debug(String msg, Throwable t) {
		log(FQCN, Level.DEBUG, msg, t);
	}

	// /**
	// * Is this logger instance enabled for the INFO level?
	// *
	// * @return True if this Logger is enabled for the INFO level, false
	// otherwise.
	// */
	// public boolean isInfoEnabled() {
	// return super.isInfoEnabled();
	// }

	/**
	 * Log a message object at the INFO level.
	 * 
	 * @param msg
	 *            - the message object to be logged
	 */
	public void info(String msg) {
		log(FQCN, Level.INFO, msg, null);
	}

	/**
	 * Log a message at level INFO according to the specified format and
	 * argument.
	 * 
	 * <p>
	 * This form avoids superfluous object creation when the logger is disabled
	 * for the INFO level.
	 * </p>
	 * 
	 * @param format
	 *            the format string
	 * @param arg
	 *            the argument
	 */
	public void info(String format, Object arg) {
		if (isInfoEnabled()) {
			FormattingTuple ft = MessageFormatter.format(format, arg);
			log(FQCN, Level.INFO, ft.getMessage(), ft.getThrowable());
		}
	}

	/**
	 * Log a message at the INFO level according to the specified format and
	 * arguments.
	 * 
	 * <p>
	 * This form avoids superfluous object creation when the logger is disabled
	 * for the INFO level.
	 * </p>
	 * 
	 * @param format
	 *            the format string
	 * @param arg1
	 *            the first argument
	 * @param arg2
	 *            the second argument
	 */
	public void info(String format, Object arg1, Object arg2) {
		if (isInfoEnabled()) {
			FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
			log(FQCN, Level.INFO, ft.getMessage(), ft.getThrowable());
		}
	}

	/**
	 * Log a message at level INFO according to the specified format and
	 * arguments.
	 * 
	 * <p>
	 * This form avoids superfluous object creation when the logger is disabled
	 * for the INFO level.
	 * </p>
	 * 
	 * @param format
	 *            the format string
	 * @param argArray
	 *            an array of arguments
	 */
	public void info(String format, Object[] argArray) {
		if (isInfoEnabled()) {
			FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
			log(FQCN, Level.INFO, ft.getMessage(), ft.getThrowable());
		}
	}

	/**
	 * Log an exception (throwable) at the INFO level with an accompanying
	 * message.
	 * 
	 * @param msg
	 *            the message accompanying the exception
	 * @param t
	 *            the exception (throwable) to log
	 */
	public void info(String msg, Throwable t) {
		log(FQCN, Level.INFO, msg, t);
	}

	/**
	 * Is this logger instance enabled for the WARN level?
	 * 
	 * @return True if this Logger is enabled for the WARN level, false
	 *         otherwise.
	 */
	public boolean isWarnEnabled() {
		return super.isEnabledFor(Level.WARN);
	}

	/**
	 * Log a message object at the WARN level.
	 * 
	 * @param msg
	 *            - the message object to be logged
	 */
	public void warn(String msg) {
		log(FQCN, Level.WARN, msg, null);
	}

	/**
	 * Log a message at the WARN level according to the specified format and
	 * argument.
	 * 
	 * <p>
	 * This form avoids superfluous object creation when the logger is disabled
	 * for the WARN level.
	 * </p>
	 * 
	 * @param format
	 *            the format string
	 * @param arg
	 *            the argument
	 */
	public void warn(String format, Object arg) {
		if (isEnabledFor(Level.WARN)) {
			FormattingTuple ft = MessageFormatter.format(format, arg);
			log(FQCN, Level.WARN, ft.getMessage(), ft.getThrowable());
		}
	}

	/**
	 * Log a message at the WARN level according to the specified format and
	 * arguments.
	 * 
	 * <p>
	 * This form avoids superfluous object creation when the logger is disabled
	 * for the WARN level.
	 * </p>
	 * 
	 * @param format
	 *            the format string
	 * @param arg1
	 *            the first argument
	 * @param arg2
	 *            the second argument
	 */
	public void warn(String format, Object arg1, Object arg2) {
		if (isEnabledFor(Level.WARN)) {
			FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
			log(FQCN, Level.WARN, ft.getMessage(), ft.getThrowable());
		}
	}

	/**
	 * Log a message at level WARN according to the specified format and
	 * arguments.
	 * 
	 * <p>
	 * This form avoids superfluous object creation when the logger is disabled
	 * for the WARN level.
	 * </p>
	 * 
	 * @param format
	 *            the format string
	 * @param argArray
	 *            an array of arguments
	 */
	public void warn(String format, Object[] argArray) {
		if (isEnabledFor(Level.WARN)) {
			FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
			log(FQCN, Level.WARN, ft.getMessage(), ft.getThrowable());
		}
	}

	/**
	 * Log an exception (throwable) at the WARN level with an accompanying
	 * message.
	 * 
	 * @param msg
	 *            the message accompanying the exception
	 * @param t
	 *            the exception (throwable) to log
	 */
	public void warn(String msg, Throwable t) {
		log(FQCN, Level.WARN, msg, t);
	}

	/**
	 * Is this logger instance enabled for level ERROR?
	 * 
	 * @return True if this Logger is enabled for level ERROR, false otherwise.
	 */
	public boolean isErrorEnabled() {
		return super.isEnabledFor(Level.ERROR);
	}

	/**
	 * Log a message object at the ERROR level.
	 * 
	 * @param msg
	 *            - the message object to be logged
	 */
	public void error(String msg) {
		log(FQCN, Level.ERROR, msg, null);
	}

	/**
	 * Log a message at the ERROR level according to the specified format and
	 * argument.
	 * 
	 * <p>
	 * This form avoids superfluous object creation when the logger is disabled
	 * for the ERROR level.
	 * </p>
	 * 
	 * @param format
	 *            the format string
	 * @param arg
	 *            the argument
	 */
	public void error(String format, Object arg) {
		if (isEnabledFor(Level.ERROR)) {
			FormattingTuple ft = MessageFormatter.format(format, arg);
			log(FQCN, Level.ERROR, ft.getMessage(), ft.getThrowable());
		}
	}

	/**
	 * Log a message at the ERROR level according to the specified format and
	 * arguments.
	 * 
	 * <p>
	 * This form avoids superfluous object creation when the logger is disabled
	 * for the ERROR level.
	 * </p>
	 * 
	 * @param format
	 *            the format string
	 * @param arg1
	 *            the first argument
	 * @param arg2
	 *            the second argument
	 */
	public void error(String format, Object arg1, Object arg2) {
		if (isEnabledFor(Level.ERROR)) {
			FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
			log(FQCN, Level.ERROR, ft.getMessage(), ft.getThrowable());
		}
	}

	/**
	 * Log a message at level ERROR according to the specified format and
	 * arguments.
	 * 
	 * <p>
	 * This form avoids superfluous object creation when the logger is disabled
	 * for the ERROR level.
	 * </p>
	 * 
	 * @param format
	 *            the format string
	 * @param argArray
	 *            an array of arguments
	 */
	public void error(String format, Object[] argArray) {
		if (isEnabledFor(Level.ERROR)) {
			FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
			log(FQCN, Level.ERROR, ft.getMessage(), ft.getThrowable());
		}
	}

	/**
	 * Log an exception (throwable) at the ERROR level with an accompanying
	 * message.
	 * 
	 * @param msg
	 *            the message accompanying the exception
	 * @param t
	 *            the exception (throwable) to log
	 */
	public void error(String msg, Throwable t) {
		log(FQCN, Level.ERROR, msg, t);
	}

	// @Override
	// protected void forcedLog(final String fqcn, final Priority level, final
	// Object message, final Throwable throwable) {
	// //update the relevant category in the MDC. when no category was specified
	// use Log as default.
	// if (message instanceof MessageWrapper) {
	// MDC.put(FondationLoggerConstants.CAT_TERM.toString(),
	// ((MessageWrapper)message).getCategoryTerm());
	// }else{
	// CategoryTerm catTerm = CategoryTerm.Library;
	// if (getName().startsWith("com.nds")) {
	// catTerm = CategoryTerm.Log;
	// }
	// MDC.put(FondationLoggerConstants.CAT_TERM.toString(), catTerm);
	// }
	// super.forcedLog(fqcn, level, message, throwable);
	// }
	//

	@Override
	public boolean isTraceEnabled(Marker marker) {
		return isTraceEnabled();
	}

	@Override
	public void trace(Marker marker, String msg) {
		log(marker, FQCN, Level.TRACE, msg, null);

	}

	@Override
	public void trace(Marker marker, String format, Object arg) {
		if (isTraceEnabled()) {
			FormattingTuple ft = MessageFormatter.format(format, arg);
			log(marker, FQCN, Level.TRACE, ft.getMessage(), ft.getThrowable());
		}
	}

	@Override
	public void trace(Marker marker, String format, Object arg1, Object arg2) {
		if (isTraceEnabled()) {
			FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
			log(marker, FQCN, Level.TRACE, ft.getMessage(), ft.getThrowable());
		}

	}

	@Override
	public void trace(Marker marker, String format, Object[] argArray) {
		if (isTraceEnabled()) {
			FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
			log(marker, FQCN, Level.TRACE, ft.getMessage(), ft.getThrowable());
		}

	}

	@Override
	public void trace(Marker marker, String msg, Throwable t) {
		log(marker, FQCN, Level.TRACE, msg, t);

	}

	@Override
	public boolean isDebugEnabled(Marker marker) {
		return isDebugEnabled();
	}

	@Override
	public void debug(Marker marker, String msg) {
		log(marker, FQCN, Level.DEBUG, msg, null);

	}

	@Override
	public void debug(Marker marker, String format, Object arg) {
		if (isDebugEnabled()) {
			FormattingTuple ft = MessageFormatter.format(format, arg);
			log(marker, FQCN, Level.DEBUG, ft.getMessage(), ft.getThrowable());
		}

	}

	@Override
	public void debug(Marker marker, String format, Object arg1, Object arg2) {
		if (isDebugEnabled()) {
			FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
			log(marker, FQCN, Level.DEBUG, ft.getMessage(), ft.getThrowable());
		}

	}

	@Override
	public void debug(Marker marker, String format, Object[] argArray) {
		if (isDebugEnabled()) {
			FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
			log(marker, FQCN, Level.DEBUG, ft.getMessage(), ft.getThrowable());
		}

	}

	@Override
	public void debug(Marker marker, String msg, Throwable t) {
		log(marker, FQCN, Level.DEBUG, msg, t);

	}

	@Override
	public boolean isInfoEnabled(Marker marker) {
		return isInfoEnabled();
	}

	@Override
	public void info(Marker marker, String msg) {
		log(marker, FQCN, Level.INFO, msg, null);

	}

	@Override
	public void info(Marker marker, String format, Object arg) {
		if (isInfoEnabled()) {
			FormattingTuple ft = MessageFormatter.format(format, arg);
			log(marker, FQCN, Level.INFO, ft.getMessage(), ft.getThrowable());
		}

	}

	@Override
	public void info(Marker marker, String format, Object arg1, Object arg2) {
		if (isInfoEnabled()) {
			FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
			log(marker, FQCN, Level.INFO, ft.getMessage(), ft.getThrowable());
		}

	}

	@Override
	public void info(Marker marker, String format, Object[] argArray) {
		if (isInfoEnabled()) {
			FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
			log(marker, FQCN, Level.INFO, ft.getMessage(), ft.getThrowable());
		}

	}

	@Override
	public void info(Marker marker, String msg, Throwable t) {
		log(marker, FQCN, Level.INFO, msg, t);

	}

	@Override
	public boolean isWarnEnabled(Marker marker) {
		return isWarnEnabled();
	}

	@Override
	public void warn(Marker marker, String msg) {
		log(marker, FQCN, Level.WARN, msg, null);

	}

	@Override
	public void warn(Marker marker, String format, Object arg) {
		if (isEnabledFor(Level.WARN)) {
			FormattingTuple ft = MessageFormatter.format(format, arg);
			log(marker, FQCN, Level.WARN, ft.getMessage(), ft.getThrowable());
		}

	}

	@Override
	public void warn(Marker marker, String format, Object arg1, Object arg2) {
		if (isEnabledFor(Level.WARN)) {
			FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
			log(marker, FQCN, Level.WARN, ft.getMessage(), ft.getThrowable());
		}

	}

	@Override
	public void warn(Marker marker, String format, Object[] argArray) {
		if (isEnabledFor(Level.WARN)) {
			FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
			log(marker, FQCN, Level.WARN, ft.getMessage(), ft.getThrowable());
		}

	}

	@Override
	public void warn(Marker marker, String msg, Throwable t) {
		log(marker, FQCN, Level.WARN, msg, t);

	}

	@Override
	public boolean isErrorEnabled(Marker marker) {
		return isErrorEnabled();
	}

	@Override
	public void error(Marker marker, String msg) {
		log(marker, FQCN, Level.ERROR, msg, null);

	}

	@Override
	public void error(Marker marker, String format, Object arg) {
		if (isEnabledFor(Level.ERROR)) {
			FormattingTuple ft = MessageFormatter.format(format, arg);
			log(marker, FQCN, Level.ERROR, ft.getMessage(), ft.getThrowable());
		}

	}

	@Override
	public void error(Marker marker, String format, Object arg1, Object arg2) {
		if (isEnabledFor(Level.ERROR)) {
			FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
			log(marker, FQCN, Level.ERROR, ft.getMessage(), ft.getThrowable());
		}

	}

	@Override
	public void error(Marker marker, String format, Object[] argArray) {
		if (isEnabledFor(Level.ERROR)) {
			FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
			log(marker, FQCN, Level.ERROR, ft.getMessage(), ft.getThrowable());
		}

	}

	@Override
	public void error(Marker marker, String msg, Throwable t) {
		log(marker, FQCN, Level.ERROR, msg, t);

	}

	public void log(Marker marker, String callerFQCN, Priority level, Object message, Throwable t) {
		if (repository.isDisabled(level.toInt())) {
			return;
		}
		if (level.isGreaterOrEqual(this.getEffectiveLevel())) {
			forcedLog(marker, callerFQCN, level, message, t);
		}
	}

	/**
	 * This method creates a new logging event and logs the event without
	 * further checks.
	 */
	protected void forcedLog(Marker marker, String fqcn, Priority level, Object message, Throwable t) {
		callAppenders(new FoundationLof4jLoggingEvent(marker, fqcn, this, level, message, t));
	}

	@Override
	public void callAppenders(LoggingEvent event) {
		int writes = 0;

		Category category = this;

		while (category != null) {

			// Protected against simultaneous call to addAppender,
			// removeAppender,...
			synchronized (category) {

				@SuppressWarnings("unchecked")
				Enumeration<Appender> allAppenders = category.getAllAppenders();

				while (allAppenders.hasMoreElements()) {
					Appender appender = allAppenders.nextElement();

					// since we may update the appender layout we must sync so
					// other threads won't use it by mistake
					synchronized (appender) {
						if(event instanceof FoundationLof4jLoggingEvent){
							appender.doAppend(new FoundationLof4jLoggingEvent((FoundationLof4jLoggingEvent) event));
						}else{
							appender.doAppend(event);
						}
					}

					writes++;
				}

				if (!category.getAdditivity()) {
					break;
				}
			}

			category = category.getParent();

		}

		if (writes == 0) {
			repository.emitNoAppenderWarning(this);
		}
	}

	private static class FoundationRepositorySelector implements RepositorySelector {

		final private LoggerRepository repository;

		public FoundationRepositorySelector(final LoggerRepository repository) {
			this.repository = repository;
		}

		@Override
		public LoggerRepository getLoggerRepository() {
			return repository;
		}

	}

    private static class FoundationLogFormatter extends java.util.logging.Formatter {

        MessageFormat messageFormat = new MessageFormat("{3} [{0}] [{2}]: {1}: {5} {4} \n");

        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendPattern("yyyy/MM/dd HH:mm:ss.SSS").toFormatter();

        @Override
        public String format(LogRecord record) {
            Object[] arguments = new Object[]{
                    truncateLoggerName(record.getLoggerName(), 1),
                    record.getLevel(),
                    Thread.currentThread().getName(),
                    (new DateTime(record.getMillis(), DateTimeZone.UTC)).toString(dateFormatter),
                    record.getMessage(),
                    FlowContextFactory.getFlowContext() == null ? "" : FlowContextFactory.getFlowContext().toString()};
            return messageFormat.format(arguments);
        }

        private String truncateLoggerName(String n, int precision) {
            String[] split = n.split("\\.");
            return split[split.length-1];
        }

    }

}
