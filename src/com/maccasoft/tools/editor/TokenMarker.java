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

import java.util.HashMap;
import java.util.Map;

public abstract class TokenMarker {

    static Map<String, TokenId> keywords = new HashMap<String, TokenId>();

    final Map<String, TokenId> additionalKeywords = new HashMap<String, TokenId>();

    Token firstToken;
    Token lastToken;

    int lastKeyword;
    int lastOffset;

    public TokenMarker() {
    }

    public boolean refreshMultilineComments(String text) {
        return false;
    }

    public abstract Token markTokens(String text, int offset);

    public void clearKeywords() {
        additionalKeywords.clear();
    }

    public void addKeyword(String text, TokenId id) {
        if (text == null || "".equals(text)) {
            return;
        }
        additionalKeywords.put(text, id);
    }

    public void removeKeyword(String text) {
        additionalKeywords.remove(text);
    }

    protected void addToken(int length, TokenId id) {
        if (length == 0 && id != TokenId.End) {
            return;
        }

        if (firstToken == null) {
            firstToken = new Token(length, id);
            lastToken = firstToken;
        }
        else if (lastToken == null) {
            lastToken = firstToken;
            firstToken.length = length;
            firstToken.id = id;
        }
        else if (lastToken.next == null) {
            lastToken.next = new Token(length, id);
            lastToken = lastToken.next;
        }
        else {
            lastToken = lastToken.next;
            lastToken.length = length;
            lastToken.id = id;
        }
    }

    protected TokenId doKeyword(String line, int i, char c) {
        int i1 = i + 1;
        int len = i - lastKeyword;
        String s = line.substring(lastKeyword, i);

        TokenId id = getNumberTokenId(s);
        if (id == null) {
            id = keywords.get(s.toUpperCase());
        }
        if (id == null) {
            id = additionalKeywords.get(s);
        }
        if (id != null) {
            if (lastKeyword != lastOffset) {
                addToken(lastKeyword - lastOffset, TokenId.Null);
            }
            addToken(len, id);
            lastOffset = i;
        }
        lastKeyword = i1;
        return id;
    }

    TokenId getNumberTokenId(String s) {
        if (s.length() > 0 && (Character.isDigit(s.charAt(0)) || s.charAt(0) == '$')) {
            int i = 1;
            char c0 = s.charAt(0);
            while (i < s.length()) {
                if (!isHexDigit(s.charAt(i))) {
                    break;
                }
                i++;
            }
            if (i == s.length()) {
                return TokenId.NumberLiteral;
            }
            if (c0 == '$' && !Character.isWhitespace(s.charAt(i))) {
                return null;
            }
            if (s.charAt(i) == 'H' || s.charAt(i) == 'h' || s.charAt(i) == 'B' || s.charAt(i) == 'b') {
                return TokenId.NumberLiteral;
            }
        }
        return null;
    }

    boolean isHexDigit(char c) {
        if (c >= '0' && c <= '9') {
            return true;
        }
        if ((c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f')) {
            return true;
        }
        return false;
    }

}
