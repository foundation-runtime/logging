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

package org.apache.logging.log4j.core;

import com.cisco.oss.foundation.configuration.ConfigurationFactory;
import com.cisco.oss.foundation.configuration.FoundationConfigurationListener;
import com.cisco.oss.foundation.configuration.FoundationConfigurationListenerRegistry;
import com.cisco.oss.foundation.logging.FoundationLogger;
import com.cisco.oss.foundation.logging.FoundationLoggerConfiguration;
import com.cisco.oss.foundation.logging.structured.AbstractFoundationLoggingMarker;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.message.MessageFactory;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Yair Ogen on 17/07/2014.
 */
public class FoundationLoggerContext extends org.apache.logging.log4j.core.LoggerContext {

    public static final CountDownLatch POST_CONFIG_LATCH = new CountDownLatch(1);

//    private final ConcurrentMap<String, Logger> loggers = new ConcurrentHashMap<String, Logger>();

    public FoundationLoggerContext(final String name) {
        super(name);
        start(FoundationLoggerConfiguration.INSTANCE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    POST_CONFIG_LATCH.await();
                } catch (InterruptedException e) {
                    System.err.println("post config thread was interrupted: " + e);
                }
                updateLoggers(FoundationLoggerConfiguration.INSTANCE);
                AbstractFoundationLoggingMarker.init();
                updateSniffingLoggersLevel();
                FoundationConfigurationListenerRegistry.addFoundationConfigurationListener(new LoggingConfigurationListener());
            }
        }, "postConfigThread").start();
//        start(new FoundationLoggerConfiguration());
    }

    public FoundationLoggerContext(final String name, final Object externalContext) {
        super(name, externalContext);
    }

    public FoundationLoggerContext(final String name, final Object externalContext,
                              final URI configLocn) {
        super(name, externalContext, configLocn);
    }

    public FoundationLoggerContext(final String name, final Object externalContext,
                              final String configLocn) {
        super(name, externalContext, configLocn);
    }

    @Override
    protected Logger newInstance(final org.apache.logging.log4j.core.LoggerContext ctx, final String name,
                                 final MessageFactory messageFactory) {
        return new FoundationLogger(ctx, name, messageFactory);
    }

    @Override
    public void stop() {
        FoundationLogger.stop();
        super.stop();
    }

//    /**
//     * Obtain a Logger from the Context.
//     * @param name The name of the Logger to return.
//     * @param messageFactory The message factory is used only when creating a
//     *            logger, subsequent use does not change the logger but will log
//     *            a warning if mismatched.
//     * @return The Logger.
//     */
//    @Override
//    public Logger getLogger(final String name, final MessageFactory messageFactory) {
//        Logger logger = loggers.get(name);
//        if (logger != null) {
//            AbstractLogger.checkMessageFactory(logger, messageFactory);
//            return logger;
//        }
//
//        logger = newInstance(this, name, messageFactory);
//        final Logger prev = loggers.putIfAbsent(name, logger);
//        return prev == null ? logger : prev;
//    }

    public void clearLoggers(){
        getLoggers().clear();
    }

    /**
     * The sniffing Loggers are some special Loggers, whose level will be set to TRACE forcedly.
     */
    private static void updateSniffingLoggersLevel() {

        InputStream settingIS = FoundationLogger.class
                .getResourceAsStream("/sniffingLogger.xml");
        if (settingIS == null) {
//            logger.debug("file sniffingLogger.xml not found in classpath");
        } else {
            try {
                SAXBuilder builder = new SAXBuilder();
                Document document = builder.build(settingIS);
                settingIS.close();
                Element rootElement = document.getRootElement();
                List<Element> sniffingloggers = rootElement.getChildren("sniffingLogger");
                for (Element sniffinglogger : sniffingloggers) {
                    String loggerName = sniffinglogger.getAttributeValue("id");
                    org.apache.logging.log4j.Logger logger = LogManager.getLogger(loggerName);
                    if(logger instanceof org.apache.logging.log4j.core.Logger){
                        org.apache.logging.log4j.core.Logger log4jLoggger = (org.apache.logging.log4j.core.Logger)logger;
                        log4jLoggger.setLevel(Level.TRACE);
                    }
                }
            } catch (Exception e) {
                System.err.println("cannot load the sniffing logger configuration file. error is: " + e);
//                logger.error("cannot load the sniffing logger configuration file. error is: " + e, e);
                throw new IllegalArgumentException(
                        "Problem parsing sniffingLogger.xml", e);
            }
        }

    }

    private class LoggingConfigurationListener implements FoundationConfigurationListener {


        public LoggingConfigurationListener() {
        }

        @Override
        public void configurationChanged() {
            org.slf4j.Logger log = LoggerFactory.getLogger(FoundationLoggerConfiguration.class);
            log.info("identified configuration changes");
            FoundationLoggerConfiguration.INSTANCE.initiateLoggingFromConfiguration(FoundationLoggerConfiguration.INSTANCE.getLayout(), ConfigurationFactory.getConfiguration());
            updateLoggers();
            updateSniffingLoggersLevel();
            log.info("finished reloading the logging configuration");
        }
    }
}
