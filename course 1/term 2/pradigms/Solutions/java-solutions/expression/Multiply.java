package expression;

import expression.generic.Ops;

import java.math.BigInteger;

public class Multiply extends AbstractExpression {
//LEGACY//LEGACY//LEGACY//LEGACY//LEGACY//LEGACY//LEGACY//LEGACY//LEGACY//LEGACY//LEGACY//LEGACY//LEGACY//LEGACY

    public Multiply(UniversalExpression exp1, UniversalExpression exp2) {
        super(exp1, exp2);
    }

    @Override
    protected Number check(Number val1, Number val2, Ops evaluator) {
        return null;
    }


    @Override
    public int evaluate(int x) {
        return eval1(x) * eval2(x);
    }
    @Override
    public int evaluate(int x, int y, int z) {
        return tripleEval1(x, y, z) * tripleEval2(x, y, z);
    }

    @Override
    protected String operationSign() {
        return "*";
    }
}
