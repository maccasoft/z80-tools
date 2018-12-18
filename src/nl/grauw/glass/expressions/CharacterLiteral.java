package nl.grauw.glass.expressions;

public class CharacterLiteral extends Literal {
	
	private final char character;
	
	public CharacterLiteral(char character) {
		this.character = character;
	}
	
	@Override
	public CharacterLiteral copy(Context context) {
		return this;
	}
	
	public char getCharacter() {
		return character;
	}
	
	@Override
	public boolean isInteger() {
		return true;
	}
	
	@Override
	public int getInteger() {
		return character;
	}
	
	public String toString() {
		String escaped = Character.toString(character);
		escaped = escaped.replace("\\", "\\\\");
		escaped = escaped.replace("\'", "\\\'");
		escaped = escaped.replace("\0", "\\0");
		escaped = escaped.replace("\7", "\\a");
		escaped = escaped.replace("\t", "\\t");
		escaped = escaped.replace("\n", "\\n");
		escaped = escaped.replace("\f", "\\f");
		escaped = escaped.replace("\r", "\\r");
		escaped = escaped.replace("\33", "\\e");
		return "'" + escaped + "'";
	}
	
	public String toDebugString() {
		return toString();
	}
	
}
