/*
 * Copyright 2014 Cisco Systems, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.cisco.oss.foundation.logging.test;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * A test for testing the sniffer loggers
 *
 * @author yidwu
 */
public class SnifferLoggerTest {

    private static final Logger logger = LoggerFactory.getLogger(SnifferLoggerTest.class);

    List<String> names = new ArrayList<String>();

    @Before
    public void loadLoggerNames() throws Exception {
        InputStream settingIS = SnifferLoggerTest.class
                .getResourceAsStream("/sniffingLogger.xml");
        assertNotNull(settingIS);
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(settingIS);
        settingIS.close();
        Element rootElement = document.getRootElement();
        List<Element> sniffingloggers = rootElement.getChildren("sniffingLogger");
        assertTrue(sniffingloggers.size() > 0);
        for (Element sniffinglogger : sniffingloggers) {
            String loggerName = sniffinglogger.getAttributeValue("id");
            assertNotNull(loggerName);
            names.add(loggerName);
            assertTrue(names.size() > 0);
            System.out.printf("SniffingLogger Name : %s\n", loggerName);
        }
    }

    @Before
    public void valiadSnifferXMLByXSD() {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(SnifferLoggerTest.class.getResourceAsStream("/sniffingLogger.xsd")));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(SnifferLoggerTest.class.getResourceAsStream("/sniffingLogger.xml")));
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }

    @Test
    public void alwaysTrace() {
        logger.info("test sniffer logger");
        for (String logName : names) {
            Assert.assertEquals(Level.TRACE, LogManager.getLogger(logName).getLevel());
            LogManager.getLogger(logName).debug("see me!");
            LogManager.getLogger(logName).trace("see me again!");
        }
    }
}
