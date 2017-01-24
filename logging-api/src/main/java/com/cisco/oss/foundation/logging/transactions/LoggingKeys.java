package com.cisco.oss.foundation.logging.transactions;

/**
 * Created by yzalazni on 1/18/2017.
 * List of keys that are presented in HTTP log.
 * The key name in log is defined as the value of each property in the keys map
 */
public enum LoggingKeys {

    HTTP_METHOD,
    IP_SRC,
    PORT_SRC,
    URL,
    HTTP_CODE,
    MSG,

    ResponseContentLength


}
