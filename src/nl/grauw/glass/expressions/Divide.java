package nl.grauw.glass.expressions;

public class Divide extends BinaryOperator {

    public Divide(Expression dividend, Expression divisor) {
        super(dividend, divisor);
    }

    @Override
    public Divide copy(Context context) {
        return new Divide(term1.copy(context), term2.copy(context));
    }

    public Expression getDividend() {
        return term1;
    }

    public Expression getDivisor() {
        return term2;
    }

    @Override
    public int getInteger() {
        int divisor = term2.getInteger();
        if (divisor == 0) {
            throw new EvaluationException("Division by zero.");
        }
        return term1.getInteger() / divisor;
    }

    @Override
    public String getLexeme() {
        return "/";
    }

}
