package expression.generic;

import java.math.BigInteger;

public class BigIntegerOps implements Ops {


    public <T extends Number> Number add(T num1, T num2) {
        return new BigInteger(String.valueOf(num1)).add(new BigInteger(String.valueOf(num2)));
    }

    @Override
    public <T extends Number> Number subtract(T num1, T num2) {
        return new BigInteger(String.valueOf(num1)).subtract(new BigInteger(String.valueOf(num2)));
    }

    @Override
    public <T extends Number> Number multiply(T num1, T num2) {
        return new BigInteger(String.valueOf(num1)).multiply(new BigInteger(String.valueOf(num2)));
    }

    @Override
    public <T extends Number> Number divide(T num1, T num2) {
        return new BigInteger(String.valueOf(num1)).divide(new BigInteger(String.valueOf(num2)));
    }


}
