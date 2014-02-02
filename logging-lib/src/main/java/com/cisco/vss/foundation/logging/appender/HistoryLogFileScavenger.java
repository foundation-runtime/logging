package com.cisco.vss.foundation.logging.appender;

import org.apache.log4j.helpers.FileHelper;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A scavenger that keep file from a given amount of last days. 
 * @author ykasten
 *
 */
public class HistoryLogFileScavenger implements LogFileScavenger {
	/**
	 * Default pattern for log files date. 
	 */
	private static final String APPENER_DATE_DEFAULT_PATTERN = ".*([0-9]{4})-([0-9]{2})-([0-9]{2}).*";
	
	private static final long MILISECONS_IN_DAY = 24*60*60*1000;
	
	/**
	 * Waiting a random time that is maximum SECONDS_RANGE_WAIT seconds, before scavenging, so not all components will delete together the files. 
	 */
	protected static final double SECONDS_RANGE_WAIT = 30;
	private Timer timer; 
	private CABFileRollingAppender appender = null;
	private File file = null;
	private AppenderRollingProperties properties = null;
	
	/**
	 * Constructor for HistoryLogFileScavenger
	 */
	public HistoryLogFileScavenger(){
		super();

	}

	@Override
	public void scavenge() {
		final FileHelper fileHelper = FileHelper.getInstance();
		final LogFileList logFileList = this.logFileList();
		final int fileListSize = logFileList.size();
		GregorianCalendar currentDate = new GregorianCalendar();
		GregorianCalendar lastRelevantDate=getLastReleventDate(currentDate);
		for(int i=0;i<fileListSize;i++){
			File currentLogFile=(File)logFileList.get(i);
			if(!relevant(currentLogFile,lastRelevantDate)){
				fileHelper.deleteExisting(currentLogFile);
			}
		}
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

	final AppenderRollingProperties getProperties() {
		return this.properties;
	}

	/**
	 * Get a log file and last relevant date, and check if the log file is relevant
	 * @param currentLogFile The log file
	 * @param lastRelevantDate The last date which files should be keeping since
	 * @return false if the file should be deleted, true if it does not. 
	 */
	private boolean relevant(File currentLogFile, GregorianCalendar lastRelevantDate) {
		String fileName=currentLogFile.getName();
		Pattern p = Pattern.compile(APPENER_DATE_DEFAULT_PATTERN);
		Matcher m = p.matcher(fileName);
		if(m.find()){
			int year=Integer.parseInt(m.group(1));
			int month=Integer.parseInt(m.group(2));
			int dayOfMonth=Integer.parseInt(m.group(3));
			GregorianCalendar fileDate=new GregorianCalendar(year, month, dayOfMonth);
			fileDate.add(Calendar.MONTH,-1); //Because of Calendar save the month such that January is 0
			return fileDate.compareTo(lastRelevantDate)>0;
		}
		else{
			return false;
		}
	}

	/**
	 * Get the last date to keep logs from, by a given current date.
	 * @param currentDate the date of today
	 * @return the last date to keep log files from.
	 */
	private GregorianCalendar getLastReleventDate(GregorianCalendar currentDate) {
		int age=this.getProperties().getMaxFileAge();
		GregorianCalendar result=new GregorianCalendar(currentDate.get(Calendar.YEAR),currentDate.get(Calendar.MONTH),currentDate.get(Calendar.DAY_OF_MONTH));
		result.add(Calendar.DAY_OF_MONTH, -age);
		return result;
	}


	@Override
	public void begin() {
		
		this.file = this.getAppender().getIoFile();
		if (this.file == null) {
			this.getAppender().getErrorHandler()
			.error("Scavenger not started: missing log file name");
			return;
		}
		//Set tomorrow 00:00:30 to be the next scavenging time, then scavenge will be done every 24 hours.
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.add(Calendar.DAY_OF_MONTH, +1);
		currentDate.set(GregorianCalendar.HOUR_OF_DAY,0);
		currentDate.set(GregorianCalendar.MINUTE,0);
		currentDate.set(GregorianCalendar.SECOND,30);
		
		timer=new Timer("HistoryScavenger", true);
		scavenge();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				Random ran=new Random();
				int randomWaiting=(int) ((ran.nextDouble())*SECONDS_RANGE_WAIT);
				try {
					//Wait random time between 0 to 30 seconds;
					Thread.sleep(randomWaiting*1000);
				} catch (InterruptedException e) {
					
				}
				scavenge();

			}
		}, currentDate.getTime(), MILISECONS_IN_DAY);
	

	}

	@Override
	public void end() {
		if(timer!=null){
			timer.cancel();		
		}

	}

	@Override
	public final void init(final CABFileRollingAppender appender,
			final AppenderRollingProperties properties) {
		this.appender = appender;
		this.properties = properties;
	
	}

	final CABFileRollingAppender getAppender() {
		return this.appender;
	}

}
