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

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

/**
 * Responsible for listing, sorting, and filtering log file lists.
 * 
 * @author <a href="mailto:simon_park_mail AT yahoo DOT co DOT uk">Simon
 *         Park</a>
 * @version 3.3
 */
final class LogFileList extends AbstractList implements List {

  private final BackupSuffixHelper backupSuffixHelper;

  private final File baseFile;

  private final FileHelper fileHelper;

  private final List fileList;

  private final FilenameFilter filenameFilter;

  /**
   * 
   */
  LogFileList(final File baseFile, final FilenameFilter filenameFilter,
      final AppenderRollingProperties properties) {
    super();
    this.backupSuffixHelper = new BackupSuffixHelper(properties);
    this.baseFile = baseFile;
    this.fileHelper = FileHelper.getInstance();
    this.fileList = new ArrayList();
    this.filenameFilter = filenameFilter;
    this.init();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.AbstractList#get(int)
   */
  public final Object get(int index) {
    return this.fileList.get(index);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.AbstractList#remove(int)
   */
  public final Object remove(int index) {
    return this.fileList.remove(index);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.AbstractCollection#size()
   */
  public final int size() {
    return this.fileList.size();
  }

  /**
   * @return The first {@link java.io.File} in the list, or null if the list is empty.
   */
  final File firstFile() {
    return (this.size() > 0) ? ((File) this.get(0)) : null;
  }

  /**
   * @return The last {@link java.io.File} in the list, or null if the list is empty.
   */
  final File lastFile() {
    return (this.size() > 0) ? ((File) this.get(this.size() - 1)) : null;
  }

  private void init() {
    File dir = null;
    if (this.baseFile.isDirectory()) {
      dir = this.baseFile;
    } else {
      dir = this.fileHelper.parentDirOf(this.baseFile);
    }
    if (dir != null) {
      this.readFileListFrom(dir);
      this.sortFileList();

    }
  }

  private void readFileListFrom(final File dir) {
    if (dir.exists() && dir.canRead()) {
      final File[] files = (this.filenameFilter != null) ? dir
          .listFiles(this.filenameFilter) : dir.listFiles();
      if (files != null) {
        this.fileList.addAll(Arrays.asList(files));
      }
    }
  }

  /**
   * Sort by time bucket, then backup count, and by compression state.
   */
  private void sortFileList() {
    if (this.size() > 1) {
      Collections.sort(this.fileList, new Comparator() {

        public final int compare(final Object o1, final Object o2) {
          final File f1 = (File) o1;
          final File f2 = (File) o2;
          final Object[] f1TimeAndCount = backupSuffixHelper
              .backupTimeAndCount(f1.getName(), baseFile);
          final Object[] f2TimeAndCount = backupSuffixHelper
              .backupTimeAndCount(f2.getName(), baseFile);
          final long f1TimeSuffix = ((Long) f1TimeAndCount[0]).longValue();
          final long f2TimeSuffix = ((Long) f2TimeAndCount[0]).longValue();
          if ((0L == f1TimeSuffix) && (0L == f2TimeSuffix)) {
            final long f1Time = f1.lastModified();
            final long f2Time = f2.lastModified();
            if (f1Time < f2Time) {
              return -1;
            }
            if (f1Time > f2Time) {
              return 1;
            }
            return 0;
          }
          if (f1TimeSuffix < f2TimeSuffix) {
            return -1;
          }
          if (f1TimeSuffix > f2TimeSuffix) {
            return 1;
          }
          final int f1Count = ((Integer) f1TimeAndCount[1]).intValue();
          final int f2Count = ((Integer) f2TimeAndCount[1]).intValue();
          if (f1Count < f2Count) {
            return -1;
          }
          if (f1Count > f2Count) {
            return 1;
          }
          if (f1Count == f2Count) {
            if (fileHelper.isCompressed(f1)) {
              return -1;
            }
            if (fileHelper.isCompressed(f2)) {
              return 1;
            }
          }
          return 0;
        }
      });
    }
  }
}
