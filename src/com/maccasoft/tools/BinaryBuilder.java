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

import java.io.ByteArrayOutputStream;

import nl.grauw.glass.AssemblyException;
import nl.grauw.glass.Line;
import nl.grauw.glass.Scope;
import nl.grauw.glass.Source;
import nl.grauw.glass.directives.If;

public class BinaryBuilder {

    Source source;

    int addr;
    byte[] code;

    int filler;
    ByteArrayOutputStream os;

    public BinaryBuilder(Source source) {
        this.source = source;
    }

    public void setFiller(int filler) {
        this.filler = filler;
    }

    public byte[] build() {
        addr = -1;
        filler = 0x00;
        os = new ByteArrayOutputStream();

        build(source);

        return os.toByteArray();
    }

    void build(Source source) {
        for (Line line : source.getLines()) {
            try {
                Scope scope = line.getScope();
                if (line.getDirective() instanceof If) {
                    code = new byte[0];
                }
                else if (scope.isAddressSet()) {
                    code = line.getBytes();
                }
                else {
                    code = new byte[0];
                }

                if (code.length != 0) {
                    int lineAddr = line.getScope().getAddress();

                    if (addr != -1) {
                        while (addr < lineAddr) {
                            os.write(filler);
                            addr++;
                        }
                    }

                    os.write(code);
                    addr = lineAddr + code.length;
                }

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
            } catch (AssemblyException e) {
                e.addContext(line);
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
