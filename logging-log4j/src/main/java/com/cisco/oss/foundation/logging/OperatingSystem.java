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

import java.util.Locale;

/**
 * Enumeration of operating systems supported by the Foundation Logging Library.
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
