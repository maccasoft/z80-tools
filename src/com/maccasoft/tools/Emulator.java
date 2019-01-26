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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

    Combo cursorKeys;

    PipedOutputStream os;
    PipedInputStream is;

    Machine machine;
    Preferences preferences;

    public Emulator() {

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

        Menu menu = new Menu(shell, SWT.BAR);
        createFileMenu(menu);
        createEditMenu(menu);
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

        machine = new Machine() {

            @Override
            protected void run() {
                try {
                    String s = preferences.getRomImage1();
                    if (s != null && !"".equals(s)) {
                        if (s.toUpperCase().endsWith(".ASM")) {
                            byte[] rom = compile(new File(s));
                            if (rom == null) {
                                return;
                            }
                            machine.setRom(preferences.getRomAddress1(), rom);
                        }
                        else {
                            machine.setRom(preferences.getRomAddress1(), new File(s));
                        }
                    }
                    s = preferences.getRomImage2();
                    if (s != null && !"".equals(s)) {
                        if (s.toUpperCase().endsWith(".ASM")) {
                            byte[] rom = compile(new File(s));
                            if (rom == null) {
                                return;
                            }
                            machine.setRom(preferences.getRomAddress2(), rom);
                        }
                        else {
                            machine.setRom(preferences.getRomAddress2(), new File(s));
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
                        try {
                            if (is.available() > 0) {
                                return 0x04 + 0x01;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return 0x04; // Always return TX buffer empty
                    case SIOA_D:
                        try {
                            if (is.available() > 0) {
                                return is.read();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }
                return super.inPort(port);
            }

            @Override
            public void outPort(int port, int value) {
                switch (port & 0xFF) {
                    case SIOA_D:
                        display.syncExec(new Runnable() {

                            @Override
                            public void run() {
                                term.write(value);
                            }
                        });
                        break;
                    case SIOB_D:
                        break;
                }
                super.outPort(port, value);
            }

        };

        String s = preferences.getCompactFlashImage();
        if (s != null && !"".equals(s)) {
            machine.setCompactFlash(new File(s));
        }

        machine.reset();
        machine.start();
    }

    void createFileMenu(Menu parent) {
        final Menu menu = new Menu(parent.getParent(), SWT.DROP_DOWN);

        MenuItem item = new MenuItem(parent, SWT.CASCADE);
        item.setText("&File");
        item.setMenu(menu);

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

    public Shell getShell() {
        return shell;
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
