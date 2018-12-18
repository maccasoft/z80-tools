package nl.grauw.glass.instructions;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Schema;

public class Cpl extends InstructionFactory {

    @Override
    public InstructionObject createObject(Scope context, Expression arguments) {
        if (Cpl_.ARGUMENTS.check(arguments)) {
            return new Cpl_(context);
        }
        throw new ArgumentException();
    }

    public static class Cpl_ extends InstructionObject {

        public static Schema ARGUMENTS = new Schema();

        public Cpl_(Scope context) {
            super(context);
        }

        @Override
        public int getSize() {
            return 1;
        }

        @Override
        public byte[] getBytes() {
            return new byte[] {
                (byte) 0x2F
            };
        }

    }

}
