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

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.maccasoft.tools.internal.ImageRegistry;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortList;

public class SerialTerminal extends Window {

    public static final String PROP_PORT = "port";
    public static final String PROP_BAUD = "baud";

    public static final String[] BAUD_RATES = new String[] {
        String.valueOf(SerialPort.BAUDRATE_9600),
        String.valueOf(SerialPort.BAUDRATE_19200),
        String.valueOf(SerialPort.BAUDRATE_38400),
        String.valueOf(SerialPort.BAUDRATE_57600),
        String.valueOf(SerialPort.BAUDRATE_115200)
    };

    Display display;
    Composite container;
    Terminal term;

    Combo cursorKeys;
    Combo comPort;
    Combo baudRate;

    String port;
    int baud;
    SerialPort serialPort;

    final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    final SelectionAdapter comPortSelectionListener = new SelectionAdapter() {

        @Override
        public void widgetSelected(SelectionEvent e) {
            String oldPort = port;
            port = comPort.getText();
            updateSerialPortSettings();
            changeSupport.firePropertyChange(PROP_PORT, oldPort, port);
            term.setFocus();
        }
    };

    final SelectionAdapter baudRateSelectionListener = new SelectionAdapter() {

        @Override
        public void widgetSelected(SelectionEvent e) {
            int oldBaud = baud;
            baud = Integer.valueOf(baudRate.getText());
            updateSerialPortSettings();
            changeSupport.firePropertyChange(PROP_BAUD, oldBaud, baud);
            term.setFocus();
        }
    };

