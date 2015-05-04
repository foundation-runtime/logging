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
package com.cisco.oss.foundation.logging.appender;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

import java.io.File;

/**
 * This event is fired after the current log file has been renamed as a backup
 * and a new file already opened for logging. This event is capable of both
 * self-dispatch to the underlying source appender, and the dispatch of other
 * custom logging event objects.
 * 
 * @author <a href="mailto:simon_park_mail AT yahoo DOT co DOT uk">Simon
 *         Park</a>
 * @version 2.6
 */
final class FileRollEvent extends LoggingEvent {

  private static final long serialVersionUID = 3505480003597965824L;

  private final File backupFile;

  private transient FoundationFileRollingAppender source;

  private final String sourceName;

  /**
   * @param source
   *          The originator of the event.
   * @param backupFile
   *          The newly-created backup file.
   */
  FileRollEvent(final FileRollable source, final File backupFile) {
    super(Logger.class.getName(), Logger.getRootLogger(), System
        .currentTimeMillis(), Level.ALL, "Log4J file roll event"
        + System.getProperty("line.separator"), null);
    this.backupFile = backupFile;
    this.source = source.getAppender();
    this.sourceName = this.source.getName();
  }

  /**
   * Copy ctor.
   * 
   * @param original
   *          The event to be copied.
   * @param message
   *          The custom message that will be used to initialise the event copy.
   */
  private FileRollEvent(final FileRollEvent original, final String message) {
    super(Logger.class.getName(), Logger.getRootLogger(), System
        .currentTimeMillis(), Level.ALL, String.valueOf(message)
        + System.getProperty("line.separator"), null);
    this.backupFile = original.backupFile;
    this.source = original.getSource();
    this.sourceName = this.source.getName();
  }

  private FileRollEvent(final LoggingEvent loggingEvent,
      final FileRollEvent fileRollEvent) {
    // Compatible with 1.2.15 and up
    super(loggingEvent.getFQNOfLoggerClass(), loggingEvent.getLogger(),
        loggingEvent.getTimeStamp(), loggingEvent.getLevel(), loggingEvent
            .getMessage(), loggingEvent.getThreadName(), loggingEvent
            .getThrowableInformation(), loggingEvent.getNDC(), loggingEvent
            .getLocationInformation(), loggingEvent.getProperties());
    this.backupFile = fileRollEvent.backupFile;
    this.source = fileRollEvent.getSource();
    this.sourceName = this.source.getName();
  }

  /**
   * @return The backup {@link java.io.File}.
   */
  final File getBackupFile() {
    return this.backupFile;
  }

  FoundationFileRollingAppender getSource() {
    if (this.source == null) {
      final Appender appender = super.getLogger().getAppender(this.sourceName);
      if (appender == null) {
        LogLog.error("Missing " + FoundationFileRollingAppender.class.getName()
            + "; expected to be attached to the Logger that created this "
            + this.getClass().getName());
        return null;
      }
      if (appender instanceof FoundationFileRollingAppender) {
        this.source = (FoundationFileRollingAppender) appender;
      } else {
        LogLog.error("Expected " + FoundationFileRollingAppender.class.getName()
            + " but was " + appender.getClass().getName());
      }
    }
    return source;
  }

  /**
   * Convenience method dispatches this object to the source appender, which
   * will result in the custom message being appended to the new file.
   * 
   * @param message
   *          The custom logging message to be appended.
   */
  final void dispatchToAppender(final String message) {
    // dispatch a copy, since events should be treated as being immutable
    final FoundationFileRollingAppender appender = this.getSource();
    if (appender != null) {
      appender.append(new FileRollEvent(this, message));
    }
  }

  /**
   * Convenience method dispatches this object to the source appender, which
   * will result in a default message being appended to the new file.
   */
  final void dispatchToAppender() {
    final FoundationFileRollingAppender appender = this.getSource();
    if (appender != null) {
      appender.append(this);
    }
  }

  /**
   * Convenience method dispatches the specified event to the source appender,
   * which will result in the custom event data being appended to the new file.
   * 
   * @param customLoggingEvent
   *          The custom Log4J event to be appended.
   */
  final void dispatchToAppender(final LoggingEvent customLoggingEvent) {
    // wrap the LoggingEvent in a FileRollEvent to prevent recursion bug
    final FoundationFileRollingAppender appender = this.getSource();
    if (appender != null) {
      appender.append(new FileRollEvent(customLoggingEvent, this));
    }
  }
}
