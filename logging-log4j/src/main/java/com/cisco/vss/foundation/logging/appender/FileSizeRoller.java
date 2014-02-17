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
import org.apache.log4j.spi.LoggingEvent;

import java.io.File;

/**
 * Responsible for deciding whether a file roll should take place based upon
 * file size limits, and for performing a file roll if necessary.
 * 
 * @author <a href="mailto:simon_park_mail AT yahoo DOT co DOT uk">Simon
 *         Park</a>
 * @version 2.2
 */
final class FileSizeRoller extends AbstractRoller implements FileRollable {

  FileSizeRoller(final FoundationFileRollingAppender rollingAppender,
      final AppenderRollingProperties appenderRollingProperties) {
    super(rollingAppender, appenderRollingProperties);
    this.initActualBytesWrittenCount();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.log4j.appender.FileRollable#roll()
   */
  public final boolean roll(final LoggingEvent loggingEvent) {
    boolean rolled = false;
    if (this.isMaxFileSizeExceeded()) {
      super.roll(loggingEvent.getTimeStamp());
      rolled = true;
    }
    return rolled;
  }

  private boolean isMaxFileSizeExceeded() {
    return (super.getProperties().getBytesWrittenCount() > super
        .getProperties().getMaxFileSize());
  }

  /**
   * Updates the internal writer with the actual number of bytes written to the
   * file.
   * 
   * @return The byte count.
   */
  private long initActualBytesWrittenCount() {
    long byteCount = this.readActualBytesWrittenCount();
    super.getProperties().setBytesWrittenCount(byteCount);
    return byteCount;
  }

  /**
   * @return The actual number of bytes written to the file.
   */
  private long readActualBytesWrittenCount() {
    final File file = super.getAppender().getIoFile();
    if (file != null) {
      return FileHelper.getInstance().sizeOf(file);
    } else {
      super.getAppender().getErrorHandler()
          .error("Bytes not counted: missing file name");
    }
    return 0L;
  }
}
