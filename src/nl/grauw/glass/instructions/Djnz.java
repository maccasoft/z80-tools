package nl.grauw.glass.instructions;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Schema;

public class Djnz extends InstructionFactory {

    @Override
    public InstructionObject createObject(Scope context, Expression arguments) {
        if (Djnz_N.ARGUMENTS.check(arguments)) {
            return new Djnz_N(context, arguments);
        }
        throw new ArgumentException();
    }

    public static class Djnz_N extends InstructionObject {

        public static Schema ARGUMENTS = new Schema(Schema.DIRECT_N);

        private Expression argument;

        public Djnz_N(Scope context, Expression arguments) {
            super(context);
            this.argument = arguments;
        }

        @Override
        public int getSize() {
            return 2;
        }

        @Override
        public byte[] getBytes() {
            int offset = argument.getAddress() - (context.getAddress() + 2);
            if (offset < -128 || offset > 127) {
                throw new ArgumentException("Jump offset out of range: " + offset);
            }
            return new byte[] {
                (byte) 0x10, (byte) offset
            };
        }

    }

}
