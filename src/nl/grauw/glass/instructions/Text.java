package nl.grauw.glass.instructions;

import java.util.List;

import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Identifier;

public class Text extends InstructionFactory {

    @Override
    public InstructionObject createObject(Scope context, Expression arguments) {
        if (arguments != null) {
            return new Text_N(context, arguments.getList());
        }
        throw new ArgumentException();
    }

    public static class Text_N extends InstructionObject {

        private List<Expression> arguments;

        public Text_N(Scope context, List<Expression> arguments) {
            super(context);
            this.arguments = arguments;
        }

        @Override
        public int getSize() {
            int count = 0;
            for (int i = 0, length = arguments.size(); i < length; i++) {
                if (arguments.get(i) instanceof Identifier) {
                    count += Integer.toString(arguments.get(i).getInteger()).length();
                }
                else {
                    count++;
                }
            }
            return count;
        }

        @Override
        public byte[] getBytes() {
            byte[] bytes = new byte[getSize()];

            int i = 0;
            for (int n = 0, length = arguments.size(); n < length; n++) {
                Expression e = arguments.get(n);
                if (e instanceof Identifier) {
                    String s = Integer.toString(e.getInteger());
                    System.arraycopy(s.getBytes(), 0, bytes, i, s.length());
                    i += s.length();
                }
                else {
                    bytes[i++] = (byte) e.getInteger();
                }
            }

            return bytes;
        }

    }

}
