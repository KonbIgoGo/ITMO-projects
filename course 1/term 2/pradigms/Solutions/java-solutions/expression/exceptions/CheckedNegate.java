package expression.exceptions;
import expression.UniversalExpression;

import java.math.BigInteger;
import java.util.List;

public class CheckedNegate implements UniversalExpression {
    private final UniversalExpression exp;
    public CheckedNegate(UniversalExpression exp) {
        this.exp = exp;
    }
    @Override
    public int evaluate(int x, int y, int z) {
        int res = exp.evaluate(x, y, z);
        if (res == Integer.MIN_VALUE) {
            throw new NumberOverflowException("overflow");
        }
        return -res;
    }

    @Override
    public int evaluate(int x) {
        int res = exp.evaluate(x);
        if (res == Integer.MIN_VALUE) {
            throw new NumberOverflowException("overflow");
        }
        return -res;
    }

    @Override
    public void toSb(StringBuilder sb) {
        sb.append("-");
        sb.append("(");
        exp.toSb(sb);
        sb.append(")");
    }

    @Override
    public <T extends Number> T evaluate(int x, int y, int z, String mode) {
        return (T) switch (mode) {
            case "i" -> evaluate(x,y,z);
            case "d" -> -exp.evaluate(x,y,z,mode).doubleValue();
            case "bi" -> {
                String res = exp.evaluate(x,y,z,mode).toString();
                if (res.charAt(0) == '-') {
                    yield new BigInteger(exp.evaluate(x,y,z,mode).toString().substring(1));
                } else {
                    yield new BigInteger("-" + exp.evaluate(x,y,z,mode).toString());
                }
            }
            case "u" -> -exp.evaluate(x,y,z, mode).intValue();
            case "b" -> (byte)(-exp.evaluate(x,y,z, mode).intValue());
            default -> throw new IllegalStateException("Unexpected mode: " + mode);
        };
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        toSb(sb);
        return sb.toString();
    }

    @Override
    public int evaluate(List<Integer> variables) {
        int res = exp.evaluate(variables);
        if (res == Integer.MIN_VALUE) {
            throw new NumberOverflowException("overflow");
        }
        return -res;
    }
}
