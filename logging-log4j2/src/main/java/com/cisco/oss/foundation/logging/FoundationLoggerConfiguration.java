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

package com.cisco.oss.foundation.logging;

import com.cisco.oss.foundation.configuration.ConfigUtil;
import com.cisco.oss.foundation.configuration.ConfigurationFactory;
import com.cisco.oss.foundation.logging.appenders.FoundationRollingRandomAccessFileAppender;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.FoundationLoggerContext;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.rolling.*;
import org.apache.logging.log4j.core.config.*;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
import java.net.URL;
import java.util.*;

/**
 * Created by Yair Ogen on 17/07/2014.
 */
public class FoundationLoggerConfiguration extends AbstractConfiguration implements Reconfigurable {

    public static final FoundationLoggerConfiguration INSTANCE = new FoundationLoggerConfiguration();
//    private static final Logger LOGGER = LoggerFactory.getLogger(FoundationLoggerConfiguration.class);
    /**
     * The name of the default configuration.
     */
    public static final String DEFAULT_NAME = "FoundationLoggerConfiguration";
    private static final String DEFAULT_CONFIGURATION_FILE = "/log4j.properties"; // NOPMD
    private org.apache.commons.configuration.Configuration configuration = null;
    private boolean useCustomConfiguration = Boolean.getBoolean("foundationLogging.useCustomConfiguration");

    public Layout<? extends Serializable> getLayout() {
        return layout;
    }

    private Layout<? extends Serializable> layout;


