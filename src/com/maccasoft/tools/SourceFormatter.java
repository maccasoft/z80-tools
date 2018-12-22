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

import nl.grauw.glass.AssemblyException;
import nl.grauw.glass.Line;
import nl.grauw.glass.Source;
import nl.grauw.glass.directives.If;
import nl.grauw.glass.expressions.Annotation;
import nl.grauw.glass.expressions.BinaryOperator;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Flag;
import nl.grauw.glass.expressions.FlagOrRegister;
import nl.grauw.glass.expressions.Group;
import nl.grauw.glass.expressions.Identifier;
import nl.grauw.glass.expressions.IfElse;
import nl.grauw.glass.expressions.Index;
import nl.grauw.glass.expressions.Member;
import nl.grauw.glass.expressions.Register;
import nl.grauw.glass.expressions.Sequence;
import nl.grauw.glass.expressions.UnaryOperator;

public class SourceFormatter {

    public static final int NO_CHANGE = 0;
    public static final int TO_UPPER = 1;
    public static final int TO_LOWER = 2;

    Source source;

    int mnemonicColumn;
    int argumentColumn;
    int commentColumn;

    int labelCase;
    int mnemonicCase;

    int column;
    StringBuilder sb;

    public SourceFormatter(Source source) {
        this.source = source;
    }

    public int getMnemonicColumn() {
        return mnemonicColumn;
    }

    public void setMnemonicColumn(int mnemonicColumn) {
        this.mnemonicColumn = mnemonicColumn;
    }

    public int getArgumentColumn() {
        return argumentColumn;
    }

    public void setArgumentColumn(int argumentColumn) {
        this.argumentColumn = argumentColumn;
    }

    public int getCommentColumn() {
        return commentColumn;
    }

    public void setCommentColumn(int commentColumn) {
        this.commentColumn = commentColumn;
    }

    public int getLabelCase() {
        return labelCase;
    }

    public void setLabelCase(int labelCase) {
        this.labelCase = labelCase;
    }

    public int getMnemonicCase() {
        return mnemonicCase;
    }

    public void setMnemonicCase(int mnemonicCase) {
        this.mnemonicCase = mnemonicCase;
    }

    public String format() {
        sb = new StringBuilder();
        format(source);
        return sb.toString();
    }

    void format(Source source) {

        for (Line line : source.getLines()) {
            try {
                column = 0;

                if (line.getLabel() != null) {
                    if (labelCase == TO_UPPER) {
                        sb.append(line.getLabel().toUpperCase());
                    }
                    else if (labelCase == TO_LOWER) {
                        sb.append(line.getLabel().toLowerCase());
                    }
                    else {
                        sb.append(line.getLabel());
                    }
                    column = line.getLabel().length();
                }

                if (line.getMnemonic() != null) {
                    if (column == 0) {
                        sb.append(' ');
                        column++;
                    }
                    while (column < mnemonicColumn) {
                        sb.append(' ');
                        column++;
                    }
                    if (sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
                        sb.append(' ');
                    }

                    if (mnemonicCase == TO_UPPER) {
                        sb.append(line.getMnemonic().toUpperCase());
                    }
                    else if (mnemonicCase == TO_LOWER) {
                        sb.append(line.getMnemonic().toLowerCase());
                    }
                    else {
                        sb.append(line.getMnemonic());
                    }

                    column += line.getMnemonic().length();

                    if (line.getArguments() != null) {
                        while (column < argumentColumn) {
                            sb.append(' ');
                            column++;
                        }
                        if (sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
                            sb.append(' ');
                        }
                        String s = formatExpression(line.getArguments());
                        sb.append(s);
                        column += s.length();
                    }
                }

                if (column != 0) {
                    if (line.getComment() != null) {
                        while (column < commentColumn) {
                            sb.append(' ');
                            column++;
                        }
                        if (sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
                            sb.append(' ');
                        }
                        sb.append(';');
                        if (!line.getComment().startsWith(" ")) {
                            sb.append(' ');
                        }
                        sb.append(line.getComment());
                    }
                }
                else {
                    sb.append(line.getSourceText());
                }

                sb.append("\r\n");

                if (line.getDirective() instanceof If) {
                    If ins = (If) line.getDirective();
                    format(ins.getThenSource());
                    if (ins.getElseSource() != null) {
                        format(ins.getElseSource());
                    }
                }
            } catch (AssemblyException e) {
                e.addContext(line);
                throw e;
            }
        }
    }

    String formatExpression(Expression e) {
        if (e instanceof Annotation) {
            return "" + formatExpression(((Annotation) e).getAnnotation()) + " " + formatExpression(((Annotation) e).getAnnotee());
        }
        if (e instanceof Group) {
            return "(" + formatExpression(((Group) e).getTerm()) + ")";
        }
        if (e instanceof Sequence) {
            Sequence op = (Sequence) e;
            return formatExpression(op.getTerm1()) + ", " + formatExpression(op.getTerm2());
        }
        if (e instanceof UnaryOperator) {
            UnaryOperator op = (UnaryOperator) e;
            return op.getLexeme() + formatExpression(op.getTerm());
        }
        if (e instanceof BinaryOperator) {
            BinaryOperator op = (BinaryOperator) e;
            return formatExpression(op.getTerm1()) + " " + op.getLexeme() + " " + formatExpression(op.getTerm2());
        }
        if (e instanceof IfElse) {
            IfElse op = (IfElse) e;
            return formatExpression(op.getCondition()) + " ? " + formatExpression(op.getTrueTerm()) + " : " + formatExpression(op.getFalseTerm());
        }
        if (e instanceof Index) {
            Index op = (Index) e;
            return formatExpression(op.getSequence()) + "[" + formatExpression(op.getIndex()) + "]";
        }
        if (e instanceof Member) {
            Member op = (Member) e;
            return formatExpression(op.getObject()) + "." + formatExpression(op.getSubject()) + "]";
        }
        if (e instanceof Identifier) {
            if (((Identifier) e).isRegister()) {
                if (mnemonicCase == TO_UPPER) {
                    return e.toString().toUpperCase();
                }
                else if (mnemonicCase == TO_LOWER) {
                    return e.toString().toLowerCase();
                }
                else {
                    return e.toString();
                }
            }
            else {
                if (labelCase == TO_UPPER) {
                    return e.toString().toUpperCase();
                }
                else if (labelCase == TO_LOWER) {
                    return e.toString().toLowerCase();
                }
                else {
                    return e.toString();
                }
            }
        }
        if ((e instanceof Register) || (e instanceof Flag) || (e instanceof FlagOrRegister)) {
            if (mnemonicCase == TO_UPPER) {
                return e.toString().toUpperCase();
            }
            else if (mnemonicCase == TO_LOWER) {
                return e.toString().toLowerCase();
            }
            else {
                return e.toString();
            }
        }
        return e.toString();
    }

}
