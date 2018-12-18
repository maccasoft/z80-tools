package nl.grauw.glass.instructions;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Register;
import nl.grauw.glass.expressions.Schema;

public class Rrc extends InstructionFactory {
	
	@Override
	public InstructionObject createObject(Scope context, Expression arguments) {
		if (Rrc_R.ARGUMENTS.check(arguments))
			return new Rrc_R(context, arguments.getElement(0));
		throw new ArgumentException();
	}
	
	public static class Rrc_R extends InstructionObject {
		
		public static Schema ARGUMENTS = new Schema(Schema.DIRECT_R_INDIRECT_HL_IX_IY);
		
		private Expression argument;
		
		public Rrc_R(Scope context, Expression argument) {
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
			return indexifyOnlyIndirect(register, (byte)0xCB, (byte)(0x08 + register.get8BitCode()));
		}
		
	}
	
}
