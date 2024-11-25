package expression.parser;

import expression.*;
import expression.exceptions.*;

import java.util.*;

public class ExpressionParser implements TripleListParser {


    @Override
    public TripleExpression parse(String expression) throws ParseException {
        return parse(new StringCharSource(expression));
    }


    public TripleExpression parse(CharSource exp) throws ParseException {
        return new Express(exp).parseExpression();
    }

    @Override
    public ListExpression parse(String expression, List<String> variables) throws Exception {
        return new Express(new StringCharSource(expression), variables).parseExpression();
    }

    private static class Express extends BaseParser {
        HashSet<Character> binOps = new HashSet<>(Arrays.asList(
                '*',
                '/',
                '+',
                '-',
                '|',
                '&',
                '^'
        ));

        HashSet<Character> unOps = new HashSet<>(Arrays.asList(
                'l',
                't',
                '-'
        ));

        Set<Character> opSymbols = new HashSet<>();
        Set<String> vars = new LinkedHashSet<>(Arrays.asList("x", "y", "z"));

        private Express(final CharSource source) {
            super(source);
        }

        public Express(final CharSource source, List<String> vars) {
            super(source);
            this.vars = new LinkedHashSet<>(vars);
            for (String s : vars) {
                for (char ch : s.toCharArray()) {
                    opSymbols.add(ch);
                }
            }
        }

        public UniversalExpression parseExpression() throws ParseException {
            UniversalExpression res = parseOr();
            if (eof()) {
                return res;
            }
            throw new UnknownSymbolException("Unexpected Symbol. Found: " + ch);
        }


        private char defineErrorSymb() {
            if (ch == END) {
                return prev;
            } else {
                return ch;
            }
        }

        private UniversalExpression parseOr() throws ParseException {
            skipWhiteSpace();
            UniversalExpression val = parseXOR();
            while (true) {
                skipWhiteSpace();
                char op = ch;
                if (op == '|') {
                    take();
                    val = new Or(val, parseXOR());
                } else return val;
            }
        }

        private UniversalExpression parseXOR() throws ParseException {
            skipWhiteSpace();
            UniversalExpression val = parseAnd();
            while (true) {
                skipWhiteSpace();
                char op = ch;
                if (op == '^') {
                    take();
                    val = new XOR(val, parseAnd());
                } else {
                    return val;
                }
            }
        }

        private UniversalExpression parseAnd() throws ParseException {
            skipWhiteSpace();
            UniversalExpression val = parseSum();
            while (true) {
                skipWhiteSpace();
                char op = ch;
                if (op == '&') {
                    take();
                    val = new And(val, parseSum());
                } else {
                    return val;
                }
            }
        }

        private UniversalExpression parseSum() throws ParseException {
            skipWhiteSpace();
            UniversalExpression val = parseMult();
            while (true) {
                skipWhiteSpace();
                char op = ch;
                switch (op) {
                    case '+' -> {
                        take();
                        skipWhiteSpace();
                        val = new Add(val, parseMult());
                    }
                    case '-' -> {
                        take();
                        skipWhiteSpace();
                        val = new Subtract(val, parseMult());
                    }
                    default -> {
                        return val;
                    }
                }
            }
        }

        private UniversalExpression parseMult() throws ParseException {
            UniversalExpression val = parseValue();
            while (true) {
                skipWhiteSpace();
                char op = ch;
                if (!binOps.contains(op) && ch != END && ch != ')') {
                    throw new UnknownSymbolException("Operator expected. Found: " + ch);
                }
                switch (op) {
                    case '*' -> {
                        take();
                        val = new Multiply(val, parseValue());
                    }
                    case '/' -> {
                        take();
                        val = new Divide(val, parseValue());
                    }
                    default -> {
                        return val;
                    }
                }
            }
        }

        private UniversalExpression parseValue() throws ParseException {
            skipWhiteSpace();
            char val = ch;
            if (val == '(') {

                return parseBrackets();

            } else if (ch == ')') {

                throw new NoArgumentException("No argument. Found: " + defineErrorSymb());

            } else if (Character.isDigit(val)) {
                try {
                    return new Const(Integer.parseInt(takeNumber()));
                } catch (NumberFormatException e) {
                    throw new NumberOverflowException("Integer overflow");
                }

            } else if (vars.contains(Character.toString(val))) {

                take();
                skipWhiteSpace();
                return new Variable(Character.toString(val));

            } else if (opSymbols.contains(val)) {

                StringBuilder op = new StringBuilder().append(take());
                while (opSymbols.contains(ch)) {
                    op.append(take());
                }

                return new Variable(Arrays.binarySearch(vars.toArray(), op.toString()), op.toString());

            } else if (unOps.contains(val)) {

                take();
                skipWhiteSpace();
                return switch (val) {
                    case '-' -> {
                        if (ch == END) {
                            throw new NoArgumentException("No argument. Found: end of input");
                        }
                        if (ch == '(') {
                            yield new Negate(parseBrackets());
                        } else if (Character.isDigit(ch)) {
                            try {
                                yield new Const(Integer.parseInt('-' + takeNumber()));
                            } catch (NumberFormatException e) {
                                throw new NumberOverflowException("Integer underflow");
                            }

                        }
                        yield new Negate(parseValue());
                    }

                    case 'l' -> {
                        if (take('0')) {
                            if (ch == END) {
                                throw new NoArgumentException("No argument. Found: end of input");
                            }
                            if (!Character.isWhitespace(ch) && ch != '(') {
                                throw new UnknownSymbolException("Unexpected symbol. Found: " + ch);
                            }
                            yield new MajorBit(parseValue());
                        } else {
                            throw new NoArgumentException("No argument. Found: " + val);
                        }
                    }

                    case 't' -> {
                        if (take('0')) {
                            if (ch == END) {
                                throw new NoArgumentException("No argument. Found: end of input");
                            }
                            if (!Character.isWhitespace(ch) && ch != '(') {
                                throw new UnknownSymbolException("Unexpected symbol. Found: " + ch);
                            }
                            yield new MinorBit(parseValue());
                        } else {
                            throw new NoArgumentException("No argument. Found: " + val);
                        }
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + val);
                };

            } else if (binOps.contains(ch)) {
                throw new NoArgumentException("No argument. Found: " + ch);
            } else if (eof()) {
                throw new NoArgumentException("No argument. Found: end of input");
            } else {
                throw new UnknownSymbolException("Unexpected Symbol. Found: " + defineErrorSymb()); // :NOTE: more info
            }
        }

        private UniversalExpression parseBrackets() throws ParseException {
            take();
            UniversalExpression res = parseOr();
            if (take(')')) {
                return res;
            }
            throw new UnknownSymbolException("Unclosed bracket");
        }
    }

}
