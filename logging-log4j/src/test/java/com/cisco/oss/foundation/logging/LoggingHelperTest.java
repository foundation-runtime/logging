/**
 * 
 */
package com.cisco.oss.foundation.logging;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.lang.reflect.Constructor;

import static org.junit.Assert.assertEquals;

/**
 * 
 * @author Yair Ogen
 */
public class LoggingHelperTest {

	private static final Logger LOGGER = Logger.getLogger(LoggingHelperTest.class);

	@Test
	public void formatterTest() {
		String formatCommunicationMessage = LoggingHelper.formatCommunicationMessage("udp", "source", "dest", "my message");
		assertEquals("protocol[udp] source[source] destination[dest] my message", formatCommunicationMessage);
		
		formatCommunicationMessage = LoggingHelper.formatCommunicationMessage("udp", "source", "dest", "my message", LoggingHelper.IN_OUT_MODE.IN);
		assertEquals("Rx: protocol[udp] source[source] destination[dest] my message", formatCommunicationMessage);
		
		formatCommunicationMessage = LoggingHelper.formatCommunicationMessage("udp", "source", "dest", "my message", LoggingHelper.IN_OUT_MODE.OUT);
		assertEquals("Tx: protocol[udp] source[source] destination[dest] my message", formatCommunicationMessage);
		
		String formatConnectionEstablishmentMessage = LoggingHelper.formatConnectionEstablishmentMessage("con name", "host", "connectionReason");
		assertEquals("[con name] remote host[host] connectionReason", formatConnectionEstablishmentMessage);
		
		String formatConnectionTerminationMessage = LoggingHelper.formatConnectionTerminationMessage("connectionName", "host", "connectionReason", "terminationReason");
		assertEquals("[connectionName] remote host[host] connectionReason - terminationReason", formatConnectionTerminationMessage);
	}

	@Test
	public void formattedTest() {
		LOGGER.debug(String.format("this is a test of %s", "test123"));
		LOGGER.debug("unformatted");
	}

	@Test
	public void isEnabledForTests() {

		LoggingHelper.trace(LOGGER, "this is my test for: %s", "trace");
//		LoggingHelper.trace(LOGGER, CategoryTerm.Application, "this is my test for: %s", "trace");

		LoggingHelper.debug(LOGGER, "this is my test for: %s", "debug");
//		LoggingHelper.debug(LOGGER, CategoryTerm.Application, "this is my test for: %s", "debug");

		LoggingHelper.info(LOGGER, "this is my test for: %s", "info");
//		LoggingHelper.info(LOGGER, CategoryTerm.Application, "this is my test for: %s", "info");

		LoggingHelper.warn(LOGGER, "this is my test for: %s", "warn");
//		LoggingHelper.warn(LOGGER, CategoryTerm.Application, "this is my test for: %s", "warn");

		LoggingHelper.error(LOGGER, "this is my test for: %s", "error");
//		LoggingHelper.error(LOGGER, CategoryTerm.Application, "this is my test for: %s", "error");

		LoggingHelper.fatal(LOGGER, "this is my test for: %s", "fatal");
//		LoggingHelper.fatal(LOGGER, CategoryTerm.Application, "this is my test for: %s", "fatal");
		
		LoggingHelper.log(LOGGER, Level.TRACE, "this is my test for: %s", "trace");
		LoggingHelper.log(LOGGER, Level.ERROR, new IllegalArgumentException(), "this is my test for: %s", "error");
		LoggingHelper.log(LOGGER, "FQCN",Level.ERROR, new IllegalArgumentException(), "this is my test for: %s", "error");
		
//		LoggingHelper.log(LOGGER, CategoryTerm.Application, Level.TRACE, "this is my test for: %s", "trace");
//		LoggingHelper.log(LOGGER, CategoryTerm.Application, Level.ERROR, new IllegalArgumentException(), "this is my test for: %s", "error");
//		LoggingHelper.log(LOGGER, CategoryTerm.Application, "FQCN",Level.ERROR, new IllegalArgumentException(), "this is my test for: %s", "error");


	}

	@Test
	public void isEnabledForTests_ErrorLevel() {
		LOGGER.setLevel(Level.ERROR);
		isEnabledForTests();
	}

	@Test
	public void mockCtor() throws Exception {
		Constructor[] constructors = LoggingHelper.class.getDeclaredConstructors();
		for (Constructor constructor : constructors) {
			constructor.setAccessible(true);
			constructor.newInstance();
		}
	}

}
