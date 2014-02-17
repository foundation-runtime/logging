/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.log4j.helpers;

import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.ErrorHandler;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.Writer;

/**
 * Counts the number of characters written. Access to the count is synchronized
 * using the {@link java.io.Writer}'s lock. This is necessary because the count is
 * maintained as a <tt>long</tt>.
 *
 * @author <a href="mailto:simon_park_mail AT yahoo DOT co DOT uk">Simon
 *         Park</a>
 * @version 1.5
 * @see org.apache.log4j.helpers.CountingQuietWriter
 */
public final class SynchronizedCountingQuietWriter extends QuietWriter {

  private long charCount;

  public SynchronizedCountingQuietWriter(final Writer writer,
      final ErrorHandler eh) {
    super(writer, eh);
  }

  public final void write(final String string) {
    if (string == null) {
      /*
       * Create an exception here so that a stack trace is available for
       * debugging purposes.
       */
      errorHandler.error("Attempt to write null - see Log4J bug list.",
          new RuntimeException("Fault in Log4J Layout"),
          ErrorCode.WRITE_FAILURE);
      return;
    }
    try {
      synchronized (lock) {
        final int length = string.length();
        out.write(string, 0, length);
        this.charCount += length;
      }
    } catch (IOException e) {
      if (e instanceof InterruptedIOException) {
        Thread.currentThread().interrupt();
      }
      errorHandler.error("Failed to write [" + string + "].", e,
          ErrorCode.WRITE_FAILURE);
    }
  }

  public void flush() {
    try {
      synchronized (lock) {
        out.flush();
      }
    } catch (IOException e) {
      errorHandler.error("Failed to flush writer,", e, ErrorCode.FLUSH_FAILURE);
    }
  }

  public void close() throws IOException {
    synchronized (lock) {
      out.close();
    }
  }

  public final long getCount() {
    synchronized (lock) {
      return charCount;
    }
  }

  public final void setCount(final long count) {
    synchronized (lock) {
      this.charCount = count;
    }
  }
}
