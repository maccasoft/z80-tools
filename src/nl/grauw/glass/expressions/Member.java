package nl.grauw.glass.expressions;

public class Member extends Passthrough {
	
	private final Expression object;
	private final Identifier subject;
	
	public Member(Expression object, Identifier subject) {
		this.object = object;
		this.subject = subject;
	}
	
	@Override
	public Member copy(Context context) {
		return new Member(object.copy(context), subject.copy(context));
	}
	
	public Expression getObject() {
		return object;
	}
	
	public Expression getSubject() {
		return subject;
	}
	
	@Override
	public Expression resolve() {
		if (!object.isContext())
			throw new EvaluationException("Object not found.");
		return object.getContext().getSymbol(subject.getName());
	}
	
	@Override
	public String toString() {
		return "" + object + "." + subject;
	}
	
	public String toDebugString() {
		return "{" + object.toDebugString() + "." + subject.toDebugString() + "}";
	}
	
}
