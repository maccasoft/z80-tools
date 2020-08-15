/*
 * Copyright (c) 2018-19 Marco Maccaferri and others.
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
    public static final String PROP_MNEMONIC_COLUMN = "mnemonicColumn";
    public static final String PROP_ARGUMENT_COLUMN = "argumentColumn";
    public static final String PROP_COMMENT_COLUMN = "commentColumn";
    public static final String PROP_USE_TABSTOPS = "useTabstops";
    public static final String PROP_TABWIDTH = "tabWidth";

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
    int lineDelimiters;
    String[] openTabs;
    String selectedTab;

    int mnemonicColumn;
    int argumentColumn;
    int commentColumn;
    int labelCase;
    int mnemonicCase;
    int directivePrefix;
    boolean useTabstops;
    int tabWidth;

    String[] includes;

    boolean generateBinary;
    boolean generateHex;
    boolean generateListing;

    String serialPort;
    int serialBaud;
    boolean serialFlowControl;
    String downloadCommand;
    String xmodemCommand;

    String romImage1;
    int romAddress1;
    String romImage2;
    int romAddress2;
    String compactFlashImage;
    boolean openTMS9918Window;
    int tms9918Ram;
    int tms9918Register;

    String debuggerCompactFlashImage;

    int lastUploadType;
    String lastPath;
    List<String> lru;

    final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    Preferences() {
        showLineNumbers = true;
        reloadOpenTabs = true;
        lineDelimiters = 1;

        tabWidth = 4;
        mnemonicColumn = 16;
        argumentColumn = mnemonicColumn + 6;
        commentColumn = mnemonicColumn + 40;
        labelCase = SourceFormatter.NO_CHANGE;
        mnemonicCase = SourceFormatter.TO_UPPER;

        generateHex = true;
        generateListing = true;

        serialBaud = 115200;
        downloadCommand = "A:DOWNLOAD {0}";
        xmodemCommand = "A:XMODEM {0} /R /X0 /Q";

        tms9918Ram = 0x98;
        tms9918Register = 0x99;

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

    public int getLineDelimiters() {
        return lineDelimiters;
    }

    public void setLineDelimiters(int lineDelimiters) {
        this.lineDelimiters = lineDelimiters;
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

    public int getMnemonicColumn() {
        return mnemonicColumn;
    }

    public void setMnemonicColumn(int mnemonicColumn) {
        changeSupport.firePropertyChange(PROP_MNEMONIC_COLUMN, this.mnemonicColumn, this.mnemonicColumn = mnemonicColumn);
    }

    public int getArgumentColumn() {
        return argumentColumn;
    }

    public void setArgumentColumn(int argumentColumn) {
        changeSupport.firePropertyChange(PROP_ARGUMENT_COLUMN, this.argumentColumn, this.argumentColumn = argumentColumn);
    }

    public int getCommentColumn() {
        return commentColumn;
    }

    public void setCommentColumn(int commentColumn) {
        changeSupport.firePropertyChange(PROP_COMMENT_COLUMN, this.commentColumn, this.commentColumn = commentColumn);
    }

    public int getLabelCase() {
        return labelCase;
    }

    public void setLabelCase(int labelCase) {
        this.labelCase = labelCase;
    }

    public int getMnemonicCase() {
        return mnemonicCase;
    }

    public void setMnemonicCase(int mnemonicCase) {
        this.mnemonicCase = mnemonicCase;
    }

    public int getDirectivePrefix() {
        return directivePrefix;
    }

    public void setDirectivePrefix(int directivePrefix) {
        this.directivePrefix = directivePrefix;
    }

    public boolean isUseTabstops() {
        return useTabstops;
    }

    public void setUseTabstops(boolean useTabstops) {
        changeSupport.firePropertyChange(PROP_USE_TABSTOPS, this.useTabstops, this.useTabstops = useTabstops);
    }

    public int getTabWidth() {
        return tabWidth;
    }

    public void setTabWidth(int tabWidth) {
        changeSupport.firePropertyChange(PROP_TABWIDTH, this.tabWidth, this.tabWidth = tabWidth);
    }

    public String[] getIncludes() {
        return includes;
    }

    public void setIncludes(String[] includes) {
        this.includes = includes;
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

    public boolean isSerialFlowControl() {
        return serialFlowControl;
    }

    public void setSerialFlowControl(boolean serialFlowControl) {
        this.serialFlowControl = serialFlowControl;
    }

    public String getDownloadCommand() {
        return downloadCommand;
    }

    public void setDownloadCommand(String downloadCommand) {
        this.downloadCommand = downloadCommand;
    }

    public String getXmodemCommand() {
        return xmodemCommand;
    }

    public void setXmodemCommand(String xmodemCommand) {
        this.xmodemCommand = xmodemCommand;
    }

    public int getLastUploadType() {
        return lastUploadType;
    }

    public void setLastUploadType(int lastUploadType) {
        this.lastUploadType = lastUploadType;
    }

    public String getRomImage1() {
        return romImage1;
    }

    public void setRomImage1(String romImage) {
        this.romImage1 = romImage;
    }

    public int getRomAddress1() {
        return romAddress1;
    }

    public void setRomAddress1(int romAddress) {
        this.romAddress1 = romAddress;
    }

    public String getRomImage2() {
        return romImage2;
    }

    public void setRomImage2(String romImage) {
        this.romImage2 = romImage;
    }

    public int getRomAddress2() {
        return romAddress2;
    }

    public void setRomAddress2(int romAddress) {
        this.romAddress2 = romAddress;
    }

    public String getCompactFlashImage() {
        return compactFlashImage;
    }

    public void setCompactFlashImage(String compactFlashImage) {
        this.compactFlashImage = compactFlashImage;
    }

    public boolean isOpenTMS9918Window() {
        return openTMS9918Window;
    }

    public void setOpenTMS9918Window(boolean openTMS9918Window) {
        this.openTMS9918Window = openTMS9918Window;
    }

    public int getTms9918Ram() {
        return tms9918Ram;
    }

    public void setTms9918Ram(int tms9918Data) {
        this.tms9918Ram = tms9918Data;
    }

    public int getTms9918Register() {
        return tms9918Register;
    }

    public void setTms9918Register(int tms9918Control) {
        this.tms9918Register = tms9918Control;
    }

    public String getDebuggerCompactFlashImage() {
        return debuggerCompactFlashImage;
    }

    public void setDebuggerCompactFlashImage(String debuggerCompactFlashImage) {
        this.debuggerCompactFlashImage = debuggerCompactFlashImage;
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
