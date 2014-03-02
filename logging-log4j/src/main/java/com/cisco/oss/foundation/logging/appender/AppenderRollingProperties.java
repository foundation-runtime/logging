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

import org.apache.log4j.helpers.SynchronizedCountingQuietWriter;

import java.util.Locale;
import java.util.zip.Deflater;

/**
 * Configuration properties used by the {@link FoundationFileRollingAppender}.
 * 
 * @author <a href="mailto:simon_park_mail AT yahoo DOT co DOT uk">Simon
 *         Park</a>
 * @version 2.2
 */
final class AppenderRollingProperties {

  private final Object compressionLock = new Object();

  private final Object countingWriterLock = new Object();

  private final Object scavengerLock = new Object();

  private final Object rollTimeLock = new Object();

  private final Object startupRollLock = new Object();

  /** The default maximum file size is 10MB. */
  private long maxFileSize = 100 * 1024 * 1024;

  /**
   * Maximum number of files that will be accumulated before the appender begins
   * removing the oldest files. Defaults to 10.
   */
  private int maxRollFileCount = 100;

  /**
   * The date pattern. By default, the pattern is set to "'.'yyyy-MM-dd" meaning
   * daily rollover.
   */
  private String datePattern = ".yyyy-MM-dd";

  /**
   * Defaults to {@link java.util.Locale#ENGLISH}.
   */
  private Locale datePatternLocale = null;

  private boolean filterEmptyMessages=false;

  /**
   * Defaults to <tt>false</tt>.
   */
  private boolean dateRollEnforced = false;

  /**
   * Default scavenge interval is 30 seconds.
   */
  private long scavengeInterval = 30 * 1000;

  /**
   * Default disk space that must not be used for log files.
   */
  private long minFreeDiscSpace = -1;

  /**
   * The name of the compression algorithm to use. Defaults to none (empty
   * string).
   */
  private String compressionAlgorithm = "";

  /**
   * The compression level used by the ZIP algorithm. Defaults to
   * {@link java.util.zip.Deflater#BEST_SPEED}.
   */
  private int compressionLevel = Deflater.BEST_SPEED;

  /**
   * Default interval between checks for backup files due for compression is 1
   * second.
   */
  private long compressionMaxWait = 1 * 1000;

  /**
   * Default number of backup files that must be in the queue before any
   * compression takes place. Defaults to zero.
   */
  private int compressionMinQueueSize = 0;

  /**
   * Defaults to <tt>false</tt>.
   */
  private boolean compressionBlocking = false;

  /**
   * The writer used by the appender that allows counting of bytes written.
   */
  private SynchronizedCountingQuietWriter countingQuietWriter = null;

  /**
   * The next time (UTC) a rollover is due, in milliseconds, or 0 if none has
   * yet been recorded. <em>Not</em> a configuration property.
   */
  private long nextRollOverTime = 0L;

  /**
   * Indicates whether the appender should roll upon receiving the first log
   * event after being activated, regardless of time or file size. Defaults to
   * <tt>false</tt>.
   */
  private boolean rollOnStartup = false;

  /**
   * Indicates whether the appender rolled on startup on first activation.
   * Defaults to <tt>false</tt>.
   * 
   * @see #rollOnStartup
   */
  private boolean rolledOnStartup = false;
  
  /**
   * In case HistoryLogFileScavenger is on- this parameter will determine how much days to keep logs from. 
   */
  private int maxFileAge = 7;
  

  AppenderRollingProperties() {
    super();
  }

  // Byte counting properties

  /**
   * @return The number of bytes accounted for by the internal writer.
   */
  final long getBytesWrittenCount() {
    synchronized (this.countingWriterLock) {
      return (this.countingQuietWriter != null) ? this.countingQuietWriter
          .getCount() : 0L;
    }
  }

  /**
   * @param byteCount
   *          The number of bytes already written to a file.
   */
  final void setBytesWrittenCount(final long byteCount) {
    synchronized (this.countingWriterLock) {
      if (this.countingQuietWriter != null) {
        this.countingQuietWriter.setCount(byteCount);
      }
    }
  }

  final SynchronizedCountingQuietWriter getCountingQuietWriter() {
    synchronized (this.countingWriterLock) {
      return this.countingQuietWriter;
    }
  }

  final void setCountingQuietWriter(
      final SynchronizedCountingQuietWriter countingQuietWriter) {
    synchronized (this.countingWriterLock) {
      this.countingQuietWriter = countingQuietWriter;
    }
  }

  // Date pattern properties

  final String getDatePattern() {
    return (this.datePattern != null) ? this.datePattern : "";
  }

  final void setDatePattern(final String datePattern) {
    this.datePattern = datePattern;
  }

  final Locale getDatePatternLocale() {
    return (this.datePatternLocale != null) ? this.datePatternLocale
        : Locale.ENGLISH;
  }

  final void setDatePatternLocale(Locale datePatternLocale) {
    this.datePatternLocale = datePatternLocale;
  }

  // Roll enforcement and time tracking properties

  final boolean isDateRollEnforced() {
    return this.dateRollEnforced;
  }

  final void setDateRollEnforced(final boolean dateRollEnforced) {
    this.dateRollEnforced = dateRollEnforced;
  }

  final long getNextRollOverTime() {
    long time = 0L;
    synchronized (this.rollTimeLock) {
      time = this.nextRollOverTime;
      this.rollTimeLock.notifyAll();
    }
    return time;
  }

