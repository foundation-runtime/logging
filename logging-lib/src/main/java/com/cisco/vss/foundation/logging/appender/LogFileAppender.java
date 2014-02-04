package com.cisco.vss.foundation.logging.appender;


import com.cisco.vss.foundation.logging.FoundationLoggingPatternLayout;

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
