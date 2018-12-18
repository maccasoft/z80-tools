package nl.grauw.glass.expressions;

public class Annotation extends Expression {

    private final Identifier annotation;
    private final Expression annotee;

    public Annotation(Identifier annotation, Expression annotee) {
        this.annotation = annotation;
        this.annotee = annotee;
    }

    @Override
    public Annotation copy(Context context) {
        return new Annotation(annotation.copy(context), annotee.copy(context));
    }

    @Override
    public Identifier getAnnotation() {
        return annotation;
    }

    @Override
    public Expression getAnnotee() {
        return annotee;
    }

    @Override
    public String toString() {
        return "" + annotation + " " + annotee;
    }

    @Override
    public String toDebugString() {
        return "{" + annotation.toDebugString() + " " + annotee.toDebugString() + "}";
    }

}
