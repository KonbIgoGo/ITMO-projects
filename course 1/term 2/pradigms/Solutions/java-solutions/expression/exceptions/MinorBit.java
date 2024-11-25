package expression.exceptions;

import expression.UniversalExpression;

import java.util.List;

public class MinorBit implements UniversalExpression {

    private final UniversalExpression content;
    public MinorBit(UniversalExpression exp) {
        this.content = exp;
    }
    @Override
    public int evaluate(int x) {
        return Integer.numberOfTrailingZeros(content.evaluate(x));
    }

    @Override
    public int evaluate(List<Integer> variables) {
        return Integer.numberOfTrailingZeros(content.evaluate(variables));
    }

    @Override
    public int evaluate(int x, int y, int z) {
        return Integer.numberOfTrailingZeros(content.evaluate(x,y,z));
    }

    public void toSb(StringBuilder sb) {
        sb.append("t0");
        sb.append("(");
        content.toSb(sb);
        sb.append(")");
    }

    @Override
    public <T extends Number> T evaluate(int x, int y, int z, String mode) {
        return null;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        toSb(sb);
        return sb.toString();
    }
}
