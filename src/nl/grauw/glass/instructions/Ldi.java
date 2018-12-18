package nl.grauw.glass.instructions;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Schema;

public class Ldi extends InstructionFactory {

    @Override
    public InstructionObject createObject(Scope context, Expression arguments) {
        if (Ldi_.ARGUMENTS.check(arguments)) {
            return new Ldi_(context);
        }
        throw new ArgumentException();
    }

    public static class Ldi_ extends InstructionObject {

        public static Schema ARGUMENTS = new Schema();

        public Ldi_(Scope context) {
            super(context);
        }

        @Override
        public int getSize() {
            return 2;
        }

        @Override
        public byte[] getBytes() {
            return new byte[] {
                (byte) 0xED, (byte) 0xA0
            };
        }

    }

}
