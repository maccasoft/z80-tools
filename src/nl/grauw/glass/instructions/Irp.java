package nl.grauw.glass.instructions;

import java.util.List;

import nl.grauw.glass.Line;
import nl.grauw.glass.ParameterScope;
import nl.grauw.glass.Scope;
import nl.grauw.glass.Source;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Schema;

public class Irp extends InstructionFactory {

    private final Source source;

    public Irp(Source source) {
        this.source = source;
    }

    @Override
    public List<Line> expand(Line line) {
        Expression arguments = line.getArguments();
        if (arguments == null || !Schema.IDENTIFIER.check(arguments.getElement())) {
            throw new ArgumentException();
        }

        List<Line> lines = super.expand(line);
        Expression parameter = arguments.getElement();
        for (int i = 0; (arguments = arguments.getNext()) != null; i++) {
            Scope parameterScope = new ParameterScope(line.getScope(), parameter, arguments.getElement());

            // set up the number symbol
            line.getScope().addSymbol(Integer.toString(i), parameterScope);
            Line rept = new Line(parameterScope, line);
            rept.setInstruction(Empty.INSTANCE);
            lines.add(rept); // so that the parameterScope address gets initialised

            // copy & expand content
            List<Line> lineCopies = source.getLineCopies(parameterScope);
            for (Line lineCopy : lineCopies) {
                lineCopy.register(parameterScope);
            }
            for (Line lineCopy : lineCopies) {
                lines.addAll(lineCopy.expand());
            }
        }
        return lines;
    }

    @Override
    public InstructionObject createObject(Scope context, Expression arguments) {
        return new Empty.EmptyObject(context);
    }

}
