package expression.exceptions;

import expression.AbstractExpression;
import expression.UniversalExpression;
import expression.generic.Ops;

import java.math.BigInteger;

public class CheckedMultiply extends AbstractExpression {
    public CheckedMultiply(UniversalExpression exp1, UniversalExpression exp2) {
        super(exp1, exp2);
    }

    @Override
    protected Number check(Number val1, Number val2, Ops evaluator) {
        return evaluator.multiply(val1, val2);
    }

    @Override
    protected String operationSign() {
        return "*";
    }

}
