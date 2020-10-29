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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import com.maccasoft.tools.internal.ImageRegistry;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;
import jssc.SerialPortTimeoutException;

public class SerialTerminal {

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
    Shell shell;
    Composite container;
    Terminal term;

    Combo cursorKeys;
    Combo comPort;
    Combo baudRate;

    ProgressIndicator progressBar;

    String port;
    int baud;
    boolean flowControl;
    long byteDelay;
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
                        term.write(rx);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    final IProgressMonitor progressMonitor = new IProgressMonitor() {

        @Override
        public void beginTask(String name, int totalWork) {
            final boolean animated = (totalWork == UNKNOWN || totalWork == 0);
            display.syncExec(new Runnable() {

                @Override
                public void run() {
                    if (progressBar == null || progressBar.isDisposed()) {
                        return;
                    }

                    if (!progressBar.getVisible()) {
                        progressBar.setVisible(true);
                        container.layout();
                    }
                    if (!animated) {
                        progressBar.beginTask(totalWork);
                    }
                    else {
                        progressBar.beginAnimatedTask();
                    }
                }
            });
        }

        @Override
        public void done() {
            display.syncExec(new Runnable() {

                @Override
                public void run() {
                    if (progressBar == null || progressBar.isDisposed()) {
                        return;
                    }
                    progressBar.sendRemainingWork();
                    progressBar.done();
                }
            });
        }

        @Override
        public void internalWorked(double work) {
            display.syncExec(new Runnable() {

                @Override
                public void run() {
                    if (progressBar == null || progressBar.isDisposed()) {
                        return;
                    }
                    progressBar.worked(work);
                }
            });
        }

        @Override
        public boolean isCanceled() {
            return false;
        }

        @Override
        public void setCanceled(boolean value) {

        }

        @Override
        public void setTaskName(String name) {

        }

        @Override
        public void subTask(String name) {

        }

        @Override
        public void worked(int work) {
            internalWorked(work);
        }

    };

    public SerialTerminal() {

    }

    public void open() {
        display = Display.getDefault();
        preferences = Preferences.getInstance();

        shell = new Shell(display);
        shell.setText(Application.APP_TITLE);
        shell.setData(this);

        Image[] images = new Image[] {
            ImageRegistry.getImageFromResources("app128.png"),
            ImageRegistry.getImageFromResources("app64.png"),
            ImageRegistry.getImageFromResources("app48.png"),
            ImageRegistry.getImageFromResources("app32.png"),
            ImageRegistry.getImageFromResources("app16.png"),
        };
        shell.setImages(images);

        port = preferences.getSerialPort();
        baud = preferences.getSerialBaud();
        flowControl = preferences.isSerialFlowControl();

        Menu menu = new Menu(shell, SWT.BAR);
        createFileMenu(menu);
        createEditMenu(menu);
        createOptionsMenu(menu);
        createHelpMenu(menu);
        shell.setMenuBar(menu);

        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = layout.marginHeight = 0;
        shell.setLayout(layout);

        Control control = createContents(shell);
        control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Rectangle screen = display.getClientArea();

        Point size = control.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);

        Rectangle rect = shell.computeTrim(0, 0, size.x, size.y);
        rect.x = (screen.width - rect.width) / 2;
        rect.y = (screen.height - rect.height) / 2;
        if (rect.y < 0) {
            rect.height += rect.y * 2;
            rect.y = 0;
        }

        shell.setLocation(rect.x, rect.y);
        shell.setSize(rect.width, rect.height);

