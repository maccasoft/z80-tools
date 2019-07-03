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

import nl.grauw.glass.AssemblyException;
import nl.grauw.glass.Line;
import nl.grauw.glass.Scope;
import nl.grauw.glass.Source;
import nl.grauw.glass.directives.If;
import nl.grauw.glass.directives.Section;

public class ListingBuilder {

    Source source;

    int column;
    int address;
    byte[] code;

    StringBuilder sb;

    public ListingBuilder(Source source) {
        this.source = source;
    }

    public StringBuilder build() {
        sb = new StringBuilder();

        build(source);

        return sb;
    }

    void build(Source source) {
        for (Line line : source.getLines()) {
            try {
                Scope scope = line.getScope();
                if (line.getDirective() instanceof If) {
                    if (scope.isAddressSet()) {
                        address = scope.getAddress();
                    }
                    code = new byte[0];
                }
                else if (scope.isAddressSet()) {
                    address = scope.getAddress();
                    code = line.getBytes();
                }
                else {
                    code = new byte[0];
                }

                sb.append(String.format("%05d  %04X ", line.getLineNumber() + 1, address));

                column = 0;
                int codeIndex = 0;
                for (int i = 0; codeIndex < code.length && (column + 3) < 24; i++, codeIndex++, address++) {
                    if (i != 0) {
                        sb.append(' ');
                        column++;
                    }
                    sb.append(String.format("%02X", code[codeIndex]));
                    column += 2;
                }
                while (column < 24 + 1) {
                    sb.append(' ');
                    column++;
                }

                sb.append(line.getSourceText());

                while (codeIndex < code.length) {
                    sb.append("\r\n");
                    sb.append(String.format("%05d  %04X ", line.getLineNumber() + 1, address));

                    column = 0;
                    for (int i = 0; codeIndex < code.length && (column + 3) < 24; i++, codeIndex++, address++) {
                        if (i != 0) {
                            sb.append(' ');
                            column++;
                        }
                        sb.append(String.format("%02X", code[codeIndex]));
                        column += 2;
                    }
                }

                sb.append("\r\n");

                if (line.getDirective() instanceof If) {
                    if (line.getInstructionObject() != null) {
                        nl.grauw.glass.instructions.If ins = (nl.grauw.glass.instructions.If) line.getInstruction();
                        build(ins.getThenSource());
                        if (ins.getElseSource() != null) {
                            build(ins.getElseSource());
                        }
                    }
                    else {
                        If ins = (If) line.getDirective();
                        build(ins.getThenSource());
                        if (ins.getElseSource() != null) {
                            build(ins.getElseSource());
                        }
                    }
                }
                else if (line.getDirective() instanceof Section) {
                    if (line.getInstructionObject() != null) {
                        nl.grauw.glass.instructions.Section ins = (nl.grauw.glass.instructions.Section) line.getInstruction();
                        build(ins.getSource());
                    }
                    else {
                        Section ins = (Section) line.getDirective();
                        build(ins.getSource());
                    }
                }
            } catch (AssemblyException e) {
                e.addContext(line);
                throw e;
            }
        }
    }

}
