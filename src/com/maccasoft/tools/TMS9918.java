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

import java.util.Arrays;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

public class TMS9918 {

    public static final int NTSC = 0;
    public static final int PAL = 1;

    public static final int TMSMODE3 = 0b00000010;

    public static final int TMSBLANK = 0b01000000;
    public static final int TMSMODE1 = 0b00010000;
    public static final int TMSMODE2 = 0b00001000;
    public static final int TMSSPRSIZE = 0b00000010;
    public static final int TMSSPRMAG = 0b00000001;

    public static final int FRAME_WIDTH = 320;
    public static final int FRAME_HEIGHT = 240;
    public static final int FRAME_TOP = (FRAME_HEIGHT - 192) / 2;
    public static final int FRAME_LEFT = (FRAME_WIDTH - 256) / 2;

    public static final int STATE_VSYNC = -1;

    PaletteData paletteData;
    ImageData imageData;

    byte[] reg;
    byte[] ram;
    byte status;

    int ramPtr;
    int data;

    int state;
    long currentTimeNs;
    long nextTimeNs;

    int ROW_TIME_NS;
    int DISPLAY_ROWS;

    public TMS9918() {
        ROW_TIME_NS = 63500;
        DISPLAY_ROWS = 262;

        reg = new byte[8];
        ram = new byte[16384];
        ramPtr = 0;

        data = -1;

        state = STATE_VSYNC - 1;
        nextTimeNs = 0;

        paletteData = new PaletteData(new RGB[] {
            new RGB(0x00, 0x00, 0x00), new RGB(0x00, 0x00, 0x00), new RGB(0x20, 0xC0, 0x20), new RGB(0x60, 0xE0, 0x60),
            new RGB(0x20, 0x20, 0xE0), new RGB(0x40, 0x60, 0xE0), new RGB(0xA0, 0x20, 0x20), new RGB(0x40, 0xC0, 0xE0),
            new RGB(0xE0, 0x20, 0x20), new RGB(0xE0, 0x60, 0x60), new RGB(0xC0, 0xC0, 0x20), new RGB(0xC0, 0xC0, 0x80),
            new RGB(0x20, 0x80, 0x20), new RGB(0xC0, 0x40, 0xA0), new RGB(0xA0, 0xA0, 0xA0), new RGB(0xE0, 0xE0, 0xE0)
        });

        imageData = new ImageData(FRAME_WIDTH, FRAME_HEIGHT, 8, paletteData, 1, new byte[FRAME_WIDTH * FRAME_HEIGHT]);
    }

    public void setTiming(int timing) {
        if (timing == PAL) {
            ROW_TIME_NS = 64000;
            DISPLAY_ROWS = 312;
        }
        else {
            ROW_TIME_NS = 63500;
            DISPLAY_ROWS = 262;
        }
    }

    public void reset() {
        Arrays.fill(reg, (byte) 0);
    }

    public void outReg(int value) {
        if (data == -1) {
            data = value;
        }
        else {
            if ((value & 0x80) != 0) {
                writeReg(value & 0x07, data);
            }
            else {
                setRamPtr((value << 8) | data);
            }
            data = -1;
        }
    }

    public void outRam(int value) {
        ram[ramPtr] = (byte) value;
        ramPtr = (ramPtr + 1) & 0x3FFF;
    }

    protected void writeReg(int index, int value) {
        reg[index] = (byte) value;
    }

    protected void setRamPtr(int value) {
        ramPtr = value & 0x3FFF;
    }

    public int getRamPtr() {
        return ramPtr;
    }

    public int inRam() {
        int result = ram[ramPtr] & 0xFF;
        ramPtr = (ramPtr + 1) & 0x3FFF;
        return result;
    }

    public int inReg() {
        int result = status;
        status &= ~0x80;
        return result;
    }

    public void processFrame(long elapsedTimeNs) {
        currentTimeNs += elapsedTimeNs;

        while (currentTimeNs >= nextTimeNs) {
            state++;
            if (state >= FRAME_HEIGHT) {
                state = STATE_VSYNC;
                status |= 0x80;
                onVSync();
            }

            if (state == STATE_VSYNC) {
                nextTimeNs += ROW_TIME_NS * (DISPLAY_ROWS - FRAME_HEIGHT);
            }
            else if (state >= 0 && state < FRAME_HEIGHT) {
                processRow(state);
                nextTimeNs += ROW_TIME_NS;
            }
        }
    }

    protected void onVSync() {
        // Do nothing
    }

