package nl.grauw.glass.instructions;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Schema;

public class Equ extends InstructionFactory {
	
	public static Schema ARGUMENTS = new Schema(Schema.ANY);
	
	@Override
	public InstructionObject createObject(Scope context, Expression arguments) {
		if (ARGUMENTS.check(arguments))
			return new Empty.EmptyObject(context);
		throw new ArgumentException();
	}
	
}
