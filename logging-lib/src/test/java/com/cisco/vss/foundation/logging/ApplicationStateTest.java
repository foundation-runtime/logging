/**
 * 
 */
package com.cisco.vss.foundation.logging;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import static org.junit.Assert.*;
import org.junit.Test;

import java.lang.reflect.Constructor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Yair Ogen
 */
public class ApplicationStateTest {
	
	static final Logger logger = Logger.getLogger(ApplicationStateTest.class);
	
	@Test
	public void equalstest(){
		
		logger.info("equalstest");
		
//		ApplicationStateMessage applicationStateMessage = new ApplicationStateMessage(Level.INFO, CategoryTerm.Audit, "my message");
//		ApplicationStateMessage applicationStateMessage1 = new ApplicationStateMessage(Level.ERROR, CategoryTerm.Audit, "my message");
//		ApplicationStateMessage applicationStateMessage2 = new ApplicationStateMessage(Level.INFO, CategoryTerm.Log, "my message");
//		ApplicationStateMessage applicationStateMessage3 = new ApplicationStateMessage(Level.INFO, null, "my message");
//		ApplicationStateMessage applicationStateMessage4 = new ApplicationStateMessage(Level.INFO,  CategoryTerm.Audit, null);
//		ApplicationStateMessage applicationStateMessage5 = new ApplicationStateMessage(null, CategoryTerm.Audit, "my message");
		
		
		ApplicationState.ApplicationStateMessage applicationStateMessage = new ApplicationState.ApplicationStateMessage(Level.INFO, "my message");
        ApplicationState.ApplicationStateMessage applicationStateMessage1 = new ApplicationState.ApplicationStateMessage(Level.ERROR, "my message");
        ApplicationState.ApplicationStateMessage applicationStateMessage2 = new ApplicationState.ApplicationStateMessage(Level.INFO, "my message");
        ApplicationState.ApplicationStateMessage applicationStateMessage3 = new ApplicationState.ApplicationStateMessage(Level.INFO, "my message");
        ApplicationState.ApplicationStateMessage applicationStateMessage4 = new ApplicationState.ApplicationStateMessage(Level.INFO, null);
        ApplicationState.ApplicationStateMessage applicationStateMessage5 = new ApplicationState.ApplicationStateMessage(null, "my message");
		
		assertFalse(applicationStateMessage.equals(new Object()));
		assertFalse(applicationStateMessage.equals(applicationStateMessage1));
//		assertFalse(applicationStateMessage.equals(applicationStateMessage2));
//		assertFalse(applicationStateMessage3.equals(applicationStateMessage));
		assertFalse(applicationStateMessage4.equals(applicationStateMessage));
		assertFalse(applicationStateMessage5.equals(applicationStateMessage));
		assertFalse(applicationStateMessage.equals(null));// NOPMD
		assertTrue(applicationStateMessage.hashCode() != 0);
		assertTrue(applicationStateMessage3.hashCode() != 0);
		assertTrue(applicationStateMessage4.hashCode() != 0);
		assertTrue(applicationStateMessage5.hashCode() != 0);
		assertEquals(applicationStateMessage, applicationStateMessage);
		
//		Properties log4jConfigProps = FoundationLogger.log4jConfigProps;
//		ApplicationState.logState(null);
//		try {
//			log4jConfigProps.put(ApplicationState.APPSTATE_ENABLED, Boolean.TRUE);
//			ApplicationState.logState(null);
//		} catch (Exception e) {			
//		}
//		
//		log4jConfigProps.put(ApplicationState.APPSTATE_ENABLED, Boolean.FALSE);
//		ApplicationState.logState(null);
		
	}
	
	
	@Test
	public void mockCtor() throws Exception {// NOPMD
		@SuppressWarnings("unchecked")
		Constructor<ApplicationState>[] constructors = (Constructor<ApplicationState>[]) ApplicationState.class.getDeclaredConstructors();
		for (Constructor<ApplicationState> constructor : constructors) {
			constructor.setAccessible(true);
			constructor.newInstance();
		}
	}

}
