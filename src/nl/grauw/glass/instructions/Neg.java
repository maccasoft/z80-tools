package nl.grauw.glass.instructions;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Schema;

public class Neg extends InstructionFactory {
	
	@Override
	public InstructionObject createObject(Scope context, Expression arguments) {
		if (Neg_.ARGUMENTS.check(arguments))
			return new Neg_(context);
		throw new ArgumentException();
	}
	
	public static class Neg_ extends InstructionObject {
		
		public static Schema ARGUMENTS = new Schema();
		
		public Neg_(Scope context) {
			super(context);
		}
		
		@Override
		public int getSize() {
			return 2;
		}
		
		@Override
		public byte[] getBytes() {
			return new byte[] { (byte)0xED, (byte)0x44 };
		}
		
	}
	
}
