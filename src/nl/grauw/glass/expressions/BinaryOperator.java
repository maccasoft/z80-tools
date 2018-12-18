package nl.grauw.glass.expressions;

public abstract class BinaryOperator extends Expression {

    protected final Expression term1;
    protected final Expression term2;

    public abstract String getLexeme();

    public BinaryOperator(Expression term1, Expression term2) {
        this.term1 = term1;
        this.term2 = term2;
    }

    public Expression getTerm1() {
        return term1;
    }

    public Expression getTerm2() {
        return term2;
    }

    @Override
    public boolean isInteger() {
        return term1.isInteger() && term2.isInteger();
    }

    @Override
    public String toString() {
        return "" + term1 + " " + getLexeme() + " " + term2;
    }

    @Override
    public String toDebugString() {
        return "{" + term1.toDebugString() + " " + getLexeme() + " " + term2.toDebugString() + "}";
    }

}
