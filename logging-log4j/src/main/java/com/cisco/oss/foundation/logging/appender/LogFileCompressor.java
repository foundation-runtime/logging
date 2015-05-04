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

import org.apache.log4j.helpers.FileHelper;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.ErrorCode;

import java.io.File;
import java.io.InterruptedIOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Responsible for compressing log files using a given compression algorithm,
 * adding checksums if specified.
 * 
 * @author <a href="mailto:simon_park_mail AT yahoo DOT co DOT uk">Simon
 *         Park</a>
 * @version 1.7
 */
final class LogFileCompressor implements Runnable, FileRollEventListener {

  private static final int QUEUE_LIMIT = 64;

  private final FoundationFileRollingAppender appender;

  private final AppenderRollingProperties properties;

  private Thread threadRef = null;
  private AtomicBoolean keepRunning;
  private final List queue;

  LogFileCompressor(final FoundationFileRollingAppender rollingAppender,
      final AppenderRollingProperties appenderRollingProperties) {
    super();
    this.appender = rollingAppender;
    this.properties = appenderRollingProperties;
    this.queue = new LinkedList();
	keepRunning=new AtomicBoolean();
	keepRunning.set(true);
  }

  public final void run() {
    LogLog.debug("Log file compressor started");
    try {
      while (this.isRunning() || !queueBelowMinSize()) {
        try {
          this.compressNext();
        } catch (InterruptedException e) {
          // game over
          Thread.currentThread().interrupt();
        } catch (InterruptedIOException e) {
          Thread.currentThread().interrupt();
        }
      }
    } catch (Exception e) {
      this.appender.getErrorHandler().error("Log file compressor failed", e,
          ErrorCode.GENERIC_FAILURE);
    }
    LogLog.debug("Log file compressor stopped");
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.log4j.appender.FileRollEventListener#onFileRoll(org.apache.log4j
   * .appender.FileRollEvent)
   */
  public final void onFileRoll(final FileRollEvent fileRollEvent) {
    this.compress(fileRollEvent.getBackupFile());
  }

  /**
   * For test purposes only.
   * 
   * @return max files in the backup file queue
   */
  final int getQueueLimit() {
    return QUEUE_LIMIT;
  }

  /**
   * For test purposes only.
   * 
   * @return number of files remaining in the backup file queue
   */
  final int getQueueSize() {
    int size = 0;
    synchronized (this.queue) {
      size = this.queue.size();
      this.queue.notifyAll();
    }
    return size;
  }

  /**
   * For test purposes only.
   */
  final void waitForEmptyQueue() {
    this.waitForSizeQueue(0);
  }

  /**
   * For test purposes only.
   */
  final void waitForSizeQueue(final int queueSize) {
    synchronized (this.queue) {
      while (this.queue.size() > queueSize) {
        try {
          this.queue.wait(250L);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
      try {
        Thread.sleep(500L);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      this.queue.notifyAll();
    }
  }

  /**
   * Starts the compressor.
   */
  final void begin() {
    if (LogFileCompressionStrategy.existsFor(this.properties)) {
      final Thread thread = new Thread(this, "Log4J File Compressor");
      thread.setDaemon(true);
      thread.start();
      this.threadRef = thread;
    }
  }

  /**
   * Stops the compressor.
   */
  final void end() {
    final Thread thread = this.threadRef;
    this.keepRunning.set(false);
    if (thread != null) {
     // thread.interrupt();
      try {
        thread.join();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    this.threadRef = null;
  }

  final void compress(final File file) {
    if (this.isRunning()) {
      synchronized (this.queue) {
        while (this.queue.size() > QUEUE_LIMIT) {
          if (this.properties.isCompressionBlocking()) {
            try {
              this.queue.wait(this.properties.getCompressionMaxWait());
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              return; // stop
            }
          } else {
            this.queue.remove(0);
          }
        }
        this.queue.add(file);
        this.queue.notifyAll();
      }
    }
  }

  private boolean isRunning() {
  //  return (this.threadRef != null) ? (!this.threadRef.isInterrupted()) : false;
	  return this.keepRunning.get();
  }

  private boolean queueBelowMinSize() {
    boolean belowMinSize = false;
    synchronized (this.queue) {
      final int compressionMinQueueSize = this.properties
          .getCompressionMinQueueSize();
      if (compressionMinQueueSize > 0) {
        belowMinSize = (this.queue.size() < compressionMinQueueSize);
      } else {
        belowMinSize = this.queue.isEmpty();
      }
      this.queue.notifyAll();
    }
    return belowMinSize;
  }

  private void compressNext() throws InterruptedException,
      InterruptedIOException {
    if (this.queue != null) {
      File file = null;
      synchronized (this.queue) {
        while (this.queueBelowMinSize()) {
          this.queue.wait(this.properties.getCompressionMaxWait());
          if(!this.keepRunning.get()){
				return;
			}
        }
        file = (File) this.queue.remove(0);
        this.queue.notifyAll();
      }
      if (file != null) {
        this.doCompression(file);
      }
    }
  }

  private void doCompression(final File file) {
    if (FileHelper.getInstance().isWriteable(file)) {
      final LogFileCompressionStrategy compressionStrategy = LogFileCompressionStrategy
          .findCompressionStrategy(this.properties);
      if (compressionStrategy != null) {
        compressionStrategy.compress(file, this.properties);
      }
    }
  }
}
