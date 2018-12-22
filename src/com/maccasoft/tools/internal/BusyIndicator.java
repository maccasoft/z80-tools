/*
 * Copyright (c) 2018 Marco Maccaferri and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.maccasoft.tools.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class BusyIndicator {

    static int nextBusyId = 1;
    static final String BUSYID_NAME = "SWT BusyIndicator"; //$NON-NLS-1$
    static final String BUSY_CURSOR = "SWT BusyIndicator Cursor"; //$NON-NLS-1$

    public static void showWhile(Display display, Runnable runnable) {
        showWhile(display, runnable, false);
    }

    public static void showWhile(Display display, Runnable runnable, boolean fork) {
        if (runnable == null) {
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        }
        if (display == null) {
            return;
        }

        Integer busyId = new Integer(nextBusyId);
        nextBusyId++;

        Cursor cursor = display.getSystemCursor(SWT.CURSOR_WAIT);

        Shell[] shells = display.getShells();
        for (int i = 0; i < shells.length; i++) {
            Integer id = (Integer) shells[i].getData(BUSYID_NAME);
            if (id == null) {
                shells[i].setCursor(cursor);
                shells[i].setData(BUSYID_NAME, busyId);
            }
        }

        Runnable busyRunnable = new Runnable() {

            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {
                    display.syncExec(new Runnable() {

                        @Override
                        public void run() {
                            Shell[] shells = display.getShells();
                            for (int i = 0; i < shells.length; i++) {
                                Integer id = (Integer) shells[i].getData(BUSYID_NAME);
                                if (id == busyId) {
                                    shells[i].setCursor(null);
                                    shells[i].setData(BUSYID_NAME, null);
                                }
                            }
                        }

                    });
                }
            }

        };
        if (fork) {
            new Thread(busyRunnable).start();
        }
        else {
            busyRunnable.run();
        }

    }

}
