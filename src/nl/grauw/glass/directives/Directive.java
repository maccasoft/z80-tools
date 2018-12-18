package nl.grauw.glass.directives;

import nl.grauw.glass.Line;
import nl.grauw.glass.Scope;

public abstract class Directive {
	
	public void register(Scope scope, Line line) {
		if (line.getLabel() != null)
			scope.addSymbol(line.getLabel(), line.getScope());
	}
	
}
