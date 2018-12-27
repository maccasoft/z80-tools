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
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import z80core.Z80;

public class Registers {

    Display display;
    Composite container;

    Text regPC;
    Text regSP;
    Text regAF;
    Text regBC;
    Text regDE;
    Text regHL;
    Text regAF1;
    Text regBC1;
    Text regDE1;
    Text regHL1;
    Text regR;
    Text regI;

    Label flagS;
    Label flagZ;
    Label flagUnk1;
    Label flagH;
    Label flagUnk2;
    Label flagP;
    Label flagN;
    Label flagC;

    Label tStates;

    Font font;

    public Registers(Composite parent) {
        container = new Composite(parent, SWT.BORDER);
        GridLayout layout = new GridLayout(4, false);
        container.setLayout(layout);

        if ("win32".equals(SWT.getPlatform())) {
            font = new Font(Display.getDefault(), "Courier New", 9, SWT.NONE);
        }
        else {
            font = new Font(Display.getDefault(), "mono", 9, SWT.NONE);
        }

        createRegistersGroup(container);

        new Label(container, SWT.NONE);
        Control control = createFlagsGroup(container);
        control.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));

        Label label = new Label(container, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));

        label = new Label(container, SWT.NONE);
        label.setText("T");
        tStates = new Label(container, SWT.NONE);
        tStates.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

        applyFont(container, font);
    }

    void createRegistersGroup(Composite parent) {
        regPC = createTextEntry(parent, "PC");
        regPC.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {

            }
        });
        Label label = new Label(parent, SWT.NONE);
        label.setText("");
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        regSP = createTextEntry(parent, "SP");
        regSP.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {

            }
        });
        label = new Label(parent, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        regAF = createTextEntry(parent, "AF");
        regAF.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {

            }
        });
        regAF1 = createTextEntry(parent, "AF'");
        regAF1.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {

            }
        });

        regBC = createTextEntry(parent, "BC");
        regBC.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {

            }
        });
        regBC1 = createTextEntry(parent, "BC'");
        regBC1.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {

            }
        });

        regDE = createTextEntry(parent, "DE");
        regDE.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {

            }
        });
        regDE1 = createTextEntry(parent, "DE'");
        regDE1.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {

            }
        });

        regHL = createTextEntry(parent, "HL");
        regHL.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {

            }
        });
        regHL1 = createTextEntry(parent, "HL'");
        regHL1.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {

            }
        });

        regR = createTextEntry(parent, "R");
        regR.setEditable(false);
        regI = createTextEntry(parent, "I");
        regI.setEditable(false);
    }

    Text createTextEntry(Composite parent, String text) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(text);
        Text entry = new Text(parent, SWT.BORDER);
        entry.setLayoutData(new GridData(40, SWT.DEFAULT));
        return entry;
    }

    Control createFlagsGroup(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(8, true);
        container.setLayout(layout);

        flagS = new Label(container, SWT.NONE);
        flagS.setText("-");
        flagZ = new Label(container, SWT.NONE);
        flagZ.setText("-");
        flagUnk1 = new Label(container, SWT.NONE);
        flagUnk1.setText("-");
        flagH = new Label(container, SWT.NONE);
        flagH.setText("-");
        flagUnk2 = new Label(container, SWT.NONE);
        flagUnk2.setText("-");
        flagP = new Label(container, SWT.NONE);
        flagP.setText("-");
        flagN = new Label(container, SWT.NONE);
        flagN.setText("-");
        flagC = new Label(container, SWT.NONE);
        flagC.setText("-");

        return container;
    }

    void applyFont(Composite container, Font font) {
        Control[] childs = container.getChildren();
        for (int i = 0; i < childs.length; i++) {
            if ((childs[i] instanceof Label) || (childs[i] instanceof Text)) {
                childs[i].setFont(font);
            }
            if (childs[i] instanceof Composite) {
                applyFont((Composite) childs[i], font);
            }
        }
    }

    public Control getControl() {
        return container;
    }

    void updateRegisters(Z80 proc) {
        regPC.setText(String.format("%04X", proc.getRegPC() & 0xFFFF));
        regSP.setText(String.format("%04X", proc.getRegSP() & 0xFFFF));

        regAF.setText(String.format("%04X", proc.getRegAF() & 0xFFFF));
        regBC.setText(String.format("%04X", proc.getRegBC() & 0xFFFF));
        regDE.setText(String.format("%04X", proc.getRegDE() & 0xFFFF));
        regHL.setText(String.format("%04X", proc.getRegHL() & 0xFFFF));

        regAF1.setText(String.format("%04X", proc.getRegAFx() & 0xFFFF));
        regBC1.setText(String.format("%04X", proc.getRegBCx() & 0xFFFF));
        regDE1.setText(String.format("%04X", proc.getRegDEx() & 0xFFFF));
        regHL1.setText(String.format("%04X", proc.getRegHLx() & 0xFFFF));

        regR.setText(String.format("%02X", proc.getRegR() & 0xFF));
        regI.setText(String.format("%02X", proc.getRegI() & 0xFF));

        flagS.setText(proc.isSignFlag() ? "S" : "-");
        flagZ.setText(proc.isZeroFlag() ? "Z" : "-");
        flagUnk1.setText(proc.isBit5Flag() ? "1" : "0");
        flagH.setText(proc.isHalfCarryFlag() ? "H" : "-");
        flagUnk2.setText(proc.isBit3Flag() ? "1" : "0");
        flagP.setText(proc.isParOverFlag() ? "P" : "-");
        flagN.setText(proc.isAddSubFlag() ? "N" : "-");
        flagC.setText(proc.isCarryFlag() ? "C" : "-");

        tStates.setText(String.valueOf(proc.getMemIoOps().getTstates()));
    }

}
