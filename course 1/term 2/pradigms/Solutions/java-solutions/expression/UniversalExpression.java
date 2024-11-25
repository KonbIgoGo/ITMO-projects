package expression;

public interface UniversalExpression extends TripleExpression, Expression, ListExpression {
    void toSb(StringBuilder sb);


    <T extends Number> T evaluate(int x, int y, int z, String mode);
}
