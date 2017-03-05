package com.cisco.oss.foundation.logging.transactions;

import com.cisco.oss.foundation.flowcontext.FlowContextFactory;
import org.slf4j.event.Level;
import org.slf4j.Logger;

/**
 * Class for internal Task transactions logging
 *
 */
public class TaskLogger extends JobLogger{

    private enum TaskPropertyKey {TaskName, HandledType, HandledNumber, Failures, TaskResult, NotificationType};

    private String notificationType;

    public static void start(final Logger logger, final Logger auditor, final String taskName) {
        if (!createLoggingAction(logger, auditor, new TaskLogger())) {
            return;
        }

        TaskLogger taskLogger = (TaskLogger) getInstance();
        if (taskLogger == null) {
            return;
        }

        taskLogger.startInstance(taskName);
    }

    public static TaskLogger startAsync(final Logger logger, final Logger auditor, final String taskName) {

        TaskLogger taskLogger = new TaskLogger();
        if (!createLoggingAction(logger, auditor, taskLogger)) {
            return null;
        }

        taskLogger.startInstance(taskName);
        return taskLogger;
    }


    public static void success() {
        TaskLogger taskLogger = (TaskLogger) getInstance();
        if (taskLogger == null) {
            return;
        }

        taskLogger.successInstance(true);
    }

    public static void successAsync(TaskLogger taskLogger) {

        FlowContextFactory.deserializeNativeFlowContext(TransactionLogger.getFlowContextAsync(taskLogger));
        taskLogger.successInstance(true);



    }
    
    public static void success(boolean handledNotification) {
    	TaskLogger taskLogger = (TaskLogger) getInstance();
        if (taskLogger == null) {
          return;
        }

        taskLogger.successInstance(handledNotification);
      }
    
    public static void success(String taskResult) {
    	TaskLogger taskLogger = (TaskLogger) getInstance();
        if (taskLogger == null) {
            return;
        }

        addProperty(TaskPropertyKey.TaskResult.name(), taskResult);

        taskLogger.successInstance(true);
    }

    public static void failure(final Exception exception) {
        TaskLogger taskLogger = (TaskLogger) getInstance();
        if (taskLogger == null) {
            return;
        }

        taskLogger.failureInstance(exception);
    }

    public static void failureAsync(final String errorMessage, TaskLogger taskLogger) {
        taskLogger.failureInstance(errorMessage);
    }

    public static void addItemsHandledAsync(String handledItemsType, int handledItemsNumber, TaskLogger taskLogger) {
        taskLogger.addItemsHandledInstance(handledItemsType, handledItemsNumber);
    }

    public static void addFailure() {
        TaskLogger taskLogger = (TaskLogger) getInstance();
        if (taskLogger == null) {
            return;
        }

        taskLogger.addFailureInstance(1);
    }

    public static void addFailureAsync(TaskLogger taskLogger) {
        taskLogger.addFailureInstance(1);
    }

    public static void addFailures(int num) {
        TaskLogger taskLogger = (TaskLogger) getInstance();
        if (taskLogger == null) {
            return;
        }

        taskLogger.addFailureInstance(num);
    }

    public static void addFailuresAsync(int num, TaskLogger taskLogger) {
        taskLogger.addFailureInstance(num);
    }

    public static void addProperty(String propertyName, String propertyValue) {
        TaskLogger taskLogger = (TaskLogger) getInstance();
        if (taskLogger != null) {
            taskLogger.properties.put(propertyName, propertyValue);
        }
    }

    public static void addPropertyAsync(String propertyName, String propertyValue, TaskLogger taskLogger) {
        taskLogger.properties.put(propertyName, propertyValue);
    }

    protected void startInstance(String taskName) {
        try {
            addPropertiesStart(taskName);
            writePropertiesToLog(this.logger, Level.DEBUG);
        } catch (Exception e) {
            logger.error("Failed logging Task transaction start: " + e.getMessage(), e);
        }
    }

    protected void successInstance(boolean handledNotification) {
        try {
            end();
            addPropertiesSuccess();
            addPropertiesProcessingTime();
            
            if (handledNotification) {
            	writePropertiesToLog(this.auditor, Level.INFO);
            } else {
            	writePropertiesToLog(this.logger, Level.DEBUG);
            }
        } catch (Exception e) {
            logger.error("Failed logging Task transaction success: " + e.getMessage(), e);
        }
    }

    private void failureInstance(Exception exception) {
        try {
            end();
            addPropertiesFailure(exception, null);
            addPropertiesProcessingTime();
            writePropertiesToLog(this.auditor, Level.ERROR);
        } catch (Exception e) {
            logger.error("Failed logging Task transaction failure: " + e.getMessage(), e);
        }
    }

    private void failureInstance(String errorMessage) {
        try {
            end();
            addPropertiesFailure(null, errorMessage);
            addPropertiesProcessingTime();
            writePropertiesToLog(this.auditor, Level.ERROR);
        } catch (Exception e) {
            logger.error("Failed logging Task transaction failure: " + errorMessage);
        }
    }

    protected void addPropertiesStart(String taskName) {
        addPropertiesStart("Task", TaskPropertyKey.TaskName.name(), taskName);
    }

    protected void addPropertiesStart(String type, String nameFieldName, String name) {
        super.addPropertiesStart(type);
        this.properties.put(nameFieldName, name);
    }

    protected void addPropertiesSuccess() {
        super.addPropertiesSuccess();

        if (notificationType != null) {
        	this.properties.put(TaskPropertyKey.NotificationType.name(), notificationType);
        }
        
        if (handledItemsType != null) {
            this.properties.put(TaskPropertyKey.HandledType.name(), this.handledItemsType);
            this.properties.put(TaskPropertyKey.HandledNumber.name(), String.valueOf(this.handledItemsNumber));
        }
        //this.properties.put(TaskPropertyKey.Failures.name(), String.valueOf(this.failures));
    }

    private void addPropertiesFailure(Exception exception, String errorMessage) {
        super.addPropertiesFailure();

        if (errorMessage == null) {
            errorMessage = exception.getMessage();
        }

        if (handledItemsType != null) {
            this.properties.put(TaskPropertyKey.HandledType.name(), this.handledItemsType);
            this.properties.put(TaskPropertyKey.HandledNumber.name(), String.valueOf(this.handledItemsNumber));
        }
        this.properties.put(TaskPropertyKey.Failures.name(), String.valueOf(this.failures));
        this.properties.put(PropertyKey.ErrorMessage.name(), errorMessage);

        if (exception != null) {
            this.exception = exception;
        }
    }

    public static void setNotificationType(String notificationType) {
        TaskLogger taskLogger = (TaskLogger) getInstance();
        if (taskLogger == null) {
          return;
        }

        taskLogger.setNotificationTypeInstance(notificationType);
      }

    private void setNotificationTypeInstance(String notificationType) {
        this.notificationType	= notificationType;
      }
}