/*
 * Copyright (c) 2018-19 Marco Maccaferri and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.maccasoft.tools;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public abstract class TMS9918Terminal {

    Display display;
    Canvas canvas;

    Image image;

    final Runnable redrawRunnable = new Runnable() {

        @Override
        public void run() {
            pendingRedraw.set(false);
            if (canvas == null || canvas.isDisposed()) {
                return;
            }
            if (image != null) {
                image.dispose();
            }
            image = new Image(display, getImageData());
            canvas.redraw();
            canvas.update();
        }
    };

    final AtomicBoolean pendingRedraw = new AtomicBoolean();

    public TMS9918Terminal(Composite parent) {
        display = parent.getDisplay();

        canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);
        canvas.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
        canvas.setBounds(canvas.computeTrim(0, 0, TMS9918.FRAME_WIDTH, TMS9918.FRAME_HEIGHT));

        canvas.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                if (image != null) {
                    image.dispose();
                }
                image = null;
            }
        });

        canvas.addPaintListener(new PaintListener() {

            @Override
            public void paintControl(PaintEvent e) {
                if (image == null || image.isDisposed()) {
                    return;
                }
                Rectangle bounds = canvas.getBounds();
                e.gc.setAdvanced(false);
                e.gc.setAntialias(SWT.OFF);
                e.gc.setInterpolation(SWT.NONE);
                e.gc.drawImage(image, 0, 0, TMS9918.FRAME_WIDTH, TMS9918.FRAME_HEIGHT, 0, 0, bounds.width, bounds.height);
            }
        });
    }

    protected abstract ImageData getImageData();

    protected void redraw() {
        if (pendingRedraw.compareAndSet(false, true)) {
            display.asyncExec(redrawRunnable);
        }
    }

    public void setLayoutData(Object data) {
        canvas.setLayoutData(data);
    }

    public Object getLayoutData() {
        return canvas.getLayoutData();
    }

    public Rectangle getBounds() {
        return canvas.getBounds();
    }

    public void setFocus() {
        canvas.setFocus();
    }

    public void addKeyListener(KeyListener l) {
        canvas.addKeyListener(l);
    }

    public void removeKeyListener(KeyListener l) {
        canvas.removeKeyListener(l);
    }

    public void dispose() {
        canvas.dispose();
    }

}
