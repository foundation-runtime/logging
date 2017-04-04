package com.cisco.oss.foundation.logging.transactions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Timer {
	private static final Logger LOGGER = LoggerFactory.getLogger(Timer.class);
    private final String actionName;

    private long startTime = 0;
	private long time = 0;
	private boolean isRunning = false;

	
	public Timer(String actionName) {
        this.actionName = actionName;
	}
	
	public void start() {
		this.startTime = System.currentTimeMillis();
		this.isRunning = true;
    	LOGGER.trace("Start {} timer. Running:{} Time:{} (now={}).", actionName, this.isRunning, this.time, this.startTime);
	}

	public void pause() {
		if(this.isRunning) {
			this.isRunning = false;
			long latestTime = System.currentTimeMillis();
			this.time += latestTime - this.startTime;
	    	LOGGER.trace("Pause {} timer. Running:{} Time:{} (now={}).", actionName, this.isRunning, this.time, latestTime);
		}
	}
	
	public void reset() {
		this.time = 0;
		this.isRunning = false;
    	LOGGER.trace("Reset {} timer. Time:{}.", actionName, this.time);
	}

    /**
     * Allow adding external measure to the timer.
     */
	public void addMillis(long duration) {
	    time += duration;
        LOGGER.trace("Ad external duration {} timer. Running:{} Time:{} (addition={}).", actionName, this.isRunning, this.time, duration);
    }
	
	public long getTime() {
		return this.time;
	}

}
