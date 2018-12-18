package nl.grauw.glass.instructions;

import java.util.List;

import nl.grauw.glass.Line;
import nl.grauw.glass.Scope;
import nl.grauw.glass.Source;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Schema;

public class Proc extends InstructionFactory {
	
	public static Schema ARGUMENTS = new Schema();
	
	private final Source source;
	
	public Proc(Source source) {
		this.source = source;
	}
	
	public List<Line> expand(Line line) {
		Expression arguments = line.getArguments();
		if (!ARGUMENTS.check(arguments))
			throw new ArgumentException();
		
		List<Line> lines = super.expand(line);
		List<Line> lineCopies = source.getLineCopies(line.getScope());
		for (Line lineCopy : lineCopies)
			lineCopy.register(line.getScope());
		for (Line lineCopy : lineCopies)
			lines.addAll(lineCopy.expand());
		return lines;
	}
	
	@Override
	public InstructionObject createObject(Scope context, Expression arguments) {
		return new Empty.EmptyObject(context);
	}
	
}
