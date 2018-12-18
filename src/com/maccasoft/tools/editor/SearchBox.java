/*
 * Copyright (c) 2018 Marco Maccaferri and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.maccasoft.tools.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SearchBox {

    Shell parentShell;
    StyledText control;

    Shell shell;
    Label label;
    Text text;

    static String lastSearch;
    int lastIndex;

    final TraverseListener traverseListener = new TraverseListener() {

        @Override
        public void keyTraversed(TraverseEvent e) {
        }
    };

    final ModifyListener modifyListener = new ModifyListener() {

        @Override
        public void modifyText(ModifyEvent e) {
            if (text.getText().length() == 0) {
                return;
            }
            text.getDisplay().timerExec(250, searchRunnable);
        }
    };

    final Runnable searchRunnable = new Runnable() {

        @Override
        public void run() {
            if (text.isDisposed()) {
                return;
            }
            searchNext(text.getText());
        }
    };

    final KeyListener keyListener = new KeyListener() {

        @Override
        public void keyReleased(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.stateMask != 0) {
                return;
            }
            if (e.character == 0x1B) {
                shell.setVisible(false);
                shell.dispose();
                e.doit = false;
                return;
            }
            if (e.character == 0x0D) {
                lastSearch = text.getText();
                searchNext();
                e.doit = false;
                return;
            }
        }
    };

    final FocusListener focusListener = new FocusListener() {

        @Override
        public void focusLost(FocusEvent e) {
            shell.setVisible(false);
            shell.dispose();
        }

        @Override
        public void focusGained(FocusEvent e) {
        }
    };

    public SearchBox(StyledText control) {
        this.control = control;
        this.parentShell = control.getShell();
    }

    public void setLabelText(String text) {
        label.setText(text);
    }

    public void setText(String text) {
        this.text.setText(text);
    }

    public void open() {
        shell = new Shell(parentShell, SWT.ON_TOP | SWT.TOOL);
        GridLayout rowLayout = new GridLayout(1, false);
        rowLayout.marginWidth = 5;
        rowLayout.marginHeight = 5;
        rowLayout.verticalSpacing = 2;
        shell.setLayout(rowLayout);
        shell.addTraverseListener(traverseListener);

        label = new Label(shell, SWT.NONE);
        label.setText("Search:");
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));

        text = new Text(shell, SWT.BORDER);
        text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        if (lastSearch != null) {
            text.setText(lastSearch);
            text.setSelection(0, lastSearch.length());
        }
        text.addKeyListener(keyListener);
        text.addFocusListener(focusListener);
        text.addModifyListener(modifyListener);

        Point size = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        if (size.x < 200) {
            size.x = 200;
        }
        Point location = control.toDisplay(control.getLocation());
        Point parentSize = control.getSize();
        shell.setBounds(location.x + parentSize.x - size.x - 10, location.y + parentSize.y - size.y - 10, size.x, size.y);

        shell.open();
        text.setFocus();
    }

    public void dispose() {
        if (shell != null && !shell.isDisposed()) {
            shell.dispose();
        }
    }

    void searchNext(String string) {
        lastSearch = string;
        searchNext(control.getCaretOffset());
    }

    public void searchNext() {
        searchNext(control.getCaretOffset() + 1);
    }

    public void searchNext(int fromIndex) {
        if (lastSearch == null) {
            return;
        }
        String search = lastSearch.toLowerCase();
        String controlText = control.getText().toLowerCase();
        int index = controlText.indexOf(search, fromIndex);
        if (index == -1) {
            index = controlText.indexOf(search);
        }
        if (index != -1) {
            control.setCaretOffset(index);
            control.setSelection(index + lastSearch.length(), index);

            int line = control.getLineAtOffset(index);
            control.setTopIndex(line > 10 ? line - 10 : 0);
        }
    }

    public void addDisposeListenr(DisposeListener l) {
        text.addDisposeListener(l);
    }

    public void removeDisposeListenr(DisposeListener l) {
        text.removeDisposeListener(l);
    }

    public void setLastSearch(String string) {
        lastSearch = string;
    }
}
