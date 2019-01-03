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

public class Z80TokenMarker extends TokenMarker {

    static {
        keywords.put("ADC", TokenId.Instruction);
        keywords.put("ADD", TokenId.Instruction);
        keywords.put("AND", TokenId.Instruction);
        keywords.put("BIT", TokenId.Instruction);
        keywords.put("CALL", TokenId.Instruction);
        keywords.put("CCF", TokenId.Instruction);
        keywords.put("CP", TokenId.Instruction);
        keywords.put("CPD", TokenId.Instruction);
        keywords.put("CPDR", TokenId.Instruction);
        keywords.put("CPI", TokenId.Instruction);
        keywords.put("CPIR", TokenId.Instruction);
        keywords.put("CPL", TokenId.Instruction);
        keywords.put("DAA", TokenId.Instruction);
        keywords.put("DEC", TokenId.Instruction);
        keywords.put("DI", TokenId.Instruction);
        keywords.put("DJNZ", TokenId.Instruction);
        keywords.put("EI", TokenId.Instruction);
        keywords.put("EX", TokenId.Instruction);
        keywords.put("EXX", TokenId.Instruction);
        keywords.put("HALT", TokenId.Instruction);
        keywords.put("IM", TokenId.Instruction);
        keywords.put("INC", TokenId.Instruction);
        keywords.put("IN", TokenId.Instruction);
        keywords.put("INI", TokenId.Instruction);
        keywords.put("IND", TokenId.Instruction);
        keywords.put("INIR", TokenId.Instruction);
        keywords.put("INDR", TokenId.Instruction);
        keywords.put("JR", TokenId.Instruction);
        keywords.put("JP", TokenId.Instruction);
        keywords.put("LD", TokenId.Instruction);
        keywords.put("LDI", TokenId.Instruction);
        keywords.put("LDIR", TokenId.Instruction);
        keywords.put("LDD", TokenId.Instruction);
        keywords.put("LDDR", TokenId.Instruction);
        keywords.put("NEG", TokenId.Instruction);
        keywords.put("NOP", TokenId.Instruction);
        keywords.put("OR", TokenId.Instruction);
        keywords.put("OUT", TokenId.Instruction);
        keywords.put("OUTI", TokenId.Instruction);
        keywords.put("OUTD", TokenId.Instruction);
        keywords.put("OTIR", TokenId.Instruction);
        keywords.put("OTDR", TokenId.Instruction);
        keywords.put("POP", TokenId.Instruction);
        keywords.put("PUSH", TokenId.Instruction);
        keywords.put("RES", TokenId.Instruction);
        keywords.put("RET", TokenId.Instruction);
        keywords.put("RETI", TokenId.Instruction);
        keywords.put("RETN", TokenId.Instruction);
        keywords.put("RL", TokenId.Instruction);
        keywords.put("RLA", TokenId.Instruction);
        keywords.put("RLC", TokenId.Instruction);
        keywords.put("RLCA", TokenId.Instruction);
        keywords.put("RLD", TokenId.Instruction);
        keywords.put("RR", TokenId.Instruction);
        keywords.put("RRA", TokenId.Instruction);
        keywords.put("RRC", TokenId.Instruction);
        keywords.put("RRCA", TokenId.Instruction);
        keywords.put("RRD", TokenId.Instruction);
        keywords.put("RST", TokenId.Instruction);
        keywords.put("SBC", TokenId.Instruction);
        keywords.put("SCF", TokenId.Instruction);
        keywords.put("SET", TokenId.Instruction);
        keywords.put("SLA", TokenId.Instruction);
        keywords.put("SLL", TokenId.Instruction);
        keywords.put("SRA", TokenId.Instruction);
        keywords.put("SRL", TokenId.Instruction);
        keywords.put("SUB", TokenId.Instruction);
        keywords.put("XOR", TokenId.Instruction);

        keywords.put("DB", TokenId.Directive2);
        keywords.put("DD", TokenId.Directive2);
        keywords.put("DS", TokenId.Directive2);
        keywords.put("DW", TokenId.Directive2);
        keywords.put("BYTE", TokenId.Directive2);
        keywords.put("WORD", TokenId.Directive2);
        keywords.put("TEXT", TokenId.Directive2);

        keywords.put("INCLUDE", TokenId.Directive1);
        keywords.put("INCBIN", TokenId.Directive1);
        keywords.put("EQU", TokenId.Directive1);
        keywords.put("ORG", TokenId.Directive2);
        keywords.put("MACRO", TokenId.Directive1);
        keywords.put("ENDM", TokenId.Directive1);
        keywords.put("PROC", TokenId.Directive1);
        keywords.put("ENDP", TokenId.Directive1);
        keywords.put("SECTION", TokenId.Directive1);
        keywords.put("ENDS", TokenId.Directive1);
        keywords.put("IF", TokenId.Directive1);
        keywords.put("ELSE", TokenId.Directive1);
        keywords.put("ENDIF", TokenId.Directive1);
        keywords.put("END", TokenId.Directive1);
        keywords.put("ERROR", TokenId.Directive1);
        keywords.put("WARNING", TokenId.Directive1);
        keywords.put("FILL", TokenId.Directive1);
        keywords.put("REPT", TokenId.Directive1);
        keywords.put("IRP", TokenId.Directive1);
        keywords.put("ONCE", TokenId.Directive1);

        keywords.put(".DB", TokenId.Directive2);
        keywords.put(".DD", TokenId.Directive2);
        keywords.put(".DS", TokenId.Directive2);
        keywords.put(".DW", TokenId.Directive2);
        keywords.put(".BYTE", TokenId.Directive2);
        keywords.put(".WORD", TokenId.Directive2);
        keywords.put(".TEXT", TokenId.Directive2);

        keywords.put(".INCLUDE", TokenId.Directive1);
        keywords.put(".INCBIN", TokenId.Directive1);
        keywords.put(".EQU", TokenId.Directive1);
        keywords.put(".ORG", TokenId.Directive2);
        keywords.put(".MACRO", TokenId.Directive1);
        keywords.put(".ENDM", TokenId.Directive1);
        keywords.put(".PROC", TokenId.Directive1);
        keywords.put(".ENDP", TokenId.Directive1);
        keywords.put(".SECTION", TokenId.Directive1);
        keywords.put(".ENDS", TokenId.Directive1);
        keywords.put(".IF", TokenId.Directive1);
        keywords.put(".ELSE", TokenId.Directive1);
        keywords.put(".ENDIF", TokenId.Directive1);
        keywords.put(".END", TokenId.Directive1);
        keywords.put(".ERROR", TokenId.Directive1);
        keywords.put(".WARNING", TokenId.Directive1);
        keywords.put(".FILL", TokenId.Directive1);
        keywords.put(".REPT", TokenId.Directive1);
        keywords.put(".IRP", TokenId.Directive1);

        keywords.put("A", TokenId.Register);
        keywords.put("B", TokenId.Register);
        keywords.put("C", TokenId.Register);
        keywords.put("D", TokenId.Register);
        keywords.put("E", TokenId.Register);
        keywords.put("F", TokenId.Register);
        keywords.put("H", TokenId.Register);
        keywords.put("L", TokenId.Register);
        keywords.put("AF", TokenId.Register);
        keywords.put("BC", TokenId.Register);
        keywords.put("DE", TokenId.Register);
        keywords.put("HL", TokenId.Register);
        keywords.put("SP", TokenId.Register);
        keywords.put("IX", TokenId.Register);
        keywords.put("IY", TokenId.Register);
        keywords.put("I", TokenId.Register);
        keywords.put("R", TokenId.Register);

        keywords.put("Z", TokenId.Flag);
        keywords.put("NZ", TokenId.Flag);
        keywords.put("C", TokenId.Flag);
        keywords.put("NC", TokenId.Flag);
        keywords.put("PO", TokenId.Flag);
        keywords.put("PE", TokenId.Flag);
        keywords.put("P", TokenId.Flag);
        keywords.put("M", TokenId.Flag);
    }

