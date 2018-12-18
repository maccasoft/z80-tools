package nl.grauw.glass.expressions;

import java.util.ArrayDeque;
import java.util.Deque;

import nl.grauw.glass.AssemblyException;

/**
 * Constructs an AST from the given expression tokens.
 *
 * It uses a shunting yard algorithm.
 */
public class ExpressionBuilder {

    private Deque<Expression> operands = new ArrayDeque<Expression>();
    private Deque<Operator> operators = new ArrayDeque<Operator>();
    private int groupCount = 0;

    public ExpressionBuilder() {
        operators.push(SENTINEL);
    }

    public void addValueToken(Expression value) {
        operands.push(value);
    }

    public void addOperatorToken(Operator operator) {
        evaluateNotYieldingTo(operator);

        if (operator == GROUP_OPEN || operator == INDEX_OPEN) {
            groupCount++;
            operators.push(operator);
            operators.push(SENTINEL);
        }
        else if (operator == GROUP_CLOSE || operator == INDEX_CLOSE) {
            groupCount--;
            if (operators.pop() != SENTINEL) {
                throw new AssemblyException("Sentinel expected.");
            }
            if (operator == GROUP_CLOSE && operators.peek() != GROUP_OPEN) {
                throw new ExpressionError("Group open expected.");
            }
            if (operator == INDEX_CLOSE && operators.peek() != INDEX_OPEN) {
                throw new ExpressionError("Index open expected.");
            }
        }
        else {
            operators.push(operator);
        }
    }

    public Expression getExpression() {
        if (operands.isEmpty() || operators.isEmpty()) {
            throw new AssemblyException("Operands / operators is empty: " + this);
        }

        // process remainder
        evaluateNotYieldingTo(SENTINEL);

        if (operators.size() > 1 && operators.peek() == SENTINEL) {
            throw new ExpressionError("Group close expected.");
        }
        if (operands.size() > 1 || operators.size() != 1) {
            throw new AssemblyException("Not all operands / operators were processed: " + this);
        }

        return operands.pop();
    }

    private void evaluateNotYieldingTo(Operator operator) {
        while (!operators.peek().yieldsTo(operator)) {
            operands.push(operators.pop().evaluate());
        }
    }

    public boolean hasOpenGroup() {
        return groupCount > 0;
    }

    @Override
    public String toString() {
        return "" + operands + " / " + operators;
    }

    private abstract class Operator {

        private Precedence precedence;
        private Associativity associativity;

        private Operator(Precedence precedence, Associativity associativity) {
            this.precedence = precedence;
            this.associativity = associativity;
        }

        public boolean yieldsTo(Operator other) {
            if (associativity == Associativity.LEFT_TO_RIGHT) {
                return precedence.ordinal() > other.precedence.ordinal();
            }
            else {
                return precedence.ordinal() >= other.precedence.ordinal();
            }
        }

        public abstract Expression evaluate();
    }

    public final Operator POSITIVE = new Operator(Precedence.UNARY, Associativity.RIGHT_TO_LEFT) {
        @Override
        public Expression evaluate() {
            return new Positive(operands.pop());
        };
    };

    public final Operator NEGATIVE = new Operator(Precedence.UNARY, Associativity.RIGHT_TO_LEFT) {
        @Override
        public Expression evaluate() {
            return new Negative(operands.pop());
        };
    };

    public final Operator COMPLEMENT = new Operator(Precedence.UNARY, Associativity.RIGHT_TO_LEFT) {
        @Override
        public Expression evaluate() {
            return new Complement(operands.pop());
        };
    };

    public final Operator NOT = new Operator(Precedence.UNARY, Associativity.RIGHT_TO_LEFT) {
        @Override
        public Expression evaluate() {
            return new Not(operands.pop());
        };
    };

    public final Operator MEMBER = new Operator(Precedence.MEMBER, Associativity.LEFT_TO_RIGHT) {
        @Override
        public Expression evaluate() {
            Expression operandRight = operands.pop();
            if (!(operandRight instanceof Identifier)) {
                throw new ExpressionError("Member operator right hand side must be an identifier.");
            }
            return new Member(operands.pop(), (Identifier) operandRight);
        };
    };

