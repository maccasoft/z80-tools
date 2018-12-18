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

import java.io.OutputStream;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import junit.framework.TestCase;

public class ConsoleTest extends TestCase {

    Display display;
    Shell shell;
    Console console;

    @Override
    protected void setUp() throws Exception {
        display = Display.getDefault();
        shell = new Shell(display);
        console = new Console(shell);
    }

    @Override
    protected void tearDown() throws Exception {
        shell.dispose();
        display.dispose();
    }

    public void testWriteToOutputStream() throws Exception {
        OutputStream os = console.getOutputStream();
        os.write("Line 1\r\nLine 2\r\n".getBytes());
        assertEquals("Line 1\nLine 2\n", console.text.getText());
        assertNull(console.text.getStyleRangeAtOffset(console.text.getOffsetAtLine(0)));
        assertNull(console.text.getStyleRangeAtOffset(console.text.getOffsetAtLine(1)));
    }

    public void testWriteToErrorStream() throws Exception {
        OutputStream os = console.getErrorStream();
        os.write("Line 1\r\nLine 2\r\n".getBytes());
        assertEquals("Line 1\nLine 2\n", console.text.getText());

        StyleRange range0 = console.text.getStyleRangeAtOffset(console.text.getOffsetAtLine(0));
        assertEquals(console.errorColor, range0.foreground);

        StyleRange range1 = console.text.getStyleRangeAtOffset(console.text.getOffsetAtLine(1));
        assertEquals(console.errorColor, range1.foreground);
    }

    public void testWriteToBothStreams() throws Exception {
        console.getOutputStream().write("Line 1\r\n".getBytes());
        console.getErrorStream().write("Line 2\r\n".getBytes());

        assertEquals("Line 1\nLine 2\n", console.text.getText());

        StyleRange range0 = console.text.getStyleRangeAtOffset(console.text.getOffsetAtLine(0));
        assertNull(range0);

        StyleRange range1 = console.text.getStyleRangeAtOffset(console.text.getOffsetAtLine(1));
        assertEquals(console.errorColor, range1.foreground);
    }

    public void testWriteLineWithCRLF() throws Exception {
        OutputStream os = console.getOutputStream();
        os.write("Line\r\n".getBytes());
        assertEquals("Line\n", console.text.getText());
    }

    public void testWriteLineWithCR() throws Exception {
        OutputStream os = console.getOutputStream();

        os.write("Line 1\r".getBytes());
        assertEquals("Line 1", console.text.getText());

        os.write("Line 2\r".getBytes());
        assertEquals("Line 2", console.text.getText());
    }

    public void testWriteBlankLine() throws Exception {
        OutputStream os = console.getOutputStream();

        os.write("Line 1\r\n".getBytes());
        assertEquals("Line 1\n", console.text.getText());

        os.write("\n".getBytes());
        assertEquals("Line 1\n\n", console.text.getText());

        os.write("Line 2\r".getBytes());
        assertEquals("Line 1\n\nLine 2", console.text.getText());
    }

    public void testWriteUnterminatedLine() throws Exception {
        OutputStream os = console.getOutputStream();
        os.write("Line 1\r\nLine 2".getBytes());
        assertEquals("Line 1\nLine 2", console.text.getText());
    }

    public void testWriteOutputStreamErrorLine() throws Exception {
        OutputStream os = console.getOutputStream();
        os.write("Line 1\r\nError : Line 2\r\n".getBytes());
        assertEquals("Line 1\nError : Line 2\n", console.text.getText());

        StyleRange range0 = console.text.getStyleRangeAtOffset(console.text.getOffsetAtLine(0));
        assertNull(range0);

        StyleRange range1 = console.text.getStyleRangeAtOffset(console.text.getOffsetAtLine(1));
        assertEquals(console.errorColor, range1.foreground);
    }

}
