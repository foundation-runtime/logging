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

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:simon_park_mail AT yahoo DOT co DOT uk">Simon
 *         Park</a>
 * @version 5.0
 */
final class BackupSuffixHelper {

  private Pattern pattern = null;

  private final TimeSuffixHelper timeHelper;

  BackupSuffixHelper(final AppenderRollingProperties properties) {
    super();
    this.timeHelper = new TimeSuffixHelper(properties);
  }

  final String defaultBackupCountAsString() {
    return Integer.toString(this.defaultBackupCount());
  }

  final String nextBackupCountAsString(final String logFilename,
      final File baseFile) {
    final int oldBackupSuffixNumber = this.backupCount(logFilename, baseFile);
    return Integer.toString(oldBackupSuffixNumber + 1);
  }

  final Object[] backupTimeAndCount(final String logFilename,
      final File baseFile) {
    final Pattern pattern = this.backupCountPattern(baseFile);
    final Matcher matcher = pattern.matcher(logFilename);
    if (matcher.find()) {
      // 2 = time, 4 = count
      final String timeStr = matcher.group(2);
      final String countStr = matcher.group(4);
      final long time = this.timeHelper.toTime(timeStr);
      final int count = this.backupCountFromString(countStr);
      return new Object[] { Long.valueOf(time), Integer.valueOf(count) };
    }
    return new Object[] { Long.valueOf(0L), Integer.valueOf(0) };
  }

  // TODO Delete and change this method's regex test for backupTimeAndCount
  final String backupTimeAsString(final String logFilename, final File baseFile) {
    final Pattern pattern = this.backupCountPattern(baseFile);
    final Matcher matcher = pattern.matcher(logFilename);
    if (matcher.find()) {
      return matcher.group(2); // date formatted part is group 2
    }
    return "";
  }

  final String backupTimeAsString(final long timeForSuffix) {
    return this.timeHelper.toString(timeForSuffix);
  }

  private int backupCount(final String logFilename, final File baseFile) {
    final String backupSuffix = this.backupCountAsString(logFilename, baseFile);
    return this.backupCountFromString(backupSuffix);
  }

  private String backupCountAsString(final String logFilename,
      final File baseFile) {
    final Pattern pattern = this.backupCountPattern(baseFile);
    final Matcher matcher = pattern.matcher(logFilename);
    if (matcher.find()) {
      return matcher.group(4); // backup count is group 4
    }
    return "";
  }

  private int backupCountFromString(final String countStr) {
    if ("".equals(countStr)) {
      return 0; // never zero - this is for sorting file lists
    }
    try {
      return Integer.decode(countStr).intValue();
    } catch (NumberFormatException e) {
      return this.defaultBackupCount();
    }
  }

  private synchronized Pattern backupCountPattern(final File baseFile) {
    if (this.pattern == null) {
      final LogFileCompressionStrategy[] compressionStrategies = LogFileCompressionStrategy
          .strategies();
      /*
       * Match (1) the base filename, (2) the date pattern part, (3) the dot
       * separator between the date part and the backup count, (4) the count
       * itself, and (5) either the end of input, or a valid compression
       * extension (.zip, .gz, etc) plus the end of input.
       */
      final StringBuffer patternBuffer = new StringBuffer();
      patternBuffer.append("(\\Q").append(baseFile.getName()).append("\\E)");
      patternBuffer.append("(.+)(\\.)([0-9]+)($");
      for (int i = 0; i < compressionStrategies.length; i++) {
        patternBuffer.append("|\\.");
        patternBuffer.append(compressionStrategies[i].getAlgorithmName());
        patternBuffer.append('$');
      }
      patternBuffer.append(')');
      this.pattern = Pattern.compile(patternBuffer.toString());
    }
    return this.pattern;
  }

  private int defaultBackupCount() {
    return 1;
  }
}
