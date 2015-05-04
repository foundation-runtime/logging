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

package com.cisco.oss.foundation.logging;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

/**
 * This factory is used for initializing a custom Logger that extends Log4j. The
 * goal is for the user to use the regular log4j API (Logger LOGGER =
 * Logger.getLogger()) but work in runtime with the NDSLogger transparently.
 * 
 * @author Yair Ogen
 */
public class FoundationLogFactory implements LoggerFactory {



	/**
	 * the Foundation Hierarchy that points to the FoundationLogFactory.
	 */
	static FoundationLogHierarchy foundationLogHierarchy = new FoundationLogHierarchy();// NOPMD

	static {
		FoundationLogger.init();
	}

	@Override
	public Logger makeNewLoggerInstance(final String name) {
		return new FoundationLogger(name);
	}

	
}
