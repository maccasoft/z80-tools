/*
 * Copyright (c) 2018 Marco Maccaferri and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.maccasoft.tools;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;

import nl.grauw.glass.Line;
import nl.grauw.glass.Source;

public class CodeRuler {

    Canvas canvas;
    GridData layoutData;
    FontMetrics fontMetrics;

    StyledText text;
    Source source;

    int leftMargin;
    int rightMargin;

    private int scrollBarSelection;
    private int lineCount;
    private int currentLine;
    private Color currentLineBackground;

    final PaintListener paintListener = new PaintListener() {

        @Override
        public void paintControl(PaintEvent e) {
            if (text != null) {
                onPaintControl(e.gc);
            }
        }
    };

    final PaintListener textPaintListener = new PaintListener() {

        @Override
        public void paintControl(PaintEvent e) {
            ScrollBar scrollBar = text.getVerticalBar();
            if (scrollBarSelection != scrollBar.getSelection() || lineCount != text.getLineCount()) {
                canvas.redraw();
                scrollBarSelection = scrollBar.getSelection();
                lineCount = text.getLineCount();
            }
        }
    };

    private final CaretListener caretListener = new CaretListener() {

        @Override
        public void caretMoved(CaretEvent event) {
            int offset = text.getCaretOffset();
            int line = text.getLineAtOffset(offset);
            if (line != currentLine) {
                currentLine = line;
            }
            canvas.redraw();
        }
    };

    public CodeRuler(Composite parent) {
        canvas = new Canvas(parent, SWT.NO_FOCUS);
        canvas.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        canvas.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
        canvas.setLayoutData(layoutData = new GridData(SWT.FILL, SWT.FILL, false, true));
        canvas.addPaintListener(paintListener);

        GC gc = new GC(canvas);
        fontMetrics = gc.getFontMetrics();
        gc.dispose();

        leftMargin = rightMargin = 5;

        layoutData.widthHint = leftMargin + fontMetrics.getAverageCharWidth() * (4 + 1 + 12) + rightMargin;

        scrollBarSelection = lineCount = -1;

        currentLine = 0;
        currentLineBackground = new Color(Display.getDefault(), 232, 242, 254);
    }

    public void setText(StyledText text) {
        this.text = text;
        this.text.addPaintListener(textPaintListener);
        this.text.addCaretListener(caretListener);
    }

    public void setSource(Source source) {
        this.source = source;
    }

    void onPaintControl(GC gc) {
        Rectangle rect = canvas.getClientArea();

        int lineNumber = text.getTopIndex() - 1;
        if (lineNumber < 0) {
            lineNumber = 0;
        }
        while (lineNumber < text.getLineCount()) {
            int y = text.getLinePixel(lineNumber);
            if (y >= rect.height) {
                break;
            }

            if (source != null && lineNumber >= 0 && lineNumber < source.getLines().size()) {
                Line line = source.getLines().get(lineNumber);
                byte[] code = line.getBytes();

                StringBuilder sb = new StringBuilder();
                sb.append(String.format("%04X ", line.getScope().getAddress()));
                for (int i = 0; i < code.length; i++) {
                    if (i != 0) {
                        sb.append(' ');
                    }
                    sb.append(String.format("%02X", code[i]));
                }
                if (lineNumber == currentLine) {
                    gc.setBackground(currentLineBackground);
                    gc.fillRectangle(leftMargin, y, rect.width - leftMargin - rightMargin, text.getLineHeight());
                }
                else {
                    gc.setBackground(canvas.getBackground());
                }
                gc.drawString(sb.toString(), leftMargin, y, true);
            }

            lineNumber++;
        }
    }

    public void setFont(Font font) {
        canvas.setFont(font);

        GC gc = new GC(canvas);
        fontMetrics = gc.getFontMetrics();
        gc.dispose();

        layoutData.widthHint = leftMargin + fontMetrics.getAverageCharWidth() * (4 + 1 + 12) + rightMargin;

        canvas.redraw();
    }

    public void setVisible(boolean visible) {
        canvas.setVisible(visible);
        layoutData.exclude = !visible;
    }

    public void redraw() {
        canvas.redraw();
    }
}
