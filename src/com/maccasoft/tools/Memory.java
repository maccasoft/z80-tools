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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;

public class Memory {

    public static final int BYTES_PER_ROW = 8;

    Display display;
    Canvas canvas;
    ScrollBar verticalBar;

    int marginHeight;
    int marginWidth;

    Font font;
    FontMetrics fontMetrics;

    byte[] data;
    byte[] dataUpdate;
    boolean needUpdate;

    final PaintListener paintListener = new PaintListener() {

        @Override
        public void paintControl(PaintEvent e) {
            needUpdate = false;

            if (data == null) {
                return;
            }

            Color normal = display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
            Color highlight = display.getSystemColor(SWT.COLOR_RED);
            Rectangle rect = canvas.getClientArea();

            int y = marginHeight;
            int addr = canvas.getVerticalBar().getSelection();
            while (addr < data.length && y < rect.height) {
                int x1 = marginWidth + 5 * fontMetrics.getAverageCharWidth();
                int x2 = x1 + BYTES_PER_ROW * (3 * fontMetrics.getAverageCharWidth());

                e.gc.setForeground(normal);
                e.gc.drawString(String.format("%04X", addr), marginWidth, y, true);

                for (int i = 0; i < BYTES_PER_ROW && addr < data.length; i++) {
                    e.gc.setForeground(dataUpdate[addr] != 0 ? highlight : normal);

                    e.gc.drawString(String.format("%02X", data[addr] & 0xFF), x1, y, true);
                    if (data[addr] >= 0x20 && data[addr] <= 0x7F) {
                        e.gc.drawString(String.format("%c", data[addr] & 0xFF), x2, y, true);
                    }
                    else {
                        e.gc.drawString(".", x2, y, true);
                    }

                    x1 += fontMetrics.getAverageCharWidth() * 3;
                    x2 += fontMetrics.getAverageCharWidth();
                    addr++;
                }
                y += fontMetrics.getHeight();
            }
        }
    };

    public Memory(Composite parent) {
        display = parent.getDisplay();

        if ("win32".equals(SWT.getPlatform())) {
            font = new Font(Display.getDefault(), "Courier New", 9, SWT.NONE);
        }
        else {
            font = new Font(Display.getDefault(), "mono", 9, SWT.NONE);
        }

        canvas = new Canvas(parent, SWT.V_SCROLL | SWT.BORDER) {

            @Override
            public Point computeSize(int wHint, int hHint, boolean changed) {
                return super.computeSize(fontMetrics.getAverageCharWidth() * (4 + 1 + 3 * BYTES_PER_ROW + BYTES_PER_ROW) + marginWidth * 2, hHint, changed);
            }

        };
        canvas.setFont(font);
        canvas.setBackground(display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        canvas.setForeground(display.getSystemColor(SWT.COLOR_LIST_FOREGROUND));

        GC gc = new GC(canvas);
        fontMetrics = gc.getFontMetrics();
        gc.dispose();

        canvas.addPaintListener(paintListener);
        canvas.addControlListener(new ControlListener() {

            @Override
            public void controlResized(ControlEvent e) {
                if (data == null) {
                    return;
                }
                int rows = (((Canvas) e.widget).getClientArea().height - marginHeight * 2) / fontMetrics.getHeight();
                int selection = verticalBar.getSelection();
                verticalBar.setValues(selection, 0, data.length, rows * BYTES_PER_ROW, BYTES_PER_ROW, rows * BYTES_PER_ROW);
            }

            @Override
            public void controlMoved(ControlEvent e) {
            }
        });
        canvas.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.HOME) {
                    if (verticalBar.getSelection() > 0) {
                        verticalBar.setSelection(0);
                        canvas.redraw();
                    }
                }
                if (e.keyCode == SWT.PAGE_UP) {
                    if (verticalBar.getSelection() > 0) {
                        verticalBar.setSelection(verticalBar.getSelection() - verticalBar.getPageIncrement());
                        canvas.redraw();
                    }
                }
                if (e.keyCode == SWT.ARROW_UP) {
                    if (verticalBar.getSelection() > 0) {
                        verticalBar.setSelection(verticalBar.getSelection() - verticalBar.getIncrement());
                        canvas.redraw();
                    }
                }
                if (e.keyCode == SWT.ARROW_DOWN) {
                    if ((verticalBar.getSelection() + verticalBar.getThumb()) < verticalBar.getMaximum()) {
                        verticalBar.setSelection(verticalBar.getSelection() + verticalBar.getIncrement());
                        canvas.redraw();
                    }
                }
                if (e.keyCode == SWT.PAGE_DOWN) {
                    if ((verticalBar.getSelection() + verticalBar.getThumb()) < verticalBar.getMaximum()) {
                        verticalBar.setSelection(verticalBar.getSelection() + verticalBar.getPageIncrement());
                        canvas.redraw();
                    }
                }
                if (e.keyCode == SWT.END) {
                    if ((verticalBar.getSelection() + verticalBar.getThumb()) < verticalBar.getMaximum()) {
                        verticalBar.setSelection(verticalBar.getMaximum() - verticalBar.getThumb());
                        canvas.redraw();
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        marginHeight = marginWidth = 5;

        verticalBar = canvas.getVerticalBar();
        verticalBar.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int selection = verticalBar.getSelection();
                verticalBar.setSelection(((selection + (BYTES_PER_ROW / 2)) / BYTES_PER_ROW) * BYTES_PER_ROW);
                canvas.redraw();
            }
        });
    }

    public Control getControl() {
        return canvas;
    }

    public void poke(int addr, int value) {
        if (addr >= verticalBar.getSelection() && addr <= (verticalBar.getSelection() + verticalBar.getThumb())) {
            if (data[addr] != value || dataUpdate[addr] != 1) {
                needUpdate = true;
            }
        }
        data[addr] = (byte) value;
        dataUpdate[addr] = 1;
    }

    public void clearUpdates() {
        int rangeStart = verticalBar.getSelection();
        int rangeEnd = verticalBar.getSelection() + verticalBar.getThumb();
        for (int i = 0; i < dataUpdate.length; i++) {
            if (dataUpdate[i] != 0) {
                if (i >= rangeStart && i <= rangeEnd) {
                    needUpdate = true;
                }
                dataUpdate[i] = 0;
            }
        }
    }

    public Rectangle getBounds() {
        return canvas.getBounds();
    }

    public void setData(byte[] data) {
        this.data = data;
        this.dataUpdate = new byte[data.length];

        int rows = (canvas.getClientArea().height - marginHeight * 2) / fontMetrics.getHeight();
        int selection = verticalBar.getSelection();
        verticalBar.setValues(selection, 0, data.length, rows * BYTES_PER_ROW, BYTES_PER_ROW, rows * BYTES_PER_ROW);

        canvas.redraw();
    }

    public void update() {
        if (needUpdate) {
            canvas.redraw();
        }
    }

    public void setSelection(int addr) {
        if (addr < 0 || addr > 65535) {
            addr = 0;
        }
        addr = ((addr + (BYTES_PER_ROW / 2)) / BYTES_PER_ROW) * BYTES_PER_ROW;
        verticalBar.setSelection(addr);
    }

}
