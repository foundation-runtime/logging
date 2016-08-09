package com.cisco.oss.foundation.logging.transactions;

import com.cisco.oss.foundation.configuration.ConfigurationFactory;
import com.cisco.oss.foundation.configuration.FoundationConfigurationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

/**
 * Created by Nuna on 27/10/2015.
 */
public enum ConfigurationUtil implements FoundationConfigurationListener {

    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationUtil.class);

    private boolean verbose = false;

    private Environment environment;

    ConfigurationUtil() {

    }

    @Override
    public void configurationChanged() {
        reloadConfig();
    }

    private void reloadConfig() {
        if (environment == null) {
            verbose = ConfigurationFactory.getConfiguration().getBoolean("logging.verbose", false);
        }else{
            verbose = Boolean.valueOf(environment.getProperty("logging.verbose", "false"));
        }
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setEnvironment(Environment environment){
        this.environment = environment;
    }

    public static void setConfigSource(Environment environment) {
        boolean configuredReady = false;
        try{
            ConfigurationFactory.getConfiguration();
            configuredReady = true;
        }catch (Exception e){
            LOGGER.debug("can't load configuration. Error is: " + e);
        }

        if (environment != null && !configuredReady){
            INSTANCE.setEnvironment(environment);
        }

        INSTANCE.reloadConfig();
    }
}
