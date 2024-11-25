package expression.generic;

public interface Ops {
    <T extends Number> Number add(T num1, T num2);
    <T extends Number> Number subtract(T num1, T num2);
    <T extends Number> Number multiply(T num1, T num2);
    <T extends Number> Number divide(T num1, T num2);
}
