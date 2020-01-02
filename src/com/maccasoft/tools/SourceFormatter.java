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

import java.util.HashSet;
import java.util.Set;

import nl.grauw.glass.AssemblyException;
import nl.grauw.glass.Line;
import nl.grauw.glass.Source;
import nl.grauw.glass.directives.If;
import nl.grauw.glass.directives.Proc;
import nl.grauw.glass.directives.Section;
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
    public static final int ADD_DOT = 1;
    public static final int REMOVE_DOT = 2;

    Source source;

    int mnemonicColumn;
    int argumentColumn;
    int commentColumn;

    int labelCase;
    int mnemonicCase;
    int directivePrefix;

    int column;
    StringBuilder sb;

    Set<String> directives;

    public SourceFormatter(Source source) {
        this.source = source;

        this.directives = new HashSet<String>();
        this.directives.add("byte");
        this.directives.add("db");
        this.directives.add("dd");
        this.directives.add("ds");
        this.directives.add("dw");
        this.directives.add("else");
        this.directives.add("end");
        this.directives.add("endif");
        this.directives.add("endm");
        this.directives.add("endp");
        this.directives.add("ends");
        this.directives.add("equ");
        this.directives.add("error");
        this.directives.add("fill");
        this.directives.add("if");
        this.directives.add("incbin");
        this.directives.add("include");
        this.directives.add("irp");
        this.directives.add("macro");
        this.directives.add("org");
        this.directives.add("proc");
        this.directives.add("rept");
        this.directives.add("section");
        this.directives.add("text");
        this.directives.add("word");
        this.directives.add("warning");
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

    public int getDirectivePrefix() {
        return directivePrefix;
    }

    public void setDirectivePrefix(int directivePrefix) {
        this.directivePrefix = directivePrefix;
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

                    String s = line.getMnemonic();
                    if (isDirective(s)) {
                        if (directivePrefix == ADD_DOT) {
                            if (!s.startsWith(".")) {
                                s = "." + s;
                            }
                        }
                        else if (directivePrefix == REMOVE_DOT) {
                            if (s.startsWith(".")) {
                                s = s.substring(1);
                            }
                        }
                    }
                    if (mnemonicCase == TO_UPPER) {
                        sb.append(s.toUpperCase());
                    }
                    else if (mnemonicCase == TO_LOWER) {
                        sb.append(s.toLowerCase());
                    }
                    else {
                        sb.append(s);
                    }
                    column += s.length();

                    if (line.getArguments() != null) {
                        while (column < argumentColumn) {
                            sb.append(' ');
                            column++;
                        }
                        if (sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
                            sb.append(' ');
                        }
                        s = formatExpression(line.getArguments());
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
                    if (line.getInstructionObject() != null) {
                        nl.grauw.glass.instructions.If ins = (nl.grauw.glass.instructions.If) line.getInstruction();
                        format(ins.getThenSource());
                        if (ins.getElseSource() != null) {
                            format(ins.getElseSource());
                        }
                    }
                    else {
                        If ins = (If) line.getDirective();
                        format(ins.getThenSource());
                        if (ins.getElseSource() != null) {
                            format(ins.getElseSource());
                        }
                    }
                }
                else if (line.getDirective() instanceof Section) {
                    if (line.getInstructionObject() != null) {
                        nl.grauw.glass.instructions.Section ins = (nl.grauw.glass.instructions.Section) line.getInstruction();
                        format(ins.getSource());
                    }
                    else {
                        Section ins = (Section) line.getDirective();
                        format(ins.getSource());
                    }
                }
                else if (line.getDirective() instanceof Proc) {
                    if (line.getInstructionObject() != null) {
                        nl.grauw.glass.instructions.Proc ins = (nl.grauw.glass.instructions.Proc) line.getInstruction();
                        format(ins.getSource());
                    }
                    else {
                        Proc ins = (Proc) line.getDirective();
                        format(ins.getSource());
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

    boolean isDirective(String s) {
        if (s.startsWith(".")) {
            return directives.contains(s.substring(1).toLowerCase());
        }
        return directives.contains(s.toLowerCase());
    }

}
