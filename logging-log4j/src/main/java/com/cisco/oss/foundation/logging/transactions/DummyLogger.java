package com.cisco.oss.foundation.logging.transactions;

import org.slf4j.Logger;

/**
 * Class for logger which does no logging.
 * This class is used in multi-threaded transaction:
 * Each thread create DummyLogger instance in order to have a components list that will calculate components processing time within this thread
 * Eventually the components list of the DummyLogger instance is added to ComponentsMultiThread object of the main logger that will log all components
 * @author abrandwi
 *
 */
public class DummyLogger extends TransactionLogger {

  // ********* Public methods *********

  public static void start(final Logger logger, final Logger auditor) {
    createLoggingAction(logger, auditor, new DummyLogger());
  }

  public static void finish() {
    DummyLogger dummyLogger = (DummyLogger) getInstance();
    if (dummyLogger == null) {
      return;
    }

    dummyLogger.end();
  }
}