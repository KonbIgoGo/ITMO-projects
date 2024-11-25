package expression;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

public class Variable implements UniversalExpression {

    String var = "x";
    int ind = -1;

    public Variable(String var) {
        this.var = var;
    }

    public Variable(int ind) {
        this.ind = ind;
    }

    public Variable(int ind, String var) {
        this.ind = ind;
        this.var = var;
    }

    @Override
    public int evaluate(int x) {
        return x;
    }

    @Override
    public int evaluate(int x, int y, int z) {
        return switch (var) {
            case "x" -> x;
            case "y" -> y;
            case "z" -> z;
            default -> 0;
        };
    }

    @Override
    public int evaluate(List<Integer> variables) {
        return variables.get(ind);
    }


    public String toString() {
        return var;
    }

    public void toSb(StringBuilder sb) {
        sb.append(var);
    }

    @Override
    public <T extends Number> T evaluate(int x, int y, int z, String mode) {
        return (T) switch (mode) {
            case "i", "u" -> evaluate(x,y,z);
            case "d" -> (double) evaluate(x,y,z);
            case "bi" -> new BigInteger(Integer.toString(evaluate(x,y,z)));
            case "b" -> (byte) evaluate(x,y,z);
            default -> throw new IllegalStateException("Unexpected mode: " + mode);
        };
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(var);
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && this.getClass() == o.getClass()) {
            Variable other = (Variable) o;
            return var.equals(other.var);
        }
        return false;
    }
}
