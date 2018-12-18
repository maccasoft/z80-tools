package nl.grauw.glass;

import nl.grauw.glass.expressions.Equals;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Identifier;
import nl.grauw.glass.instructions.ArgumentException;

public class ParameterScope extends Scope {
	
	public ParameterScope(Scope parent, Expression parameters, Expression arguments) {
		super(parent);
		
		while (parameters != null) {
			Expression parameter = parameters.getElement();
			Expression argument;
			
			if (parameter instanceof Equals) {
				argument = arguments != null ? arguments.getElement() : ((Equals)parameter).getTerm2();
				parameter = ((Equals)parameter).getTerm1();
			} else {
				if (arguments == null)
					throw new ArgumentException("Not enough arguments.");
				argument = arguments.getElement();
			}
			
			if (!(parameter instanceof Identifier))
				throw new ArgumentException("Parameter must be an identifier.");
			
			addSymbol(((Identifier)parameter).getName(), argument);
			
			parameters = parameters.getNext();
			if (arguments != null)
				arguments = arguments.getNext();
		}
		if (arguments != null)
			throw new ArgumentException("Too many arguments.");
	}
	
}
