/*
 * Copyright (c) 2020 Marco Maccaferri and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.maccasoft.tools.internal;

public class Utility {

    public static byte[] getSwappedBytes(String s) {
        byte[] result = s.getBytes();

        for (int i = 0; i < result.length; i += 2) {
            byte a = result[i];
            result[i] = result[i + 1];
            result[i + 1] = a;
        }

        return result;
    }
}
