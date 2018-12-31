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

import nl.grauw.glass.AssemblyException;
import nl.grauw.glass.Line;
import nl.grauw.glass.Scope;
import nl.grauw.glass.Source;
import nl.grauw.glass.directives.If;

public class IntelHexBuilder {

    Source source;

    int addr;
    int nextAddr;
    byte[] code;
    ByteArrayOutputStream os;

    StringBuilder sb;

    public IntelHexBuilder(Source source) {
        this.source = source;
    }

    public StringBuilder build() {
        sb = new StringBuilder();

        addr = -1;
        nextAddr = -1;
        os = new ByteArrayOutputStream();

        build(source);

        byte[] data = os.toByteArray();
        if (data.length != 0) {
            sb.append(toHexString(addr, data));
        }

        sb.append(":00000001FF\r\n");

        return sb;
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
                    if (line.getScope().getAddress() != nextAddr) {
                        byte[] data = os.toByteArray();
                        if (data.length != 0) {
                            sb.append(toHexString(addr, data));
                        }

                        nextAddr = addr = line.getScope().getAddress();
                        os = new ByteArrayOutputStream();
                    }

                    os.write(code);
                    nextAddr += code.length;
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

    String toHexString(int addr, byte[] data) {
        StringBuilder sb = new StringBuilder();

        int i = 0;

        while ((data.length - i) > 0) {
            int l = data.length - i;
            if (l > 24) {
                l = 24;
            }
            sb.append(String.format(":%02X%04X%02X", l, addr, 0));

            int checksum = l + (addr & 0xFF) + ((addr >> 8) & 0xFF) + 0;
            for (int n = 0; n < l; n++, i++, addr++) {
                sb.append(String.format("%02X", data[i]));
                checksum += data[i];
            }

            sb.append(String.format("%02X\r\n", (-checksum) & 0xFF));
        }

        return sb.toString();
    }

}
