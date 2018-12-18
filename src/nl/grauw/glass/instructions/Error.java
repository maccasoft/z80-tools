package nl.grauw.glass.instructions;

import nl.grauw.glass.AssemblyException;
import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Schema;

public class Error extends InstructionFactory {
	
	public static Schema ARGUMENTS = new Schema();
	public static Schema ARGUMENTS_S = new Schema(Schema.STRING);
	
	@Override
	public InstructionObject createObject(Scope context, Expression arguments) {
		if (ARGUMENTS.check(arguments) || ARGUMENTS_S.check(arguments))
			return new Error_(context, arguments);
		throw new ArgumentException();
	}
	
	public static class Error_ extends Empty.EmptyObject {
		
		private final Expression argument;
		
		public Error_(Scope context, Expression argument) {
			super(context);
			this.argument = argument;
		}
		
		@Override
		public byte[] getBytes() {
			if (argument == null)
				throw new ErrorDirectiveException();
			throw new ErrorDirectiveException(argument.getString());
		}
		
	}
	
	public static class ErrorDirectiveException extends AssemblyException {
		private static final long serialVersionUID = 1L;
		
		public ErrorDirectiveException() {
			this("Error directive was encountered.");
		}
		
		public ErrorDirectiveException(String message) {
			super(message);
		}
		
	}
	
}
