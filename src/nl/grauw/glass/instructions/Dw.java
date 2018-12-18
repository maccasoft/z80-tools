package nl.grauw.glass.instructions;

import java.util.List;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;

public class Dw extends InstructionFactory {
	
	@Override
	public InstructionObject createObject(Scope context, Expression arguments) {
		if (arguments != null)
			return new Dw_N(context, arguments.getList());
		throw new ArgumentException();
	}
	
	public static class Dw_N extends InstructionObject {
		
		private List<Expression> arguments;
		
		public Dw_N(Scope context, List<Expression> arguments) {
			super(context);
			this.arguments = arguments;
		}
		
		@Override
		public int getSize() {
			return arguments.size() * 2;
		}
		
		@Override
		public byte[] getBytes() {
			byte[] bytes = new byte[arguments.size() * 2];
			for (int i = 0, length = arguments.size(); i < length; i++) {
				bytes[i * 2] = (byte)arguments.get(i).getInteger();
				bytes[i * 2 + 1] = (byte)(arguments.get(i).getInteger() >> 8);
			}
			return bytes;
		}
		
	}
	
}
