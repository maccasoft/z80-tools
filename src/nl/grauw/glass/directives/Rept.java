package nl.grauw.glass.directives;

import nl.grauw.glass.Line;
import nl.grauw.glass.Scope;
import nl.grauw.glass.Source;

public class Rept extends Directive {
	
	private final Source source;
	
	public Rept(Source source) {
		this.source = source;
	}
	
	@Override
	public void register(Scope scope, Line line) {
		line.setInstruction(new nl.grauw.glass.instructions.Rept(source));
		super.register(scope, line);
	}
	
}
