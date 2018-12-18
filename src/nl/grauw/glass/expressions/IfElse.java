package nl.grauw.glass.expressions;

public class IfElse extends Passthrough {
	
	private final Expression condition;
	private final Expression trueTerm;
	private final Expression falseTerm;
	
	public IfElse(Expression condition, Expression trueTerm, Expression falseTerm) {
		this.condition = condition;
		this.trueTerm = trueTerm;
		this.falseTerm = falseTerm;
	}
	
	@Override
	public IfElse copy(Context context) {
		return new IfElse(condition.copy(context), trueTerm.copy(context), falseTerm.copy(context));
	}
	
	public Expression getCondition() {
		return condition;
	}
	
	public Expression getTrueTerm() {
		return trueTerm;
	}
	
	public Expression getFalseTerm() {
		return falseTerm;
	}
	
	public boolean isTrue() {
		return condition.getInteger() != 0;
	}
	
	@Override
	public Expression resolve() {
		return isTrue() ? trueTerm : falseTerm;
	}
	
	@Override
	public boolean isInteger() {
		return (trueTerm.isInteger() && falseTerm.isInteger()) || super.isInteger();
	}
	
	@Override
	public boolean isString() {
		return (trueTerm.isString() && falseTerm.isString()) || super.isString();
	}
	
	@Override
	public boolean isRegister() {
		return (trueTerm.isRegister() && falseTerm.isRegister()) || super.isRegister();
	}
	
	@Override
	public boolean isFlag() {
		return (trueTerm.isFlag() && falseTerm.isFlag()) || super.isFlag();
	}
	
	@Override
	public boolean isContext() {
		return (trueTerm.isContext() && falseTerm.isContext()) || super.isContext();
	}
	
	@Override
	public String toString() {
		return "" + condition + " ? " + trueTerm + " : " + falseTerm;
	}
	
	@Override
	public String toDebugString() {
		return "{" + condition.toDebugString() + " ? " + trueTerm.toDebugString() + " : " + falseTerm.toDebugString() + "}";
	}
	
}
