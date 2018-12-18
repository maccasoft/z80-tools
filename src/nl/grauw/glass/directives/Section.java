package nl.grauw.glass.directives;

import nl.grauw.glass.Line;
import nl.grauw.glass.Scope;
import nl.grauw.glass.Source;

public class Section extends Directive {
	
	private final Source source;
	
	public Section(Source source) {
		this.source = source;
	}
	
	@Override
	public void register(Scope scope, Line line) {
		nl.grauw.glass.instructions.Section section = new nl.grauw.glass.instructions.Section(source);
		line.setInstruction(section);
		
		source.register();
		super.register(scope, line);
	}
	
}
