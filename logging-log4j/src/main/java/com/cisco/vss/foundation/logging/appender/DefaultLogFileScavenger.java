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

import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * Responsible for periodically sampling the number of log files with a given
 * base filename and time-based extension, and for deleting the older files if
 * the file count limit is exceeded.
 * 
 * @author <a href="mailto:simon_park_mail AT yahoo DOT co DOT uk">Simon
 *         Park</a>
 * @version 1.3
 */
public final class DefaultLogFileScavenger extends AbstractLogFileScavenger {

  DefaultLogFileScavenger() {
    super();
  }

  public void scavenge() {
    final LogFileList logFileList = this.logFileList();
    final int fileListSize = logFileList.size();
    // count number of excess files
    final int maxRollFileCount = this.getProperties().getMaxRollFileCount();
    if (fileListSize >= maxRollFileCount) {
      // oldest ones at start of list won't be being written to, so delete
      // them
      final int toIndex = fileListSize - maxRollFileCount;
      final List subList = logFileList.subList(0, toIndex);
      final FileHelper fileHelper = FileHelper.getInstance();
      for (final Iterator iter = subList.iterator(); iter.hasNext();) {
        final File logFile = (File) iter.next();
        if (fileHelper.deleteExisting(logFile)) {
          LogLog.debug("Scavenged log file '" + logFile.getName() + '\'');
        }
      }
    }
  }

}
