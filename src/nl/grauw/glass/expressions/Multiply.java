package nl.grauw.glass.expressions;

public class Multiply extends BinaryOperator {
	
	public Multiply(Expression multiplicand, Expression multiplier) {
		super(multiplicand, multiplier);
	}
	
	@Override
	public Multiply copy(Context context) {
		return new Multiply(term1.copy(context), term2.copy(context));
	}
	
	public Expression getMultiplicand() {
		return term1;
	}
	
	public Expression getMultiplier() {
		return term2;
	}
	
	@Override
	public int getInteger() {
		return term1.getInteger() * term2.getInteger();
	}
	
	@Override
	public String getLexeme() {
		return "*";
	}
	
}
