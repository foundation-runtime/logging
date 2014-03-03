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

import com.cisco.oss.foundation.logging.FoundationLof4jLoggingEvent;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.helpers.SynchronizedCountingQuietWriter;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

/**
 * This appender is responsible for writing log events to file, and rolling
 * files when they reach configurable limits of time and/or size. It is
 * configured and behaves in much the same way as the
 * {@link org.apache.log4j.DailyRollingFileAppender}, with some useful
 * differences. <h2>Maximum file size</h2>
 * <p>
 * Maximum log file size can be configured (default 10MB) using the
 * <tt>MaxFileSize</tt> property. This means that log files can be rolled within
 * the configured time period if the maximum file size is exceeded. Backups are
 * indicated by a count suffix at the end of log filenames, e.g. for the time
 * suffix specified by the default pattern &quot;<tt>'.'yyyy-MM-dd</tt>&quot;
 * (which would ordinarily result in a log file named
 * e.g.&nbsp;&quot;foo.log.2007-01-01&quot;) will result in backups named
 * &quot;foo.log.2007-01-01<b>.1</b>&quot;,
 * &quot;foo.log.2007-01-01<b>.2</b>&quot;, etc.
 * <h2>Maximum number of backup files</h2>
 * <p>
 * Configuring the maximum number of allowable backup log files (default 10)
 * using the <tt>MaxRollFileCount</tt> property helps prevent this appender from
 * consuming disk space without limit. Excess backup log files are scavenged by
 * a background thread. Configuring the scavenging interval (default 30 seconds)
 * using the <tt>ScavengeInterval</tt> property specifies the duration for which
 * the scavenger thread should sleep between operations. The log file scavenger
 * only operates upon files that start with the path specified by the
 * <tt>File</tt> configuration parameter. Older files will be deleted first.
 * Setting a scavenge interval of <tt>-1</tt> prevents the scavenger from
 * running.
 * <h2>Backup file compression</h2>
 * <p>
 * Backup log file compression may be configured using the allowed compression
 * algorithms specified by the <tt>CompressionAlgorithm</tt> property:
 * <ul>
 * <li>&quot;ZIP&quot; for zip compression</li>
 * <li>&quot;GZ&quot; for gzip compression</li>
 * </ul>
 * <p>
 * Default behaviour is not to compress backup log files unless the
 * <tt>CompressionAlgorithm</tt> property is configured. Backup files are
 * compressed by a background thread. At roll time the name of the backup log
 * {@link java.io.File} object is put into the compressor thread's FIFO queue. By
 * default the compressor works on a best-effort basis: if the queue fills up,
 * then older backup filenames are discarded and will therefore not be
 * compressed.
 * <p>
 * The appender can be configured to force compression of all backup files by
 * setting the <tt>CompressionUseBlockingQueue</tt> property to &quot;
 * <tt>true</tt>&quot; (default is &quot;<tt>false</tt>&quot;). Forcing
 * compression comes at the cost of blocking the compressor's queue (and
 * therefore the application thread that invoked the {@link org.apache.log4j.Logger}). The
 * application will cease to block once the current compression operation has
 * completed and the compressor has removed the next file from its queue.
 * <p>
 * The appender's <tt>CompressionMinQueueSize</tt> property (default 0) controls
 * the minimum number of backup files that must be in the queue awaiting
 * compression before any compression will take actually take place. For
 * example, if this property is set to 5, then at least 5 backup file rolls must
 * take place before the oldest file currently in the compressor's queue will be
 * compressed. Keeping the most recent files uncompressed can be helpful at
 * support time.
 * <h2>Time boundaries</h2>
 * <p>
 * Setting the appender's <tt>DateRollEnforced</tt> property to <tt>true</tt>
 * (default <tt>false</tt>) activates pro-active log rolling at time boundaries.
 * Time boundaries are enforced by a background thread. The standard
 * {@link org.apache.log4j.DailyRollingFileAppender} only rolls log files
 * reactively upon the dispatch of a logging event. This appender allows
 * pro-active control over log rolling by enforcing a schedule implied by the
 * <tt>DatePattern</tt> property. For example, &lt;param
 * name=&quot;DatePattern&quot; value=&quot;.yyyy-MM-dd&quot;/&gt; will see the
 * log file roll at the end of the day, even if the application is otherwise
 * inactive. Similarly &lt;param name=&quot;DatePattern&quot;
 * value=&quot;.yyyy-MM-dd-HH&quot;/&gt; will result in log files being rolled
 * every hour.
 * <h2>File roll events</h2>
 * <p>
 * A custom message of your choosing may be written into the first line of each
 * new log file created after a file roll has completed. This is achieved by
 * setting the <tt>FileRollEventMessage</tt> property to a message string. If
 * this property is configured with a blank value (e.g. &lt;param
 * name=&quot;FileRollEventMessage&quot;/&gt;), the appender will ensure that a
 * default message is written at the top of the new log file instead. If this
 * property is not set, no message will be written to the top of new log files.
 * Messages are appended at {@link org.apache.log4j.Level#ALL} using the root
 * logger.
 * <h2>Roll on start-up</h2>
 * <p>
 * The appender can be configured to roll the most recent backup file,
 * regardless of the file's last modification time or size, immediately after
 * receiving the first logging event after the appender's options are activated.
 * <p>
 * For example, say the appender is configured to roll every hour, or for files
 * exceeding 10MB in size, and that a 1 MB log file exists that was last written
 * 10 minutes before the hour. The application is restarted 5 minutes before the
 * hour and logs an event. When the <tt>RollOnStartup</tt> property is set to
 * <tt>true</tt>, the log file described in this example scenario will be rolled
 * into a backup, and a new log file will be created.
 * <h2>Sample configurations</h2>
 * <h3>Sample 1</h3>
 * <p>
 * An example configuration snippet taken from an actual Log4J XML configuration
 * file is given here (generate the Javadoc to see the correct formatting). This
 * configuration provides an appender that rolls each day and creates log files
 * no larger than 10MB. The number of backup files is checked every 30 seconds,
 * whereupon if the number of backup files exceeds 100 the extra backups will be
 * deleted. The appender will make best efforts to compress backup files using
 * the GZ algorithm.
 * <p>
 * &lt;appender name=&quot;LOG-DAILYROLL&quot;
 * class=&quot;org.apache.log4j.appender.TimeAndSizeRollingAppender&quot;&gt;<br/>
 * &nbsp;&nbsp;&lt;param name=&quot;File&quot;
 * value=&quot;/logs/app.log&quot;/&gt;<br/>
 * &nbsp;&nbsp;&lt;param name=&quot;Threshold&quot; value=&quot;DEBUG&quot;/&gt;
 * <br/>
 * &nbsp;&nbsp;&lt;param name=&quot;DatePattern&quot;
 * value=&quot;.yyyy-MM-dd&quot;/&gt;<br/>
 * &nbsp;&nbsp;&lt;param name=&quot;MaxFileSize&quot;
 * value=&quot;10MB&quot;/&gt;<br/>
 * &nbsp;&nbsp;&lt;param name=&quot;MaxRollFileCount&quot;
 * value=&quot;100&quot;/&gt;<br/>
 * &nbsp;&nbsp;&lt;param name=&quot;ScavengeInterval&quot;
 * value=&quot;30000&quot;/&gt;<br/>
 * &nbsp;&nbsp;&lt;param name=&quot;BufferedIO&quot;
 * value=&quot;false&quot;/&gt;<br/>
 * &nbsp;&nbsp;&lt;param name=&quot;CompressionAlgorithm&quot;
 * value=&quot;GZ&quot;/&gt;<br/>
 * &nbsp;&nbsp;&lt;layout class=&quot;org.apache.log4j.PatternLayout&quot;&gt;<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;param name=&quot;ConversionPattern&quot;
 * value=&quot;%-5p %-23d{ISO8601} [%t] %x: %c{1} - %m%n&quot;/&gt;<br/>
 * &nbsp;&nbsp;&lt;/layout&gt;<br/>
 * &lt;/appender&gt;
 * <h3>Sample 2</h3>
 * <p>
 * This next configuration provides an appender that rolls each day, creates log
 * files no larger than 100MB, and limits the number of backup files to no more
 * than 10. The number of backup files is checked every 30 seconds, whereupon if
 * the number of backup files exceeds 10 the extra backups will be deleted. The
 * appender will make best efforts to compress backup files using the ZIP
 * algorithm, but it will only compress backup files after more than 5 rolls
 * have taken place during the lifetime of the application instance. Finally,
 * this configuration causes the appender to honour time boundaries by rolling
 * logs pro-actively at the end of each day, rather that reactively in response
 * to a logging event. After file roll is complete, the new log file will have
 * the message &quot;First line of each file following a roll&quot; printed on
 * the first line.
 * <p>
 * &lt;appender name=&quot;LOG-DAILYROLL&quot;
 * class=&quot;org.apache.log4j.appender.TimeAndSizeRollingAppender&quot;&gt;<br/>
 * &nbsp;&nbsp;&lt;param name=&quot;File&quot;
 * value=&quot;/logs/app.log&quot;/&gt;<br/>
 * &nbsp;&nbsp;&lt;param name=&quot;Threshold&quot; value=&quot;DEBUG&quot;/&gt;
 * <br/>
 * &nbsp;&nbsp;&lt;param name=&quot;DatePattern&quot;
 * value=&quot;.yyyy-MM-dd&quot;/&gt;<br/>
 * &nbsp;&nbsp;&lt;param name=&quot;MaxFileSize&quot;
 * value=&quot;100MB&quot;/&gt;<br/>
 * &nbsp;&nbsp;&lt;param name=&quot;DateRollEnforced&quot;
 * value=&quot;true&quot;/&gt;<br/>
 * &nbsp;&nbsp;&lt;param name=&quot;FileRollEventMessage&quot; value=&quot;First
 * line of each file following a roll&quot;/&gt;<br/>
 * &nbsp;&nbsp;&lt;param name=&quot;BufferedIO&quot;
 * value=&quot;false&quot;/&gt;<br/>
 * &nbsp;&nbsp;&lt;param name=&quot;CompressionAlgorithm&quot;
 * value=&quot;ZIP&quot;/&gt;<br/>
 * &nbsp;&nbsp;&lt;param name=&quot;CompressionMinQueueSize&quot;
 * value=&quot;5&quot;/&gt;<br/>
 * &nbsp;&nbsp;&lt;layout class=&quot;org.apache.log4j.PatternLayout&quot;&gt;<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;param name=&quot;ConversionPattern&quot;
 * value=&quot;%-5p %-23d{ISO8601} [%t] %x: %c{1} - %m%n&quot;/&gt;<br/>
 * &nbsp;&nbsp;&lt;/layout&gt;<br/>
 * &lt;/appender&gt;
 * <h3>Sample 3</h3>
 * <p>
 * A final example of the simplest configuration provides an appender that rolls
 * each day and creates log files no larger than 10MB. The number of backup
 * files is checked every 30 seconds, whereupon if the number of backup files
 * exceeds 10 the extra backups will be deleted. Backup files are not
 * compressed, log files are rolled reactively, and no roll event messages are
 * written at the top of each new log file.
 * <p>
 * &lt;appender name=&quot;LOG-DAILYROLL&quot;
 * class=&quot;org.apache.log4j.appender.TimeAndSizeRollingAppender&quot;&gt;<br/>
 * &nbsp;&nbsp;&lt;param name=&quot;File&quot;
 * value=&quot;/logs/app.log&quot;/&gt;<br/>
 * &nbsp;&nbsp;&lt;param name=&quot;Threshold&quot; value=&quot;DEBUG&quot;/&gt;
 * <br/>
 * &nbsp;&nbsp;&lt;param name=&quot;BufferedIO&quot;
 * value=&quot;false&quot;/&gt;<br/>
 * &nbsp;&nbsp;&lt;layout class=&quot;org.apache.log4j.PatternLayout&quot;&gt;<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;param name=&quot;ConversionPattern&quot;
 * value=&quot;%-5p %-23d{ISO8601} [%t] %x: %c{1} - %m%n&quot;/&gt;<br/>
 * &nbsp;&nbsp;&lt;/layout&gt;<br/>
 * &lt;/appender&gt;
 *
 * @author <a href="mailto:simon_park_mail AT yahoo DOT co DOT uk">Simon
 *         Park</a>
 * @version 2.7
 * @see org.apache.log4j.DailyRollingFileAppender
 */
