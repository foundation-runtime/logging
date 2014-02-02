package com.cisco.vss.foundation.logging;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The application can register state values with the CAB logging library. The values are logged when first registered, when values are changed, and at roll-over. This facility can be used to log persistent messages, e.g. log an entry when a failure is encountered, and log the corresponding clearing
 * message.
 * 
 * @author Jethro Revill
 * @author Yair Ogen
 */
public final class ApplicationState {

    /**
     * Eliminate the default constructor.
     */
    private ApplicationState() {
    }

    /**
     * Fully Qualified Class Name used when application state changes.
     */
    public static final String FQCN = ApplicationState.class.getName() + ".";

    /**
     * Map containing the application state values.
     */
    private static Map<Integer, ApplicationStateMessage> appState = Collections.synchronizedMap(new HashMap<Integer, ApplicationStateMessage>());

    private static AtomicInteger stateUniqueKey = new AtomicInteger(0);

    public static final Logger LOGGER = Logger.getLogger(ApplicationState.class);


    /**
     * Set an application state key/value pair (create). the value is logged
     * immediately. This method will return the generated key to be used later
     * on for updating or removing the state.
     *
     * @param level   Level of the log message
     * @param message Log message to output
     * @return The key created for future use.
     */
    public static Integer setState(final Level level, final Object message) {

        final Integer key = stateUniqueKey.incrementAndGet();
        final String messageString = message.toString();
        final ApplicationStateMessage newValue = new ApplicationStateMessage(level, messageString);

//        if (isApplicationStateEnabled()) {
            
//        }
        String flowContext = (String) MDC.get("flowCtxt");
		MDC.remove("flowCtxt");
        LOGGER.log(FQCN, level, message, null);
        if (flowContext != null) {
   			MDC.put("flowCtxt", flowContext);
   		}
        appState.put(key, newValue);
        return key;
    }


    /**
     * Update an application state key/value pair. If the value has changed then
     * it is logged immediately.
     *
     * @param key     Key of the state item
     * @param level   Level of the log message
     * @param message Log message to output	 *
     */
    public static void updateState(final Integer key, final Level level, final Object message) {

        final String messageString = message.toString();
        final ApplicationStateMessage newValue = new ApplicationStateMessage(level, messageString);
        final ApplicationStateMessage oldValue = appState.get(key);

        // Only add this item if it did not previously exist, or it has changed
        if (!newValue.equals(oldValue)) {
        	String flowContext = (String) MDC.get("flowCtxt");
    		MDC.remove("flowCtxt");
        	 LOGGER.log(FQCN, level, message, null);
        	 if (flowContext != null) {
       			MDC.put("flowCtxt", flowContext);
       		}
            appState.put(key, newValue);
           
        }
    }

    /**
     * Clear an application state item. If the value is removed then provided
     * message is logged immediately at the provided Level.
     *
     * @param key Key of the state item to remove
     */
    public static void removeState(final Integer key) {

        // Only log if an item was really removed, i.e. if a value existed for
        // this key
        final ApplicationStateMessage applicationStateMessage = appState.get(key);
        if (applicationStateMessage != null) {
         
        	appState.remove(key);
        	String flowContext = (String) MDC.get("flowCtxt");
    		MDC.remove("flowCtxt");
        	LOGGER.log(FQCN, applicationStateMessage.getLevel(), applicationStateMessage.getMessage(), null);
        	 if (flowContext != null) {
      			MDC.put("flowCtxt", flowContext);
      		}
        }
    }

    /**
     * Write all state items to the log file.
     *
     * @param logger the logger to use in order write the logs with.
     */
    public static void logState(final Appender appender) {

//        if (isApplicationStateEnabled()) {

            synchronized (appState) {

                final Collection<ApplicationStateMessage> entries = appState.values();
                for (ApplicationStateMessage entry : entries) {
                    if (entry.getLevel().isGreaterOrEqual(LOGGER.getEffectiveLevel())) {
                        final org.apache.log4j.spi.LoggingEvent loggingEvent = new org.apache.log4j.spi.LoggingEvent(FQCN, LOGGER, entry.getLevel(), entry.getMessage(), null);
                        appender.doAppend(loggingEvent);
                    }
                }

            }

//        }
    }

//    /**
//     * return true if presitent logging is enabled. can be turned on or off in log4j configuration file:<br>
//     * CABLogger.applicationstateEnabled=true<br>
//     * Default is set to true
//     *
//     * @return
//     */
//    public static boolean isApplicationStateEnabled() {
//
//        boolean isApplicationStateEnabled = true;
//
//        if (CABLogger.log4jConfigProps != null && CABLogger.log4jConfigProps.containsKey(CABLoggerConstants.APPSTATE_ENABLED.toString())) {
//            isApplicationStateEnabled = Boolean.valueOf(CABLogger.log4jConfigProps.getProperty(CABLoggerConstants.APPSTATE_ENABLED.toString()));
//        }
//
//        return isApplicationStateEnabled;
//
//    }

    /**
     * get all the application state entries that were set by a user.
     *
     * @return all the application state entries that were set by a user.
     */
    public static Collection<ApplicationStateMessage> getAppStateEntries() {
        return appState.values();
    }

    /**
     * A level, category, message triple held in the application state
     * collection.
     */
    public static class ApplicationStateMessage { // NOPMD

        /**
         * Level of the item.
         */
        private final Level level;


        /**
         * Message of the item.
         */
        private final String message;


        /**
         * Constructor.
         *
         * @param level   Level
         * @param message Message
         */
        public ApplicationStateMessage(final Level level, final String message) {
            this.level = level;
            this.message = message;
        }

        @Override
        public int hashCode() {
            int result = 1;
            result = 31 * result + ((level == null) ? 0 : level.hashCode());
            result = 31 * result + ((message == null) ? 0 : message.hashCode());
            return result;
        }

        @Override
        /*
           * Indicates whether some other object is "equal to" this one.
           *
           * @param anObject The reference object with which to compare
           *
           * @return true if this ApplicationStateMessage is the same as the
           * object argument, otherwise false.
           */
        public boolean equals(final Object obj) {// NOPMD
            if (this == obj) {
                return true;
            }

            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            final ApplicationStateMessage other = (ApplicationStateMessage) obj;

            if (level == null) {

                if (other.level != null) {
                    return false;
                }

            } else if (!level.equals(other.level)) {
                return false;
            }

            if (message == null) {

                if (other.message != null) {
                    return false;
                }

            } else if (!message.equals(other.message)) {
                return false;
            }

            return true;
        }

        public Level getLevel() {
            return level;
        }


        public String getMessage() {
            return message;
        }
    }

}