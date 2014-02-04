/**
 * 
 */
package com.cisco.vss.foundation.logging;


/**
 * @author Yair Ogen
 *
 */
public class FoundationLoggingPatternParser extends org.apache.log4j.helpers.PatternParser {


	  /**
	   * Create a new instance.
	   * @param conversionPattern pattern, may not be null.
	   */
	  public FoundationLoggingPatternParser(
              final String conversionPattern) {
	    super(conversionPattern);
	  }

	  /**
	   * Create new pattern converter.
	   * @return pattern converter.
	   */
	  public org.apache.log4j.helpers.PatternConverter parse() {
	    return new FoundationLoggingPatternConverter(pattern);
	  }
	}