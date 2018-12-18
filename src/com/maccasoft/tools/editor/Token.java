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

public class Token {

    public TokenId id;
    public int length;
    public Token next;

    public Token(int length, TokenId id) {
        this.length = length;
        this.id = id;
    }

    @Override
    public String toString() {
        return "[id=" + id + ",length=" + length + "]";
    }
}
