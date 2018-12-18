package nl.grauw.glass.expressions;

public abstract class UnaryOperator extends Expression {

    protected final Expression term;

    public abstract String getLexeme();

    public UnaryOperator(Expression term) {
        this.term = term;
    }

    public Expression getTerm() {
        return term;
    }

    @Override
    public boolean isInteger() {
        return term.isInteger();
    }

    @Override
    public String toString() {
        return getLexeme() + term;
    }

    @Override
    public String toDebugString() {
        return getLexeme() + term.toDebugString();
    }

}
