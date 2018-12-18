package nl.grauw.glass.instructions;

import java.util.List;

import nl.grauw.glass.Line;
import nl.grauw.glass.ParameterScope;
import nl.grauw.glass.Scope;
import nl.grauw.glass.Source;
import nl.grauw.glass.expressions.Equals;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Identifier;

public class MacroInstruction extends InstructionFactory {

    private final Expression parameters;
    private final Source source;

    public MacroInstruction(Expression parameters, Source source) {
        this.parameters = parameters;
        this.source = source;

        Expression parameter = parameters != null ? parameters.getElement(0) : null;
        for (int i = 0; parameter != null; i++) {
            if (!(parameter instanceof Identifier) &&
                !(parameter instanceof Equals && ((Equals) parameter).getTerm1() instanceof Identifier)) {
                throw new ArgumentException("Parameter must be an identifier.");
            }
            parameter = parameters.getElement(i);
        }
    }

    @Override
    public List<Line> expand(Line line) {
        Scope parameterScope = new ParameterScope(source.getScope(), parameters, line.getArguments());
        List<Line> lines = super.expand(line);
        List<Line> lineCopies = source.getLineCopies(parameterScope);
        for (Line lineCopy : lineCopies) {
            lineCopy.register(parameterScope);
            lineCopy.register(line.getScope());
        }
        for (Line lineCopy : lineCopies) {
            lines.addAll(lineCopy.expand());
        }
        return lines;
    }

    @Override
    public InstructionObject createObject(Scope context, Expression arguments) {
        return new Empty.EmptyObject(context);
    }

}
