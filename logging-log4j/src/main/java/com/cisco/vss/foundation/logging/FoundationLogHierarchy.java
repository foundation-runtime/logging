package com.cisco.vss.foundation.logging;

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
