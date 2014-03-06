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

package com.cisco.oss.foundation.logging;

import com.cisco.oss.foundation.environment.utils.EnvUtils;
import com.cisco.oss.foundation.flowcontext.FlowContextFactory;
import com.cisco.oss.foundation.logging.converters.FoundationLoggingCompNamePatternConverter;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class JSONTest {

	private static Logger LOGGER = LoggerFactory.getLogger(JSONTest.class);

	private static final String[] COMPONENTS = new String[] { "PPS", "UPM", "CMDC" };

	private static final int NUM_OF_ITER = 500000;

	private static Properties orig = new Properties();

	// @BeforeClass
	public static void init() throws Exception {
		URL origUrl = JSONTest.class.getResource("/log4j.properties");
		InputStream openStream = origUrl.openStream();

		orig.load(openStream);

		Properties temp = new Properties();
		openStream.close();

		openStream = origUrl.openStream();
		temp.load(openStream);
		openStream.close();

		updatePropertiesFile(origUrl, temp);

	}

	private static void updatePropertiesFile(URL origUrl, Properties props) throws FileNotFoundException, IOException {
		FileOutputStream out = new FileOutputStream(origUrl.getPath());
		props.store(out, null);
		out.flush();
		out.close();
	}

	@Test
	public void testJsonString() {

		// Object parse =
		// JSON.parse("{loggerName:\"JSONTest\",thread:\"main\",compName:\"UNKNOWN\",compInstance:\"UNKNOWNInstance1\",host:\"Yair Ogenlt\",time:{ \"$date\":\"2012-07-24T13:02:34.582\"},level:\"INFO\",message:\"this is my first message\"}");

		LOGGER.info("this is my first message");
		LOGGER.warn("warning {}. be ware of {}", "two", "the monster");
		LOGGER.error("error message", new RuntimeException("dummy exception"));
		LOGGER.error("error message 2");

		FlowContextFactory.createFlowContext();

		LOGGER.info("this is my second message");
	}

	@Test
	@Ignore
	public void testJsonStringLoad() {

		for (int i = 0; i < NUM_OF_ITER; i++) {			
			if (i % 3 == 0) {
				EnvUtils.updateEnv("_RPM_SOFTWARE_NAME", COMPONENTS[2]);
			} else if (i % 2 == 0) {
				EnvUtils.updateEnv("_RPM_SOFTWARE_NAME", COMPONENTS[1]);
			} else {
				EnvUtils.updateEnv("_RPM_SOFTWARE_NAME", COMPONENTS[0]);
			}
//			FoundationLoggingCompNamePatternConverter.componentName = FoundationLoggingCompNamePatternConverter.getComponentName();
			FlowContextFactory.createFlowContext();
			LOGGER.info("this is my first message");
			LOGGER.warn("warning {}. be ware of {}", "two", "the monster");
			LOGGER.error("error message", new RuntimeException("dummy exception"));
			LOGGER.error("error message 2");
			LOGGER.info("this is my second message");
		}
	}


	// @AfterClass
	public static void shutdown() throws Exception {

		URL origUrl = JSONTest.class.getResource("/log4j.properties");
		updatePropertiesFile(origUrl, orig);

	}
	
}
