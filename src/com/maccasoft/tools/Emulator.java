/*
 * Copyright (c) 2018-20 Marco Maccaferri and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.maccasoft.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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

import nl.grauw.glass.AssemblyException;
import nl.grauw.glass.Source;
import nl.grauw.glass.SourceBuilder;

public class Emulator {

    Display display;
    Shell shell;
    Composite container;
    Terminal term;

    Shell tms9918Shell;
    TMS9918Terminal tms9918term;

    Combo cursorKeys;

    PipedOutputStream os;
    PipedInputStream is;

    Machine machine;
    Preferences preferences;

    public Emulator() {

    }

    public void open() {
        preferences = Preferences.getInstance();

        machine = new Machine() {

            @Override
            protected void run() {
                try {
                    String s1 = preferences.getRomImage1();
                    String s2 = preferences.getRomImage2();

                    if ((s1 == null || "".equals(s1)) && (s2 == null || "".equals(s2))) {
                        InputStream is = Emulator.class.getResourceAsStream("ROM.BIN");
                        byte[] rom = new byte[is.available()];
                        is.read(rom);
                        is.close();
                        machine.setRom(0, rom);
                    }
                    else {
                        if (s1 != null && !"".equals(s1)) {
                            if (s1.toUpperCase().endsWith(".ASM")) {
                                byte[] rom = compile(new File(s1));
                                if (rom == null) {
                                    return;
                                }
                                machine.setRom(preferences.getRomAddress1(), rom);
                            }
                            else {
                                machine.setRom(preferences.getRomAddress1(), new File(s1));
                            }
                        }

                        if (s2 != null && !"".equals(s2)) {
                            if (s2.toUpperCase().endsWith(".ASM")) {
                                byte[] rom = compile(new File(s2));
                                if (rom == null) {
                                    return;
                                }
                                machine.setRom(preferences.getRomAddress2(), rom);
                            }
                            else {
                                machine.setRom(preferences.getRomAddress2(), new File(s2));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                super.run();
            }

            @Override
            public int inPort(int port) {
                switch (port & 0xFF) {
                    case SIOA_C:
                        int result = 0b00101100; // TX Buffer Empty, DCD and CTS
                        try {
                            if (is.available() > 0) {
                                result |= 0x01; // RX Char Available
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return result;
                    case SIOA_D:
                        try {
                            if (is.available() > 0) {
                                return is.read();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return 0x00;
                    case SIOB_C:
                        return 0b00101100; // TX Buffer Empty, DCD and CTS
                    case SIOB_D:
                        return 0x00;
                }
                return super.inPort(port);
            }

            @Override
            public void outPort(int port, int value) {
                switch (port & 0xFF) {
                    case SIOA_D:
                        term.write(value);
                        break;
                    case SIOB_D:
                        break;
                }
                super.outPort(port, value);
            }

            @Override
            protected void onTMS9918VSync() {
                if (tms9918term != null) {
                    tms9918term.redraw();
                }
                super.onTMS9918VSync();
            }

        };

        machine.tmsRam = preferences.getTms9918Ram();
        machine.tmsReg = preferences.getTms9918Register();

        String s = preferences.getCompactFlashImage();
        if (s != null && !"".equals(s)) {
            machine.setCompactFlash(new File(s));
        }

        createTerminalShell();

        if (preferences.isOpenTMS9918Window()) {
            tms9918Shell = createTMS9918Shell();

            Rectangle screen = display.getClientArea();

            Rectangle rect = new Rectangle(0, 0, shell.getSize().x + 5 + tms9918Shell.getSize().x, shell.getSize().y);
            rect.x = (screen.width - rect.width) / 2;
            if (rect.x < 0) {
                rect.x = 0;
            }
            rect.y = (screen.height - rect.height) / 2;
            if (rect.y < 0) {
                rect.height += rect.y * 2;
                rect.y = 0;
            }

            shell.setLocation(rect.x, rect.y);
            tms9918Shell.setLocation(rect.x + shell.getSize().x + 5, rect.y);

            tms9918Shell.open();
        }

        shell.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                if (tms9918Shell != null) {
                    tms9918Shell.dispose();
                }
                tms9918Shell = null;
            }
        });
        shell.open();

        machine.reset();
        machine.start();
    }

    protected Shell createTerminalShell() {
        display = Display.getDefault();

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

        Menu menu = new Menu(shell, SWT.BAR);
        createFileMenu(menu);
        createEditMenu(menu);
        createWindowMenu(menu);
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

        return shell;
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

    void createWindowMenu(Menu parent) {
        Menu menu = new Menu(parent.getParent(), SWT.DROP_DOWN);

        MenuItem item = new MenuItem(parent, SWT.CASCADE);
        item.setText("&Window");
        item.setMenu(menu);

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Open TMS9918 Window");
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                try {
                    handleOpenTMS9918Window();
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
                    while (is.available() >= 16) {
                        Thread.yield();
                    }
                    os.write(b);
                } catch (IOException e) {
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

        try {
            is = new PipedInputStream();
            os = new PipedOutputStream(is);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        container.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                if (machine != null) {
                    machine.stop();
                }
                try {
                    if (os != null) {
                        os.close();
                    }

                } catch (Exception e1) {
                    // Do nothing
                }
            }
        });

        return container;
    }

    void createBottomControls(Composite parent) {
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

        Button button = new Button(container, SWT.PUSH);
        button.setText("Reset");
        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    while (is.available() > 0) {
                        is.read();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                machine.reset();
                term.setFocus();
            }
        });

        layout.numColumns = container.getChildren().length;
    }

    public void setFocus() {
        term.setFocus();
    }

    byte[] compile(File file) {
        System.out.print("Compiling " + file.getName() + "...");

        try {
            final List<File> includePaths = new ArrayList<File>();
            if (file != null) {
                includePaths.add(file.getParentFile());
            }

            String[] includes = preferences.getIncludes();
            if (includes != null) {
                for (int i = 0; i < includes.length; i++) {
                    includePaths.add(new File(includes[i]));
                }
            }

            SourceBuilder builder = new SourceBuilder(includePaths) {

                @Override
                public Source parse(File sourceFile) {
                    System.out.print("\r\nCompiling " + sourceFile.getName() + "...");
                    return super.parse(sourceFile);
                }

            };

            Source source = builder.parse(new InputStreamReader(new FileInputStream(file)), file);
            source.register();
            source.expand();
            source.resolve();

            System.out.println();

            return new BinaryBuilder(source).build();

        } catch (AssemblyException ex) {
            StringBuilder sb = new StringBuilder();

            Iterator<AssemblyException.Context> iter = ex.contexts.iterator();
            if (iter.hasNext()) {
                AssemblyException.Context context = iter.next();
                sb.append(context.file.getName());
                sb.append(":");
                sb.append(context.line + 1);
                if (context.column != -1) {
                    sb.append(":");
                    sb.append(context.column);
                }
                sb.append(": error: ");
                sb.append(ex.getPlainMessage());
            }

            System.out.println();
            System.err.println(sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
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
                        try {
                            uploadPackedBinary(fileName[i].getName().toUpperCase(), is);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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

    String filterPath;

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
            "*",
            "*.COM;*.com"
        });
        dlg.setFilterIndex(0);
        dlg.setFilterPath(filterPath);

        if (dlg.open() == null) {
            return null;
        }

        String[] names = dlg.getFileNames();

        File[] files = new File[names.length];
        for (int i = 0; i < names.length; i++) {
            files[i] = new File(dlg.getFilterPath(), names[i]);
        }

        if (names.length != 0) {
            filterPath = dlg.getFilterPath();
        }

        return files;
    }

    public void uploadPackedBinary(String name, InputStream is) throws Exception {
        int data;
        int length = is.available();
        int checksum = 0;

        String cmd = preferences.getDownloadCommand();
        if (cmd != null && !cmd.equals("")) {
            cmd = cmd.replace("{0}", name.toUpperCase());
            writeString(cmd + "\r");
        }

        writeString("U0\r");
        writeString(":");

        for (int i = 0; i < length; i++) {
            data = is.read();
            writeString(String.format("%02X", data & 0xFF));

            checksum += data & 0xFF;
        }

        writeString(">");

        writeString(String.format("%02X", length & 0xFF));
        writeString(String.format("%02X", checksum & 0xFF));
    }

    void writeString(String s) throws IOException {
        while (is.available() >= 16) {
            Thread.yield();
        }
        os.write(s.getBytes());
    }

    public Shell getShell() {
        return shell;
    }

    private void handleOpenTMS9918Window() {
        if (tms9918term != null) {
            tms9918term.setFocus();
            return;
        }

        tms9918Shell = createTMS9918Shell();

        Rectangle bounds = shell.getBounds();
        tms9918Shell.setLocation(bounds.x + bounds.width + 5, bounds.y);
        tms9918Shell.open();

        shell.setFocus();
    }

    protected Shell createTMS9918Shell() {
        final Shell shell = new Shell(display);
        shell.setText("TMS9918");

        Image[] images = new Image[] {
            ImageRegistry.getImageFromResources("app128.png"),
            ImageRegistry.getImageFromResources("app64.png"),
            ImageRegistry.getImageFromResources("app48.png"),
            ImageRegistry.getImageFromResources("app32.png"),
            ImageRegistry.getImageFromResources("app16.png"),
        };
        shell.setImages(images);

        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = layout.marginHeight = 0;
        shell.setLayout(layout);

        Control control = createTMS9918Contents(shell);
        control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Point size = control.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        Rectangle rect = shell.computeTrim(0, 0, size.x * 2, size.y * 2);

        Rectangle screen = getShell().getBounds();

        shell.setLocation(screen.x + screen.width + 10, screen.y);
        shell.setSize(rect.width, rect.height);

        return shell;
    }

    protected Control createTMS9918Contents(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = layout.marginHeight = 0;
        container.setLayout(layout);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        tms9918term = new TMS9918Terminal(container) {

            @Override
            protected ImageData getImageData() {
                return machine.tms9918.getImageData();
            }

        };

        Rectangle rect = tms9918term.getBounds();
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = rect.width;
        gridData.heightHint = rect.height;
        tms9918term.setLayoutData(gridData);

        container.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                tms9918term = null;
            }
        });

        return container;
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
                    Emulator emulator = new Emulator();
                    emulator.open();

                    emulator.getShell().setFocus();

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
