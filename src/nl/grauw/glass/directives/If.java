package nl.grauw.glass.directives;

import nl.grauw.glass.Line;
import nl.grauw.glass.Scope;
import nl.grauw.glass.Source;

public class If extends Directive {
	
	private final Source thenSource;
	private final Source elseSource;
	
	public If(Source thenSource, Source elseSource) {
		this.thenSource = thenSource;
		this.elseSource = elseSource;
	}
	
	@Override
	public void register(Scope scope, Line line) {
		line.setInstruction(new nl.grauw.glass.instructions.If(
				new Source(scope, thenSource),
				elseSource != null ? new Source(scope, elseSource) : null
			));
		super.register(scope, line);
	}
	
}
