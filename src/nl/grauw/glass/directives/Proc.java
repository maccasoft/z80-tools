package nl.grauw.glass.directives;

import nl.grauw.glass.Line;
import nl.grauw.glass.Scope;
import nl.grauw.glass.Source;

public class Proc extends Directive {
	
	private final Source source;
	
	public Proc(Source source) {
		this.source = source;
	}
	
	@Override
	public void register(Scope scope, Line line) {
		line.setInstruction(new nl.grauw.glass.instructions.Proc(source));
		super.register(scope, line);
	}
	
}
