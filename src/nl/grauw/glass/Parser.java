package nl.grauw.glass;

import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;

import nl.grauw.glass.expressions.CharacterLiteral;
import nl.grauw.glass.expressions.ExpressionBuilder;
import nl.grauw.glass.expressions.Identifier;
import nl.grauw.glass.expressions.IntegerLiteral;
import nl.grauw.glass.expressions.StringLiteral;

public class Parser {

    private Scope scope;
    private LineBuilder lineBuilder = new LineBuilder();

    private State state;
    private StringBuilder accumulator = new StringBuilder();
    private ExpressionBuilder expressionBuilder = new ExpressionBuilder();

    public Line parse(LineNumberReader reader, Scope scope, File sourceFile) {
        this.scope = scope;
        state = labelStartState;

        final int firstLineNumber = reader.getLineNumber();
        int lineNumber = firstLineNumber;
        int column = 0;
        ArrayList<String> sourceLines = new ArrayList<String>();
        try {
            while (state != endState) {
                String sourceLine = reader.readLine();
                if (sourceLine == null) {
                    state = state.parse('\0');
                    if (state != endState) {
                        throw new AssemblyException("Unexpected end of file.");
                    }
                    if (sourceLines.size() > 0) {
                        break; // return null (parsing end) the next time
                    }
                    return null;
                }
                sourceLines.add(sourceLine);
                lineNumber = reader.getLineNumber();
                column = 0;

                for (int i = 0, length = sourceLine.length(); i < length; i++) {
                    column = i;
                    state = state.parse(sourceLine.charAt(i));
                }
                column = sourceLine.length();
                state = state.parse('\n');
            }

            if (accumulator.length() > 0) {
                throw new AssemblyException("Accumulator not consumed. Value: " + accumulator.toString());
            }
        } catch (AssemblyException e) {
            e.addContext(sourceFile, lineNumber, column, String.join("\n", sourceLines));
            throw e;
        } catch (IOException e) {
            throw new AssemblyException(e);
        }

        lineBuilder.setSourceText(String.join("\n", sourceLines));

        return lineBuilder.getLine(scope, sourceFile, firstLineNumber);
    }

    private abstract class State {
        public abstract State parse(char character);

        public boolean isWhitespace(char character) {
            return character == ' ' || character == '\t';
        }

        public boolean isIdentifier(char character) {
            return isIdentifierStart(character) || character >= '0' && character <= '9' ||
                character == '\'' || character == '$';
        }

        public boolean isIdentifierStart(char character) {
            return character >= 'a' && character <= 'z' || character >= 'A' && character <= 'Z' ||
                character == '_' || character == '.' || character == '?' || character == '@';
        }

    }

    private LabelStartState labelStartState = new LabelStartState();
    private class LabelStartState extends State {
        @Override
        public State parse(char character) {
            if (isIdentifierStart(character)) {
                accumulator.append(character);
                return labelReadState;
            }
            else if (isWhitespace(character)) {
                return statementStartState;
            }
            else if (character == ';') {
                return commentReadState;
            }
            else if (character == '\n' || character == '\0') {
                return endState;
            }
            throw new SyntaxError();
        }
    }

    private LabelReadState labelReadState = new LabelReadState();
    private class LabelReadState extends State {
        @Override
        public State parse(char character) {
            if (isIdentifier(character)) {
                accumulator.append(character);
                return labelReadState;
            }
            else {
                lineBuilder.setLabel(accumulator.toString());
                accumulator.setLength(0);
                if (character == ':' || isWhitespace(character)) {
                    return statementStartState;
                }
                else if (character == ';') {
                    return commentReadState;
                }
                else if (character == '\n' || character == '\0') {
                    return endState;
                }
            }
            throw new SyntaxError();
        }
    }

    private StatementStartState statementStartState = new StatementStartState();
    private class StatementStartState extends State {
        @Override
        public State parse(char character) {
            if (isIdentifierStart(character)) {
                accumulator.append(character);
                return statementReadState;
            }
            else if (isWhitespace(character)) {
                return statementStartState;
            }
            else if (character == ';') {
                return commentReadState;
            }
            else if (character == '\n' || character == '\0') {
                return endState;
            }
            throw new SyntaxError();
        }
    }

