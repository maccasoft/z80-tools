package nl.grauw.glass.instructions;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Register;
import nl.grauw.glass.expressions.Schema;

public class Dec extends InstructionFactory {
	
	@Override
	public InstructionObject createObject(Scope context, Expression arguments) {
		if (Dec_R.ARGUMENTS.check(arguments))
			return new Dec_R(context, arguments.getElement(0));
		if (Dec_RR.ARGUMENTS.check(arguments))
			return new Dec_RR(context, arguments.getElement(0));
		throw new ArgumentException();
	}
	
	public static class Dec_R extends InstructionObject {
		
		public static Schema ARGUMENTS = new Schema(Schema.DIRECT_R_INDIRECT_HL_IX_IY);
		
		private Expression argument;
		
		public Dec_R(Scope context, Expression arguments) {
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
			return indexifyIndirect(register, (byte)(0x05 | register.get8BitCode() << 3));
		}
		
	}
	
	public static class Dec_RR extends InstructionObject {
		
		public static Schema ARGUMENTS = new Schema(Schema.DIRECT_RR_INDEX);
		
		private Expression argument;
		
		public Dec_RR(Scope context, Expression arguments) {
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
			return indexifyDirect(register, (byte)(0x0B | register.get16BitCode() << 4));
		}
		
	}
	
}
