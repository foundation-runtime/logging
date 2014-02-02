package com.cisco.vss.foundation.logging;

import java.util.Locale;

/**
 * Enumeration of operating systems supported by the CAB Logging Library.
 * @author Jethro Revill
 */
enum OperatingSystem {

    /** Microsoft Windows operating systems. */
    Windows,

    /** HP Unix operating systems. */
    HPUX,

    /** Linux operating systems. */
    Linux;

    /**
     * The name of the Operating System returned by toString().
     */
    private String logName;

    /**
     * OperatingSystem constructor where the logName will be just the name of
     * the category.
     */
    private OperatingSystem() {
        logName = name();
    }

    /**
     * Obtains the current operating system.
     *
     * @return The current operating system
     */
    static OperatingSystem getOperatingSystem() {
    	final String osName = System.getProperty("os.name").toLowerCase(Locale.getDefault());
        if (osName.contains("windows")) {
            return Windows;
        } else if (osName.contains("hp-ux")) {
            return HPUX;
        } else {
            // assume linux
            return Linux;
        }
    }

    /**
     * Over-riden toString().
     *
     * @return The name of the operating system
     */
    public String toString() {
        return logName;
    }
}
