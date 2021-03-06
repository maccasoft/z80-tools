package nl.grauw.glass;

import java.util.HashMap;
import java.util.Map;

import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Instruction;
import nl.grauw.glass.instructions.Adc;
import nl.grauw.glass.instructions.Add;
import nl.grauw.glass.instructions.And;
import nl.grauw.glass.instructions.Bit;
import nl.grauw.glass.instructions.Call;
import nl.grauw.glass.instructions.Ccf;
import nl.grauw.glass.instructions.Cp;
import nl.grauw.glass.instructions.Cpd;
import nl.grauw.glass.instructions.Cpdr;
import nl.grauw.glass.instructions.Cpi;
import nl.grauw.glass.instructions.Cpir;
import nl.grauw.glass.instructions.Cpl;
import nl.grauw.glass.instructions.Daa;
import nl.grauw.glass.instructions.Db;
import nl.grauw.glass.instructions.Dd;
import nl.grauw.glass.instructions.Dec;
import nl.grauw.glass.instructions.Di;
import nl.grauw.glass.instructions.Djnz;
import nl.grauw.glass.instructions.Ds;
import nl.grauw.glass.instructions.Dw;
import nl.grauw.glass.instructions.Ei;
import nl.grauw.glass.instructions.Else;
import nl.grauw.glass.instructions.End;
import nl.grauw.glass.instructions.Endif;
import nl.grauw.glass.instructions.Endm;
import nl.grauw.glass.instructions.Endp;
import nl.grauw.glass.instructions.Ends;
import nl.grauw.glass.instructions.Equ;
import nl.grauw.glass.instructions.Error;
import nl.grauw.glass.instructions.Ex;
import nl.grauw.glass.instructions.Exx;
import nl.grauw.glass.instructions.Fill;
import nl.grauw.glass.instructions.Halt;
import nl.grauw.glass.instructions.Im;
import nl.grauw.glass.instructions.In;
import nl.grauw.glass.instructions.Inc;
import nl.grauw.glass.instructions.Include;
import nl.grauw.glass.instructions.Ind;
import nl.grauw.glass.instructions.Indr;
import nl.grauw.glass.instructions.Ini;
import nl.grauw.glass.instructions.Inir;
import nl.grauw.glass.instructions.Jp;
import nl.grauw.glass.instructions.Jr;
import nl.grauw.glass.instructions.Ld;
import nl.grauw.glass.instructions.Ldd;
import nl.grauw.glass.instructions.Lddr;
import nl.grauw.glass.instructions.Ldi;
import nl.grauw.glass.instructions.Ldir;
import nl.grauw.glass.instructions.Mulub;
import nl.grauw.glass.instructions.Muluw;
import nl.grauw.glass.instructions.Neg;
import nl.grauw.glass.instructions.Nop;
import nl.grauw.glass.instructions.Or;
import nl.grauw.glass.instructions.Org;
import nl.grauw.glass.instructions.Otdr;
import nl.grauw.glass.instructions.Otir;
import nl.grauw.glass.instructions.Out;
import nl.grauw.glass.instructions.Outd;
import nl.grauw.glass.instructions.Outi;
import nl.grauw.glass.instructions.Pop;
import nl.grauw.glass.instructions.Push;
import nl.grauw.glass.instructions.Res;
import nl.grauw.glass.instructions.Ret;
import nl.grauw.glass.instructions.Reti;
import nl.grauw.glass.instructions.Retn;
import nl.grauw.glass.instructions.Rl;
import nl.grauw.glass.instructions.Rla;
import nl.grauw.glass.instructions.Rlc;
import nl.grauw.glass.instructions.Rlca;
import nl.grauw.glass.instructions.Rld;
import nl.grauw.glass.instructions.Rr;
import nl.grauw.glass.instructions.Rra;
import nl.grauw.glass.instructions.Rrc;
import nl.grauw.glass.instructions.Rrca;
import nl.grauw.glass.instructions.Rrd;
import nl.grauw.glass.instructions.Rst;
import nl.grauw.glass.instructions.Sbc;
import nl.grauw.glass.instructions.Scf;
import nl.grauw.glass.instructions.Set;
import nl.grauw.glass.instructions.Sla;
import nl.grauw.glass.instructions.Sll;
import nl.grauw.glass.instructions.Sra;
import nl.grauw.glass.instructions.Srl;
import nl.grauw.glass.instructions.Sub;
import nl.grauw.glass.instructions.Text;
import nl.grauw.glass.instructions.Warning;
import nl.grauw.glass.instructions.Xor;

