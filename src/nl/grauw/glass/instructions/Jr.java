package nl.grauw.glass.instructions;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Schema;

public class Jr extends InstructionFactory {

    @Override
    public InstructionObject createObject(Scope context, Expression arguments) {
        if (Jr_F_N.ARGUMENTS.check(arguments)) {
            return new Jr_F_N(context, arguments.getElement(0), arguments.getElement(1));
        }
        if (Jr_N.ARGUMENTS.check(arguments)) {
            return new Jr_N(context, arguments.getElement(0));
        }
        throw new ArgumentException();
    }

    public static class Jr_N extends InstructionObject {

        public static Schema ARGUMENTS = new Schema(Schema.DIRECT_N);

        private Expression argument;

        public Jr_N(Scope context, Expression argument) {
            super(context);
            this.argument = argument;
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
                (byte) 0x18, (byte) offset
            };
        }

    }

    public static class Jr_F_N extends InstructionObject {

        public static Schema ARGUMENTS = new Schema(new Schema.IsFlagZC(), Schema.DIRECT_N);

        private final Scope context;
        private Expression argument1;
        private Expression argument2;

        public Jr_F_N(Scope context, Expression argument1, Expression argument2) {
            super(context);
            this.context = context;
            this.argument1 = argument1;
            this.argument2 = argument2;
        }

        @Override
        public int getSize() {
            return 2;
        }

        @Override
        public byte[] getBytes() {
            int offset = argument2.getAddress() - (context.getAddress() + 2);
            if (offset < -128 || offset > 127) {
                throw new ArgumentException("Jump offset out of range: " + offset);
            }
            return new byte[] {
                (byte) (0x20 | argument1.getFlag().getCode() << 3), (byte) offset
            };
        }

    }

}
