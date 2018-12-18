package nl.grauw.glass.expressions;

import nl.grauw.glass.AssemblyException;

public class Not extends UnaryOperator {
	
	public Not(Expression term) {
		super(term);
	}
	
	@Override
	public Not copy(Context context) {
		return new Not(term.copy(context));
	}
	
	@Override
	public int getInteger() {
		return term.getInteger() == 0 ? -1 : 0;
	}
	
	@Override
	public boolean isFlag() {
		return term.isFlag();
	}
	
	@Override
	public Flag getFlag() {
		Flag flag = term.getFlag();
		if (flag == Flag.NZ)
			return Flag.Z;
		if (flag == Flag.Z)
			return Flag.NZ;
		if (flag == Flag.NC)
			return Flag.C;
		if (flag == Flag.C)
			return Flag.NC;
		if (flag == Flag.PO)
			return Flag.PE;
		if (flag == Flag.PE)
			return Flag.PO;
		if (flag == Flag.P)
			return Flag.M;
		if (flag == Flag.M)
			return Flag.P;
		throw new AssemblyException("Unrecognised flag.");
	}
	
	@Override
	public String getLexeme() {
		return "!";
	}
	
}
