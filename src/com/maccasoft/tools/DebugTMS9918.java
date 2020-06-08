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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.maccasoft.tools.internal.ImageRegistry;

public class DebugTMS9918 {

    Display display;
    Shell shell;

    Memory memory;

    Canvas canvas;

    Text[] reg;
    Label[] regPtr;

    TMS9918 tms9918;
    ImageData imageData;
    Image image;

    Font font;

    final Runnable redrawRunnable = new Runnable() {

        @Override
        public void run() {
            if (canvas == null || canvas.isDisposed()) {
                return;
            }

            memory.getControl().redraw();

            if (image != null) {
                image.dispose();
            }
            image = new Image(display, imageData);

            GC gc = new GC(canvas);
            try {
                Rectangle canvasBounds = canvas.getBounds();
                gc.setAdvanced(false);
                gc.setAntialias(SWT.OFF);
                gc.setInterpolation(SWT.NONE);
                gc.drawImage(image, 0, 0, imageData.width, imageData.height, 0, 0, canvasBounds.width, canvasBounds.height);
            } finally {
                gc.dispose();
            }

            for (int i = 0; i < reg.length; i++) {
                reg[i].setText(String.format("%02X", tms9918.getReg(i)));
                if (i >= 2 && i <= 6) {
                    regPtr[i].setText(String.format("%04X", tms9918.getRegAddr(i)));
                }
            }
        }
    };

    public DebugTMS9918() {

    }

    public void open() {
        shell = new Shell();
        shell.setText("Debug TMS9918");
        shell.setData(this);

        display = shell.getDisplay();

        Image[] images = new Image[] {
            ImageRegistry.getImageFromResources("app128.png"),
            ImageRegistry.getImageFromResources("app64.png"),
            ImageRegistry.getImageFromResources("app48.png"),
            ImageRegistry.getImageFromResources("app32.png"),
            ImageRegistry.getImageFromResources("app16.png"),
        };
        shell.setImages(images);

        shell.setLayout(new GridLayout(1, false));

        Control control = createContents(shell);
        control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Point size = control.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);

        Rectangle screen = display.getBounds();

        Rectangle rect = shell.computeTrim(0, 0, size.x, size.y);
        rect.x = (screen.width - rect.width) / 2;
        rect.y = (screen.height - rect.height) / 2;
        if (rect.y < 0) {
            rect.height += rect.y * 2;
            rect.y = 0;
        }
        shell.setBounds(rect);

        shell.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                if (image != null) {
                    image.dispose();
                }
                image = null;

                if (font != null) {
                    font.dispose();
                }
                font = null;
            }
        });

        redrawRunnable.run();

        shell.open();
    }

    protected Control createContents(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(3, false);
        layout.marginWidth = layout.marginHeight = 0;
        container.setLayout(layout);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createMemoryPanel(container);
        createCanvas(container);
        createRegistersGroup(container);

        container.setTabList(new Control[] {
            canvas
        });

        return container;
    }

    void createMemoryPanel(Composite parent) {
        memory = new Memory(parent);
        memory.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        memory.setData(tms9918.getRam());
    }

    void createCanvas(Composite parent) {
        canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED | SWT.NO_BACKGROUND);
        canvas.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
        canvas.setBounds(canvas.computeTrim(0, 0, imageData.width * 2, imageData.height * 2));

        canvas.addPaintListener(new PaintListener() {

            @Override
            public void paintControl(PaintEvent e) {
                if (image == null || image.isDisposed()) {
                    return;
                }
                Rectangle canvasBounds = canvas.getBounds();
                e.gc.setAdvanced(false);
                e.gc.setAntialias(SWT.OFF);
                e.gc.setInterpolation(SWT.NONE);
                e.gc.drawImage(image, 0, 0, imageData.width, imageData.height, 0, 0, canvasBounds.width, canvasBounds.height);
            }
        });

        Rectangle rect = canvas.getBounds();
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = rect.width;
        gridData.heightHint = rect.height;
        canvas.setLayoutData(gridData);
    }

    void createRegistersGroup(Composite parent) {
        Composite container = new Composite(parent, SWT.BORDER);
        GridLayout layout = new GridLayout(3, false);
        container.setLayout(layout);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

        if ("win32".equals(SWT.getPlatform())) {
            font = new Font(display, "Courier New", 9, SWT.NONE);
        }
        else {
            font = new Font(display, "mono", 9, SWT.NONE);
        }

        reg = new Text[8];
        regPtr = new Label[reg.length];

        for (int i = 0; i < reg.length; i++) {
            Label label = new Label(container, SWT.NONE);
            label.setText("R" + i);
            label.setFont(font);

            reg[i] = new Text(container, SWT.BORDER);
            reg[i].setLayoutData(new GridData(20, SWT.DEFAULT));
            reg[i].setFont(font);
            reg[i].setData(i);
            reg[i].addFocusListener(new FocusListener() {

                @Override
                public void focusLost(FocusEvent e) {
                    updateRegister((Text) e.widget);
                }

                @Override
                public void focusGained(FocusEvent e) {
                    ((Text) e.widget).selectAll();
                }
            });
            reg[i].addKeyListener(new KeyAdapter() {

                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.keyCode == SWT.CR) {
                        updateRegister((Text) e.widget);
                    }
                }
            });

            if (i >= 2 && i <= 6) {
                regPtr[i] = new Label(container, SWT.NONE);
                regPtr[i].setLayoutData(new GridData(40, SWT.DEFAULT));
                regPtr[i].setFont(font);
            }
            else {
                new Label(container, SWT.NONE);
            }
        }
    }

    void updateRegister(Text widget) {
        try {
            String text = widget.getText();
            int value = Integer.parseInt(text.toUpperCase(), 16);
            if (value != tms9918.getReg((Integer) widget.getData())) {
                tms9918.setReg((Integer) widget.getData(), value);
                tms9918.redrawFrame();
                display.asyncExec(redrawRunnable);
            }
        } catch (Exception ex) {
            // Do nothing
        }
    }

    public void setFocus() {
        canvas.setFocus();
    }

    public void dispose() {
        shell.dispose();
    }

    public Shell getShell() {
        return shell;
    }

    public void setTMS9918(TMS9918 tms9918) {
        this.tms9918 = tms9918;
        this.imageData = tms9918.getImageData();
        if (this.memory != null) {
            this.memory.setData(tms9918.getRam());
        }
    }

    public void redraw() {
        display.asyncExec(redrawRunnable);
    }

}