    private StatementReadState statementReadState = new StatementReadState();
    private class StatementReadState extends State {
        @Override
        public State parse(char character) {
            if (isIdentifier(character)) {
                accumulator.append(character);
                return statementReadState;
            }
            if (character == ':') {
                lineBuilder.setLabel(accumulator.toString());
                accumulator.setLength(0);
                return statementStartState;
            }
            else {
                lineBuilder.setMnemonic(accumulator.toString());
                accumulator.setLength(0);
                if (isWhitespace(character)) {
                    return argumentStartState;
                }
                else if (character == ';') {
                    return commentReadState;
                }
                else if (character == '\n' || character == '\0') {
                    return endState;
                }
            }
            throw new SyntaxError();
        }
    }

    private ArgumentStartState argumentStartState = new ArgumentStartState();
    private class ArgumentStartState extends State {
        @Override
        public State parse(char character) {
            if (character == ';') {
                return commentReadState;
            }
            else if (character == '\n' || character == '\0') {
                return endState;
            }
            else if (isWhitespace(character)) {
                return argumentStartState;
            }
            else {
                return argumentValueState.parse(character);
            }
        }
    }

    private ArgumentValueState argumentValueState = new ArgumentValueState();
    private class ArgumentValueState extends State {
        @Override
        public State parse(char character) {
            if (isIdentifierStart(character)) {
                accumulator.append(character);
                return argumentIdentifierState;
            }
            else if (character == '0') {
                accumulator.append(character);
                return argumentZeroState;
            }
            else if (character >= '1' && character <= '9') {
                accumulator.append(character);
                return argumentNumberState;
            }
            else if (character == '#') {
                return argumentHexadecimalState;
            }
            else if (character == '$') {
                return argumentDollarState;
            }
            else if (character == '%') {
                return argumentBinaryState;
            }
            else if (character == '"') {
                return argumentStringState;
            }
            else if (character == '\'') {
                return argumentCharacterState;
            }
            else if (character == '+') {
                expressionBuilder.addOperatorToken(expressionBuilder.POSITIVE);
                return argumentValueState;
            }
            else if (character == '-') {
                expressionBuilder.addOperatorToken(expressionBuilder.NEGATIVE);
                return argumentValueState;
            }
            else if (character == '~') {
                expressionBuilder.addOperatorToken(expressionBuilder.COMPLEMENT);
                return argumentValueState;
            }
            else if (character == '!') {
                expressionBuilder.addOperatorToken(expressionBuilder.NOT);
                return argumentValueState;
            }
            else if (character == '(') {
                expressionBuilder.addOperatorToken(expressionBuilder.GROUP_OPEN);
                return argumentValueState;
            }
            else if (isWhitespace(character)) {
                return argumentValueState;
            }
            else if (character == ';') {
                return commentReadThenArgumentState;
            }
            else if (character == '\n') {
                return argumentValueState;
            }
            throw new SyntaxError();
        }
    }

    private ArgumentIdentifierState argumentIdentifierState = new ArgumentIdentifierState();
    private class ArgumentIdentifierState extends State {
        @Override
        public State parse(char character) {
            if (isIdentifier(character)) {
                accumulator.append(character);
                return argumentIdentifierState;
            }
            else {
                expressionBuilder.addValueToken(new Identifier(accumulator.toString(), scope));
                accumulator.setLength(0);
                return argumentOperatorState.parse(character);
            }
        }
    }

    private ArgumentStringState argumentStringState = new ArgumentStringState();
    private class ArgumentStringState extends State {
        @Override
        public State parse(char character) {
            if (character == '"') {
                expressionBuilder.addValueToken(new StringLiteral(accumulator.toString()));
                accumulator.setLength(0);
                return argumentOperatorState;
            }
            else if (character == '\\') {
                return argumentStringEscapeState;
            }
            else if (character == '\n' || character == '\0') {
                throw new SyntaxError();
            }
            else {
                accumulator.append(character);
                return argumentStringState;
            }
        }
    }

    private ArgumentStringEscapeState argumentStringEscapeState = new ArgumentStringEscapeState();
    private class ArgumentStringEscapeState extends State {
        @Override
        public State parse(char character) {
            if (character == '0') {
                accumulator.append('\0');
                return argumentStringState;
            }
            else if (character == 'a') {
                accumulator.append('\7');
                return argumentStringState;
            }
            else if (character == 't') {
                accumulator.append('\t');
                return argumentStringState;
            }
            else if (character == 'n') {
                accumulator.append('\n');
                return argumentStringState;
            }
            else if (character == 'f') {
                accumulator.append('\f');
                return argumentStringState;
            }
            else if (character == 'r') {
                accumulator.append('\r');
                return argumentStringState;
            }
            else if (character == 'e') {
                accumulator.append('\33');
                return argumentStringState;
            }
            else if (character == '"') {
                accumulator.append('"');
                return argumentStringState;
            }
            else if (character == '\'') {
                accumulator.append('\'');
                return argumentStringState;
            }
            else if (character == '\\') {
                accumulator.append('\\');
                return argumentStringState;
            }
            else if (character == '\n' || character == '\0') {
                throw new SyntaxError();
            }
            else {
                throw new SyntaxError();
            }
        }
    }

