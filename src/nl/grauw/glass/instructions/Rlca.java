package nl.grauw.glass.instructions;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Schema;

public class Rlca extends InstructionFactory {

    @Override
    public InstructionObject createObject(Scope context, Expression arguments) {
        if (Rlca_.ARGUMENTS.check(arguments)) {
            return new Rlca_(context);
        }
        throw new ArgumentException();
    }

    public static class Rlca_ extends InstructionObject {

        public static Schema ARGUMENTS = new Schema();

        public Rlca_(Scope context) {
            super(context);
        }

        @Override
        public int getSize() {
            return 1;
        }

        @Override
        public byte[] getBytes() {
            return new byte[] {
                (byte) 0x07
            };
        }

    }

}
