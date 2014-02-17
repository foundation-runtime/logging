/**
 * 
 */
package com.cisco.vss.foundation.logging;

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
