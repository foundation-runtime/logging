package com.cisco.vss.foundation.logging.appender;


import com.cisco.vss.foundation.logging.FondationLoggerConstants;
import com.cisco.vss.foundation.logging.FoundationLoggingPatternLayout;

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
