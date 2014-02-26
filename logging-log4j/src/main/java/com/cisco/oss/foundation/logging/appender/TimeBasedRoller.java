/* 
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cisco.oss.foundation.logging.appender;

import org.apache.log4j.helpers.FileHelper;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

import java.io.File;

/**
 * Responsible for rolling at a logging time boundary, as configured by a date
 * pattern.
 * 
 * @author <a href="mailto:simon_park_mail AT yahoo DOT co DOT uk">Simon
 *         Park</a>
 * @version 2.6
 */
final class TimeBasedRoller extends AbstractRoller implements FileRollable {

  private final class TimeSample {
    private final long sample;

    TimeSample(long timeInMillis) {
      super();
      this.sample = timeInMillis;
    }

    long getMillis() {
      return this.sample;
    }

    boolean isAtOrAfter(long timeInMillis) {
      return this.sample >= timeInMillis;
    }

    boolean isAtOrAfter(TimeSample other) {
      return (other != null) ? (this.sample >= other.sample) : true;
    }
  }

  private final TimeBasedRollStrategy rollStrategy;

  private TimeSample currentSample;

  private TimeSample previousSample;

  TimeBasedRoller(final FoundationFileRollingAppender rollingAppender,
      final AppenderRollingProperties appenderRollingProperties) {
    super(rollingAppender, appenderRollingProperties);
    TimeBasedRollStrategy rollStrategyEnum = TimeBasedRollStrategy
        .findRollStrategy(appenderRollingProperties);
    if (TimeBasedRollStrategy.ROLL_ERROR.equals(rollStrategyEnum)) {
      rollStrategyEnum = TimeBasedRollStrategy.ROLL_EACH_DAY;
      LogLog.warn("Unable to parse date pattern ["
          + appenderRollingProperties.getDatePattern()
          + "] configured on appender [" + rollingAppender.getName()
          + "], defaulting to roll " + rollStrategyEnum.toString());
    }
    LogLog.debug("Appender [" + rollingAppender.getName() + "] to be rolled "
        + rollStrategyEnum.toString());
    this.rollStrategy = rollStrategyEnum;
    this.currentSample = new TimeSample(System.currentTimeMillis());
    this.initNextRolloverTime();
  }

  /**
   * Not thread-safe.
   * 
   * @see com.nds.cab.infra.logging.appender.FileRollable#roll(org.apache.log4j.spi.LoggingEvent)
   */
  public final boolean roll(final LoggingEvent loggingEvent) {
    boolean rolled = false;
    this.takeTimeSample(loggingEvent);
    final long nextRolloverTime = this.getNextRolloverTimeMillis();
    if (this.isRolloverDue(nextRolloverTime)) {
      super.roll(this.sampledTime());
      this.updateNextRolloverTime();
      rolled = true;
    }
    this.storeTimeSample();
    return rolled;
  }

  /**
   * For test purposes only.
   * 
   * @return The current strategy.
   */
  final TimeBasedRollStrategy getRollStrategy() {
    return this.rollStrategy;
  }

  private void takeTimeSample(final LoggingEvent loggingEvent) {
    this.currentSample = new TimeSample(loggingEvent.getTimeStamp());
  }

  private long sampledTime() {
    if (this.previousSample == null) {
      final File file = super.getAppender().getIoFile();
      if (FileHelper.getInstance().isReadable(file)) {
        this.previousSample = new TimeSample(file.lastModified());
      } else {
        this.previousSample = this.currentSample;
      }
    }
    return this.previousSample.getMillis();
  }

  /**
   * Allow for a thread to be blocked for a long time before its logging event
   * is actually appended; the timestamp of the current logging event may well
   * precede the timestamp of the last logging event to be appended.
   */
  private void storeTimeSample() {
    if (this.currentSample.isAtOrAfter(this.previousSample)) {
      this.previousSample = this.currentSample;
      this.currentSample = null;
    }
  }

  private long getNextRolloverTimeMillis() {
    return this.getProperties().getNextRollOverTime();
  }

  private void setNextRolloverTimeMillis(final long millis) {
    this.getProperties().setNextRollOverTime(millis);
  }

  private boolean isRolloverDue(final long nextRolloverTime) {
    return this.currentSample.isAtOrAfter(nextRolloverTime);
  }

  private void initNextRolloverTime() {
    this.updateNextRolloverTime();
    final File file = super.getAppender().getIoFile();
    if (FileHelper.getInstance().isReadable(file)) {
      final long thenRolloverTimeMillis = this.rollStrategy
          .nextRollTimeInMillis(file.lastModified());
      /*
       * If the 'rollover point' due *after* the 'file modification date' is
       * before the *next* scheduled 'rollover point' from now, then schedule a
       * rollover by using the *old* 'rollover point'. To put it another way, if
       * the 'file modification date' falls within the current period, just use
       * the *next* scheduled 'rollover point' from now.
       */
      if (thenRolloverTimeMillis < this.getNextRolloverTimeMillis()) {
        this.setNextRolloverTimeMillis(thenRolloverTimeMillis);
      }
    } else {
      this.getAppender()
          .getErrorHandler()
          .error(
              "Unable to initialise next rollover time based upon last"
                  + " modification time of " + String.valueOf(file)
                  + " because the file is not readable");
    }
  }

  private void updateNextRolloverTime() {
    final long nowInMillis = this.currentSample.getMillis();
    final long nextRollMillis = this.rollStrategy
        .nextRollTimeInMillis(nowInMillis);
    this.setNextRolloverTimeMillis(nextRollMillis);
  }
}
