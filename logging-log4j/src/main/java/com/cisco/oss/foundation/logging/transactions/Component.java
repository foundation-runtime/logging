package com.cisco.oss.foundation.logging.transactions;

/**
 * Hold component processing time
 * @author abrandwi
 *
 */
public class Component {

    private Timer timer;

	private final String componentType;

    public Component( String ComponentType ) {
        this.componentType = ComponentType;
        this.timer		= new Timer();
    }

    public long getLastTime() {
            return timer.getLastTime();
    }

    public long getTime() {
            return timer.getTime();
    }

    public void startTimer() {
    	timer.start(getComponentType());
    }

    public void pauseTimer() {
    	timer.pause(getComponentType());
    }

    public void resetTimer() {
        timer.reset(getComponentType());
    }

	public String getComponentType() {
		return componentType;
	}
}
