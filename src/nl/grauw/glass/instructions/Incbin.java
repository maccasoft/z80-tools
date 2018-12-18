package nl.grauw.glass.instructions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.grauw.glass.AssemblyException;
import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.IntegerLiteral;
import nl.grauw.glass.expressions.Schema;

public class Incbin extends InstructionFactory {

    private final List<File> basePaths = new ArrayList<File>();

    public Incbin(File basePath, List<File> includePaths) {
        this.basePaths.add(basePath);
        this.basePaths.addAll(includePaths);
    }

    @Override
    public InstructionObject createObject(Scope context, Expression arguments) {
        if (Incbin_.ARGUMENTS_S.check(arguments)) {
            return new Incbin_(context, arguments.getElement(0),
                IntegerLiteral.ZERO, null, basePaths);
        }
        if (Incbin_.ARGUMENTS_S_N.check(arguments)) {
            return new Incbin_(context, arguments.getElement(0),
                arguments.getElement(1), null, basePaths);
        }
        if (Incbin_.ARGUMENTS_S_N_N.check(arguments)) {
            return new Incbin_(context, arguments.getElement(0),
                arguments.getElement(1), arguments.getElement(2), basePaths);
        }
        throw new ArgumentException();
    }

    public static class Incbin_ extends InstructionObject {

        public static Schema ARGUMENTS_S = new Schema(Schema.STRING);
        public static Schema ARGUMENTS_S_N = new Schema(Schema.STRING, Schema.INTEGER);
        public static Schema ARGUMENTS_S_N_N = new Schema(Schema.STRING, Schema.INTEGER, Schema.INTEGER);

        private final Expression path;
        private final Expression start;
        private final Expression length;
        private final List<File> basePaths;
        private byte[] bytes;

        public Incbin_(Scope context, Expression path, Expression start, Expression length, List<File> basePaths) {
            super(context);
            this.path = path;
            this.start = start;
            this.length = length;
            this.basePaths = basePaths;
        }

        @Override
        public int getSize() {
            return length != null ? length.getInteger() : getBytes().length;
        }

        @Override
        public byte[] getBytes() {
            if (bytes == null) {
                byte[] allBytes = loadFile();

                int from = this.start.getInteger();
                int to = this.length != null ? from + this.length.getInteger() : allBytes.length;
                if (from < 0 || from > allBytes.length) {
                    throw new AssemblyException("Incbin start exceeds file size.");
                }
                if (to < from || to > allBytes.length) {
                    throw new AssemblyException("Incbin length exceeds file size.");
                }

                bytes = Arrays.copyOfRange(allBytes, from, to);
            }
            return bytes;
        }

        private byte[] loadFile() {
            for (File basePath : basePaths) {
                File fullPath = new File(basePath.getParent(), path.getString());
                if (fullPath.exists()) {
                    try {
                        return Files.readAllBytes(fullPath.toPath());
                    } catch (IOException e) {
                        throw new AssemblyException(e);
                    }
                }
            }
            throw new AssemblyException("Incbin file not found: " + path.getString());
        }

    }

}
