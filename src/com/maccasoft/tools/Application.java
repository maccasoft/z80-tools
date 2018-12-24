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
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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

import com.maccasoft.tools.internal.BusyIndicator;
import com.maccasoft.tools.internal.ImageRegistry;

import jssc.SerialPort;
import nl.grauw.glass.AssemblyException;
import nl.grauw.glass.Line;
import nl.grauw.glass.Source;
import nl.grauw.glass.SourceBuilder;
import nl.grauw.glass.directives.Directive;
import nl.grauw.glass.directives.Include;

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

    final PropertyChangeListener preferencesChangeListner = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (Preferences.PROP_ROOTS.equals(evt.getPropertyName())) {
                String[] roots = preferences.getRoots();
                if (roots == null || roots.length == 0) {
                    roots = new String[] {
                        new File(System.getProperty("user.home")).getAbsolutePath()
                    };
                }
                browser.setRoots(roots);
            }
            else if (Preferences.PROP_EDITOR_FONT.equals(evt.getPropertyName())) {
                CTabItem[] tabItem = tabFolder.getItems();
                for (int i = 0; i < tabItem.length; i++) {
                    SourceEditorTab tab = (SourceEditorTab) tabItem[i].getData();
                    tab.getEditor().setFont((String) evt.getNewValue());
                }
            }
            else if (Preferences.PROP_SHOW_LINE_NUMBERS.equals(evt.getPropertyName())) {
                CTabItem[] tabItem = tabFolder.getItems();
                for (int i = 0; i < tabItem.length; i++) {
                    SourceEditorTab tab = (SourceEditorTab) tabItem[i].getData();
                    tab.getEditor().setShowLineNumbers(((Boolean) evt.getNewValue()).booleanValue());
                }
            }
            else if (Preferences.PROP_TABWIDTH.equals(evt.getPropertyName())) {
                CTabItem[] tabItem = tabFolder.getItems();
                for (int i = 0; i < tabItem.length; i++) {
                    SourceEditorTab tab = (SourceEditorTab) tabItem[i].getData();
                    tab.getEditor().setTabWidth(((Integer) evt.getNewValue()).intValue());
                }
            }
            else if (Preferences.PROP_USE_TABSTOPS.equals(evt.getPropertyName())) {
                CTabItem[] tabItem = tabFolder.getItems();
                for (int i = 0; i < tabItem.length; i++) {
                    SourceEditorTab tab = (SourceEditorTab) tabItem[i].getData();
                    tab.getEditor().setUseTabstops(((Boolean) evt.getNewValue()).booleanValue());
                }
            }
            else if (Preferences.PROP_MNEMONIC_COLUMN.equals(evt.getPropertyName()) || Preferences.PROP_ARGUMENT_COLUMN.equals(evt.getPropertyName())
                || Preferences.PROP_COMMENT_COLUMN.equals(evt.getPropertyName())) {
                CTabItem[] tabItem = tabFolder.getItems();
                for (int i = 0; i < tabItem.length; i++) {
                    SourceEditorTab tab = (SourceEditorTab) tabItem[i].getData();
                    tab.getEditor().setTabStops(new int[] {
                        preferences.getMnemonicColumn(),
                        preferences.getArgumentColumn(),
                        preferences.getCommentColumn()
                    });
                }
            }
        }

    };

    public Application() {

    }

    public void open() {
        display = Display.getDefault();
        preferences = Preferences.getInstance();

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

        preferences.addPropertyChangeListener(preferencesChangeListner);

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
                    preferences.removePropertyChangeListener(preferencesChangeListner);

                    List<String> openTabs = new ArrayList<String>();
                    for (int i = 0; i < tabFolder.getItemCount(); i++) {
                        SourceEditorTab tab = (SourceEditorTab) tabFolder.getItem(i).getData();
                        if (tab.getFile() != null) {
                            openTabs.add(tab.getFile().getAbsolutePath());
                        }
                    }
                    preferences.setOpenTabs(openTabs.toArray(new String[openTabs.size()]));

                    if (tabFolder.getSelection() != null) {
                        SourceEditorTab tab = (SourceEditorTab) tabFolder.getSelection().getData();
                        preferences.setSelectedTab(tab.getFile().getAbsolutePath());
                    }

                    preferences.save();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                ImageRegistry.dispose();
            }
        });

        BusyIndicator.showWhile(display, new Runnable() {

            @Override
            public void run() {
                String line;

                final String[] openTabs = preferences.getOpenTabs();
                if (openTabs == null || openTabs.length == 0 || !preferences.isReloadOpenTabs()) {
                    return;
                }

                for (int i = 0; i < openTabs.length; i++) {
                    File file = new File(openTabs[i]);
                    StringBuilder sb = new StringBuilder();
                    try {
                        if (!file.exists() || file.isDirectory()) {
                            continue;
                        }

                        BufferedReader reader = new BufferedReader(new FileReader(file));
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                            sb.append("\n");
                        }
                        reader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    display.asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            SourceEditorTab tab = new SourceEditorTab(tabFolder, sb.toString());
                            tab.setText(file.getName());
                            tab.setToolTipText(file.getAbsolutePath());
                            tab.setFile(file);
                            applyEditoTabPreferences(tab);
                        }
                    });
                }

                display.asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        if (tabFolder.getItemCount() == 0) {
                            return;
                        }

                        SourceEditorTab tab = (SourceEditorTab) tabFolder.getItem(0).getData();

                        String s = preferences.getSelectedTab();
                        if (s != null) {
                            File selectedFile = new File(s);
                            for (int i = 0; i < tabFolder.getItemCount(); i++) {
                                SourceEditorTab editorTab = (SourceEditorTab) tabFolder.getItem(i).getData();
                                if (selectedFile.equals(editorTab.getFile())) {
                                    tab = editorTab;
                                    break;
                                }
                            }
                        }

                        tabFolder.setSelection(tab.getTabItem());
                        tab.setFocus();
                    }
                });
            }
        }, true);
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
                PreferencesDialog dlg = new PreferencesDialog(shell);
                dlg.open();
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
                        openSourceTab(fileToOpen);
                        preferences.addLru(fileToOpen);
                    } catch (Exception e1) {
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
        item.setText("Format source\tCtrl+Shift+F");
        item.setAccelerator(SWT.MOD1 + +SWT.MOD2 + 'F');
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                try {
                    CTabItem tabItem = tabFolder.getSelection();
                    if (tabItem == null) {
                        return;
                    }
                    SourceEditorTab tab = (SourceEditorTab) tabItem.getData();

                    StringReader reader = new StringReader(tab.getEditor().getText());
                    SourceBuilder builder = new SourceBuilder(new ArrayList<File>()) {

                        @Override
                        public Directive getDirective(Line line, LineNumberReader reader, File sourceFile) {
                            if (line.getMnemonic() != null) {
                                switch (line.getMnemonic()) {
                                    case "include":
                                    case "INCLUDE":
                                    case ".include":
                                    case ".INCLUDE":
                                        return new Include();
                                }
                            }
                            return super.getDirective(line, reader, sourceFile);
                        }

                    };

                    SourceFormatter formatter = new SourceFormatter(builder.parse(reader, new File("")));
                    formatter.setMnemonicColumn(preferences.getMnemonicColumn());
                    formatter.setArgumentColumn(preferences.getArgumentColumn());
                    formatter.setCommentColumn(preferences.getCommentColumn());
                    formatter.setLabelCase(preferences.getLabelCase());
                    formatter.setMnemonicCase(preferences.getMnemonicCase());

                    tab.getEditor().replaceText(formatter.format());

                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        new MenuItem(menu, SWT.SEPARATOR);

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

        String[] roots = preferences.getRoots();
        if (roots == null || roots.length == 0) {
            roots = new String[] {
                new File(System.getProperty("user.home")).getAbsolutePath()
            };
        }
        browser.setRoots(roots);

        String lastPath = preferences.getLastPath();
        if (lastPath == null) {
            lastPath = new File("").getAbsolutePath();
        }
        File file = new File(lastPath).getAbsoluteFile();
        if (!file.isDirectory()) {
            file = file.getParentFile();
        }
        browser.setSelection(file);
        browser.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if (event.getSelection().isEmpty()) {
                    preferences.setLastPath(null);
                    return;
                }
                File file = (File) ((IStructuredSelection) event.getSelection()).getFirstElement();
                if (!file.isDirectory()) {
                    file = file.getParentFile();
                }
                if (file != null) {
                    preferences.setLastPath(file.getAbsolutePath());
                }
            }
        });
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
                        openSourceTab(file);
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
        applyEditoTabPreferences(tab);
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
        dlg.setText("Open File");
        String[] filterNames = new String[] {
            "Assembler Files"
        };
        String[] filterExtensions = new String[] {
            "*.ASM;*.asm"
        };
        dlg.setFilterNames(filterNames);
        dlg.setFilterExtensions(filterExtensions);

        String filterPath = null;
        if (tabFolder.getSelection() != null) {
            SourceEditorTab tab = (SourceEditorTab) tabFolder.getSelection().getData();
            if (tab.getFile() != null) {
                File file = tab.getFile().getAbsoluteFile().getParentFile();
                if (file != null) {
                    filterPath = file.getAbsolutePath();
                }
            }
        }
        if (filterPath == null) {
            filterPath = preferences.getLastPath();
        }
        if (filterPath != null) {
            dlg.setFilterPath(filterPath);
        }

        final String fileName = dlg.open();
        if (fileName != null) {
            File file = new File(fileName);
            try {
                openSourceTab(file);
                preferences.addLru(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void openSourceTab(File file) {
        CTabItem[] tabItem = tabFolder.getItems();
        for (int i = 0; i < tabItem.length; i++) {
            SourceEditorTab tab = (SourceEditorTab) tabItem[i].getData();
            if (file.equals(tab.getFile())) {
                tabFolder.setSelection(tab.getTabItem());
                if (tabFolder.getSelection() != null) {
                    tabFolder.getSelection().getControl().setFocus();
                }
                return;
            }
        }
        BusyIndicator.showWhile(display, new Runnable() {

            @Override
            public void run() {
                String line;
                StringBuilder sb = new StringBuilder();

                if (file.exists()) {
                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(file));
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                            sb.append("\n");
                        }
                        reader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                display.asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        SourceEditorTab tab = new SourceEditorTab(tabFolder, sb.toString());
                        tab.setText(file.getName());
                        tab.setToolTipText(file.getAbsolutePath());
                        tab.setFile(file);
                        applyEditoTabPreferences(tab);

                        tabFolder.setSelection(tab.getTabItem());
                        tab.setFocus();
                    }
                });
            }
        }, true);
    }

    void applyEditoTabPreferences(SourceEditorTab tab) {
        tab.getEditor().setShowLineNumbers(preferences.isShowLineNumbers());
        tab.getEditor().setFont(preferences.getEditorFont());
        tab.getEditor().setTabWidth(preferences.getTabWidth());
        tab.getEditor().setTabStops(new int[] {
            preferences.getMnemonicColumn(),
            preferences.getArgumentColumn(),
            preferences.getCommentColumn()
        });
        tab.getEditor().setUseTabstops(preferences.isUseTabstops());
    }

    private void handleFileSave() throws IOException {
        CTabItem tabItem = tabFolder.getSelection();
        if (tabItem == null) {
            return;
        }
        File file = saveSourceTab((SourceEditorTab) tabItem.getData(), false);
        if (file != null) {
            browser.refresh(file.getAbsoluteFile());
        }
    }

    private void handleFileSaveAs() throws IOException {
        CTabItem tabItem = tabFolder.getSelection();
        if (tabItem == null) {
            return;
        }
        File file = saveSourceTab((SourceEditorTab) tabItem.getData(), true);
        if (file != null) {
            browser.refresh(file.getAbsoluteFile());
        }
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
        browser.refresh();
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
            dlg.setText("Save File");
            dlg.setFileName(tab.getText());

            String filterPath = null;
            if (file != null) {
                file = file.getAbsoluteFile().getParentFile();
                if (file != null) {
                    filterPath = file.getAbsolutePath();
                }
            }
            if (filterPath == null) {
                filterPath = preferences.getLastPath();
            }
            if (filterPath != null) {
                dlg.setFilterPath(filterPath);
            }

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

        final List<File> includePaths = new ArrayList<File>();
        if (tab.getFile() != null) {
            includePaths.add(tab.getFile().getParentFile());
        }

        final StringReader reader = new StringReader(tab.getEditor().getText());

        final String name = tab.getText();
        final File file = tab.getFile();

        new Thread(new Runnable() {

            @Override
            public void run() {
                IProgressMonitor monitor = statusLine.getProgressMonitor();
                monitor.beginTask("Compile", IProgressMonitor.UNKNOWN);

                compile(reader, name, file, includePaths);

                monitor.done();
            }
        }).start();
    }

    Source compile(Reader reader, String name, File file, List<File> includePaths) {
        PrintStream out = new PrintStream(console.getOutputStream());
        PrintStream err = new PrintStream(console.getErrorStream());

        out.print("Compiling " + name + "...");

        try {
            SourceBuilder builder = new SourceBuilder(includePaths) {

                @Override
                public Source parse(File sourceFile) {
                    final AtomicReference<StringReader> result = new AtomicReference<StringReader>();

                    out.print("\r\nCompiling " + sourceFile.getName() + "...");

                    display.syncExec(new Runnable() {

                        @Override
                        public void run() {
                            CTabItem[] tabItem = tabFolder.getItems();
                            for (int i = 0; i < tabItem.length; i++) {
                                SourceEditorTab tab = (SourceEditorTab) tabItem[i].getData();
                                if (sourceFile.equals(tab.getFile())) {
                                    StringReader reader = new StringReader(tab.getEditor().getText());
                                    result.set(reader);
                                    break;
                                }
                            }
                        }
                    });
                    if (result.get() != null) {
                        return parse(result.get(), sourceFile);
                    }

                    return super.parse(sourceFile);
                }

            };

            Source source = builder.parse(reader, new File(name));
            source.register();
            source.expand();
            source.resolve();

            if (file != null) {
                String baseName = name;
                if (baseName.indexOf('.') != -1) {
                    baseName = baseName.substring(0, baseName.lastIndexOf('.'));
                }

                if (preferences.isGenerateListing()) {
                    OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(new File(file.getParentFile(), baseName + ".LST")));
                    os.write(new ListingBuilder(source).build().toString());
                    os.close();
                }

                if (preferences.isGenerateBinary()) {
                    OutputStream output = new FileOutputStream(new File(file.getParentFile(), baseName + ".BIN"));
                    source.generateObjectCode(output);
                    output.close();
                }

                if (preferences.isGenerateHex()) {
                    OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(new File(file.getParentFile(), baseName + ".HEX")));
                    os.write(new IntelHexBuilder(source).build().toString());
                    os.close();
                }
            }

            int lower = Integer.MAX_VALUE;
            int higher = Integer.MIN_VALUE;
            for (Line line : source.getLines()) {
                try {
                    if (line.getSize() != 0) {
                        lower = Math.min(lower, line.getScope().getAddress());
                        higher = Math.max(higher, line.getScope().getAddress() + line.getSize() - 1);
                    }
                } catch (Exception e) {
                    // Ignore, not important
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

        final List<File> includePaths = new ArrayList<File>();
        if (tab.getFile() != null) {
            includePaths.add(tab.getFile().getParentFile());
        }

        final StringReader reader = new StringReader(tab.getEditor().getText());

        final String name = tab.getText();
        final File file = tab.getFile();

        new Thread(new Runnable() {

            @Override
            public void run() {
                final PrintStream out = new PrintStream(console.getOutputStream());

                IProgressMonitor monitor = statusLine.getProgressMonitor();
                monitor.beginTask("Compile", IProgressMonitor.UNKNOWN);

                try {
                    Source source = compile(reader, name, file, includePaths);
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
                    StringBuilder sb = new IntelHexBuilder(source).build();

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
