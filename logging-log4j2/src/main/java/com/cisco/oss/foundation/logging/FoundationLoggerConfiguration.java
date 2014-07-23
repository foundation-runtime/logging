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

import com.cisco.oss.foundation.logging.appenders.FoundationRollingRandomAccessFileAppender;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.rolling.*;
import org.apache.logging.log4j.core.config.*;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Yair Ogen on 17/07/2014.
 */
public class FoundationLoggerConfiguration extends AbstractConfiguration implements Reconfigurable {

    public static final FoundationLoggerConfiguration INSTANCE = new FoundationLoggerConfiguration();
    /**
     * The name of the default configuration.
     */
    public static final String DEFAULT_NAME = "FoundationLoggerConfiguration";
    /**
     * The System Property used to specify the logging level.
     */
    public static final String DEFAULT_LEVEL = "org.apache.logging.log4j.level";
    /**
     * The default Pattern used for the default Layout.
     */
    private static final String DEFAULT_CONFIGURATION_FILE = "/log4j.properties"; // NOPMD
    private AtomicBoolean isFirstTime = new AtomicBoolean(true);

    /**
     * Constructor to create the default configuration.
     */
    public FoundationLoggerConfiguration() {
        super(ConfigurationSource.NULL_SOURCE);


//        if (isFirstTime.compareAndSet()) {
        final Layout<? extends Serializable> layout = PatternLayout.newBuilder()
                .withPattern(FoundationLoggerConstants.DEFAULT_CONV_PATTERN.toString())
                .withConfiguration(this)
                .build();

        init(layout);
//        }

//        pluginManager.loadPlugins();
//        REGISTRY.getCategory(entry.getKey()).putAll(entry.getValue());

        setName(DEFAULT_NAME);
//        final Appender appender =
//                ConsoleAppender.createAppender(layout, null, "SYSTEM_OUT", "Console", "false", "true");
//        appender.start();
//        addAppender(appender);
//        final LoggerConfig root = getRootLogger();
//        root.addAppender(appender, null, null);
//
//        final String levelName = PropertiesUtil.getProperties().getStringProperty(DEFAULT_LEVEL);
//        final Level level = levelName != null && Level.valueOf(levelName) != null ?
//                Level.valueOf(levelName) : Level.ERROR;
//        root.setLevel(level);

    }

    private void init(Layout layout) {

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

        if(log4jSubset.containsKey("rootLogger")){
            rootIsSet=true;
            String val = log4jSubset.getString("rootLogger");
            if(StringUtils.isNotBlank(val)){
                String[] rootParts = val.trim().split(",");
                for (String rootPart : rootParts) {

                    String trimmedRootPart = rootPart.trim();
                    if(Level.getLevel(rootPart.toUpperCase()) == null){
                        getRootLogger().getAppenderRefs().add(AppenderRef.createAppenderRef(trimmedRootPart, Level.ALL,null));
                    }else{
                        getRootLogger().setLevel(Level.getLevel(trimmedRootPart.toUpperCase()));
                    }
                }
            }
        }else if(log4jSubset.containsKey("rootCategory")){
            rootIsSet=true;
            String val = log4jSubset.getString("rootCategory");
            if(StringUtils.isNotBlank(val)){
                String[] rootParts = val.trim().split(",");
                for (String rootPart : rootParts) {

                    if(Level.getLevel(rootPart.toUpperCase()) == null){
                        getRootLogger().getAppenderRefs().add(AppenderRef.createAppenderRef(rootPart, Level.ALL,null));
                    }else{
                        getRootLogger().setLevel(Level.getLevel(rootPart.toUpperCase()));
                    }
                }
            }

        }

        List<AppenderRef> appenderRefs = getRootLogger().getAppenderRefs();
        for (AppenderRef appenderRef : appenderRefs) {
            Appender appender = getAppender(appenderRef.getRef());
            getRootLogger().addAppender(appender,appenderRef.getLevel(),appenderRef.getFilter());
        }

        getRootLogger().start();


        return rootIsSet;

    }

    private void initLoggers(org.apache.commons.configuration.Configuration loggerSubset) {
        Iterator<String> keys = loggerSubset.getKeys();
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
                name = key;
                String val = loggerSubset.getString(key);
                if (StringUtils.isNotBlank(val)) {
                    if (val.contains(",")) {
                        String[] strings = val.split(",");
                        level = strings[0];
                        //TODO support appenders
                    } else {
                        level = val;
                    }
                }
                LoggerConfig loggerConfig = new LoggerConfig(name, Level.getLevel(level.toUpperCase()), additivity);
//                loggerConfig.getAppenderRefs()
                addLogger(key, loggerConfig);
            }
            System.out.println("logger key=" + key);

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
                RolloverStrategy rolloverStrategy = DefaultRolloverStrategy.createStrategy("100",null,null,null,this);
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

            System.out.println("appender key=" + key);


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
