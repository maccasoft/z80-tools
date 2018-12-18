package nl.grauw.glass.instructions;

import nl.grauw.glass.AssemblyException;

public class ArgumentException extends AssemblyException {
	private static final long serialVersionUID = 1L;
	
	public ArgumentException() {
		this("Invalid arguments.");
	}
	
	public ArgumentException(String message) {
		super(message);
	}
	
}
