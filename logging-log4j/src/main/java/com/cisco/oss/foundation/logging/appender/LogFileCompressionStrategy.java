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

import java.io.File;
import java.io.IOException;

/**
 * Responsible for providing compression strategy instances (via factory
 * methods), implementing template methods used by specific strategies, and the
 * creation of compressed backup files.
 * 
 * @author <a href="mailto:simon_park_mail AT yahoo DOT co DOT uk">Simon
 *         Park</a>
 * @version 1.4
 */
abstract class LogFileCompressionStrategy {

  static final LogFileCompressionStrategy ZIP = new ZipFileCompressionStrategy();

  static final LogFileCompressionStrategy GZIP = new GzipFileCompressionStrategy();

  static final LogFileCompressionStrategy DEFAULT = new LogFileCompressionStrategy() {

    boolean compress(File backupFile, File deflatedFile,
        AppenderRollingProperties properties) {
      return false;
    }

    String getAlgorithmName() {
      return "error";
    }

    boolean isCompressed(File backupFile) {
      return true;
    }

  };

  static final boolean existsFor(final AppenderRollingProperties properties) {
    return (!LogFileCompressionStrategy.DEFAULT
        .equals(findCompressionStrategy(properties)));
  }

  static final LogFileCompressionStrategy findCompressionStrategy(
      final AppenderRollingProperties properties) {
    if (ZIP.isRequiredStrategy(properties)) {
      LogLog.debug("Using ZIP compression");
      return ZIP;
    }
    if (GZIP.isRequiredStrategy(properties)) {
      LogLog.debug("Using GZIP compression");
      return GZIP;
    }
    LogLog.debug("Not using compression");
    return DEFAULT;
  }

  static final LogFileCompressionStrategy[] strategies() {
    // default strategy must not be included
    return new LogFileCompressionStrategy[] { ZIP, GZIP };
  }

  /**
   * Compress the backup file as per the implemented algorithm.
   * 
   * @param backupFile
   *          The file to be compressed.
   * @param deflatedFile
   *          The target file to which the compressed stream will be written.
   * @param properties
   *          The appender's configuration.
   * @return <tt>true</tt> if compression was successful
   */
  abstract boolean compress(File backupFile, File deflatedFile,
      AppenderRollingProperties properties);

  /**
   * @return The name to be used in the suffix of the compressed file.
   */
  abstract String getAlgorithmName();

  abstract boolean isCompressed(File backupFile);

  /**
   * @param properties
   *          The appender's configuration.
   * @return <tt>true</tt> if the algorithm name of this strategy matches the
   *         configured name.
   */
  final boolean isRequiredStrategy(final AppenderRollingProperties properties) {
    return this.getAlgorithmName().equalsIgnoreCase(
        properties.getCompressionAlgorithm());
  }

  /**
   * Template method responsible for file compression checks, file creation, and
   * delegation to specific strategy implementations.
   * 
   * @param backupFile
   *          The file to be compressed.
   * @param properties
   *          The appender's configuration.
   */
  final void compress(final File backupFile,
      final AppenderRollingProperties properties) {
    if (this.isCompressed(backupFile)) {
      LogLog.debug("Backup log file " + backupFile.getName()
          + " is already compressed");
      return; // try not to do unnecessary work
    }
    final long lastModified = backupFile.lastModified();
    if (0L == lastModified) {
      LogLog.debug("Backup log file " + backupFile.getName()
          + " may have been scavenged");
      return; // backup file may have been scavenged
    }
    final File deflatedFile = this.createDeflatedFile(backupFile);
    if (deflatedFile == null) {
      LogLog.debug("Backup log file " + backupFile.getName()
          + " may have been scavenged");
      return; // an error occurred creating the file
    }
    if (this.compress(backupFile, deflatedFile, properties)) {
      deflatedFile.setLastModified(lastModified);
      FileHelper.getInstance().deleteExisting(backupFile);
      LogLog.debug("Compressed backup log file to " + deflatedFile.getName());
    } else {
      FileHelper.getInstance().deleteExisting(deflatedFile); // clean up
      LogLog
          .debug("Unable to compress backup log file " + backupFile.getName());
    }
  }

  private File createDeflatedFile(final File backupFile) {
    try {
      final FileHelper fileHelper = FileHelper.getInstance();
      final File deflatedFile = new File(fileHelper.parentDirOf(backupFile),
          backupFile.getName() + '.' + this.getAlgorithmName());
      fileHelper.deleteExisting(deflatedFile);
      if (deflatedFile.createNewFile()) {
        return deflatedFile;
      } else {
        LogLog.warn("Unable to create compressed backup log file");
        return null;
      }
    } catch (IOException e) {
      LogLog.error("Unable to create compressed backup log file", e);
      return null;
    }
  }
}