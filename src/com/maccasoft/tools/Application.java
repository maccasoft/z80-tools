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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.databinding.observable.Realm;
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
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import com.maccasoft.tools.internal.ImageRegistry;

public class Application {

    public static final String APP_TITLE = "Z80 Tools";
    public static final String APP_VERSION = "0.0.0";

    Display display;
    Shell shell;

    SashForm sashForm;
    FileBrowser browser;
    CTabFolder tabFolder;

    Preferences preferences;

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

        Rectangle screen = display.getClientArea();

        Rectangle rect = new Rectangle(0, 0, (int) ((float) screen.width / (float) screen.height * 800), 800);
        rect.x = (screen.width - rect.width) / 2;
        rect.y = (screen.height - rect.height) / 2;
        if (rect.y < 0) {
            rect.height += rect.y * 2;
            rect.y = 0;
        }

        shell.setLocation(rect.x, rect.y);
        shell.setSize(rect.width, rect.height);

        FillLayout layout = new FillLayout();
        layout.marginWidth = layout.marginHeight = 5;
        shell.setLayout(layout);

        createContents(shell);

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

            }
        });

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Redo\tCtrl+Shift+Z");
        item.setAccelerator(SWT.MOD1 + SWT.MOD2 + 'Z');
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {

            }
        });

        new MenuItem(menu, SWT.SEPARATOR);

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Cut\tCtrl+X");
        item.setAccelerator(SWT.MOD1 + 'X');
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {

            }
        });

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Copy\tCtrl+C");
        item.setAccelerator(SWT.MOD1 + 'C');
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {

            }
        });

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Paste\tCtrl+V");
        item.setAccelerator(SWT.MOD1 + 'V');
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {

            }
        });

        new MenuItem(menu, SWT.SEPARATOR);

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Select All\tCtrl+A");
        item.setAccelerator(SWT.MOD1 + 'A');
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {

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

            }
        });

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Upload Intel HEX");
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {

            }
        });

        new MenuItem(menu, SWT.SEPARATOR);

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Serial Terminal\tCtrl+Shift+T");
        item.setAccelerator(SWT.MOD1 + SWT.MOD2 + 'T');
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {

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

    protected void createContents(Composite parent) {
        GC gc = new GC(parent);
        FontMetrics fontMetrics = gc.getFontMetrics();
        gc.dispose();

        sashForm = new SashForm(parent, SWT.HORIZONTAL);
        sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        browser = new FileBrowser(sashForm);
        browser.setRoots(new File[] {
            new File(System.getProperty("user.home"))
        });

        tabFolder = new CTabFolder(sashForm, SWT.BORDER);
        tabFolder.setTabHeight((int) (fontMetrics.getHeight() * 1.5));
        tabFolder.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {

            }
        });
        tabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {

            @Override
            public void close(CTabFolderEvent event) {
                SourceEditorTab tab = (SourceEditorTab) event.item.getData();
                event.doit = canCloseSourceTab(tab);
            }
        });

        sashForm.setWeights(new int[] {
            20, 80
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
