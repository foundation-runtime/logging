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

import org.apache.log4j.Hierarchy;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

/**
 *	This is the Foundation implementation for the log4j Hierarchy.
 *	It's function is to add the FoundationHierarchyEventListener - in order to act upon appender addition and to delegate creation of new Loggers to the <code>FoundationLogFactory<code>.
 * @author Yair Ogen
 */
public class FoundationLogHierarchy extends Hierarchy {
	
	private static LoggerFactory factory= new FoundationLogFactory();
	
	/**
	 * The deafult constructor. 
	 */
	public FoundationLogHierarchy(){
		 super(LogManager.getRootLogger());
		 addHierarchyEventListener(new FoundationHierarchyEventListener());
	 }	 	
	
	@Override
	/**
	 * calls getLogger with the Foundation factory.
	 */
	public Logger getLogger(final String name) {
		return super.getLogger(name,factory);
	}
}
