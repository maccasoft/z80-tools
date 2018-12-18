package nl.grauw.glass.expressions;

public class Group extends Passthrough {

    private final Expression term;

    public Group(Expression term) {
        this.term = term;
    }

    @Override
    public Group copy(Context context) {
        return new Group(term.copy(context));
    }

    public Expression getTerm() {
        return term;
    }

    @Override
    public Expression resolve() {
        return term;
    }

    @Override
    public boolean isGroup() {
        return true;
    }

    @Override
    public String toString() {
        return "(" + term + ")";
    }

    @Override
    public String toDebugString() {
        return "(" + term.toDebugString() + ")";
    }

}
