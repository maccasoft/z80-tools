package nl.grauw.glass.instructions;

import java.io.IOException;
import java.io.OutputStream;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Register;

public abstract class InstructionObject {
	
	protected final Scope context;
	
	public InstructionObject(Scope context) {
		this.context = context;
	}
	
	public int resolve(int address) {
		context.setAddress(address);
		return address + getSize();
	}
	
	public abstract int getSize();
	
	public void generateObjectCode(OutputStream output) throws IOException {
		byte[] object = getBytes();
		output.write(object, 0, object.length);
	}
	
	public abstract byte[] getBytes();
	
	public int indexifyDirect(Register register, int size) {
		return register.isIndex() ? size + 1 : size;
	}
	
	public int indexifyIndirect(Register register, int size) {
		return register.isIndex() ? register.isPair() ? size + 2 : size + 1 : size;
	}
	
	/**
	 * Inserts index register prefix in the object code if needed.
	 */
	public byte[] indexifyDirect(Register register, byte byte1) {
		if (!register.isIndex())
			return new byte[] { byte1 };
		if (register.isPair() && register.getIndexOffset().getInteger() != 0)
			throw new ArgumentException("Can not have index offset for direct addressing.");
		return new byte[] { register.getIndexCode(), byte1 };
	}
	
	public byte[] indexifyDirect(Register register, byte byte1, byte byte2) {
		if (!register.isIndex())
			return new byte[] { byte1, byte2 };
		if (register.isPair() && register.getIndexOffset().getInteger() != 0)
			throw new ArgumentException("Can not have index offset for direct addressing.");
		return new byte[] { register.getIndexCode(), byte1, byte2 };
	}
	
	public byte[] indexifyDirect(Register register, byte byte1, byte byte2, byte byte3) {
		if (!register.isIndex())
			return new byte[] { byte1, byte2, byte3 };
		if (register.isPair() && register.getIndexOffset().getInteger() != 0)
			throw new ArgumentException("Can not have index offset for direct addressing.");
		return new byte[] { register.getIndexCode(), byte1, byte2, byte3 };
	}
	
	/**
	 * Inserts index register prefix + offset in the object code if needed.
	 */
	public byte[] indexifyIndirect(Register register, byte byte1) {
		if (!register.isIndex())
			return new byte[] { byte1 };
		if (!register.isPair())
			return indexifyDirect(register, byte1);
		int offset = register.getIndexOffset().getInteger();
		if (offset < -128 || offset > 127)
			throw new ArgumentException("Index offset out of range: " + offset);
		return new byte[] { register.getIndexCode(), byte1, (byte)offset };
	}
	
	public byte[] indexifyIndirect(Register register, byte byte1, byte byte2) {
		if (!register.isIndex())
			return new byte[] { byte1, byte2 };
		if (!register.isPair())
			return indexifyDirect(register, byte1, byte2);
		int offset = register.getIndexOffset().getInteger();
		if (offset < -128 || offset > 127)
			throw new ArgumentException("Index offset out of range: " + offset);
		return new byte[] { register.getIndexCode(), byte1, (byte)offset, byte2 };
	}
	
	public byte[] indexifyOnlyIndirect(Register register, byte byte1, byte byte2) {
		if (!register.isIndex())
			return new byte[] { byte1, byte2 };
		if (!register.isPair())
			throw new ArgumentException();
		int offset = register.getIndexOffset().getInteger();
		if (offset < -128 || offset > 127)
			throw new ArgumentException("Index offset out of range: " + offset);
		return new byte[] { register.getIndexCode(), byte1, (byte)offset, byte2 };
	}
	
}