public class FoundationFileRollingAppender extends FileAppender {

	/**
	 * Properties bean to facilitate property sharing between objects.
	 */
	private final AppenderRollingProperties properties = new AppenderRollingProperties();

	private FileRollable fileRollable = null;

	/**
	 * This is for saving the original layout so we can write application state
	 * always by the original layout. Original layout may be changed during log
	 * writing (e.g marker logging) and when a roll occurs, we need the ability
	 * to use the original layout for application state.
	 */
	// private Layout originalLayout;

	private LogFileScavenger logFileScavenger = null;

	private LogFileCompressor logFileCompressor = null;

	private TimeBasedRollEnforcer logRollEnforcer = null;

	private FileRollEventListener guestFileRollEventListener = null;

	private boolean addApplicationState = true;

	public boolean isAddApplicationState() {
		return addApplicationState;
	}

	public void setAddApplicationState(boolean addApplicationState) {
		this.addApplicationState = addApplicationState;
	}

	public FoundationFileRollingAppender() {
		super();
	}

	/**
	 * @param layout
	 * @param filename
	 * @throws java.io.IOException
	 */
	public FoundationFileRollingAppender(final Layout layout, final String filename) throws IOException {
		this();
		this.initSuper(layout, filename, super.getAppend(), super.getBufferedIO(), super.getBufferSize());
	}

