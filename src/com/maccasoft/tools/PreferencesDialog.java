/*
 * Copyright (c) 2018-19 Marco Maccaferri and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.maccasoft.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.maccasoft.tools.internal.ImageRegistry;

public class PreferencesDialog extends Dialog {

    List pages;
    Composite stack;
    StackLayout stackLayout;

    List roots;
    Button rootAdd;
    Button rootRemove;
    Button rootMoveUp;
    Button rootMoveDown;
    Combo lineDelimiters;

    Text editorFont;
    Button editorFontBrowse;
    Button showLineNumbers;
    Text tabWidth;
    Button useTabstops;
    Button reloadOpenTabs;

    Text mnemonicColumn;
    Text argumentColumn;
    Text commentColumn;
    Combo labelCase;
    Combo mnemonicCase;
    Combo directivePrefix;

    List includes;
    Button includesAdd;
    Button includesRemove;
    Button includesMoveUp;
    Button includesMoveDown;
    Button generateBinary;
    Button generateHex;
    Button generateListing;
    Text downloadCommand;
    Text xmodemCommand;

    Text romImage1;
    Text romAddress1;
    Text romImage2;
    Text romAddress2;
    Text compactFlashImage;

    Preferences preferences;
    String defaultFont;
    Font fontBold;

    static int lastPage;

    public PreferencesDialog(Shell parentShell) {
        super(parentShell);
        preferences = Preferences.getInstance();
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Preferences");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        applyDialogFont(composite);

        if ("win32".equals(SWT.getPlatform())) {
            defaultFont = StringConverter.asString(new FontData("Courier New", 9, SWT.NONE));
        }
        else {
            defaultFont = StringConverter.asString(new FontData("mono", 9, SWT.NONE));
        }

        FontData[] fontData = composite.getFont().getFontData();
        fontBold = new Font(composite.getDisplay(), fontData[0].getName(), fontData[0].getHeight(), SWT.BOLD);
        composite.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                fontBold.dispose();
            }
        });

        pages = new List(composite, SWT.SIMPLE | SWT.BORDER);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, true);
        gridData.widthHint = convertWidthInCharsToPixels(20);
        pages.setLayoutData(gridData);

        stack = new Composite(composite, SWT.NONE);
        stackLayout = new StackLayout();
        stackLayout.marginHeight = stackLayout.marginWidth = 0;
        stack.setLayout(stackLayout);
        stack.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createGeneralPage(stack);
        createAssemblerPage(stack);
        createEditorPage(stack);
        createEmulatorPage(stack);
        createFormatterPage(stack);

        stackLayout.topControl = stack.getChildren()[lastPage];

        pages.select(lastPage);
        pages.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                lastPage = pages.getSelectionIndex();
                stackLayout.topControl = stack.getChildren()[lastPage];
                stack.layout();
            }
        });

        return composite;
    }

    void createGeneralPage(Composite parent) {
        Composite composite = createPage(parent, "General");

        Composite group = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = layout.marginHeight = 0;
        group.setLayout(layout);
        group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));

        Label label = new Label(group, SWT.NONE);
        label.setText("File browser root paths");
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        roots = new List(group, SWT.SINGLE | SWT.BORDER);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = convertWidthInCharsToPixels(50);
        gridData.heightHint = convertHeightInCharsToPixels(5) + roots.getBorderWidth() * 2;
        roots.setLayoutData(gridData);
        roots.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateRootDirectoryButtons();
            }
        });

        Composite container = new Composite(group, SWT.NONE);
        layout = new GridLayout(1, true);
        layout.marginWidth = layout.marginHeight = 0;
        container.setLayout(layout);
        container.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

        rootAdd = new Button(container, SWT.PUSH);
        rootAdd.setImage(ImageRegistry.getImageFromResources("add.png"));
        rootAdd.setToolTipText("Add");
        rootAdd.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dlg = new DirectoryDialog(getShell());

                int index = roots.getSelectionIndex();
                if (index != -1) {
                    dlg.setFilterPath(roots.getItem(index));
                }

                String s = dlg.open();
                if (s != null) {
                    roots.add(s);
                    updateRootDirectoryButtons();
                }
            }

        });

        rootRemove = new Button(container, SWT.PUSH);
        rootRemove.setImage(ImageRegistry.getImageFromResources("delete.png"));
        rootRemove.setToolTipText("Remove");
        rootRemove.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                roots.remove(roots.getSelectionIndex());
                updateRootDirectoryButtons();
            }

        });
        rootRemove.setEnabled(false);

        rootMoveUp = new Button(container, SWT.PUSH);
        rootMoveUp.setImage(ImageRegistry.getImageFromResources("arrow_up.png"));
        rootMoveUp.setToolTipText("Up");
        rootMoveUp.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = roots.getSelectionIndex();
                ArrayList<String> items = new ArrayList<String>(Arrays.asList(roots.getItems()));
                String s = items.get(index);
                items.remove(index);
                items.add(index - 1, s);
                roots.setItems(items.toArray(new String[items.size()]));
                roots.setSelection(index - 1);
                roots.setFocus();
                updateRootDirectoryButtons();
            }

        });
        rootMoveUp.setEnabled(false);

        rootMoveDown = new Button(container, SWT.PUSH);
        rootMoveDown.setImage(ImageRegistry.getImageFromResources("arrow_down.png"));
        rootMoveDown.setToolTipText("Down");
        rootMoveDown.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = roots.getSelectionIndex();
                ArrayList<String> items = new ArrayList<String>(Arrays.asList(roots.getItems()));
                String s = items.get(index);
                items.add(index + 2, s);
                items.remove(index);
                roots.setItems(items.toArray(new String[items.size()]));
                roots.setSelection(index + 1);
                roots.setFocus();
                updateRootDirectoryButtons();
            }

        });
        rootMoveDown.setEnabled(false);

        String[] items = preferences.getRoots();
        if (items != null) {
            roots.setItems(items);
        }

        addSeparator(composite);

        new Label(composite, SWT.NONE);

        reloadOpenTabs = new Button(composite, SWT.CHECK);
        reloadOpenTabs.setText("Reload open tabs");
        reloadOpenTabs.setSelection(preferences.isReloadOpenTabs());

        label = new Label(composite, SWT.NONE);
        label.setText("Line delimiters");
        lineDelimiters = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        lineDelimiters.setItems(new String[] {
            "Platform",
            "Windows (CR/LF)",
            "Linux (LF)",
        });
        lineDelimiters.select(preferences.getLineDelimiters());

        addSeparator(composite);

        label = new Label(composite, SWT.NONE);
        label.setText("Download cmd.");
        downloadCommand = new Text(composite, SWT.BORDER);
        downloadCommand.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        downloadCommand.setText(preferences.getDownloadCommand());

        label = new Label(composite, SWT.NONE);
        label.setText("XModem cmd.");
        xmodemCommand = new Text(composite, SWT.BORDER);
        xmodemCommand.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        xmodemCommand.setText(preferences.getXmodemCommand());

        label = new Label(composite, SWT.NONE);
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
        ((GridData) label.getLayoutData()).heightHint = convertHeightInCharsToPixels(3);
    }

    void updateRootDirectoryButtons() {
        int index = roots.getSelectionIndex();
        rootRemove.setEnabled(index != -1);
        rootMoveUp.setEnabled(index != -1 && index > 0);
        rootMoveDown.setEnabled(index != -1 && index < (roots.getItemCount() - 1));
    }

    void createAssemblerPage(Composite parent) {
        Composite composite = createPage(parent, "Assembler");

        Composite group = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = layout.marginHeight = 0;
        group.setLayout(layout);
        group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));

        Label label = new Label(group, SWT.NONE);
        label.setText("Include paths");
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        includes = new List(group, SWT.SINGLE | SWT.BORDER);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.widthHint = convertWidthInCharsToPixels(50);
        gridData.heightHint = convertHeightInCharsToPixels(5) + includes.getBorderWidth() * 2;
        includes.setLayoutData(gridData);
        includes.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateIncludesButtons();
            }
        });

        Composite container = new Composite(group, SWT.NONE);
        layout = new GridLayout(1, true);
        layout.marginWidth = layout.marginHeight = 0;
        container.setLayout(layout);
        container.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

        includesAdd = new Button(container, SWT.PUSH);
        includesAdd.setImage(ImageRegistry.getImageFromResources("add.png"));
        includesAdd.setToolTipText("Add");
        includesAdd.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dlg = new DirectoryDialog(getShell());

                int index = includes.getSelectionIndex();
                if (index != -1) {
                    dlg.setFilterPath(includes.getItem(index));
                }

                String s = dlg.open();
                if (s != null) {
                    includes.add(s);
                    updateIncludesButtons();
                }
            }

        });

        includesRemove = new Button(container, SWT.PUSH);
        includesRemove.setImage(ImageRegistry.getImageFromResources("delete.png"));
        includesRemove.setToolTipText("Remove");
        includesRemove.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                includes.remove(includes.getSelectionIndex());
                updateIncludesButtons();
            }

        });
        includesRemove.setEnabled(false);

        includesMoveUp = new Button(container, SWT.PUSH);
        includesMoveUp.setImage(ImageRegistry.getImageFromResources("arrow_up.png"));
        includesMoveUp.setToolTipText("Up");
        includesMoveUp.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = includes.getSelectionIndex();
                ArrayList<String> items = new ArrayList<String>(Arrays.asList(includes.getItems()));
                String s = items.get(index);
                items.remove(index);
                items.add(index - 1, s);
                includes.setItems(items.toArray(new String[items.size()]));
                includes.setSelection(index - 1);
                includes.setFocus();
                updateIncludesButtons();
            }

        });
        includesMoveUp.setEnabled(false);

        includesMoveDown = new Button(container, SWT.PUSH);
        includesMoveDown.setImage(ImageRegistry.getImageFromResources("arrow_down.png"));
        includesMoveDown.setToolTipText("Down");
        includesMoveDown.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = includes.getSelectionIndex();
                ArrayList<String> items = new ArrayList<String>(Arrays.asList(includes.getItems()));
                String s = items.get(index);
                items.add(index + 2, s);
                items.remove(index);
                includes.setItems(items.toArray(new String[items.size()]));
                includes.setSelection(index + 1);
                includes.setFocus();
                updateIncludesButtons();
            }

        });
        includesMoveDown.setEnabled(false);

        String[] items = preferences.getIncludes();
        if (items != null) {
            includes.setItems(items);
        }

        label = new Label(composite, SWT.NONE);
        label.setText("Output");

        group = new Composite(composite, SWT.NONE);
        layout = new GridLayout(3, false);
        layout.marginWidth = layout.marginHeight = 0;
        group.setLayout(layout);
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        generateBinary = new Button(group, SWT.CHECK);
        generateBinary.setText("Binary");
        generateBinary.setSelection(preferences.isGenerateBinary());

        generateHex = new Button(group, SWT.CHECK);
        generateHex.setText("Intel Hex");
        generateHex.setSelection(preferences.isGenerateHex());

        generateListing = new Button(group, SWT.CHECK);
        generateListing.setText("Listing");
        generateListing.setSelection(preferences.isGenerateListing());
    }

    void updateIncludesButtons() {
        int index = includes.getSelectionIndex();
        includesRemove.setEnabled(index != -1);
        includesMoveUp.setEnabled(index != -1 && index > 0);
        includesMoveDown.setEnabled(index != -1 && index < (includes.getItemCount() - 1));
    }

    void createEditorPage(Composite parent) {
        Composite composite = createPage(parent, "Editor");

        Label label = new Label(composite, SWT.NONE);
        label.setText("Font");

        Composite container = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = layout.marginHeight = 0;
        container.setLayout(layout);
        container.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        editorFont = new Text(container, SWT.BORDER);
        editorFont.setLayoutData(new GridData(convertWidthInCharsToPixels(35), SWT.DEFAULT));
        editorFontBrowse = new Button(container, SWT.PUSH);
        editorFontBrowse.setText("Select");
        editorFontBrowse.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                FontDialog dlg = new FontDialog(getShell());
                dlg.setText("Editor font");
                dlg.setFontList(new FontData[] {
                    StringConverter.asFontData(editorFont.getText())
                });
                FontData result = dlg.open();
                if (result != null) {
                    editorFont.setText(StringConverter.asString(result));
                }
            }
        });
        String s = preferences.getEditorFont();
        editorFont.setText((s != null && !"".equals(s)) ? s : defaultFont);

        new Label(composite, SWT.NONE);

        showLineNumbers = new Button(composite, SWT.CHECK);
        showLineNumbers.setText("Show line numbers");
        showLineNumbers.setSelection(preferences.isShowLineNumbers());

        label = new Label(composite, SWT.NONE);
        label.setText("Tab width");

        tabWidth = new Text(composite, SWT.BORDER);
        tabWidth.setLayoutData(new GridData(convertWidthInCharsToPixels(4), SWT.DEFAULT));
        tabWidth.setText(String.valueOf(preferences.getTabWidth()));

        new Label(composite, SWT.NONE);

        useTabstops = new Button(composite, SWT.CHECK);
        useTabstops.setText("Use formatter tabstops");
        useTabstops.setSelection(preferences.isUseTabstops());
    }

    void createFormatterPage(Composite parent) {
        Composite composite = createPage(parent, "Formatter");

        Label label = new Label(composite, SWT.NONE);
        label.setText("Mnemonic column");
        mnemonicColumn = new Text(composite, SWT.BORDER);
        mnemonicColumn.setLayoutData(new GridData(convertWidthInCharsToPixels(4), SWT.DEFAULT));
        mnemonicColumn.setText(String.valueOf(preferences.getMnemonicColumn()));

        label = new Label(composite, SWT.NONE);
        label.setText("Arguments column");
        argumentColumn = new Text(composite, SWT.BORDER);
        argumentColumn.setLayoutData(new GridData(convertWidthInCharsToPixels(4), SWT.DEFAULT));
        argumentColumn.setText(String.valueOf(preferences.getArgumentColumn()));

        label = new Label(composite, SWT.NONE);
        label.setText("Comment column");
        commentColumn = new Text(composite, SWT.BORDER);
        commentColumn.setLayoutData(new GridData(convertWidthInCharsToPixels(4), SWT.DEFAULT));
        commentColumn.setText(String.valueOf(preferences.getCommentColumn()));

        label = new Label(composite, SWT.NONE);
        label.setText("Label case");
        labelCase = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        labelCase.setItems(new String[] {
            "No change",
            "Upper",
            "Lower",
        });
        labelCase.select(preferences.getLabelCase());

        label = new Label(composite, SWT.NONE);
        label.setText("Mnemonic case");
        mnemonicCase = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        mnemonicCase.setItems(new String[] {
            "No change",
            "Upper",
            "Lower",
        });
        mnemonicCase.select(preferences.getMnemonicCase());

        label = new Label(composite, SWT.NONE);
        label.setText("Directive prefix");
        directivePrefix = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        directivePrefix.setItems(new String[] {
            "No change",
            "Add dot",
            "Remove dot",
        });
        directivePrefix.select(preferences.getDirectivePrefix());
    }

    void createEmulatorPage(Composite parent) {
        Composite composite = createPage(parent, "Emulator");

        Label label = new Label(composite, SWT.NONE);
        label.setText("ROM images");
        Composite container = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout(3, false);
        layout.marginWidth = layout.marginHeight = 0;
        container.setLayout(layout);
        container.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        romAddress1 = new Text(container, SWT.BORDER);
        romAddress1.setLayoutData(new GridData(convertWidthInCharsToPixels(5), SWT.DEFAULT));
        romAddress1.setText(String.format("%04X", preferences.getRomAddress1()));
        romAddress1.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                try {
                    int value = Integer.valueOf(((Text) e.widget).getText(), 16);
                    ((Text) e.widget).setText(String.format("%04X", value));
                } catch (Exception e1) {
                    // Do nothing
                }
            }
        });

        romImage1 = new Text(container, SWT.BORDER);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.widthHint = convertWidthInCharsToPixels(40);
        romImage1.setLayoutData(gridData);
        if (preferences.getRomImage1() != null) {
            romImage1.setText(preferences.getRomImage1());
        }

        Button button = new Button(container, SWT.PUSH);
        button.setText("Browse");
        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dlg = new FileDialog(getShell(), SWT.OPEN);
                dlg.setText("Open ROM image");
                dlg.setFilterNames(new String[] {
                    "All files",
                    "Rom images",
                    "Source files"
                });
                dlg.setFilterExtensions(new String[] {
                    "*.*",
                    "*.BIN;*.bin",
                    "*.ASM;*.asm"
                });

                String file = romImage1.getText();
                if (!"".equals(file)) {
                    dlg.setFilterPath(new File(file).getAbsoluteFile().getParent());
                    if (file.toLowerCase().endsWith(".bin")) {
                        dlg.setFilterIndex(1);
                    }
                    else if (file.toLowerCase().endsWith(".asm")) {
                        dlg.setFilterIndex(2);
                    }
                    else {
                        dlg.setFilterIndex(0);
                    }
                }
                else {
                    dlg.setFilterIndex(1);
                }

                if ((file = dlg.open()) != null) {
                    romImage1.setText(file);
                }
            }
        });

        new Label(composite, SWT.NONE);
        container = new Composite(composite, SWT.NONE);
        layout = new GridLayout(3, false);
        layout.marginWidth = layout.marginHeight = 0;
        container.setLayout(layout);
        container.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        romAddress2 = new Text(container, SWT.BORDER);
        romAddress2.setLayoutData(new GridData(convertWidthInCharsToPixels(5), SWT.DEFAULT));
        romAddress2.setText(String.format("%04X", preferences.getRomAddress2()));
        romAddress2.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                try {
                    int value = Integer.valueOf(((Text) e.widget).getText(), 16);
                    ((Text) e.widget).setText(String.format("%04X", value));
                } catch (Exception e1) {
                    // Do nothing
                }
            }
        });

        romImage2 = new Text(container, SWT.BORDER);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.widthHint = convertWidthInCharsToPixels(40);
        romImage2.setLayoutData(gridData);
        if (preferences.getRomImage2() != null) {
            romImage2.setText(preferences.getRomImage2());
        }

        button = new Button(container, SWT.PUSH);
        button.setText("Browse");
        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dlg = new FileDialog(getShell(), SWT.OPEN);
                dlg.setText("Open ROM image");
                dlg.setFilterNames(new String[] {
                    "All files",
                    "Rom images",
                    "Source files"
                });
                dlg.setFilterExtensions(new String[] {
                    "*.*",
                    "*.BIN;*.bin",
                    "*.ASM;*.asm"
                });

                String file = romImage2.getText();
                if (!"".equals(file)) {
                    dlg.setFilterPath(new File(file).getAbsoluteFile().getParent());
                    if (file.toLowerCase().endsWith(".bin")) {
                        dlg.setFilterIndex(1);
                    }
                    else if (file.toLowerCase().endsWith(".asm")) {
                        dlg.setFilterIndex(2);
                    }
                    else {
                        dlg.setFilterIndex(0);
                    }
                }
                else {
                    dlg.setFilterIndex(1);
                }

                if ((file = dlg.open()) != null) {
                    romImage2.setText(file);
                }
            }
        });

        label = new Label(composite, SWT.NONE);
        label.setText("CF card image");
        container = new Composite(composite, SWT.NONE);
        layout = new GridLayout(2, false);
        layout.marginWidth = layout.marginHeight = 0;
        container.setLayout(layout);
        container.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        compactFlashImage = new Text(container, SWT.BORDER);
        compactFlashImage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        if (preferences.getCompactFlashImage() != null) {
            compactFlashImage.setText(preferences.getCompactFlashImage());
        }

        button = new Button(container, SWT.PUSH);
        button.setText("Browse");
        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dlg = new FileDialog(getShell(), SWT.OPEN);
                dlg.setText("Open Compact Flash image");
                dlg.setFilterNames(new String[] {
                    "All files",
                    "Compact Flash images"
                });
                dlg.setFilterExtensions(new String[] {
                    "*.*",
                    "*.IMG;*.img"
                });

                String file = compactFlashImage.getText();
                if (!"".equals(file)) {
                    dlg.setFilterPath(new File(file).getAbsoluteFile().getParent());
                    if (file.toLowerCase().endsWith(".img")) {
                        dlg.setFilterIndex(1);
                    }
                    else {
                        dlg.setFilterIndex(0);
                    }
                }
                else {
                    dlg.setFilterIndex(1);
                }

                if ((file = dlg.open()) != null) {
                    compactFlashImage.setText(file);
                }
            }
        });
    }

    void addSeparator(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, ((GridLayout) parent.getLayout()).numColumns, 1));
        ((GridData) label.getLayoutData()).heightHint = ((GridLayout) parent.getLayout()).marginTop;
    }

    Composite createPage(Composite parent, String text) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = layout.marginWidth = 0;
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label label = new Label(composite, SWT.NONE);
        label.setText(text);
        label.setFont(fontBold);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, layout.numColumns, 1));

        label = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, layout.numColumns, 1));

        pages.add(text);

        return composite;
    }

    @Override
    protected void okPressed() {
        preferences.setRoots(roots.getItems());

        preferences.setEditorFont(editorFont.getText().equals(defaultFont) ? null : editorFont.getText());
        preferences.setShowLineNumbers(showLineNumbers.getSelection());
        preferences.setTabWidth(Integer.valueOf(tabWidth.getText()));
        preferences.setUseTabstops(useTabstops.getSelection());
        preferences.setReloadOpenTabs(reloadOpenTabs.getSelection());
        preferences.setLineDelimiters(lineDelimiters.getSelectionIndex());

        preferences.setMnemonicColumn(Integer.valueOf(mnemonicColumn.getText()));
        preferences.setArgumentColumn(Integer.valueOf(argumentColumn.getText()));
        preferences.setCommentColumn(Integer.valueOf(commentColumn.getText()));
        preferences.setLabelCase(labelCase.getSelectionIndex());
        preferences.setMnemonicCase(mnemonicCase.getSelectionIndex());
        preferences.setDirectivePrefix(directivePrefix.getSelectionIndex());

        preferences.setIncludes(includes.getItems());
        preferences.setGenerateBinary(generateBinary.getSelection());
        preferences.setGenerateHex(generateHex.getSelection());
        preferences.setGenerateListing(generateListing.getSelection());
        preferences.setDownloadCommand(downloadCommand.getText());
        preferences.setXmodemCommand(xmodemCommand.getText());

        preferences.setRomAddress1(Integer.valueOf(romAddress1.getText(), 16));
        preferences.setRomImage1(romImage1.getText());
        preferences.setRomAddress2(Integer.valueOf(romAddress2.getText(), 16));
        preferences.setRomImage2(romImage2.getText());
        preferences.setCompactFlashImage(compactFlashImage.getText());

        super.okPressed();
    }
}
