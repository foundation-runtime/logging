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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.async.AsyncLogger;
import org.apache.logging.log4j.core.async.RingBufferLogEvent;
import org.apache.logging.log4j.core.jmx.RingBufferAdmin;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;

/**
 * Created by Yair Ogen on 17/07/2014.
 */
public class FoundationLogger extends AsyncLogger {

    private AsyncLogger asyncLogger;

    /**
     * Constructs an {@code AsyncLogger} with the specified context, name and
     * message factory.
     *
     * @param context        context of this logger
     * @param name           name of this logger
     * @param messageFactory message factory of this logger
     */
    public FoundationLogger(final LoggerContext context, final String name, final MessageFactory messageFactory) {
        super(context, name, messageFactory);
        asyncLogger = new AsyncLogger(context, name, messageFactory);
    }

    public static void stop() {
        AsyncLogger.stop();
    }

    /**
     * Creates and returns a new {@code RingBufferAdmin} that instruments the
     * ringbuffer of the {@code AsyncLogger}.
     *
     * @param contextName name of the global {@code AsyncLoggerContext}
     */
    public static RingBufferAdmin createRingBufferAdmin(final String contextName) {
        return AsyncLogger.createRingBufferAdmin(contextName);
    }

    @Override
    public void logMessage(final String fqcn, final Level level, final Marker marker, final Message message, final Throwable thrown) {
        asyncLogger.logMessage(fqcn, level, marker, message, thrown);
    }

    /**
     * This method is called by the EventHandler that processes the
     * RingBufferLogEvent in a separate thread.
     *
     * @param event the event to log
     */
    public void actualAsyncLog(final RingBufferLogEvent event) {
        asyncLogger.actualAsyncLog(event);
    }

}
