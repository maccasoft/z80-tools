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

import java.io.PrintStream;

import com.maccasoft.tools.SourceMap.LineEntry;

import nl.grauw.glass.Source;
import z80core.MemIoOps;
import z80core.NotifyOps;
import z80core.Z80;

public class Debugger extends MemIoOps implements NotifyOps {

    Z80 proc;
    SourceMap sourceMap;

    DebugTerminal debugTerminal;

    int tms9918Ram;
    int tms9918Reg;
    TMS9918 tms9918;

    boolean stop;

    final PrintStream out;

    public Debugger(PrintStream out) {
        super(65536);

        this.out = out;

        z80Ram[0] = (byte) 0xC3;
        z80Ram[1] = 0x00;
        z80Ram[2] = 0x01; // JP 0x100 CP/M TPA
        z80Ram[5] = (byte) 0xC9; // Return from BDOS call

        z80Ram[8] = (byte) 0xD3; // RST08
        z80Ram[9] = (byte) 0x81;
        z80Ram[10] = (byte) 0xC9;

        tms9918Ram = 0x40;
        tms9918Reg = 0x41;
        tms9918 = new TMS9918();

        proc = new Z80(this, this);
        proc.setBreakpoint(0x0005, true);
    }

    public void setSource(Source source) {
        sourceMap = new SourceMap(source, this);
        sourceMap.build();

        proc.setPinReset();
        proc.reset();
        proc.setRegPC(sourceMap.getEntryAddress());
    }

    public SourceMap getSourceMap() {
        return sourceMap;
    }

    public void setDebugTerminal(DebugTerminal debugTerminal) {
        this.debugTerminal = debugTerminal;
    }

    @Override
    public void reset() {
        tms9918.reset();

        proc.setPinReset();
        proc.reset();
        proc.setRegPC(sourceMap.getEntryAddress());

        super.reset();
    }

