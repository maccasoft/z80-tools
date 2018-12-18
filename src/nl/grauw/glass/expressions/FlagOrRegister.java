package nl.grauw.glass.expressions;

public class FlagOrRegister extends Literal {
	
	public static FlagOrRegister C = new FlagOrRegister(Flag.C, Register.C);
	
	private final Flag flag;
	private final Register register;
	
	public FlagOrRegister(Flag flag, Register register) {
		this.flag = flag;
		this.register = register;
	}
	
	@Override
	public FlagOrRegister copy(Context context) {
		return this;
	}
	
	@Override
	public boolean isFlag() {
		return true;
	}
	
	@Override
	public Flag getFlag() {
		return flag;
	}
	
	@Override
	public boolean isRegister() {
		return true;
	}
	
	@Override
	public Register getRegister() {
		return register;
	}
	
	@Override
	public String toString() {
		return flag.toString();
	}
	
	@Override
	public String toDebugString() {
		return flag.toString();
	}
	
	public static Literal getByName(String name) {
		Flag flag = Flag.getByName(name);
		Register register = Register.getByName(name);
		if (flag != null && register == null)
			return flag;
		if (flag == null && register != null)
			return register;
		if (flag != null && register != null)
			return new FlagOrRegister(flag, register);
		return null;
	}
	
}