    private ArgumentCharacterState argumentCharacterState = new ArgumentCharacterState();
    private class ArgumentCharacterState extends State {
        @Override
        public State parse(char character) {
            if (character == '\\') {
                return argumentCharacterEscapeState;
            }
            else if (character == '\'' || character == '\n' || character == '\0') {
                throw new SyntaxError();
            }
            else {
                accumulator.append(character);
                return argumentCharacterEndState;
            }
        }
    }

    private ArgumentCharacterEscapeState argumentCharacterEscapeState = new ArgumentCharacterEscapeState();
    private class ArgumentCharacterEscapeState extends State {
        @Override
        public State parse(char character) {
            State state = argumentStringEscapeState.parse(character);
            if (state == argumentStringState) {
                return argumentCharacterEndState;
            }
            throw new AssemblyException("Unexpected state.");
        }
    }

    private ArgumentCharacterEndState argumentCharacterEndState = new ArgumentCharacterEndState();
    private class ArgumentCharacterEndState extends State {
        @Override
        public State parse(char character) {
            if (character == '\'') {
                expressionBuilder.addValueToken(new CharacterLiteral(accumulator.charAt(0)));
                accumulator.setLength(0);
                return argumentOperatorState;
            }
            else {
                throw new SyntaxError();
            }
        }
    }

    private ArgumentZeroState argumentZeroState = new ArgumentZeroState();
    private class ArgumentZeroState extends State {
        @Override
        public State parse(char character) {
            if (character == 'x' || character == 'X') {
                accumulator.setLength(0);
                return argumentHexadecimalState;
            }
            else {
                return argumentNumberState.parse(character);
            }
        }
    }

    private ArgumentNumberState argumentNumberState = new ArgumentNumberState();
    private class ArgumentNumberState extends State {
        @Override
        public State parse(char character) {
            if (character >= '0' && character <= '9' || character >= 'A' && character <= 'F' ||
                character >= 'a' && character <= 'f') {
                accumulator.append(character);
                return argumentNumberState;
            }
            else {
                String string = accumulator.toString();
                if (character == 'H' || character == 'h') {
                    int value = parseInt(string, 16);
                    expressionBuilder.addValueToken(new IntegerLiteral(value));
                    accumulator.setLength(0);
                    return argumentOperatorState;
                }
                else if (character == 'O' || character == 'o') {
                    int value = parseInt(string, 8);
                    expressionBuilder.addValueToken(new IntegerLiteral(value));
                    accumulator.setLength(0);
                    return argumentOperatorState;
                }
                else {
                    if (string.endsWith("B") || string.endsWith("b")) {
                        int value = parseInt(string.substring(0, string.length() - 1), 2);
                        expressionBuilder.addValueToken(new IntegerLiteral(value));
                        accumulator.setLength(0);
                    }
                    else {
                        int value = parseInt(string, 10);
                        expressionBuilder.addValueToken(new IntegerLiteral(value));
                        accumulator.setLength(0);
                    }
                    return argumentOperatorState.parse(character);
                }
            }
        }
    }

    private ArgumentDollarState argumentDollarState = new ArgumentDollarState();
    private class ArgumentDollarState extends State {
        @Override
        public State parse(char character) {
            if (character >= '0' && character <= '9' || character >= 'A' && character <= 'F' ||
                character >= 'a' && character <= 'f') {
                accumulator.append(character);
                return argumentHexadecimalState;
            }
            else {
                expressionBuilder.addValueToken(new Identifier("$", scope));
                accumulator.setLength(0);
                return argumentOperatorState.parse(character);
            }
        }
    }

    private ArgumentHexadecimalState argumentHexadecimalState = new ArgumentHexadecimalState();
    private class ArgumentHexadecimalState extends State {
        @Override
        public State parse(char character) {
            if (character >= '0' && character <= '9' || character >= 'A' && character <= 'F' ||
                character >= 'a' && character <= 'f') {
                accumulator.append(character);
                return argumentHexadecimalState;
            }
            else {
                int value = parseInt(accumulator.toString(), 16);
                expressionBuilder.addValueToken(new IntegerLiteral(value));
                accumulator.setLength(0);
                return argumentOperatorState.parse(character);
            }
        }
    }

