package nl.grauw.glass.instructions;

import java.io.IOException;
import java.io.OutputStream;

import nl.grauw.glass.AssemblyException;
import nl.grauw.glass.Scope;
import nl.grauw.glass.Source;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.Schema;

public class If extends InstructionFactory {

    private static Schema ARGUMENTS = new Schema(Schema.INTEGER);

    private final Source thenSource;
    private final Source elseSource;

    public If(Source thenSource, Source elseSource) {
        this.thenSource = thenSource;
        this.elseSource = elseSource;
    }

    @Override
    public InstructionObject createObject(Scope context, Expression arguments) {
        if (ARGUMENTS.check(arguments)) {
            return new IfObject(context, arguments);
        }
        throw new ArgumentException();
    }

    public class IfObject extends InstructionObject {

        private final Expression argument;

        public IfObject(Scope context, Expression argument) {
            super(context);
            this.argument = argument;
        }

        @Override
        public int resolve(int address) {
            context.setAddress(address);
            if (argument.getInteger() != 0) {
                thenSource.register();
                thenSource.expand();
                return thenSource.resolve(address);
            }
            else if (elseSource != null) {
                elseSource.register();
                elseSource.expand();
                return elseSource.resolve(address);
            }
            else {
                return address;
            }
        }

        @Override
        public int getSize() {
            throw new AssemblyException("Not implemented.");
        }

        @Override
        public void generateObjectCode(OutputStream output) throws IOException {
            if (argument.getInteger() != 0) {
                thenSource.generateObjectCode(output);
            }
            else if (elseSource != null) {
                elseSource.generateObjectCode(output);
            }
        }

        @Override
        public byte[] getBytes() {
            throw new AssemblyException("Not implemented.");
        }

    }

    public Source getThenSource() {
        return thenSource;
    }

    public Source getElseSource() {
        return elseSource;
    }

}
