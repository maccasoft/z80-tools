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

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class TerminalFont {

    final int width;
    final int height;
    final ImageData imageData;

    Image image;
    int rowSize;

    public TerminalFont(int width, int height) {
        this.width = width;
        this.height = height;

        InputStream is = TerminalFont.class.getResourceAsStream("font" + width + "x" + height + "-1.png");
        ImageData fontImageData = new ImageData(is);

        this.imageData = new ImageData(fontImageData.width, fontImageData.height, 1,
            new PaletteData(new RGB(0, 0, 0), new RGB(255, 255, 255)));
        for (int y = 0; y < fontImageData.height; y++) {
            for (int x = 0; x < fontImageData.width; x++) {
                int pixel = fontImageData.getPixel(x, y);
                this.imageData.setPixel(x, y, pixel);
            }
        }

        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.image = new Image(Display.getDefault(), imageData);
        this.rowSize = imageData.width / width;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setForeground(RGB color) {
        imageData.palette.colors[1] = new RGB(color.red, color.green, color.blue);
        if (image != null && !image.isDisposed()) {
            image.dispose();
        }
        image = new Image(Display.getDefault(), imageData);
    }

    public void setBackground(RGB color) {
        imageData.palette.colors[0] = new RGB(color.red, color.green, color.blue);
        if (image != null && !image.isDisposed()) {
            image.dispose();
        }
        image = new Image(Display.getDefault(), imageData);
    }

    public void print(GC gc, int c, int x, int y) {
        int py = ((c & 0xFF) / rowSize) * height;
        int px = ((c & 0xFF) % rowSize) * width;
        gc.drawImage(image, px, py, width, height, x, y, width, height);
    }

    public void dispose() {
        if (image != null && !image.isDisposed()) {
            image.dispose();
        }
    }
}
