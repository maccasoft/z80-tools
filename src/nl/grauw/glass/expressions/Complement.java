package nl.grauw.glass.expressions;

public class Complement extends UnaryOperator {

    public Complement(Expression term) {
        super(term);
    }

    @Override
    public Complement copy(Context context) {
        return new Complement(term.copy(context));
    }

    @Override
    public int getInteger() {
        return ~term.getInteger();
    }

    @Override
    public String getLexeme() {
        return "~";
    }

}