    public final Operator MULTIPLY = new Operator(Precedence.MULTIPLICATION, Associativity.LEFT_TO_RIGHT) {
        @Override
        public Expression evaluate() {
            Expression operandRight = operands.pop();
            return new Multiply(operands.pop(), operandRight);
        };
    };

    public final Operator DIVIDE = new Operator(Precedence.MULTIPLICATION, Associativity.LEFT_TO_RIGHT) {
        @Override
        public Expression evaluate() {
            Expression operandRight = operands.pop();
            return new Divide(operands.pop(), operandRight);
        };
    };

    public final Operator MODULO = new Operator(Precedence.MULTIPLICATION, Associativity.LEFT_TO_RIGHT) {
        @Override
        public Expression evaluate() {
            Expression operandRight = operands.pop();
            return new Modulo(operands.pop(), operandRight);
        };
    };

    public final Operator ADD = new Operator(Precedence.ADDITION, Associativity.LEFT_TO_RIGHT) {
        @Override
        public Expression evaluate() {
            Expression operandRight = operands.pop();
            return new Add(operands.pop(), operandRight);
        };
    };

    public final Operator SUBTRACT = new Operator(Precedence.ADDITION, Associativity.LEFT_TO_RIGHT) {
        @Override
        public Expression evaluate() {
            Expression operandRight = operands.pop();
            return new Subtract(operands.pop(), operandRight);
        };
    };

    public final Operator SHIFT_LEFT = new Operator(Precedence.SHIFT, Associativity.LEFT_TO_RIGHT) {
        @Override
        public Expression evaluate() {
            Expression operandRight = operands.pop();
            return new ShiftLeft(operands.pop(), operandRight);
        };
    };

    public final Operator SHIFT_RIGHT = new Operator(Precedence.SHIFT, Associativity.LEFT_TO_RIGHT) {
        @Override
        public Expression evaluate() {
            Expression operandRight = operands.pop();
            return new ShiftRight(operands.pop(), operandRight);
        };
    };

    public final Operator LESS_THAN = new Operator(Precedence.COMPARISON, Associativity.LEFT_TO_RIGHT) {
        @Override
        public Expression evaluate() {
            Expression operandRight = operands.pop();
            return new LessThan(operands.pop(), operandRight);
        };
    };

    public final Operator LESS_OR_EQUALS = new Operator(Precedence.COMPARISON, Associativity.LEFT_TO_RIGHT) {
        @Override
        public Expression evaluate() {
            Expression operandRight = operands.pop();
            return new LessOrEquals(operands.pop(), operandRight);
        };
    };

    public final Operator GREATER_THAN = new Operator(Precedence.COMPARISON, Associativity.LEFT_TO_RIGHT) {
        @Override
        public Expression evaluate() {
            Expression operandRight = operands.pop();
            return new GreaterThan(operands.pop(), operandRight);
        };
    };

    public final Operator GREATER_OR_EQUALS = new Operator(Precedence.COMPARISON, Associativity.LEFT_TO_RIGHT) {
        @Override
        public Expression evaluate() {
            Expression operandRight = operands.pop();
            return new GreaterOrEquals(operands.pop(), operandRight);
        };
    };

    public final Operator EQUALS = new Operator(Precedence.EQUALITY, Associativity.LEFT_TO_RIGHT) {
        @Override
        public Expression evaluate() {
            Expression operandRight = operands.pop();
            return new Equals(operands.pop(), operandRight);
        };
    };

    public final Operator NOT_EQUALS = new Operator(Precedence.EQUALITY, Associativity.LEFT_TO_RIGHT) {
        @Override
        public Expression evaluate() {
            Expression operandRight = operands.pop();
            return new NotEquals(operands.pop(), operandRight);
        };
    };

    public final Operator AND = new Operator(Precedence.AND, Associativity.LEFT_TO_RIGHT) {
        @Override
        public Expression evaluate() {
            Expression operandRight = operands.pop();
            return new And(operands.pop(), operandRight);
        };
    };

    public final Operator XOR = new Operator(Precedence.XOR, Associativity.LEFT_TO_RIGHT) {
        @Override
        public Expression evaluate() {
            Expression operandRight = operands.pop();
            return new Xor(operands.pop(), operandRight);
        };
    };

