package nl.grauw.glass.directives;

import java.io.File;
import java.util.List;

import nl.grauw.glass.Line;
import nl.grauw.glass.Scope;

public class Incbin extends Directive {
	
	private final File sourceFile;
	private final List<File> includePaths;
	
	public Incbin(File sourceFile, List<File> includePaths) {
		this.sourceFile = sourceFile;
		this.includePaths = includePaths;
	}
	
	@Override
	public void register(Scope scope, Line line) {
		line.setInstruction(new nl.grauw.glass.instructions.Incbin(sourceFile, includePaths));
		super.register(scope, line);
	}
	
}
