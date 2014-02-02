package com.cisco.vss.foundation.logging.appender;


import com.cisco.vss.foundation.logging.CABLoggerConstants;
import com.cisco.vss.foundation.logging.CABLoggingPatternLayout;

public class AuditFileAppender extends CABFileRollingAppender {
	public AuditFileAppender(){
		super();
		super.setLayout(new CABLoggingPatternLayout(CABLoggerConstants.DEFAULT_AUDIT_PATTERN.toString()));
	}
  @Override
  public  boolean requiresLayout() {
    return false;
  }
}
