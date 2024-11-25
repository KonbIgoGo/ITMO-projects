package expression.parser;

public class BaseParser {

    public static final char END = 0;
    protected CharSource source;

    protected char ch;
    protected int id = 0;
    protected char prev;
    public BaseParser(CharSource source) {
        this.source = source;
        take();
    }

    protected char take() {
        id += 1;
        final char res = ch;
        prev = ch;
        ch = source.hasNext() ? source.next() : END;
        return res;
    }

    protected boolean take(char expect) {
        return take() == expect;
    }

    protected boolean take(String expect) {
        char[] exp = expect.toCharArray();
        for (int i = 0; i < expect.length(); i++) {
            if (!take(exp[i])) {
                return false;
            }
        }
        return true;
    }


    protected String takeNumber() {
        StringBuilder sb = new StringBuilder();
        while (Character.isDigit(ch)) {
            sb.append(take());
        }
        return sb.toString();
    }

    protected String takeDouble() {
        StringBuilder sb = new StringBuilder();
        while (Character.isDigit(ch) || ch == '.') {
            sb.append(take());
        }
        return sb.toString();
    }
    public void skipWhiteSpace() {
        while (Character.isWhitespace(ch)) {
            take();
        }
    }

    protected boolean eof() {
        return ch == END;
    }

    protected IllegalArgumentException error(String msg) {
        return source.error(msg);
    }
}
