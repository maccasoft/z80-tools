/*
 * Copyright (c) 2020 Marco Maccaferri and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.maccasoft.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import junit.framework.TestCase;
import nl.grauw.glass.Source;
import nl.grauw.glass.SourceBuilder;

public class DebuggerTest extends TestCase {

    public void testSetStartAddress() throws Exception {
        Debugger debugger = new Debugger(System.out);
        debugger.setSource(assemble(
            " org 100h",
            " ld a,23",
            " ret"));

        assertEquals(0x0100, debugger.proc.getRegPC());
    }

    public void testStepInto() throws Exception {
        Debugger debugger = new Debugger(System.out);
        debugger.setSource(assemble(
            " ld a,23",
            " ret"));

        debugger.stepInto();

        assertEquals(0x0002, debugger.proc.getRegPC());
    }

    public void testStepIntoCall() throws Exception {
        Debugger debugger = new Debugger(System.out);
        debugger.setSource(assemble(
            " call label",
            " ret",
            "label:",
            " ld a,23",
            " ret"));

        debugger.stepInto();

        assertEquals(0x0004, debugger.proc.getRegPC());
    }

    public void testStepOver() throws Exception {
        Debugger debugger = new Debugger(System.out);
        debugger.setSource(assemble(
            " ld a,23",
            " ret"));

        debugger.stepOver();

        assertEquals(0x0002, debugger.proc.getRegPC());
    }

    public void testStepOverCall() throws Exception {
        Debugger debugger = new Debugger(System.out);
        debugger.setSource(assemble(
            " call label",
            " ret",
            "label:",
            " ld a,23",
            " ret"));

        debugger.stepOver();

        assertEquals(0x0003, debugger.proc.getRegPC());
    }

    public void testStepIntoRepeatInstructions() throws Exception {
        Debugger debugger = new Debugger(System.out);
        debugger.setSource(assemble(
            " ldir",
            " ret"));

        debugger.proc.setRegBC(2);
        debugger.proc.setRegDE(0x1000);
        debugger.proc.setRegHL(0x2000);

        debugger.stepInto();
        assertEquals(0x0000, debugger.proc.getRegPC());
        assertEquals(1, debugger.proc.getRegBC());
    }

    public void testStepOverRepeatInstructions() throws Exception {
        Debugger debugger = new Debugger(System.out);
        debugger.setSource(assemble(
            " ldir",
            " ret"));

        debugger.proc.setRegBC(2);
        debugger.proc.setRegDE(0x1000);
        debugger.proc.setRegHL(0x2000);

        debugger.stepOver();

        assertEquals(0x0002, debugger.proc.getRegPC());
        assertEquals(0, debugger.proc.getRegBC());
    }

    public void testRunToAddress() throws Exception {
        Debugger debugger = new Debugger(System.out);
        debugger.setSource(assemble(
            " nop",
            " ld a,23",
            " ret"));

        debugger.runToAddress(0x0003);

        assertEquals(0x0003, debugger.proc.getRegPC());
    }

    private Source assemble(String... sourceLines) {
        StringBuilder builder = new StringBuilder();
        for (String lineText : sourceLines) {
            builder.append(lineText).append("\n");
        }
        SourceBuilder sourceBuilder = new SourceBuilder(new ArrayList<File>());
        Source source = sourceBuilder.parse(new StringReader(builder.toString()), null);
        try {
            source.assemble(new ByteArrayOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return source;
    }

}