    void processRow(int row) {
        int register2 = (reg[2] << 10) & 0x3FFF;
        int register3 = (reg[3] << 6) & 0x3FFF;
        int register4 = (reg[4] << 11) & 0x3FFF;
        int register5 = (reg[5] << 7) & 0x3FFF;
        int register6 = (reg[6] << 11) & 0x3FFF;

        byte backdrop = (byte) (reg[7] & 0x0F);

        int index = row * FRAME_WIDTH;

        if ((reg[1] & TMSBLANK) == 0) {
            Arrays.fill(imageData.data, index, index + FRAME_WIDTH, (byte) 0);
            return;
        }

        Arrays.fill(imageData.data, index, index + FRAME_WIDTH, backdrop);

        if (row >= FRAME_TOP && row < (FRAME_TOP + 192)) {
            index += FRAME_LEFT;
            row -= FRAME_TOP;

            if ((reg[1] & TMSMODE1) != 0) {
                byte c0 = (byte) (reg[7] & 0x0F);
                byte c1 = (byte) ((reg[7] & 0xF0) >> 4);

                register2 += (row >> 3) * 40;
                register4 += row & 0x07;

                index += 8;
                for (int i = 0; i < 40; i++) {
                    int tile = ram[register2++] & 0xFF;
                    byte pattern = ram[register4 + (tile << 3)];

                    imageData.data[index++] = (pattern & 0x80) != 0 ? c1 : c0;
                    imageData.data[index++] = (pattern & 0x40) != 0 ? c1 : c0;
                    imageData.data[index++] = (pattern & 0x20) != 0 ? c1 : c0;
                    imageData.data[index++] = (pattern & 0x10) != 0 ? c1 : c0;
                    imageData.data[index++] = (pattern & 0x08) != 0 ? c1 : c0;
                    imageData.data[index++] = (pattern & 0x04) != 0 ? c1 : c0;
                }
            }
            else {
                if ((reg[1] & TMSMODE2) != 0) {
                    register2 += (row >> 3) << 5;
                    register4 += (row >> 2) & 0x07;

                    for (int i = 0; i < 32; i++) {
                        int tile = ram[register2++] & 0xFF;
                        byte colors = ram[register4 + (tile << 3)];

                        byte c0 = (byte) (colors & 0x0F);
                        if (c0 == 0) {
                            c0 = backdrop;
                        }
                        byte c1 = (byte) ((colors & 0xF0) >> 4);
                        if (c1 == 0) {
                            c1 = backdrop;
                        }

                        imageData.data[index++] = c1;
                        imageData.data[index++] = c1;
                        imageData.data[index++] = c1;
                        imageData.data[index++] = c1;

                        imageData.data[index++] = c0;
                        imageData.data[index++] = c0;
                        imageData.data[index++] = c0;
                        imageData.data[index++] = c0;
                    }
                }
                else if ((reg[0] & TMSMODE3) != 0) {
                    register2 += (row >> 3) * 32;

                    register3 &= ~0x1FFF;
                    register3 += (row >> 6) << 11;
                    register3 += row & 0x07;

                    register4 &= ~0x1FFF;
                    register4 += (row >> 6) << 11;
                    register4 += row & 0x07;

                    for (int i = 0; i < 32; i++) {
                        int tile = ram[register2++] & 0xFF;

                        byte pattern = ram[register4 + (tile << 3)];

                        byte colors = ram[register3 + (tile << 3)];
                        byte c0 = (byte) (colors & 0x0F);
                        if (c0 == 0) {
                            c0 = backdrop;
                        }
                        byte c1 = (byte) ((colors & 0xF0) >> 4);
                        if (c1 == 0) {
                            c1 = backdrop;
                        }

                        imageData.data[index++] = (pattern & 0x80) != 0 ? c1 : c0;
                        imageData.data[index++] = (pattern & 0x40) != 0 ? c1 : c0;
                        imageData.data[index++] = (pattern & 0x20) != 0 ? c1 : c0;
                        imageData.data[index++] = (pattern & 0x10) != 0 ? c1 : c0;
                        imageData.data[index++] = (pattern & 0x08) != 0 ? c1 : c0;
                        imageData.data[index++] = (pattern & 0x04) != 0 ? c1 : c0;
                        imageData.data[index++] = (pattern & 0x02) != 0 ? c1 : c0;
                        imageData.data[index++] = (pattern & 0x01) != 0 ? c1 : c0;
                    }
                }
                else {
                    register2 += (row >> 3) * 32;
                    register4 += row & 0x07;

                    for (int i = 0; i < 32; i++) {
                        int tile = ram[register2++] & 0xFF;

                        byte pattern = ram[register4 + (tile << 3)];

                        byte colors = ram[register3 + (tile >> 3)];
                        byte c0 = (byte) (colors & 0x0F);
                        if (c0 == 0) {
                            c0 = backdrop;
                        }
                        byte c1 = (byte) ((colors & 0xF0) >> 4);
                        if (c1 == 0) {
                            c1 = backdrop;
                        }

                        imageData.data[index++] = (pattern & 0x80) != 0 ? c1 : c0;
                        imageData.data[index++] = (pattern & 0x40) != 0 ? c1 : c0;
                        imageData.data[index++] = (pattern & 0x20) != 0 ? c1 : c0;
                        imageData.data[index++] = (pattern & 0x10) != 0 ? c1 : c0;
                        imageData.data[index++] = (pattern & 0x08) != 0 ? c1 : c0;
                        imageData.data[index++] = (pattern & 0x04) != 0 ? c1 : c0;
                        imageData.data[index++] = (pattern & 0x02) != 0 ? c1 : c0;
                        imageData.data[index++] = (pattern & 0x01) != 0 ? c1 : c0;
                    }
                }

                int size = 8;
                if ((reg[1] & TMSSPRSIZE) != 0) {
                    size <<= 1;
                }
                if ((reg[1] & TMSSPRMAG) != 0) {
                    size <<= 1;
                }

                int count = 0;
                for (int sprite = 0; sprite < 32 && count < 4; sprite++, register5 += 4) {
                    int y = ram[register5] & 0xFF;
                    if (y == 0xD0) {
                        break;
                    }

                    int x = ram[register5 + 1] & 0xFF;
                    if ((ram[register5 + 3] & 0x80) != 0) {
                        x -= 32;
                    }
                    int tile = ram[register5 + 2] & 0xFF;

                    byte c1 = (byte) (ram[register5 + 3] & 0x0F);
                    if (c1 == 0) {
                        continue;
                    }

                    int offset = row - y;
                    if (offset < 0 || offset >= size) {
                        continue;
                    }
                    index = (row + FRAME_TOP) * FRAME_WIDTH + FRAME_LEFT + x;

                    if ((reg[1] & TMSSPRMAG) != 0) {
                        offset >>= 1;
                    }

                    int patternIndex = register6 + (tile << 3) + offset;

                    int pattern = ram[patternIndex] & 0xFF;
                    if ((reg[1] & TMSSPRSIZE) != 0) {
                        pattern = (pattern << 8) | (ram[patternIndex + 16] & 0xFF);
                        for (int i = 0x8000; i != 0x0000; i >>= 1) {
                            if (x >= 0 && x < 256) {
                                if ((pattern & i) != 0) {
                                    imageData.data[index] = c1;
                                }
                                if ((reg[1] & TMSSPRMAG) != 0) {
                                    x++;
                                    index++;
                                    if (x >= 0 && x < 256) {
                                        if ((pattern & i) != 0) {
                                            imageData.data[index] = c1;
                                        }
                                    }
                                }
                            }
                            x++;
                            index++;
                        }
                    }
                    else {
                        for (int i = 0x80; i != 0x00; i >>= 1) {
                            if (x >= 0 && x < 256) {
                                if ((pattern & i) != 0) {
                                    imageData.data[index] = c1;
                                }
                                if ((reg[1] & TMSSPRMAG) != 0) {
                                    x++;
                                    index++;
                                    if (x >= 0 && x < 256) {
                                        if ((pattern & i) != 0) {
                                            imageData.data[index] = c1;
                                        }
                                    }
                                }
                            }
                            x++;
                            index++;
                        }
                    }

                    count++;
                }
            }
        }
    }

