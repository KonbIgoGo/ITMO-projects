package expression;


import expression.generic.*;

import java.math.BigInteger;
import java.util.List;

public abstract class AbstractExpression implements UniversalExpression {

    //Note private
    private final List<UniversalExpression> content;

    public AbstractExpression(UniversalExpression exp1, UniversalExpression exp2) {
        this.content = List.of(exp1, exp2);
    }

    protected int eval1(int x) {
        return content.get(0).evaluate(x);
    }

    protected int eval2(int x) {
        return content.get(1).evaluate(x);
    }

    protected int tripleEval1(int x, int y, int z) {
        return content.get(0).evaluate(x, y, z);
    }

    protected int tripleEval2(int x, int y, int z) {
        return content.get(1).evaluate(x, y, z);
    }

    protected int listEval1(List<Integer> vals) {
        return content.get(0).evaluate(vals);
    }

    protected int listEval2(List<Integer> vals) {
        return content.get(1).evaluate(vals);
    }

    protected <T extends Number> T genericEval1(int x, int y, int z, String mode) {
        return content.get(0).evaluate(x,y,z,mode);
    }

    protected <T extends Number> T genericEval2(int x, int y, int z, String mode) {
        return content.get(1).evaluate(x,y,z,mode);
    }

    protected abstract Number check(Number val1, Number val2, Ops evaluator);

    @Override
    public int evaluate(int x) {
        return check(eval1(x), eval2(x), new CheckedIntOps()).intValue();
    }

    public int evaluate(int x, int y, int z) {
        return check(tripleEval1(x,y,z), tripleEval2(x,y,z), new CheckedIntOps()).intValue();
    };

    public int evaluate(List<Integer> vals) {
        return check(listEval1(vals), listEval2(vals), new CheckedIntOps()).intValue();
    };

    public <T extends Number> T evaluate(int x, int y, int z, String mode) {
        return (T) switch (mode) {
            case "i" -> check(tripleEval1(x,y,z), tripleEval2(x,y,z), new CheckedIntOps()).intValue();
            case "d" -> check(genericEval1(x,y,z, "d"), genericEval2(x,y,z, "d"), new DoubleOps()).doubleValue();
            case "bi" -> new BigInteger(String.valueOf(check(new BigInteger(String.valueOf(genericEval1(x,y,z, "bi"))),
                new BigInteger(String.valueOf(genericEval2(x,y,z, "bi"))), new BigIntegerOps())));
            case "u" -> check(genericEval1(x,y,z, "u"), genericEval2(x,y,z,"u"), new UncheckedIntOps()).intValue();
            case "b" -> check(genericEval1(x,y,z,"b"), genericEval2(x,y,z,"b"), new ByteOps()).byteValue();
            default -> throw new IllegalArgumentException("Incorrect mode");
        };
    }

    protected abstract String operationSign();
    
    public void toSb(StringBuilder sb) {
        sb.append("(");
        content.get(0).toSb(sb);
        sb.append(" ").append(operationSign()).append(" ");
        content.get(1).toSb(sb);
        sb.append(")");
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toSb(sb);
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return (content.get(0).hashCode() * 41 + (content.get(1).hashCode()) * 20 + operationSign().hashCode() % 1000);
    }
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o != null && this.getClass() == o.getClass()) {
            AbstractExpression other = (AbstractExpression) o;
            return this.content.get(0).equals(other.content.get(0))
                    && this.content.get(1).equals(other.content.get(1));
        }
        return false;
    }


}
