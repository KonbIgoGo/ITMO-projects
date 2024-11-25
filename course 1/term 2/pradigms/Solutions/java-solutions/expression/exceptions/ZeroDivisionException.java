package expression.exceptions;

public class ZeroDivisionException extends ArithmeticException {
    public ZeroDivisionException(String msg) {
        super(msg);
    }

    public ZeroDivisionException() {
        super();
    }
}
