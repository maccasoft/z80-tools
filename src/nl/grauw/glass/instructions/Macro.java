package nl.grauw.glass.instructions;

import java.util.List;

import nl.grauw.glass.AssemblyException;
import nl.grauw.glass.Line;
import nl.grauw.glass.Scope;
import nl.grauw.glass.Source;
import nl.grauw.glass.expressions.Equals;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Identifier;
import nl.grauw.glass.expressions.IntegerLiteral;

public class Macro extends InstructionFactory {

    private final Source source;
    private final Scope parameterScope;

    public Macro(Source source) {
        this.source = new Source(source.getScope());
        this.parameterScope = new Scope(source.getScope());
        this.source.addLines(source.getLineCopies(parameterScope));
        this.source.register();
    }

    @Override
    public List<Line> expand(Line line) {
        Expression parameters = line.getArguments();
        while (parameters != null) {
            Expression parameter = parameters.getElement();
            if (!(parameter instanceof Identifier) &&
                !(parameter instanceof Equals && ((Equals) parameter).getTerm1() instanceof Identifier)) {
                throw new ArgumentException("Parameter must be an identifier.");
            }

            if (parameter instanceof Equals) {
                Equals equals = (Equals) parameter;
                parameterScope.addSymbol(((Identifier) equals.getTerm1()).getName(), equals.getTerm2());
            }
            else {
                parameterScope.addSymbol(((Identifier) parameter).getName(), IntegerLiteral.ZERO);
            }
            parameters = parameters.getNext();
        }

        try {
            source.expand();
        } catch (AssemblyException e) {
            // ignore
        }
        return super.expand(line);
    }

    @Override
    public InstructionObject createObject(Scope context, Expression arguments) {
        return new MacroObject(context);
    }

    public class MacroObject extends Empty.EmptyObject {

        public MacroObject(Scope context) {
            super(context);
        }

        @Override
        public int resolve(int address) {
            try {
                source.resolve(0);
            } catch (AssemblyException e) {
                // ignore
            }
            return super.resolve(address);
        }

    }

}
