package expression.generic;

public class DoubleOps implements Ops {
    @Override
    public <T extends Number> Number add(T num1, T num2) {
        return num1.doubleValue() + num2.doubleValue();
    }

    @Override
    public <T extends Number> Number subtract(T num1, T num2) {
        return num1.doubleValue() - num2.doubleValue();
    }

    @Override
    public <T extends Number> Number multiply(T num1, T num2) {
        return num1.doubleValue() * num2.doubleValue();
    }

    @Override
    public <T extends Number> Number divide(T num1, T num2) {
        return num1.doubleValue() / num2.doubleValue();
    }
}
