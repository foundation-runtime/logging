package com.cisco.oss.foundation.logging.transactions;

import org.slf4j.event.Level;
import org.slf4j.Logger;

/**
 * Class for internal scheduler transactions logging
 *
 * @author abrandwi
 */
public class SchedulerLogger extends JobLogger{

    private enum SchedulerPropertyKey {SchedulerName, HandledType, HandledNumber, Failures, SchedulerResult}
    ;

    public static void start(final Logger logger, final Logger auditor, final String schedulerName) {
        if (!createLoggingAction(logger, auditor, new SchedulerLogger())) {
            return;
        }

        SchedulerLogger schedulerLogger = (SchedulerLogger) getInstance();
        if (schedulerLogger == null) {
            return;
        }

        schedulerLogger.startInstance(schedulerName);
    }

    public static void success() {
        SchedulerLogger schedulerLogger = (SchedulerLogger) getInstance();
        if (schedulerLogger == null) {
            return;
        }

        schedulerLogger.successInstance();
    }
    
    public static void success(String schedulerResult) {
    	SchedulerLogger schedulerLogger = (SchedulerLogger) getInstance();
        if (schedulerLogger == null) {
            return;
        }

        addProperty(SchedulerPropertyKey.SchedulerResult.name(), schedulerResult);

        schedulerLogger.successInstance();

    }

    public static void failure(final Exception exception) {
        SchedulerLogger schedulerLogger = (SchedulerLogger) getInstance();
        if (schedulerLogger == null) {
            return;
        }

        schedulerLogger.failureInstance(exception);
    }

    public static void failure(final String errorMessage) {
        SchedulerLogger schedulerLogger = (SchedulerLogger) getInstance();
        if (schedulerLogger == null) {
            return;
        }

        schedulerLogger.failureInstance(errorMessage);
    }

    public static void failureAsync(final String errorMessage, SchedulerLogger schedulerLogger) {
        schedulerLogger.failureInstance(errorMessage);
    }

    public static void addItemsHandledAsync(String handledItemsType, int handledItemsNumber, SchedulerLogger schedulerLogger) {
        schedulerLogger.addItemsHandledInstance(handledItemsType, handledItemsNumber);
    }

    public static void addFailureAsync(SchedulerLogger schedulerLogger) {
        schedulerLogger.addFailureInstance(1);
    }

    public static void addFailures(int num) {
        SchedulerLogger schedulerLogger = (SchedulerLogger) getInstance();
        if (schedulerLogger == null) {
            return;
        }

        schedulerLogger.addFailureInstance(num);
    }

    public static void addFailuresAsync(int num, SchedulerLogger schedulerLogger) {
        schedulerLogger.addFailureInstance(num);
    }

    public static void addProperty(String propertyName, String propertyValue) {
        SchedulerLogger schedulerLogger = (SchedulerLogger) getInstance();
        if (schedulerLogger != null) {
            schedulerLogger.properties.put(propertyName, propertyValue);
        }
    }

    public static void addPropertyAsync(String propertyName, String propertyValue, SchedulerLogger schedulerLogger) {
        schedulerLogger.properties.put(propertyName, propertyValue);
    }

    protected void startInstance(String schedulerName) {
        try {
            addPropertiesStart(schedulerName);
            writePropertiesToLog(this.logger, Level.DEBUG);
        } catch (Exception e) {
            logger.error("Failed logging Scheduler transaction start: " + e.getMessage(), e);
        }
    }

    protected void successInstance() {
        try {
            end();
            addPropertiesSuccess();
            addPropertiesProcessingTime();
            writePropertiesToLog(this.auditor, Level.INFO);
        } catch (Exception e) {
            logger.error("Failed logging Scheduler transaction success: " + e.getMessage(), e);
        }
    }

    private void failureInstance(Exception exception) {
        try {
            end();
            addPropertiesFailure(exception, null);
            addPropertiesProcessingTime();
            writePropertiesToLog(this.auditor, Level.ERROR);
        } catch (Exception e) {
            logger.error("Failed logging Scheduler transaction failure: " + e.getMessage(), e);
        }
    }

    private void failureInstance(String errorMessage) {
        try {
            end();
            addPropertiesFailure(null, errorMessage);
            addPropertiesProcessingTime();
            writePropertiesToLog(this.auditor, Level.ERROR);
        } catch (Exception e) {
            logger.error("Failed logging Scheduler transaction failure: " + errorMessage);
        }
    }

    protected void addPropertiesStart(String schedulerName) {
        addPropertiesStart("Scheduler", SchedulerPropertyKey.SchedulerName.name(), schedulerName);
    }

    protected void addPropertiesStart(String type, String nameFieldName, String name) {
        super.addPropertiesStart(type);
        this.properties.put(nameFieldName, name);
    }

    protected void addPropertiesSuccess() {
        super.addPropertiesSuccess();

        if (handledItemsType != null) {
            this.properties.put(SchedulerPropertyKey.HandledType.name(), this.handledItemsType);
            this.properties.put(SchedulerPropertyKey.HandledNumber.name(), String.valueOf(this.handledItemsNumber));
        }
        //this.properties.put(SchedulerPropertyKey.Failures.name(), String.valueOf(this.failures));
    }

    private void addPropertiesFailure(Exception exception, String errorMessage) {
        super.addPropertiesFailure();

        if (errorMessage == null) {
            errorMessage = exception.getMessage();
        }

        if (handledItemsType != null) {
            this.properties.put(SchedulerPropertyKey.HandledType.name(), this.handledItemsType);
            this.properties.put(SchedulerPropertyKey.HandledNumber.name(), String.valueOf(this.handledItemsNumber));
        }
        this.properties.put(SchedulerPropertyKey.Failures.name(), String.valueOf(this.failures));
        this.properties.put(PropertyKey.ErrorMessage.name(), errorMessage);

        if (exception != null) {
            this.exception = exception;
        }
    }
}