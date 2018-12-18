package nl.grauw.glass.instructions;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Register;
import nl.grauw.glass.expressions.Schema;

public class Bit extends InstructionFactory {
	
	@Override
	public InstructionObject createObject(Scope context, Expression arguments) {
		if (Bit_N_R.ARGUMENTS.check(arguments))
			return new Bit_N_R(context, arguments.getElement(0), arguments.getElement(1));
		throw new ArgumentException();
	}
	
	public static class Bit_N_R extends InstructionObject {
		
		public static Schema ARGUMENTS = new Schema(Schema.DIRECT_N, Schema.DIRECT_R_INDIRECT_HL_IX_IY);
		
		private Expression argument1;
		private Expression argument2;
		
		public Bit_N_R(Scope context, Expression argument1, Expression argument2) {
			super(context);
			this.argument1 = argument1;
			this.argument2 = argument2;
		}
		
		@Override
		public int getSize() {
			return indexifyIndirect(argument2.getRegister(), 2);
		}
		
		@Override
		public byte[] getBytes() {
			int value = argument1.getInteger();
			if (value < 0 || value > 7)
				throw new ArgumentException();
			Register register = argument2.getRegister();
			return indexifyOnlyIndirect(register, (byte)0xCB, (byte)(0x40 | value << 3 | register.get8BitCode()));
		}
		
	}
	
}
