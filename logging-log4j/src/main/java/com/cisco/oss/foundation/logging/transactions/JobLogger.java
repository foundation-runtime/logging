package com.cisco.oss.foundation.logging.transactions;

public abstract class JobLogger extends TransactionLogger {

    protected String handledItemsType;        // Type of DB items that where handled in scheduler, e.g. catalogItem, pvr...
    protected int handledItemsNumber = 0;        // Number of DB items that where handled in scheduler
    protected int failures = 0;                // Number of failed actions in scheduler

    public static void addItemsHandled(String handledItemsType, int handledItemsNumber) {
    	JobLogger jobLogger = (JobLogger) getInstance();
        if (jobLogger == null) {
            return;
        }

        jobLogger.addItemsHandledInstance(handledItemsType, handledItemsNumber);
    }
    
    protected void addItemsHandledInstance(String handledItemsType, int handledItemsNumber) {
        this.handledItemsType = handledItemsType;
        this.handledItemsNumber += handledItemsNumber;
    }
    
    public static void addFailure() {
        JobLogger jobLogger = (JobLogger) getInstance();
        if (jobLogger == null) {
            return;
        }

        jobLogger.addFailureInstance(1);
    }

    protected void addFailureInstance(int num) {
        this.failures += num;
    }

}
