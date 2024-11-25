package expression;

import java.util.List;

public class Negate implements UniversalExpression {
    //LEGACY//LEGACY//LEGACY//LEGACY//LEGACY//LEGACY//LEGACY//LEGACY//LEGACY//LEGACY//LEGACY//LEGACY//LEGACY
    private final UniversalExpression exp;
    public Negate(UniversalExpression exp) {
        this.exp = exp;
    }
    @Override
    public int evaluate(int x) {
        return -(exp.evaluate(x));
    }

    @Override
    public int evaluate(int x, int y, int z) {
        return -(exp.evaluate(x,y,z));
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
        return null;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        toSb(sb);
        return sb.toString();
    }

    @Override
    public int evaluate(List<Integer> variables) {
        return -exp.evaluate(variables);
    }
}
