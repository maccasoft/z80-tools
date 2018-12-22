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
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Preferences {

    public static final String PROP_ROOTS = "roots";
    public static final String PROP_EDITOR_FONT = "editorFont";
    public static final String PROP_SHOW_LINE_NUMBERS = "showLineNumbers";

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

    String[] roots;

    String editorFont;
    boolean showLineNumbers;
    boolean reloadOpenTabs;
    String[] openTabs;
    String selectedTab;

    boolean generateBinary;
    boolean generateHex;
    boolean generateListing;

    String lastPath;
    String serialPort;
    int serialBaud;
    List<String> lru;

    final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    Preferences() {
        showLineNumbers = true;
        reloadOpenTabs = true;

        generateHex = true;
        generateListing = true;

        serialBaud = 115200;
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

    public String[] getRoots() {
        return roots;
    }

    public void setRoots(String[] roots) {
        if (this.roots == roots) {
            return;
        }
        if (this.roots == null && roots != null && roots.length == 0) {
            return;
        }
        if (Arrays.deepEquals(this.roots, roots)) {
            return;
        }
        changeSupport.firePropertyChange(PROP_ROOTS, this.roots, this.roots = roots);
    }

    public String getEditorFont() {
        return editorFont;
    }

    public void setEditorFont(String editorFont) {
        if (this.editorFont != editorFont) {
            changeSupport.firePropertyChange(PROP_EDITOR_FONT, this.editorFont, this.editorFont = editorFont);
        }
    }

    public boolean isShowLineNumbers() {
        return showLineNumbers;
    }

    public void setShowLineNumbers(boolean showLineNumbers) {
        changeSupport.firePropertyChange(PROP_SHOW_LINE_NUMBERS, this.showLineNumbers, this.showLineNumbers = showLineNumbers);
    }

    public boolean isReloadOpenTabs() {
        return reloadOpenTabs;
    }

    public void setReloadOpenTabs(boolean reloadOpenTabs) {
        this.reloadOpenTabs = reloadOpenTabs;
    }

    public String[] getOpenTabs() {
        return openTabs;
    }

    public void setOpenTabs(String[] openTabs) {
        this.openTabs = openTabs;
    }

    public String getSelectedTab() {
        return selectedTab;
    }

    public void setSelectedTab(String selectedTab) {
        this.selectedTab = selectedTab;
    }

    public boolean isGenerateBinary() {
        return generateBinary;
    }

    public void setGenerateBinary(boolean generateBinary) {
        this.generateBinary = generateBinary;
    }

    public boolean isGenerateHex() {
        return generateHex;
    }

    public void setGenerateHex(boolean generateHex) {
        this.generateHex = generateHex;
    }

    public boolean isGenerateListing() {
        return generateListing;
    }

    public void setGenerateListing(boolean generateListing) {
        this.generateListing = generateListing;
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
    }

    public void removeLru(File file) {
        lru.remove(file.getAbsolutePath());
    }

    public String getSerialPort() {
        return serialPort;
    }

    public void setSerialPort(String serialPort) {
        this.serialPort = serialPort;
    }

    public int getSerialBaud() {
        return serialBaud;
    }

    public void setSerialBaud(int serialBaud) {
        this.serialBaud = serialBaud;
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
