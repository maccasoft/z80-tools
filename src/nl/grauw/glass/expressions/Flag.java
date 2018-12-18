package nl.grauw.glass.expressions;

public class Flag extends Literal {

    public static Flag NZ = new Flag("nz", 0);
    public static Flag Z = new Flag("z", 1);
    public static Flag NC = new Flag("nc", 2);
    public static Flag C = new Flag("c", 3);
    public static Flag PO = new Flag("po", 4);
    public static Flag PE = new Flag("pe", 5);
    public static Flag P = new Flag("p", 6);
    public static Flag M = new Flag("m", 7);

    private final String name;
    private final int code;

    public Flag(String name, int code) {
        this.name = name;
        this.code = code;
    }

    @Override
    public Flag copy(Context context) {
        return this;
    }

    public int getCode() {
        return code;
    }

    @Override
    public boolean isFlag() {
        return true;
    }

    @Override
    public Flag getFlag() {
        return this;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String toDebugString() {
        return toString();
    }

    public static Flag getByName(String name) {
        switch (name) {
            case "nz":
            case "NZ":
                return Flag.NZ;
            case "z":
            case "Z":
                return Flag.Z;
            case "nc":
            case "NC":
                return Flag.NC;
            case "c":
            case "C":
                return Flag.C;
            case "po":
            case "PO":
                return Flag.PO;
            case "pe":
            case "PE":
                return Flag.PE;
            case "p":
            case "P":
                return Flag.P;
            case "m":
            case "M":
                return Flag.M;
        }
        return null;
    }

}
