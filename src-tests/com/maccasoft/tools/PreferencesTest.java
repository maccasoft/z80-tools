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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Test;

public class PreferencesTest {

    @Test
    public void testRootsNotification() {
        final AtomicReference<PropertyChangeEvent> notify = new AtomicReference<PropertyChangeEvent>();
        final PropertyChangeListener preferencesChangeListner = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                notify.set(evt);
            }

        };

        Preferences preferences = new Preferences();
        preferences.addPropertyChangeListener(preferencesChangeListner);

        notify.set(null);
        preferences.setRoots(null);
        Assert.assertNull(notify.get());

        notify.set(null);
        preferences.setRoots(new String[0]);
        Assert.assertNull(notify.get());

        notify.set(null);
        preferences.setRoots(new String[] {
            "root"
        });
        Assert.assertEquals(Preferences.PROP_ROOTS, notify.get().getPropertyName());
        Assert.assertNull(notify.get().getOldValue());
        Assert.assertArrayEquals(new String[] {
            "root"
        }, (String[]) notify.get().getNewValue());

        notify.set(null);
        preferences.setRoots(new String[] {
            "root"
        });
        Assert.assertNull(notify.get());

        notify.set(null);
        preferences.setRoots(new String[] {
            "root1, root2"
        });
        Assert.assertEquals(Preferences.PROP_ROOTS, notify.get().getPropertyName());
        Assert.assertArrayEquals(new String[] {
            "root"
        }, (String[]) notify.get().getOldValue());
        Assert.assertArrayEquals(new String[] {
            "root1, root2"
        }, (String[]) notify.get().getNewValue());
    }

}
