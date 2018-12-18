package nl.grauw.glass.instructions;

import java.util.List;

import nl.grauw.glass.Line;
import nl.grauw.glass.Scope;
import nl.grauw.glass.Source;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Schema;

public class Section extends InstructionFactory {

    public static Schema ARGUMENTS = new Schema(Schema.IDENTIFIER);

    private final Source source;

    public Section(Source source) {
        this.source = source;
    }

    public Source getSource() {
        return source;
    }

    @Override
    public List<Line> expand(Line line) {
        if (!ARGUMENTS.check(line.getArguments())) {
            throw new ArgumentException();
        }

        if (!line.getArguments().isSectionContext()) {
            throw new ArgumentException("Argument does not reference a section context.");
        }

        line.getArguments().getSectionContext().addSection(this);

        source.expand();
        return super.expand(line);
    }

    @Override
    public InstructionObject createObject(Scope context, Expression arguments) {
        if (ARGUMENTS.check(arguments)) {
            return new Empty.EmptyObject(context);
        }
        throw new ArgumentException();
    }

}
