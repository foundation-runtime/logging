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

import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.spi.LoggerContextFactory;

import java.net.URI;

/**
 * Created by Yair Ogen on 16/07/2014.
 */
public class FoundationLoggerContextFactory implements LoggerContextFactory {
    private static final FoundationLoggerContext CONTEXT = new FoundationLoggerContext("FoundationLoggerContext@"
            + FoundationLoggerContext.class.hashCode());

    @Override
    public LoggerContext getContext(final String fqcn, final ClassLoader loader, final Object externalContext,
                                    final boolean currentContext) {
//        CONTEXT.start(new FoundationLoggerConfiguration());
        return CONTEXT;
    }

    @Override
    public LoggerContext getContext(final String fqcn, final ClassLoader loader, final Object externalContext,
                                    final boolean currentContext, final URI configLocation, final String name) {
//        CONTEXT.start(new FoundationLoggerConfiguration());
        return CONTEXT;
    }

    @Override
    public void removeContext(final LoggerContext context) {
    }
}
