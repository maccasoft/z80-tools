package nl.grauw.glass.instructions;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Schema;

public class Halt extends InstructionFactory {
	
	@Override
	public InstructionObject createObject(Scope context, Expression arguments) {
		if (Halt_.ARGUMENTS.check(arguments))
			return new Halt_(context);
		throw new ArgumentException();
	}
	
	public static class Halt_ extends InstructionObject {
		
		public static Schema ARGUMENTS = new Schema();
		
		public Halt_(Scope context) {
			super(context);
		}
		
		@Override
		public int getSize() {
			return 1;
		}
		
		@Override
		public byte[] getBytes() {
			return new byte[] { (byte)0x76 };
		}
		
	}
	
}