	/**
	 * @param layout
	 * @param filename
	 * @param append
	 * @throws java.io.IOException
	 */
	public FoundationFileRollingAppender(final Layout layout, final String filename, final boolean append) throws IOException {
		this();
		this.initSuper(layout, filename, append, super.getBufferedIO(), super.getBufferSize());
	}

	/**
	 * @param layout
	 * @param filename
	 * @param append
	 * @param bufferedIO
	 * @param bufferSize
	 * @throws java.io.IOException
	 */
	public FoundationFileRollingAppender(final Layout layout, final String filename, final boolean append, final boolean bufferedIO, final int bufferSize) throws IOException {
		this();
		this.initSuper(layout, filename, append, bufferedIO, bufferSize);
	}

	private void initSuper(final Layout layout, final String filename, final boolean append, final boolean bufferedIO, final int bufferSize) throws IOException {
		super.setLayout(layout);
		super.setFile(filename);
		super.setAppend(append);
		super.setBufferedIO(bufferedIO);
		super.setBufferSize(bufferSize);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.log4j.FileAppender#activateOptions()
	 */
	public synchronized final void activateOptions() {
		this.deactivateOptions();
		super.activateOptions();
		if (getFilterEmptyMessages()) {
			addFilter(new EmptyMessageFilter());
		}

		// rollables
		this.setFileRollable(new CompositeRoller(this, this.getProperties()));
		// scavenger
		LogFileScavenger fileScavenger = this.getLogFileScavenger();
		if (fileScavenger == null) {
			fileScavenger = this.initLogFileScavenger(new DefaultLogFileScavenger());
		}
		fileScavenger.begin();
		// compressor
		final LogFileCompressor logFileCompressor = new LogFileCompressor(this, this.getProperties());
		this.setLogFileCompressor(logFileCompressor);
		this.getFileRollable().addFileRollEventListener(logFileCompressor);
		logFileCompressor.begin();
		// guest listener
		this.registerGuestFileRollEventListener();
		// roll enforcer
		final TimeBasedRollEnforcer logRollEnforcer = new TimeBasedRollEnforcer(this, this.getProperties());
		this.setLogRollEnforcer(logRollEnforcer);
		logRollEnforcer.begin();
		// roll on start-up
		if (this.getProperties().shouldRollOnActivation()) {
			synchronized (this) {
				this.rollFile(new StartupFileRollEvent());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.log4j.WriterAppender#close()
	 */
	public synchronized final void close() {
		this.deactivateOptions();
		super.close();
	}

	public String getCompressionAlgorithm() {
		return this.getProperties().getCompressionAlgorithm();
	}

	public boolean getCompressionUseBlockingQueue() {
		return this.getProperties().isCompressionBlocking();
	}

	public long getCompressionMaxBlockingInterval() {
		return this.getProperties().getCompressionMaxWait();
	}

	public int getCompressionMinQueueSize() {
		return this.getProperties().getCompressionMinQueueSize();
	}

	public int getCompressionLevel() {
		return this.getProperties().getCompressionLevel();
	}

	public String getDatePattern() {
		return this.getProperties().getDatePattern();
	}

	public boolean getFilterEmptyMessages() {
		return this.getProperties().getFilterEmptyMessages();
	}

	public void setFilterEmptyMessages(final boolean filterEmptyMessages) {
		this.getProperties().setFilterEmptyMessages(filterEmptyMessages);
	}

	public String getDatePatternLocale() {
		return this.getProperties().getDatePatternLocale().toString();
	}

	public boolean getDateRollEnforced() {
		return this.getProperties().isDateRollEnforced();
	}

	public String getFileRollEventListener() {
		return (this.getGuestFileRollEventListener() != null) ? this.getGuestFileRollEventListener().getClass().getName() : String.valueOf(this.getGuestFileRollEventListener());
	}

	public String getMaxFileSize() {
		return Long.toString(this.getProperties().getMaxFileSize());
	}

	public String getMinFreeDiskSpace() {
		return Long.toString(this.getProperties().getMinFreeDiscSpace());
	}

	public int getMaxRollFileCount() {
		return this.getProperties().getMaxRollFileCount();
	}

	public int getMaxFileAge() {
		return this.getProperties().getMaxFileAge();
	}

	public boolean getRollOnStartup() {
		return this.getProperties().shouldRollOnStartup();
	}

	public long getScavengeInterval() {
		return this.getProperties().getScavengeInterval();
	}

	/**
	 * @param compressionAlgorithm
	 *            &quot;ZIP&quot; or &quot;GZ&quot;
	 */
	public void setCompressionAlgorithm(String compressionAlgorithm) {
		if (compressionAlgorithm == null) {
			LogLog.warn("Null name supplied for compression algorithm [" + this.getName() + "], defaulting to '" + this.getProperties().getCompressionAlgorithm() + '\'');
			return;
		}
		compressionAlgorithm = compressionAlgorithm.trim();
		if ("".equals(compressionAlgorithm)) {
			LogLog.warn("Empty name supplied for compression algorithm [" + this.getName() + "], defaulting to '" + this.getProperties().getCompressionAlgorithm() + '\'');
			return;
		}
		this.getProperties().setCompressionAlgorithm(compressionAlgorithm);
	}

	public void setCompressionUseBlockingQueue(final boolean compressionBlockingQueue) {
		this.getProperties().setCompressionBlocking(compressionBlockingQueue);
	}

	public void setCompressionMaxBlockingInterval(final long compressionInterval) {
		this.getProperties().setCompressionMaxWait(compressionInterval);
	}

	/**
	 * @param compressionLevel
	 *            {@link java.util.zip.Deflater#DEFAULT_COMPRESSION},
	 *            {@link java.util.zip.Deflater#NO_COMPRESSION}, or in the range
	 *            {@link java.util.zip.Deflater#BEST_SPEED} to
	 *            {@link java.util.zip.Deflater#BEST_COMPRESSION}.
	 * @see java.util.zip.Deflater
	 */
	public void setCompressionLevel(final int compressionLevel) {
		this.getProperties().setCompressionLevel(compressionLevel);
	}

	/**
	 * The minimum number of backup files that must be in the queue awaiting
	 * compression before any compression will take place.
	 *
	 * @param compressionMinQueueSize
	 *            &gt;= 0.
	 */
	public void setCompressionMinQueueSize(final int compressionMinQueueSize) {
		this.getProperties().setCompressionMinQueueSize(compressionMinQueueSize);
	}

	/**
	 * @param datePattern
	 *            in compliance with <em>localized</em> patterns similar to
	 *            those specified by {@link java.text.SimpleDateFormat}. Note that the
	 *            pattern characters in the main Javadoc of
	 *            {@link java.text.SimpleDateFormat} are defaults for
	 *            {@link java.util.Locale#ENGLISH}, if I understand correctly.
	 * @see java.text.SimpleDateFormat
	 */
	public void setDatePattern(String datePattern) {
		if (datePattern == null) {
			LogLog.warn("Null date pattern supplied for appender [" + this.getName() + "], defaulting to " + this.getProperties().getDatePattern());
			return;
		}
		datePattern = datePattern.trim();
		if ("".equals(datePattern)) {
			LogLog.warn("Empty date pattern supplied for appender [" + this.getName() + "], defaulting to " + this.getProperties().getDatePattern());
			return;
		}
		this.getProperties().setDatePattern(datePattern);
	}

	/**
	 * Sets the {@link java.util.Locale} to be used when processing date patterns.
	 * Variants are not supported; only language and (optionally) country may be
	 * used, e.g.&nbsp;&quot;en&quot;, &nbsp;&quot;en_GB&quot; or
	 * &quot;fr_CA&quot; are all valid. If no locale is supplied,
	 * {@link java.util.Locale#ENGLISH} will be used.
	 *
	 * @param datePatternLocale
	 * @see java.util.Locale
	 * @see #setDatePattern(String)
	 */
	public void setDatePatternLocale(String datePatternLocale) {
		if (datePatternLocale == null) {
			LogLog.warn("Null date pattern locale supplied for appender [" + this.getName() + "], defaulting to " + this.getProperties().getDatePatternLocale());
			return;
		}
		datePatternLocale = datePatternLocale.trim();
		if ("".equals(datePatternLocale)) {
			LogLog.warn("Empty date pattern locale supplied for appender [" + this.getName() + "], defaulting to " + this.getProperties().getDatePatternLocale());
			return;
		}
		final String[] parts = datePatternLocale.split("_");
		switch (parts.length) {
		case 1:
			this.getProperties().setDatePatternLocale(new Locale(parts[0]));
			break;
		case 2:
			this.getProperties().setDatePatternLocale(new Locale(parts[0], parts[1]));
			break;
		default:
			LogLog.warn("Unable to parse date pattern locale supplied for appender [" + this.getName() + "], defaulting to " + this.getProperties().getDatePatternLocale());
		}
	}

	/**
	 * @param dateRollEnforced
	 *            When <tt>true</tt> file rolls will occur pro-actively when the
	 *            time boundary is reached, rather than reactively in response
	 *            to a logging event.
	 */
	public void setDateRollEnforced(final boolean dateRollEnforced) {
		this.getProperties().setDateRollEnforced(dateRollEnforced);
	}

	/**
	 * @param className
	 *            The name of the class that implements the
	 *            {@link FileRollEventListener} interface; implementors must be
	 *            declared public and have a default constructor.
	 */
	public void setFileRollEventListener(String className) {
		if (className != null) {
			className = className.trim();
			final FileRollEventListener fileRollEventListener = (FileRollEventListener) OptionConverter.instantiateByClassName(className, FileRollEventListener.class, null);
			if (fileRollEventListener != null) {
				this.initGuestFileRollEventListener(fileRollEventListener);
			}
		}
	}

	public void setLogFileScavenger(String className) {
		if (className != null) {
			className = className.trim();
			final LogFileScavenger logFileScavenger = (LogFileScavenger) OptionConverter.instantiateByClassName(className, LogFileScavenger.class, null);
			if (logFileScavenger != null) {
				this.initLogFileScavenger(logFileScavenger);
			}
		}
	}

	/**
	 * @param message
	 *            The message to be appended at the top of each new file created
	 *            following a file roll. If the message is empty, a default
	 *            message will be appended instead.
	 */
	public void setFileRollEventMessage(final String message) {
		if (message != null) {
			if (!"".equals(message.trim())) {
				this.initGuestFileRollEventListener(new FileRollEventListener() {
					public final void onFileRoll(final FileRollEvent fileRollEvent) {
						fileRollEvent.dispatchToAppender(message);
					}
				});
			} else {
				this.initGuestFileRollEventListener(new FileRollEventListener() {
					public final void onFileRoll(final FileRollEvent fileRollEvent) {
						fileRollEvent.dispatchToAppender();
					}
				});
			}
		}
	}

	public void setMaxFileSize(String value) {
		if (value == null) {
			LogLog.warn("Null max file size supplied for appender [" + this.getName() + "], defaulting to " + this.getProperties().getMaxFileSize());
			return;
		}
		value = value.trim();
		if ("".equals(value)) {
			LogLog.warn("Empty max file size supplied for appender [" + this.getName() + "], defaulting to " + this.getProperties().getMaxFileSize());
			return;
		}
		final long defaultMaxFileSize = this.getProperties().getMaxFileSize();
		final long maxFileSize = OptionConverter.toFileSize(value, defaultMaxFileSize);
		this.getProperties().setMaxFileSize(maxFileSize);
	}

	/**
	 * <b>Warning</b> Use of this property requires Java 6.
	 * 
	 * @param value
	 */
	public void setMinFreeDiskSpace(String value) {
		if (value == null) {
			LogLog.warn("Null min free disk space supplied for appender [" + this.getName() + "], defaulting to " + this.getProperties().getMinFreeDiscSpace());
			return;
		}
		value = value.trim();
		if ("".equals(value)) {
			LogLog.warn("Empty min free disk space supplied for appender [" + this.getName() + "], defaulting to " + this.getProperties().getMinFreeDiscSpace());
			return;
		}
		final long defaultMinFreeDiskSpace = this.getProperties().getMinFreeDiscSpace();
		final long minFreeDiskSpace = OptionConverter.toFileSize(value, defaultMinFreeDiskSpace);
		this.getProperties().setMinFreeDiscSpace(minFreeDiskSpace);
	}

	public void setMaxRollFileCount(final int maxRollFileCount) {
		this.getProperties().setMaxRollFileCount(maxRollFileCount);
	}

	public void setMaxFileAge(final int maxFileAge) {
		this.getProperties().setMaxFileAge(maxFileAge);
	}

	/**
	 * @param rollOnStartup
	 *            <tt>true</tt> if the appender should roll, and create a new
	 *            log file, immediately upon receiving the first logging event
	 *            after activation.
	 */
	public void setRollOnStartup(final boolean rollOnStartup) {
		this.getProperties().setRollOnStartup(rollOnStartup);
	}

	public void setScavengeInterval(final long intervalMillis) {
		this.getProperties().setScavengeInterval(intervalMillis);
	}

	public void setMinFreeDiskSpace(final long minFreeDiskSpace) {
		this.getProperties().setMinFreeDiscSpace(minFreeDiskSpace);
	}

	// for internal and test use only
	final FileRollEventListener getGuestFileRollEventListener() {
		return this.guestFileRollEventListener;
	}

	final File getIoFile() {
		final String fileName = super.getFile();
		if (fileName == null) {
			super.getErrorHandler().error("Filename has not been set", new IllegalStateException(), ErrorCode.FILE_OPEN_FAILURE);
			return null;
		}
		return new File(fileName);
	}

	/**
	 * Opens the log file and prepares this appender to write to it.
	 */
	final void openFile() {
		/*
		 * FileAppender::activateOptions() calls setFile(:String, :boolean,
		 * :boolean, :int) with all the options and error handling we're
		 * interested in. NB - this is a deliberate super call since we don't
		 * want to stop this appender's scavenger, etc.
		 */
		super.activateOptions();
	}

	/**
	 * Makes file closing behaviour visible to classes in this package.
	 * 
	 * @see org.apache.log4j.FileAppender#closeFile()
	 */
	protected final void closeFile() {
		/*
		 * The Log4J 1.2.15 WriterAppender doesn't write footers on file roll,
		 * so do it here.
		 */
		super.writeFooter();
		/*
		 * closeWriter() duplicates closeFile(), so we can take our pick
		 */
		super.closeWriter();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.log4j.FileAppender#setQWForFiles(java.io.Writer)
	 */
	protected final void setQWForFiles(final Writer writer) {
		final SynchronizedCountingQuietWriter countingQuietWriter = new SynchronizedCountingQuietWriter(writer, super.getErrorHandler());
		this.getProperties().setCountingQuietWriter(countingQuietWriter);
		super.qw = countingQuietWriter;
	}

	/**
	 * Responsible for executing file rolls as and when required, in addition to
	 * delegating to the super class to perform the actual append operation.
	 * Synchronized for safety during enforced file roll.
	 * 
	 * @see org.apache.log4j.WriterAppender#subAppend(org.apache.log4j.spi.LoggingEvent)
	 */
	@Override
	protected final void subAppend(final LoggingEvent event) {
		if (event instanceof ScheduledFileRollEvent) {
			// the scheduled append() call has been made by a different thread
			synchronized (this) {
				if (this.closed) {
					// just consume the event
					return;
				}
				this.rollFile(event);
			}
		} else if (event instanceof FileRollEvent) {
			// definitely want to avoid rolling here whilst a file roll event is still being handled
			super.subAppend(event);
		} else {

			if(event instanceof FoundationLof4jLoggingEvent){
				FoundationLof4jLoggingEvent foundationLof4jLoggingEvent = (FoundationLof4jLoggingEvent)event;
				foundationLof4jLoggingEvent.setAppenderName(this.getName());
			}
			
			this.rollFile(event);
			super.subAppend(event);
		}
	}

	private synchronized void deactivateOptions() {
		// scavenger
		final LogFileScavenger logFileScavenger = this.getLogFileScavenger();
		if (logFileScavenger != null) {
			logFileScavenger.end();
		}
		// roll enforcer
		final TimeBasedRollEnforcer logRollEnforcer = this.getLogRollEnforcer();
		if (logRollEnforcer != null) {
			logRollEnforcer.end();
		}
		// compressor
		final LogFileCompressor logFileCompressor = this.getLogFileCompressor();
		if (logFileCompressor != null) {
			logFileCompressor.end();
		}
		final FileRollable rollable = this.getFileRollable();
		if (rollable != null) {
			rollable.removeFileRollEventListener(logFileCompressor);
		}
		// guest listeners
		this.deregisterGuestFileRollEventListener();
	}

	private void deregisterGuestFileRollEventListener() {
		if (this.getGuestFileRollEventListener() != null) {
			// there may be only one guest listener, so remove any
			// previously-registered listeners
			final FileRollable fileRollable = this.getFileRollable();
			if (fileRollable != null) {
				fileRollable.removeFileRollEventListener(this.getGuestFileRollEventListener());
			}
		}
	}

	private void registerGuestFileRollEventListener() {
		if (this.getGuestFileRollEventListener() != null) {
			final FileRollable fileRollable = this.getFileRollable();
			if (fileRollable != null) {
				fileRollable.addFileRollEventListener(this.getGuestFileRollEventListener());
			}
		}
	}

	private void rollFile(final LoggingEvent event) {
		this.getFileRollable().roll(event);
	}

	private AppenderRollingProperties getProperties() {
		return this.properties;
	}

	private FileRollable getFileRollable() {
		return this.fileRollable;
	}

	private void setFileRollable(final FileRollable fileRollable) {
		this.fileRollable = fileRollable;
	}

	/**
	 * Default visibility for test purposes only.
	 * 
	 * @return The scavenger.
	 */
	final LogFileScavenger getLogFileScavenger() {
		return this.logFileScavenger;
	}

	private void initGuestFileRollEventListener(final FileRollEventListener guestListener) {
		this.deregisterGuestFileRollEventListener();
		this.guestFileRollEventListener = guestListener;
	}

	private LogFileScavenger initLogFileScavenger(final LogFileScavenger logFileScavenger) {
		if (this.logFileScavenger != null) {
			this.logFileScavenger.end();
		}
		this.logFileScavenger = logFileScavenger;
		this.logFileScavenger.init(this, this.getProperties());
		return this.logFileScavenger;
	}

	/**
	 * Default visibility for test purposes only.
	 * 
	 * @return The compressor.
	 */
	final LogFileCompressor getLogFileCompressor() {
		return this.logFileCompressor;
	}

	private void setLogFileCompressor(final LogFileCompressor logFileCompressor) {
		if (this.logFileCompressor != null) {
			this.logFileCompressor.end();
		}
		this.logFileCompressor = logFileCompressor;
	}

	private TimeBasedRollEnforcer getLogRollEnforcer() {
		return logRollEnforcer;
	}

	private void setLogRollEnforcer(final TimeBasedRollEnforcer logRollEnforcer) {
		if (this.logRollEnforcer != null) {
			this.logRollEnforcer.end();
		}
		this.logRollEnforcer = logRollEnforcer;
	}

}
