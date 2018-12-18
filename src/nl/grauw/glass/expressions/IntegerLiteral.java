package nl.grauw.glass.expressions;

public class IntegerLiteral extends Literal {
	
	public static final IntegerLiteral ZERO = new IntegerLiteral(0);
	public static final IntegerLiteral ONE = new IntegerLiteral(1);
	
	private final int value;
	
	public IntegerLiteral(int value) {
		this.value = value;
	}
	
	@Override
	public IntegerLiteral copy(Context context) {
		return this;
	}
	
	@Override
	public boolean isInteger() {
		return true;
	}
	
	@Override
	public int getInteger() {
		return value;
	}
	
	public String toString() {
		String string = Integer.toHexString(value).toUpperCase();
		return (string.charAt(0) >= 'A' && string.charAt(0) <= 'F' ? "0" : "") + string + "H";
	}
	
	public String toDebugString() {
		return toString();
	}
	
}
