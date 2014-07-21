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

import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.MessageFactory;

import java.net.URI;

/**
 * Created by Yair Ogen on 17/07/2014.
 */
public class FoundationLoggerContext extends org.apache.logging.log4j.core.LoggerContext {

    public FoundationLoggerContext(final String name) {
        super(name);
        start(new FoundationLoggerConfiguration());
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

}
