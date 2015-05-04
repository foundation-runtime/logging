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

package com.cisco.oss.foundation.logging.converters;

import com.cisco.oss.foundation.logging.FoundationLoggerConfiguration;
import com.cisco.oss.foundation.logging.FoundationLoggingPatternLayout;
import com.cisco.oss.foundation.logging.slf4j.Log4jMarker;
import com.cisco.oss.foundation.logging.structured.FoundationLoggingMarker;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.MessagePatternConverter;
import org.apache.logging.log4j.core.pattern.NamePatternConverter;
import org.apache.logging.log4j.core.util.Charsets;
import org.slf4j.Marker;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * A converter form structured logging. can also get a regular logging message.
 * will log pretty message (in case of regular message) if it was set as
 * sl{pretty}
 * 
 * @author ykasten
 * 
 */
@Plugin(name = "FoundationLoggingStructuredMessageConverter", category = "Converter")
@ConverterKeys({ "sl" })
public class FoundationLoggingStructuredMessageConverter  extends NamePatternConverter {

	private String key;

	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private ReadLock readLock = lock.readLock();
	private WriteLock writeLock = lock.writeLock();

	/**
	 * A static map of layouts so layout for marker will be set only one time.
	 */
	private static HashMap<String, Layout> layoutsMap = new HashMap<String, Layout>();

	private static MessagePatternConverter messageConverter = MessagePatternConverter.newInstance(null, null);
	private static FoundationLoggingMessagePatternConverter PrettyMessageConverter = FoundationLoggingMessagePatternConverter.newInstance(null);

	private FoundationLoggingStructuredMessageConverter(final String[] options) {
		super("StructuredMessage", "sl",options);
	}

	public static FoundationLoggingStructuredMessageConverter newInstance(final String[] options) {
		return new FoundationLoggingStructuredMessageConverter(options);
	}

	@Override
    public void format(final LogEvent event, final StringBuilder toAppendTo) {
		boolean appended = false;

        if(event.getMarker() instanceof Log4jMarker){
            Log4jMarker log4jMarker = (Log4jMarker)event.getMarker();
            Marker marker = log4jMarker.getMarker();

            if (marker instanceof FoundationLoggingMarker) { // So this converter may
                // uses internal pattern
                // that is composed
                // layout of %u{}

                FoundationLoggingMarker foundationMarker = (FoundationLoggingMarker) marker;

                String pattern = foundationMarker.getFormatter().getFormat(ThreadContext.get("%APPENDER_NAME%"));

                if (pattern != null) { // only if we have pattern from one of two options
                    if (layoutsMap.get(pattern) == null) {
                        layoutsMap.put(pattern, new FoundationLoggingPatternLayout(
                                FoundationLoggerConfiguration.INSTANCE,
                                null,
                                pattern,
                                Charsets.UTF_8,
                                true,
                                false,
                                null,
                                null));
                    }
                    // get the layout from static layouts map and get its format string.
                    String fromInnerPattern = layoutsMap.get(pattern).toSerializable(event).toString();
                    toAppendTo.append(fromInnerPattern);
                    appended = true;
                }
            }
        }

//        if (event instanceof FoundationLog4jLogEvent) { // So we can call get marker
//            FoundationLog4jLogEvent foundationLog4jLogEvent = (FoundationLog4jLogEvent) event;
//
//            Marker marker = (Marker)foundationLog4jLogEvent.getMarker();
//
//		}
		if (!appended) { // not FoundationLof4jLoggingEvent, or not FoundationLoggingMarker, or no
							// special pattern for marker composed from %u{}
			if (key != null) {
				// pretty message
				PrettyMessageConverter.format(event, toAppendTo);
			} else {
				// regular message
				messageConverter.format(event, toAppendTo);
			}
		}

	}

}
