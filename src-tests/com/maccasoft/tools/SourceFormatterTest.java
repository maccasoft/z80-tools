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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import nl.grauw.glass.Line;
import nl.grauw.glass.Parser;
import nl.grauw.glass.Scope;
import nl.grauw.glass.Source;
import nl.grauw.glass.SourceBuilder;

public class SourceFormatterTest {

    int mnemonicColumn;
    int argumentColumn;
    int commentColumn;

    @Before
    public void setUp() {
        mnemonicColumn = 0;
        argumentColumn = 0;
        commentColumn = 0;
    }

    @Test
    public void testFormat() {
        mnemonicColumn = 16;
        argumentColumn = mnemonicColumn + 6;
        commentColumn = mnemonicColumn + 16;
        assertEquals("                exx\r\n", format(" exx"));
        assertEquals("                ld    a, b\r\n", format(" ld a,b"));
        assertEquals("test            ld    a, b\r\n", format("test ld a,b"));
        assertEquals("                exx             ; test\r\n", format(" exx ; test"));
        assertEquals("; test\r\n", format("; test"));
    }

    @Test
    public void testDefaultFormat() {
        assertEquals(" exx\r\n", format(" exx"));
        assertEquals(" ld a, b\r\n", format(" ld a,b"));
        assertEquals("test ld a, b\r\n", format("test ld a,b"));
        assertEquals(" exx ; test\r\n", format(" exx ; test"));
        assertEquals("; test\r\n", format("; test"));
    }

    @Test
    public void testFormatNumber() {
        assertEquals("test ld a, 24\r\n", format("test ld a,24"));
        assertEquals("test ld a, 08H\r\n", format("test ld a,8h"));
        assertEquals("test ld hl, 01F0H\r\n", format("test ld hl,1f0h"));
    }

    @Test
    public void testFormatExpression() throws Exception {
        assertEquals("a", parse("a"));
        assertEquals("a + 01H", parse("a + 1H"));
        assertEquals("a + (01H + 02H) * 03H", parse("a + (1H + 2H) * 3H"));
        assertEquals("a ? 01H : 02H", parse("a ? 1H : 2H"));
    }

    @Test
    public void testFormatMnemonicCase() throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append("test ld a,8h").append("\n");
        SourceBuilder sourceBuilder = new SourceBuilder(new ArrayList<File>());
        Source source = sourceBuilder.parse(new StringReader(builder.toString()), null);

        SourceFormatter formatter = new SourceFormatter(source);
        formatter.mnemonicCase = SourceFormatter.TO_UPPER;
        assertEquals("test LD A, 08H\r\n", formatter.format());
    }

    @Test
    public void testFormatLabelCase() throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append("test ld hl,test").append("\n");
        SourceBuilder sourceBuilder = new SourceBuilder(new ArrayList<File>());
        Source source = sourceBuilder.parse(new StringReader(builder.toString()), null);

        SourceFormatter formatter = new SourceFormatter(source);
        formatter.labelCase = SourceFormatter.TO_UPPER;
        assertEquals("TEST ld hl, TEST\r\n", formatter.format());
    }

    public String format(String... sourceLines) {
        StringBuilder builder = new StringBuilder();
        for (String lineText : sourceLines) {
            builder.append(lineText).append("\n");
        }
        SourceBuilder sourceBuilder = new SourceBuilder(new ArrayList<File>());
        Source source = sourceBuilder.parse(new StringReader(builder.toString()), null);
        SourceFormatter formatter = new SourceFormatter(source);
        formatter.mnemonicColumn = mnemonicColumn;
        formatter.argumentColumn = argumentColumn;
        formatter.commentColumn = commentColumn;
        return formatter.format();
    }

    public String parse(String text) {
        LineNumberReader reader = new LineNumberReader(new StringReader(" test " + text));
        Line line = new Parser().parse(reader, new Scope(), null);
        return new SourceFormatter(null).formatExpression(line.getArguments());
    }

}
