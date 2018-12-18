package nl.grauw.glass.instructions;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;

public class Empty extends InstructionFactory {
	
	public static final Empty INSTANCE = new Empty();
	
	@Override
	public InstructionObject createObject(Scope context, Expression arguments) {
		return new EmptyObject(context);
	}
	
	public static class EmptyObject extends InstructionObject {
		
		private static final byte[] NO_BYTES = new byte[] {};
		
		public EmptyObject(Scope context) {
			super(context);
		}
		
		@Override
		public int getSize() {
			return 0;
		}
		
		@Override
		public byte[] getBytes() {
			return NO_BYTES;
		}
		
	}
	
}
