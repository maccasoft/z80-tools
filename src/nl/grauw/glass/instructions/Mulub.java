package nl.grauw.glass.instructions;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Schema;

public class Mulub extends InstructionFactory {
	
	@Override
	public InstructionObject createObject(Scope context, Expression arguments) {
		if (Mulub_R_R.ARGUMENTS.check(arguments))
			return new Mulub_R_R(context, arguments.getElement(1));
		throw new ArgumentException();
	}
	
	public static class Mulub_R_R extends InstructionObject {
		
		public static Schema ARGUMENTS = new Schema(Schema.DIRECT_A, Schema.DIRECT_R);
		
		private Expression argument;
		
		public Mulub_R_R(Scope context, Expression argument) {
			super(context);
			this.argument = argument;
		}
		
		@Override
		public int getSize() {
			return 2;
		}
		
		@Override
		public byte[] getBytes() {
			return new byte[] { (byte)0xED, (byte)(0xC1 | argument.getRegister().get8BitCode() << 3) };
		}
		
	}
	
}