    private ArgumentBinaryState argumentBinaryState = new ArgumentBinaryState();
    private class ArgumentBinaryState extends State {
        @Override
        public State parse(char character) {
            if (character >= '0' && character <= '1') {
                accumulator.append(character);
                return argumentBinaryState;
            }
            else {
                int value = parseInt(accumulator.toString(), 2);
                expressionBuilder.addValueToken(new IntegerLiteral(value));
                accumulator.setLength(0);
                return argumentOperatorState.parse(character);
            }
        }
    }

    private ArgumentOperatorState argumentOperatorState = new ArgumentOperatorState();
    private class ArgumentOperatorState extends State {
        @Override
        public State parse(char character) {
            if (character == ')') {
                expressionBuilder.addOperatorToken(expressionBuilder.GROUP_CLOSE);
                return argumentOperatorState;
            }
            else if (character == '[') {
                expressionBuilder.addOperatorToken(expressionBuilder.INDEX_OPEN);
                return argumentValueState;
            }
            else if (character == ']') {
                expressionBuilder.addOperatorToken(expressionBuilder.INDEX_CLOSE);
                return argumentOperatorState;
            }
            else if (character == '.') {
                expressionBuilder.addOperatorToken(expressionBuilder.MEMBER);
                return argumentValueState;
            }
            else if (character == '*') {
                expressionBuilder.addOperatorToken(expressionBuilder.MULTIPLY);
                return argumentValueState;
            }
            else if (character == '/') {
                expressionBuilder.addOperatorToken(expressionBuilder.DIVIDE);
                return argumentValueState;
            }
            else if (character == '%') {
                expressionBuilder.addOperatorToken(expressionBuilder.MODULO);
                return argumentValueState;
            }
            else if (character == '+') {
                expressionBuilder.addOperatorToken(expressionBuilder.ADD);
                return argumentValueState;
            }
            else if (character == '-') {
                expressionBuilder.addOperatorToken(expressionBuilder.SUBTRACT);
                return argumentValueState;
            }
            else if (character == '<') {
                return argumentLessThanState;
            }
            else if (character == '>') {
                return argumentGreaterThanState;
            }
            else if (character == '=') {
                expressionBuilder.addOperatorToken(expressionBuilder.EQUALS);
                return argumentValueState;
            }
            else if (character == '!') {
                return argumentNotEqualsState;
            }
            else if (character == '&') {
                return argumentAndState;
            }
            else if (character == '^') {
                expressionBuilder.addOperatorToken(expressionBuilder.XOR);
                return argumentValueState;
            }
            else if (character == '|') {
                return argumentOrState;
            }
            else if (character == '?') {
                expressionBuilder.addOperatorToken(expressionBuilder.TERNARYIF);
                return argumentValueState;
            }
            else if (character == ':') {
                expressionBuilder.addOperatorToken(expressionBuilder.TERNARYELSE);
                return argumentValueState;
            }
            else if (character == ',') {
                expressionBuilder.addOperatorToken(expressionBuilder.SEQUENCE);
                return argumentValueState;
            }
            else if (isWhitespace(character)) {
                return argumentOperatorState;
            }
            else if (character == ';') {
                if (!expressionBuilder.hasOpenGroup()) {
                    lineBuilder.setArguments(expressionBuilder.getExpression());
                    return commentReadState;
                }
                else {
                    return commentReadThenOperatorState;
                }
            }
            else if (character == '\n' || character == '\0') {
                if (!expressionBuilder.hasOpenGroup() || character == '\0') {
                    lineBuilder.setArguments(expressionBuilder.getExpression());
                    return endState;
                }
                else {
                    return argumentOperatorState;
                }
            }
            else {
                expressionBuilder.addOperatorToken(expressionBuilder.ANNOTATION);
                return argumentValueState.parse(character);
            }
        }
    }

    private ArgumentLessThanState argumentLessThanState = new ArgumentLessThanState();
    private class ArgumentLessThanState extends State {
        @Override
        public State parse(char character) {
            if (character == '<') {
                expressionBuilder.addOperatorToken(expressionBuilder.SHIFT_LEFT);
                return argumentValueState;
            }
            else if (character == '=') {
                expressionBuilder.addOperatorToken(expressionBuilder.LESS_OR_EQUALS);
                return argumentValueState;
            }
            else {
                expressionBuilder.addOperatorToken(expressionBuilder.LESS_THAN);
                return argumentValueState.parse(character);
            }
        }
    }

