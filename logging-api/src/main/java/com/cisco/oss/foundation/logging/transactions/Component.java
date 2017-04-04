package com.cisco.oss.foundation.logging.transactions;

/**
 * Hold component processing time
 * @author abrandwi
 *
 */
public class Component {

    private Timer timer;

	private final String componentType;

    public Component( String componentType ) {
        this.componentType = componentType;
        this.timer		= new Timer(componentType);
    }

    public long getTime() {
            return timer.getTime();
    }

    public void startTimer() {
    	timer.start();
    }

    public void pauseTimer() {
    	timer.pause();
    }

    public void resetTimer() {
        timer.reset();
    }

    public void addMillis(long duration) {
        timer.addMillis(duration);
    }

	public String getComponentType() {
		return componentType;
	}
}
