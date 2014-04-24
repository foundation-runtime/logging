package com.cisco.oss.foundation.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.junit.Before;
import org.junit.Test;

/**
 * A test for testing the sniffer loggers
 * @author yidwu
 *
 */
public class SnifferLoggerTest {
	
	private static final Logger logger =  Logger.getLogger(SnifferLoggerTest.class);
	
	List<String> names = new ArrayList<String>();
	
	@Before
	public void loadLoggerNames() throws Exception {
		InputStream settingIS = FoundationLogger.class
				.getResourceAsStream("/sniffingloggers.xml");
		assertNotNull(settingIS);
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(settingIS);
		settingIS.close();
		Element rootElement = document.getRootElement();
		List<Element> sniffingloggers = rootElement.getChildren("sniffinglogger");
		assertTrue(sniffingloggers.size()>0);
		for (Element sniffinglogger : sniffingloggers) {
			String loggerName = sniffinglogger.getAttributeValue("id");
			assertNotNull(loggerName);
			names.add(loggerName);
			assertTrue(names.size()>0);
			System.out.printf("SniffingLogger Name : %s\n",loggerName);
		}
	}
	
	@Test
	public void alwaysTrace(){
		logger.info("test sniffer logger");
		for (String logName:names){
			assertEquals(Level.TRACE,Logger.getLogger(logName).getLevel());
			Logger.getLogger(logName).debug("see me!");
			Logger.getLogger(logName).trace("see me again!");
		}
	}
}
