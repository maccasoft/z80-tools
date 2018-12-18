package nl.grauw.glass.instructions;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Schema;

public class Lddr extends InstructionFactory {
	
	@Override
	public InstructionObject createObject(Scope context, Expression arguments) {
		if (Lddr_.ARGUMENTS.check(arguments))
			return new Lddr_(context);
		throw new ArgumentException();
	}
	
	public static class Lddr_ extends InstructionObject {
		
		public static Schema ARGUMENTS = new Schema();
		
		public Lddr_(Scope context) {
			super(context);
		}
		
		@Override
		public int getSize() {
			return 2;
		}
		
		@Override
		public byte[] getBytes() {
			return new byte[] { (byte)0xED, (byte)0xB8 };
		}
		
	}
	
}