public class GlobalScope extends Scope {

    private final Map<String, Expression> builtInSymbols = new HashMap<>();

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
        addBuiltInSymbol("dec", new Instruction(new Dec()));
        addBuiltInSymbol("di", new Instruction(new Di()));
        addBuiltInSymbol("djnz", new Instruction(new Djnz()));
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
        addBuiltInSymbol("sll", new Instruction(new Sll()));
        addBuiltInSymbol("sra", new Instruction(new Sra()));
        addBuiltInSymbol("srl", new Instruction(new Srl()));
        addBuiltInSymbol("sub", new Instruction(new Sub()));
        addBuiltInSymbol("xor", new Instruction(new Xor()));

        addBuiltInSymbol("db", new Instruction(new Db()));
        addBuiltInSymbol("dd", new Instruction(new Dd()));
        addBuiltInSymbol("ds", new Instruction(new Ds()));
        addBuiltInSymbol("dw", new Instruction(new Dw()));
        addBuiltInSymbol("byte", new Instruction(new Db()));
        addBuiltInSymbol("word", new Instruction(new Dw()));
        addBuiltInSymbol("text", new Instruction(new Text()));

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
        addBuiltInSymbol("fill", new Instruction(new Fill()));

        addBuiltInSymbol(".db", new Instruction(new Db()));
        addBuiltInSymbol(".dd", new Instruction(new Dd()));
        addBuiltInSymbol(".ds", new Instruction(new Ds()));
        addBuiltInSymbol(".dw", new Instruction(new Dw()));
        addBuiltInSymbol(".byte", new Instruction(new Db()));
        addBuiltInSymbol(".word", new Instruction(new Dw()));
        addBuiltInSymbol(".text", new Instruction(new Text()));

        addBuiltInSymbol(".include", new Instruction(new Include()));
        addBuiltInSymbol(".equ", new Instruction(new Equ()));
        addBuiltInSymbol(".org", new Instruction(new Org()));
        addBuiltInSymbol(".endm", new Instruction(new Endm()));
        addBuiltInSymbol(".endp", new Instruction(new Endp()));
        addBuiltInSymbol(".ends", new Instruction(new Ends()));
        addBuiltInSymbol(".end", new Instruction(new End()));
        addBuiltInSymbol(".endif", new Instruction(new Endif()));
        addBuiltInSymbol(".else", new Instruction(new Else()));
        addBuiltInSymbol(".error", new Instruction(new Error()));
        addBuiltInSymbol(".warning", new Instruction(new Warning()));
        addBuiltInSymbol(".fill", new Instruction(new Fill()));
    }

    public void addBuiltInSymbol(String name, Expression value) {
        if (name == null || value == null) {
            throw new AssemblyException("Symbol name and value must not be null.");
        }
        if (builtInSymbols.containsKey(name)) {
            throw new AssemblyException("Can not redefine symbol: " + name);
        }
        builtInSymbols.put(name, value);
        builtInSymbols.put(name.toUpperCase(), value);
    }

    @Override
    protected Expression getLocalSymbol(String name) {
        Expression value = super.getLocalSymbol(name);
        if (value == null) {
            value = builtInSymbols.get(name);
        }
        return value;
    }

}
