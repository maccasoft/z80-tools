package nl.grauw.glass.instructions;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Schema;

public class Muluw extends InstructionFactory {

    @Override
    public InstructionObject createObject(Scope context, Expression arguments) {
        if (Muluw_RR_RR.ARGUMENTS.check(arguments)) {
            return new Muluw_RR_RR(context, arguments.getElement(1));
        }
        throw new ArgumentException();
    }

    public static class Muluw_RR_RR extends InstructionObject {

        public static Schema ARGUMENTS = new Schema(Schema.DIRECT_HL, Schema.DIRECT_RR);

        private Expression argument;

        public Muluw_RR_RR(Scope context, Expression argument) {
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
                (byte) 0xED, (byte) (0xC3 | argument.getRegister().get16BitCode() << 4)
            };
        }

    }

}
