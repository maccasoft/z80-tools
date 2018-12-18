package nl.grauw.glass.instructions;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Register;
import nl.grauw.glass.expressions.Schema;

public class Sub extends InstructionFactory {
	
	@Override
	public InstructionObject createObject(Scope context, Expression arguments) {
		if (Sub_R.ARGUMENTS.check(arguments))
			return new Sub_R(context, arguments);
		if (Sub_N.ARGUMENTS.check(arguments))
			return new Sub_N(context, arguments);
		throw new ArgumentException();
	}
	
	public static class Sub_R extends InstructionObject {
		
		public static Schema ARGUMENTS = new Schema(Schema.DIRECT_R_INDIRECT_HL_IX_IY);
		
		private Expression argument;
		
		public Sub_R(Scope context, Expression arguments) {
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
			return indexifyIndirect(register, (byte)(0x90 | register.get8BitCode()));
		}
		
	}
	
	public static class Sub_N extends InstructionObject {
		
		public static Schema ARGUMENTS = new Schema(Schema.DIRECT_N);
		
		private Expression argument;
		
		public Sub_N(Scope context, Expression arguments) {
			super(context);
			this.argument = arguments;
		}
		
		@Override
		public int getSize() {
			return 2;
		}
		
		@Override
		public byte[] getBytes() {
			return new byte[] { (byte)0xD6, (byte)argument.getInteger() };
		}
		
	}
	
}
