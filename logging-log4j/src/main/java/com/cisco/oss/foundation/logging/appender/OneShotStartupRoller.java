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

import org.apache.log4j.helpers.FileHelper;
import org.apache.log4j.spi.LoggingEvent;

/**
 * @author <a href="mailto:simon_park_mail AT yahoo DOT co DOT uk">Simon
 *         Park</a>
 * @version 1.5
 */
final class OneShotStartupRoller extends AbstractRoller implements FileRollable {

  public OneShotStartupRoller(final FoundationFileRollingAppender rollingAppender,
      AppenderRollingProperties appenderRollingProperties) {
    super(rollingAppender, appenderRollingProperties);
  }

  public final boolean roll(final LoggingEvent loggingEvent) {
    if (super.getProperties().wasRolledOnStartup()) {
      return false;
    }
    final boolean shouldRollNow = super.getProperties().updateRolledOnStartup();
    if (shouldRollNow) {
      if (FileHelper.getInstance().isEmpty(super.getAppender().getIoFile())) {
        return false;
      }
      super.roll(loggingEvent.getTimeStamp());
      return true;
    }
    return false;
  }

}
