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

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides support for processing {@link java.text.SimpleDateFormat} patterns.
 * <p>
 * <em>NB</em> Due to a dependence upon
 * {@link java.text.DateFormatSymbols#getLocalPatternChars()} any {@link java.text.DateFormat}
 * objects must also be initialised with
 * {@link java.text.SimpleDateFormat#applyLocalizedPattern(String)}.
 *
 * @see java.text.DateFormatSymbols
 * @see java.text.DateFormat
 * @see java.text.SimpleDateFormat
 * @author <a href="mailto:simon_park_mail AT yahoo DOT co DOT uk">Simon
 *         Park</a>
 * @version 2.0
 */
final class LocalizedDateFormatPatternHelper {

  private static final String ANY = ".*";

  private static final String OR = ".*|.*";

  /**
   * Format symbols for the default locale of the current VM instance, used to
   * construct regular expressions for date-time patterns.
   */
  private final DateFormatSymbols symbols;

  LocalizedDateFormatPatternHelper(final Locale datePatternLocale) {
    super();
    this.symbols = new DateFormatSymbols(datePatternLocale);
  }

  /**
   * @return A regular expression used to match only characters representing the
   *         'Minute in hour' part of a date-time pattern.
   */
  final String minutePatternRegex() {
    final StringBuffer regex = new StringBuffer();
    final String pattern = this.symbols.getLocalPatternChars();
    regex.append(ANY);
    regex.append(pattern.charAt(DateFormat.MINUTE_FIELD));
    regex.append(ANY);
    return regex.toString();
  }

  /**
   * @return A regular expression used to match only characters representing the
   *         'Hour in day (0-23)', 'Hour in day (1-24)', 'Hour in am/pm (0-11)',
   *         or 'Hour in am/pm (1-12)' part of a date-time pattern.
   */
  final String hourPatternRegex() {
    final StringBuffer regex = new StringBuffer();
    final String pattern = this.symbols.getLocalPatternChars();
    regex.append(ANY);
    regex.append(pattern.charAt(DateFormat.HOUR0_FIELD));
    regex.append(OR);
    regex.append(pattern.charAt(DateFormat.HOUR1_FIELD));
    regex.append(OR);
    regex.append(pattern.charAt(DateFormat.HOUR_OF_DAY0_FIELD));
    regex.append(OR);
    regex.append(pattern.charAt(DateFormat.HOUR_OF_DAY1_FIELD));
    regex.append(ANY);
    return regex.toString();
  }

  /**
   * @return A regular expression used to match only characters representing the
   *         'Am/pm marker' part of a date-time pattern.
   */
  final String amPmPatternRegex() {
    final StringBuffer regex = new StringBuffer();
    final String pattern = this.symbols.getLocalPatternChars();
    regex.append(ANY);
    regex.append(pattern.charAt(DateFormat.AM_PM_FIELD));
    regex.append(ANY);
    return regex.toString();
  }

  /**
   * @return A regular expression used to match only characters representing the
   *         'Day in year', 'Day in month', 'Day of week in month', or 'Day in
   *         week' part of a date-time pattern.
   */
  final String dayPatternRegex() {
    final StringBuffer regex = new StringBuffer();
    final String pattern = this.symbols.getLocalPatternChars();
    regex.append(ANY);
    regex.append(pattern.charAt(DateFormat.DATE_FIELD));
    regex.append(OR);
    regex.append(pattern.charAt(DateFormat.DAY_OF_WEEK_FIELD));
    regex.append(OR);
    regex.append(pattern.charAt(DateFormat.DAY_OF_WEEK_IN_MONTH_FIELD));
    regex.append(OR);
    regex.append(pattern.charAt(DateFormat.DAY_OF_YEAR_FIELD));
    regex.append(ANY);
    return regex.toString();
  }

  /**
   * @return A regular expression used to match only characters representing the
   *         'Week in year' or 'Week in month' part of a date-time pattern.
   */
  final String weekPatternRegex() {
    final StringBuffer regex = new StringBuffer();
    final String pattern = this.symbols.getLocalPatternChars();
    regex.append(ANY);
    regex.append(pattern.charAt(DateFormat.WEEK_OF_MONTH_FIELD));
    regex.append(OR);
    regex.append(pattern.charAt(DateFormat.WEEK_OF_YEAR_FIELD));
    regex.append(ANY);
    return regex.toString();
  }

  /**
   * @return A regular expression used to match only characters representing the
   *         'Month in year' part of a date-time pattern.
   */
  final String monthPatternRegex() {
    final StringBuffer regex = new StringBuffer();
    final String pattern = this.symbols.getLocalPatternChars();
    regex.append(ANY);
    regex.append(pattern.charAt(DateFormat.MONTH_FIELD));
    regex.append(ANY);
    return regex.toString();
  }

  /**
   * Strips out quoted sections from a date format pattern to leave us with only
   * characters meant to be interpreted by (or which are reserved by)
   * {@link java.text.SimpleDateFormat}.
   * 
   * @param datePattern
   *          The full pattern specified by the appender config.
   * @return The datePattern minus quoted sections.
   * @see java.text.SimpleDateFormat
   */
  final String excludeQuoted(final String datePattern) {
    final Pattern pattern = Pattern.compile("'('{2}|[^'])+'");
    final Matcher matcher = pattern.matcher(datePattern);
    final StringBuffer buffer = new StringBuffer();
    while (matcher.find()) {
      matcher.appendReplacement(buffer, "");
    }
    matcher.appendTail(buffer);
    LogLog.debug("DatePattern reduced to " + buffer.toString()
        + " for computation of time-based rollover strategy (full pattern"
        + " will be used for actual roll-file naming)");
    return buffer.toString();
  }
}
