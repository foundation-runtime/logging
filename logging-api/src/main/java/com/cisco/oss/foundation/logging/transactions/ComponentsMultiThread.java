package com.cisco.oss.foundation.logging.transactions;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Hold all components processing time during multi-threaded transaction
 * Each thread add it's components list to this components List
 *
 * @author abrandwi
 */
public class ComponentsMultiThread {
    private List<Component> components = new CopyOnWriteArrayList<>();

    public List<Component> getComponents() {
        return components;
    }


}
