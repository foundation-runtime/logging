/**
 * 
 */
package com.cisco.vss.foundation.logging;

import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.pattern.FormattingInfo;
import org.apache.log4j.pattern.LoggingEventPatternConverter;
import org.apache.log4j.pattern.PatternParser;
import org.apache.log4j.spi.LoggingEvent;

import java.util.*;


/**
 * This extension was made due to the fact the BridgePatternConverter cannot be
 * extended. CAB Logging extension made to add new converter in the pattern rule
 * map. Most of the following code is Log4j code.
 * 
 * @author Yair Ogen
 * 
 */
public class CABLoggingPatternConverter extends org.apache.log4j.helpers.PatternConverter {
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

	public CABLoggingPatternConverter(final String pattern) {
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
		newPatternLayoutRules.put("throwable", CABLoggingThrowableInformationPatternConverter.class);
		newPatternLayoutRules.put("pm", CABLoggingMessagePatternConverter.class);
		newPatternLayoutRules.put("sl", CABLoggingStucturedMessageConverter.class);
		newPatternLayoutRules.put("prettymessage", CABLoggingMessagePatternConverter.class);
		newPatternLayoutRules.put("errorcode", CABLoggingErrorCodePatternConverter.class);
		newPatternLayoutRules.put("audit", CABLoggingAuditPatternConverter.class);
		newPatternLayoutRules.put("u", CABLoggingUserFieldPatternConverter.class);
		newPatternLayoutRules.put("host", CABLoggingHostPatternConverter.class);
		newPatternLayoutRules.put("compInstallPath", CABLoggingCompInstPathPatternConverter.class);
		newPatternLayoutRules.put("compVersion", CABLoggingCompVersionPatternConverter.class);
		newPatternLayoutRules.put("compName", CABLoggingCompNamePatternConverter.class);
		newPatternLayoutRules.put("compInstanceName", CABLoggingCompInstNamePatternConverter.class);

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
