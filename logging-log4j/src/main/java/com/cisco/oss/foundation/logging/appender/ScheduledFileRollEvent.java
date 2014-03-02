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

package com.cisco.oss.foundation.logging.appender;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

/**
 * @author <a href="mailto:simon_park_mail AT yahoo DOT co DOT uk">Simon
 *         Park</a>
 * @version 2.0
 */
final class ScheduledFileRollEvent extends LoggingEvent {

  private static final long serialVersionUID = -2423899714190505565L;

  private final FoundationFileRollingAppender appender;

  ScheduledFileRollEvent(final FoundationFileRollingAppender appender,
      final long eventTime) {
    super(Logger.class.getName(), Logger.getRootLogger(), eventTime, Level.ALL,
        "Log4J scheduled file roll event", null);
    this.appender = appender;
  }

  /**
   * Convenience method dispatches this object to the source appender, which
   * will result in a forced file roll.
   */
  final void dispatchToAppender() {
    this.appender.append(this);
  }
}
