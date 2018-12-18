package nl.grauw.glass.instructions;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Schema;

public class Rst extends InstructionFactory {

    @Override
    public InstructionObject createObject(Scope context, Expression arguments) {
        if (Rst_N.ARGUMENTS.check(arguments)) {
            return new Rst_N(context, arguments.getElement(0));
        }
        throw new ArgumentException();
    }

    public static class Rst_N extends InstructionObject {

        public static Schema ARGUMENTS = new Schema(Schema.DIRECT_N);

        private Expression argument;

        public Rst_N(Scope context, Expression argument) {
            super(context);
            this.argument = argument;
        }

        @Override
        public int getSize() {
            return 1;
        }

        @Override
        public byte[] getBytes() {
            int value = argument.getInteger();
            if (value < 0 || value > 0x38 || (value & 7) != 0) {
                throw new ArgumentException();
            }
            return new byte[] {
                (byte) (0xC7 + value)
            };
        }

    }

}
