package expression;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

public class Const implements UniversalExpression {
    final Number num;

    public Const(int num) {
        this.num = num;
    }

    public<T extends Number> Const(T num) {
        this.num = num;
    }

    @Override
    public int evaluate(int x) {
        return num.intValue();
    }

    @Override
    public int evaluate(int x, int y, int z) {
        // :NOTE: ??
        return num.intValue();
    }

    @Override
    public int evaluate(List<Integer> variables) {
        return num.intValue();
    }
    public String toString() {
        return num.toString();
    }

    public void toSb(StringBuilder sb) {
        sb.append(num);
    }

    @Override
    public <V extends Number> V evaluate(int x, int y, int z, String mode) {
        return (V) switch (mode) {
            case "i", "u" -> evaluate(x,y,z);
            case "d" -> (double) evaluate(x,y,z);
            case "bi" -> new BigInteger(Integer.toString(evaluate(x,y,z)));
            case "b" -> (byte) evaluate(x,y,z);
            default -> throw new IllegalStateException("Unexpected mode: " + mode);
        };
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(num);
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && this.getClass() == o.getClass()) {
            Const other = (Const) o;
            return Objects.equals(num, other.num);
        }
        return false;
    }
}
