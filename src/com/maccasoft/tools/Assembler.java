package com.maccasoft.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nl.grauw.glass.AssemblyException;
import nl.grauw.glass.Line;
import nl.grauw.glass.Scope;
import nl.grauw.glass.Source;
import nl.grauw.glass.SourceBuilder;
import nl.grauw.glass.directives.If;
import nl.grauw.glass.directives.Section;

public class Assembler {

    public static final int RAM_SIZE = 65536;

    private static Assembler instance;

    Source source;

    Map<Integer, Byte> ram;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java -jar glass.jar [OPTION] SOURCE [OBJECT]");
            System.exit(1);
        }

        File sourcePath = null;
        File objectPath = null;
        List<File> includePaths = new ArrayList<File>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-I") && i + 1 < args.length) {
                includePaths.add(new File(args[++i]));
            }
            else if (sourcePath == null) {
                sourcePath = new File(args[i]);
            }
            else if (objectPath == null) {
                objectPath = new File(args[i]);
            }
            else {
                throw new AssemblyException("Too many arguments.");
            }
        }

        try {
            instance = new Assembler();
            instance.assemble(sourcePath, includePaths, objectPath);
        } catch (AssemblyException ex) {
            StringBuilder sb = new StringBuilder();

            Iterator<AssemblyException.Context> iter = ex.contexts.iterator();
            if (iter.hasNext()) {
                AssemblyException.Context context = iter.next();
                sb.append(context.file.getName());
                sb.append(":");
                sb.append(context.line + 1);
                if (context.column != -1) {
                    sb.append(":");
                    sb.append(context.column);
                }
                sb.append(": error: ");
                sb.append(ex.getPlainMessage());
            }

            System.out.println();
            System.err.println(sb.toString());
            System.exit(1);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
    }

    public Assembler() {
        ram = new HashMap<Integer, Byte>(RAM_SIZE);
    }

    void assemble(File sourcePath, List<File> includePaths, File objectPath) {
        System.out.print("Compiling " + sourcePath.getName() + "...");
        System.out.flush();

        source = new SourceBuilder(includePaths) {

            @Override
            public Source parse(File sourceFile) {
                if (!sourceFile.equals(sourcePath)) {
                    System.out.print("\r\nCompiling " + sourceFile.getName() + "...");
                    System.out.flush();
                }
                return super.parse(sourceFile);
            }

        }.parse(sourcePath);

        source.register();
        source.expand();
        source.resolve();

        build(source);
        writeObject(objectPath);

        int lower = Integer.MAX_VALUE;
        int higher = Integer.MIN_VALUE;
        for (Line line : source.getLines()) {
            try {
                if (line.getSize() != 0) {
                    lower = Math.min(lower, line.getScope().getAddress());
                    higher = Math.max(higher, line.getScope().getAddress() + line.getSize() - 1);
                }
            } catch (Exception e) {
                // Ignore, not important
            }
        }
        System.out.println();
        System.out.println(String.format("Compiled %d lines from %04XH to %04XH (%d bytes)", source.getLines().size(), lower, higher, higher - lower + 1));
    }

    void build(Source source) {
        for (Line line : source.getLines()) {
            try {
                if (line.getDirective() instanceof If) {
                    if (line.getInstructionObject() != null) {
                        nl.grauw.glass.instructions.If ins = (nl.grauw.glass.instructions.If) line.getInstruction();
                        build(ins.getThenSource());
                        if (ins.getElseSource() != null) {
                            build(ins.getElseSource());
                        }
                    }
                    else {
                        If ins = (If) line.getDirective();
                        build(ins.getThenSource());
                        if (ins.getElseSource() != null) {
                            build(ins.getElseSource());
                        }
                    }
                }
                else if (line.getDirective() instanceof Section) {
                    if (line.getInstructionObject() != null) {
                        nl.grauw.glass.instructions.Section ins = (nl.grauw.glass.instructions.Section) line.getInstruction();
                        build(ins.getSource());
                    }
                    else {
                        Section ins = (Section) line.getDirective();
                        build(ins.getSource());
                    }
                }
                else {
                    Scope scope = line.getScope();
                    if (scope.isAddressSet()) {
                        byte[] code = line.getBytes();
                        for (int i = 0; i < code.length; i++) {
                            ram.put(scope.getAddress() + i, code[i]);
                        }
                    }
                }
            } catch (AssemblyException e) {
                e.addContext(line);
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void writeObject(File objectPath) {
        try {
            if (objectPath.getName().toLowerCase().endsWith(".hex")) {
                OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(objectPath));
                os.write(getHex().toString());
                os.write(":00000001FF\r\n");
                os.close();
            }
            else {
                OutputStream output = new FileOutputStream(objectPath);
                output.write(getBinary());
                output.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    byte[] getBinary() {
        int from, to;

        from = 0;
        while (from < RAM_SIZE && !ram.containsKey(from)) {
            from++;
        }
        to = RAM_SIZE - 1;
        while (to > from && !ram.containsKey(to)) {
            to--;
        }
        to++;

        byte[] data = new byte[to - from];
        for (int i = 0; i < data.length; i++) {
            if (ram.containsKey(from + i)) {
                data[i] = ram.get(from + i);
            }
        }

        return data;
    }

    StringBuilder getHex() {
        int from, to;
        StringBuilder sb = new StringBuilder();

        from = 0;
        while (from < RAM_SIZE) {
            if (ram.containsKey(from)) {
                to = from + 1;
                while (to < RAM_SIZE && ram.containsKey(to)) {
                    to++;
                }
                byte[] data = new byte[to - from];
                for (int i = 0; i < data.length; i++) {
                    data[i] = ram.get(from + i);
                }
                sb.append(toHexString(from, data));
                from = to - 1;
            }
            from++;
        }

        return sb;
    }

    String toHexString(int addr, byte[] data) {
        StringBuilder sb = new StringBuilder();

        int i = 0;

        while ((data.length - i) > 0) {
            int l = data.length - i;
            if (l > 24) {
                l = 24;
            }
            sb.append(String.format(":%02X%04X%02X", l, addr, 0));

            int checksum = l + (addr & 0xFF) + ((addr >> 8) & 0xFF) + 0;
            for (int n = 0; n < l; n++, i++, addr++) {
                sb.append(String.format("%02X", data[i]));
                checksum += data[i];
            }

            sb.append(String.format("%02X\r\n", (-checksum) & 0xFF));
        }

        return sb.toString();
    }

}
