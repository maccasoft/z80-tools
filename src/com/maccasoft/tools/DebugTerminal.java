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

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

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

public class DebugTerminal extends Window {

    Display display;
    Composite container;
    Terminal term;

    Combo cursorKeys;

    PipedOutputStream os;
    PipedInputStream is;

    public DebugTerminal() {
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
        newShell.setText("Debug Terminal");

        newShell.setData(this);
    }

    @Override
    protected Control createContents(Composite parent) {
        display = parent.getDisplay();

        container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = layout.marginHeight = 0;
        container.setLayout(layout);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        term = new Terminal(container) {

            @Override
            protected void writeByte(byte b) {
                try {
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

        layout.numColumns = container.getChildren().length;
    }

    public void setFocus() {
        term.setFocus();
    }

    public void dispose() {
        getShell().dispose();
    }

    public void write(int b) {
        term.write(b);
    }

    public PipedOutputStream getOutputStream() {
        return os;
    }

    public PipedInputStream getInputStream() {
        return is;
    }

}
