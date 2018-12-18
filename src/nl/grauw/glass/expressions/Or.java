package nl.grauw.glass.expressions;

public class Or extends BinaryOperator {
	
	public Or(Expression term1, Expression term2) {
		super(term1, term2);
	}
	
	@Override
	public Or copy(Context context) {
		return new Or(term1.copy(context), term2.copy(context));
	}
	
	@Override
	public int getInteger() {
		return term1.getInteger() | term2.getInteger();
	}
	
	@Override
	public String getLexeme() {
		return "|";
	}
	
}
