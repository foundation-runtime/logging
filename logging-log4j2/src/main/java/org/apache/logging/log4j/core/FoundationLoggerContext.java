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

import com.cisco.oss.foundation.logging.FoundationLogger;
import com.cisco.oss.foundation.logging.FoundationLoggerConfiguration;
import org.apache.logging.log4j.message.MessageFactory;

import java.net.URI;
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

}