    final SerialPortEventListener serialEventListener = new SerialPortEventListener() {

        @Override
        public void serialEvent(SerialPortEvent serialPortEvent) {
            if (serialPortEvent.getEventType() == SerialPortEvent.RXCHAR) {
                try {
                    final byte[] rx = serialPort.readBytes();
                    if (rx != null) {
                        display.syncExec(new Runnable() {

                            @Override
                            public void run() {
                                term.print(rx);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    final KeyAdapter vt100TerminalKeyListener = new KeyAdapter() {

        @Override
        public void keyPressed(KeyEvent e) {
            try {
                if (!serialPort.isOpened()) {
                    return;
                }
                switch (e.keyCode) {
                    case SWT.ARROW_UP:
                        serialPort.writeByte((byte) 0x1B);
                        serialPort.writeByte((byte) 'A');
                        break;
                    case SWT.ARROW_DOWN:
                        serialPort.writeByte((byte) 0x1B);
                        serialPort.writeByte((byte) 'B');
                        break;
                    case SWT.ARROW_LEFT:
                        serialPort.writeByte((byte) 0x1B);
                        serialPort.writeByte((byte) 'D');
                        break;
                    case SWT.ARROW_RIGHT:
                        serialPort.writeByte((byte) 0x1B);
                        serialPort.writeByte((byte) 'C');
                        break;
                    case SWT.HOME:
                        serialPort.writeByte((byte) 0x1B);
                        serialPort.writeByte((byte) 'H');
                        break;
                    case SWT.END:
                        serialPort.writeByte((byte) 0x1B);
                        serialPort.writeByte((byte) 'K');
                        break;
                    case SWT.F1:
                    case SWT.F2:
                    case SWT.F3:
                    case SWT.F4:
                    case SWT.F5:
                    case SWT.F6:
                    case SWT.F7:
                    case SWT.F8:
                    case SWT.F9:
                    case SWT.F10:
                        serialPort.writeByte((byte) 0x1B);
                        serialPort.writeByte((byte) 'O');
                        serialPort.writeByte((byte) ('P' + (e.keyCode - SWT.F1)));
                        break;
                    default:
                        if (e.character != 0) {
                            serialPort.writeByte((byte) e.character);
                        }
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    final KeyAdapter wordstarTerminalKeyListener = new KeyAdapter() {

        @Override
        public void keyPressed(KeyEvent e) {
            try {
                if (!serialPort.isOpened()) {
                    return;
                }
                switch (e.keyCode) {
                    case SWT.ARROW_UP:
                        serialPort.writeByte((byte) 5);
                        break;
                    case SWT.ARROW_DOWN:
                        serialPort.writeByte((byte) 24);
                        break;
                    case SWT.ARROW_LEFT:
                        if ((e.stateMask & SWT.MOD2) != 0) {
                            serialPort.writeByte((byte) 1);
                        }
                        else {
                            serialPort.writeByte((byte) 19);
                        }
                        break;
                    case SWT.ARROW_RIGHT:
                        if ((e.stateMask & SWT.MOD2) != 0) {
                            serialPort.writeByte((byte) 6);
                        }
                        else {
                            serialPort.writeByte((byte) 4);
                        }
                        break;
                    case SWT.INSERT:
                        if ((e.stateMask & SWT.MOD2) != 0) {
                            pasteFromClipboard();
                        }
                        else {
                            serialPort.writeByte((byte) 22);
                        }
                        break;
                    case SWT.HOME:
                        serialPort.writeByte((byte) 17);
                        serialPort.writeByte((byte) 'S');
                        break;
                    case SWT.END:
                        serialPort.writeByte((byte) 17);
                        serialPort.writeByte((byte) 'D');
                        break;
                    case SWT.PAGE_UP:
                        serialPort.writeByte((byte) 18);
                        break;
                    case SWT.PAGE_DOWN:
                        serialPort.writeByte((byte) 3);
                        break;
                    default:
                        if (e.character == SWT.DEL) {
                            serialPort.writeByte((byte) 7);
                        }
                        else if (e.character != 0) {
                            serialPort.writeByte((byte) e.character);
                        }
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    KeyAdapter currentKeyListener;

    public SerialTerminal() {
        super((Shell) null);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);

        Image[] images = new Image[] {
            ImageRegistry.getImageFromResources("app128.png"),
            ImageRegistry.getImageFromResources("app64.png"),
            ImageRegistry.getImageFromResources("app48.png"),
            ImageRegistry.getImageFromResources("app32.png"),
            ImageRegistry.getImageFromResources("app16.png"),
        };
        newShell.setImages(images);

        newShell.setData(this);
    }

    @Override
    protected Control createContents(Composite parent) {
        display = parent.getDisplay();

        container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = layout.marginHeight = 0;
        container.setLayout(layout);

        term = new Terminal(container);
        Rectangle rect = term.getBounds();
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = rect.width;
        gridData.heightHint = rect.height;
        term.setLayoutData(gridData);

        createBottomControls(container);
        updateSerialPortSettings();

        currentKeyListener = vt100TerminalKeyListener;
        term.addKeyListener(currentKeyListener);

        container.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                try {
                    PropertyChangeListener[] l = changeSupport.getPropertyChangeListeners();
                    for (int i = 0; i < l.length; i++) {
                        changeSupport.removePropertyChangeListener(l[i]);
                    }

                    serialPort.removeEventListener();
                    if (serialPort.isOpened()) {
                        serialPort.closePort();
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        return container;
    }

    void createBottomControls(Composite parent) {
        int index;

        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(7, false);
        layout.marginBottom = layout.marginHeight;
        layout.marginLeft = layout.marginRight = layout.marginWidth;
        layout.marginWidth = layout.marginHeight = 0;
        container.setLayout(layout);
        container.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Label label = new Label(container, SWT.NONE);
        label.setText("Keys");

        cursorKeys = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        cursorKeys.setItems(new String[] {
            "VT-100",
            "WordStar"
        });
        cursorKeys.select(0);
        cursorKeys.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                term.removeKeyListener(currentKeyListener);
                switch (cursorKeys.getSelectionIndex()) {
                    case 0:
                        currentKeyListener = vt100TerminalKeyListener;
                        break;
                    case 1:
                        currentKeyListener = wordstarTerminalKeyListener;
                        break;
                }
                term.addKeyListener(currentKeyListener);
                term.setFocus();
            }
        });

        label = new Label(container, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        label = new Label(container, SWT.NONE);
        label.setText("Port");

        comPort = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        comPort.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        comPort.setItems(SerialPortList.getPortNames());
        index = port != null ? comPort.indexOf(port) : -1;
        if (index == -1) {
            index = 0;
            port = comPort.getItem(index);
        }
        comPort.select(index);
        comPort.addSelectionListener(comPortSelectionListener);

        label = new Label(container, SWT.NONE);
        label.setText("Baud");

        baudRate = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        baudRate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        baudRate.setItems(BAUD_RATES);
        index = baudRate.indexOf(String.valueOf(baud));
        if (index == -1) {
            index = 0;
            baud = Integer.valueOf(baudRate.getItem(index));
        }
        baudRate.select(index);
        baudRate.addSelectionListener(baudRateSelectionListener);
    }

    void updateSerialPortSettings() {
        if (serialPort == null || !serialPort.getPortName().equals(port)) {
            try {
                serialPort.removeEventListener();
                if (serialPort.isOpened()) {
                    serialPort.closePort();
                }
            } catch (Exception e) {

            }
            try {
                serialPort = new SerialPort(port);
                serialPort.openPort();
                serialPort.addEventListener(serialEventListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            serialPort.setParams(
                baud,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE,
                false,
                false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        getShell().setText("Serial Terminal on " + port);
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

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        if (comPort != null && !comPort.isDisposed()) {
            int index = comPort.indexOf(port);
            if (index != -1) {
                comPort.select(index);
            }
        }
        this.port = port;
    }

    public int getBaud() {
        return baud;
    }

    public void setBaud(int baud) {
        if (baudRate != null && !baudRate.isDisposed()) {
            int index = baudRate.indexOf(String.valueOf(baud));
            if (index != -1) {
                baudRate.select(index);
            }
        }
        this.baud = baud;
    }

    public void setFocus() {
        term.setFocus();
    }

    public SerialPort getSerialPort() {
        return serialPort;
    }

    void pasteFromClipboard() {
        Clipboard clipboard = new Clipboard(display);
        try {
            final String s = (String) clipboard.getContents(TextTransfer.getInstance());
            if (s != null) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            serialPort.writeString(s);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }).start();
            }
        } finally {
            clipboard.dispose();
        }
    }

    public void dispose() {
        getShell().dispose();
    }
}
