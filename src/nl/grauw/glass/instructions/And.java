package nl.grauw.glass.instructions;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Register;
import nl.grauw.glass.expressions.Schema;

public class And extends InstructionFactory {
	
	@Override
	public InstructionObject createObject(Scope context, Expression arguments) {
		if (And_R.ARGUMENTS.check(arguments))
			return new And_R(context, arguments);
		if (And_N.ARGUMENTS.check(arguments))
			return new And_N(context, arguments);
		throw new ArgumentException();
	}
	
	public static class And_R extends InstructionObject {
		
		public static Schema ARGUMENTS = new Schema(Schema.DIRECT_R_INDIRECT_HL_IX_IY);
		
		private Expression argument;
		
		public And_R(Scope context, Expression arguments) {
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
			return indexifyIndirect(register, (byte)(0xA0 | register.get8BitCode()));
		}
		
	}
	
	public static class And_N extends InstructionObject {
		
		public static Schema ARGUMENTS = new Schema(Schema.DIRECT_N);
		
		private Expression argument;
		
		public And_N(Scope context, Expression arguments) {
			super(context);
			this.argument = arguments;
		}
		
		@Override
		public int getSize() {
			return 2;
		}
		
		@Override
		public byte[] getBytes() {
			return new byte[] { (byte)0xE6, (byte)argument.getInteger() };
		}
		
	}
	
}
