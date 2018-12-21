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
import java.io.FileFilter;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import com.maccasoft.tools.internal.ImageRegistry;

public class FileBrowser {

    TreeViewer viewer;
    File[] roots;

    Set<String> visibleExtensions;

    class FileLabelProvider extends LabelProvider {

        @Override
        public String getText(Object element) {
            return ((File) element).getName();
        }

        @Override
        public Image getImage(Object element) {
            if (((File) element).isDirectory()) {
                return ImageRegistry.getImageFromResources("folder.png");
            }
            return ImageRegistry.getImageFromResources("document-code.png");
        }

    }

    class FileTreeContentProvider implements ITreeContentProvider {

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof File[]) {
                return (File[]) inputElement;
            }
            return new File[0];
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            File[] childs = ((File) parentElement).listFiles(visibleExtensionsFilter);
            return childs;
        }

        @Override
        public Object getParent(Object element) {
            return ((File) element).getParentFile();
        }

        @Override
        public boolean hasChildren(Object element) {
            return ((File) element).isDirectory();
        }

    }

    class FileSorter extends ViewerSorter {

        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            if (((File) e1).isDirectory() && !((File) e2).isDirectory()) {
                return -1;
            }
            if (!((File) e1).isDirectory() && ((File) e2).isDirectory()) {
                return 1;
            }
            return super.compare(viewer, e1, e2);
        }

    }

    final FileFilter visibleExtensionsFilter = new FileFilter() {

        @Override
        public boolean accept(File pathname) {
            String name = pathname.getName();
            if (pathname.isDirectory()) {
                if (name.equals(".") || name.equals("..")) {
                    return false;
                }
                return true;
            }
            if (name.startsWith(".")) {
                return false;
            }
            return accept(name);
        }

        boolean accept(String name) {
            int i = name.lastIndexOf('.');
            if (i != -1) {
                return visibleExtensions.contains(name.substring(i).toLowerCase());
            }
            return false;
        }

    };

    public FileBrowser(Composite parent) {
        viewer = new TreeViewer(parent);
        viewer.setLabelProvider(new FileLabelProvider());
        viewer.setSorter(new FileSorter());
        viewer.setContentProvider(new FileTreeContentProvider());
        viewer.setUseHashlookup(true);

        visibleExtensions = new HashSet<String>();
        visibleExtensions.add(".asm");
    }

    public File[] getRoots() {
        return roots;
    }

    public void setRoots(File[] roots) {
        this.roots = roots;
        viewer.setInput(roots);
    }

    public void setRoots(String[] roots) {
        this.roots = new File[roots.length];
        for (int i = 0; i < roots.length; i++) {
            this.roots[i] = new File(roots[i]);
        }
        viewer.setInput(this.roots);
    }

    public void setSelection(File file) {
        viewer.setSelection(new StructuredSelection(file), true);
        while (viewer.getSelection().isEmpty()) {
            file = file.getAbsoluteFile().getParentFile();
            if (file == null) {
                return;
            }
            viewer.setSelection(new StructuredSelection(file), true);
        }
        viewer.setExpandedState(file, true);
    }

    public void addSelectionChangedListener(ISelectionChangedListener l) {
        viewer.addSelectionChangedListener(l);
    }

    public void removeSelectionChangedListener(ISelectionChangedListener l) {
        viewer.removeSelectionChangedListener(l);
    }

    public void addOpenListener(IOpenListener l) {
        viewer.addOpenListener(l);
    }

    public void removeOpenListener(IOpenListener l) {
        viewer.removeOpenListener(l);
    }

    public void setFocus() {
        viewer.getControl().setFocus();
    }
}
