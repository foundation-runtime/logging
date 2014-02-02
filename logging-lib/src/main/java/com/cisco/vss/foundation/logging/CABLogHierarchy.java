package com.cisco.vss.foundation.logging;

import org.apache.log4j.Hierarchy;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

/**
 *	This is the CAB implementation for the log4j Hierarchy.
 *	It's function is to add the CABHierarchyEventListener - in order to act upon appender addition and to delegate creation of new Loggers to the <code>CABLogFactory<code>. 
 * @author Yair Ogen
 */
public class CABLogHierarchy extends Hierarchy {
	
	private static LoggerFactory factory= new CABLogFactory();
	
	/**
	 * The deafult constructor. 
	 */
	public CABLogHierarchy(){
		 super(LogManager.getRootLogger());
		 addHierarchyEventListener(new CABHierarchyEventListener());
	 }	 	
	
	@Override
	/**
	 * calls getLogger with the CAB factory.
	 */
	public Logger getLogger(final String name) {
		return super.getLogger(name,factory);
	}
}
