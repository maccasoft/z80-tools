package nl.grauw.glass.expressions;

import nl.grauw.glass.AssemblyException;

public class EvaluationException extends AssemblyException {
	private static final long serialVersionUID = 1L;
	
	public EvaluationException() {
		this(null);
	}
	
	public EvaluationException(String message) {
		super(message);
	}
	
}