  final long getNextRollOverTimeWithWait() {
    long time = 0L;
    synchronized (this.rollTimeLock) {
      while (this.nextRollOverTime == 0L) {
        try {
          this.rollTimeLock.wait();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
      time = this.nextRollOverTime;
      this.rollTimeLock.notifyAll();
    }
    return time;
  }

  final void setNextRollOverTime(final long nextRollOverTime) {
    synchronized (this.rollTimeLock) {
      this.nextRollOverTime = nextRollOverTime;
      this.rollTimeLock.notifyAll();
    }
  }

  // File size rollover properties

  synchronized final long getMaxFileSize() {
    return this.maxFileSize;
  }

  synchronized final void setMaxFileSize(final long maxFileSize) {
    if (maxFileSize >= 0L) {
      this.maxFileSize = maxFileSize;
    }
  }
  synchronized final int getMaxFileAge() {
	    return this.maxFileAge;
	  }

	  synchronized final void setMaxFileAge(final int maxFileAge) {
	    if (maxFileAge >= 0) {
	      this.maxFileAge = maxFileAge;
	    }
	  }

  final boolean getFilterEmptyMessages(){
	  return filterEmptyMessages;
  }
  
  final void setFilterEmptyMessages(final boolean filterEmptyMessages){
	  this.filterEmptyMessages=filterEmptyMessages;
  }
  
  // Startup rollover properties

  final boolean shouldRollOnStartup() {
    synchronized (this.startupRollLock) {
      return this.rollOnStartup;
    }
  }

  final void setRollOnStartup(final boolean rollOnStartup) {
    synchronized (this.startupRollLock) {
      this.rollOnStartup = rollOnStartup;
    }
  }

  final boolean wasRolledOnStartup() {
    synchronized (this.startupRollLock) {
      return rolledOnStartup;
    }
  }

  /**
   * @return <tt>true</tt> if the appender should now go ahead and roll
   */
  final boolean updateRolledOnStartup() {
    synchronized (this.startupRollLock) {
      if (this.rolledOnStartup) {
        return false;
      }
      this.rolledOnStartup = true; // past tense
      return this.rollOnStartup; // present tense
    }
  }

  final boolean shouldRollOnActivation() {
    synchronized (this.startupRollLock) {
      return (this.rollOnStartup && (!this.rolledOnStartup));
    }
  }

  // Scavenger properties

  final int getMaxRollFileCount() {
    synchronized (this.scavengerLock) {
      return this.maxRollFileCount;
    }
  }

  final void setMaxRollFileCount(final int maxRollFileCount) {
    synchronized (this.scavengerLock) {
      if (maxRollFileCount >= 0) {
        this.maxRollFileCount = maxRollFileCount;
      }
    }
  }

  final long getMinFreeDiscSpace() {
    synchronized (this.scavengerLock) {
      return this.minFreeDiscSpace;
    }
  }

  final void setMinFreeDiscSpace(final long minFreeDiskSpace) {
    synchronized (this.scavengerLock) {
      if (minFreeDiskSpace >= 0L) {
        this.minFreeDiscSpace = minFreeDiskSpace;
      }
    }
  }

  /**
   * @return the scavengeInterval
   */
  final long getScavengeInterval() {
    synchronized (this.scavengerLock) {
      return this.scavengeInterval;
    }
  }

  /**
   * @param scavengeInterval
   *          the scavengeInterval to set
   */
  final void setScavengeInterval(final long scavengeInterval) {
    synchronized (this.scavengerLock) {
      if (scavengeInterval >= -1) {
        this.scavengeInterval = scavengeInterval;
      }
    }
  }

  // Compressor properties

  final String getCompressionAlgorithm() {
    synchronized (this.compressionLock) {
      return compressionAlgorithm;
    }
  }

  final void setCompressionAlgorithm(final String compressionAlgorithm) {
    synchronized (this.compressionLock) {
      this.compressionAlgorithm = (compressionAlgorithm != null) ? compressionAlgorithm
          .trim() : "";
    }
  }

  final long getCompressionMaxWait() {
    synchronized (this.compressionLock) {
      return compressionMaxWait;
    }
  }

  final void setCompressionMaxWait(final long compressionMaxWait) {
    synchronized (this.compressionLock) {
      if (compressionMaxWait >= 0) {
        this.compressionMaxWait = compressionMaxWait;
      }
    }
  }

  final int getCompressionLevel() {
    synchronized (this.compressionLock) {
      return compressionLevel;
    }
  }

  final void setCompressionLevel(final int compressionLevel) {
    synchronized (this.compressionLock) {
      if ((compressionLevel >= Deflater.DEFAULT_COMPRESSION)
          && (compressionLevel <= Deflater.BEST_COMPRESSION)) {
        this.compressionLevel = compressionLevel;
      }
    }
  }

  final boolean isCompressionBlocking() {
    synchronized (this.compressionLock) {
      return compressionBlocking;
    }
  }

  final void setCompressionBlocking(final boolean compressionBlocks) {
    synchronized (this.compressionLock) {
      this.compressionBlocking = compressionBlocks;
    }
  }

  final int getCompressionMinQueueSize() {
    synchronized (this.compressionLock) {
      return compressionMinQueueSize;
    }
  }

  final void setCompressionMinQueueSize(final int compressionMinQueueSize) {
    synchronized (this.compressionLock) {
      if (compressionMinQueueSize >= 0) {
        this.compressionMinQueueSize = compressionMinQueueSize;
      }
    }
  }
}
