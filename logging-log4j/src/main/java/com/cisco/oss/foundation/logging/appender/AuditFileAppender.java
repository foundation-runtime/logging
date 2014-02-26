package com.cisco.oss.foundation.logging.appender;


import com.cisco.oss.foundation.logging.FondationLoggerConstants;
import com.cisco.oss.foundation.logging.FoundationLoggingPatternLayout;

public class AuditFileAppender extends FoundationFileRollingAppender {
	public AuditFileAppender(){
		super();
		super.setLayout(new FoundationLoggingPatternLayout(FondationLoggerConstants.DEFAULT_AUDIT_PATTERN.toString()));
	}
  @Override
  public  boolean requiresLayout() {
    return false;
  }
}
