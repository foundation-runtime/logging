package com.cisco.vss.foundation.logging;

import com.cisco.vss.foundation.logging.stuctured.CABLoggingMarker;
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
public class CABLoggingStucturedMessageConverter extends LoggingEventPatternConverter {

	private String key;

	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private ReadLock readLock = lock.readLock();
	private WriteLock writeLock = lock.writeLock();

	/**
	 * A static map of layouts so layout for marker will be set only one time.
	 */
	private static HashMap<String, Layout> layoutsMap = new HashMap<String, Layout>();

	private static MessagePatternConverter messageConverter = MessagePatternConverter.newInstance(null);
	private static CABLoggingMessagePatternConverter PrettyMessageConverter = CABLoggingMessagePatternConverter.newInstance(null);

	private CABLoggingStucturedMessageConverter(String key) {
		super("StructuredMessages", "StructuredMessages");
		if (key != null && !key.equals("pretty")) {
			throw new IllegalArgumentException("key value should be \"pretty\"");
		}

		this.key = key;

	}

	public static CABLoggingStucturedMessageConverter newInstance(final String[] options) {
		return new CABLoggingStucturedMessageConverter(options.length > 0 ? options[0] : null);
	}

	@Override
	public void format(LoggingEvent event, StringBuffer toAppendTo) {
		boolean appended = false;
		if (event instanceof CabLoggingEvent) { // So we can call get marker
			CabLoggingEvent cabLoggingEvent = (CabLoggingEvent) event;

			Marker marker = cabLoggingEvent.getMarker();

			if (marker instanceof CABLoggingMarker) { // So this converter may
														// uses internal pattern
														// that is composed
														// layout of %u{}

				CABLoggingMarker cabMarker = (CABLoggingMarker) marker;

				String pattern = cabMarker.getFormatter().getFormat(cabLoggingEvent);
				
				if (pattern != null) { // only if we have pattern from one of
										// two options
					if (layoutsMap.get(pattern) == null) {
						layoutsMap.put(pattern, new CABLoggingPatternLayout(pattern));
					}
					// get the layout from static layouts map and get its format
					// string.
					String fromInnerPattern = layoutsMap.get(pattern).format(event);
					toAppendTo.append(fromInnerPattern);
					appended = true;
				}
			}
		}
		if (!appended) { // not CabLoggingEvent, or not CABLoggingMarker, or no
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
