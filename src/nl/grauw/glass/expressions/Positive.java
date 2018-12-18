package nl.grauw.glass.expressions;

public class Positive extends UnaryOperator {
	
	public Positive(Expression term) {
		super(term);
	}
	
	@Override
	public Positive copy(Context context) {
		return new Positive(term.copy(context));
	}
	
	@Override
	public int getInteger() {
		return +term.getInteger();
	}
	
	@Override
	public String getLexeme() {
		return "+";
	}
	
}
