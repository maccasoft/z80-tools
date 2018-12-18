package nl.grauw.glass.instructions;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Schema;

public class Cpir extends InstructionFactory {
	
	@Override
	public InstructionObject createObject(Scope context, Expression arguments) {
		if (Cpir_.ARGUMENTS.check(arguments))
			return new Cpir_(context);
		throw new ArgumentException();
	}
	
	public static class Cpir_ extends InstructionObject {
		
		public static Schema ARGUMENTS = new Schema();
		
		public Cpir_(Scope context) {
			super(context);
		}
		
		@Override
		public int getSize() {
			return 2;
		}
		
		@Override
		public byte[] getBytes() {
			return new byte[] { (byte)0xED, (byte)0xB1 };
		}
		
	}
	
}
