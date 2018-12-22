package nl.grauw.glass.expressions;

public class IntegerLiteral extends Literal {

    public static final IntegerLiteral ZERO = new IntegerLiteral(0);
    public static final IntegerLiteral ONE = new IntegerLiteral(1);

    private final int value;
    private final int base;

    public IntegerLiteral(int value) {
        this.value = value;
        this.base = 10;
    }

    public IntegerLiteral(int value, int base) {
        this.value = value;
        this.base = base;
    }

    @Override
    public IntegerLiteral copy(Context context) {
        return this;
    }

    @Override
    public boolean isInteger() {
        return true;
    }

    @Override
    public int getInteger() {
        return value;
    }

    public int getBase() {
        return base;
    }

    @Override
    public String toString() {
        String string = Integer.toString(value, base).toUpperCase();

        switch (base) {
            case 2:
                if (string.length() > 8) {
                    while (string.length() < 16) {
                        string = "0" + string;
                    }
                }
                else {
                    while (string.length() < 8) {
                        string = "0" + string;
                    }
                }
                return string + "B";
            case 8:
                return string + "H";
            case 16:
                if (string.length() == 1 || string.length() == 3) {
                    string = "0" + string;
                }
                return (string.charAt(0) >= 'A' && string.charAt(0) <= 'F' ? "0" : "") + string + "H";
        }

        return string;
    }

    @Override
    public String toDebugString() {
        String string = Integer.toHexString(value).toUpperCase();
        return (string.charAt(0) >= 'A' && string.charAt(0) <= 'F' ? "0" : "") + string + "H";
    }

}
