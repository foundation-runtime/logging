package com.cisco.oss.foundation.logging.transactions;

import org.apache.commons.lang.StringUtils;
import org.slf4j.event.Level;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;

/**
 * Class for MessageQueue transactions logging
 * @author abrandwi
 *
 */
public class MessageQueueLogger extends TransactionLogger {

  private enum MessageQueuePropertyKey {NotificationType};
  private enum MessageQueueVerbosePropertyKey {NotificationBody};

  private String notificationType;

  @Autowired
  private Environment environment;

  public MessageQueueLogger(){

  }

  @PostConstruct
  public void init(){
    ConfigurationUtil.setConfigSource(environment);
  }

  public static void start(final Logger logger, final Logger auditor, String notification) {
    if(!createLoggingAction(logger, auditor, new MessageQueueLogger())) {
      return;
    }

    MessageQueueLogger messageQueueLogger = (MessageQueueLogger) getInstance();
    if (messageQueueLogger == null) {
      return;
    }

    messageQueueLogger.startInstance(notification);
  }

  public static void startAsync(final Logger logger, final Logger auditor, String notification) {
    MessageQueueLogger messageQueueLogger = new MessageQueueLogger();
    if(!createLoggingActionAsync(logger, auditor, messageQueueLogger)) {
      return;
    }

    messageQueueLogger.startInstance(notification);
  }

  /**
   * @param handledNotification - whether notification was relevant and handled or irrelevant and ignored
   */
  public static void success(boolean handledNotification) {
    MessageQueueLogger messageQueueLogger = (MessageQueueLogger) getInstance();
    if (messageQueueLogger == null) {
      return;
    }

    messageQueueLogger.successInstance(handledNotification);
  }

  public static void successAsync(boolean handledNotification, MessageQueueLogger messageQueueLogger) {
    messageQueueLogger.successInstance(handledNotification);
  }

  public static void failure(final Exception exception) {
    MessageQueueLogger messageQueueLogger = (MessageQueueLogger) getInstance();
    if (messageQueueLogger == null) {
      return;
    }

    messageQueueLogger.failureInstance(exception);
  }

  public static void failureAsync(final Exception exception, MessageQueueLogger messageQueueLogger) {
    messageQueueLogger.failureInstance(exception);
  }

  public static void setNotificationType(String notificationType) {
    MessageQueueLogger messageQueueLogger = (MessageQueueLogger) getInstance();
    if (messageQueueLogger == null) {
      return;
    }

    messageQueueLogger.setNotificationTypeInstance(notificationType);
  }

  public static void setNotificationTypeAsync(String notificationType, MessageQueueLogger messageQueueLogger) {
    messageQueueLogger.setNotificationTypeInstance(notificationType);
  }

  private void startInstance(String notification) {
    try {
      addPropertiesStart(notification);
      writePropertiesToLog(logger, Level.DEBUG);
    } catch (Exception e) {
      this.logger.error("Failed logging Message Queue transaction start: " + e.getMessage(), e);
    }
  }

  private void successInstance(boolean handledNotification) {
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
      logger.error("Failed logging Message Queue transaction success: " + e.getMessage(), e);
    }
  }

  private void failureInstance(Exception exception) {
    try {
      end();
      addPropertiesFailure(exception);
      addPropertiesProcessingTime();
      writePropertiesToLog(this.auditor, Level.ERROR);
    } catch (Exception e) {
      logger.error("Failed logging Message Queue transaction failure: " + e.getMessage(), e);
    }
  }

  private void setNotificationTypeInstance(String notificationType) {
    this.notificationType	= notificationType;
  }

  protected void addPropertiesStart(String notification) {
    super.addPropertiesStart("MessageQueue");

    addVerbosePropertiesStart(notification);
  }

  private void addVerbosePropertiesStart(String notification) {
    if (ConfigurationUtil.INSTANCE.isVerbose()) {
      if (notification != null) {
        this.properties.put(MessageQueueVerbosePropertyKey.NotificationBody.name(), StringUtils.deleteWhitespace(notification));
      }
    }
  }

  protected void addPropertiesSuccess() {
    super.addPropertiesSuccess();

    this.properties.put(MessageQueuePropertyKey.NotificationType.name(), notificationType);
  }

  private void addPropertiesFailure(Exception exception) {
    super.addPropertiesFailure();

    this.properties.put(MessageQueuePropertyKey.NotificationType.name(), notificationType);
    this.properties.put(PropertyKey.ErrorMessage.name(), exception.getMessage());

    this.exception = exception;
  }
}