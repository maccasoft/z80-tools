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

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
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
import jssc.SerialPortException;
import jssc.SerialPortList;
import jssc.SerialPortTimeoutException;

public class SerialTerminal extends Window {

    public static final String[] BAUD_RATES = new String[] {
        String.valueOf(SerialPort.BAUDRATE_9600),
        String.valueOf(SerialPort.BAUDRATE_19200),
        String.valueOf(SerialPort.BAUDRATE_38400),
        String.valueOf(SerialPort.BAUDRATE_57600),
        String.valueOf(SerialPort.BAUDRATE_115200)
    };

    public static final byte SOH = 0x01;
    public static final byte EOT = 0x04;
    public static final byte ACK = 0x06;
    public static final byte NAK = 0x15;
    public static final byte CAN = 0x18;
    public static final byte C = 0x43; // 'C' which use in XModem/CRC

    Display display;
    Composite container;
    Terminal term;

    Combo cursorKeys;
    Combo comPort;
    Combo baudRate;

    String port;
    int baud;
    SerialPort serialPort;

    Preferences preferences;

    final SelectionAdapter comPortSelectionListener = new SelectionAdapter() {

        @Override
        public void widgetSelected(SelectionEvent e) {
            port = comPort.getText();
            updateSerialPortSettings();
            preferences.setSerialPort(port);
            term.setFocus();
        }
    };

