package com.cisco.oss.foundation.logging.transactions;

import com.cisco.oss.foundation.flowcontext.FlowContextFactory;
import com.cisco.oss.foundation.ip.utils.IpUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;

/**
 * Abstract class for transactions logging
 * @author abrandwi
 *
 */
public abstract class TransactionLogger {

  protected final static String TOTAL_COMPONENT = "Total";

  // ********************************* Public properties *********************************
  // ********************************* Protected properties *********************************
  protected enum PropertyKey {Host, Type, Status, ErrorMessage};
  protected enum Status {Start, Success, Failure};

  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionLogger.class);

  protected Logger logger;
  protected Logger auditor;

  protected String flowContext;

  protected Map<String, String> properties; 			// Contains key-value properties to log
  protected Exception exception;

  protected static final String secondSeparator = " $ ";

  private static String separator = " | ";
  protected static LoggingKeysHandler loggingKeys;
  private static InputStream keysPropStream = TransactionLogger.class.getResourceAsStream("/loggingKeys.properties");


  // ********************************* Private properties *********************************

  private static final ThreadLocal<TransactionLogger> LOGGING_ACTION = new ThreadLocal<TransactionLogger>(); // Contains information for current thread's transaction

  private Map<String, Component> components; 	// Contains processing time of components during transaction
  private ComponentsMultiThread componentsMultiThread; // Contains processing time of components during multi-threaded transaction
  private Component total;
  private boolean finished;



  // ********************************* Public methods *********************************

  /**
   * Get 'TransactionLogger' instance of this thread-local
   * @return instance
   */
  public static TransactionLogger getInstance() {
    return LOGGING_ACTION.get();
  }

  /**
   * Set 'TransactionLogger' instance of this thread-local
   * @param instance
   */
  public static void setInstance(TransactionLogger instance) {
    LOGGING_ACTION.set(instance);

  }


  /**
   * Start component timer for current instance
   * @param type - of component
   */
  public static void startTimer(final String type) {
    TransactionLogger instance = getInstance();
    if (instance == null) {
      return;
    }

    instance.components.putIfAbsent(type, new Component(type));
    instance.components.get(type).startTimer();
  }

  public static void startTimerAsync(final String type, TransactionLogger transactionLogger) {
    transactionLogger.components.putIfAbsent(type, new Component(type));
    transactionLogger.components.get(type).startTimer();
  }

  /**
   * Pause component timer for current instance
   * @param type - of component
   */
  public static void pauseTimer(final String type) {
    TransactionLogger instance = getInstance();
    if (instance == null) {
      return;
    }

    instance.components.get(type).pauseTimer();
  }

  public static void pauseTimerAsync(final String type, TransactionLogger transactionLogger) {
    FlowContextFactory.deserializeNativeFlowContext(transactionLogger.getFlowContextAsync(transactionLogger));
    transactionLogger.components.get(type).pauseTimer();
  }


  /**
   * Log details of component processing (including processing time) to debug for current instance
   * @param type - of component
   * @param details - of component processing
   */
  public static void logComponent(final String type, final String details) {

    TransactionLogger instance = getInstance();
    logComponent(type, details, instance);
  }

  public static void logComponent(String type, String details, TransactionLogger instance) {
    if (instance == null || !instance.logger.isDebugEnabled()) {
      return;
    }

    StringBuilder builder = new StringBuilder("Completed " + type + " request. ")
            .append(details)
            .append(" Total:")
            .append(instance.components.get(type).getLastTime())
            .append("ms");

    instance.logger.debug(builder.toString());
  }

  /**
   * Get ComponentsMultiThread of current instance
   * @return componentsMultiThread
   */
  public static ComponentsMultiThread getComponentsMultiThread() {
    TransactionLogger instance = getInstance();
    if (instance == null) {
      return null;
    }

    return instance.componentsMultiThread;
  }

  /**
   * Get components list for current instance
   * @return components
   */
  public static Collection<Component> getComponentsList() {
    TransactionLogger instance = getInstance();
    if (instance == null) {
      return null;
    }

    return instance.components.values();
  }

  /**
   * Get string value of flow context for current instance
   * @return string value of flow context
   */
  public static String getFlowContext() {
    TransactionLogger instance = getInstance();
    if (instance == null) {
      return null;
    }

    return instance.flowContext;
  }

  public static String getFlowContextAsync(TransactionLogger logger) {
    return logger.flowContext;
  }

  /**
   * Add property to 'properties' map on transaction
   * @param key - of property
   * @param value - of property
   */
  public static void addProperty(String key, String value) {
    TransactionLogger instance = getInstance();
    if (instance != null) {
      instance.properties.put(key, value);
    }
  }


  public static void setSeparator(String separatorChar) {

      separator = separatorChar;

  }


  public static void setKeysPropStream(InputStream keysPropStream) {
    if(keysPropStream == null){
      LOGGER.error("logging-keys properties stream can't be null, logger will use default keys");
    }
    else {
      TransactionLogger.keysPropStream = keysPropStream;
    }
  }



  public String getSeparator() {
    return separator;
  }


  // ********************************* Protected methods *********************************

  /**
   * Create new logging action
   * This method check if there is an old instance for this thread-local
   * If not - Initialize new instance and set it as this thread-local's instance
   * @param logger
   * @param auditor
   * @param instance
   * @return whether new instance was set to thread-local
   */
  protected static boolean createLoggingAction(final Logger logger, final Logger auditor, final TransactionLogger instance) {
    TransactionLogger oldInstance = getInstance();
    if (oldInstance == null || oldInstance.finished) {
      if(loggingKeys == null) {
        loggingKeys = new LoggingKeysHandler(keysPropStream);
      }
        initInstance(instance, logger, auditor);
        setInstance(instance);
      return true;
    }
    return false; // Really not sure it can happen - since we arrive here in a new thread of transaction I think it's ThreadLocal should be empty. But leaving this code just in case...
  }

  protected static boolean createLoggingActionAsync(final Logger logger, final Logger auditor, final TransactionLogger instance) {
    if(loggingKeys == null) {
    loggingKeys = new LoggingKeysHandler(keysPropStream);
    }
    initInstance(instance, logger, auditor);
    return true;
  }

  /**
   * Add properties to 'properties' map on transaction start
   * @param type - of transaction
   */
  protected void addPropertiesStart(String type) {
    putProperty(PropertyKey.Host.name(), IpUtils.getHostName());
    putProperty(PropertyKey.Type.name(), type);
    putProperty(PropertyKey.Status.name(), Status.Start.name());
  }

  /**
   * Add properties to 'properties' map on transaction success
   */
  protected void addPropertiesSuccess() {
    putProperty(PropertyKey.Status.name(), Status.Success.name());
  }

  /**
   * Add properties to 'properties' map on transaction failure
   */
  protected void addPropertiesFailure() {
    putProperty(PropertyKey.Status.name(), Status.Failure.name());
  }

  /**
   * Add components processing time to 'properties' map:
   * key = componentType
   * value = time + "ms"
   */
  protected void addPropertiesProcessingTime() {

    HashMap<String, Long> mapComponentTimes			= new HashMap<>();
    HashMap<String, Long> mapComponentInvocations	= new HashMap<>();

    // For multi-threaded transaction: will calculate the sum of the time spent by all components in all threads
    if (this.componentsMultiThread != null) {
      for (Component component : this.componentsMultiThread.getComponents()) {
        addTimePerComponent(mapComponentTimes, component);
        addInvocationPerComponent(mapComponentInvocations, component);
      }
    }

    for (Component component : this.components.values()) {
      addTimePerComponent(mapComponentTimes, component);
    }

    for (Entry<String, Long> entry : mapComponentTimes.entrySet()) {
      putProperty(entry.getKey(), entry.getValue() + "ms");
    }

    for (Entry<String, Long> entry : mapComponentInvocations.entrySet()) {
      putProperty(entry.getKey(), entry.getValue().toString());
    }
    putProperty(TOTAL_COMPONENT, this.total.getTime() + "ms");
  }

  /**
   * Write 'properties' map to given log in given level - with pipe separator between each entry
   * Write exception stack trace to 'logger' in 'error' level, if not empty
   * @param logger
   * @param level - of logging
   */
  protected void writePropertiesToLog(Logger logger, Level level) {
    writeToLog(logger, level, getMapAsString(this.properties, separator), null);

    if (this.exception != null) {
      writeToLog(this.logger, Level.ERROR, "Error:", this.exception);
    }
  }

  /**
   * Get string representation of the given map:
   * key:value separator key:value separator ...
   * @param map
   * @param separator
   * @return string representation of the given map
   */
  protected static String getMapAsString(Map<String, String> map, String separator) {
    if (map != null && !map.isEmpty()) {
      StringBuilder str = new StringBuilder();
      boolean isFirst = true;
      for (Entry<String, String> entry : map.entrySet() ) {
        if (!isFirst) {
          str.append(separator);
        } else {
          isFirst = false;
        }
        str.append(entry.getKey()).append(":").append(entry.getValue());
      }
      return str.toString();
    }
    return null;
  }

  /**
   * End transaction:
   * 1. Pause total timer
   * 2. Mark instance as 'finished'
   */
  protected void end() {
    this.total.pauseTimer();
    this.finished = true;
  }

  // ********************************* Private methods *********************************

  /**
   * Initialize new instance
   * @param instance
   * @param logger
   * @param auditor
   */
  private static void initInstance(final TransactionLogger instance, final Logger logger, final Logger auditor) {
    instance.logger = logger;
    instance.auditor = auditor;
    instance.components = new LinkedHashMap<>();
    instance.properties = new LinkedHashMap<>();
    instance.total = new Component(TOTAL_COMPONENT);
    instance.total.startTimer();
    instance.componentsMultiThread = new ComponentsMultiThread();
    instance.flowContext = FlowContextFactory.serializeNativeFlowContext();
  }

  /**
   * Write the given pattern to given log in given logging level
   * @param logger
   * @param level
   * @param pattern
   * @param exception
   */
  private static void writeToLog(Logger logger, Level level, String pattern, Exception exception) {
    if (level == Level.ERROR) {
      logger.error(pattern, exception);
    } else if (level == Level.INFO) {
      logger.info(pattern);
    } else if (level == Level.DEBUG) {
      logger.debug(pattern);
    }
  }

  /**
   * Add component processing time to given map
   * @param mapComponentTimes
   * @param component
   */
  private static void addTimePerComponent(HashMap<String, Long> mapComponentTimes,	Component component) {
    Long currentTimeOfComponent = 0L;
    String key = component.getComponentType();
    if (mapComponentTimes.containsKey(key)) {
      currentTimeOfComponent = mapComponentTimes.get(key);
    }
    //when transactions are run in parallel, we should log the longest transaction only to avoid that 
    //for ex 'Total time' would be 100ms and transactions in parallel to hornetQ will be 2000ms 
    Long maxTime =  Math.max(component.getTime(), currentTimeOfComponent);
    mapComponentTimes.put(key, maxTime);
  }

  private static void addInvocationPerComponent(HashMap<String, Long> mapComponentInvocations, Component component) {
    Long invocationComponent = 0L;
    String key = component.getComponentType() + "_Invocations";
    if (mapComponentInvocations.containsKey(key)) {
      invocationComponent = mapComponentInvocations.get(key);
    }

    invocationComponent ++;
    mapComponentInvocations.put(key, invocationComponent);
  }


  protected void putProperty(String key , String value) {

    String keyName = loggingKeys.getKeyValue(key);
    if (keyName != ""){
      this.properties.put(keyName, value);
    } else {
      this.properties.put(key, value);
    }
  }
}
