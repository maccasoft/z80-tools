package nl.grauw.glass.expressions;

public class LogicalOr extends BinaryOperator {

    public LogicalOr(Expression term1, Expression term2) {
        super(term1, term2);
    }

    @Override
    public LogicalOr copy(Context context) {
        return new LogicalOr(term1.copy(context), term2.copy(context));
    }

    @Override
    public int getInteger() {
        int value1 = term1.getInteger();
        return value1 != 0 ? value1 : term2.getInteger();
    }

    @Override
    public String getLexeme() {
        return "||";
    }

}
