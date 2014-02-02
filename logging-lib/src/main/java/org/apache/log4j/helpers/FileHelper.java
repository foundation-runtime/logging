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
package org.apache.log4j.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;

/**
 * @author <a href="mailto:simon_park_mail AT yahoo DOT co DOT uk">Simon
 *         Park</a>
 * @version 3.7
 */
public final class FileHelper {

  private static final FileHelper INSTANCE = new FileHelper();

  /**
   * @see <a href="http://www.pkware.com/documents/casestudies/APPNOTE.TXT">Zip
   *      file specification</a>
   */
  private static final long MAGIC_ZIP = ZipEntry.LOCSIG; // 0x04034b50L;

  /**
   * @see <a href="http://www.gzip.org/zlib/rfc-gzip.html">GZip file
   *      specification</a>
   */
  private static final long MAGIC_GZIP = GZIPInputStream.GZIP_MAGIC; // 0x8b1f;

  public static final FileHelper getInstance() {
    return INSTANCE;
  }

  private FileHelper() {
    super();
  }

  public long sizeOf(final File file) {
    return this.isReadable(file) ? file.length() : 0L;
  }

  public boolean isEmpty(final File file) {
    return (this.sizeOf(file) == 0L);
  }

  /**
   * Delete with retry.
   * 
   * @param file
   * @return <tt>true</tt> if the file was successfully deleted.
   */
  public boolean deleteExisting(final File file) {
    if (!file.exists()) {
      return true;
    }
    boolean deleted = false;
    if (file.canWrite()) {
      deleted = file.delete();
    } else {
      LogLog.debug(file + " is not writeable for delete (retrying)");
    }
    if (!deleted) {
      if (!file.exists()) {
        deleted = true;
      } else {
        file.delete();
        deleted = (!file.exists());
      }
    }
    return deleted;
  }

  /**
   * Rename with retry.
   * 
   * @param from
   * @param to
   * @return <tt>true</tt> if the file was successfully renamed.
   */
  public boolean rename(final File from, final File to) {
    boolean renamed = false;
    if (this.isWriteable(from)) {
      renamed = from.renameTo(to);
    } else {
      LogLog.debug(from + " is not writeable for rename (retrying)");
    }
    if (!renamed) {
      from.renameTo(to);
      renamed = (!from.exists());
    }
    return renamed;
  }

  public boolean isReadable(final File file) {
    return (file.exists() && file.canRead());
  }

  public boolean isWriteable(final File file) {
    return (file.exists() && file.canWrite());
  }

  /**
   * @param file
   * @return The parent {@link java.io.File} if one exists, the current working
   *         directory otherwise.
   */
  public File parentDirOf(final File file) {
    File parentDir = file.getParentFile();
    if (parentDir == null) {
      parentDir = new File(System.getProperty("user.dir"));
    }
    return parentDir;
  }

  public boolean isCompressed(final File file) {
    return this.isZip(file) || this.isGZip(file);
  }

  public boolean isZip(final File file) {
    long magic = 0L;
    if (this.isReadable(file)) {
      // read enough bytes for a Java long
      final byte[] magicBytes = new byte[4];
      this.readBytes(file, magicBytes);
      magic = this.getUnsigned32Bit(magicBytes, 0);
    }
    return MAGIC_ZIP == magic;
  }

  public boolean isGZip(final File file) {
    long magic = 0L;
    if (this.isReadable(file)) {
      // read enough bytes for a Java short
      final byte[] magicBytes = new byte[2];
      this.readBytes(file, magicBytes);
      magic = this.getUnsigned16Bit(magicBytes, 0);
    }
    return MAGIC_GZIP == magic;
  }

  /*
   * Fetches unsigned 16-bit value from byte array at specified offset. The
   * bytes are in little-endian byte order.
   */
  private int getUnsigned16Bit(final byte[] bytes, final int offset) {
    return (bytes[offset] & 0xff) | ((bytes[offset + 1] & 0xff) << 8);
  }

  /*
   * Fetches unsigned 32-bit value from byte array at specified offset. The
   * bytes are in little-endian byte order.
   */
  private long getUnsigned32Bit(final byte[] bytes, final int offset) {
    return getUnsigned16Bit(bytes, offset)
        | ((long) getUnsigned16Bit(bytes, offset + 2) << 16);
  }

  private void readBytes(final File file, final byte[] bytes) {
    FileChannel channel = null;
    try {
      channel = new FileInputStream(file).getChannel();
      final ByteBuffer buffer = ByteBuffer.wrap(bytes);
      int bytesRead = channel.read(buffer);
      if (bytesRead != bytes.length) {
        LogLog.debug("Incomplete log file");
      }
    } catch (ClosedByInterruptException e) {
      // may occur if we're closing down
      LogLog.debug("Interrupted during log file read");
      Arrays.fill(bytes, (byte) 0);
    } catch (FileNotFoundException e) {
      LogLog.debug("Unable to open log file (it may have been scavenged) "
          + e.getMessage());
      Arrays.fill(bytes, (byte) 0);
    } catch (IOException e) {
      LogLog.error("Unable to read log file", e);
      Arrays.fill(bytes, (byte) 0);
    } finally {
      if (channel != null) {
        try {
          channel.close();
        } catch (IOException e) {
          LogLog.error("Unable to close log file", e);
        }
      }
    }
  }
}
