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

import org.apache.log4j.helpers.FileHelper;
import org.apache.log4j.helpers.LogLog;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author <a href="mailto:simon_park_mail AT yahoo DOT co DOT uk">Simon
 *         Park</a>
 * @version 2.1
 */
final class ZipFileCompressionStrategy extends LogFileCompressionStrategy {

  final String getAlgorithmName() {
    return "zip";
  }

  final boolean isCompressed(final File backupFile) {
    return FileHelper.getInstance().isZip(backupFile);
  }

  final boolean compress(final File backupFile, final File deflatedFile,
      final AppenderRollingProperties properties) {
    FileInputStream inputStream = null;
    ZipOutputStream outputStream = null;
    try {
      inputStream = this.createInputStream(backupFile);
      if (inputStream == null) {
        return false;
      }
      outputStream = this.createOutputStream(deflatedFile, properties);
      if (outputStream == null) {
        return false;
      }
      final long startTime = System.currentTimeMillis();
      if (!this.openEntry(backupFile, outputStream)) {
        return false;
      }
      if (!addEntry(inputStream, outputStream)) {
        return false;
      }
      if (!closeEntry(outputStream)) {
        return false;
      }
      LogLog.debug("Compressed in " + (System.currentTimeMillis() - startTime)
          + "ms");
    } finally {
      this.closeStreams(inputStream, outputStream);
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

  private ZipOutputStream createOutputStream(final File deflatedFile,
      final AppenderRollingProperties properties) {
    try {
      final ZipOutputStream outputStream = new ZipOutputStream(
          new BufferedOutputStream(new FileOutputStream(deflatedFile)));
      outputStream.setLevel(properties.getCompressionLevel());
      return outputStream;
    } catch (FileNotFoundException e) {
      LogLog.warn("Unable to open compressed log file", e);
      return null;
    }
  }

  private void closeStreams(final InputStream inputStream,
      final OutputStream outputStream) {
    if (inputStream != null) {
      try {
        inputStream.close();
      } catch (IOException e) {
        LogLog.warn("Unable to close backup log file", e);
      }
    }
    if (outputStream != null) {
      try {
        outputStream.close();
      } catch (IOException e) {
        LogLog.warn("Unable to close compressed log file", e);
      }
    }
  }

  private boolean addEntry(final FileInputStream inputStream,
      final ZipOutputStream outputStream) {
    FileChannel inputChannel = null;
    try {
      inputChannel = inputStream.getChannel();
      final WritableByteChannel outputChannel = Channels
          .newChannel(outputStream);
      inputChannel.transferTo(0, inputChannel.size(), outputChannel);
      // Original implementation
      // while (inputStream.available() > 0) {
      // int data = inputStream.read();
      // if (data == -1) {
      // break;
      // }
      // outputStream.write(data);
      // }
      return true;
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
    }
  }

  private boolean closeEntry(final ZipOutputStream outputStream) {
    try {
      outputStream.closeEntry();
      outputStream.finish();
      return true;
    } catch (IOException e) {
      LogLog.warn("Unable to complete log file compression", e);
      return false;
    }
  }

  private boolean openEntry(final File file, final ZipOutputStream outputStream) {
    try {
      ZipEntry zipEntry = new ZipEntry(file.getName());
      zipEntry.setMethod(ZipEntry.DEFLATED);
      outputStream.putNextEntry(zipEntry);
      return true;
    } catch (IOException e) {
      LogLog.warn("Unable to add zip entry to compressed log file", e);
      return false;
    }
  }
}