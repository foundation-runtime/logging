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
import org.apache.log4j.helpers.LogLog;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.zip.GZIPOutputStream;

/**
 * @author <a href="mailto:simon_park_mail AT yahoo DOT co DOT uk">Simon
 *         Park</a>
 * @author <a href="mailto:axvpast AT gmail DOG com">Oleksiy Pastukhov</a>
 * @version 2.1
 */
final class GzipFileCompressionStrategy extends LogFileCompressionStrategy {

  private static final int DEFLATER_BUFFER_SIZE = 131071;

  final String getAlgorithmName() {
    return "gz";
  }

  final boolean isCompressed(final File backupFile) {
    return FileHelper.getInstance().isGZip(backupFile);
  }

  final boolean compress(final File backupFile, final File deflatedFile,
      final AppenderRollingProperties properties) {
    final FileInputStream inputStream = this.createInputStream(backupFile);
    if (inputStream == null) {
      return false;
    }
    final GZIPOutputStream outputStream = this.createGZipOutputStream(
        deflatedFile, properties);
    if (outputStream == null) {
      return false;
    }
    if (!this.compress(inputStream, outputStream)) {
      return false;
    }
    return true;
  }

  private FileInputStream createInputStream(final File file) {
    try {
      return new FileInputStream(file);
    } catch (FileNotFoundException e) {
      // may occur if backup has been scavenged
      LogLog.debug("Unable to open backup log file", e);
      return null;
    }
  }

  private GZIPOutputStream createGZipOutputStream(final File deflatedFile,
      final AppenderRollingProperties properties) {
    try {
      return new GZIPOutputStream(new BufferedOutputStream(
          new FileOutputStream(deflatedFile)), DEFLATER_BUFFER_SIZE);
    } catch (IOException e) {
      LogLog.warn("Unable to open compressed log file", e);
      return null;
    }
  }

  private boolean compress(final FileInputStream inputStream,
      final GZIPOutputStream outputStream) {
    final long startTime = System.currentTimeMillis();
    FileChannel inputChannel = null;
    WritableByteChannel outputChannel = null;
    try {
      inputChannel = inputStream.getChannel();
      outputChannel = Channels.newChannel(outputStream);
      inputChannel.transferTo(0, inputChannel.size(), outputChannel);
      outputStream.finish();
    } catch (ClosedByInterruptException e) {
      // may occur if we're closing down
      LogLog.debug("Compression operation interrupted");
      return false;
    } catch (IOException e) {
      LogLog.warn("Unable to transfer data from backup log file"
          + " to compressed log file", e);
      return false;
    } finally {
      if (inputChannel != null) {
        try {
          inputChannel.close();
        } catch (IOException e) {
          LogLog.warn("Unable to close backup log file", e);
        }
      }
      if (outputChannel != null) {
        try {
          outputChannel.close();
        } catch (IOException e) {
          LogLog.warn("Unable to close compressed log file", e);
        }
      }
    }
    LogLog.debug("Compressed in " + (System.currentTimeMillis() - startTime)
        + "ms");
    return true;

    // Original implementation
    // try {
    // while (inputStream.available() > 0) {
    // int data = inputStream.read();
    // if (data == -1) {
    // break;
    // }
    // outputStream.write(data);
    // }
    // outputStream.finish();
    // return true;
    // } catch (IOException e) {
    // LogLog.warn("Unable to transfer data from backup log file"
    // + " to compressed log file", e);
    // return false;
    // }
  }
}