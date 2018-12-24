/*
 * Copyright (c) 2018 Marco Maccaferri and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.maccasoft.tools.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.MovementEvent;
import org.eclipse.swt.custom.MovementListener;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

public class SourceEditor {

    private static final int UNDO_LIMIT = 500;
    private static final int CURRENT_CHANGE_TIMER_EXPIRE = 500;

    Composite container;
    LineNumbersRuler ruler;
    StyledText text;
    TokenMarker tokenMarker;

    private int currentLine;
    private Color currentLineBackground;

    private Font font;
    private Font fontBold;
    private Map<TokenId, TextStyle> styleMap = new HashMap<TokenId, TextStyle>();

    Caret insertCaret;
    Caret overwriteCaret;
    SearchBox searchBox;

    TextChange currentChange;
    Stack<TextChange> undoStack = new Stack<TextChange>();
    Stack<TextChange> redoStack = new Stack<TextChange>();
    boolean ignoreUndo;
    boolean ignoreRedo;

    boolean showLineNumbers = true;
    int[] tabStops;
    boolean useTabstops;

    class TextChange {

        int caretOffset;
        int topIndex;
        int start;
        int length;
        String replacedText;
        long timeStamp;

        TextChange(int start, int length, String replacedText, int topIndex, int caretOffset) {
            this.start = start;
            this.length = length;
            this.replacedText = replacedText;
            this.timeStamp = System.currentTimeMillis();
            this.topIndex = topIndex;
            this.caretOffset = caretOffset;
        }

        void append(int length, String replacedText) {
            this.length += length;
            this.replacedText += replacedText;
            this.timeStamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return (System.currentTimeMillis() - timeStamp) >= CURRENT_CHANGE_TIMER_EXPIRE;
        }
    }

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

    final Runnable refreshMarkersRunnable = new Runnable() {

        @Override
        public void run() {
            if (text == null || text.isDisposed()) {
                return;
            }
            tokenMarker.refreshMultilineComments(text.getText());
            text.redraw();
        }
    };

    public SourceEditor(Composite parent, TokenMarker tokenMarker) {
        this.tokenMarker = tokenMarker;
        createTextEditor(parent);
    }

    protected void createTextEditor(Composite parent) {
        container = new Composite(parent, SWT.NONE);
        GridLayout containerLayout = new GridLayout(2, false);
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

        text = new StyledText(container, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL) {

            @Override
            public void invokeAction(int action) {
                switch (action) {
                    case ST.LINE_START:
                        SourceEditor.this.doLineStart(false);
                        break;
                    case ST.LINE_END:
                        SourceEditor.this.doLineEnd(false);
                        break;
                    case ST.SELECT_LINE_START:
                        SourceEditor.this.doLineStart(true);
                        break;
                    case ST.SELECT_LINE_END:
                        SourceEditor.this.doLineEnd(true);
                        break;
                    case ST.PASTE:
                        SourceEditor.this.paste();
                        break;
                    default:
                        super.invokeAction(action);
                }
            }
        };
        text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        text.setMargins(5, 5, 5, 5);
        text.setTabs(4);
        text.setFont(font);

        ruler.setFont(font);
        ruler.setText(text);

        insertCaret = createInsertCaret(text);
        overwriteCaret = createOverwriteCaret(text);
        text.setCaret(insertCaret);

        currentLine = 0;
        currentLineBackground = new Color(Display.getDefault(), 232, 242, 254);
        text.setLineBackground(currentLine, 1, currentLineBackground);

        updateTokenStyles();

        text.addCaretListener(caretListener);
        text.addTraverseListener(new TraverseListener() {

            @Override
            public void keyTraversed(TraverseEvent e) {
                if (e.character != SWT.TAB || (e.stateMask & SWT.MODIFIER_MASK) == 0) {
                    return;
                }
                final Event e1 = new Event();
                e1.character = e.character;
                e1.stateMask = e.stateMask;
                e1.detail = e.detail;
                e.display.asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        if (!text.isDisposed()) {
                            Control control = text.getParent();
                            while (!(control instanceof CTabFolder) && control.getParent() != null) {
                                control = control.getParent();
                            }
                            control.notifyListeners(SWT.Traverse, e1);
                        }
                    }
                });
                e.doit = false;
            }
        });
        text.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == 'f') {
                    if ((e.stateMask & SWT.MODIFIER_MASK) == SWT.MOD1) {
                        if (searchBox == null) {
                            searchBox = new SearchBox(text);
                        }
                        String selection = text.getSelectionText();
                        if (!selection.isEmpty()) {
                            searchBox.setLastSearch(selection);
                        }
                        searchBox.open();
                    }
                }
                else if (e.keyCode == 'k') {
                    if ((e.stateMask & SWT.MODIFIER_MASK) == SWT.MOD1) {
                        if (searchBox == null) {
                            searchBox = new SearchBox(text);
                        }
                        searchBox.searchNext();
                    }
                }
                else if (e.keyCode == 'l') {
                    if ((e.stateMask & SWT.MODIFIER_MASK) == SWT.MOD1) {
                        goToLine();
                    }
                }
                else if (e.keyCode == SWT.INSERT && e.stateMask == 0) {
                    text.setCaret(text.getCaret() == insertCaret ? overwriteCaret : insertCaret);
                }
            }
        });
        text.addVerifyKeyListener(new VerifyKeyListener() {

            @Override
            public void verifyKey(VerifyEvent e) {
                if (e.keyCode == SWT.CR) {
                    handleAutoIndent();
                    e.doit = false;
                }
                else if (e.keyCode == SWT.TAB) {
                    e.doit = false;
                    if ((e.stateMask & SWT.CTRL) != 0) {
                        return;
                    }
                    doTab();
                }
                else if (e.keyCode == SWT.BS) {
                    e.doit = doBackspace();
                }
            }
        });

        text.addVerifyListener(new VerifyListener() {

            @Override
            public void verifyText(VerifyEvent e) {
                String replacedText = text.getTextRange(e.start, e.end - e.start);
                if (!ignoreUndo) {
                    if (currentChange == null || currentChange.isExpired()) {
                        undoStack.push(currentChange = new TextChange(e.start, e.text.length(), replacedText, text.getTopIndex(), text.getCaretOffset()));
                        if (undoStack.size() > UNDO_LIMIT) {
                            undoStack.remove(0);
                        }
                    }
                    else {
                        if (e.start != currentChange.start + currentChange.length) {
                            undoStack.push(currentChange = new TextChange(e.start, e.text.length(), replacedText, text.getTopIndex(), text.getCaretOffset()));
                            if (undoStack.size() > UNDO_LIMIT) {
                                undoStack.remove(0);
                            }
                        }
                        else {
                            currentChange.append(e.text.length(), replacedText);
                        }
                    }
                }
                else if (!ignoreRedo) {
                    redoStack.push(new TextChange(e.start, e.text.length(), replacedText, text.getTopIndex(), text.getCaretOffset()));
                    if (redoStack.size() > UNDO_LIMIT) {
                        redoStack.remove(0);
                    }
                }
            }
        });

        text.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                e.display.timerExec(500, refreshMarkersRunnable);
            }
        });

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

        text.addWordMovementListener(new MovementListener() {

            @Override
            public void getNextOffset(MovementEvent event) {
                int offset = event.offset;
                String lineText = text.getText();
                if (offset < lineText.length()) {
                    if (event.movement == SWT.MOVEMENT_WORD_END) {
                        if (Character.isLetterOrDigit(lineText.charAt(offset)) || lineText.charAt(offset) == '_') {
                            do {
                                offset++;
                                if (offset >= lineText.length()) {
                                    return;
                                }
                            } while (Character.isLetterOrDigit(lineText.charAt(offset)) || lineText.charAt(offset) == '_');
                        }
                        event.newOffset = offset;
                        return;
                    }
                    if (Character.isDigit(lineText.charAt(offset))) {
                        do {
                            offset++;
                            if (offset >= lineText.length()) {
                                return;
                            }
                        } while (Character.isDigit(lineText.charAt(offset)));
                        event.newOffset = offset;
                        return;
                    }
                    if (Character.isLetterOrDigit(lineText.charAt(offset))) {
                        offset++;
                        if (offset >= lineText.length()) {
                            return;
                        }
                        boolean lowerCase = Character.isLowerCase(lineText.charAt(offset));
                        do {
                            offset++;
                            if (offset >= lineText.length()) {
                                return;
                            }
                        } while (Character.isLetterOrDigit(lineText.charAt(offset)) && Character.isLowerCase(lineText.charAt(offset)) == lowerCase);
                        if (Character.isLetterOrDigit(lineText.charAt(offset))) {
                            event.newOffset = offset;
                            return;
                        }
                        if (lineText.charAt(offset) == '_') {
                            while (lineText.charAt(offset) == '_') {
                                offset++;
                                if (offset >= lineText.length()) {
                                    return;
                                }
                            }
                            event.newOffset = offset;
                            return;
                        }
                        if (EditorUtil.isSeparator(lineText.charAt(offset))) {
                            event.newOffset = offset;
                            return;
                        }
                    }
                    if (EditorUtil.isWhitespace(lineText.charAt(offset))) {
                        do {
                            offset++;
                            if (offset >= lineText.length()) {
                                return;
                            }
                        } while (EditorUtil.isWhitespace(lineText.charAt(offset)));
                        event.newOffset = offset;
                    }
                    else if (EditorUtil.isSeparator(lineText.charAt(offset))) {
                        do {
                            offset++;
                            if (offset >= lineText.length()) {
                                return;
                            }
                        } while (EditorUtil.isSeparator(lineText.charAt(offset)));
                        if (EditorUtil.isWhitespace(lineText.charAt(offset))) {
                            do {
                                offset++;
                                if (offset >= lineText.length()) {
                                    return;
                                }
                            } while (EditorUtil.isWhitespace(lineText.charAt(offset)));
                        }
                        event.newOffset = offset;
                    }
                }
            }

            @Override
            public void getPreviousOffset(MovementEvent event) {
                int offset = event.offset;
                String lineText = text.getText();
                if (offset > 0 && offset < lineText.length()) {
                    offset--;
                    if (event.movement == SWT.MOVEMENT_WORD_START) {
                        if (Character.isLetterOrDigit(lineText.charAt(offset)) || lineText.charAt(offset) == '_') {
                            do {
                                offset--;
                                if (offset < 0) {
                                    return;
                                }
                            } while (Character.isLetterOrDigit(lineText.charAt(offset)) || lineText.charAt(offset) == '_');
                        }
                        event.newOffset = offset + 1;
                        return;
                    }
                    if (EditorUtil.isWhitespace(lineText.charAt(offset))) {
                        do {
                            offset--;
                            if (offset < 0) {
                                return;
                            }
                        } while (EditorUtil.isWhitespace(lineText.charAt(offset)));
                        if (EditorUtil.isSeparator(lineText.charAt(offset))) {
                            while (offset > 0 && EditorUtil.isSeparator(lineText.charAt(offset - 1))) {
                                offset--;
                            }
                            event.newOffset = offset;
                            return;
                        }
                    }
                    if (Character.isDigit(lineText.charAt(offset))) {
                        do {
                            offset--;
                            if (offset < 0) {
                                return;
                            }
                        } while (Character.isDigit(lineText.charAt(offset)));
                        event.newOffset = offset + 1;
                        return;
                    }
                    while (lineText.charAt(offset) == '_') {
                        offset--;
                        if (offset < 0) {
                            return;
                        }
                    }
                    if (Character.isLetterOrDigit(lineText.charAt(offset))) {
                        if (Character.isLowerCase(lineText.charAt(offset))) {
                            do {
                                offset--;
                                if (offset < 0) {
                                    return;
                                }
                            } while (Character.isLetterOrDigit(lineText.charAt(offset)) && Character.isLowerCase(lineText.charAt(offset)));
                            if (Character.isLetterOrDigit(lineText.charAt(offset))) {
                                event.newOffset = offset;
                                return;
                            }
                        }
                        else {
                            do {
                                offset--;
                                if (offset < 0) {
                                    return;
                                }
                            } while (Character.isLetterOrDigit(lineText.charAt(offset)));
                        }
                        if (EditorUtil.isSeparator(lineText.charAt(offset)) || EditorUtil.isWhitespace(lineText.charAt(offset))) {
                            event.newOffset = offset + 1;
                            return;
                        }
                    }
                    if (EditorUtil.isSeparator(lineText.charAt(offset))) {
                        do {
                            offset--;
                            if (offset < 0) {
                                return;
                            }
                        } while (EditorUtil.isSeparator(lineText.charAt(offset)));
                        if (!EditorUtil.isSeparator(lineText.charAt(offset))) {
                            offset++;
                        }
                        event.newOffset = offset;
                    }
                }
            }
        });

        text.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                if (searchBox != null) {
                    searchBox.dispose();
                }

                font.dispose();
                fontBold.dispose();

                for (TextStyle style : styleMap.values()) {
                    if (style.foreground != null) {
                        style.foreground.dispose();
                    }
                }
                currentLineBackground.dispose();

                insertCaret.dispose();
                overwriteCaret.dispose();
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

    public void setText(String text) {
        this.text.removeCaretListener(caretListener);

        ignoreUndo = true;
        ignoreRedo = true;

        text = text.replaceAll("[ \\t]+(\r\n|\n|\r)", "$1");
        text = EditorUtil.replaceTabs(text, this.text.getTabs());

        currentLine = 0;
        this.text.setText(text);
        this.text.setLineBackground(currentLine, 1, currentLineBackground);

        undoStack = new Stack<TextChange>();
        redoStack = new Stack<TextChange>();
        ignoreUndo = false;
        ignoreRedo = false;

        this.text.addCaretListener(caretListener);
    }

    public void replaceText(String text) {
        int offset = this.text.getCaretOffset();
        int line = this.text.getLineAtOffset(offset);
        int topindex = line - this.text.getTopIndex();

        text = EditorUtil.replaceTabs(text, this.text.getTabs());
        this.text.setText(text);

        if (line > this.text.getLineCount()) {
            line = this.text.getLineCount() - 1;
        }
        this.text.setTopIndex(line - topindex);
        this.text.setCaretOffset(this.text.getOffsetAtLine(line));
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

    private Caret createInsertCaret(StyledText styledText) {
        Caret caret = new Caret(styledText, SWT.NULL);
        caret.setSize(2, styledText.getLineHeight());
        caret.setFont(styledText.getFont());
        return caret;
    }

    private Caret createOverwriteCaret(StyledText styledText) {
        Caret caret = new Caret(styledText, SWT.NULL);

        GC gc = new GC(styledText);
        Point charSize = gc.stringExtent("a"); //$NON-NLS-1$

        caret.setSize(charSize.x, styledText.getLineHeight());
        caret.setFont(styledText.getFont());

        gc.dispose();

        return caret;
    }

    void handleAutoIndent() {
        int i;
        int offset = text.getCaretOffset();
        int lineIndex = text.getLineAtOffset(offset);
        String lineText = text.getLine(lineIndex);
        int lineOffset = offset - text.getOffsetAtLine(lineIndex);
        int index = 0;
        StringBuilder sb = new StringBuilder();

        i = 0;
        while (i < lineText.length() && (lineText.charAt(i) == ' ' || lineText.charAt(i) == '\t')) {
            sb.append(' ');
            i++;
        }

        i = lineOffset;
        while (i < lineText.length() && (lineText.charAt(i) == ' ' || lineText.charAt(i) == '\t') && index < sb.length()) {
            index++;
            i++;
        }
        sb.insert(index, text.getLineDelimiter());

        text.setRedraw(false);
        try {
            text.insert(sb.toString());
            text.setCaretOffset(offset + sb.length());
        } finally {
            text.setRedraw(true);
        }
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

    public void undo() {
        if (undoStack.empty()) {
            return;
        }

        TextChange change = undoStack.pop();

        ignoreUndo = true;
        try {
            text.setRedraw(false);
            text.replaceTextRange(change.start, change.length, change.replacedText);
            text.setCaretOffset(change.caretOffset);
            text.setTopIndex(change.topIndex);
        } finally {
            text.setRedraw(true);
            ignoreUndo = false;
        }
    }

    public void redo() {
        if (redoStack.empty()) {
            return;
        }

        TextChange change = redoStack.pop();

        ignoreRedo = true;
        try {
            text.setRedraw(false);
            text.replaceTextRange(change.start, change.length, change.replacedText);
            text.setCaretOffset(change.caretOffset);
            text.setTopIndex(change.topIndex);
        } finally {
            text.setRedraw(true);
            ignoreRedo = false;
        }
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

    public void setFont(String name) {
        if (name == null || "".equals(name)) {
            if ("win32".equals(SWT.getPlatform())) {
                name = StringConverter.asString(new FontData("Courier New", 9, SWT.NONE));
            }
            else {
                name = StringConverter.asString(new FontData("mono", 9, SWT.NONE));
            }
        }
        FontData fontData = StringConverter.asFontData(name);
        setFont(fontData.getName(), fontData.getHeight());
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

    void doLineStart(boolean doSelect) {
        int offset = text.getCaretOffset();
        int lineNumber = text.getLineAtOffset(offset);
        int lineOffset = offset - text.getOffsetAtLine(lineNumber);
        String line = text.getLine(lineNumber);
        int nonBlankOffset = 0;
        while (nonBlankOffset < line.length() && (line.charAt(nonBlankOffset) == ' ' || line.charAt(nonBlankOffset) == '\t')) {
            nonBlankOffset++;
        }
        if (lineOffset == nonBlankOffset) {
            lineOffset = 0;
        }
        else {
            lineOffset = nonBlankOffset;
        }

        text.setRedraw(false);
        try {
            int newOffset = lineOffset + text.getOffsetAtLine(lineNumber);
            if (doSelect) {
                Point selection = text.getSelection();
                if (offset == selection.x) {
                    text.setSelection(selection.y, newOffset);
                }
                else if (offset == selection.y) {
                    text.setSelection(selection.x, newOffset);
                }
                else {
                    text.setSelection(offset, newOffset);
                }
            }
            text.setCaretOffset(newOffset);
            text.setHorizontalIndex(0);
            text.showSelection();
        } finally {
            text.setRedraw(true);
        }
    }

    void doLineEnd(boolean doSelect) {
        int offset = text.getCaretOffset();
        int lineNumber = text.getLineAtOffset(offset);
        int lineOffset = offset - text.getOffsetAtLine(lineNumber);
        String line = text.getLine(lineNumber);
        int nonBlankOffset = line.length();
        while (nonBlankOffset > 0 && (line.charAt(nonBlankOffset - 1) == ' ' || line.charAt(nonBlankOffset - 1) == '\t')) {
            nonBlankOffset--;
        }
        if (lineOffset == nonBlankOffset) {
            lineOffset = line.length();
        }
        else {
            lineOffset = nonBlankOffset;
        }

        text.setRedraw(false);
        try {
            int newOffset = lineOffset + text.getOffsetAtLine(lineNumber);
            if (doSelect) {
                Point selection = text.getSelection();
                if (offset == selection.x) {
                    text.setSelection(selection.y, newOffset);
                }
                else if (offset == selection.y) {
                    text.setSelection(selection.x, newOffset);
                }
                else {
                    text.setSelection(offset, newOffset);
                }
            }
            text.setCaretOffset(newOffset);
            text.showSelection();
        } finally {
            text.setRedraw(true);
        }
    }

    void doTab() {
        int offset = text.getCaretOffset();
        int lineNumber = text.getLineAtOffset(offset);
        int lineStart = text.getOffsetAtLine(lineNumber);
        int lineOffset = offset - lineStart;

        int tabStop = ((lineOffset + text.getTabs()) / text.getTabs()) * text.getTabs();
        if (useTabstops && tabStops != null) {
            for (int i = tabStops.length - 1; i >= 0; i--) {
                if (lineOffset < tabStops[i]) {
                    tabStop = tabStops[i];
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        while (sb.length() < (tabStop - lineOffset)) {
            sb.append(' ');
        }

        text.setRedraw(false);
        try {
            text.insert(sb.toString());
            text.setCaretOffset(lineStart + tabStop);
            text.showSelection();
        } finally {
            text.setRedraw(true);
        }
    }

    boolean doBackspace() {
        int offset = text.getCaretOffset();
        int lineNumber = text.getLineAtOffset(offset);
        int lineStart = text.getOffsetAtLine(lineNumber);

        if (offset == lineStart || !useTabstops) {
            return true;
        }

        int lineOffset = offset - lineStart;

        int tabStop = (lineOffset / text.getTabs()) * text.getTabs();
        if (tabStop == lineOffset) {
            tabStop -= text.getTabs();
        }

        if (tabStops != null) {
            for (int i = 0; i < tabStops.length; i++) {
                if (tabStops[i] < lineOffset) {
                    tabStop = tabStops[i];
                }
            }
        }

        String s = text.getLine(lineNumber).substring(tabStop, lineOffset);
        while (s.endsWith(" ")) {
            s = s.substring(0, s.length() - 1);
        }
        if (s.length() != 0) {
            s = s + " ";
            if ((s.length() + tabStop) >= lineOffset) {
                return true;
            }
        }

        text.setRedraw(false);
        try {
            text.replaceTextRange(lineStart + tabStop, lineOffset - tabStop, s);
            text.showSelection();
        } finally {
            text.setRedraw(true);
        }

        return false;
    }

    public int[] getTabStops() {
        return tabStops;
    }

    public void setTabStops(int[] tabStops) {
        this.tabStops = new int[tabStops.length + 1];
        this.tabStops[0] = 0;
        System.arraycopy(tabStops, 0, this.tabStops, 1, tabStops.length);
    }

    public boolean isUseTabstops() {
        return useTabstops;
    }

    public void setUseTabstops(boolean useTabstops) {
        this.useTabstops = useTabstops;
    }

    public void setTabWidth(int width) {
        text.setTabs(width);
    }

    public int getTabWidth() {
        return text.getTabs();
    }

}
