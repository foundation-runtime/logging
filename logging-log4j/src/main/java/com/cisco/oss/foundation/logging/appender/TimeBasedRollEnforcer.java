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

import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.ErrorCode;

/**
 * Responsible for ensuring that log files are rolled pro-actively when rollover
 * is due. This is as opposed to rolling files reactively when logging events
 * are dispatched to the appender.
 * 
 * @author <a href="mailto:simon_park_mail AT yahoo DOT co DOT uk">Simon
 *         Park</a>
 * @version 1.2
 */
final class TimeBasedRollEnforcer implements Runnable {

  /**
   * One second.
   */
  private static final long TIME_OFFSET = 1000L;

  private final FoundationFileRollingAppender appender;

  private final AppenderRollingProperties properties;

  private Thread threadRef = null;

  private long nextEnforcedRollTimeMillis = 0L;

  TimeBasedRollEnforcer(final FoundationFileRollingAppender rollingAppender,
      final AppenderRollingProperties appenderRollingProperties) {
    super();
    this.appender = rollingAppender;
    this.properties = appenderRollingProperties;
  }

  public final void run() {
    LogLog.debug("Log time-based file-roll enforcer started");
    try {
      while (this.isRunning()) {
        try {
          this.enforceRollSchedule();
          Thread.sleep(this.sleepInterval());
        } catch (InterruptedException e) {
          // game over
          Thread.currentThread().interrupt();
        }
      }
    } catch (Exception e) {
      this.appender.getErrorHandler().error(
          "Log time-based file-roll enforcer failed", e,
          ErrorCode.GENERIC_FAILURE);
    }
    LogLog.debug("Log time-based file-roll enforcer stopped");
  }

  /**
   * Starts the enforcer.
   */
  final void begin() {
    if (this.properties.isDateRollEnforced()) {
      final Thread thread = new Thread(this,
          "Log4J Time-based File-roll Enforcer");
      thread.setDaemon(true);
      thread.start();
      this.threadRef = thread;
    }
  }

  /**
   * Stops the enforcer.
   */
  final void end() {
    final Thread thread = this.threadRef;
    if (thread != null) {
      thread.interrupt();
      try {
        thread.join();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    this.threadRef = null;
  }

  private boolean isScheduledRollTimeExceeded(final long eventTime) {
    // no waiting around
    return (eventTime > this.properties.getNextRollOverTime());
  }

  private boolean isRunning() {
    return (this.threadRef != null) ? (!this.threadRef.isInterrupted()) : false;
  }

  private void enforceRollSchedule() {
    final long eventTime = this.nextEnforcedRollTimeMillis;
    if (this.isScheduledRollTimeExceeded(eventTime)) {
      /*
       * We can't just call rollFile() on the appender as it wouldn't be
       * thread-safe, so append a special-purpose event
       */
      final ScheduledFileRollEvent fileRollEvent = new ScheduledFileRollEvent(
          this.appender, eventTime);
      fileRollEvent.dispatchToAppender();
    }
    // wait around for initialisation
    this.updateNextEnforcedRollTime(this.properties
        .getNextRollOverTimeWithWait());
  }

  private void updateNextEnforcedRollTime(final long eventTime) {
    this.nextEnforcedRollTimeMillis = eventTime + TIME_OFFSET;
  }

  private long sleepInterval() {
    final long interval = this.nextEnforcedRollTimeMillis
        - System.currentTimeMillis();
    if (interval < 0L) {
      return TIME_OFFSET;
    }
    return interval;
  }
}
