/*
 * Copyright (c) 2018-20 Marco Maccaferri and others.
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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class AboutDialog extends Dialog {

    public AboutDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("About " + Application.APP_TITLE);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = layout.marginWidth = 0;
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        content.setLayout(layout);
        content.setLayoutData(new GridData(GridData.FILL_BOTH));
        content.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        content.setBackgroundMode(SWT.INHERIT_FORCE);

        applyDialogFont(content);

        Label label = new Label(content, SWT.NONE);
        label.setLayoutData(new GridData(SWT.TOP, SWT.RIGHT, false, false));

        final Image image = getImageFromResources("about.png");
        label.setImage(image);
        label.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent arg0) {
                image.dispose();
            }
        });

        String title = Application.APP_TITLE + " " + Application.APP_VERSION;
        final String epl = "http://www.eclipse.org/legal/epl-v10.html";
        final String famfamfam = "http://www.famfamfam.com/lab/icons/silk";
        final String fugue = "http://p.yusukekamiyamane.com";
        final String message = title + "\r\n\r\n" + "Copyright \u00a9 2018-20 Marco Maccaferri and others.\r\nAll rights reserved.\r\n\r\n" + //
            "This program and the accompanying materials are made available under the\r\n" + //
            "terms of the Eclipse Public License v1.0 which accompanies this distribution\r\n" + //
            "and is available at " + epl + "\r\n" + //
            "\r\nIcons from " + famfamfam + //
            "\r\nand " + fugue + "\r\n";

        final StyledText text = new StyledText(content, SWT.READ_ONLY);
        text.setLayoutData(new GridData(SWT.TOP, SWT.RIGHT, true, false));
        text.setCaret(null);
        text.setMargins(0, layout.verticalSpacing, convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING), layout.verticalSpacing);
        text.setText(message);

        final List<StyleRange> linkRanges = new ArrayList<StyleRange>();

        StyleRange style = new StyleRange();
        style.start = 0;
        style.length = title.length();
        style.fontStyle = SWT.BOLD;
        text.setStyleRange(style);

        style = new StyleRange();
        style.start = message.indexOf(epl);
        style.length = epl.length();
        style.underline = true;
        style.underlineStyle = SWT.UNDERLINE_LINK;
        text.setStyleRange(style);
        linkRanges.add(style);

        style = new StyleRange();
        style.start = message.indexOf(famfamfam);
        style.length = famfamfam.length();
        style.underline = true;
        style.underlineStyle = SWT.UNDERLINE_LINK;
        text.setStyleRange(style);
        linkRanges.add(style);

        style = new StyleRange();
        style.start = message.indexOf(fugue);
        style.length = fugue.length();
        style.underline = true;
        style.underlineStyle = SWT.UNDERLINE_LINK;
        text.setStyleRange(style);
        linkRanges.add(style);

        text.addListener(SWT.MouseDown, new Listener() {

            @Override
            public void handleEvent(Event event) {
                try {
                    int offset = text.getOffsetAtLocation(new Point(event.x, event.y));
                    for (StyleRange style : linkRanges) {
                        if (offset >= style.start && offset < (style.start + style.length)) {
                            String link = text.getText(style.start, style.start + style.length - 1);
                            Program.launch(link);
                            break;
                        }
                    }
                } catch (IllegalArgumentException e) {
                    // no character under event.x, event.y
                }
            }
        });

        return content;
    }

    Image getImageFromResources(String name) {
        InputStream is = getClass().getResourceAsStream(name);
        ImageLoader loader = new ImageLoader();
        ImageData[] data = loader.load(is);
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Image(Display.getDefault(), data[0]);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.CLOSE_LABEL, true);
    }
}
