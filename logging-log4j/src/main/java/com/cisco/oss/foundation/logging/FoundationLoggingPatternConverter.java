/**
 * 
 */
package com.cisco.oss.foundation.logging;

import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.pattern.FormattingInfo;
import org.apache.log4j.pattern.LoggingEventPatternConverter;
import org.apache.log4j.pattern.PatternParser;
import org.apache.log4j.spi.LoggingEvent;

import java.util.*;


/**
 * This extension was made due to the fact the BridgePatternConverter cannot be
 * extended. Foundation Logging extension made to add new converter in the pattern rule
 * map. Most of the following code is Log4j code.
 * 
 * @author Yair Ogen
 * 
 */
public class FoundationLoggingPatternConverter extends org.apache.log4j.helpers.PatternConverter {
	/**
	 * Pattern converters.
	 */
	private final LoggingEventPatternConverter[] patternConverters;

	/**
	 * Field widths and alignment corresponding to pattern converters.
	 */
	private final FormattingInfo[] patternFields;

	/**
	 * Does pattern process exceptions.
	 */
	private boolean handlesExceptions;// NOPMD

	/**
	 * Create a new instance.
	 * 
	 * @param pattern
	 *            pattern, may not be null.
	 */

	public FoundationLoggingPatternConverter(final String pattern) {
		super();
		next = null;
		handlesExceptions = false;

		final List<PatternConverter> converters = new ArrayList<PatternConverter>();
		final List<FormattingInfo> fields = new ArrayList<FormattingInfo>();
		@SuppressWarnings("unchecked")
		final Map converterRegistry = null;

		@SuppressWarnings("unchecked")
		final Map<String, Class<? extends org.apache.log4j.pattern.PatternConverter>> patternLayoutRules = PatternParser.getPatternLayoutRules();
		final Map<String, Class<? extends org.apache.log4j.pattern.PatternConverter>> newPatternLayoutRules = new HashMap<String, Class<? extends org.apache.log4j.pattern.PatternConverter>>();
		newPatternLayoutRules.putAll(patternLayoutRules);
		newPatternLayoutRules.put("throwable", FoundationLoggingThrowableInformationPatternConverter.class);
		newPatternLayoutRules.put("pm", FoundationLoggingMessagePatternConverter.class);
		newPatternLayoutRules.put("sl", FoundationLoggingStructuredMessageConverter.class);
		newPatternLayoutRules.put("prettymessage", FoundationLoggingMessagePatternConverter.class);
		newPatternLayoutRules.put("errorcode", FoundationLoggingErrorCodePatternConverter.class);
		newPatternLayoutRules.put("audit", FoundationLoggingAuditPatternConverter.class);
		newPatternLayoutRules.put("u", FoundationLoggingUserFieldPatternConverter.class);
		newPatternLayoutRules.put("host", FoundationLoggingHostPatternConverter.class);
		newPatternLayoutRules.put("compInstallPath", FoundationLoggingCompInstPathPatternConverter.class);
		newPatternLayoutRules.put("compVersion", FoundationLoggingCompVersionPatternConverter.class);
		newPatternLayoutRules.put("compName", FoundationLoggingCompNamePatternConverter.class);
		newPatternLayoutRules.put("compInstanceName", FoundationLoggingCompInstNamePatternConverter.class);

		PatternParser.parse(pattern, converters, fields, converterRegistry, newPatternLayoutRules);

		patternConverters = new LoggingEventPatternConverter[converters.size()];
		patternFields = new FormattingInfo[converters.size()];

		int index = 0;
		final Iterator<PatternConverter> converterIter = converters.iterator();
		final Iterator<FormattingInfo> fieldIter = fields.iterator();

		while (converterIter.hasNext()) {
			final Object converter = converterIter.next();

			if (converter instanceof LoggingEventPatternConverter) {
				patternConverters[index] = (LoggingEventPatternConverter) converter;
				handlesExceptions |= patternConverters[index].handlesThrowable();
			} else {
				patternConverters[index] = new org.apache.log4j.pattern.LiteralPatternConverter("");
			}

			if (fieldIter.hasNext()) {
				patternFields[index] = fieldIter.next();
			} else {
				patternFields[index] = FormattingInfo.getDefault();
			}

			index++;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String convert(final LoggingEvent event) {
		//
		// code should be unreachable.
		//
		final StringBuffer sbuf = new StringBuffer();
		format(sbuf, event);

		return sbuf.toString();
	}

	/**
	 * Format event to string buffer.
	 * 
	 * @param sbuf
	 *            string buffer to receive formatted event, may not be null.
	 * @param event
	 *            event to format, may not be null.
	 */
	@Override
	public void format(final StringBuffer sbuf, final LoggingEvent event) {
		for (int i = 0; i < patternConverters.length; i++) {
			final int startField = sbuf.length();
			patternConverters[i].format(event, sbuf);
			patternFields[i].format(startField, sbuf);
		}
	}

	/**
	 * Will return false if any of the conversion specifiers in the pattern
	 * handles {@link Exception Exceptions}.
	 * 
	 * @return true if the pattern formats any information from exceptions.
	 */
	public boolean ignoresThrowable() {
		return false;
	}
}
