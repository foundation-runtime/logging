package com.cisco.oss.foundation.logging.transactions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by yzalazni on 1/22/2017.
 */
public class LoggingKeysHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingKeysHandler.class);

    private Map<String, String> keysMap;

    //generate a map with the logging available keys
    public LoggingKeysHandler(InputStream propertiesInputStream) {
        loadKeysFromStream(propertiesInputStream);
    }


    private void loadKeysFromStream(InputStream propertiesInputStream){
        LOGGER.info("Starting to load keys from map");
        Properties properties = new Properties();
        try {
            properties.load(propertiesInputStream);
            propertiesInputStream.close();
        }
        catch (Exception e) {
            LOGGER.error("Some issue finding or loading logging keys properties file. " + e.getMessage());
        }
        keysMap = (Map)properties;
        LOGGER.info("completed to load keys from map");
        LOGGER.info("The logging keysMap is " + keysMap.toString());

    }

    //get the key name to use in log from the logging keys map
    public String getKeyValue(String key){
        String keyName = keysMap.get(key);
        if (keyName != null){
            return keyName;
        }
        return ""; //key wasn't defined in keys properties file
    }

    public int getMapSize(){
     return keysMap.size();
    }





}
