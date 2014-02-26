package com.cisco.oss.foundation.logging.appender;

import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.ErrorCode;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author <a href="mailto:simon_park_mail AT yahoo DOT co DOT uk">Simon
 *         Park</a>
 * @author <a href="mailto:berndq AT gmx DOT net">Bernd</a>
 * @version 1.4
 */
abstract class AbstractLogFileScavenger implements LogFileScavenger, Runnable {

  private FoundationFileRollingAppender appender = null;

  private File file = null;

  private AppenderRollingProperties properties = null;

  private Thread threadRef = null;

  AbstractLogFileScavenger() {
    super();
  }

  public final void init(final FoundationFileRollingAppender appender,
      final AppenderRollingProperties properties) {
    this.appender = appender;
    this.properties = properties;
  }

  /**
   * Starts the scavenger.
   */
  public final void begin() {
    this.file = this.getAppender().getIoFile();
    if (this.file == null) {
      this.getAppender().getErrorHandler()
          .error("Scavenger not started: missing log file name");
      return;
    }
    if (this.getProperties().getScavengeInterval() > -1) {
      final Thread thread = new Thread(this, "Log4J File Scavenger");
      thread.setDaemon(true);
      thread.start();
      this.threadRef = thread;
    }
  }

  /**
   * Stops the scavenger.
   */
  public final void end() {
    final Thread thread = threadRef;
    if (thread != null) {
      thread.interrupt();
      try {
        thread.join();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    this.threadRef = null;
  }

  public final void run() {
    LogLog.debug(this.getClass().getName() + " started");
    try {
      while (this.isRunning()) {
        this.scavenge();
        try {
          final long scavengeInterval = this.getProperties()
              .getScavengeInterval();
          if (scavengeInterval <= 0) {
            Thread.currentThread().interrupt();
          } else {
            Thread.sleep(scavengeInterval);
          }
        } catch (InterruptedException e) {
          // game over
          Thread.currentThread().interrupt();
        }
      }
    } catch (Exception e) {
      this.getAppender().getErrorHandler()
          .error("Log file scavenger failed", e, ErrorCode.GENERIC_FAILURE);
    }
    LogLog.debug("Log file scavenger stopped");
  }

  public final boolean isRunning() {
    return (this.threadRef != null) ? (!this.threadRef.isInterrupted()) : false;
  }

  final FoundationFileRollingAppender getAppender() {
    return this.appender;
  }

  final AppenderRollingProperties getProperties() {
    return this.properties;
  }

  final LogFileList logFileList() {
    final String filename = this.file.getName();
    return new LogFileList(this.file, new FilenameFilter() {

      public final boolean accept(final File logDir, final String name) {
        // select all but the base log filename, i.e. those that have
        // temporal/backup extensions
        return (!(name.equals(filename))) && name.startsWith(filename);
      }
    }, this.getProperties());
  }
}
