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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

public class StatusLine implements IProgressMonitor {

    Display display;
    Composite container;

    Label messageLabel;
    Label caretPositionLabel;
    ProgressIndicator progressBar;

    boolean canceled;

    public StatusLine(Composite parent) {
        display = parent.getDisplay();

        container = new Composite(parent, SWT.NONE);

        GC gc = new GC(parent);
        FontMetrics fontMetrics = gc.getFontMetrics();
        gc.dispose();

        messageLabel = new Label(container, SWT.NONE);
        messageLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        addSeparator();

        caretPositionLabel = new Label(container, SWT.CENTER);
        GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, false, false);
        layoutData.widthHint = Dialog.convertWidthInCharsToPixels(fontMetrics, 10);
        caretPositionLabel.setLayoutData(layoutData);

        addSeparator();

        Label label = new Label(container, SWT.NONE);
        layoutData = new GridData(SWT.FILL, SWT.CENTER, false, false);
        layoutData.widthHint = Dialog.convertWidthInCharsToPixels(fontMetrics, 35);
        label.setLayoutData(layoutData);

        addSeparator();

        progressBar = new ProgressIndicator(container, SWT.HORIZONTAL);
        layoutData = new GridData(GridData.GRAB_VERTICAL);
        layoutData.widthHint = 128;
        progressBar.setLayoutData(layoutData);

        GridLayout layout = new GridLayout(container.getChildren().length, false);
        layout.marginHeight = 0;
        container.setLayout(layout);
    }

    void addSeparator() {
        Label label = new Label(container, SWT.SEPARATOR);
        GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, false, true);
        layoutData.heightHint = 24;
        label.setLayoutData(layoutData);
    }

    public void setLayoutData(Object data) {
        container.setLayoutData(data);
    }

    public Object getLayoutData() {
        return container.getLayoutData();
    }

    public void setMessage(String text) {
        messageLabel.setText(text);
    }

    public void setCaretPosition(String text) {
        caretPositionLabel.setText(text);
    }

    public IProgressMonitor getProgressMonitor() {
        return this;
    }

    @Override
    public void beginTask(String name, final int totalWork) {
        canceled = false;

        final boolean animated = (totalWork == UNKNOWN || totalWork == 0);
        display.syncExec(new Runnable() {

            @Override
            public void run() {
                if (progressBar == null || progressBar.isDisposed()) {
                    return;
                }

                if (!progressBar.getVisible()) {
                    progressBar.setVisible(true);
                    container.layout();
                }
                if (!animated) {
                    progressBar.beginTask(totalWork);
                }
                else {
                    progressBar.beginAnimatedTask();
                }
            }
        });
    }

    @Override
    public void done() {
        display.syncExec(new Runnable() {

            @Override
            public void run() {
                if (progressBar == null || progressBar.isDisposed()) {
                    return;
                }
                progressBar.sendRemainingWork();
                progressBar.done();
            }
        });
    }

    @Override
    public void subTask(String name) {

    }

    @Override
    public void worked(int work) {
        internalWorked(work);
    }

    @Override
    public void internalWorked(final double work) {
        display.syncExec(new Runnable() {

            @Override
            public void run() {
                if (progressBar == null || progressBar.isDisposed()) {
                    return;
                }
                progressBar.worked(work);
            }
        });
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public void setCanceled(boolean value) {
        canceled = value;
    }

    @Override
    public void setTaskName(String name) {

    }
}