        shell.open();
    }

    void createFileMenu(Menu parent) {
        final Menu menu = new Menu(parent.getParent(), SWT.DROP_DOWN);

        MenuItem item = new MenuItem(parent, SWT.CASCADE);
        item.setText("&File");
        item.setMenu(menu);

        item = new MenuItem(menu, SWT.CASCADE);
        item.setText("Packed CP/M Binary Upload...");
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                try {
                    handleUploadPackedBinary();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        new MenuItem(menu, SWT.SEPARATOR);

        item = new MenuItem(menu, SWT.CASCADE);
        item.setText("XModem Upload...");
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                try {
                    handleUploadXModem();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        item = new MenuItem(menu, SWT.CASCADE);
        item.setText("XModem Download...");
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                try {
                    handleDownloadXModem();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        new MenuItem(menu, SWT.SEPARATOR);

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Close");
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                shell.dispose();
            }
        });
    }

    void createEditMenu(Menu parent) {
        Menu menu = new Menu(parent.getParent(), SWT.DROP_DOWN);

        MenuItem item = new MenuItem(parent, SWT.CASCADE);
        item.setText("&Edit");
        item.setMenu(menu);

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Paste\tShift+Ins");
        item.setAccelerator(SWT.MOD2 + SWT.INSERT);
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                try {
                    term.pasteFromClipboard();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    void createOptionsMenu(Menu parent) {
        Menu menu = new Menu(parent.getParent(), SWT.DROP_DOWN);

        MenuItem item = new MenuItem(parent, SWT.CASCADE);
        item.setText("&Options");
        item.setMenu(menu);

        item = new MenuItem(menu, SWT.CHECK);
        item.setText("Hardware flow control");
        item.setSelection(flowControl);
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                try {
                    flowControl = ((MenuItem) e.widget).getSelection();
                    if (serialPort != null && serialPort.isOpened()) {
                        if (flowControl) {
                            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
                        }
                        else {
                            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                        }
                    }
                    preferences.setSerialFlowControl(flowControl);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    void createHelpMenu(Menu parent) {
        Menu menu = new Menu(parent.getParent(), SWT.DROP_DOWN);

        MenuItem item = new MenuItem(parent, SWT.CASCADE);
        item.setText("&Help");
        item.setMenu(menu);

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("About " + Application.APP_TITLE);
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                AboutDialog dlg = new AboutDialog(shell);
                dlg.open();
            }
        });
    }

    protected Control createContents(Composite parent) {
        container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = layout.marginHeight = 0;
        container.setLayout(layout);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        term = new Terminal(container) {

            @Override
            protected void writeByte(byte b) {
                try {
                    if (!serialPort.isOpened()) {
                        return;
                    }
                    serialPort.writeByte(b);
                    if (!flowControl) {
                        long ns = System.nanoTime();
                        do {
                            Thread.yield();
                        } while ((System.nanoTime() - ns) <= byteDelay);
                    }
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
                    if (serialPort != null && serialPort.isOpened()) {
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

        progressBar = new ProgressIndicator(container, SWT.HORIZONTAL);
        GridData layoutData = new GridData(GridData.GRAB_VERTICAL);
        layoutData.widthHint = 128;
        progressBar.setLayoutData(layoutData);

        label = new Label(container, SWT.NONE);
        label.setText("Port");

        comPort = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        comPort.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        comPort.setItems(SerialPortList.getPortNames());
        index = port != null ? comPort.indexOf(port) : -1;
        if (index == -1 && comPort.getItemCount() != 0) {
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
        if (serialPort != null && !serialPort.getPortName().equals(port)) {
            try {
                if (serialPort.isOpened()) {
                    serialPort.closePort();
                }
            } catch (Exception e) {
                // Do nothing
            } finally {
                serialPort = null;
            }
        }

        if (serialPort == null) {
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
                true,
                true);
            if (flowControl) {
                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
            }
            else {
                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        byteDelay = (1000000000L / (baud / 10)) * 2;

        shell.setText("Serial Terminal on " + port);
    }

    public void setFocus() {
        term.setFocus();
    }

    public SerialPort getSerialPort() {
        return serialPort;
    }

    public void dispose() {
        shell.dispose();
    }

    private void handleUploadPackedBinary() {
        String s = preferences.getDownloadCommand();
        final File[] fileName = getFileToOpen("Packed-Binary Upload", s != null && !s.equals(""));
        if (fileName == null || fileName.length == 0) {
            return;
        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    for (int i = 0; i < fileName.length; i++) {
                        InputStream is = new FileInputStream(fileName[i]);
                        progressMonitor.beginTask("Upload", (is.available() + 127) / 128);
                        try {
                            uploadPackedBinary(fileName[i].getName().toUpperCase(), is, progressMonitor);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        progressMonitor.done();
                        is.close();

                        if ((i + 1) < fileName.length) {
                            try {
                                Thread.sleep(2000);
                            } catch (Exception e) {
                                // Do nothing
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }

    private void handleUploadXModem() {
        String s = preferences.getXmodemCommand();
        final File[] fileName = getFileToOpen("XModem Upload", s != null && !s.equals(""));
        if (fileName == null || fileName.length == 0) {
            return;
        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    for (int i = 0; i < fileName.length; i++) {
                        InputStream is = new FileInputStream(fileName[i]);
                        progressMonitor.beginTask("Upload", (is.available() + 127) / 128);
                        try {
                            uploadXModem(fileName[i].getName().toUpperCase(), is, progressMonitor);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        progressMonitor.done();
                        is.close();

                        if ((i + 1) < fileName.length) {
                            try {
                                Thread.sleep(2000);
                            } catch (Exception e) {
                                // Do nothing
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }

    private File[] getFileToOpen(String text, boolean multi) {
        int style = SWT.OPEN;
        if (multi) {
            style |= SWT.MULTI;
        }

        FileDialog dlg = new FileDialog(shell, style);
        dlg.setText(text);
        dlg.setFilterNames(new String[] {
            "All Files",
            "CP/M Programs"
        });
        dlg.setFilterExtensions(new String[] {
            "*.*",
            "*.COM;*.com"
        });
        dlg.setFilterIndex(0);

        if (dlg.open() == null) {
            return null;
        }

        String[] names = dlg.getFileNames();

        File[] files = new File[names.length];
        for (int i = 0; i < names.length; i++) {
            files[i] = new File(dlg.getFilterPath(), names[i]);
        }

        return files;
    }

    public void uploadPackedBinary(String name, byte[] data, IProgressMonitor monitor) throws Exception {
        uploadPackedBinary(name, new ByteArrayInputStream(data), monitor);
    }

    public void uploadPackedBinary(String name, InputStream is, IProgressMonitor monitor) throws Exception {
        int data, block;
        int length = is.available();
        int checksum = 0;

        if (flowControl) {
            String cmd = preferences.getDownloadCommand();
            if (cmd != null && !cmd.equals("")) {
                cmd = cmd.replace("{0}", name.toUpperCase());
                serialPort.writeString(cmd + "\r");

                String s;
                do {
                    s = readString();
                } while (s.length() != 0 && !s.contains(cmd));
            }

            serialPort.writeString("U0\r");
            serialPort.writeString(":");

            block = 0;
            for (int i = 0; i < length; i++) {
                data = is.read();
                serialPort.writeString(String.format("%02X", data & 0xFF));

                checksum += data & 0xFF;

                block++;
                if (block == 128) {
                    if (monitor != null) {
                        monitor.worked(1);
                    }
                    block = 0;
                }

                if (monitor != null && monitor.isCanceled()) {
                    return;
                }
            }

            serialPort.writeString(">");
            if (block != 128) {
                if (monitor != null) {
                    monitor.worked(1);
                }
            }

            serialPort.writeString(String.format("%02X", length & 0xFF));
            serialPort.writeString(String.format("%02X", checksum & 0xFF));
        }
        else {
            try {
                serialPort.removeEventListener();
            } catch (SerialPortException e) {
                // Do nothing
            }

            try {
                String cmd = preferences.getDownloadCommand();
                if (cmd != null && !cmd.equals("")) {
                    cmd = cmd.replace("{0}", name.toUpperCase());
                    writeString(cmd + "\r");

                    String s;
                    do {
                        s = readString();
                    } while (s.length() != 0 && !s.contains(cmd));
                }

                writeString("U0\r");
                waitFlush(100);

                writeString(":");
                waitFlush(1000);

                block = 0;
                for (int i = 0; i < length; i++) {
                    data = is.read();
                    writeString(String.format("%02X", data & 0xFF));

                    checksum += data & 0xFF;

                    block++;
                    if (block == 128) {
                        //System.out.println("block");
                        waitDot();
                        if (monitor != null) {
                            monitor.worked(1);
                        }
                        block = 0;
                    }

                    if (monitor != null && monitor.isCanceled()) {
                        return;
                    }
                }

                writeString(">");
                if (block != 128) {
                    //System.out.println("block");
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

            writeString(String.format("%02X", length & 0xFF));
            writeString(String.format("%02X", checksum & 0xFF));
        }
    }

    String readString() throws SerialPortException, SerialPortTimeoutException {
        String s = "";

        while (true) {
            byte[] b = serialPort.readBytes(1, 5000);
            term.write(b[0]);
            if (b[0] == '\r') {
                try {
                    b = serialPort.readBytes(1, 200);
                    term.write(b[0]);
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

    void waitDot() throws SerialPortException, SerialPortTimeoutException {
        byte[] b;

        do {
            b = serialPort.readBytes(1, 5000);
            term.write(b);
        } while (b[0] != '.');
    }

    public void uploadXModem(String name, byte[] data, IProgressMonitor monitor) throws Exception {
        uploadXModem(name, new ByteArrayInputStream(data), monitor);
    }

    public void uploadXModem(String name, InputStream is, IProgressMonitor monitor) throws Exception {
        int i;
        String s;
        byte[] data = new byte[128];

        try {
            serialPort.removeEventListener();
        } catch (SerialPortException e) {
            // Do nothing
        }

        try {
            String cmd = preferences.getXmodemCommand();
            if (cmd != null && !cmd.equals("")) {
                cmd = cmd.replace("{0}", name.toUpperCase());
                writeString(cmd + "\r");

                s = "";
                while (true) {
                    byte[] b = serialPort.readBytes(1, 15000);
                    term.write(b[0]);
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
            }

            boolean doCrc = false;
            int packet = 1;
            int errors = 0;

            Arrays.fill(data, (byte) 0);
            if ((i = is.read(data)) < data.length) {
                data[i] = 0x1A;
            }

            while (true) {
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
                        packet++;
                        if (monitor != null) {
                            monitor.worked(1);
                        }
                        errors = 0;

                        Arrays.fill(data, (byte) 0);
                        if ((i = is.read(data)) <= 0) {
                            serialPort.writeByte(EOT);
                            b = serialPort.readBytes(1, 5000);
                            if (b[0] == NAK) {
                                serialPort.writeByte(EOT);
                            }
                            b = serialPort.readBytes(1, 5000);
                            if (b[0] == NAK) {
                                serialPort.writeByte(EOT);
                            }
                            term.write('\r');
                            break;
                        }
                        if (i < data.length) {
                            data[i] = 0x1A;
                        }
                    }

                    if (b[0] == NAK) {
                        errors++;
                        if (errors >= 10) {
                            return;
                        }
                    }

                    if (b[0] == ACK || b[0] == NAK) {
                        if (flowControl) {
                            serialPort.writeByte(SOH);
                            serialPort.writeByte((byte) packet);
                            serialPort.writeByte((byte) (packet ^ 0xFF));

                            int checksum = 0, crc = 0;
                            for (int x = 0; x < data.length; x++) {
                                serialPort.writeByte(data[x]);
                                checksum += data[x] & 0xFF;
                                crc = updateCrc(crc, data[x] & 0xFF);
                            }

                            if (doCrc) {
                                serialPort.writeByte((byte) ((crc >> 8) & 0xFF));
                                serialPort.writeByte((byte) (crc & 0xFF));
                            }
                            else {
                                serialPort.writeByte((byte) checksum);
                            }
                        }
                        else {
                            writeByte(SOH);
                            writeByte((byte) packet);
                            writeByte((byte) (packet ^ 0xFF));

                            int checksum = 0, crc = 0;
                            for (int x = 0; x < data.length; x++) {
                                writeByte(data[x]);
                                checksum += data[x] & 0xFF;
                                crc = updateCrc(crc, data[x] & 0xFF);
                            }

                            if (doCrc) {
                                writeByte((byte) ((crc >> 8) & 0xFF));
                                writeByte((byte) (crc & 0xFF));
                            }
                            else {
                                writeByte((byte) checksum);
                            }
                        }
                    }
                    else {
                        term.write(b);
                    }

                    if (monitor != null && monitor.isCanceled()) {
                        writeByte(CAN);
                        writeByte(CAN);
                        writeByte(CAN);
                        return;
                    }
                } catch (SerialPortTimeoutException e) {
                    errors++;
                    if (errors >= 10) {
                        return;
                    }
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

    private void handleDownloadXModem() {
        FileDialog dlg = new FileDialog(shell, SWT.SAVE);
        dlg.setText("XModem Download");
        dlg.setFilterNames(new String[] {
            "All Files",
            "CP/M Programs"
        });
        dlg.setFilterExtensions(new String[] {
            "*.*",
            "*.COM;*.com"
        });
        dlg.setFilterIndex(0);
        dlg.setOverwrite(true);

        String fileName = dlg.open();
        if (fileName == null) {
            return;
        }

        final File file = new File(fileName);

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    OutputStream os = new FileOutputStream(file);
                    progressMonitor.beginTask("Upload", IProgressMonitor.UNKNOWN);
                    try {
                        downloadXModem(file.getName().toUpperCase(), os, progressMonitor);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    progressMonitor.done();
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }

    public void downloadXModem(String name, OutputStream os, IProgressMonitor monitor) throws Exception {
        byte[] b, data, c;

        try {
            serialPort.removeEventListener();
        } catch (SerialPortException e) {
            // Do nothing
        }

        try {
            boolean doCrc = true;
            int packet = 1;
            int errors = 0;

            serialPort.writeByte(doCrc ? C : NAK);

            while (true) {
                try {
                    b = serialPort.readBytes(1, 15000);

                    if (b[0] == SOH) {
                        b = serialPort.readBytes(2, 15000);
                        data = serialPort.readBytes(128, 15000);
                        c = serialPort.readBytes(doCrc ? 2 : 1, 15000);

                        if (b[0] != (byte) packet || (b[0] ^ b[1]) != (byte) 0xFF) {
                            serialPort.writeByte(NAK);
                            errors++;
                            if (errors >= 10) {
                                return;
                            }
                            continue;
                        }

                        int checksum = 0, crc = 0;
                        for (int x = 0; x < data.length; x++) {
                            checksum += data[x] & 0xFF;
                            crc = updateCrc(crc, data[x] & 0xFF);
                        }

                        if (doCrc) {
                            if (c[0] != (byte) (crc >> 8) || c[1] != (byte) (crc & 0xFF)) {
                                serialPort.writeByte(NAK);
                                errors++;
                                if (errors >= 10) {
                                    return;
                                }
                                continue;
                            }
                        }
                        else {
                            if (c[0] != (byte) (checksum & 0xFF)) {
                                serialPort.writeByte(NAK);
                                errors++;
                                if (errors >= 10) {
                                    return;
                                }
                                continue;
                            }
                        }

                        os.write(data);

                        serialPort.writeByte(ACK);
                        packet++;
                        errors = 0;
                    }
                    else if (b[0] == EOT) {
                        serialPort.writeByte(ACK);
                        break;
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
                    if (doCrc && errors >= 5) {
                        doCrc = false;
                        errors = 0;
                    }
                    serialPort.writeByte(doCrc ? C : NAK);
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

    public Shell getShell() {
        return shell;
    }

    void writeString(String s) throws SerialPortException {
        for (int i = 0; i < s.length(); i++) {
            serialPort.writeInt(s.charAt(i));

            long ns = System.nanoTime();
            do {
                Thread.yield();
            } while ((System.nanoTime() - ns) <= byteDelay);
        }
    }

    void waitFlush(int ms) throws SerialPortException {
        byte[] b;

        while (true) {
            try {
                b = serialPort.readBytes(1, ms);
                term.write(b);
            } catch (SerialPortTimeoutException e1) {
                break;
            }
        }
    }

    void waitCRLF(int ms) throws SerialPortException {
        byte[] b;

        do {
            try {
                b = serialPort.readBytes(1, ms);
                term.write(b);
            } catch (SerialPortTimeoutException e1) {
                break;
            }
        } while (b[0] != 0x0D);
        do {
            try {
                b = serialPort.readBytes(1, ms);
                term.write(b);
            } catch (SerialPortTimeoutException e1) {
                break;
            }
        } while (b[0] != 0x0A);
    }

    void writeByte(byte b) throws SerialPortException {
        serialPort.writeByte(b);

        long ns = System.nanoTime();
        do {
            Thread.yield();
        } while ((System.nanoTime() - ns) <= byteDelay);
    }

    static {
        System.setProperty("SWT_GTK3", "0");
    }

    public static void main(String[] args) {
        final Display display = new Display();

        Realm.runWithDefault(DisplayRealm.getRealm(display), new Runnable() {

            @Override
            public void run() {
                Preferences preferences = Preferences.getInstance();
                try {
                    SerialTerminal app = new SerialTerminal();
                    app.open();

                    while (display.getShells().length != 0) {
                        if (!display.readAndDispatch()) {
                            display.sleep();
                        }
                    }

                    preferences.save();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        });

        display.dispose();
    }

}
