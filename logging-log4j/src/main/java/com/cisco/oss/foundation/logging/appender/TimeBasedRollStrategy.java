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

import org.apache.log4j.helpers.LogLog;

import java.util.Calendar;
import java.util.regex.Pattern;

/**
 * Responsible for selecting the appropriate time-based rolling strategy at
 * start-up, based upon the contents of the Appender's DatePattern property;
 * also responsible for computing the next roll time. This class is heavily
 * dependent upon the specification of the JDK's {@link java.text.SimpleDateFormat} class,
 * whose pattern letters are used to select the rolling strategy.
 *
 * @author <a href="mailto:simon_park_mail AT yahoo DOT co DOT uk">Simon
 *         Park</a>
 * @version 3.0
 * @see java.text.SimpleDateFormat
 */
abstract class TimeBasedRollStrategy {

  static final TimeBasedRollStrategy ROLL_ERROR = new TimeBasedRollStrategy() {

    final boolean isRequiredStrategy(
        final LocalizedDateFormatPatternHelper localizedDateFormatPatternHelper,
        final String datePattern) {
      return false;
    }

    final long nextRollTimeInMillis(final long nowInMillis) {
      return 0L;
    }

    public String toString() {
      return "error";
    }
  };

  static final TimeBasedRollStrategy ROLL_EACH_MINUTE = new TimeBasedRollStrategy() {
    /**
     * @param localizedDateFormatPatternHelper
     * @param datePattern
     * @return <tt>true</tt> if the parameter contains &quot;m&quot;
     */
    final boolean isRequiredStrategy(
        final LocalizedDateFormatPatternHelper localizedDateFormatPatternHelper,
        final String datePattern) {
      final String patternRegex = localizedDateFormatPatternHelper
          .minutePatternRegex();
      return Pattern.matches(patternRegex, datePattern);
    }

    final long nextRollTimeInMillis(final long nowInMillis) {
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(nowInMillis);
      cal.set(Calendar.MILLISECOND, 0);
      cal.set(Calendar.SECOND, 0);
      cal.add(Calendar.MINUTE, 1);
      return cal.getTimeInMillis();
    }

    public String toString() {
      return "every minute";
    }
  };

  static final TimeBasedRollStrategy ROLL_EACH_HOUR = new TimeBasedRollStrategy() {
    /**
     * @param localizedDateFormatPatternHelper
     * @param datePattern
     * @return <tt>true</tt> if the parameter contains &quot;H&quot;,
     *         &quot;K&quot;, &quot;h&quot;, or &quot;k&quot;
     */
    final boolean isRequiredStrategy(
        final LocalizedDateFormatPatternHelper localizedDateFormatPatternHelper,
        final String datePattern) {
      final String patternRegex = localizedDateFormatPatternHelper
          .hourPatternRegex();
      return Pattern.matches(patternRegex, datePattern);
    }

    final long nextRollTimeInMillis(final long nowInMillis) {
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(nowInMillis);
      cal.set(Calendar.MILLISECOND, 0);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.add(Calendar.HOUR_OF_DAY, 1);
      return cal.getTimeInMillis();
    }

    public String toString() {
      return "hourly";
    }
  };

  static final TimeBasedRollStrategy ROLL_EACH_HALF_DAY = new TimeBasedRollStrategy() {
    /**
     * @param localizedDateFormatPatternHelper
     * @param datePattern
     * @return <tt>true</tt> if the parameter contains &quot;a&quot;
     */
    final boolean isRequiredStrategy(
        final LocalizedDateFormatPatternHelper localizedDateFormatPatternHelper,
        final String datePattern) {
      final String patternRegex = localizedDateFormatPatternHelper
          .amPmPatternRegex();
      return Pattern.matches(patternRegex, datePattern);
    }

    final long nextRollTimeInMillis(final long nowInMillis) {
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(nowInMillis);
      cal.set(Calendar.MILLISECOND, 0);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MINUTE, 0);
      int hour = cal.get(Calendar.HOUR_OF_DAY);
      if (hour < 12) {
        cal.set(Calendar.HOUR_OF_DAY, 12);
      } else {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.add(Calendar.DAY_OF_MONTH, 1);
      }
      return cal.getTimeInMillis();
    }