    /**
     * Constructor to create the default configuration.
     */
    public FoundationLoggerConfiguration() {
        super(ConfigurationSource.NULL_SOURCE);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //wait a second before shutting down to allow async logging to finish
                    Thread.sleep(2000);
                } catch (InterruptedException e) {}
            }
        }));

        final Layout<? extends Serializable> layout = PatternLayout.newBuilder()
                .withPattern(FoundationLoggerConstants.DEFAULT_CONV_PATTERN.toString())
                .withConfiguration(this)
                .build();
        this.layout = layout;

        Runnable initConfig = new Runnable() {
            public void run() {
                initConfig();
                FoundationLoggerContext.POST_CONFIG_LATCH.countDown();
            }
        };
        Thread firstTimeConfigInit = new Thread(initConfig, "FirstTimeConfigInit");
        firstTimeConfigInit.start();

        setName(DEFAULT_NAME);
    }

    private void initConfig() {

        if (!useCustomConfiguration) {
            try {
                initiateLoggingFromConfiguration(layout, ConfigurationFactory.getConfiguration());
            } catch (Exception e) {
                System.err.println("ERROR: Could not initialize configuration factory using commons config. error is: " + e.toString() + ". Failing back to log4j.properties!");
                initiateLog4jPropertiesFallback(layout);
            }
        }
    }

    public void initiateLoggingFromConfiguration(Layout layout, org.apache.commons.configuration.Configuration configuration) {
        this.configuration = configuration;
        org.apache.commons.configuration.Configuration loggingConfig = configuration.subset("logging");


        if (loggingConfig.isEmpty()) {
            throw new IllegalArgumentException("logging config section is missing");
        }

        org.apache.commons.configuration.Configuration loggerConfig = loggingConfig.subset("logger");
        org.apache.commons.configuration.Configuration destinationConfig = loggingConfig.subset("destination");

        if (loggerConfig.isEmpty()) {
            throw new IllegalArgumentException("logger config section is missing");
        }

        if (destinationConfig.isEmpty()) {
            throw new IllegalArgumentException("destination config section is missing");
        }

        Map<String, Map<String, String>> loggersMap = ConfigUtil.parseComplexArrayStructure("logging.logger");
        Map<String, Map<String, String>> destinationsMap = ConfigUtil.parseComplexArrayStructure("logging.destination");


        initDestinationsFromCommon(destinationsMap, layout);
        initLoggersFromCommon(loggersMap, layout);
//        initRoot(loggerConfig);

        doConfigure();


    }

    private void initLoggersFromCommon(Map<String, Map<String, String>> loggerMap, Layout layout) {

        List<LoggerConfig> loggerConfigs = new ArrayList<LoggerConfig>();

        Set<Map.Entry<String, Map<String, String>>> entries = loggerMap.entrySet();
        for (Map.Entry<String, Map<String, String>> entry : entries) {
            String loggerGroupName = entry.getKey();
            Map<String, String> loggerEntryMap = entry.getValue();
            String levelStr = loggerEntryMap.get("level");
            String inheritParent = loggerEntryMap.get("inheritParent");
            boolean additivity = StringUtils.isNotBlank(inheritParent) ? Boolean.valueOf(inheritParent) : true;

            boolean destinationExists = loggerEntryMap.containsKey("destinations.1");
            boolean prefixesExists = loggerEntryMap.containsKey("prefix.1");

            if (prefixesExists) {

                List<Appender> appenders = new ArrayList<Appender>();

                if (destinationExists) {
                    Map<String, String> destinationsMap = ConfigUtil.parseSimpleArrayAsMap("logging.logger." + loggerGroupName + ".destinations");
                    Set<Map.Entry<String, String>> destinations = destinationsMap.entrySet();
                    for (Map.Entry<String, String> destination : destinations) {
                        String appenderName = destination.getValue();
                        Appender appender = getAppender(appenderName);
                        appenders.add(appender);
                    }
                }

                Map<String, String> prefixesMap = ConfigUtil.parseSimpleArrayAsMap("logging.logger." + loggerGroupName + ".prefix");
                Set<Map.Entry<String, String>> prefixes = prefixesMap.entrySet();
                for (Map.Entry<String, String> prefix : prefixes) {
                    Level level = Level.getLevel(levelStr.toUpperCase());
                    String loggerName = prefix.getValue();
                    LoggerConfig loggerConfig = new LoggerConfig(loggerName, level, additivity);
                    for (Appender appender : appenders) {
                        loggerConfig.addAppender(appender, level, null);
                    }

                    if (loggerConfig.getName().equals("root")) {
                        //TODO update root logger
                        getRootLogger().setLevel(loggerConfig.getLevel());
                        Set<Map.Entry<String, Appender>> appenderEntries = loggerConfig.getAppenders().entrySet();
                        for (Map.Entry<String, Appender> appenderEntry : appenderEntries) {
                            getRootLogger().addAppender(appenderEntry.getValue(), Level.ALL, null);
                        }
                    } else {
                        addLogger(loggerName, loggerConfig);
                    }
                    loggerConfigs.add(loggerConfig);
                }
            }

        }

//        if(!loggerConfigs.isEmpty()){
//
//            Collection<Logger> loggers = FoundationLoggerContextFactory.CONTEXT.getLoggers();
//            for (Logger logger : loggers) {
//                removeLogger(logger.getName());
//            }
//
//            for (LoggerConfig loggerConfig : loggerConfigs) {
//                if(loggerConfig.getName().equals("root")){
//                    //TODO update root logger
//                    getRootLogger().setLevel(loggerConfig.getLevel());
//                    Set<Map.Entry<String, Appender>> appenderEntries = loggerConfig.getAppenders().entrySet();
//                    for (Map.Entry<String, Appender> appenderEntry : appenderEntries) {
//                        getRootLogger().addAppender(appenderEntry.getValue(),Level.ALL, null);
//                    }
//                }else{
//                    addLogger(loggerConfig.getName(),loggerConfig);
//                }
//            }
//
//            if (LoggerFactory.getILoggerFactory() instanceof Log4jLoggerFactory) {
//                ((Log4jLoggerFactory)LoggerFactory.getILoggerFactory()).clearLoggers();
//            }
//
//            FoundationLoggerContextFactory.CONTEXT.clearLoggers();
//
//            for (Logger logger : loggers) {
//                LoggerFactory.getLogger(logger.getName());
//            }
//
//        }

    }

    private void initDestinationsFromCommon(Map<String, Map<String, String>> destinationsMap, Layout layout) {

        Set<Map.Entry<String, Map<String, String>>> entries = destinationsMap.entrySet();
        for (Map.Entry<String, Map<String, String>> entry : entries) {

            String destinationName = entry.getKey();
            Map<String, String> destinationMap = entry.getValue();
            String destinationType = destinationMap.get("type");

            if (StringUtils.isBlank(destinationType)) {
                throw new IllegalArgumentException("destination type is mandatory for destination: " + destinationName);
            }

            DestinationType type = DestinationType.valueOf(destinationType.toUpperCase());

            switch (type) {
                case CONSOLE:
                    createConsoleDestination(destinationName, destinationMap, layout);
                    continue;
                case ROLLING_RANDOM_ACCESS_FILE:
                    createRollingRandomAccessFile(destinationName, destinationMap, layout);
                    continue;
                default:
                    throw new UnsupportedOperationException("destination type: " + destinationType + "is not supported");

            }
        }

    }

    private void createRollingRandomAccessFile(String destinationName, Map<String, String> destinationMap, Layout layout) {
        Layout layoutToUse = getLayout(destinationMap, layout);

        String fileName = destinationMap.get("fileName");
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("fileName is mandatory for destination: " + destinationName);
        }

        String filePattern = fileName + ".%d{yyyy-MM-dd}.%i.gz";

        List<TriggeringPolicy> defualtTriggeringPolicies = new ArrayList<TriggeringPolicy>(3);
        defualtTriggeringPolicies.add(OnStartupTriggeringPolicy.createPolicy());
        defualtTriggeringPolicies.add(TimeBasedTriggeringPolicy.createPolicy("1", "false"));
        defualtTriggeringPolicies.add(SizeBasedTriggeringPolicy.createPolicy("100 MB"));

        Map<String, String> rollingPoliciesMap = ConfigUtil.parseSimpleArrayAsMap("logging.destination." + destinationName + ".rollingPolicy");
        List<TriggeringPolicy> triggeringPolicies = new ArrayList<TriggeringPolicy>(3);
        if (!rollingPoliciesMap.isEmpty()) {
            Set<Map.Entry<String, String>> entries = rollingPoliciesMap.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                if (entry.getKey().contains("type")) {
                    String policyType = entry.getValue();
                    switch (PolicyType.valueOf(policyType)) {
                        case TIME:
                            String interval = rollingPoliciesMap.get(entry.getKey().replace("type", "unit"));
                            if (StringUtils.isBlank(interval)) {
                                interval = "1";
                            }
                            triggeringPolicies.add(TimeBasedTriggeringPolicy.createPolicy(interval, "false"));
                            continue;
                        case ON_STARTUP:
                            triggeringPolicies.add(OnStartupTriggeringPolicy.createPolicy());
                            continue;

                        case SIZE:
                            String size = rollingPoliciesMap.get(entry.getKey().replace("type", "unit"));
                            if (StringUtils.isBlank(size)) {
                                size = "100 MB";
                            }
                            triggeringPolicies.add(SizeBasedTriggeringPolicy.createPolicy(size));
                            continue;

                        default:
                            throw new UnsupportedOperationException("destination rolling policy is not allowed for destination: " + destinationName);
                    }


                }
            }
        }

        TriggeringPolicy[] policies = !triggeringPolicies.isEmpty() ? triggeringPolicies.toArray(new TriggeringPolicy[0]) : defualtTriggeringPolicies.toArray(new TriggeringPolicy[0]);
        TriggeringPolicy trigerringPolicy = CompositeTriggeringPolicy.createPolicy(policies);

        RolloverStrategy rolloverStrategy = DefaultRolloverStrategy.createStrategy("100", null, null, null, this);

        FoundationRollingRandomAccessFileAppender appender = FoundationRollingRandomAccessFileAppender.createAppender(fileName, filePattern, "true", destinationName, "false", null, trigerringPolicy, rolloverStrategy, layoutToUse, null, null, null, null, this);
        appender.start();
        addAppender(appender);


    }

    private void createConsoleDestination(String destinationName, Map<String, String> destinationMap, Layout layout) {
        Layout layoutToUse = getLayout(destinationMap, layout);

        Appender appender = ConsoleAppender.createAppender(layoutToUse, null, "SYSTEM_OUT", destinationName, "false", "true");
        appender.start();
        addAppender(appender);
    }

    private Layout getLayout(Map<String, String> destinationMap, Layout layout) {
        Layout layoutToUse = layout;
        String layoutPattern = destinationMap.get("layout");
        if (StringUtils.isNotBlank(layoutPattern)) {
            layoutToUse = PatternLayout.newBuilder()
                    .withPattern(layoutPattern)
                    .withConfiguration(this)
                    .build();
        }
        return layoutToUse;
    }

    private void initiateLog4jPropertiesFallback(Layout layout) {

        URL resource = this.getClass().getResource(DEFAULT_CONFIGURATION_FILE);
        if (resource != null) {
            try {
                PropertiesConfiguration.setDefaultListDelimiter('~');
                PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration(resource);
                propertiesConfiguration.setDelimiterParsingDisabled(true);
                propertiesConfiguration.reload();
                init(propertiesConfiguration, layout);
            } catch (Exception e) {
                System.err.println("ERROR: can't load log4j.properties: " + e.toString());
            }
        } else {
            System.err.println("can't load log4j.properties - not found in classpath");
        }

    }

    private void init(PropertiesConfiguration propertiesConfiguration, Layout layout) {

        org.apache.commons.configuration.Configuration log4jSubset = propertiesConfiguration.subset("log4j");
        org.apache.commons.configuration.Configuration loggerSubset = log4jSubset.subset("logger");
        org.apache.commons.configuration.Configuration appenderSubset = log4jSubset.subset("appender");

        initAppenders(appenderSubset, layout);
        initLoggers(loggerSubset);
        initRoot(log4jSubset);

        doConfigure();

    }

    private boolean initRoot(org.apache.commons.configuration.Configuration log4jSubset) {

        boolean rootIsSet = false;

        if (log4jSubset.containsKey("rootLogger")) {
            rootIsSet = true;
            String val = log4jSubset.getString("rootLogger");
            if (StringUtils.isNotBlank(val)) {
                String[] rootParts = val.trim().split(",");
                for (String rootPart : rootParts) {

                    String trimmedRootPart = rootPart.trim();
                    if (Level.getLevel(rootPart.toUpperCase()) == null) {
                        getRootLogger().getAppenderRefs().add(AppenderRef.createAppenderRef(trimmedRootPart, Level.ALL, null));
                    } else {
                        getRootLogger().setLevel(Level.getLevel(trimmedRootPart.toUpperCase()));
                    }
                }
            }
        } else if (log4jSubset.containsKey("rootCategory")) {
            rootIsSet = true;
            String val = log4jSubset.getString("rootCategory");
            if (StringUtils.isNotBlank(val)) {
                String[] rootParts = val.trim().split(",");
                for (String rootPart : rootParts) {

                    if (Level.getLevel(rootPart.toUpperCase()) == null) {
                        getRootLogger().getAppenderRefs().add(AppenderRef.createAppenderRef(rootPart, Level.ALL, null));
                    } else {
                        getRootLogger().setLevel(Level.getLevel(rootPart.toUpperCase()));
                    }
                }
            }

        }

        List<AppenderRef> appenderRefs = getRootLogger().getAppenderRefs();
        for (AppenderRef appenderRef : appenderRefs) {
            Appender appender = getAppender(appenderRef.getRef());
            getRootLogger().addAppender(appender, appenderRef.getLevel(), appenderRef.getFilter());
        }

        getRootLogger().start();


        return rootIsSet;

    }

    private void initLoggers(org.apache.commons.configuration.Configuration loggerSubset) {
        Iterator<String> keys = loggerSubset.getKeys();
        List<LoggerConfig> loggerConfigs = new ArrayList<LoggerConfig>();
        while (keys.hasNext()) {
            String key = keys.next();
            String name = "";
            String level = "";
            boolean additivity = true;
            if (key.startsWith("additivity")) {
                LoggerConfig logger = getLogger(key.substring("additivity.".length()));
                Boolean additive = Boolean.valueOf(loggerSubset.getString(key));
                logger.setAdditive(additive);
            } else {
                List<String> appenderRefs = new ArrayList<String>();
                name = key;
                String val = loggerSubset.getString(key);
                if (StringUtils.isNotBlank(val)) {
                    if (val.contains(",")) {
                        String[] strings = val.split(",");
                        level = strings[0];
                        if (strings.length > 1) {
                            for (int i = 1; i < strings.length; i++) {
                                String appenderName = strings[i].trim();
                                appenderRefs.add(appenderName);
                            }
                        }
                    } else {
                        level = val;
                    }
                }
                Level level1 = Level.getLevel(level.toUpperCase());
                LoggerConfig loggerConfig = new LoggerConfig(name, level1, additivity);
                for (String appenderRef : appenderRefs) {
                    loggerConfig.addAppender(getAppender(appenderRef), level1, null);
                }
//                loggerConfig.getAppenderRefs()
                addLogger(name, loggerConfig);
            }

        }


    }

    private void initAppenders(org.apache.commons.configuration.Configuration appenderSubset, Layout layout) {
        Iterator<String> keys = appenderSubset.getKeys();
        while (keys.hasNext()) {
            String key = keys.next();

            String val = appenderSubset.getString(key);
            if (key.contains(".file") && StringUtils.isNotBlank(val)) {
                int endIndex = key.indexOf('.');
                String appenderName = key.substring(0, endIndex);
                String fileName = val;
                String filePattern = fileName + ".%d{yyyy-MM-dd}.%i.gz";
                TriggeringPolicy trigerringPolicy = CompositeTriggeringPolicy.createPolicy(
                        OnStartupTriggeringPolicy.createPolicy(),
                        TimeBasedTriggeringPolicy.createPolicy("1", "false"),
                        SizeBasedTriggeringPolicy.createPolicy("100 MB")
                );
                RolloverStrategy rolloverStrategy = DefaultRolloverStrategy.createStrategy("100", null, null, null, this);
                FoundationRollingRandomAccessFileAppender appender = FoundationRollingRandomAccessFileAppender.createAppender(fileName, filePattern, "true", appenderName, "false", null, trigerringPolicy, rolloverStrategy, layout, null, null, null, null, this);
                appender.start();
                addAppender(appender);
//                appender.

            }

            if (StringUtils.isNotBlank(val) && val.contains("org.apache.log4j.ConsoleAppender")) {
                Appender appender =
                        ConsoleAppender.createAppender(layout, null, "SYSTEM_OUT", key, "false", "true");
                appender.start();
                addAppender(appender);
            }


        }
    }

    @Override
    protected void doConfigure() {
    }

    @Override
    public Configuration reconfigure() {
        return this;
    }

}