    final SelectionAdapter baudRateSelectionListener = new SelectionAdapter() {

        @Override
        public void widgetSelected(SelectionEvent e) {
            baud = Integer.valueOf(baudRate.getText());
            updateSerialPortSettings();
            preferences.setSerialBaud(baud);
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
                                term.write(rx);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

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

        preferences = Preferences.getInstance();
        port = preferences.getSerialPort();
        baud = preferences.getSerialBaud();

        container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = layout.marginHeight = 0;
        container.setLayout(layout);

        term = new Terminal(container) {

            @Override
            protected void writeByte(byte b) {
                try {
                    if (!serialPort.isOpened()) {
                        return;
                    }
                    serialPort.writeByte(b);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        };
        Rectangle rect = term.getBounds();
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = rect.width;
        gridData.heightHint = rect.height;
        term.setLayoutData(gridData);

        createBottomControls(container);
        updateSerialPortSettings();

        container.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                try {
                    serialPort.removeEventListener();
                } catch (Exception ex) {
                    // Do nothing
                }
                try {
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
        GridLayout layout = new GridLayout(1, false);
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
                switch (cursorKeys.getSelectionIndex()) {
                    case 0:
                        term.setCursorKeys(Terminal.CURSORS_VT100);
                        break;
                    case 1:
                        term.setCursorKeys(Terminal.CURSORS_WORDSTAR);
                        break;
                }
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

        layout.numColumns = container.getChildren().length;
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

    public void setFocus() {
        term.setFocus();
    }

    public SerialPort getSerialPort() {
        return serialPort;
    }

    public void dispose() {
        getShell().dispose();
    }

    public void uploadPackedBinary(String name, byte[] data, IProgressMonitor monitor) throws SerialPortException, SerialPortTimeoutException {
        int checksum;
        String s;

        try {
            serialPort.removeEventListener();
        } catch (SerialPortException e) {
            // Do nothing
        }

        try {
            s = preferences.getDownloadCommand();
            s = s.replace("{0}", name.toUpperCase());
            serialPort.writeString(s);
            serialPort.writeInt(13);
            flushOutput();
            do {
                s = readString();
            } while (s.length() != 0 && !s.contains("DOWNLOAD "));

            serialPort.writeString("U0\r");
            flushOutput();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                // Do nothing
            }

            serialPort.writeString(":");
            flushOutput();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                // Do nothing
            }

            checksum = 0;
            for (int i = 0; i < data.length; i++) {
                serialPort.writeString(String.format("%02X", data[i] & 0xFF));
                flushOutput();
                checksum += data[i] & 0xFF;
                if (i > 0 && (i & 127) == 0) {
                    waitDot();
                    if (monitor != null) {
                        monitor.worked(1);
                    }
                }
                if (monitor != null && monitor.isCanceled()) {
                    return;
                }
            }

            serialPort.writeString(">");
            flushOutput();
            if ((data.length & 127) != 0) {
                waitDot();
                if (monitor != null) {
                    monitor.worked(1);
                }
            }
        } finally {
            try {
                serialPort.addEventListener(serialEventListener);
            } catch (SerialPortException e) {
                // Do nothing
            }
        }

        serialPort.writeString(String.format("%02X", data.length & 0xFF));
        flushOutput();

        serialPort.writeString(String.format("%02X", checksum & 0xFF));
        flushOutput();
    }

    void flushOutput() throws SerialPortException {
        while (serialPort.getOutputBufferBytesCount() > 0) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // Do nothing
            }
        }
    }

    String readString() throws SerialPortException, SerialPortTimeoutException {
        String s = "";

        while (true) {
            byte[] b = serialPort.readBytes(1, 5000);
            print(b[0]);
            if (b[0] == '\r') {
                try {
                    b = serialPort.readBytes(1, 200);
                    print(b[0]);
                } catch (SerialPortTimeoutException e) {
                    // Do nothing
                }
                break;
            }
            if (b[0] >= ' ') {
                s = s + (char) b[0];
            }
        }

        return s;
    }

    void print(byte b) {
        display.syncExec(new Runnable() {

            @Override
            public void run() {
                term.write(b);
            }
        });
    }

    void waitDot() throws SerialPortException, SerialPortTimeoutException {
        while (true) {
            byte[] b = serialPort.readBytes(1, 5000);
            if (b != null) {
                print(b[0]);
                if (b[0] == '.') {
                    break;
                }
            }
        }
    }

    public void uploadXModem(String name, byte[] data, IProgressMonitor monitor) throws SerialPortException, SerialPortTimeoutException {
        String s;

        try {
            serialPort.removeEventListener();
        } catch (SerialPortException e) {
            // Do nothing
        }

        try {
            s = preferences.getXmodemCommand();
            s = s.replace("{0}", name.toUpperCase());
            serialPort.writeString(s);
            serialPort.writeInt(13);
            flushOutput();
            do {
                s = readString();
            } while (s.length() != 0 && !s.contains("XMODEM "));

            s = "";
            while (true) {
                byte[] b = serialPort.readBytes(1, 15000);
                print(b[0]);
                if (b[0] >= ' ') {
                    s = s + (char) b[0];
                    if (s.endsWith("Overwrite (Y/N)?")) {
                        serialPort.writeString("Y\r");
                        s = "";
                    }
                    if (s.endsWith("with CRCs")) {
                        break;
                    }
                }
            }

            boolean doCrc = false;
            int packet = 1;
            int errors = 0;

            int i = 0;
            while (i < data.length) {
                try {
                    byte[] b = serialPort.readBytes(1, 15000);

                    if (b[0] == CAN) {
                        break;
                    }

                    if (b[0] == C) {
                        doCrc = true;
                        b[0] = NAK;
                    }

                    if (b[0] == ACK) {
                        i += 128;
                        packet++;
                        if (monitor != null) {
                            monitor.worked(1);
                        }
                        errors++;
                    }

                    if (b[0] == NAK) {
                        errors++;
                        if (errors >= 10) {
                            return;
                        }
                    }

                    if (b[0] == ACK || b[0] == NAK) {
                        serialPort.writeByte(SOH);
                        serialPort.writeByte((byte) packet);
                        serialPort.writeByte((byte) (packet ^ 0xFF));

                        int checksum = 0, crc = 0;
                        int x = 0;
                        while (x < 128 && (i + x) < data.length) {
                            serialPort.writeByte(data[i + x]);
                            checksum += data[i + x] & 0xFF;
                            crc = updateCrc(crc, data[i + x] & 0xFF);
                            x++;
                        }
                        if (x < 128) {
                            serialPort.writeByte((byte) 0x1A);
                            checksum += 0x1A;
                            crc = updateCrc(crc, 0x1A);
                            x++;
                            while (x < 128) {
                                serialPort.writeByte((byte) 0);
                                crc = updateCrc(crc, 0);
                                x++;
                            }
                        }

                        if (doCrc) {
                            serialPort.writeByte((byte) ((crc >> 8) & 0xFF));
                            serialPort.writeByte((byte) (crc & 0xFF));
                        }
                        else {
                            serialPort.writeByte((byte) checksum);
                        }
                    }

                    if (monitor != null && monitor.isCanceled()) {
                        serialPort.writeBytes(new byte[] {
                            CAN, CAN, CAN
                        });
                        return;
                    }
                } catch (SerialPortTimeoutException e) {
                    errors++;
                    if (errors >= 10) {
                        return;
                    }
                }
            }

            if (i >= data.length) {
                serialPort.writeByte(EOT);
                byte[] b = serialPort.readBytes(1, 5000);
                if (b[0] == NAK) {
                    serialPort.writeByte(EOT);
                }
                b = serialPort.readBytes(1, 5000);
                if (b[0] == NAK) {
                    serialPort.writeByte(EOT);
                }
            }
        } finally {
            try {
                serialPort.addEventListener(serialEventListener);
            } catch (SerialPortException e) {
                // Do nothing
            }
        }
    }

    int updateCrc(int crc, int b) {
        for (int i = 0; i < 8; i++) {
            boolean bit = ((b >>> (7 - i) & 1) == 1);
            boolean c15 = ((crc >>> 15 & 1) == 1);
            crc <<= 1;
            if (c15 ^ bit) {
                crc ^= 0x1021;
            }
        }
        return crc & 0xFFFF;
    }

    static {
        System.setProperty("SWT_GTK3", "0");
    }

    public static void main(String[] args) {
        final Display display = new Display();

        Realm.runWithDefault(DisplayRealm.getRealm(display), new Runnable() {

            @Override
            public void run() {
                try {
                    SerialTerminal app = new SerialTerminal();
                    app.open();

                    while (display.getShells().length != 0) {
                        if (!display.readAndDispatch()) {
                            display.sleep();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        });

        display.dispose();
    }

}
