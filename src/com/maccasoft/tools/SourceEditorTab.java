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

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;

import com.maccasoft.tools.editor.SourceEditor;
import com.maccasoft.tools.editor.Z80TokenMarker;

public class SourceEditorTab {

    String text;
    SourceEditor editor;
    CTabItem tabItem;

    File file;
    boolean dirty;

    final ModifyListener modifyListener = new ModifyListener() {

        @Override
        public void modifyText(ModifyEvent e) {
            if (!dirty) {
                dirty = true;
                updateTabItemText();
            }
        }
    };

    public SourceEditorTab(CTabFolder parent, String text) {
        editor = new SourceEditor(parent, new Z80TokenMarker());
        editor.setText(text);

        tabItem = new CTabItem(parent, SWT.NONE);
        tabItem.setShowClose(true);
        tabItem.setControl(editor.getControl());
        tabItem.setData(this);

        editor.addModifyListener(modifyListener);

        tabItem.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {

            }
        });
    }

    public void setFocus() {
        tabItem.getParent().setSelection(tabItem);
        editor.getControl().setFocus();
    }

    public void setText(String text) {
        if (this.text != null && !text.equals(this.text)) {
            dirty = true;
        }
        this.text = text;
        updateTabItemText();
    }

    public String getText() {
        return text;
    }

    public void setToolTipText(String text) {
        tabItem.setToolTipText(text);
    }

    public String getToolTipText() {
        return tabItem.getToolTipText();
    }

    public void addModifyListener(ModifyListener listener) {
        editor.addModifyListener(listener);
    }

    public void removeModifyListener(ModifyListener listener) {
        editor.removeModifyListener(listener);
    }

    public void addDisposeListener(DisposeListener listener) {
        tabItem.addDisposeListener(listener);
    }

    public void removeDisposeListener(DisposeListener listener) {
        tabItem.removeDisposeListener(listener);
    }

    public void dispose() {
        editor.removeModifyListener(modifyListener);
        editor.getControl().dispose();
        tabItem.dispose();
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void clearDirty() {
        if (dirty) {
            dirty = false;
        }
        updateTabItemText();
    }

    void updateTabItemText() {
        String dirtyFlag = dirty ? "*" : "";
        tabItem.setText(dirtyFlag + text);
    }

    public SourceEditor getEditor() {
        return editor;
    }

    public CTabItem getTabItem() {
        return tabItem;
    }

}
