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
package com.cisco.oss.foundation.logging.test;

import com.cisco.oss.foundation.logging.ApplicationState;
import com.cisco.oss.foundation.logging.FoundationLevel;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 *
 * @author Yair Ogen
 */
public class ApplicationStateTest {
	
	static final Logger logger = LoggerFactory.getLogger(ApplicationStateTest.class);

    @Test
    public void testLog(){
        Integer state = ApplicationState.getInstance().setState(FoundationLevel.INFO, "application state message");
        ApplicationState.getInstance().updateState(state,FoundationLevel.INFO,"application state message updated");
    }
	
	@Test
	public void equalstest(){
		
		logger.info("equalstest");
		
//		ApplicationStateMessage applicationStateMessage = new ApplicationStateMessage(Level.INFO, CategoryTerm.Audit, "my message");
//		ApplicationStateMessage applicationStateMessage1 = new ApplicationStateMessage(Level.ERROR, CategoryTerm.Audit, "my message");
//		ApplicationStateMessage applicationStateMessage2 = new ApplicationStateMessage(Level.INFO, CategoryTerm.Log, "my message");
//		ApplicationStateMessage applicationStateMessage3 = new ApplicationStateMessage(Level.INFO, null, "my message");
//		ApplicationStateMessage applicationStateMessage4 = new ApplicationStateMessage(Level.INFO,  CategoryTerm.Audit, null);
//		ApplicationStateMessage applicationStateMessage5 = new ApplicationStateMessage(null, CategoryTerm.Audit, "my message");
		
		
		ApplicationState.ApplicationStateMessage applicationStateMessage = new ApplicationState.ApplicationStateMessage(FoundationLevel.INFO, "my message");
        ApplicationState.ApplicationStateMessage applicationStateMessage1 = new ApplicationState.ApplicationStateMessage(FoundationLevel.ERROR, "my message");
        ApplicationState.ApplicationStateMessage applicationStateMessage2 = new ApplicationState.ApplicationStateMessage(FoundationLevel.INFO, "my message");
        ApplicationState.ApplicationStateMessage applicationStateMessage3 = new ApplicationState.ApplicationStateMessage(FoundationLevel.INFO, "my message");
        ApplicationState.ApplicationStateMessage applicationStateMessage4 = new ApplicationState.ApplicationStateMessage(FoundationLevel.INFO, null);
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
	
	


}
