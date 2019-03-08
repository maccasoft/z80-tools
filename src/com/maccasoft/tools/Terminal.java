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

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class Terminal {

    public static final int CURSOR_BLINK_MS = 250;
    public static final int REDRAW_MS = 25;

    public static final int CURSOR_OFF = 0x00;
    public static final int CURSOR_ON = 0x04;
    public static final int CURSOR_ULINE = 0x02;
    public static final int CURSOR_BLOCK = 0x00;
    public static final int CURSOR_FLASH = 0x01;
    public static final int CURSOR_SOLID = 0x00;

    public static final int CURSORS_VT100 = 0;
    public static final int CURSORS_WORDSTAR = 1;

    Display display;
    Canvas canvas;
    Image image;
    PaletteData paletteData;
    Rectangle bounds;

    TerminalFont font;
    Color[] colors;
    int cursorKeys;

    int cx;
    int cy;
    int foreground;
    int background;
    int cursor;

    int argc;
    int[] args;
    int state;
    int savedCx;
    int savedCy;

    boolean cursorState;

    final Runnable cursorBlinkRunnable = new Runnable() {

        @Override
        public void run() {
            if (canvas.isDisposed() || bounds == null) {
                return;
            }
            if ((cursor & CURSOR_FLASH) != 0) {
                cursorState = !cursorState;
                canvas.redraw(cx, cy, font.getWidth(), font.getHeight(), false);
            }
            display.timerExec(CURSOR_BLINK_MS, this);
        }
    };

    final Runnable redrawRunnable = new Runnable() {

        @Override
        public void run() {
            if (canvas.isDisposed() || bounds == null) {
                return;
            }
            canvas.redraw();
        }
    };

    Terminal() {
        // For JUnit tests
    }

    public Terminal(Composite parent) {
        display = parent.getDisplay();

        canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);
        canvas.setBackground(display.getSystemColor(SWT.COLOR_BLACK));

        font = new TerminalFont(8, 16);

        paletteData = new PaletteData(new RGB[] {
            new RGB(0, 0, 0),
            new RGB(170, 0, 0),
            new RGB(0, 170, 0),
            new RGB(170, 170, 0),
            new RGB(0, 0, 170),
            new RGB(170, 0, 170),
            new RGB(0, 170, 170),
            new RGB(170, 170, 170),
            new RGB(85, 85, 85),
            new RGB(255, 0, 0),
            new RGB(0, 255, 0),
            new RGB(255, 255, 0),
            new RGB(0, 0, 255),
            new RGB(255, 0, 255),
            new RGB(0, 255, 255),
            new RGB(255, 255, 255),
        });

        colors = new Color[paletteData.colors.length];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = new Color(display, paletteData.colors[i]);
        }

        bounds = canvas.computeTrim(0, 0, font.getWidth() * 80, font.getHeight() * 25);

        int bytesPerLine = (((bounds.width * 8 + 7) / 8) + (4 - 1)) / 4 * 4;
        image = new Image(display, new ImageData(bounds.width, bounds.height, 8, paletteData, 4, new byte[bytesPerLine * bounds.height]));

        canvas.setBounds(bounds);

        canvas.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                display.timerExec(-1, cursorBlinkRunnable);
                font.dispose();
                for (int i = 0; i < colors.length; i++) {
                    colors[i].dispose();
                }
            }
        });

        canvas.addControlListener(new ControlAdapter() {

            @Override
            public void controlResized(ControlEvent e) {
                Point size = canvas.getSize();

                int bytesPerLine = (((size.x * 8 + 7) / 8) + (4 - 1)) / 4 * 4;
                Image newImage = new Image(display, new ImageData(size.x, size.y, 8, paletteData, 4, new byte[bytesPerLine * size.y]));
                Rectangle newImageBounds = newImage.getBounds();

                GC gc = new GC(newImage);
                try {
                    gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
                    gc.fillRectangle(newImageBounds);
                    if (image != null && !image.isDisposed()) {
                        gc.drawImage(image, 0, 0);
                    }
                } finally {
                    gc.dispose();
                }

                if (image != null) {
                    image.dispose();
                }
                image = newImage;
                bounds = newImageBounds;
                bounds.width -= (bounds.width % Terminal.this.font.getWidth());
                bounds.height -= (bounds.height % Terminal.this.font.getHeight());
            }
        });

        canvas.addPaintListener(new PaintListener() {

            @Override
            public void paintControl(PaintEvent e) {
                if (image == null || image.isDisposed()) {
                    return;
                }
                e.gc.drawImage(image, e.x, e.y, e.width, e.height, e.x, e.y, e.width, e.height);
                if (cursorState && (cursor & CURSOR_ON) != 0) {
                    int h;
                    int w = Math.min(font.getWidth(), bounds.width - cx);

                    if ((cursor & CURSOR_ULINE) != 0) {
                        h = Math.min(font.getHeight() / 4, bounds.height - cy);
                    }
                    else {
                        h = Math.min(font.getHeight(), bounds.height - cy);
                    }
                    e.gc.setBackground(colors[foreground]);
                    e.gc.fillRectangle(cx, cy + font.getHeight() - h, w, h);
                }
            }
        });

        canvas.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent event) {
                try {
                    if (event.keyCode == SWT.INSERT && (event.stateMask & SWT.MOD2) != 0) {
                        pasteFromClipboard();
                        return;
                    }
                    switch (cursorKeys) {
                        case CURSORS_VT100:
                            onVT100KeyPressed(event);
                            break;
                        case CURSORS_WORDSTAR:
                            onWordStarKeyPressed(event);
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        state = 0;
        argc = 0;
        args = new int[32];
        foreground = 7;
        background = 0;
        cursor = CURSOR_ON | CURSOR_FLASH | CURSOR_ULINE;

        font.setForeground(paletteData.colors[foreground]);
        font.setBackground(paletteData.colors[background]);

        cursorState = (cursor & CURSOR_ON) != 0 && (cursor & CURSOR_FLASH) == 0;

        display.timerExec(CURSOR_BLINK_MS, cursorBlinkRunnable);
    }

    public int getCursorKeys() {
        return cursorKeys;
    }

    public void setCursorKeys(int cursorKeys) {
        this.cursorKeys = cursorKeys;
    }

    void onVT100KeyPressed(KeyEvent e) {
        switch (e.keyCode) {
            case SWT.ARROW_UP:
                writeByte((byte) 0x1B);
                writeByte((byte) 'A');
                break;
            case SWT.ARROW_DOWN:
                writeByte((byte) 0x1B);
                writeByte((byte) 'B');
                break;
            case SWT.ARROW_LEFT:
                writeByte((byte) 0x1B);
                writeByte((byte) 'D');
                break;
            case SWT.ARROW_RIGHT:
                writeByte((byte) 0x1B);
                writeByte((byte) 'C');
                break;
            case SWT.HOME:
                writeByte((byte) 0x1B);
                writeByte((byte) 'H');
                break;
            case SWT.END:
                writeByte((byte) 0x1B);
                writeByte((byte) 'K');
                break;
            case SWT.F1:
            case SWT.F2:
            case SWT.F3:
            case SWT.F4:
            case SWT.F5:
            case SWT.F6:
            case SWT.F7:
            case SWT.F8:
            case SWT.F9:
            case SWT.F10:
                writeByte((byte) 0x1B);
                writeByte((byte) 'O');
                writeByte((byte) ('P' + (e.keyCode - SWT.F1)));
                break;
            default:
                if (e.character != 0) {
                    writeByte((byte) e.character);
                }
                break;
        }
    }

    void onWordStarKeyPressed(KeyEvent e) {
        switch (e.keyCode) {
            case SWT.ARROW_UP:
                writeByte((byte) 5);
                break;
            case SWT.ARROW_DOWN:
                writeByte((byte) 24);
                break;
            case SWT.ARROW_LEFT:
                if ((e.stateMask & SWT.MOD2) != 0) {
                    writeByte((byte) 1);
                }
                else {
                    writeByte((byte) 19);
                }
                break;
            case SWT.ARROW_RIGHT:
                if ((e.stateMask & SWT.MOD2) != 0) {
                    writeByte((byte) 6);
                }
                else {
                    writeByte((byte) 4);
                }
                break;
            case SWT.INSERT:
                writeByte((byte) 22);
                break;
            case SWT.HOME:
                writeByte((byte) 17);
                writeByte((byte) 'S');
                break;
            case SWT.END:
                writeByte((byte) 17);
                writeByte((byte) 'D');
                break;
            case SWT.PAGE_UP:
                writeByte((byte) 18);
                break;
            case SWT.PAGE_DOWN:
                writeByte((byte) 3);
                break;
            default:
                if (e.character == SWT.DEL) {
                    writeByte((byte) 7);
                }
                else if (e.character != 0) {
                    writeByte((byte) e.character);
                }
                break;
        }
    }

    public void pasteFromClipboard() {
        Clipboard clipboard = new Clipboard(display);
        try {
            String s = (String) clipboard.getContents(TextTransfer.getInstance());
            if (s != null) {
                final byte[] b = s.replaceAll("(\r\n|\n|\r)", "\r").getBytes();
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            for (int i = 0; i < b.length; i++) {
                                writeByte(b[i]);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }).start();
            }
        } finally {
            clipboard.dispose();
        }
    }

    protected void writeByte(byte b) {

    }

    public void setLayoutData(Object data) {
        canvas.setLayoutData(data);
    }

    public Object getLayoutData() {
        return canvas.getLayoutData();
    }

    public TerminalFont getFont() {
        return font;
    }

    public void setFont(TerminalFont font) {
        this.font = font;
    }

    public Rectangle getBounds() {
        return canvas.getBounds();
    }

    public void write(int c) {
        display.syncExec(new Runnable() {

            @Override
            public void run() {
                GC gc = new GC(image);
                try {
                    write(gc, c);
                } finally {
                    gc.dispose();
                }
                display.timerExec(REDRAW_MS, redrawRunnable);
            }
        });
    }

    void write(GC gc, int c) {
        int p0;

        if (state == 0) {
            if (c == 0x1B) {
                state = 1;
                argc = -1;
                args[0] = 0;
                return;
            }
        }
        if (state == 1) {
            state = (c == '[') ? (state + 1) : 0;
            return;
        }
        if (state == 2) {
            if (c == '?') {
                state = state + 2;
                return;
            }
            state++;
        }
        if (state == 3 || state == 4) {
            if (c >= '0' && c <= '9') {
                if (argc == -1) {
                    argc++;
                    args[argc] = 0;
                }
                args[argc] = args[argc] * 10 + (c - '0');
                return;
            }
            if (c == ';') {
                argc++;
                args[argc] = 0;
                return;
            }
            argc++;
        }
        if (state == 3) {
            switch (c) {
                case 'A':
                    cy -= (argc == 0 || args[0] == 0 ? 1 : args[0]) * font.getHeight();
                    if (cy < 0) {
                        cy = 0;
                    }
                    break;
                case 'B':
                    cy += (argc == 0 || args[0] == 0 ? 1 : args[0]) * font.getHeight();
                    while (cy >= bounds.height) {
                        cy -= font.getHeight();
                    }
                    break;
                case 'C':
                    cx += (argc == 0 || args[0] == 0 ? 1 : args[0]) * font.getWidth();
                    while (cx >= bounds.width) {
                        cx -= font.getWidth();
                    }
                    break;
                case 'D':
                    cx -= (argc == 0 || args[0] == 0 ? 1 : args[0]) * font.getWidth();
                    if (cx < 0) {
                        cx = 0;
                    }
                    break;
                case 'H':
                case 'f':
                    if (argc == 0) {
                        cx = cy = 0;
                    }
                    else if (argc >= 2) {
                        cy = (args[0] > 0 ? (args[0] - 1) : 0) * font.getHeight();
                        cx = (args[1] > 0 ? (args[1] - 1) : 0) * font.getWidth();
                    }
                    break;
                case 'J': {
                    gc.setBackground(colors[background]);
                    if (argc == 0 || args[0] == 0) {
                        gc.fillRectangle(cx, cy, bounds.width - cx, bounds.height - cy);
                    }
                    else if (args[0] == 1) {
                        gc.fillRectangle(0, 0, cx + font.getWidth() - 1, cy + font.getHeight() - 1);
                    }
                    else if (args[0] == 2) {
                        gc.fillRectangle(0, 0, bounds.width, bounds.height);
                    }
                    break;
                }
                case 'K': {
                    gc.setBackground(colors[background]);
                    if (argc == 0 || args[0] == 0) {
                        gc.fillRectangle(cx, cy, bounds.width - cx, font.getHeight() - 1);
                    }
                    else if (args[0] == 1) {
                        gc.fillRectangle(0, cy, cx, font.getHeight() - 1);
                    }
                    else if (args[0] == 2) {
                        gc.fillRectangle(0, 0, bounds.width, font.getHeight() - 1);
                    }
                    break;
                }
                case 'L': {
                    Image temp = new Image(image.getDevice(), bounds.width, bounds.height - cy - font.getHeight());
                    gc.copyArea(temp, 0, cy);
                    gc.drawImage(temp, 0, cy + font.getHeight());
                    gc.setBackground(colors[background]);
                    gc.fillRectangle(0, cy, bounds.width, font.getHeight());
                    temp.dispose();
                    break;
                }
                case 'M': {
                    gc.copyArea(0, cy + font.getHeight(), bounds.width, bounds.height - cy - font.getHeight(), 0, cy);
                    gc.setBackground(colors[background]);
                    gc.fillRectangle(0, bounds.height - font.getHeight(), bounds.width, font.getHeight());
                    break;
                }
                case 'm': {
                    for (int i = 0; i < argc; i++) {
                        if (args[i] == 0) {
                            background = 0;
                            foreground = 7;
                        }
                        else if (args[i] == 1) {
                            foreground |= 8;
                        }
                        else if (args[i] == 2) {
                            foreground &= 7;
                        }
                        else if (args[i] == 5) {
                            cursor |= CURSOR_FLASH;
                        }
                        else if (args[i] == 7) {
                            int t = foreground;
                            foreground = background;
                            background = t & 0x07;
                        }
                        else if (args[i] == 25) {
                            cursor &= ~CURSOR_FLASH;
                        }
                        else if (args[i] >= 30 && args[i] <= 37) {
                            foreground = (foreground & 0x08) | (args[i] - 30);
                        }
                        else if (args[i] >= 40 && args[i] <= 47) {
                            background = args[i] - 40;
                        }
                    }
                    if (argc == 0) {
                        background = 0;
                        foreground = 7;
                    }
                    font.setForeground(paletteData.colors[foreground]);
                    font.setBackground(paletteData.colors[background]);
                    break;
                }
                case 's':
                    savedCx = cx;
                    savedCy = cy;
                    break;
                case 'u':
                    cx = savedCx;
                    cy = savedCy;
                    break;
                case 'q':
                    if (argc == 0) {
                        cursor = CURSOR_ON | CURSOR_FLASH | CURSOR_BLOCK;
                    }
                    else if (args[0] == 0 || args[0] == 1) {
                        cursor = CURSOR_ON | CURSOR_FLASH | CURSOR_BLOCK;
                    }
                    else if (args[0] == 2) {
                        cursor = CURSOR_ON | CURSOR_SOLID | CURSOR_BLOCK;
                    }
                    else if (args[0] == 3) {
                        cursor = CURSOR_ON | CURSOR_FLASH | CURSOR_ULINE;
                    }
                    else if (args[0] == 4) {
                        cursor = CURSOR_ON | CURSOR_SOLID | CURSOR_ULINE;
                    }
                    break;
            }
            state = 0;
            return;
        }
        if (state == 4) {
            switch (c) {
                case 'h':
                    if (argc >= 1) {
                        switch (args[0]) {
                            case 12:
                                cursor |= CURSOR_FLASH;
                                break;
                            case 25:
                                cursor |= CURSOR_ON;
                                break;
                        }
                    }
                    break;
                case 'l':
                    if (argc >= 1) {
                        switch (args[0]) {
                            case 12:
                                cursor &= ~CURSOR_FLASH;
                                break;
                            case 25:
                                cursor &= ~CURSOR_ON;
                                break;
                        }
                    }
                    break;
            }
            state = 0;
            return;
        }
        switch (c) {
            case 0x08:
                if (cx >= font.getWidth()) {
                    cx -= font.getWidth();
                }
                else {
                    cx = 0;
                }
                break;
            case 0x09:
                p0 = cx / font.getWidth();
                p0 = ((p0 / 8) + 1) * 8;
                if (p0 > (bounds.width / font.getWidth())) {
                    p0 = 0;
                }
                cx = p0 * font.getWidth();
                break;
            case 0x0A:
                cy += font.getHeight();
                if (cy >= bounds.height) {
                    gc.copyArea(0, font.getHeight(), bounds.width, bounds.height - font.getHeight(), 0, 0);
                    gc.setBackground(colors[background]);
                    gc.fillRectangle(0, bounds.height - font.getHeight(), bounds.width, font.getHeight());
                    cy -= font.getHeight();
                }
                break;
            case 0x0C:
                cx = cy = 0;
                gc.setBackground(colors[background]);
                gc.fillRectangle(bounds);
                break;
            case 0x0D:
                cx = 0;
                break;
            default:
                if (cx >= bounds.width) {
                    cx = 0;
                    cy += font.getHeight();
                    if (cy >= bounds.height) {
                        gc.copyArea(0, font.getHeight(), bounds.width, bounds.height - font.getHeight(), 0, 0);
                        gc.setBackground(colors[background]);
                        gc.fillRectangle(0, bounds.height - font.getHeight(), bounds.width, font.getHeight());
                        cy -= font.getHeight();
                    }
                }

                font.print(gc, c, cx, cy);

                cx += font.getWidth();
                break;
        }
    }

    public void print(String s) {
        display.syncExec(new Runnable() {

            @Override
            public void run() {
                GC gc = new GC(image);
                try {
                    for (int i = 0; i < s.length(); i++) {
                        write(gc, s.charAt(i));
                    }
                } finally {
                    gc.dispose();
                }
                display.timerExec(REDRAW_MS, redrawRunnable);
            }
        });
    }

    public void write(byte[] b) {
        display.syncExec(new Runnable() {

            @Override
            public void run() {
                GC gc = new GC(image);
                try {
                    for (int i = 0; i < b.length; i++) {
                        write(gc, b[i]);
                    }
                } finally {
                    gc.dispose();
                }
                display.timerExec(REDRAW_MS, redrawRunnable);
            }
        });
    }

    public void setForeground(RGB color) {
        font.setForeground(color);
    }

    public void setBackground(RGB color) {
        font.setBackground(color);
    }

    public void setLocation(int x, int y) {
        cx = (x % (bounds.width / font.getWidth())) * font.getWidth();
        cy = (y % (bounds.height / font.getHeight())) * font.getHeight();
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

    public int getCursor() {
        return cursor;
    }

    public void setCursor(int cursor) {
        this.cursor = cursor;
    }

    public void dispose() {
        canvas.dispose();
    }
}
