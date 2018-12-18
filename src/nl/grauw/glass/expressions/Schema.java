package nl.grauw.glass.expressions;

public class Schema implements SchemaType {
	
	private SchemaType[] types;
	
	public Schema(SchemaType... types) {
		this.types = types;
	}
	
	public boolean check(Expression arguments) {
		for (int i = 0; i < types.length; i++) {
			if (arguments == null || !types[i].check(arguments.getElement()))
				return false;
			arguments = arguments.getNext();
		}
		return arguments == null;
	}
	
	public static SchemaType ANY = new IsAny();
	public static SchemaType DIRECT = new IsDirect();
	public static SchemaType INDIRECT = new IsIndirect();
	public static SchemaType INTEGER = new IsInteger();
	public static SchemaType STRING = new IsString();
	public static SchemaType IDENTIFIER = new IsIdentifier();
	public static SchemaType DIRECT_N = new And(DIRECT, INTEGER);
	public static SchemaType DIRECT_R = new And(DIRECT, new IsRegister8Bit());
	public static SchemaType DIRECT_A = new And(DIRECT, new IsRegister(Register.A));
	public static SchemaType DIRECT_IR = new And(DIRECT, new IsRegister(Register.I, Register.R));
	public static SchemaType DIRECT_RR = new And(DIRECT, new IsRegister(Register.BC, Register.DE, Register.HL, Register.SP));
	public static SchemaType DIRECT_RR_INDEX = new And(DIRECT, new IsRegister(Register.BC, Register.DE, Register.HL, Register.SP, Register.IX, Register.IY));
	public static SchemaType DIRECT_RR_AF_INDEX = new And(DIRECT, new IsRegister(Register.BC, Register.DE, Register.HL, Register.AF, Register.IX, Register.IY));
	public static SchemaType DIRECT_DE = new And(DIRECT, new IsRegister(Register.DE));
	public static SchemaType DIRECT_HL = new And(DIRECT, new IsRegister(Register.HL));
	public static SchemaType DIRECT_HL_IX_IY = new And(DIRECT, new IsRegister(Register.HL, Register.IX, Register.IY));
	public static SchemaType DIRECT_SP = new And(DIRECT, new IsRegister(Register.SP));
	public static SchemaType DIRECT_AF = new And(DIRECT, new IsRegister(Register.AF));
	public static SchemaType DIRECT_AF_ = new And(DIRECT, new IsRegister(Register.AF_));
	public static SchemaType INDIRECT_N = new And(INDIRECT, INTEGER);
	public static SchemaType INDIRECT_C = new And(INDIRECT, new IsRegister(Register.C));
	public static SchemaType INDIRECT_BC_DE = new And(INDIRECT, new IsRegister(Register.BC, Register.DE));
	public static SchemaType INDIRECT_HL_IX_IY = new And(INDIRECT, new IsRegister(Register.HL, Register.IX, Register.IY));
	public static SchemaType INDIRECT_SP = new And(INDIRECT, new IsRegister(Register.SP));
	public static SchemaType DIRECT_R_INDIRECT_HL_IX_IY = new IsDirectRIndirectHLIXIY();
	
	public static class IsAny implements SchemaType {
		public boolean check(Expression argument) {
			return true;
		}
	}
	
	public static class And implements SchemaType {
		private SchemaType[] types;
		public And(SchemaType... types) {
			this.types = types;
		}
		public boolean check(Expression argument) {
			for (SchemaType type : types)
				if (!type.check(argument))
					return false;
			return true;
		}
	}
	
	public static class IsDirect implements SchemaType {
		public boolean check(Expression argument) {
			return !argument.isGroup();
		}
	}
	
	public static class IsIndirect implements SchemaType {
		public boolean check(Expression argument) {
			return argument.isGroup();
		}
	}
	
	public static class IsAnnotation implements SchemaType {
		private final SchemaType rhsType;
		public IsAnnotation(SchemaType rhsType) {
			this.rhsType = rhsType;
		}
		public boolean check(Expression argument) {
			return rhsType.check(argument.getAnnotee());
		}
	}
	
	public static class IsInteger implements SchemaType {
		public boolean check(Expression argument) {
			return argument.isInteger();
		}
	}
	
	public static class IsString implements SchemaType {
		public boolean check(Expression argument) {
			return argument.isString();
		}
	}
	
	public static class IsIdentifier implements SchemaType {
		public boolean check(Expression argument) {
			return argument instanceof Identifier;
		}
	}
	
	public static class IsRegister implements SchemaType {
		private Register[] registers;
		public IsRegister(Register... registers) {
			this.registers = registers;
		}
		public boolean check(Expression argument) {
			if (argument.isRegister()) {
				Register register = argument.getRegister();
				for (Register expected : registers)
					if (register == expected)
						return true;
			}
			return false;
		}
	}
	
	public static class IsRegister8Bit implements SchemaType {
		public boolean check(Expression argument) {
			if (argument.isRegister()) {
				Register register = argument.getRegister();
				return !register.isPair() && register != Register.I && register != Register.R;
			}
			return false;
		}
	}
	
	public static class IsDirectRIndirectHLIXIY implements SchemaType {
		public boolean check(Expression argument) {
			if (argument.isRegister()) {
				Register register = argument.getRegister();
				return DIRECT.check(argument) && !register.isPair() && register != Register.I && register != Register.R ||
						INDIRECT.check(argument) && (register == Register.HL || register.isIndex());
			}
			return false;
		}
	}
	
	public static class IsFlag implements SchemaType {
		public boolean check(Expression argument) {
			return argument.isFlag();
		}
	}
	
	public static class IsFlagZC implements SchemaType {
		public boolean check(Expression argument) {
			return argument.isFlag() && argument.getFlag().getCode() < 4;
		}
	}
	
}
