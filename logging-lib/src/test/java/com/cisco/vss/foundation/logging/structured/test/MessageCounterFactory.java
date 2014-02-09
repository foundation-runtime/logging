package com.cisco.vss.foundation.logging.structured.test;

public class MessageCounterFactory
{
	private static final ThreadLocal<Long> COUNTER_THREAD_LOCAL = new ThreadLocal<Long>();
	
	public static long getCounter()
	{
		Long counter = COUNTER_THREAD_LOCAL.get();
		if (counter == null)
		{
			COUNTER_THREAD_LOCAL.set(0L);
			counter = 0L;
		}
		//userFields.put("msgCounter", String.valueOf(counter++));
		COUNTER_THREAD_LOCAL.set(counter + 1);
		
		return counter;
	}
}
