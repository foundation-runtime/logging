/**
 * 
 */
package com.cisco.vss.foundation.logging;

import org.apache.log4j.Logger;
import org.junit.Test;

/**
 *
 * @author Yair Ogen
 */
public class MyTest2 {
	
	private static final Logger logger = Logger.getLogger("com.nds");
	
	@Test
	public void printMe(){
		logger.info("printed in MyTest2");
	}

}
