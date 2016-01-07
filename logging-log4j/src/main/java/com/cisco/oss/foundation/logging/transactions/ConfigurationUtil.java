package com.cisco.oss.foundation.logging.transactions;

import com.cisco.oss.foundation.configuration.ConfigurationFactory;
import com.cisco.oss.foundation.configuration.FoundationConfigurationListener;

/**
 * Created by Nuna on 27/10/2015.
 */
public enum ConfigurationUtil implements FoundationConfigurationListener {

    INSTANCE;

    private boolean verbose = false;

    ConfigurationUtil() {
        refresh();
    }

    @Override
    public void configurationChanged() {
        refresh();
    }

    private void refresh() {
        verbose = ConfigurationFactory.getConfiguration().getBoolean("logging.verbose", false);
    }

    public boolean isVerbose() {
        return verbose;
    }
}
