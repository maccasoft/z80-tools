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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import junit.framework.TestCase;
import junit.framework.TestResult;

public abstract class DatabindingTestCase extends TestCase {

    private Collection<Shell> capturedShells;

    private boolean displayOwner;
    private Display display;

    public DatabindingTestCase() {

    }

    public DatabindingTestCase(String name) {
        super(name);
    }

    @Override
    public void run(final TestResult result) {
        ErrorDialog.AUTOMATED_MODE = true;

        capturedShells = new LinkedList<Shell>();
        capturedShells.addAll(Arrays.asList(captureShells()));

        Realm.runWithDefault(DisplayRealm.getRealm(getDisplay()), new Runnable() {

            @Override
            public void run() {
                DatabindingTestCase.super.run(result);
            }
        });

        dispose();

        ErrorDialog.AUTOMATED_MODE = false;
    }

    private static Shell[] captureShells() {
        Shell[] result = new Shell[0];
        Display currentDisplay = Display.getCurrent();
        if (currentDisplay != null) {
            result = currentDisplay.getShells();
        }
        return result;
    }

    public Display getDisplay() {
        if (display == null) {
            displayOwner = Display.getCurrent() == null;
            display = Display.getDefault();
        }
        return display;
    }

    public void dispose() {
        flushPendingEvents();
        disposeNewShells();
        disposeDisplay();
    }

    public void flushPendingEvents() {
        while (Display.getCurrent() != null
            && !Display.getCurrent().isDisposed()
            && Display.getCurrent().readAndDispatch()) {
        }
    }

    private void disposeNewShells() {
        Shell[] newShells = getNewShells();
        for (Shell shell : newShells) {
            shell.dispose();
        }
    }

    private void disposeDisplay() {
        if (display != null && displayOwner) {
            if (display.isDisposed()) {
                display.dispose();
            }
            display = null;
        }
    }

    private Shell[] getNewShells() {
        Collection<Shell> newShells = new LinkedList<Shell>();
        Shell[] shells = captureShells();
        for (Shell shell : shells) {
            if (!capturedShells.contains(shell)) {
                newShells.add(shell);
            }
        }
        return newShells.toArray(new Shell[newShells.size()]);
    }

    public Shell createShell() {
        return createShell(SWT.NONE);
    }

    public Shell createShell(int style) {
        return new Shell(getDisplay(), style);
    }
}
