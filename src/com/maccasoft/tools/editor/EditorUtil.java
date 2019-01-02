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

public class EditorUtil {

    public static boolean isWhitespace(char c) {
        return c == ' ' || c == '\t';
    }

    public static boolean isSeparator(char c) {
        if (Character.isWhitespace(c)) {
            return false;
        }
        return !Character.isLetterOrDigit(c);
    }

    public static String replaceTabs(String text, int tabs) {
        final StringBuilder spaces = new StringBuilder(tabs);
        for (int i = 0; i < tabs; i++) {
            spaces.append(' ');
        }

        int i = 0;
        while ((i = text.indexOf('\t', i)) != -1) {
            int s = i;
            while (s > 0) {
                s--;
                if (text.charAt(s) == '\r' || text.charAt(s) == '\n') {
                    s++;
                    break;
                }
            }
            int n = ((i - s) % tabs);
            text = text.substring(0, i) + spaces.substring(0, tabs - n) + text.substring(i + 1);
        }

        return text;
    }

    public static String trimLines(String text) {
        return text.replaceAll("[ \\t]+(\r\n|\n|\r)", "$1");
    }
}
