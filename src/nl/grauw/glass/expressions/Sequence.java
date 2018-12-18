package nl.grauw.glass.expressions;

import java.util.List;

public class Sequence extends BinaryOperator {

    public Sequence(Expression value, Expression tail) {
        super(value, tail);
    }

    @Override
    public Sequence copy(Context context) {
        return new Sequence(term1.copy(context), term2.copy(context));
    }

    public Expression getValue() {
        return term1;
    }

    public Expression getTail() {
        return term2;
    }

    @Override
    protected void addToList(List<Expression> list) {
        term1.addToList(list);
        Expression tail = term2;
        while (tail != null) {
            tail.getElement().addToList(list);
            tail = tail.getNext();
        }
    }

    @Override
    public Expression getElement(int index) {
        return index == 0 ? term1 : term2.getElement(index - 1);
    }

    @Override
    public Expression getNext() {
        return term2;
    }

    @Override
    public String getLexeme() {
        return ",";
    }

    @Override
    public String toString() {
        return "" + term1 + ", " + term2;
    }

    @Override
    public String toDebugString() {
        return "{" + term1.toDebugString() + ", " + term2.toDebugString() + "}";
    }

}
