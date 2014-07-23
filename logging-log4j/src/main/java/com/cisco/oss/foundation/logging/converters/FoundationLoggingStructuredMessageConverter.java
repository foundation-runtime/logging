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

package com.cisco.oss.foundation.logging.converters;

import com.cisco.oss.foundation.logging.FoundationLof4jLoggingEvent;
import com.cisco.oss.foundation.logging.FoundationLoggingPatternLayout;
import com.cisco.oss.foundation.logging.structured.FoundationLoggingMarker;
import org.apache.log4j.Layout;
import org.apache.log4j.pattern.LoggingEventPatternConverter;
import org.apache.log4j.pattern.MessagePatternConverter;
import org.apache.log4j.spi.LoggingEvent;
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
public class FoundationLoggingStructuredMessageConverter extends LoggingEventPatternConverter {

	private String key;

	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private ReadLock readLock = lock.readLock();
	private WriteLock writeLock = lock.writeLock();

	/**
	 * A static map of layouts so layout for marker will be set only one time.
	 */
	private static HashMap<String, Layout> layoutsMap = new HashMap<String, Layout>();

	private static MessagePatternConverter messageConverter = MessagePatternConverter.newInstance(null);
	private static FoundationLoggingMessagePatternConverter PrettyMessageConverter = FoundationLoggingMessagePatternConverter.newInstance(null);

	private FoundationLoggingStructuredMessageConverter(String key) {
		super("StructuredMessages", "StructuredMessages");
		if (key != null && !key.equals("pretty")) {
			throw new IllegalArgumentException("key value should be \"pretty\"");
		}

		this.key = key;

	}

	public static FoundationLoggingStructuredMessageConverter newInstance(final String[] options) {
		return new FoundationLoggingStructuredMessageConverter(options.length > 0 ? options[0] : null);
	}

	@Override
	public void format(LoggingEvent event, StringBuffer toAppendTo) {
		boolean appended = false;
		if (event instanceof FoundationLof4jLoggingEvent) { // So we can call get marker
			FoundationLof4jLoggingEvent foundationLof4jLoggingEvent = (FoundationLof4jLoggingEvent) event;

			Marker marker = foundationLof4jLoggingEvent.getSlf4jMarker();

			if (marker instanceof FoundationLoggingMarker) { // So this converter may
														// uses internal pattern
														// that is composed
														// layout of %u{}

				FoundationLoggingMarker foundationMarker = (FoundationLoggingMarker) marker;

				String pattern = foundationMarker.getFormatter().getFormat(foundationLof4jLoggingEvent.getAppenderName());
				
				if (pattern != null) { // only if we have pattern from one of
										// two options
					if (layoutsMap.get(pattern) == null) {
						layoutsMap.put(pattern, new FoundationLoggingPatternLayout(pattern));
					}
					// get the layout from static layouts map and get its format
					// string.
					String fromInnerPattern = layoutsMap.get(pattern).format(event);
					toAppendTo.append(fromInnerPattern);
					appended = true;
				}
			}
		}
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