    public String toString() {
      return "half-daily";
    }
  };

  static final TimeBasedRollStrategy ROLL_EACH_DAY = new TimeBasedRollStrategy() {
    /**
     * @param localizedDateFormatPatternHelper
     * @param datePattern
     * @return <tt>true</tt> if the parameter contains &quot;D&quot;,
     *         &quot;d&quot;, &quot;F&quot;, or &quot;E&quot;
     */
    final boolean isRequiredStrategy(
        final LocalizedDateFormatPatternHelper localizedDateFormatPatternHelper,
        final String datePattern) {
      final String patternRegex = localizedDateFormatPatternHelper
          .dayPatternRegex();
      return Pattern.matches(patternRegex, datePattern);
    }

    final long nextRollTimeInMillis(final long nowInMillis) {
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(nowInMillis);
      cal.set(Calendar.MILLISECOND, 0);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.add(Calendar.DATE, 1);
      return cal.getTimeInMillis();
    }

    public String toString() {
      return "daily";
    }
  };

  static final TimeBasedRollStrategy ROLL_EACH_WEEK = new TimeBasedRollStrategy() {
    /**
     * @param localizedDateFormatPatternHelper
     * @param datePattern
     * @return <tt>true</tt> if the parameter contains &quot;W&quot;, or
     *         &quot;w&quot;
     */
    final boolean isRequiredStrategy(
        final LocalizedDateFormatPatternHelper localizedDateFormatPatternHelper,
        final String datePattern) {
      final String patternRegex = localizedDateFormatPatternHelper
          .weekPatternRegex();
      return Pattern.matches(patternRegex, datePattern);
    }

    final long nextRollTimeInMillis(final long nowInMillis) {
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(nowInMillis);
      cal.set(Calendar.MILLISECOND, 0);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
      cal.add(Calendar.WEEK_OF_YEAR, 1);
      return cal.getTimeInMillis();
    }

    public String toString() {
      return "weekly";
    }
  };

  static final TimeBasedRollStrategy ROLL_EACH_MONTH = new TimeBasedRollStrategy() {
    /**
     * @param localizedDateFormatPatternHelper
     * @param datePattern
     * @return <tt>true</tt> if the parameter contains &quot;M&quot;
     */
    final boolean isRequiredStrategy(
        final LocalizedDateFormatPatternHelper localizedDateFormatPatternHelper,
        final String datePattern) {
      final String patternRegex = localizedDateFormatPatternHelper
          .monthPatternRegex();
      return Pattern.matches(patternRegex, datePattern);
    }

    final long nextRollTimeInMillis(final long nowInMillis) {
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(nowInMillis);
      cal.set(Calendar.MILLISECOND, 0);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.DATE, 1);
      cal.add(Calendar.MONTH, 1);
      return cal.getTimeInMillis();
    }

    public String toString() {
      return "monthly";
    }
  };

  /**
   * Checks each available roll strategy in turn, starting at the per-minute
   * strategy, next per-hour, and so on for increasing units of time until a
   * match is found. If no match is found, the error strategy is returned.
   * 
   * @param properties
   * @return The appropriate roll strategy.
   */
  static final TimeBasedRollStrategy findRollStrategy(
      final AppenderRollingProperties properties) {
    if (properties.getDatePattern() == null) {
      LogLog.error("null date pattern");
      return ROLL_ERROR;
    }
    // Strip out quoted sections so that we may safely scan the undecorated
    // pattern for characters that are meaningful to SimpleDateFormat.
    final LocalizedDateFormatPatternHelper localizedDateFormatPatternHelper = new LocalizedDateFormatPatternHelper(
        properties.getDatePatternLocale());
    final String undecoratedDatePattern = localizedDateFormatPatternHelper
        .excludeQuoted(properties.getDatePattern());
    if (ROLL_EACH_MINUTE.isRequiredStrategy(localizedDateFormatPatternHelper,
        undecoratedDatePattern)) {
      return ROLL_EACH_MINUTE;
    }
    if (ROLL_EACH_HOUR.isRequiredStrategy(localizedDateFormatPatternHelper,
        undecoratedDatePattern)) {
      return ROLL_EACH_HOUR;
    }
    if (ROLL_EACH_HALF_DAY.isRequiredStrategy(localizedDateFormatPatternHelper,
        undecoratedDatePattern)) {
      return ROLL_EACH_HALF_DAY;
    }
    if (ROLL_EACH_DAY.isRequiredStrategy(localizedDateFormatPatternHelper,
        undecoratedDatePattern)) {
      return ROLL_EACH_DAY;
    }
    if (ROLL_EACH_WEEK.isRequiredStrategy(localizedDateFormatPatternHelper,
        undecoratedDatePattern)) {
      return ROLL_EACH_WEEK;
    }
    if (ROLL_EACH_MONTH.isRequiredStrategy(localizedDateFormatPatternHelper,
        undecoratedDatePattern)) {
      return ROLL_EACH_MONTH;
    }
    return ROLL_ERROR;
  }

  /**
   * @param localizedDateFormatPatternHelper
   * @param datePattern
   *          The appender's pattern, stripped of quoted sections that might
   *          interfere with pattern matching.
   * @return <tt>true</tt> if the implementor rolls by the unit of time found in
   *         the specified <tt>datePattern</tt>.
   */
  abstract boolean isRequiredStrategy(
      LocalizedDateFormatPatternHelper localizedDateFormatPatternHelper,
      String datePattern);

  /**
   * @param nowInMillis
   * @return The next roll time based upon the implementor's unit of time.
   */
  abstract long nextRollTimeInMillis(long nowInMillis);
}