    @Override
    public int inPort(int port) {
        tstates += 4; // 4 clocks for read byte from bus

        port &= 0xFF;

        switch (port) {
            case Machine.SIOA_C: {
                int result = 0b00101100; // TX Buffer Empty, DCD and CTS
                try {
                    if (debugTerminal != null && debugTerminal.getInputStream().available() > 0) {
                        result |= 0x01; // RX Char Available
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return result;
            }
            case Machine.SIOA_D: {
                try {
                    if (debugTerminal != null && debugTerminal.getInputStream().available() > 0) {
                        return debugTerminal.getInputStream().read();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0x00;
            }
            case Machine.SIOB_C:
                return 0b00101100; // TX Buffer Empty, DCD and CTS
            case Machine.SIOB_D:
                return 0x00;
        }

        if (port == tms9918Ram) {
            return tms9918.inRam();
        }
        if (port == tms9918Reg) {
            return tms9918.inReg();
        }

        return port;
    }

    @Override
    public void outPort(int port, int value) {
        tstates += 4; // 4 clocks for write byte to bus

        port &= 0xFF;
        value &= 0xFF;

        switch (port) {
            case Machine.SIOA_D:
                if (debugTerminal != null) {
                    debugTerminal.write(value);
                }
                else {
                    out.write(value);
                }
                break;
            case Machine.SIOB_D:
                out.write(value);
                break;
        }

        if (port == tms9918Ram) {
            tms9918.outRam(value);
        }
        if (port == tms9918Reg) {
            tms9918.outReg(value);
        }
    }

    @Override
    public int breakpoint(int address, int opcode) {

        // Emulate CP/M Syscall at address 5
        if (address == 0x0005) {
            if (z80Ram[5] != (byte) 0xC9) {
                return opcode;
            }
            doCPMEmulation();
        }

        return opcode;
    }

    protected void doCPMEmulation() {
        switch (proc.getRegC()) {
            case 0x00: // BDOS 0 System Reset
                stop = true;
                out.println("Z80 reset after " + getTstates() + " t-states");
                break;
            case 0x01: // BDOS 1 console char input
                try {
                    if (debugTerminal != null && debugTerminal.getInputStream().available() > 0) {
                        proc.setRegA(debugTerminal.getInputStream().read());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 0x02: // BDOS 2 console char output
                if (debugTerminal != null) {
                    debugTerminal.write(proc.getRegE());
                }
                else {
                    out.write(proc.getRegE());
                }
                break;
            case 0x04: // BDOS 4 punch output
            case 0x05: // BDOS 2 list output
                out.write(proc.getRegE());
                break;
            case 0x06: { // BDOS 6 direct console I/O
                if (proc.getRegE() == 0xFF) {
                    try {
                        if (debugTerminal != null && debugTerminal.getInputStream().available() > 0) {
                            proc.setRegA(debugTerminal.getInputStream().read());
                        }
                        else {
                            proc.setRegA(0);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    if (debugTerminal != null) {
                        debugTerminal.write(proc.getRegE());
                    }
                    else {
                        out.write(proc.getRegE());
                    }
                }
                break;
            }
            case 0x09: { // BDOS 9 console string output (string terminated by "$")
                int strAddr = proc.getRegDE();
                if (debugTerminal != null) {
                    while (peek8(strAddr) != '$') {
                        debugTerminal.write(peek8(strAddr++));
                    }
                }
                else {
                    while (peek8(strAddr) != '$') {
                        out.write(peek8(strAddr++));
                    }
                }
                break;
            }
            case 0x0B: // BDOS 11 console status
                try {
                    if (debugTerminal != null && debugTerminal.getInputStream().available() > 0) {
                        proc.setRegA(0xFF);
                    }
                    else {
                        proc.setRegA(0x00);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                out.println("BDOS Call " + proc.getRegC());
                break;
        }
    }

    @Override
    public void execDone() {
    }

    public void doStop() {
        stop = true;
    }

    public void stepOver() {
        LineEntry lineEntry = sourceMap.getLineAtAddress(proc.getRegPC());
        if (lineEntry != null) {
            int stepOverPC1 = proc.getRegPC() + lineEntry.code.length;
            int stepOverPC2 = peek16(proc.getRegSP());
            int stepOverSP = proc.getRegSP();

            stop = false;
            do {
                int currentPC = proc.getRegPC();
                proc.execute();

                if (isBreakpoint(proc.getRegPC())) {
                    break;
                }
                if (proc.getRegPC() == stepOverPC1 || proc.getRegPC() == stepOverPC2 || (proc.getRegPC() != currentPC && proc.getRegSP() == stepOverSP)) {
                    break;
                }
            } while (!stop);

        }
        else {
            proc.execute();
        }
        doUpdateDebuggerState();
    }

    public void stepInto() {
        LineEntry lineEntry = sourceMap.getLineAtAddress(proc.getRegPC());
        if (lineEntry != null) {
            int stepOverPC1 = proc.getRegPC() + lineEntry.code.length;
            int stepOverPC2 = peek16(proc.getRegSP());
            int stepOverSP = proc.getRegSP();

            proc.execute();

            if (sourceMap.getLineAtAddress(proc.getRegPC()) == null) {
                stop = false;
                do {
                    int currentPC = proc.getRegPC();
                    proc.execute();

                    if (isBreakpoint(proc.getRegPC())) {
                        break;
                    }
                    if (proc.getRegPC() == stepOverPC1 || proc.getRegPC() == stepOverPC2 || (proc.getRegPC() != currentPC && proc.getRegSP() == stepOverSP)) {
                        break;
                    }
                } while (!stop);
            }
        }
        else {
            proc.execute();
        }
        doUpdateDebuggerState();
    }

    public void run() {
        stop = false;

        int count = 0;
        do {
            proc.execute();

            if (isBreakpoint(proc.getRegPC())) {
                break;
            }

            if (count++ >= 16) {
                doUpdateDebuggerState();
                count = 0;
            }
        } while (!stop);

        doUpdateDebuggerState();
    }

    public void runToAddress(int addr) {
        stop = false;
        do {
            proc.execute();

            if (isBreakpoint(proc.getRegPC())) {
                break;
            }
            if (proc.getRegPC() == addr) {
                break;
            }
        } while (!stop);

        doUpdateDebuggerState();
    }

    protected boolean isBreakpoint(int address) {
        return false;
    }

    protected void doUpdateDebuggerState() {
        tms9918.redrawFrame();
    }

    public void resetBreakpoints() {
        proc.resetBreakpoints();
        proc.setBreakpoint(0x0005, true);
    }
}