    public final Operator OR = new Operator(Precedence.OR, Associativity.LEFT_TO_RIGHT) {
        @Override
        public Expression evaluate() {
            Expression operandRight = operands.pop();
            return new Or(operands.pop(), operandRight);
        };
    };

    public final Operator LOGICAL_AND = new Operator(Precedence.LOGICAL_AND, Associativity.LEFT_TO_RIGHT) {
        @Override
        public Expression evaluate() {
            Expression operandRight = operands.pop();
            return new LogicalAnd(operands.pop(), operandRight);
        };
    };

    public final Operator LOGICAL_OR = new Operator(Precedence.LOGICAL_OR, Associativity.LEFT_TO_RIGHT) {
        @Override
        public Expression evaluate() {
            Expression operandRight = operands.pop();
            return new LogicalOr(operands.pop(), operandRight);
        };
    };

    public final Operator ANNOTATION = new Operator(Precedence.ANNOTATION, Associativity.RIGHT_TO_LEFT) {
        @Override
        public Expression evaluate() {
            Expression operandRight = operands.pop();
            Expression operandLeft = operands.pop();
            if (!(operandLeft instanceof Identifier)) {
                throw new ExpressionError("Annotation left hand side must be an identifier.");
            }
            return new Annotation((Identifier) operandLeft, operandRight);
        };
    };

    public final Operator SEQUENCE = new Operator(Precedence.SEQUENCE, Associativity.RIGHT_TO_LEFT) {
        @Override
        public Expression evaluate() {
            Expression operandRight = operands.pop();
            return new Sequence(operands.pop(), operandRight);
        };
    };

    public final Operator TERNARYIF = new Operator(Precedence.TERNARYIFELSE, Associativity.RIGHT_TO_LEFT) {
        @Override
        public Expression evaluate() {
            throw new ExpressionError("Ternary if (?) without else (:).");
        };
    };

    public final Operator TERNARYELSE = new Operator(Precedence.TERNARYIFELSE, Associativity.RIGHT_TO_LEFT) {
        @Override
        public Expression evaluate() {
            Expression operandRight = operands.pop();
            while (operators.peek() == TERNARYELSE) {
                operands.push(operators.pop().evaluate());
            }
            if (operators.peek() == TERNARYIF) {
                operators.pop();
                Expression operandMiddle = operands.pop();
                return new IfElse(operands.pop(), operandMiddle, operandRight);
            }
            else {
                throw new ExpressionError("Ternary else (:) without if (?).");
            }
        };
    };

    public final Operator GROUP_OPEN = new Operator(Precedence.GROUPING, Associativity.LEFT_TO_RIGHT) {
        @Override
        public Expression evaluate() {
            return new Group(operands.pop());
        };
    };

    public final Operator GROUP_CLOSE = new Operator(Precedence.NONE, Associativity.LEFT_TO_RIGHT) {
        @Override
        public Expression evaluate() {
            throw new AssemblyException("Can not evaluate group close.");
        };
    };

    public final Operator INDEX_OPEN = new Operator(Precedence.MEMBER, Associativity.LEFT_TO_RIGHT) {
        @Override
        public Expression evaluate() {
            Expression operandRight = operands.pop();
            return new Index(operands.pop(), operandRight);
        };
    };

    public final Operator INDEX_CLOSE = new Operator(Precedence.NONE, Associativity.LEFT_TO_RIGHT) {
        @Override
        public Expression evaluate() {
            throw new AssemblyException("Can not evaluate group close.");
        };
    };

    public final Operator SENTINEL = new Operator(Precedence.NONE, Associativity.RIGHT_TO_LEFT) {
        @Override
        public Expression evaluate() {
            throw new AssemblyException("Can not evaluate sentinel.");
        };
    };

    private enum Precedence {
        GROUPING,
        MEMBER,
        UNARY,
        MULTIPLICATION,
        ADDITION,
        SHIFT,
        COMPARISON,
        EQUALITY,
        AND,
        XOR,
        OR,
        LOGICAL_AND,
        LOGICAL_OR,
        TERNARYIFELSE,
        ANNOTATION,
        SEQUENCE,
        NONE
    }

    private enum Associativity {
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT
    }

    public static class ExpressionError extends AssemblyException {
        private static final long serialVersionUID = 1L;

        public ExpressionError(String message) {
            super("Expression error: " + message);
        }
    }

}
