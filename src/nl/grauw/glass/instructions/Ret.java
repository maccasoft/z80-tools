package nl.grauw.glass.instructions;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Schema;

public class Ret extends InstructionFactory {
	
	@Override
	public InstructionObject createObject(Scope context, Expression arguments) {
		if (Ret_.ARGUMENTS.check(arguments))
			return new Ret_(context);
		if (Ret_F.ARGUMENTS.check(arguments))
			return new Ret_F(context, arguments.getElement(0));
		throw new ArgumentException();
	}
	
	public static class Ret_ extends InstructionObject {
		
		public static Schema ARGUMENTS = new Schema();
		
		public Ret_(Scope context) {
			super(context);
		}
		
		@Override
		public int getSize() {
			return 1;
		}
		
		@Override
		public byte[] getBytes() {
			return new byte[] { (byte)0xC9 };
		}
		
	}
	
	public static class Ret_F extends InstructionObject {
		
		public static Schema ARGUMENTS = new Schema(new Schema.IsFlag());
		
		private Expression argument;
		
		public Ret_F(Scope context, Expression argument) {
			super(context);
			this.argument = argument;
		}
		
		@Override
		public int getSize() {
			return 1;
		}
		
		@Override
		public byte[] getBytes() {
			return new byte[] { (byte)(0xC0 | argument.getFlag().getCode() << 3) };
		}
		
	}
	
}
