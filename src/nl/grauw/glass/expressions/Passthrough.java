package nl.grauw.glass.expressions;

import nl.grauw.glass.instructions.InstructionFactory;

public abstract class Passthrough extends Expression {
	
	@Override
	public boolean isInteger() {
		return resolve().isInteger();
	}
	
	@Override
	public int getInteger() {
		return resolve().getInteger();
	}
	
	@Override
	public boolean isString() {
		return resolve().isString();
	}
	
	@Override
	public String getString() {
		return resolve().getString();
	}
	
	@Override
	public boolean isRegister() {
		return resolve().isRegister();
	}
	
	@Override
	public Register getRegister() {
		return resolve().getRegister();
	}
	
	@Override
	public boolean isFlag() {
		return resolve().isFlag();
	}
	
	@Override
	public Flag getFlag() {
		return resolve().getFlag();
	}
	
	@Override
	public boolean isGroup() {
		return resolve().isGroup();
	}
	
	@Override
	public boolean isInstruction() {
		return resolve().isInstruction();
	}
	
	@Override
	public InstructionFactory getInstruction() {
		return resolve().getInstruction();
	}
	
	@Override
	public boolean isContext() {
		return resolve().isContext();
	}
	
	@Override
	public Context getContext() {
		return resolve().getContext();
	}
	
	@Override
	public boolean isSectionContext() {
		return resolve().isSectionContext();
	}
	
	@Override
	public SectionContext getSectionContext() {
		return resolve().getSectionContext();
	}
	
}
