package nl.grauw.glass.expressions;

public class GreaterThan extends BinaryOperator {

    public GreaterThan(Expression term1, Expression term2) {
        super(term1, term2);
    }

    @Override
    public GreaterThan copy(Context context) {
        return new GreaterThan(term1.copy(context), term2.copy(context));
    }

    @Override
    public int getInteger() {
        return term1.getInteger() > term2.getInteger() ? -1 : 0;
    }

    @Override
    public String getLexeme() {
        return ">";
    }

}
