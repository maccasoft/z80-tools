package nl.grauw.glass.instructions;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Register;
import nl.grauw.glass.expressions.Schema;

public class Sra extends InstructionFactory {
	
	@Override
	public InstructionObject createObject(Scope context, Expression arguments) {
		if (Sra_R.ARGUMENTS.check(arguments))
			return new Sra_R(context, arguments.getElement(0));
		throw new ArgumentException();
	}
	
	public static class Sra_R extends InstructionObject {
		
		public static Schema ARGUMENTS = new Schema(Schema.DIRECT_R_INDIRECT_HL_IX_IY);
		
		private Expression argument;
		
		public Sra_R(Scope context, Expression argument) {
			super(context);
			this.argument = argument;
		}
		
		@Override
		public int getSize() {
			return indexifyIndirect(argument.getRegister(), 2);
		}
		
		@Override
		public byte[] getBytes() {
			Register register = argument.getRegister();
			return indexifyOnlyIndirect(register, (byte)0xCB, (byte)(0x28 + register.get8BitCode()));
		}
		
	}
	
}
