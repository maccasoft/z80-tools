package nl.grauw.glass.expressions;

public class Subtract extends BinaryOperator {

    public Subtract(Expression minuend, Expression subtrahend) {
        super(minuend, subtrahend);
    }

    @Override
    public Subtract copy(Context context) {
        return new Subtract(term1.copy(context), term2.copy(context));
    }

    public Expression getMinuend() {
        return term1;
    }

    public Expression getSubtrahend() {
        return term2;
    }

    @Override
    public int getInteger() {
        return term1.getInteger() - term2.getInteger();
    }

    @Override
    public boolean isRegister() {
        if (term1.isRegister() && term2.isInteger()) {
            Register register = term1.getRegister();
            return register.isIndex() && register.isPair();
        }
        return false;
    }

    @Override
    public Register getRegister() {
        if (term1.isRegister() && term2.isInteger()) {
            Register register = term1.getRegister();
            if (register.isIndex() && register.isPair()) {
                return new Register(register, new Subtract(register.getIndexOffset(), term2));
            }
        }
        throw new EvaluationException("Not a register.");
    }

    @Override
    public String getLexeme() {
        return "-";
    }

}
