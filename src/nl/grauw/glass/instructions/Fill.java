package nl.grauw.glass.instructions;

import java.util.Arrays;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Schema;

public class Fill extends InstructionFactory {

    public static Schema ARGUMENTS_N_N = new Schema(Schema.INTEGER, Schema.INTEGER);

    @Override
    public InstructionObject createObject(Scope context, Expression arguments) {
        if (ARGUMENTS_N_N.check(arguments)) {
            return new Fill_N_N(context, arguments.getElement(0), arguments.getElement(1));
        }
        throw new ArgumentException();
    }

    public class Fill_N_N extends InstructionObject {

        private final Expression size;
        private final Expression value;

        public Fill_N_N(Scope context, Expression size, Expression value) {
            super(context);
            this.size = size;
            this.value = value;
        }

        @Override
        public int getSize() {
            return size.getInteger();
        }

        @Override
        public byte[] getBytes() {
            byte[] bytes = new byte[size.getInteger()];
            Arrays.fill(bytes, (byte) value.getInteger());
            return bytes;
        }

    }

}
