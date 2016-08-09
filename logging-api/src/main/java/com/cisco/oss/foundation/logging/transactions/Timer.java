package com.cisco.oss.foundation.logging.transactions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Timer {
	private static final Logger LOGGER = LoggerFactory.getLogger(Timer.class);	

	private long startTime = 0;
	private long time = 0;
	private long lastTime = 0;
	private boolean isRunning = false;	

	
	public Timer() {

	}
	
	public void start(String actionName) {
		this.startTime = System.currentTimeMillis();
		this.isRunning = true;
    	LOGGER.trace("Start {} timer. Running:{} Time:{} (now={}).", actionName, this.isRunning, this.time, this.startTime);
	}
	
	public void pause(String actionName) {
		if(this.isRunning) {
			this.isRunning = false;
			long latestTime = System.currentTimeMillis();
			this.lastTime = latestTime - this.startTime;
			this.time += this.lastTime;
	    	LOGGER.trace("Pause {} timer. Running:{} Time:{} (now={}).", actionName, this.isRunning, this.time, latestTime);
		}
	}
	
	public void reset(String actionName) {
		this.time = 0;
		this.isRunning = false;
    	LOGGER.trace("Reset {} timer. Time:{}.", actionName, this.time);
	}
	
	public long getLastTime() {
		return this.lastTime;
	}
	
	public long getTime() {
		return this.time;
	}

}
