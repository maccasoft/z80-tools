package nl.grauw.glass.instructions;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Schema;

public class Cpi extends InstructionFactory {

    @Override
    public InstructionObject createObject(Scope context, Expression arguments) {
        if (Cpi_.ARGUMENTS.check(arguments)) {
            return new Cpi_(context);
        }
        throw new ArgumentException();
    }

    public static class Cpi_ extends InstructionObject {

        public static Schema ARGUMENTS = new Schema();

        public Cpi_(Scope context) {
            super(context);
        }

        @Override
        public int getSize() {
            return 2;
        }

        @Override
        public byte[] getBytes() {
            return new byte[] {
                (byte) 0xED, (byte) 0xA1
            };
        }

    }

}
