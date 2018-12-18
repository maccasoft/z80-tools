package nl.grauw.glass.instructions;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Schema;

public class Scf extends InstructionFactory {
	
	@Override
	public InstructionObject createObject(Scope context, Expression arguments) {
		if (Scf_.ARGUMENTS.check(arguments))
			return new Scf_(context);
		throw new ArgumentException();
	}
	
	public static class Scf_ extends InstructionObject {
		
		public static Schema ARGUMENTS = new Schema();
		
		public Scf_(Scope context) {
			super(context);
		}
		
		@Override
		public int getSize() {
			return 1;
		}
		
		@Override
		public byte[] getBytes() {
			return new byte[] { (byte)0x37 };
		}
		
	}
	
}
