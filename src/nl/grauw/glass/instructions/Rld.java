package nl.grauw.glass.instructions;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Schema;

public class Rld extends InstructionFactory {
	
	@Override
	public InstructionObject createObject(Scope context, Expression arguments) {
		if (Rld_.ARGUMENTS.check(arguments))
			return new Rld_(context);
		throw new ArgumentException();
	}
	
	public static class Rld_ extends InstructionObject {
		
		public static Schema ARGUMENTS = new Schema();
		
		public Rld_(Scope context) {
			super(context);
		}
		
		@Override
		public int getSize() {
			return 2;
		}
		
		@Override
		public byte[] getBytes() {
			return new byte[] { (byte)0xED, (byte)0x6F };
		}
		
	}
	
}
