package nl.grauw.glass.directives;

import nl.grauw.glass.Line;
import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.SectionContextLiteral;

public class Fill extends Directive {

    @Override
    public void register(Scope scope, Line line) {
        nl.grauw.glass.instructions.Fill fill = new nl.grauw.glass.instructions.Fill();
        line.setInstruction(fill);
        if (line.getLabel() != null) {
            scope.addSymbol(line.getLabel(), new SectionContextLiteral(line.getScope(), fill));
        }
    }

}
