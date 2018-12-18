package nl.grauw.glass.expressions;

public class ContextLiteral extends Literal {

    private final Context context;

    public ContextLiteral(Context context) {
        this.context = context;
    }

    @Override
    public ContextLiteral copy(Context context) {
        return new ContextLiteral(context);
    }

    @Override
    public boolean isContext() {
        return true;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public boolean isInteger() {
        return true;
    }

    @Override
    public int getInteger() {
        return context.getAddress();
    }

    @Override
    public String toString() {
        try {
            return getHexValue();
        } catch (EvaluationException e) {
            return "<" + e.getMessage() + ">";
        }
    }

    @Override
    public String toDebugString() {
        return toString();
    }

}
