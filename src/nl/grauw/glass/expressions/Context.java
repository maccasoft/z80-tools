package nl.grauw.glass.expressions;

public interface Context {
	
	public Expression getSymbol(String name);
	
	public boolean hasSymbol(String name);
	
	public int getAddress();
	
}
