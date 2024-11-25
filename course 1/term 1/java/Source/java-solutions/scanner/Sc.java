package scanner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class Sc {
    private final InputStream source;
    private int readLineProgress = 0;
    private int readedLen = -2;
    private final char[] cbuf = new char[1024];
    private final InputStreamReader reader;

    
    public Sc(final InputStream source) {
        this.source = source;
        this.reader = new InputStreamReader(source, StandardCharsets.UTF_8);
    }

    public Sc(final String str) {
        this.source = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        this.reader = new InputStreamReader(source, StandardCharsets.UTF_8);
    }

    private void checkCbufExist() {
        if (readLineProgress >= readedLen || readedLen == -2) {
            try {
                readedLen = reader.read(cbuf);
                readLineProgress = 0;
            } catch (IOException e) {
                System.out.println("Can not read: " + e);
            }
        }
    }

    private void checkCbufOverflow() {
        if (readLineProgress >= readedLen) {
            try {
                readedLen = reader.read(cbuf);
                readLineProgress = 0;
            } catch (IOException e) {
                System.out.println("Can not read: " + e);
            }
        }
    }

    public String next() {
        StringBuilder next = new StringBuilder();
        checkCbufExist();

        while (true) {
            checkCbufOverflow();
            if (readedLen == -1) {
                break;
            }
            if (Character.isWhitespace(cbuf[readLineProgress])) {
                if (!next.isEmpty()) {
                    break;
                } else {
                    readLineProgress++;
                    continue;
                }
            }
            next.append(cbuf[readLineProgress]);
            readLineProgress++;
        }
        
        return next.toString();
    }

    public List<String> nextToTheEndOfLine() {
        checkCbufExist();
        List<String> words = new ArrayList<>();
        int countSep = 0;
        while (true) {
            checkCbufOverflow();
            if (readedLen == -1) {
                return words;
            }
            outer:while (Character.isWhitespace(cbuf[readLineProgress])) {
                if (readedLen == -1) {
                    return words;
                }
                for (char c : System.lineSeparator().toCharArray()) {
                    if (cbuf[readLineProgress] == c) {
                        countSep++;
                        readLineProgress++;
                        if (countSep == System.lineSeparator().length()) {
                            if (words.isEmpty()) {
                                words.add("");
                                return words;
                            }
                            return words;
                        }
                        checkCbufOverflow();
                    }
                }
                readLineProgress++;
                checkCbufOverflow();
            }

            words.add(next());
        }
    }

    public int nextInt() {
        return Integer.parseInt(next());
    }

    public String nextLine() {
        StringBuilder line = new StringBuilder();
        checkCbufExist();

        while (true) {
            int countSep = 0;
            checkCbufOverflow();
            if (readedLen == -1) {
                return line.toString();
            }

            for (char c : System.lineSeparator().toCharArray()) {
                checkCbufOverflow();
                if ((int) cbuf[readLineProgress] == (int) c) {
                    countSep++;
                    readLineProgress++;
                    if (countSep == System.lineSeparator().length()) {
                        return line.toString();
                    }
                }
            }
            line.append(cbuf[readLineProgress]);
            readLineProgress++;

        }
    }
    
    public Boolean hasNextLine() {
        checkCbufExist();

        return readedLen != -1;
    }

    public Boolean hasNext() {
        checkCbufExist();

        while (readedLen != -1) {

            for (int i = readLineProgress; i < readedLen; i++) {
                if (!Character.isWhitespace(cbuf[i])) {
                    return true;
                }
            }

            try {
                readedLen = reader.read(cbuf);
            } catch (IOException ignored) {

            }
            readLineProgress = 0;
        }

        return false;
    }

    public void close() {
        try {
            this.reader.close();
        } catch (IOException e) {
            System.out.println("Can not close reader: " + e);
        }
    }

}