    public void redrawFrame() {
        for (int row = 0; row < FRAME_HEIGHT; row++) {
            processRow(row);
        }
        status |= 0x80;
    }

    public ImageData getImageData() {
        return imageData;
    }

    public int getStatus() {
        return status & 0xFF;
    }

    public void setStatus(int status) {
        this.status = (byte) status;
    }

    public int getReg(int index) {
        return reg[index] & 0xFF;
    }

    public void setReg(int index, int value) {
        reg[index] = (byte) value;
    }

    public int getRegAddr(int index) {
        switch (index) {
            case 2:
                return (reg[2] << 10) & 0x3FFF;
            case 3:
                return (reg[3] << 6) & 0x3FFF;
            case 4:
                return (reg[4] << 11) & 0x3FFF;
            case 5:
                return (reg[5] << 7) & 0x3FFF;
            case 6:
                return (reg[6] << 11) & 0x3FFF;
        }
        return 0;
    }

    public byte[] getRegs() {
        return this.reg;
    }

    public void setRegs(byte[] regs) {
        System.arraycopy(regs, 0, this.reg, 0, Math.min(regs.length, this.reg.length));
    }

    public byte[] getRam() {
        return this.ram;
    }

    public void setRam(byte[] ram) {
        System.arraycopy(ram, 0, this.ram, 0, Math.min(ram.length, this.ram.length));
    }

}
