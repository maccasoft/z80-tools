package nl.grauw.glass;

import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Instruction;
import nl.grauw.glass.instructions.*;
import nl.grauw.glass.instructions.Error;

public class GlobalScope extends Scope {
	
	public GlobalScope() {
		super();
		setAddress(0);
		
		addBuiltInSymbol("adc", new Instruction(new Adc()));
		addBuiltInSymbol("add", new Instruction(new Add()));
		addBuiltInSymbol("and", new Instruction(new And()));
		addBuiltInSymbol("bit", new Instruction(new Bit()));
		addBuiltInSymbol("call", new Instruction(new Call()));
		addBuiltInSymbol("ccf", new Instruction(new Ccf()));
		addBuiltInSymbol("cp", new Instruction(new Cp()));
		addBuiltInSymbol("cpd", new Instruction(new Cpd()));
		addBuiltInSymbol("cpdr", new Instruction(new Cpdr()));
		addBuiltInSymbol("cpi", new Instruction(new Cpi()));
		addBuiltInSymbol("cpir", new Instruction(new Cpir()));
		addBuiltInSymbol("cpl", new Instruction(new Cpl()));
		addBuiltInSymbol("daa", new Instruction(new Daa()));
		addBuiltInSymbol("db", new Instruction(new Db()));
		addBuiltInSymbol("dd", new Instruction(new Dd()));
		addBuiltInSymbol("dec", new Instruction(new Dec()));
		addBuiltInSymbol("di", new Instruction(new Di()));
		addBuiltInSymbol("djnz", new Instruction(new Djnz()));
		addBuiltInSymbol("ds", new Instruction(new Ds()));
		addBuiltInSymbol("dw", new Instruction(new Dw()));
		addBuiltInSymbol("ei", new Instruction(new Ei()));
		addBuiltInSymbol("ex", new Instruction(new Ex()));
		addBuiltInSymbol("exx", new Instruction(new Exx()));
		addBuiltInSymbol("halt", new Instruction(new Halt()));
		addBuiltInSymbol("im", new Instruction(new Im()));
		addBuiltInSymbol("in", new Instruction(new In()));
		addBuiltInSymbol("inc", new Instruction(new Inc()));
		addBuiltInSymbol("ind", new Instruction(new Ind()));
		addBuiltInSymbol("indr", new Instruction(new Indr()));
		addBuiltInSymbol("ini", new Instruction(new Ini()));
		addBuiltInSymbol("inir", new Instruction(new Inir()));
		addBuiltInSymbol("jp", new Instruction(new Jp()));
		addBuiltInSymbol("jr", new Instruction(new Jr()));
		addBuiltInSymbol("ld", new Instruction(new Ld()));
		addBuiltInSymbol("ldd", new Instruction(new Ldd()));
		addBuiltInSymbol("lddr", new Instruction(new Lddr()));
		addBuiltInSymbol("ldi", new Instruction(new Ldi()));
		addBuiltInSymbol("ldir", new Instruction(new Ldir()));
		addBuiltInSymbol("mulub", new Instruction(new Mulub()));
		addBuiltInSymbol("muluw", new Instruction(new Muluw()));
		addBuiltInSymbol("neg", new Instruction(new Neg()));
		addBuiltInSymbol("nop", new Instruction(new Nop()));
		addBuiltInSymbol("or", new Instruction(new Or()));
		addBuiltInSymbol("otdr", new Instruction(new Otdr()));
		addBuiltInSymbol("otir", new Instruction(new Otir()));
		addBuiltInSymbol("out", new Instruction(new Out()));
		addBuiltInSymbol("outi", new Instruction(new Outi()));
		addBuiltInSymbol("outd", new Instruction(new Outd()));
		addBuiltInSymbol("pop", new Instruction(new Pop()));
		addBuiltInSymbol("push", new Instruction(new Push()));
		addBuiltInSymbol("res", new Instruction(new Res()));
		addBuiltInSymbol("ret", new Instruction(new Ret()));
		addBuiltInSymbol("reti", new Instruction(new Reti()));
		addBuiltInSymbol("retn", new Instruction(new Retn()));
		addBuiltInSymbol("rl", new Instruction(new Rl()));
		addBuiltInSymbol("rla", new Instruction(new Rla()));
		addBuiltInSymbol("rlc", new Instruction(new Rlc()));
		addBuiltInSymbol("rlca", new Instruction(new Rlca()));
		addBuiltInSymbol("rld", new Instruction(new Rld()));
		addBuiltInSymbol("rr", new Instruction(new Rr()));
		addBuiltInSymbol("rra", new Instruction(new Rra()));
		addBuiltInSymbol("rrc", new Instruction(new Rrc()));
		addBuiltInSymbol("rrca", new Instruction(new Rrca()));
		addBuiltInSymbol("rrd", new Instruction(new Rrd()));
		addBuiltInSymbol("rst", new Instruction(new Rst()));
		addBuiltInSymbol("sbc", new Instruction(new Sbc()));
		addBuiltInSymbol("scf", new Instruction(new Scf()));
		addBuiltInSymbol("set", new Instruction(new Set()));
		addBuiltInSymbol("sla", new Instruction(new Sla()));
		addBuiltInSymbol("sra", new Instruction(new Sra()));
		addBuiltInSymbol("srl", new Instruction(new Srl()));
		addBuiltInSymbol("sub", new Instruction(new Sub()));
		addBuiltInSymbol("xor", new Instruction(new Xor()));
		
		addBuiltInSymbol("include", new Instruction(new Include()));
		addBuiltInSymbol("equ", new Instruction(new Equ()));
		addBuiltInSymbol("org", new Instruction(new Org()));
		addBuiltInSymbol("endm", new Instruction(new Endm()));
		addBuiltInSymbol("endp", new Instruction(new Endp()));
		addBuiltInSymbol("ends", new Instruction(new Ends()));
		addBuiltInSymbol("end", new Instruction(new End()));
		addBuiltInSymbol("endif", new Instruction(new Endif()));
		addBuiltInSymbol("else", new Instruction(new Else()));
		addBuiltInSymbol("error", new Instruction(new Error()));
		addBuiltInSymbol("warning", new Instruction(new Warning()));
	}
	
	private void addBuiltInSymbol(String symbol, Expression value) {
		addSymbol(symbol, value);
		addSymbol(symbol.toUpperCase(), value);
	}
	
}
