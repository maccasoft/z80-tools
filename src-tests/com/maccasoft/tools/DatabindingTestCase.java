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

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.swt.widgets.Display;

import junit.framework.TestCase;
import junit.framework.TestResult;

public abstract class DatabindingTestCase extends TestCase {

    public DatabindingTestCase() {
    }

    public DatabindingTestCase(String name) {
        super(name);
    }

    @Override
    public void run(final TestResult result) {
        Display display = Display.getDefault();
        Realm.runWithDefault(DisplayRealm.getRealm(display), new Runnable() {

            @Override
            public void run() {
                DatabindingTestCase.super.run(result);
            }
        });
    }
}
