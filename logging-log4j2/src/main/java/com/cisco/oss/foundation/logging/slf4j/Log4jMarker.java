/*
 * Copyright 2014 Cisco Systems, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.cisco.oss.foundation.logging.slf4j;

import org.apache.logging.log4j.MarkerManager;
import org.slf4j.IMarkerFactory;
import org.slf4j.Marker;
import org.slf4j.impl.StaticMarkerBinder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Yair Ogen on 20/07/2014.
 */
public class Log4jMarker implements Marker, org.apache.logging.log4j.Marker {

    public static final long serialVersionUID = 1590472L;

    private final IMarkerFactory factory = StaticMarkerBinder.SINGLETON.getMarkerFactory();

    private final org.apache.logging.log4j.Marker log4jMarker;

    public Marker getMarker() {
        return marker;
    }

    private Marker marker;

    public Log4jMarker(Marker marker) {
        this.marker = marker;
        this.log4jMarker = new MarkerManager.Log4jMarker(marker.getName());
    }

    public Log4jMarker(org.apache.logging.log4j.Marker marker) {
//        this.marker = marker;
        this.log4jMarker = new MarkerManager.Log4jMarker(marker.getName());
    }

    public org.apache.logging.log4j.Marker getLog4jMarker() {
        return log4jMarker;
    }

    @Override
    public void add(final Marker marker) {
        final Marker m = factory.getMarker(marker.getName());
        this.log4jMarker.addParents(((Log4jMarker) m).getLog4jMarker());
    }

    @Override
    public boolean remove(final Marker marker) {
        return this.log4jMarker.remove(MarkerManager.getMarker(marker.getName()));
    }

    @Override
    public String getName() {
        return log4jMarker.getName();
    }

    @Override
    public boolean hasReferences() {
        return log4jMarker.hasParents();
    }

    @Override
    public boolean hasChildren() {
        return log4jMarker.hasParents();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Iterator iterator() {
        final List<Marker> parents = new ArrayList<Marker>();
        for (final org.apache.logging.log4j.Marker m : this.log4jMarker.getParents()) {
            parents.add(factory.getMarker(m.getName()));
        }
        return parents.iterator();
    }

    @Override
    public boolean contains(final org.slf4j.Marker marker) {
        return this.log4jMarker.isInstanceOf(marker.getName());
    }

    @Override
    public boolean contains(final String s) {
        return this.log4jMarker.isInstanceOf(s);
    }

    @Override
    public org.apache.logging.log4j.Marker[] getParents() {
        return log4jMarker.getParents();
    }

    @Override
    public boolean hasParents() {
        return log4jMarker.hasParents();
    }

    @Override
    public boolean isInstanceOf(org.apache.logging.log4j.Marker m) {
        return log4jMarker.isInstanceOf(m);
    }

    @Override
    public boolean isInstanceOf(String name) {
        return log4jMarker.isInstanceOf(name);
    }

    @Override
    public org.apache.logging.log4j.Marker addParents(org.apache.logging.log4j.Marker... markers) {
        return log4jMarker.addParents(markers);
    }

    @Override
    public org.apache.logging.log4j.Marker setParents(org.apache.logging.log4j.Marker... markers) {
        return log4jMarker.setParents(markers);
    }

    @Override
    public boolean remove(org.apache.logging.log4j.Marker marker) {
        return marker.remove(marker);
    }
}
