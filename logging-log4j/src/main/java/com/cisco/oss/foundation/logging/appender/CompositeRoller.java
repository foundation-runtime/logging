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

import org.apache.log4j.spi.LoggingEvent;

/**
 * Composes the behaviour of the startup roller, file-size roller, and the
 * time-based roller. The startup roller takes precedence, followed by the
 * time-based roller, with the file-size roller being evaluated last in the
 * chain.
 * 
 * @author <a href="mailto:simon_park_mail AT yahoo DOT co DOT uk">Simon
 *         Park</a>
 * @version 2.2
 */
final class CompositeRoller implements FileRollable {

  private final FileRollable[] fileRollables;

  private final FoundationFileRollingAppender appender;

  CompositeRoller(final FoundationFileRollingAppender rollingAppender,
      final AppenderRollingProperties appenderRollingProperties) {
    super();
    this.appender = rollingAppender;
    this.fileRollables = new FileRollable[] {
        new OneShotStartupRoller(rollingAppender, appenderRollingProperties),
        new TimeBasedRoller(rollingAppender, appenderRollingProperties),
        new FileSizeRoller(rollingAppender, appenderRollingProperties) };
  }

  /**
   * Delegates file rolling to composed objects.
   * 
   * @see FileRollable#roll(org.apache.log4j.spi.LoggingEvent)
   */
  public final boolean roll(final LoggingEvent loggingEvent) {
    for (int i = 0; i < this.fileRollables.length; i++) {
      if (this.fileRollables[i].roll(loggingEvent)) {
        return true;
      }
    }
    return false;
  }

  public final void addFileRollEventListener(
      final FileRollEventListener fileRollEventListener) {
    for (int i = 0; i < this.fileRollables.length; i++) {
      this.fileRollables[i].addFileRollEventListener(fileRollEventListener);
    }
  }

  public final void fireFileRollEvent(final FileRollEvent fileRollEvent) {
    for (int i = 0; i < this.fileRollables.length; i++) {
      this.fileRollables[i].fireFileRollEvent(fileRollEvent);
    }
  }

  public final void removeFileRollEventListener(
      final FileRollEventListener fileRollEventListener) {
    for (int i = 0; i < this.fileRollables.length; i++) {
      this.fileRollables[i].removeFileRollEventListener(fileRollEventListener);
    }
  }

  public final FoundationFileRollingAppender getAppender() {
    return this.appender;
  }
}
