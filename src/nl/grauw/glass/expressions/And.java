package nl.grauw.glass.expressions;

public class And extends BinaryOperator {

    public And(Expression term1, Expression term2) {
        super(term1, term2);
    }

    @Override
    public And copy(Context context) {
        return new And(term1.copy(context), term2.copy(context));
    }

    @Override
    public int getInteger() {
        return term1.getInteger() & term2.getInteger();
    }

    @Override
    public String getLexeme() {
        return "&";
    }

}
