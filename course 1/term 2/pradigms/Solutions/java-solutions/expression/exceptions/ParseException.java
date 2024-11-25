package expression.exceptions;

import java.io.IOException;

public class ParseException extends Exception {
    public ParseException(String msg) {
        super(msg);
    }
    public ParseException() {
        super();
    }
}
