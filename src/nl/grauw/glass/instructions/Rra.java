package nl.grauw.glass.instructions;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Schema;

public class Rra extends InstructionFactory {
	
	@Override
	public InstructionObject createObject(Scope context, Expression arguments) {
		if (Rra_.ARGUMENTS.check(arguments))
			return new Rra_(context);
		throw new ArgumentException();
	}
	
	public static class Rra_ extends InstructionObject {
		
		public static Schema ARGUMENTS = new Schema();
		
		public Rra_(Scope context) {
			super(context);
		}
		
		@Override
		public int getSize() {
			return 1;
		}
		
		@Override
		public byte[] getBytes() {
			return new byte[] { (byte)0x1F };
		}
		
	}
	
}
