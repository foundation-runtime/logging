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

import java.io.File;
import java.util.Iterator;

/**
 * Responsible for periodically sampling the number of log files with a given
 * base filename and time-based extension, and for deleting the older files if
 * the minimum specified space remains available on the filesystem.
 * <p>
 * Requires Java 6 and up.
 * <p>
 * The use case of this Scavenger is when you want to use all but a certain
 * amount of your disk space for log file to keep as much logs as your system
 * permits without worrying about running out of disk space.
 * <p>
 * To determine free disk space, the AbsoluteMinFreeDiskSpaceLogFileScavenger
 * calls {@link java.io.File#getUsableSpace()}. Javadoc says this method &quot;Returns
 * the number of bytes available to this virtual machine on the partition named
 * by this abstract pathname...&quot;.
 * <p>
 * When running more than one AbsoluteMinFreeDiskSpaceLogFileScavenger (e.g. in
 * multiple applications or for different appenders) on the same partition, care
 * must be taken when configuring them: All
 * AbsoluteMinFreeDiskSpaceLogFileScavenger working on the same partition must
 * have the same value for <tt>MinFreeDiskSpace</tt>. If this is not the case
 * and the partition fills up, the scavenger with the highest
 * <tt>MinFreeDiskSpace</tt> value will start deleting it's log files. Because
 * of this scavenger deleting log files, the other scavengers will not start
 * deleting log files because their <tt>MinFreeDiskSpace</tt> is lower and will
 * not be hit. Finally the scavenger with the highest <tt>MinFreeDiskSpace</tt>
 * will have deleted all it's log files (which is likely not what should
 * happen!) and only then the disk may fill further so that the scavengers with
 * the lower <tt>MinFreeDiskSpace</tt> will start deleting logs.
 * 
 * @author <a href="mailto:berndq AT gmx DOT net">Bernd</a>
 * @version 1.4
 */
public final class AbsoluteMinFreeDiskSpaceLogFileScavenger extends
    AbstractLogFileScavenger {

  public void scavenge() {
    // get complete sorted list of files
    final LogFileList logFileList = super.logFileList();
    if (logFileList.size() == 0) {
      return;
    }
    // TODO getUsableSpace() is a Java 1.6 feature!
    final long usableDiscSpace = logFileList.firstFile().getUsableSpace();
    final long minFreeDiscSpace = this.getProperties().getMinFreeDiscSpace();
    // log files use too much disk space?
    if (usableDiscSpace < minFreeDiscSpace) {
      // yes, then find out out many log files need to be deleted
      final long needToFree = minFreeDiscSpace - usableDiscSpace;
      LogLog.debug("Need to free " + needToFree
          + " bytes to get the configured minimum of " + minFreeDiscSpace
          + " bytes free disc space");
      final FileHelper fileHelper = FileHelper.getInstance();
      long freeableDiscSpace = 0L;
      // old ones are at start of list
      int lastToBeDeleted = -1;
      for (final Iterator iter = logFileList.iterator(); iter.hasNext();) {
        final File logFile = (File) iter.next();
        freeableDiscSpace += logFile.length();
        lastToBeDeleted++;
        if (freeableDiscSpace >= needToFree) {
          break;
        }
      }
      if (lastToBeDeleted == -1) {
        LogLog.debug("Should free " + needToFree
            + " bytes, but not deletable logfiles found");
        return;
      }
      if (lastToBeDeleted >= 0) {
        LogLog.debug("About to delete " + (lastToBeDeleted + 1)
            + " log file(s) which will recover " + freeableDiscSpace
            + " bytes on disk.");
      }
      long freed = 0L;
      for (int d = 0; d <= lastToBeDeleted; d++) {
        final File logFile = (File) logFileList.get(d);
        final long size = logFile.length();
        if (fileHelper.deleteExisting(logFile)) {
          LogLog.debug("Scavenged log file '" + logFile.getName()
              + "\', freed " + size + " bytes.");
          freed += size;
        }
      }
      LogLog.debug("Totally freed " + freed + " bytes.");
    } else {
      LogLog.debug("No need to scavenge log files: "
          + (usableDiscSpace - minFreeDiscSpace)
          + " bytes of disk space available for log files");
    }
  }
}
