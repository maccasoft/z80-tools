/*
 * Copyright (c) 2018 Marco Maccaferri and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.maccasoft.tools;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Preferences {

    public static final String PROP_LRU = "lru";
    public static final String PROP_LASTPATH = "lastPath";

    public static final String PREFERENCES_NAME = ".z80-tools";

    private static Preferences instance;

    public static Preferences getInstance() {
        if (instance != null) {
            return instance;
        }
        File file = new File(System.getProperty("user.home"), PREFERENCES_NAME);
        if (file.exists()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                instance = mapper.readValue(file, Preferences.class);
            } catch (Exception e) {
                e.printStackTrace();
                instance = new Preferences();
            }
        }
        else {
            instance = new Preferences();
        }
        return instance;
    }

    String lastPath;
    List<String> lru;

    final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    Preferences() {
        lru = new ArrayList<String>();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(propertyName, listener);
    }

    public String getLastPath() {
        return lastPath;
    }

    public void setLastPath(String lastPath) {
        this.lastPath = lastPath;
    }

    public List<String> getLru() {
        return lru;
    }

    public void addLru(File file) {
        lru.remove(file.getAbsolutePath());
        lru.add(0, file.getAbsolutePath());
        while (lru.size() > 10) {
            lru.remove(lru.size() - 1);
        }
        changeSupport.firePropertyChange(PROP_LRU, null, this.lru);
    }

    public void removeLru(File file) {
        lru.remove(file.getAbsolutePath());
        changeSupport.firePropertyChange(PROP_LRU, null, this.lru);
    }

    public void save() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        mapper.setSerializationInclusion(Include.NON_EMPTY);
        mapper.writeValue(new File(System.getProperty("user.home"), PREFERENCES_NAME), this);
    }

    public void dispose() {
        instance = null;
        for (PropertyChangeListener l : changeSupport.getPropertyChangeListeners()) {
            changeSupport.removePropertyChangeListener(l);
        }
    }
}
