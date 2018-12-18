package nl.grauw.glass.instructions;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Schema;

public class Otir extends InstructionFactory {
	
	@Override
	public InstructionObject createObject(Scope context, Expression arguments) {
		if (Otir_.ARGUMENTS.check(arguments))
			return new Otir_(context);
		throw new ArgumentException();
	}
	
	public static class Otir_ extends InstructionObject {
		
		public static Schema ARGUMENTS = new Schema();
		
		public Otir_(Scope context) {
			super(context);
		}
		
		@Override
		public int getSize() {
			return 2;
		}
		
		@Override
		public byte[] getBytes() {
			return new byte[] { (byte)0xED, (byte)0xB3 };
		}
		
	}
	
}
