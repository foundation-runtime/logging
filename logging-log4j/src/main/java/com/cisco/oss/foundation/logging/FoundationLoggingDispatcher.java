package com.cisco.oss.foundation.logging;

import org.apache.log4j.*;
import org.apache.log4j.helpers.AppenderAttachableImpl;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.Marker;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FoundationLoggingDispatcher implements Runnable {
	/**
	 * Parent AsyncAppender.
	 */
	private final AsyncAppender parent;

	/**
	 * Event buffer.
	 */
	private final List<LoggingEvent> buffer;

	/**
	 * Map of DiscardSummary keyed by logger name.
	 */
	private final Map<String,DiscardSummary> discardMap;

	/**
	 * Wrapped appenders.
	 */
	private final AppenderAttachableImpl appenders;

	/**
	 * Create new instance of dispatcher.
	 * 
	 * @param parent
	 *            parent AsyncAppender, may not be null.
	 * @param buffer
	 *            event buffer, may not be null.
	 * @param discardMap
	 *            discard map, may not be null.
	 * @param appenders
	 *            appenders, may not be null.
	 */
	public FoundationLoggingDispatcher(final AsyncAppender parent, final List<LoggingEvent> buffer, final Map<String, DiscardSummary> discardMap, final AppenderAttachableImpl appenders) {

		this.parent = parent;
		this.buffer = buffer;
		this.appenders = appenders;
		this.discardMap = discardMap;
	}	

	/**
	 * {@inheritDoc}
	 */
	public void run() {
		boolean isActive = true;

		//
		// if interrupted (unlikely), end thread
		//
		try {
			//
			// loop until the AsyncAppender is closed.
			//
			while (isActive) {
				LoggingEvent[] events = null;

				//
				// extract pending events while synchronized
				// on buffer
				//
				synchronized (buffer) {
					int bufferSize = buffer.size();
					isActive = !isAsyncAppenderClosed(parent);

					while ((bufferSize == 0) && isActive) {
						buffer.wait();
						bufferSize = buffer.size();
						isActive = !isAsyncAppenderClosed(parent);
					}

					if (bufferSize > 0) {
						events = new LoggingEvent[bufferSize + discardMap.size()];
						buffer.toArray(events);

						//
						// add events due to buffer overflow
						//
						int index = bufferSize;

						for (Iterator iter = discardMap.values().iterator(); iter.hasNext();) {
							events[index++] = ((DiscardSummary) iter.next()).createEvent();
						}

						//
						// clear buffer and discard map
						//
						buffer.clear();
						discardMap.clear();

						//
						// allow blocked appends to continue
						buffer.notifyAll();
					}
				}

				//
				// process events after lock on buffer is released.
				//
				if (events != null) {
					for (int i = 0; i < events.length; i++) {
						synchronized (appenders) {
							LoggingEvent event = events[i];											
							@SuppressWarnings("unchecked")
							Enumeration<Appender> allAppenders = appenders.getAllAppenders();
							
							while (allAppenders.hasMoreElements()) {
								Appender appender = allAppenders.nextElement();

								//since we may update the appender layout we must sync so other threads won't use it by mistake
								synchronized (appender) {
									Layout originalLayout = appender.getLayout();
									boolean appenderUpdated = udpateLayoutIfNeeded(appender, event);

									appender.doAppend(event);
									if (appenderUpdated) {
										appender.setLayout(originalLayout);
									}
								}

							}																										
						}
					}
				}
			}
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}
	
	private boolean udpateLayoutIfNeeded(Appender appender, LoggingEvent event) {

		if (event instanceof FoundationLoggingEvent) {

			FoundationLoggingEvent foundationLoggingEvent = (FoundationLoggingEvent) event;
			Marker marker = foundationLoggingEvent.getMarker();

			if (marker != null) {

				Map<String, Layout> map = FoundationLogger.markerAppendersMap.get(marker.getName());

				if (map != null) {
					Layout specificPatternLayout = map.get(appender.getName());
					if (specificPatternLayout != null) {
						appender.setLayout(specificPatternLayout);
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean isAsyncAppenderClosed(Appender appender) {
		// no other way than use reflection to validate the state of the appender.
		// NOTE: this code will break if java SecurityManager is used.
		try {
			Field closedField = AppenderSkeleton.class.getDeclaredField("closed");
			closedField.setAccessible(true);
			boolean closed = (Boolean) closedField.get(appender);

			return closed;
		} catch (Exception e) {
			System.err.println("Cannot check AsyncAppender state. Error is: " + e);
			// assume closed in case of error.
			return true;
		}
	}

	/**
	 * Summary of discarded logging events for a logger.
	 */
	public static final class DiscardSummary {
		/**
		 * First event of the highest severity.
		 */
		private LoggingEvent maxEvent;

		/**
		 * Total count of messages discarded.
		 */
		private int count;

		/**
		 * Create new instance.
		 * 
		 * @param event
		 *            event, may not be null.
		 */
		public DiscardSummary(final LoggingEvent event) {
			maxEvent = event;
			count = 1;
		}

		/**
		 * Add discarded event to summary.
		 * 
		 * @param event
		 *            event, may not be null.
		 */
		public void add(final LoggingEvent event) {
			if (event.getLevel().toInt() > maxEvent.getLevel().toInt()) {
				maxEvent = event;
			}

			count++;
		}

		/**
		 * Create event with summary information.
		 * 
		 * @return new event.
		 */
		public LoggingEvent createEvent() {
			String msg = MessageFormat.format("Discarded {0} messages due to full event buffer including: {1}", new Object[] { new Integer(count), maxEvent.getMessage() });

			return new LoggingEvent("org.apache.log4j.AsyncAppender.DONT_REPORT_LOCATION", Logger.getLogger(maxEvent.getLoggerName()), maxEvent.getLevel(), msg, null);
		}
	}
}