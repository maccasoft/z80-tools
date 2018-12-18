package nl.grauw.glass.expressions;

import nl.grauw.glass.instructions.InstructionFactory;

public class Instruction extends Expression {
	
	private final InstructionFactory instruction;
	private final Context context;
	
	public Instruction(InstructionFactory instruction) {
		this(instruction, null);
	}
	
	public Instruction(InstructionFactory instruction, Context context) {
		this.instruction = instruction;
		this.context = context;
	}
	
	@Override
	public Instruction copy(Context context) {
		return new Instruction(instruction, context);
	}
	
	@Override
	public boolean isInstruction() {
		return true;
	}
	
	@Override
	public InstructionFactory getInstruction() {
		return instruction;
	}
	
	@Override
	public boolean isContext() {
		return context != null;
	}
	
	@Override
	public Context getContext() {
		if (!isContext())
			super.getContext();
		return context;
	}
	
	public String toString() {
		return "instruction";
	}
	
	public String toDebugString() {
		return toString();
	}
	
}
