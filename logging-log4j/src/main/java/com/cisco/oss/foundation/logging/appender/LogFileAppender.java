package com.cisco.oss.foundation.logging.appender;


import com.cisco.oss.foundation.logging.FoundationLoggingPatternLayout;

public class LogFileAppender extends FoundationFileRollingAppender {
		public LogFileAppender(){
			super();
			super.setLayout(new FoundationLoggingPatternLayout());
		}
		
	  @Override
	  public  boolean requiresLayout() {
	    return false;
	  }
}
