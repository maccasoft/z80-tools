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
import java.io.OutputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

public class Console {
    Composite container;
    StyledText text;

    ConsoleOutputStream outputStream;
    ConsoleOutputStream errorStream;

    Font font;
    Color errorColor;

    private class ConsoleOutputStream extends OutputStream {

        boolean isError;

        int moreLinesToMark;

        StringBuilder lineBuilder;

        public ConsoleOutputStream(boolean isError) {
            this.isError = isError;

            this.moreLinesToMark = 0;

            lineBuilder = new StringBuilder();
        }

        @Override
        public void write(final int b) throws IOException {

        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            Display.getDefault().syncExec(new Runnable() {

                @Override
                public void run() {
                    appendBytes(b, off, len);
                }
            });
        }

        void appendBytes(byte[] b, int off, int len) {
            for (int i = 0; i < len; i++) {
                char c = (char) b[off + i];
                if (c == '\r') {
                    int ofs = getLastLineOffset();
                    String s = lineBuilder.toString();
                    text.replaceTextRange(ofs, text.getCharCount() - ofs, s);
                    updateStyle(ofs, s);
                    lineBuilder = new StringBuilder();
                }
                else if (c == '\n') {
                    if (lineBuilder.length() > 0) {
                        int ofs = getLastLineOffset();
                        String s = lineBuilder.toString();
                        text.replaceTextRange(ofs, text.getCharCount() - ofs, s);
                        updateStyle(ofs, s);
                        lineBuilder = new StringBuilder();
                    }
                    text.append("\n");
                }
                else {
                    lineBuilder.append(c);
                }
            }
            if (lineBuilder.length() > 0) {
                int ofs = getLastLineOffset();
                String s = lineBuilder.toString();
                text.replaceTextRange(ofs, text.getCharCount() - ofs, s);
                updateStyle(ofs, s);
            }

            text.setCaretOffset(text.getCharCount());
            if (text.getLineCount() > 0) {
                text.setTopIndex(text.getLineCount() - 1);
            }
        }

        int getLastLineOffset() {
            int index = text.getLineCount() - 1;
            return text.getOffsetAtLine(index);
        }

        void updateStyle(int start, String line) {
            if (isError) {
                text.setStyleRange(new StyleRange(start, line.length(), errorColor, null));
            }
            else if (line.contains(": error :") || line.contains(": error:")) {
                text.setStyleRange(new StyleRange(start, line.length(), errorColor, null));
                moreLinesToMark += 3;
            }
            else if (line.contains("Error : ")) {
                text.setStyleRange(new StyleRange(start, line.length(), errorColor, null));
                moreLinesToMark += 2;
            }
            else if (moreLinesToMark > 0) {
                text.setStyleRange(new StyleRange(start, line.length(), errorColor, null));
                moreLinesToMark--;
            }
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
        }

    }

    public Console(Composite parent) {
        container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = layout.marginHeight = 0;
        container.setLayout(layout);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Label label = new Label(container, SWT.NONE);
        label.setText("Console");
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.horizontalIndent = 5;
        label.setLayoutData(gridData);

        text = new StyledText(container, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        text.setMargins(5, 5, 5, 5);
        text.setTabs(4);

        if ("win32".equals(SWT.getPlatform())) {
            font = new Font(parent.getDisplay(), "Courier New", 9, SWT.NONE);
        }
        else {
            font = new Font(parent.getDisplay(), "mono", 9, SWT.NONE);
        }
        text.setFont(font);

        errorColor = parent.getDisplay().getSystemColor(SWT.COLOR_RED);

        text.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                font.dispose();
            }
        });

        outputStream = new ConsoleOutputStream(false);
        errorStream = new ConsoleOutputStream(true);
    }

    public Composite getControl() {
        return container;
    }

    public StyledText getStyledText() {
        return text;
    }

    public void clear() {
        text.setText("\r\n");
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public OutputStream getErrorStream() {
        return errorStream;
    }
}
