package expression.exceptions;

public class NoArgumentException extends ParseException {
    public NoArgumentException(String msg) {
        super(msg);
    }

    public NoArgumentException() {
        super();
    }
}
