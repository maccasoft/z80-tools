package nl.grauw.glass.instructions;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Register;
import nl.grauw.glass.expressions.Schema;

public class Pop extends InstructionFactory {
	
	@Override
	public InstructionObject createObject(Scope context, Expression arguments) {
		if (Pop_RR.ARGUMENTS.check(arguments))
			return new Pop_RR(context, arguments.getElement(0));
		throw new ArgumentException();
	}
	
	public static class Pop_RR extends InstructionObject {
		
		public static Schema ARGUMENTS = new Schema(Schema.DIRECT_RR_AF_INDEX);
		
		Expression argument;
		
		public Pop_RR(Scope context, Expression argument) {
			super(context);
			this.argument = argument;
		}
		
		@Override
		public int getSize() {
			return indexifyDirect(argument.getRegister(), 1);
		}
		
		@Override
		public byte[] getBytes() {
			Register register = argument.getRegister();
			return indexifyDirect(register, (byte)(0xC1 | register.get16BitCode() << 4));
		}
		
	}
	
}
