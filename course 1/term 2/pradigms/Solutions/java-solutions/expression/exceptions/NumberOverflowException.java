package expression.exceptions;

public class NumberOverflowException extends ArithmeticException {
    public NumberOverflowException(String msg) {
        super(msg);
    }

    public NumberOverflowException() {
        super();
    }
}