    private ArgumentGreaterThanState argumentGreaterThanState = new ArgumentGreaterThanState();
    private class ArgumentGreaterThanState extends State {
        @Override
        public State parse(char character) {
            if (character == '>') {
                expressionBuilder.addOperatorToken(expressionBuilder.SHIFT_RIGHT);
                return argumentValueState;
            }
            else if (character == '=') {
                expressionBuilder.addOperatorToken(expressionBuilder.GREATER_OR_EQUALS);
                return argumentValueState;
            }
            else {
                expressionBuilder.addOperatorToken(expressionBuilder.GREATER_THAN);
                return argumentValueState.parse(character);
            }
        }
    }

    private ArgumentNotEqualsState argumentNotEqualsState = new ArgumentNotEqualsState();
    private class ArgumentNotEqualsState extends State {
        @Override
        public State parse(char character) {
            if (character == '=') {
                expressionBuilder.addOperatorToken(expressionBuilder.NOT_EQUALS);
                return argumentValueState;
            }
            else {
                expressionBuilder.addOperatorToken(expressionBuilder.ANNOTATION);
                expressionBuilder.addOperatorToken(expressionBuilder.NOT);
                return argumentValueState.parse(character);
            }
        }
    }

    private ArgumentAndState argumentAndState = new ArgumentAndState();
    private class ArgumentAndState extends State {
        @Override
        public State parse(char character) {
            if (character == '&') {
                expressionBuilder.addOperatorToken(expressionBuilder.LOGICAL_AND);
                return argumentValueState;
            }
            else {
                expressionBuilder.addOperatorToken(expressionBuilder.AND);
                return argumentValueState.parse(character);
            }
        }
    }

    private ArgumentOrState argumentOrState = new ArgumentOrState();
    private class ArgumentOrState extends State {
        @Override
        public State parse(char character) {
            if (character == '|') {
                expressionBuilder.addOperatorToken(expressionBuilder.LOGICAL_OR);
                return argumentValueState;
            }
            else {
                expressionBuilder.addOperatorToken(expressionBuilder.OR);
                return argumentValueState.parse(character);
            }
        }
    }

    private CommentReadState commentReadState = new CommentReadState();
    private class CommentReadState extends State {
        @Override
        public State parse(char character) {
            if (character == '\n' || character == '\0') {
                lineBuilder.setComment(accumulator.toString());
                accumulator.setLength(0);
                return endState;
            }
            else {
                accumulator.append(character);
                return commentReadState;
            }
        }
    }

    private CommentReadThenArgumentState commentReadThenArgumentState = new CommentReadThenArgumentState();
    private class CommentReadThenArgumentState extends State {
        @Override
        public State parse(char character) {
            if (character == '\n' || character == '\0') {
                lineBuilder.setComment(accumulator.toString());
                accumulator.setLength(0);
                if (character == '\0') {
                    throw new SyntaxError();
                }
                else {
                    return argumentValueState;
                }
            }
            else {
                accumulator.append(character);
                return commentReadState;
            }
        }
    }

    private CommentReadThenOperatorState commentReadThenOperatorState = new CommentReadThenOperatorState();
    private class CommentReadThenOperatorState extends State {
        @Override
        public State parse(char character) {
            if (character == '\n' || character == '\0') {
                lineBuilder.setComment(accumulator.toString());
                accumulator.setLength(0);
                if (character == '\0') {
                    lineBuilder.setArguments(expressionBuilder.getExpression());
                    return endState;
                }
                else {
                    return argumentOperatorState;
                }
            }
            else {
                accumulator.append(character);
                return commentReadState;
            }
        }
    }

    private EndState endState = new EndState();
    private class EndState extends State {
        @Override
        public State parse(char character) {
            throw new AssemblyException("End state reached but not all characters consumed.");
        }
    }

    private static int parseInt(String string, int radix) {
        try {
            long value = Long.parseLong(string, radix);
            if (value > 0xFFFFFFFFL) {
                throw new SyntaxError();
            }
            return (int) value;
        } catch (NumberFormatException e) {
            throw new SyntaxError();
        }
    }

    public static class SyntaxError extends AssemblyException {
        private static final long serialVersionUID = 1L;

        public SyntaxError() {
            this(null);
        }

        public SyntaxError(Throwable cause) {
            super("Syntax error.", cause);
        }

    }

}
