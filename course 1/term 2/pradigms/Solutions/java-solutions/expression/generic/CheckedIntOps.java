package expression.generic;

import expression.exceptions.NumberOverflowException;
import expression.exceptions.ZeroDivisionException;

public class CheckedIntOps implements Ops{

    public <T extends Number> Number add(T val1, T val2) {
        int num1 = val1.intValue();
        int num2 = val2.intValue();
        if (num1 > 0 && num2 > Integer.MAX_VALUE - num1
                || num1 < 0 && num2 < Integer.MIN_VALUE - num1) {
            throw new NumberOverflowException("overflow");
        }
        return num1 + num2;
    }

    public <T extends Number> Number subtract(T val1, T val2) {
        int num1 = val1.intValue();
        int num2 = val2.intValue();
        if ((num2 > 0 && num1 < Integer.MIN_VALUE + num2)
                || (num2 < 0 && num1 > Integer.MAX_VALUE + num2)) {
            throw new NumberOverflowException("overflow");
        }
        return num1 - num2;
    }

    public <T extends Number> Number multiply(T val1, T val2) {
        int num1 = val1.intValue();
        int num2 = val2.intValue();
        if (((num1 == Integer.MIN_VALUE && num2 == -1) || (num1 == -1 && num2 == Integer.MIN_VALUE))
                || (num1 > 0 && num2 > 0 && num1 > Integer.MAX_VALUE / num2)
                || (num1 > 0 && num2 < 0 && ((num1 > Integer.MIN_VALUE / num2) && num2 != -1))
                || (num1 < 0 && num2 > 0 && num1 < Integer.MIN_VALUE / num2)
                || (num1 < 0 && num2 < 0 && num1 < Integer.MAX_VALUE / num2)) {
            throw new NumberOverflowException("integer overflow");
        }
        return num1 * num2;
    }

    public <T extends Number> Number divide(T val1, T val2) {
        int num1 = val1.intValue();
        int num2 = val2.intValue();
        if (num2 == 0) {
            throw new ZeroDivisionException("division by zero");
        } else if( num1 == Integer.MIN_VALUE && num2 == -1) {
            throw new NumberOverflowException("overflow");
        }
        return num1 / num2;
    }
}
