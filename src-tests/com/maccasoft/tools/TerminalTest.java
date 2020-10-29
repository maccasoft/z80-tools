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

import java.io.ByteArrayOutputStream;

import org.eclipse.swt.widgets.Shell;

public class TerminalTest extends DatabindingTestCase {

    Shell shell;
    Terminal term;
    ByteArrayOutputStream out;

    @Override
    protected void setUp() throws Exception {
        shell = createShell();

        out = new ByteArrayOutputStream();

        term = new Terminal(shell) {

            @Override
            protected void writeByte(byte b) {
                out.write(b);
            }

        };
    }

    @Override
    protected void tearDown() throws Exception {
        shell.dispose();
    }

    public void testNoArguments() throws Exception {
        term.print("\033[H");
        assertEquals(0, term.argc);
    }

    public void testOneArgument() throws Exception {
        term.print("\033[10H");
        assertEquals(1, term.argc);
        assertEquals(10, term.args[0]);
    }

    public void testMoreThanOneArgument() throws Exception {
        term.print("\033[10;20;30H");
        assertEquals(3, term.argc);
        assertEquals(10, term.args[0]);
        assertEquals(20, term.args[1]);
        assertEquals(30, term.args[2]);
    }

    public void testCursorPositionH() throws Exception {
        term.cy = 10 * term.getFont().getHeight();
        term.cx = 10 * term.getFont().getWidth();

        term.print("\033[H");

        assertEquals(0, term.argc);
        assertEquals(0, term.cy);
        assertEquals(0, term.cx);

        term.print("\033[2;3H");

        assertEquals(2, term.argc);
        assertEquals(2, term.args[0]);
        assertEquals(3, term.args[1]);

        assertEquals(1 * term.getFont().getHeight(), term.cy);
        assertEquals(2 * term.getFont().getWidth(), term.cx);
    }

    public void testCursorPositionF() throws Exception {
        term.cy = 10 * term.getFont().getHeight();
        term.cx = 10 * term.getFont().getWidth();

        term.print("\033[f");

        assertEquals(0, term.argc);
        assertEquals(0, term.cy);
        assertEquals(0, term.cx);

        term.print("\033[2;3f");

        assertEquals(2, term.argc);
        assertEquals(2, term.args[0]);
        assertEquals(3, term.args[1]);

        assertEquals(1 * term.getFont().getHeight(), term.cy);
        assertEquals(2 * term.getFont().getWidth(), term.cx);
    }

    public void testCursorUp() throws Exception {
        term.cx = 10 * term.getFont().getWidth();
        term.cy = 10 * term.getFont().getHeight();

        term.print("\033[A");

        assertEquals(10 * term.getFont().getWidth(), term.cx);
        assertEquals(9 * term.getFont().getHeight(), term.cy);

        term.print("\033[2A");

        assertEquals(10 * term.getFont().getWidth(), term.cx);
        assertEquals(7 * term.getFont().getHeight(), term.cy);
    }

    public void testCursorDown() throws Exception {
        term.cx = 10 * term.getFont().getWidth();
        term.cy = 10 * term.getFont().getHeight();

        term.print("\033[B");

        assertEquals(10 * term.getFont().getWidth(), term.cx);
        assertEquals(11 * term.getFont().getHeight(), term.cy);

        term.print("\033[2B");

        assertEquals(10 * term.getFont().getWidth(), term.cx);
        assertEquals(13 * term.getFont().getHeight(), term.cy);
    }

    public void testCursorRight() throws Exception {
        term.cx = 10 * term.getFont().getWidth();
        term.cy = 10 * term.getFont().getHeight();

        term.print("\033[C");

        assertEquals(11 * term.getFont().getWidth(), term.cx);
        assertEquals(10 * term.getFont().getHeight(), term.cy);

        term.print("\033[2C");

        assertEquals(13 * term.getFont().getWidth(), term.cx);
        assertEquals(10 * term.getFont().getHeight(), term.cy);
    }

    public void testCursorLeft() throws Exception {
        term.cx = 10 * term.getFont().getWidth();
        term.cy = 10 * term.getFont().getHeight();

        term.print("\033[D");

        assertEquals(9 * term.getFont().getWidth(), term.cx);
        assertEquals(10 * term.getFont().getHeight(), term.cy);

        term.print("\033[2D");

        assertEquals(7 * term.getFont().getWidth(), term.cx);
        assertEquals(10 * term.getFont().getHeight(), term.cy);
    }

    public void testResetAttributes() throws Exception {
        term.foreground = 15;
        term.background = 2;

        term.print("\033[m");

        assertEquals(7, term.foreground);
        assertEquals(0, term.background);

        term.foreground = 15;
        term.background = 2;

        term.print("\033[0m");

        assertEquals(7, term.foreground);
        assertEquals(0, term.background);
    }

    public void testForeground() throws Exception {
        term.foreground = 7;
        term.background = 0;

        term.print("\033[31m");

        assertEquals(1, term.foreground);
        assertEquals(0, term.background);

        term.print("\033[1;31m");

        assertEquals(1 + 8, term.foreground);
        assertEquals(0, term.background);
    }

    public void testBackground() throws Exception {
        term.foreground = 7;
        term.background = 0;

        term.print("\033[41m");

        assertEquals(7, term.foreground);
        assertEquals(1, term.background);

        term.print("\033[1;41m");

        assertEquals(7 + 8, term.foreground);
        assertEquals(1, term.background);
    }

    public void testToggleCursor() throws Exception {
        term.cursor = Terminal.CURSOR_ON | Terminal.CURSOR_FLASH | Terminal.CURSOR_ULINE;

        term.print("\033[?25l");

        assertEquals(Terminal.CURSOR_FLASH | Terminal.CURSOR_ULINE, term.cursor);

        term.print("\033[?25h");

        assertEquals(Terminal.CURSOR_ON | Terminal.CURSOR_FLASH | Terminal.CURSOR_ULINE, term.cursor);
    }

    public void testCursorReport() throws Exception {
        out = new ByteArrayOutputStream();
        term.print("\033[1;1f\033[6n");
        assertEquals("\033[1;1R", out.toString());

        out = new ByteArrayOutputStream();
        term.print("\033[10;1f\033[6n");
        assertEquals("\033[10;1R", out.toString());

        out = new ByteArrayOutputStream();
        term.print("\033[1;10f\033[6n");
        assertEquals("\033[1;10R", out.toString());
    }

    public void testCursorPositionBounds() throws Exception {
        term.print("\033[100;100f\033[6n");
        assertEquals("\033[25;80R", out.toString());
    }
}
