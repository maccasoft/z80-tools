package nl.grauw.glass.expressions;

public class Identifier extends Passthrough {
	
	private final String name;
	private final Context context;
	
	public Identifier(String name, Context context) {
		this.name = name;
		this.context = context;
	}
	
	@Override
	public Identifier copy(Context context) {
		return new Identifier(name, context);
	}
	
	public String getName() {
		return name;
	}
	
	public boolean exists() {
		return FlagOrRegister.getByName(name) != null || context.hasSymbol(name);
	}
	
	@Override
	public Expression resolve() {
		Literal flagOrRegister = FlagOrRegister.getByName(name);
		return flagOrRegister != null ? flagOrRegister : context.getSymbol(name);
	}
	
	@Override
	public boolean isRegister() {
		return exists() && super.isRegister();
	}
	
	@Override
	public boolean isFlag() {
		return exists() && super.isFlag();
	}
	
	@Override
	public boolean isGroup() {
		return exists() && super.isGroup();
	}
	
	public String toString() {
		return name;
	}
	
	public String toDebugString() {
		return toString();
	}
	
}
