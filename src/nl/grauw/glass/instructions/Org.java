package nl.grauw.glass.instructions;

import java.io.IOException;
import java.io.OutputStream;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Schema;

public class Org extends InstructionFactory {
	
	@Override
	public InstructionObject createObject(Scope context, Expression arguments) {
		if (Org_N.ARGUMENTS.check(arguments))
			return new Org_N(context, arguments.getElement(0));
		throw new ArgumentException();
	}
	
	public static class Org_N extends Empty.EmptyObject {
		
		public static Schema ARGUMENTS = new Schema(Schema.INTEGER);
		
		private Expression argument;
		
		public Org_N(Scope context, Expression argument) {
			super(context);
			this.argument = argument;
		}
		
		public int getAddress() {
			return argument.getAddress();
		}
		
		@Override
		public int resolve(int address) {
			super.resolve(address);
			return getAddress();
		}
		
		@Override
		public void generateObjectCode(OutputStream output) throws IOException {
		}
		
	}
	
}
