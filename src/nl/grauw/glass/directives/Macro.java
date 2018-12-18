package nl.grauw.glass.directives;

import nl.grauw.glass.AssemblyException;
import nl.grauw.glass.Line;
import nl.grauw.glass.Scope;
import nl.grauw.glass.Source;
import nl.grauw.glass.expressions.Instruction;
import nl.grauw.glass.instructions.MacroInstruction;

public class Macro extends Directive {
	
	private final Source source;
	
	public Macro(Source source) {
		this.source = source;
	}
	
	@Override
	public void register(Scope scope, Line line) {
		if (line.getLabel() == null)
			throw new AssemblyException("Macro without label.");
		scope.addSymbol(line.getLabel(),
			new Instruction(
				new MacroInstruction(line.getArguments(), source),
				source.getScope()
			)
		);
		line.setInstruction(new nl.grauw.glass.instructions.Macro(source));
	}
	
}
