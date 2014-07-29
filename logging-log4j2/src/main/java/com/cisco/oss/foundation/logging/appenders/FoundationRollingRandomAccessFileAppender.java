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

package com.cisco.oss.foundation.logging.appenders;

import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender;
import org.apache.logging.log4j.core.appender.RollingRandomAccessFileAppender;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.appender.rolling.RollingRandomAccessFileManager;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.util.Integers;

import java.io.Serializable;

/**
 * Created by Yair Ogen on 22/07/2014.
 */
public class FoundationRollingRandomAccessFileAppender  extends AbstractOutputStreamAppender<RollingFileManager> {

    private RollingRandomAccessFileAppender rollingRandomAccessFileAppender = null;

    private FoundationRollingRandomAccessFileAppender(final String name, final Layout<? extends Serializable> layout, final Filter filter,
                                           final boolean ignoreExceptions, final boolean immediateFlush,
                                           final RollingFileManager manager) {
        super(name, layout, filter, ignoreExceptions, immediateFlush, manager);
    }

//    private FoundationRollingRandomAccessFileAppender(final String name, final Layout<? extends Serializable> layout,
//                                            final Filter filter, final RollingFileManager manager, final String fileName,
//                                            final String filePattern, final boolean ignoreExceptions,
//                                            final boolean immediateFlush, final int bufferSize, final Advertiser advertiser) {
//        super(name, layout, filter, manager, fileName, filePattern, ignoreExceptions, immediateFlush, bufferSize, advertiser);
//    }

    public static FoundationRollingRandomAccessFileAppender createAppender(
            @PluginAttribute("fileName") final String fileName,
            @PluginAttribute("filePattern") final String filePattern,
            @PluginAttribute("append") final String append,
            @PluginAttribute("name") final String name,
            @PluginAttribute("immediateFlush") final String immediateFlush,
            @PluginAttribute("bufferSize") final String bufferSizeStr,
            @PluginElement("Policy") final TriggeringPolicy policy,
            @PluginElement("Strategy") RolloverStrategy strategy,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter,
            @PluginAttribute("ignoreExceptions") final String ignore,
            @PluginAttribute("advertise") final String advertise,
            @PluginAttribute("advertiseURI") final String advertiseURI,
            @PluginConfiguration final Configuration config) {

        final FoundationRollingRandomAccessFileManager manager = FoundationRollingRandomAccessFileManager.getRollingRandomAccessFileManager(
                fileName,
                filePattern,
                Boolean.valueOf(append),
                Boolean.valueOf(immediateFlush),
                Integers.parseInt(bufferSizeStr, RollingRandomAccessFileManager.DEFAULT_BUFFER_SIZE),
                policy,
                strategy,
                advertiseURI,
                layout);
//                fileName, filePattern, isAppend, isFlush, bufferSize, policy, strategy, advertiseURI, layout);

        FoundationRollingRandomAccessFileAppender foundationRollingRandomAccessFileAppender = new FoundationRollingRandomAccessFileAppender(name, layout, filter, Boolean.valueOf(ignore), Boolean.valueOf(immediateFlush), manager);
        foundationRollingRandomAccessFileAppender.rollingRandomAccessFileAppender = RollingRandomAccessFileAppender.createAppender(
                fileName,
                filePattern,
                append,
                name,
                immediateFlush,
                bufferSizeStr,
                policy,
                strategy,
                layout,
                filter,
                ignore,
                advertise,
                advertiseURI,
                config);

        manager.setFoundationRollingRandomAccessFileAppender(foundationRollingRandomAccessFileAppender);

        return foundationRollingRandomAccessFileAppender;

    }


    @Override
    public void stop() {
        rollingRandomAccessFileAppender.stop();
    }

    @Override
    public void append(final LogEvent event) {
        ThreadContext.put("%APPENDER_NAME%", getName());
        rollingRandomAccessFileAppender.append(event);
    }


}