    public Z80TokenMarker() {
    }

    @Override
    public Token markTokens(String text, int offset) {
        boolean backslash = false;

        TokenId token = TokenId.Null;
        firstToken = lastToken = null;
        lastOffset = lastKeyword = 0;

        int start = 0;

        for (int i = start; i < text.length(); i++) {
            int i1 = (i + 1);
            char c = text.charAt(i);

            if (c == '\\') {
                backslash = !backslash;
                continue;
            }

            switch (token) {
                case Null:
                    switch (c) {
                        case '\'':
                        case '"':
                            doKeyword(text, i, c);
                            if (backslash) {
                                backslash = false;
                            }
                            else {
                                addToken(i - lastOffset, token);
                                token = c == '"' ? TokenId.StringLiteral1 : TokenId.StringLiteral2;
                                lastOffset = lastKeyword = i;
                            }
                            break;
                        case ';': {
                            backslash = false;
                            doKeyword(text, i, c);
                            addToken(i - lastOffset, token);
                            int mlength = text.indexOf('\n', i);
                            if (mlength == -1) {
                                mlength = text.length();
                            }
                            addToken(mlength - i, TokenId.Comment);
                            lastOffset = lastKeyword = i = mlength;
                            break;
                        }
                        case '$':
                            backslash = false;
                            doKeyword(text, i, c);
                            addToken(i - lastOffset, token);

                            int mlength = i + 1;
                            while (mlength < text.length() && isHexDigit(text.charAt(mlength))) {
                                mlength++;
                            }
                            if ((mlength - i) > 1) {
                                addToken(mlength - i, TokenId.NumberLiteral);
                                lastOffset = lastKeyword = i = mlength;
                            }
                            break;
                        default:
                            backslash = false;
                            if (!Character.isLetterOrDigit(c) && c != '_' && c != '.') {
                                doKeyword(text, i, c);
                            }
                            break;
                    }
                    break;
                case StringLiteral1:
                    if (backslash) {
                        backslash = false;
                    }
                    else if (c == '"') {
                        addToken(i1 - lastOffset, token);
                        token = TokenId.Null;
                        lastOffset = lastKeyword = i1;
                    }
                    break;
                case StringLiteral2:
                    if (backslash) {
                        backslash = false;
                    }
                    else if (c == '\'') {
                        addToken(i1 - lastOffset, token);
                        token = TokenId.Null;
                        lastOffset = lastKeyword = i1;
                    }
                    break;
                default:
                    // Do nothing
                    break;
            }
        }

        if (token == TokenId.Null) {
            doKeyword(text, text.length(), '\0');
        }

        switch (token) {
            case StringLiteral1:
            case StringLiteral2:
                addToken(text.length() - lastOffset, null);
                token = null;
                break;
            default:
                addToken(text.length() - lastOffset, token);
                break;
        }

        return firstToken;
    }

}
