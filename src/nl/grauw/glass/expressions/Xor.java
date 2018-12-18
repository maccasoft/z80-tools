package nl.grauw.glass.expressions;

public class Xor extends BinaryOperator {
	
	public Xor(Expression term1, Expression term2) {
		super(term1, term2);
	}
	
	@Override
	public Xor copy(Context context) {
		return new Xor(term1.copy(context), term2.copy(context));
	}
	
	@Override
	public int getInteger() {
		return term1.getInteger() ^ term2.getInteger();
	}
	
	@Override
	public String getLexeme() {
		return "^";
	}
	
}
