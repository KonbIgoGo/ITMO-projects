package expression.generic;

public class ByteOps implements Ops {
    @Override
    public <T extends Number> Number add(T num1, T num2) {
        return (num1.intValue() + num2.intValue());
    }

    @Override
    public <T extends Number> Number subtract(T num1, T num2) {
        return (num1.intValue() - num2.intValue());
    }

    @Override
    public <T extends Number> Number multiply(T num1, T num2) {
        return (num1.intValue() * num2.intValue());
    }

    @Override
    public <T extends Number> Number divide(T num1, T num2) {
        return (num1.intValue() / num2.intValue());
    }
}
