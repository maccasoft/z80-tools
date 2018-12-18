package nl.grauw.glass.instructions;

import java.util.ArrayList;
import java.util.List;

import nl.grauw.glass.Line;
import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;

public abstract class InstructionFactory {

    public List<Line> expand(Line line) {
        List<Line> lines = new ArrayList<Line>();
        lines.add(line);
        return lines;
    }

    public abstract InstructionObject createObject(Scope context, Expression arguments);

}
