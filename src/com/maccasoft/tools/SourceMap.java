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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.grauw.glass.AssemblyException;
import nl.grauw.glass.Line;
import nl.grauw.glass.Scope;
import nl.grauw.glass.Source;
import nl.grauw.glass.directives.If;
import z80core.MemIoOps;

public class SourceMap {

    Source source;
    MemIoOps memIoOps;

    int address;
    int entryAddress;

    public static class LineEntry {
        public final Line line;
        public final int lineNumber;

        public final int address;
        public final byte[] code;

        public LineEntry(int lineNumber, int address, Line line) {
            this.line = line;
            this.address = address;
            this.lineNumber = lineNumber;

            Scope scope = line.getScope();

            if (line.getDirective() instanceof If) {
                this.code = new byte[0];
            }
            else if (scope.isAddressSet()) {
                this.code = line.getBytes();
            }
            else {
                this.code = new byte[0];
            }
        }

    }

    Map<Integer, LineEntry> map;
    List<LineEntry> list;

    public SourceMap(Source source, MemIoOps memIoOps) {
        this.source = source;
        this.memIoOps = memIoOps;
    }

    public void build() {
        address = 0;
        entryAddress = -1;

        list = new ArrayList<LineEntry>();
        map = new HashMap<Integer, LineEntry>();

        build(source, 0);
    }

    int build(Source source, int lineNumber) {
        for (Line line : source.getLines()) {
            try {
                if (line.getScope().isAddressSet()) {
                    address = line.getScope().getAddress();
                }
                LineEntry lineEntry = new LineEntry(lineNumber++, address, line);
                if (lineEntry.code.length != 0) {
                    if (entryAddress == -1) {
                        entryAddress = address;
                    }
                    System.arraycopy(lineEntry.code, 0, memIoOps.getRam(), lineEntry.address, lineEntry.code.length);
                    map.put(lineEntry.address, lineEntry);
                }
                list.add(lineEntry);

                if (line.getDirective() instanceof If) {
                    if (line.getInstructionObject() != null) {
                        nl.grauw.glass.instructions.If ins = (nl.grauw.glass.instructions.If) line.getInstruction();
                        lineNumber = build(ins.getThenSource(), lineNumber);
                        if (ins.getElseSource() != null) {
                            lineNumber = build(ins.getElseSource(), lineNumber);
                        }
                    }
                    else {
                        If ins = (If) line.getDirective();
                        lineNumber = build(ins.getThenSource(), lineNumber);
                        if (ins.getElseSource() != null) {
                            lineNumber = build(ins.getElseSource(), lineNumber);
                        }
                    }
                }
            } catch (AssemblyException e) {
                e.addContext(line);
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return lineNumber;
    }

    public int getEntryAddress() {
        return entryAddress;
    }

    public LineEntry getLineAtAddress(int address) {
        return map.get(address);
    }

    public List<LineEntry> getLines() {
        return list;
    }

    public LineEntry getLine(int lineNumber) {
        return list.get(lineNumber);
    }

}
