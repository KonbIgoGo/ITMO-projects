package expression.exceptions;

public class UnknownSymbolException extends ParseException {
    public UnknownSymbolException(String msg) {
        super(msg);
    }
}
