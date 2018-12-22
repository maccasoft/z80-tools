/*
 * Copyright (c) 2018 Marco Maccaferri and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.maccasoft.tools;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.maccasoft.tools.internal.ImageRegistry;

public class PreferencesDialog extends Dialog {

    List roots;
    Button rootAdd;
    Button rootRemove;
    Button rootMoveUp;
    Button rootMoveDown;

    Text editorFont;
    Button editorFontBrowse;
    Button showLineNumbers;
    Button reloadOpenTabs;

    Button generateBinary;
    Button generateHex;
    Button generateListing;

    Preferences preferences;
    String defaultFont;

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
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        applyDialogFont(composite);

        if ("win32".equals(SWT.getPlatform())) {
            defaultFont = StringConverter.asString(new FontData("Courier New", 9, SWT.NONE));
        }
        else {
            defaultFont = StringConverter.asString(new FontData("mono", 9, SWT.NONE));
        }

        Control control = createRootDirectoryGroup(composite);
        control.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        addSeparator(composite);

        Label label = new Label(composite, SWT.NONE);
        label.setText("Editor font:");

        Composite container = new Composite(composite, SWT.NONE);
        layout = new GridLayout(2, false);
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

        new Label(composite, SWT.NONE);

        reloadOpenTabs = new Button(composite, SWT.CHECK);
        reloadOpenTabs.setText("Reload open tabs");
        reloadOpenTabs.setSelection(preferences.isReloadOpenTabs());

        addSeparator(composite);

        label = new Label(composite, SWT.NONE);
        label.setText("Compiler output");

        Composite group = new Composite(composite, SWT.NONE);
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

        addSeparator(composite);

        label = new Label(composite, SWT.NONE);
        label.setLayoutData(new GridData(SWT.DEFAULT, convertHeightInCharsToPixels(5)));

        return composite;
    }

    Control createRootDirectoryGroup(Composite parent) {
        Composite group = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = layout.marginHeight = 0;
        group.setLayout(layout);

        Label label = new Label(group, SWT.NONE);
        label.setText("File browser root paths");
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        roots = new List(group, SWT.SINGLE | SWT.BORDER);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = convertWidthInCharsToPixels(60);
        gridData.heightHint = roots.getItemHeight() * 5 + roots.getBorderWidth() * 2;
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

        rootAdd = new Button(container, SWT.PUSH);
        rootAdd.setImage(ImageRegistry.getImageFromResources("add.png"));
        rootAdd.setToolTipText("Add");
        rootAdd.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dlg = new DirectoryDialog(getShell());
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

        return group;
    }

    void updateRootDirectoryButtons() {
        int index = roots.getSelectionIndex();
        rootRemove.setEnabled(index != -1);
        rootMoveUp.setEnabled(index != -1 && index > 0);
        rootMoveDown.setEnabled(index != -1 && index < (roots.getItemCount() - 1));
    }

    void addSeparator(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, ((GridLayout) parent.getLayout()).numColumns, 1));
        ((GridData) label.getLayoutData()).heightHint = ((GridLayout) parent.getLayout()).marginTop;
    }

    @Override
    protected void okPressed() {
        preferences.setRoots(roots.getItems());

        preferences.setEditorFont(editorFont.getText().equals(defaultFont) ? null : editorFont.getText());
        preferences.setShowLineNumbers(showLineNumbers.getSelection());
        preferences.setReloadOpenTabs(reloadOpenTabs.getSelection());

        preferences.setGenerateBinary(generateBinary.getSelection());
        preferences.setGenerateHex(generateHex.getSelection());
        preferences.setGenerateListing(generateListing.getSelection());

        super.okPressed();
    }
}
