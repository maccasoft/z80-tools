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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.maccasoft.tools.internal.ImageRegistry;

import jssc.SerialPort;
import nl.grauw.glass.AssemblyException;
import nl.grauw.glass.Line;
import nl.grauw.glass.Source;
import nl.grauw.glass.SourceBuilder;

public class Application {

    public static final String APP_TITLE = "Z80 Tools";
    public static final String APP_VERSION = "0.0.0";

    Display display;
    Shell shell;

    SashForm sashForm1;
    FileBrowser browser;
    SashForm sashForm2;
    CTabFolder tabFolder;
    Console console;
    StatusLine statusLine;

    SerialTerminal terminal;

    Preferences preferences;

    WritableValue tabFolderSelection;

    final CaretListener caretListener = new CaretListener() {

        @Override
        public void caretMoved(CaretEvent event) {
            StyledText text = (StyledText) event.widget;
            int offset = text.getCaretOffset();
            int y = text.getLineAtOffset(offset);
            int x = offset - text.getOffsetAtLine(y);
            statusLine.setCaretPosition(String.format("%d : %d ", y + 1, x + 1));
        }
    };

    public Application() {
        preferences = Preferences.getInstance();
    }

    public void open() {
        display = Display.getDefault();

        shell = new Shell(display);
        shell.setText(APP_TITLE);

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
        createToolsMenu(menu);
        createHelpMenu(menu);
        shell.setMenuBar(menu);

        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = layout.marginHeight = 5;
        shell.setLayout(layout);

        Control control = createToolbar(shell);
        control.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        control = createContents(shell);
        control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        statusLine = new StatusLine(shell);
        statusLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Rectangle screen = display.getClientArea();

        Rectangle rect = new Rectangle(0, 0, (int) ((float) screen.width / (float) screen.height * 800), 900);
        rect.x = (screen.width - rect.width) / 2;
        rect.y = (screen.height - rect.height) / 2;
        if (rect.y < 0) {
            rect.height += rect.y * 2;
            rect.y = 0;
        }

        shell.setLocation(rect.x, rect.y);
        shell.setSize(rect.width, rect.height);
        if (rect.width > screen.width || rect.height > screen.height) {
            shell.setMaximized(true);
        }

        shell.open();

        shell.addListener(SWT.Close, new Listener() {

            @Override
            public void handleEvent(Event event) {
                event.doit = handleUnsavedContent();
            }
        });
        shell.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                if (terminal != null) {
                    terminal.dispose();
                    terminal = null;
                }
                try {
                    preferences.save();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                ImageRegistry.dispose();
            }
        });
    }

    void createFileMenu(Menu parent) {
        final Menu menu = new Menu(parent.getParent(), SWT.DROP_DOWN);

        MenuItem item = new MenuItem(parent, SWT.CASCADE);
        item.setText("&File");
        item.setMenu(menu);

        item = new MenuItem(menu, SWT.CASCADE);
        item.setText("New\tCtrl+N");
        item.setAccelerator(SWT.MOD1 + 'N');
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                try {
                    handleFileNew();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Open...\tCtrl+O");
        item.setAccelerator(SWT.MOD1 + 'O');
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                try {
                    handleFileOpen();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        new MenuItem(menu, SWT.SEPARATOR);

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Close");
        item.setAccelerator(SWT.MOD1 + 'O');
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                try {
                    handleFileClose();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Close All");
        item.setAccelerator(SWT.MOD1 + 'O');
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                try {
                    handleFileCloseAll();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        new MenuItem(menu, SWT.SEPARATOR);

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Save\tCtrl+S");
        item.setAccelerator(SWT.MOD1 + 'S');
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                try {
                    handleFileSave();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Save As...");
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                try {
                    handleFileSaveAs();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Save All\tCtrl+Shift+S");
        item.setAccelerator(SWT.MOD1 + SWT.MOD2 + 'S');
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                try {
                    handleFileSaveAll();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        new MenuItem(menu, SWT.SEPARATOR);

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Preferences");
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {

            }
        });

        new MenuItem(menu, SWT.SEPARATOR);

        final int lruItemIndex = menu.getItemCount();
        menu.addMenuListener(new MenuListener() {

            MenuItem[] lruItems;

            @Override
            public void menuShown(MenuEvent e) {
                if (lruItems != null) {
                    for (int i = 0; i < lruItems.length; i++) {
                        lruItems[i].dispose();
                    }
                }
                lruItems = populateLruFiles(menu, lruItemIndex);
            }

            @Override
            public void menuHidden(MenuEvent e) {

            }
        });

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Exit");
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                shell.dispose();
            }
        });
    }

    MenuItem[] populateLruFiles(Menu menu, int itemIndex) {
        int index = 0;
        List<MenuItem> list = new ArrayList<MenuItem>();

        Iterator<String> iter = preferences.getLru().iterator();
        while (iter.hasNext() && index < 4) {
            final File fileToOpen = new File(iter.next());
            MenuItem item = new MenuItem(menu, SWT.PUSH, itemIndex++);
            item.setText(String.format("%d %s", index + 1, fileToOpen.getName()));
            item.addListener(SWT.Selection, new Listener() {

                @Override
                public void handleEvent(Event e) {
                    try {
                        if (!fileToOpen.exists()) {
                            preferences.removeLru(fileToOpen);
                            return;
                        }
                        SourceEditorTab tab = openSourceTab(fileToOpen);
                        tabFolder.setSelection(tab.getTabItem());
                        tab.setFocus();
                        preferences.addLru(fileToOpen);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            });
            list.add(item);
            index++;
        }

        if (index > 0) {
            list.add(new MenuItem(menu, SWT.SEPARATOR, itemIndex));
        }

        return list.toArray(new MenuItem[list.size()]);
    }

    void createEditMenu(Menu parent) {
        Menu menu = new Menu(parent.getParent(), SWT.DROP_DOWN);

        MenuItem item = new MenuItem(parent, SWT.CASCADE);
        item.setText("&Edit");
        item.setMenu(menu);

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Undo\tCtrl+Z");
        item.setAccelerator(SWT.MOD1 + 'Z');
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                try {
                    CTabItem tabItem = tabFolder.getSelection();
                    if (tabItem == null) {
                        return;
                    }
                    ((SourceEditorTab) tabItem.getData()).getEditor().undo();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Redo\tCtrl+Shift+Z");
        item.setAccelerator(SWT.MOD1 + SWT.MOD2 + 'Z');
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                try {
                    CTabItem tabItem = tabFolder.getSelection();
                    if (tabItem == null) {
                        return;
                    }
                    ((SourceEditorTab) tabItem.getData()).getEditor().redo();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        new MenuItem(menu, SWT.SEPARATOR);

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Cut\tCtrl+X");
        item.setAccelerator(SWT.MOD1 + 'X');
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                try {
                    CTabItem tabItem = tabFolder.getSelection();
                    if (tabItem == null) {
                        return;
                    }
                    ((SourceEditorTab) tabItem.getData()).getEditor().cut();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Copy\tCtrl+C");
        item.setAccelerator(SWT.MOD1 + 'C');
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                try {
                    CTabItem tabItem = tabFolder.getSelection();
                    if (tabItem == null) {
                        return;
                    }
                    ((SourceEditorTab) tabItem.getData()).getEditor().copy();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Paste\tCtrl+V");
        item.setAccelerator(SWT.MOD1 + 'V');
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                try {
                    CTabItem tabItem = tabFolder.getSelection();
                    if (tabItem == null) {
                        return;
                    }
                    ((SourceEditorTab) tabItem.getData()).getEditor().paste();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        new MenuItem(menu, SWT.SEPARATOR);

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Select All\tCtrl+A");
        item.setAccelerator(SWT.MOD1 + 'A');
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                try {
                    CTabItem tabItem = tabFolder.getSelection();
                    if (tabItem == null) {
                        return;
                    }
                    ((SourceEditorTab) tabItem.getData()).getEditor().selectAll();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        new MenuItem(menu, SWT.SEPARATOR);

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Next Source\tCtrl+Tab");
        item.setAccelerator(SWT.MOD1 + SWT.TAB);
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                try {
                    handleNextTab();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Previous Source\tCtrl+Shift+Tab");
        item.setAccelerator(SWT.MOD1 + SWT.MOD2 + SWT.TAB);
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                try {
                    handlePreviousTab();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    void createToolsMenu(Menu parent) {
        Menu menu = new Menu(parent.getParent(), SWT.DROP_DOWN);

        MenuItem item = new MenuItem(parent, SWT.CASCADE);
        item.setText("&Tools");
        item.setMenu(menu);

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Verify / Compile\tCtrl+R");
        item.setAccelerator(SWT.MOD1 + 'R');
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                try {
                    handleCompile();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Upload Intel HEX");
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                try {
                    handleCompileAndUploadIntelHex();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        new MenuItem(menu, SWT.SEPARATOR);

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Serial Terminal\tCtrl+Shift+T");
        item.setAccelerator(SWT.MOD1 + SWT.MOD2 + 'T');
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                try {
                    handleOpenTerminal();
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
        item.setText("About " + APP_TITLE);
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                AboutDialog dlg = new AboutDialog(shell);
                dlg.open();
            }
        });
    }

    ToolBar createToolbar(Composite parent) {
        ToolBar toolBar = new ToolBar(parent, SWT.FLAT);

        ToolItem toolItem = new ToolItem(toolBar, SWT.PUSH);
        toolItem.setImage(ImageRegistry.getImageFromResources("application_verify.png"));
        toolItem.setToolTipText("Verify / Compile");
        toolItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    handleCompile();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        toolItem = new ToolItem(toolBar, SWT.PUSH);
        toolItem.setImage(ImageRegistry.getImageFromResources("application_go.png"));
        toolItem.setToolTipText("Upload Intel HEX");
        toolItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    handleCompileAndUploadIntelHex();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        new ToolItem(toolBar, SWT.SEPARATOR);

        toolItem = new ToolItem(toolBar, SWT.PUSH);
        toolItem.setImage(ImageRegistry.getImageFromResources("application_xp_terminal.png"));
        toolItem.setToolTipText("Serial Terminal");
        toolItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    handleOpenTerminal();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        new ToolItem(toolBar, SWT.SEPARATOR);

        toolItem = new ToolItem(toolBar, SWT.PUSH);
        toolItem.setImage(ImageRegistry.getImageFromResources("application_add.png"));
        toolItem.setToolTipText("New");
        toolItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    handleFileNew();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        toolItem = new ToolItem(toolBar, SWT.PUSH);
        toolItem.setImage(ImageRegistry.getImageFromResources("application_get.png"));
        toolItem.setToolTipText("Open");
        toolItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    handleFileOpen();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        toolItem = new ToolItem(toolBar, SWT.PUSH);
        toolItem.setImage(ImageRegistry.getImageFromResources("application_put.png"));
        toolItem.setToolTipText("Save");
        toolItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    handleFileSave();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        return toolBar;
    }

    protected Control createContents(Composite parent) {
        GC gc = new GC(parent);
        FontMetrics fontMetrics = gc.getFontMetrics();
        gc.dispose();

        sashForm1 = new SashForm(parent, SWT.HORIZONTAL);
        sashForm1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        browser = new FileBrowser(sashForm1);
        browser.setRoots(new File[] {
            new File(System.getProperty("user.home"))
        });

        sashForm2 = new SashForm(sashForm1, SWT.VERTICAL);
        sashForm2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        tabFolder = new CTabFolder(sashForm2, SWT.BORDER) {

            @Override
            public void setSelection(int index) {
                super.setSelection(index);
                CTabItem tabItem = tabFolder.getSelection();
                tabFolderSelection.setValue(tabItem != null ? tabItem.getData() : null);
            }

        };
        tabFolder.setTabHeight((int) (fontMetrics.getHeight() * 1.5));
        tabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {

            @Override
            public void close(CTabFolderEvent event) {
                SourceEditorTab tab = (SourceEditorTab) event.item.getData();
                event.doit = canCloseSourceTab(tab);
            }
        });

        tabFolderSelection = new WritableValue();
        tabFolderSelection.addValueChangeListener(new IValueChangeListener() {

            @Override
            public void handleValueChange(ValueChangeEvent event) {
                SourceEditorTab tab = (SourceEditorTab) event.diff.getOldValue();
                if (tab != null) {
                    tab.getEditor().removeCaretListener(caretListener);
                }
                tab = (SourceEditorTab) event.diff.getNewValue();
                if (tab != null) {
                    StyledText text = tab.getEditor().getStyledText();
                    int offset = text.getCaretOffset();
                    int y = text.getLineAtOffset(offset);
                    int x = offset - text.getOffsetAtLine(y);
                    statusLine.setCaretPosition(String.format("%d : %d ", y + 1, x + 1));
                    tab.getEditor().addCaretListener(caretListener);
                }
                else {
                    statusLine.setCaretPosition("");
                }
            }
        });

        tabFolder.addTraverseListener(new TraverseListener() {

            @Override
            public void keyTraversed(TraverseEvent e) {
                if (e.character == SWT.TAB) {
                    if ((e.stateMask & SWT.MODIFIER_MASK) == SWT.MOD1) {
                        handleNextTab();
                        e.doit = false;
                    }
                    else if ((e.stateMask & SWT.MODIFIER_MASK) == (SWT.MOD1 | SWT.MOD2)) {
                        handlePreviousTab();
                        e.doit = false;
                    }
                }
            }
        });

        console = new Console(sashForm2);
        console.getStyledText().addListener(SWT.MouseDown, new Listener() {

            @Override
            public void handleEvent(Event event) {
                try {
                    StyledText control = (StyledText) event.widget;
                    int offset = control.getOffsetAtLocation(new Point(event.x, event.y));
                    String line = control.getLine(control.getLineAtOffset(offset));
                    if (line.toLowerCase().contains(" error : ")) {
                        int s = 0;
                        int e = line.indexOf(')');
                        if (e != -1) {
                            line = line.substring(s, e + 1);

                            s = 0;
                            e = line.indexOf('(');
                            String name = line.substring(s, e);
                            s = e + 1;
                            if ((e = line.indexOf(':', s)) == -1) {
                                e = line.indexOf(',', s);
                            }
                            int row = Integer.parseInt(line.substring(s, e)) - 1;
                            if (row < 0) {
                                row = 0;
                            }
                            s = e + 1;
                            e = line.indexOf(')', s);
                            int column = Integer.parseInt(line.substring(s, e)) - 1;
                            if (column < 0) {
                                column = 0;
                            }
                            switchToEditor(name, row, column);
                        }
                    }
                    else if (line.toLowerCase().contains(" error: ") || line.toLowerCase().contains(" warning: ")) {
                        int e = line.indexOf(':');
                        String name = line.substring(0, e);
                        int s = e + 1;
                        e = line.indexOf(':', s);
                        int row = Integer.parseInt(line.substring(s, e)) - 1;
                        s = e + 1;
                        int column = 0;
                        if (line.charAt(s) != ' ') {
                            e = line.indexOf(':', s);
                            column = Integer.parseInt(line.substring(s, e)) - 1;
                        }
                        switchToEditor(name, row, column);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            void switchToEditor(String name, int line, int column) {
                CTabItem[] tabItem = tabFolder.getItems();
                for (int i = 0; i < tabItem.length; i++) {
                    SourceEditorTab tab = (SourceEditorTab) tabItem[i].getData();
                    if (name.equalsIgnoreCase(tab.getText())) {
                        tab.getEditor().gotToLineColumn(line, column);
                        tab.setFocus();
                        break;
                    }
                }
            }
        });

        sashForm1.setWeights(new int[] {
            20, 80
        });
        sashForm2.setWeights(new int[] {
            80, 20
        });

        String lastPath = preferences.getLastPath();
        if (lastPath == null) {
            lastPath = new File("").getAbsolutePath();
        }
        File file = new File(lastPath).getAbsoluteFile();
        if (!file.isDirectory()) {
            file = file.getParentFile();
        }
        browser.setSelection(file);
        browser.addOpenListener(new IOpenListener() {

            @Override
            public void open(OpenEvent event) {
                try {
                    IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                    if (selection.getFirstElement() instanceof File) {
                        File file = (File) selection.getFirstElement();
                        if (file.isDirectory()) {
                            return;
                        }
                        SourceEditorTab tab = openSourceTab(file);
                        tabFolder.setSelection(tab.getTabItem());
                        tab.setFocus();
                        preferences.addLru(file);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        browser.setFocus();

        return sashForm1;
    }

    private void handleFileNew() {
        SourceEditorTab tab = new SourceEditorTab(tabFolder, "");
        tab.setText(getDefaultName());
        tab.setFocus();
    }

    String getDefaultName() {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("MMMdd");

        for (char c = 'a'; c <= 'z'; c++) {
            String result = String.format("%s%c.asm", df.format(date), c);
            File file = new File(result);
            if (!file.exists() && !nameExists(result)) {
                return result;
            }
        }

        return null;
    }

    boolean nameExists(String name) {
        CTabItem[] tabItem = tabFolder.getItems();
        for (int n = 0; n < tabItem.length; n++) {
            if (name.equals(tabItem[n].getText())) {
                return true;
            }
        }
        return false;
    }

    private void handleFileOpen() {
        FileDialog dlg = new FileDialog(shell, SWT.OPEN);
        String[] filterNames = new String[] {
            "Assembler Files"
        };
        String[] filterExtensions = new String[] {
            "*.ASM;*.asm"
        };
        dlg.setFilterNames(filterNames);
        dlg.setFilterExtensions(filterExtensions);
        if (preferences.getLastPath() != null) {
            dlg.setFilterPath(preferences.getLastPath());
        }
        dlg.setText("Open File");

        final String fileName = dlg.open();
        if (fileName != null) {
            File file = new File(fileName);
            try {
                SourceEditorTab tab = openSourceTab(file);
                tabFolder.setSelection(tab.getTabItem());
                tab.setFocus();
            } catch (IOException e) {
                e.printStackTrace();
            }
            preferences.addLru(file);
            preferences.setLastPath(file.getParent());
        }
    }

    SourceEditorTab openSourceTab(File file) throws IOException {
        String line;
        StringBuilder sb = new StringBuilder();

        if (file.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            reader.close();
        }

        SourceEditorTab tab = new SourceEditorTab(tabFolder, sb.toString());
        tab.setText(file.getName());
        tab.setToolTipText(file.getAbsolutePath());
        tab.setFile(file);

        return tab;
    }

    private void handleFileSave() throws IOException {
        CTabItem tabItem = tabFolder.getSelection();
        if (tabItem == null) {
            return;
        }
        saveSourceTab((SourceEditorTab) tabItem.getData(), false);
    }

    private void handleFileSaveAs() throws IOException {
        CTabItem tabItem = tabFolder.getSelection();
        if (tabItem == null) {
            return;
        }
        saveSourceTab((SourceEditorTab) tabItem.getData(), true);
    }

    private void handleFileSaveAll() {
        CTabItem[] tabItem = tabFolder.getItems();
        for (int i = 0; i < tabItem.length; i++) {
            SourceEditorTab tab = (SourceEditorTab) tabItem[i].getData();
            if (tab.isDirty()) {
                try {
                    if (saveSourceTab(tab, false) == null) {
                        break;
                    }
                    tab.clearDirty();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    private boolean handleUnsavedContent() {
        boolean dirty = false;

        CTabItem[] tabItem = tabFolder.getItems();
        for (int i = 0; i < tabItem.length; i++) {
            SourceEditorTab tab = (SourceEditorTab) tabItem[i].getData();
            if (tab.isDirty()) {
                dirty = true;
                break;
            }
        }

        if (dirty) {
            String msg = "Editor contains unsaved changes.  Save before exit?";
            String[] buttons = new String[] {
                IDialogConstants.YES_LABEL,
                IDialogConstants.NO_LABEL,
                IDialogConstants.CANCEL_LABEL
            };
            MessageDialog dlg = new MessageDialog(shell, APP_TITLE, null, msg, MessageDialog.QUESTION, buttons, 0);
            switch (dlg.open()) {
                case 0: // YES
                    try {
                        for (int i = 0; i < tabItem.length; i++) {
                            SourceEditorTab tab = (SourceEditorTab) tabItem[i].getData();
                            if (tab.isDirty()) {
                                if (saveSourceTab(tab, false) == null) {
                                    return false;
                                }
                            }
                        }
                        dirty = false;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 1: // NO
                    return true;
                case 2: // CANCEL
                    return false;
            }
        }

        return !dirty;
    }

    File saveSourceTab(SourceEditorTab tab, boolean saveAs) throws IOException {
        File file = tab.getFile();

        if (saveAs || file == null) {
            FileDialog dlg = new FileDialog(shell, SWT.SAVE);
            if (preferences.getLastPath() != null) {
                dlg.setFilterPath(preferences.getLastPath());
            }
            dlg.setFileName(tab.getText());
            dlg.setText("Save File");

            String fileName = dlg.open();
            if (fileName == null) {
                return null;
            }
            file = new File(fileName);
        }

        Writer os = new OutputStreamWriter(new FileOutputStream(file));
        os.write(tab.getEditor().getText());
        os.close();

        if (saveAs || tab.getFile() == null) {
            tab.setFile(file);
            tab.setText(file.getName());
            tab.setToolTipText(file.getAbsolutePath());
            preferences.setLastPath(file.getParent());
        }

        tab.clearDirty();

        return file;
    }

    private void handleFileClose() {
        CTabItem tabItem = tabFolder.getSelection();
        if (tabItem == null) {
            return;
        }
        SourceEditorTab tab = (SourceEditorTab) tabItem.getData();
        if (canCloseSourceTab(tab)) {
            tabItem.dispose();
        }
    }

    private void handleFileCloseAll() {
        CTabItem[] tabItem = tabFolder.getItems();
        for (int i = 0; i < tabItem.length; i++) {
            SourceEditorTab tab = (SourceEditorTab) tabItem[i].getData();
            if (!canCloseSourceTab(tab)) {
                return;
            }
        }
        for (int i = 0; i < tabItem.length; i++) {
            tabItem[i].dispose();
        }
    }

    boolean canCloseSourceTab(SourceEditorTab tab) {
        if (tab.isDirty()) {
            String msg = "Save changes in '" + tab.getText() + "'?";
            String[] buttons = new String[] {
                IDialogConstants.YES_LABEL,
                IDialogConstants.NO_LABEL,
                IDialogConstants.CANCEL_LABEL
            };
            MessageDialog dlg = new MessageDialog(shell, APP_TITLE, null, msg, MessageDialog.QUESTION, buttons, 0);
            switch (dlg.open()) {
                case 0:
                    try {
                        if (saveSourceTab(tab, false) == null) {
                            return false;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                    break;
                case 2:
                    return false;
            }
            tab.clearDirty();
        }
        return true;
    }

    private void handleCompile() throws Exception {
        CTabItem tabItem = tabFolder.getSelection();
        if (tabItem == null) {
            return;
        }
        SourceEditorTab tab = (SourceEditorTab) tabItem.getData();

        console.clear();

        PrintStream out = new PrintStream(console.getOutputStream());
        out.print("Compiling " + tab.getText() + "...");

        List<File> includePaths = new ArrayList<File>();
        if (tab.getFile() != null) {
            includePaths.add(tab.getFile().getParentFile());
        }
        final SourceBuilder builder = new SourceBuilder(includePaths);

        final StringReader reader = new StringReader(tab.getEditor().getText());

        final String name = tab.getText();
        final File file = tab.getFile();

        new Thread(new Runnable() {

            @Override
            public void run() {
                IProgressMonitor monitor = statusLine.getProgressMonitor();
                monitor.beginTask("Compile", IProgressMonitor.UNKNOWN);

                compile(builder, reader, name, file);

                monitor.done();
            }
        }).start();
    }

    Source compile(SourceBuilder builder, Reader reader, String name, File file) {
        PrintStream out = new PrintStream(console.getOutputStream());
        PrintStream err = new PrintStream(console.getErrorStream());

        try {
            Source source = builder.parse(reader, new File(name));
            source.register();
            source.expand();
            source.resolve();

            if (file != null) {
                String baseName = name;
                if (baseName.indexOf('.') != -1) {
                    baseName = baseName.substring(0, baseName.lastIndexOf('.'));
                }

                OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(new File(file.getParentFile(), baseName + ".LST")));
                os.write(buildListing(new ArrayList<Line>(source.getLines())).toString());
                os.close();

                os = new OutputStreamWriter(new FileOutputStream(new File(file.getParentFile(), baseName + ".HEX")));
                os.write(buildIntelHexString(new ArrayList<Line>(source.getLines())).toString());
                os.close();
            }

            int lower = Integer.MAX_VALUE;
            int higher = Integer.MIN_VALUE;
            for (Line line : source.getLines()) {
                if (line.getSize() != 0) {
                    lower = Math.min(lower, line.getScope().getAddress());
                    higher = Math.max(higher, line.getScope().getAddress());
                }
            }
            out.println();
            out.println(String.format("Compiled %d lines from %04XH to %04XH (%d bytes)", source.getLines().size(), lower, higher, higher - lower + 1));

            return source;

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

            out.println();
            err.println(sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    StringBuilder buildListing(List<Line> lines) throws IOException {
        int column;
        StringBuilder sb = new StringBuilder();

        for (Line line : lines) {
            try {
                int address = line.getScope().getAddress();
                byte[] code = line.getBytes();

                sb.append(String.format("%05d  %04X ", line.getLineNumber() + 1, address));

                column = 0;
                int codeIndex = 0;
                for (int i = 0; codeIndex < code.length && (column + 3) < 24; i++, codeIndex++, address++) {
                    if (i != 0) {
                        sb.append(' ');
                        column++;
                    }
                    sb.append(String.format("%02X", code[codeIndex]));
                    column += 2;
                }
                while (column < 24 + 1) {
                    sb.append(' ');
                    column++;
                }

                sb.append(line.getSourceText());

                while (codeIndex < code.length) {
                    sb.append("\r\n");
                    sb.append(String.format("%05d  %04X ", line.getLineNumber() + 1, address));

                    column = 0;
                    for (int i = 0; codeIndex < code.length && (column + 3) < 24; i++, codeIndex++, address++) {
                        if (i != 0) {
                            sb.append(' ');
                            column++;
                        }
                        sb.append(String.format("%02X", code[codeIndex]));
                        column += 2;
                    }
                }

                sb.append("\r\n");
            } catch (AssemblyException e) {
                e.addContext(line);
                throw e;
            }
        }

        return sb;
    }

    StringBuilder buildIntelHexString(List<Line> lines) throws IOException {
        int addr = -1;
        int nextAddr = -1;
        StringBuilder sb = new StringBuilder();

        Collections.sort(lines, new Comparator<Line>() {

            @Override
            public int compare(Line o1, Line o2) {
                return o1.getScope().getAddress() - o2.getScope().getAddress();
            }
        });

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        for (Line line : lines) {
            try {
                byte[] code = line.getBytes();
                if (code.length == 0) {
                    continue;
                }
                if (line.getScope().getAddress() != nextAddr) {
                    os.close();

                    byte[] data = os.toByteArray();
                    if (data.length != 0) {
                        sb.append(toHexString(addr, data));
                    }

                    nextAddr = addr = line.getScope().getAddress();
                    os = new ByteArrayOutputStream();
                }
                os.write(code);
                nextAddr += code.length;
            } catch (AssemblyException e) {
                e.addContext(line);
                throw e;
            }
        }

        os.close();
        byte[] data = os.toByteArray();
        if (data.length != 0) {
            sb.append(toHexString(addr, data));
        }

        sb.append(":00000001FF\r\n");

        return sb;
    }

    static String toHexString(int addr, byte[] data) {
        StringBuilder sb = new StringBuilder();

        int i = 0;

        while ((data.length - i) > 0) {
            int l = data.length - i;
            if (l > 24) {
                l = 24;
            }
            sb.append(String.format(":%02X%04X%02X", l, addr, 0));

            int checksum = l + (addr & 0xFF) + ((addr >> 8) & 0xFF) + 0;
            for (int n = 0; n < l; n++, i++, addr++) {
                sb.append(String.format("%02X", data[i]));
                checksum += data[i];
            }

            sb.append(String.format("%02X\r\n", (-checksum) & 0xFF));
        }

        return sb.toString();
    }

    private void handleOpenTerminal() {
        if (terminal == null) {
            terminal = new SerialTerminal();
            terminal.setPort(preferences.getSerialPort());
            terminal.setBaud(preferences.getSerialBaud());
            terminal.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    preferences.setSerialPort(terminal.getPort());
                    preferences.setSerialBaud(terminal.getBaud());
                }
            });
            terminal.open();
            terminal.getShell().addDisposeListener(new DisposeListener() {

                @Override
                public void widgetDisposed(DisposeEvent e) {
                    terminal = null;
                }
            });
        }
        terminal.setFocus();
    }

    private void handleCompileAndUploadIntelHex() throws Exception {
        CTabItem tabItem = tabFolder.getSelection();
        if (tabItem == null) {
            return;
        }
        SourceEditorTab tab = (SourceEditorTab) tabItem.getData();

        console.clear();

        final PrintStream out = new PrintStream(console.getOutputStream());
        out.print("Compiling " + tab.getText() + "...");

        List<File> includePaths = new ArrayList<File>();
        if (tab.getFile() != null) {
            includePaths.add(tab.getFile().getParentFile());
        }
        final SourceBuilder builder = new SourceBuilder(includePaths);

        final StringReader reader = new StringReader(tab.getEditor().getText());

        final String name = tab.getText();
        final File file = tab.getFile();

        new Thread(new Runnable() {

            @Override
            public void run() {
                IProgressMonitor monitor = statusLine.getProgressMonitor();
                monitor.beginTask("Compile", IProgressMonitor.UNKNOWN);

                try {
                    Source source = compile(builder, reader, name, file);
                    if (source == null) {
                        return;
                    }

                    display.asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            handleOpenTerminal();
                        }
                    });

                    SerialPort serialPort = terminal.getSerialPort();
                    StringBuilder sb = buildIntelHexString(new ArrayList<Line>(source.getLines()));

                    monitor.beginTask("Upload", sb.length());
                    out.println("Sending to serial port " + serialPort.getPortName() + " ...");

                    for (int i = 0; i < sb.length(); i++) {
                        serialPort.writeInt(sb.charAt(i));
                        monitor.worked(1);
                    }
                    while (serialPort.getOutputBufferBytesCount() > 0) {
                        Thread.sleep(1);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                out.println("Done");
                monitor.done();
            }
        }).start();

    }

    private void handleNextTab() {
        int index = tabFolder.getSelectionIndex();
        index++;
        if (index >= tabFolder.getItemCount()) {
            index = 0;
        }
        tabFolder.setSelection(index);
        if (tabFolder.getSelection() != null) {
            tabFolder.getSelection().getControl().setFocus();
        }
    }

    private void handlePreviousTab() {
        int index = tabFolder.getSelectionIndex();
        index--;
        if (index < 0) {
            index = tabFolder.getItemCount() - 1;
        }
        tabFolder.setSelection(index);
        if (tabFolder.getSelection() != null) {
            tabFolder.getSelection().getControl().setFocus();
        }
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
                    Application app = new Application();
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
