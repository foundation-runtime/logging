/*
 * Copyright 2015 Cisco Systems, Inc.
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

/**
 * 
 */
package com.cisco.oss.foundation.logging;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Yair Ogen
 */
public class OperatingSystemTest {
	
	@Test
	public void opTest(){
		OperatingSystem operatingSystem = OperatingSystem.getOperatingSystem();
//		FoundationLogger.init();
//		assertTrue(operatingSystem.equals(OperatingSystem.Windows) || operatingSystem.equals(OperatingSystem.Linux));
		
		System.setProperty("os.name", "hp-ux");
		operatingSystem = OperatingSystem.getOperatingSystem();
		assertEquals(OperatingSystem.HPUX, operatingSystem);
		assertEquals(OperatingSystem.HPUX.toString(), operatingSystem.toString());
//		FoundationLogger.init();
		
		
		System.setProperty("os.name", "linux");
		operatingSystem = OperatingSystem.getOperatingSystem();
		assertEquals(OperatingSystem.Linux, operatingSystem);
//		FoundationLogger.init();
	}

}
