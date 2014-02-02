package com.cisco.vss.foundation.logging.appender;


import com.cisco.vss.foundation.logging.CABLoggingPatternLayout;

public class LogFileAppender extends CABFileRollingAppender {
		public LogFileAppender(){
			super();
			super.setLayout(new CABLoggingPatternLayout());
		}
		
	  @Override
	  public  boolean requiresLayout() {
	    return false;
	  }
}
