/*
 * Copyright (c) 2019 Marco Maccaferri and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.maccasoft.tools;

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;

import junit.framework.TestCase;
import nl.grauw.glass.Source;
import nl.grauw.glass.SourceBuilder;

public class AssemblerTest extends TestCase {

    public void testGetHex() throws Exception {
        Assembler builder = new Assembler();

        builder.ram.put(0x0000, (byte) 0x01);
        builder.ram.put(0x0001, (byte) 0x02);
        builder.ram.put(0x0002, (byte) 0x03);

        StringBuilder sb = builder.getHex();
        assertEquals(":03000000010203F7\r\n", sb.toString());
    }

    public void testGetHexWithGap() throws Exception {
        Assembler builder = new Assembler();

        builder.ram.put(0x0000, (byte) 0x01);
        builder.ram.put(0x0001, (byte) 0x02);
        builder.ram.put(0x0003, (byte) 0x03);

        StringBuilder sb = builder.getHex();
        assertEquals(":020000000102FB\r\n:0100030003F9\r\n", sb.toString());
    }

    public void testGetHexWithOrigin() throws Exception {
        Assembler builder = new Assembler();

        builder.ram.put(0x0100, (byte) 0x01);
        builder.ram.put(0x0101, (byte) 0x02);
        builder.ram.put(0x0102, (byte) 0x03);

        StringBuilder sb = builder.getHex();
        assertEquals(":03010000010203F6\r\n", sb.toString());
    }

    public void testBuildHex() throws Exception {
        Source source = assemble(
            " xor a",
            " ret");

        Assembler builder = new Assembler();
        builder.build(source);

        StringBuilder sb = builder.getHex();
        assertEquals(":02000000AFC986\r\n", sb.toString());
    }

    public void testBuildHexWithOrigin() throws Exception {
        Source source = assemble(
            " .org 100H",
            " xor a",
            " ret");

        Assembler builder = new Assembler();
        builder.build(source);

        StringBuilder sb = builder.getHex();
        assertEquals(":02010000AFC985\r\n", sb.toString());
    }

    public void testBuildHexWithDsGap() throws Exception {
        Source source = assemble(
            " xor a",
            " .ds 1",
            " ret");

        Assembler builder = new Assembler();
        builder.build(source);

        StringBuilder sb = builder.getHex();
        assertEquals(":01000000AF50\r\n:01000200C934\r\n", sb.toString());
    }

    public void testGetBinary() throws Exception {
        Assembler builder = new Assembler();

        builder.ram.put(0x0000, (byte) 0x01);
        builder.ram.put(0x0001, (byte) 0x02);
        builder.ram.put(0x0002, (byte) 0x03);

        assertArrayEquals(b(0x01, 0x02, 0x03), builder.getBinary());
    }

    public void testGetBinaryWithGap() throws Exception {
        Assembler builder = new Assembler();

        builder.ram.put(0x0000, (byte) 0x01);
        builder.ram.put(0x0001, (byte) 0x02);
        builder.ram.put(0x0003, (byte) 0x03);

        assertArrayEquals(b(0x01, 0x02, 0x00, 0x03), builder.getBinary());
    }

    public void testGetBinaryWithOrigin() throws Exception {
        Assembler builder = new Assembler();

        builder.ram.put(0x0100, (byte) 0x01);
        builder.ram.put(0x0101, (byte) 0x02);
        builder.ram.put(0x0102, (byte) 0x03);

        assertArrayEquals(b(0x01, 0x02, 0x03), builder.getBinary());
    }

    public void testBuildBinary() throws Exception {
        Source source = assemble(
            " xor a",
            " ret");

        Assembler builder = new Assembler();
        builder.build(source);

        assertArrayEquals(b(0xAF, 0xC9), builder.getBinary());
    }

    public void testBuildBinaryWithOrigin() throws Exception {
        Source source = assemble(
            " .org 100H",
            " xor a",
            " ret");

        Assembler builder = new Assembler();
        builder.build(source);

        assertArrayEquals(b(0xAF, 0xC9), builder.getBinary());
    }

    public void testBuildBinaryWithDsGap() throws Exception {
        Source source = assemble(
            " xor a",
            " .ds 1",
            " ret");

        Assembler builder = new Assembler();
        builder.build(source);

        assertArrayEquals(b(0xAF, 0x00, 0xC9), builder.getBinary());
    }

    private Source assemble(String... sourceLines) {
        StringBuilder builder = new StringBuilder();
        for (String lineText : sourceLines) {
            builder.append(lineText).append("\n");
        }
        SourceBuilder sourceBuilder = new SourceBuilder(new ArrayList<File>());
        Source source = sourceBuilder.parse(new StringReader(builder.toString()), null);
        source.register();
        source.expand();
        source.resolve();
        return source;
    }

    private byte[] b(int... values) {
        byte[] bytes = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            bytes[i] = (byte) values[i];
        }
        return bytes;
    }

}
