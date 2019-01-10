/*
 * Copyright (c) 2018 Marco Maccaferri and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.maccasoft.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import com.maccasoft.tools.editor.EditorUtil;
import com.maccasoft.tools.editor.LineNumbersRuler;
import com.maccasoft.tools.editor.Token;
import com.maccasoft.tools.editor.TokenId;
import com.maccasoft.tools.editor.TokenMarker;

import nl.grauw.glass.Line;
import nl.grauw.glass.Source;

public class SourceViewer {

    Composite container;
    LineNumbersRuler ruler;
    CodeRuler codeRuler;
    StyledText text;
    TokenMarker tokenMarker;

    private int currentLine;
    private Color currentLineBackground;

    private Font font;
    private Font fontBold;
    private Map<TokenId, TextStyle> styleMap = new HashMap<TokenId, TextStyle>();

    Source source;

    boolean showLineNumbers = true;

    private final CaretListener caretListener = new CaretListener() {

        @Override
        public void caretMoved(CaretEvent event) {
            int offset = text.getCaretOffset();
            int line = text.getLineAtOffset(offset);
            if (line != currentLine) {
                if (currentLine >= 0 && currentLine < text.getLineCount()) {
                    text.setLineBackground(currentLine, 1, null);
                }
                text.setLineBackground(line, 1, currentLineBackground);
                currentLine = line;
            }
        }
    };

    public SourceViewer(Composite parent, TokenMarker tokenMarker) {
        this.tokenMarker = tokenMarker;
        createTextEditor(parent);
    }

    protected void createTextEditor(Composite parent) {
        container = new Composite(parent, SWT.BORDER);
        GridLayout containerLayout = new GridLayout(3, false);
        containerLayout.horizontalSpacing = 1;
        containerLayout.marginWidth = containerLayout.marginHeight = 0;
        container.setLayout(containerLayout);

        if ("win32".equals(SWT.getPlatform())) {
            font = new Font(Display.getDefault(), "Courier New", 9, SWT.NONE);
            fontBold = new Font(Display.getDefault(), "Courier New", 9, SWT.BOLD);
        }
        else {
            font = new Font(Display.getDefault(), "mono", 9, SWT.NONE);
            fontBold = new Font(Display.getDefault(), "mono", 9, SWT.BOLD);
        }

        ruler = new LineNumbersRuler(container);

        codeRuler = new CodeRuler(container);

        text = new StyledText(container, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        text.setEditable(false);
        text.setDoubleClickEnabled(false);
        text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        text.setMargins(5, 5, 5, 5);
        text.setTabs(4);
        text.setFont(font);
        text.setCaret(null);

        ruler.setFont(font);
        ruler.setText(text);

        codeRuler.setFont(font, fontBold);
        codeRuler.setText(text);

        currentLine = 0;
        currentLineBackground = new Color(Display.getDefault(), 232, 242, 254);
        text.setLineBackground(currentLine, 1, currentLineBackground);

        updateTokenStyles();

        text.addCaretListener(caretListener);

        text.addLineStyleListener(new LineStyleListener() {

            @Override
            public void lineGetStyle(LineStyleEvent event) {
                List<StyleRange> ranges = new ArrayList<StyleRange>();

                Token token = tokenMarker.markTokens(event.lineText, event.lineOffset);

                int offset = event.lineOffset;
                while (token != null) {
                    TextStyle style = styleMap.get(token.id);
                    if (style != null) {
                        StyleRange range = new StyleRange(style);
                        range.start = offset;
                        range.length = token.length;
                        ranges.add(range);
                    }
                    offset += token.length;
                    token = token.next;
                }

                event.styles = ranges.toArray(new StyleRange[ranges.size()]);
            }
        });

        text.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                font.dispose();
                fontBold.dispose();
                for (TextStyle style : styleMap.values()) {
                    if (style.foreground != null) {
                        style.foreground.dispose();
                    }
                }
                currentLineBackground.dispose();
            }
        });

        container.setTabList(new Control[] {
            text
        });
    }

    public Control getControl() {
        return container;
    }

    public StyledText getStyledText() {
        return text;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;

        StringBuilder sb = new StringBuilder();
        for (Line line : source.getLines()) {
            sb.append(line.getSourceText());
            sb.append("\r\n");
        }
        String text = sb.toString();

        codeRuler.setSource(source);

        currentLine = 0;
        this.text.setText(text);
        this.text.setLineBackground(currentLine, 1, currentLineBackground);
        tokenMarker.refreshMultilineComments(text);
    }

    public String getText() {
        String s = text.getText();
        String s2 = s.replaceAll("[ \\t]+(\r\n|\n|\r)", "$1");
        if (!s2.equals(s)) {
            int offset = text.getCaretOffset();
            int line = text.getLineAtOffset(offset);
            int column = offset - text.getOffsetAtLine(line);
            int topindex = text.getTopIndex();

            text.setRedraw(false);
            try {
                text.setText(s2);
                text.setTopIndex(topindex);

                String ls = text.getLine(line);
                text.setCaretOffset(text.getOffsetAtLine(line) + Math.min(column, ls.length()));
            } finally {
                text.setRedraw(true);
            }
        }
        return s2;
    }

    public void clearKeywords() {
        tokenMarker.clearKeywords();
    }

    public void addKeyword(String text, TokenId id) {
        tokenMarker.addKeyword(text, id);
    }

    public void removeKeyword(String text) {
        tokenMarker.removeKeyword(text);
    }

    void goToLine() {
        IInputValidator validator = new IInputValidator() {

            @Override
            public String isValid(String newText) {
                try {
                    int i = Integer.parseInt(newText);
                    if (i < 1 || i > text.getLineCount()) {
                        return "Line number out of range";
                    }
                } catch (Exception e) {
                    return "Not a number";
                }
                return null;
            }
        };
        String msg = String.format("Enter line number (1..%d):", text.getLineCount());
        int line = text.getLineAtOffset(text.getCaretOffset());
        InputDialog dlg = new InputDialog(text.getShell(), "Go to Line", msg, String.format("%d", line), validator);
        if (dlg.open() == InputDialog.OK) {
            line = Integer.parseInt(dlg.getValue()) - 1;
            text.setCaretOffset(text.getOffsetAtLine(line));
            text.setTopIndex(line > 10 ? line - 10 : 1);
        }
    }

    public void cut() {
        text.cut();
    }

    public void copy() {
        text.copy();
    }

    public void paste() {
        final VerifyListener verifyListener = new VerifyListener() {

            @Override
            public void verifyText(VerifyEvent e) {
                if (e.text != null) {
                    e.text = EditorUtil.replaceTabs(e.text, text.getTabs());
                }
            }
        };
        text.addVerifyListener(verifyListener);
        text.paste();
        text.removeVerifyListener(verifyListener);
    }

    public void selectAll() {
        text.selectAll();
    }

    public void addModifyListener(ModifyListener listener) {
        text.addModifyListener(listener);
    }

    public void removeModifyListener(ModifyListener listener) {
        text.removeModifyListener(listener);
    }

    public void addCaretListener(CaretListener listener) {
        text.addCaretListener(listener);
    }

    public void removeCaretListener(CaretListener listener) {
        text.removeCaretListener(listener);
    }

    public void gotToLineColumn(int line, int column) {
        if (line >= text.getLineCount()) {
            return;
        }

        Rectangle rect = text.getClientArea();
        int bottomIndex = text.getLineIndex(rect.height - text.getLineHeight());
        if (line <= text.getTopIndex() || line >= bottomIndex) {
            text.setTopIndex(line > 10 ? line - 10 : 1);
        }

        int offset = text.getOffsetAtLine(line);
        text.setCaretOffset(offset + column);

        text.showSelection();

        ruler.redraw();
        codeRuler.redraw();
    }

    public boolean isShowLineNumbers() {
        return showLineNumbers;
    }

    public void setShowLineNumbers(boolean showLineNumbers) {
        if (this.showLineNumbers == showLineNumbers) {
            return;
        }
        this.showLineNumbers = showLineNumbers;

        ruler.setVisible(showLineNumbers);

        container.getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                if (container.isDisposed()) {
                    return;
                }
                container.layout();
                text.redraw();
                text.setCaretOffset(text.getCaretOffset());
            }
        });
    }

    public void toggleLineNumbers() {
        setShowLineNumbers(!showLineNumbers);
    }

    public void setFont(String name, int size) {
        Font oldFont = font;
        Font oldFontBold = fontBold;

        font = new Font(Display.getDefault(), name, size, SWT.NONE);
        fontBold = new Font(Display.getDefault(), name, size, SWT.BOLD);

        updateTokenStyles();
        text.setStyleRanges(new StyleRange[0]);
        text.setFont(font);

        text.redraw();
        text.setCaretOffset(text.getCaretOffset());

        ruler.setFont(font);
        codeRuler.setFont(font, fontBold);

        container.getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                if (!container.isDisposed()) {
                    container.layout();
                }
            }
        });

        if (oldFont != null) {
            oldFont.dispose();
        }
        if (oldFontBold != null) {
            oldFontBold.dispose();
        }
    }

    public void setDefaultFont() {
        if ("win32".equals(SWT.getPlatform())) {
            setFont("Courier New", 9);
        }
        else {
            setFont("mono", 9);
        }
    }

    void updateTokenStyles() {
        styleMap.put(TokenId.Comment, new TextStyle(font, new Color(Display.getDefault(), 0x00, 0x00, 0xFF), null));

        styleMap.put(TokenId.Directive1, new TextStyle(font, new Color(Display.getDefault(), 0xA0, 0x20, 0xF0), null));
        styleMap.put(TokenId.Directive2, new TextStyle(fontBold, new Color(Display.getDefault(), 0x2E, 0x8B, 0x57), null));

        styleMap.put(TokenId.Instruction, new TextStyle(fontBold, new Color(Display.getDefault(), 0xA5, 0x2A, 0x2A), null));
        styleMap.put(TokenId.Flag, new TextStyle(fontBold, new Color(Display.getDefault(), 0xA5, 0x2A, 0x2A), null));
        styleMap.put(TokenId.Register, new TextStyle(fontBold, new Color(Display.getDefault(), 0xA5, 0x2A, 0x2A), null));

        styleMap.put(TokenId.StringLiteral1, new TextStyle(font, new Color(Display.getDefault(), 0xFF, 0x00, 0xFF), null));
        styleMap.put(TokenId.StringLiteral2, new TextStyle(font, new Color(Display.getDefault(), 0xFF, 0x00, 0xFF), null));
        styleMap.put(TokenId.NumberLiteral, new TextStyle(font, new Color(Display.getDefault(), 0xFF, 0x00, 0xFF), null));
    }

    public void toggleBreakpoint(int address) {
        codeRuler.toggleBreakpoint(address);
        codeRuler.redraw();
    }

    public boolean isBreakpoint(int address) {
        return codeRuler.isBreakpoint(address);
    }

    public void resetBreakpoints() {
        codeRuler.resetBreakpoints();
        codeRuler.redraw();
    }

}
