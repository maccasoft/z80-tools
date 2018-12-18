package nl.grauw.glass.instructions;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Register;
import nl.grauw.glass.expressions.Schema;

public class Inc extends InstructionFactory {
	
	@Override
	public InstructionObject createObject(Scope context, Expression arguments) {
		if (Inc_R.ARGUMENTS.check(arguments))
			return new Inc_R(context, arguments.getElement(0));
		if (Inc_RR.ARGUMENTS.check(arguments))
			return new Inc_RR(context, arguments.getElement(0));
		throw new ArgumentException();
	}
	
	public static class Inc_R extends InstructionObject {
		
		public static Schema ARGUMENTS = new Schema(Schema.DIRECT_R_INDIRECT_HL_IX_IY);
		
		private Expression argument;
		
		public Inc_R(Scope context, Expression arguments) {
			super(context);
			this.argument = arguments;
		}
		
		@Override
		public int getSize() {
			return indexifyIndirect(argument.getRegister(), 1);
		}
		
		@Override
		public byte[] getBytes() {
			Register register = argument.getRegister();
			return indexifyIndirect(register, (byte)(0x04 | register.get8BitCode() << 3));
		}
		
	}
	
	public static class Inc_RR extends InstructionObject {
		
		public static Schema ARGUMENTS = new Schema(Schema.DIRECT_RR_INDEX);
		
		private Expression argument;
		
		public Inc_RR(Scope context, Expression arguments) {
			super(context);
			this.argument = arguments;
		}
		
		@Override
		public int getSize() {
			return indexifyDirect(argument.getRegister(), 1);
		}
		
		@Override
		public byte[] getBytes() {
			Register register = argument.getRegister();
			return indexifyDirect(register, (byte)(0x03 | register.get16BitCode() << 4));
		}
		
	}
	
}
