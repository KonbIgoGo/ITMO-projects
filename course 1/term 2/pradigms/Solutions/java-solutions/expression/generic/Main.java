package expression.generic;

import expression.exceptions.ExpressionParser;
import expression.exceptions.ParseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws ParseException {
        ExpressionParser p = new ExpressionParser();
        System.out.println(p.parse("200 * x*2122412/13").evaluate(5,0,0));

    }
}
