package nl.grauw.glass.expressions;

public class GreaterOrEquals extends BinaryOperator {

    public GreaterOrEquals(Expression term1, Expression term2) {
        super(term1, term2);
    }

    @Override
    public GreaterOrEquals copy(Context context) {
        return new GreaterOrEquals(term1.copy(context), term2.copy(context));
    }

    @Override
    public int getInteger() {
        return term1.getInteger() >= term2.getInteger() ? -1 : 0;
    }

    @Override
    public String getLexeme() {
        return ">=";
    }

}
