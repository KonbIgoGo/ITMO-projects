package expression;
import expression.exceptions.ExpressionParser;
import expression.exceptions.ParseException;
import expression.generic.GenericTabulator;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        ExpressionParser p = new ExpressionParser();

        for (int i = 0; i < 11; i++) {
            StringBuilder out = new StringBuilder();
            out.append(i).append(" ".repeat(10 - Integer.toString(i).length()));
            try {
                int ans = p.parse("1000000*x*x*x*x*x/(x-1)").evaluate(i, 0, 0);
                out.append(ans);
            } catch (Exception e) {
                out.append(e.getMessage());
            }


        }

        for (int i = 0; i < 200; i++) {
            for (int j = 0; j < 200; j++) {
                for (int k = 0; k < 200; k++) {
                    for (int l = 0; l < 200; l++) {
                        if ((i - k) * 31 == l - j) {
                            StringBuilder w = new StringBuilder();
                            StringBuilder w2 = new StringBuilder();
                            w.append((char) i);
                            w.append((char) k);
                            w2.append((char) j);
                            w2.append((char) l);

                            if (!w.toString().contentEquals(w2)) {
                                System.out.println(w.toString().hashCode() == w2.toString().hashCode());
                            }
                        }
                    }
                }
            }
        }
    }
}
