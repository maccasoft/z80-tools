package nl.grauw.glass.expressions;

public class Index extends Passthrough {
	
	private final Expression sequence;
	private final Expression index;
	
	public Index(Expression sequence, Expression index) {
		this.sequence = sequence;
		this.index = index;
	}
	
	@Override
	public Index copy(Context context) {
		return new Index(sequence.copy(context), index.copy(context));
	}
	
	public Expression getSequence() {
		return sequence;
	}
	
	public Expression getIndex() {
		return index;
	}
	
	@Override
	public Expression resolve() {
		Expression element = sequence.resolve().getElement(index.getInteger());
		if (element == null)
			throw new EvaluationException("Index out of bounds.");
		return element;
	}
	
	public String toString() {
		return "" + sequence + "[" + index + "]";
	}
	
	public String toDebugString() {
		return "{" + sequence.toDebugString() + "[" + index.toDebugString() + "]}";
	}
	
}
