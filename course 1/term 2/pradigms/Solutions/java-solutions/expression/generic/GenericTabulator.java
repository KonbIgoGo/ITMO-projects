package expression.generic;

import expression.UniversalExpression;
import expression.exceptions.ExpressionParser;
import expression.exceptions.ParseException;

public class GenericTabulator implements Tabulator {
    @Override
    public Object[][][] tabulate(String mode, String expression, int x1, int x2, int y1, int y2, int z1, int z2) throws ParseException {
        Object[][][] res = new Object[x2 - x1 + 1][y2 - y1 + 1][z2 - z1 + 1];
        UniversalExpression exp = new ExpressionParser().parse(expression, mode);


        for (int i = 0; i < x2 - x1 + 1; i++) {
            for (int j = 0; j < y2 - y1 + 1; j++) {
                for (int k = 0; k < z2 - z1 + 1; k++) {
                    try {
                        res[i][j][k] = exp.evaluate(x1 + i, y1 + j, z1 + k, mode);
                    } catch (ArithmeticException e) {
                        res[i][j][k] = null;
                    }
                }
            }
        }
        return res;
    }
}
