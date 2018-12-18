package nl.grauw.glass.instructions;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Register;
import nl.grauw.glass.expressions.Schema;

public class Adc extends InstructionFactory {

    @Override
    public InstructionObject createObject(Scope context, Expression arguments) {
        if (Adc_A_R.ARGUMENTS.check(arguments)) {
            return new Adc_A_R(context, arguments.getElement(1));
        }
        if (Adc_A_N.ARGUMENTS.check(arguments)) {
            return new Adc_A_N(context, arguments.getElement(1));
        }
        if (Adc_HL_RR.ARGUMENTS.check(arguments)) {
            return new Adc_HL_RR(context, arguments.getElement(1));
        }
        throw new ArgumentException();
    }

    public static class Adc_A_R extends InstructionObject {

        public static Schema ARGUMENTS = new Schema(Schema.DIRECT_A, Schema.DIRECT_R_INDIRECT_HL_IX_IY);

        private Expression argument;

        public Adc_A_R(Scope context, Expression arguments) {
            super(context);
            this.argument = arguments;
        }

        @Override
        public int getSize() {
            return indexifyIndirect(argument.getRegister(), 1);
        }

        @Override
        public byte[] getBytes() {
            Register register = argument.getRegister();
            return indexifyIndirect(register, (byte) (0x88 | register.get8BitCode()));
        }

    }

    public static class Adc_A_N extends InstructionObject {

        public static Schema ARGUMENTS = new Schema(Schema.DIRECT_A, Schema.DIRECT_N);

        private Expression argument;

        public Adc_A_N(Scope context, Expression arguments) {
            super(context);
            this.argument = arguments;
        }

        @Override
        public int getSize() {
            return 2;
        }

        @Override
        public byte[] getBytes() {
            return new byte[] {
                (byte) 0xCE, (byte) argument.getInteger()
            };
        }

    }

    public static class Adc_HL_RR extends InstructionObject {

        public static Schema ARGUMENTS = new Schema(Schema.DIRECT_HL, Schema.DIRECT_RR);

        private Expression argument;

        public Adc_HL_RR(Scope context, Expression argument) {
            super(context);
            this.argument = argument;
        }

        @Override
        public int getSize() {
            return 2;
        }

        @Override
        public byte[] getBytes() {
            return new byte[] {
                (byte) 0xED, (byte) (0x4A | argument.getRegister().get16BitCode() << 4)
            };
        }

    }

}
