package expression.parser;

public class StringCharSource implements CharSource {
    private final String str;
    private int pos;

    public StringCharSource(String str) {
        this.str = str;
    }

    @Override
    public boolean hasNext() {
        return pos < str.length();
    }

    @Override
    public char next() {
        return str.charAt(pos++);
    }

    @Override
    public IllegalArgumentException error(String msg) {
        return new IllegalArgumentException(pos + ": " + msg);
    }
}
