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
package com.cisco.vss.foundation.logging.appender;

import org.apache.log4j.helpers.LogLog;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Responsible for encapsulating time-oriented operations on log filenames or
 * partial log filenames via the concatenation methods.
 * 
 * @author <a href="mailto:simon_park_mail AT yahoo DOT co DOT uk">Simon
 *         Park</a>
 * @version 2.3
 */
final class TimeSuffixHelper {

  private final DateFormat dateFormat;

  /**
   * Initialise to format log filenames and partial filenames using the current
   * default {@link java.util.Locale} and {@link java.util.TimeZone}.
   */
  TimeSuffixHelper(final AppenderRollingProperties properties) {
    super();
    /*
     * Must use a localized pattern due to the
     * LocalizedDateFormatPatternHelper's dependence upon local pattern chars.
     */
    final DateFormat df = new SimpleDateFormat(properties.getDatePattern(),
        properties.getDatePatternLocale());
    df.setLenient(false); // leniency can cause errors in the log file sort
    this.dateFormat = df;
  }

  final synchronized long toTime(final String filenameTimePart) {
    if ("".equals(filenameTimePart)) {
      return 0L;
    }
    try {
      return this.dateFormat.parse(filenameTimePart).getTime();
    } catch (ParseException e) {
      LogLog.warn("Unable to parse date from filename " + filenameTimePart);
      return 0L;
    }
  }

  /**
   * Returns the time part, used in a log filename suffix, for the period within
   * which the specified time falls.
   * <p>
   * Synchronized as used by both the appender and the log file scavenger.
   * 
   * @param timeForSuffix
   * @return Time part of a log filename.
   */
  final synchronized String toString(final long timeForSuffix) {
    return this.dateFormat.format(new Date(timeForSuffix));
  }
}
