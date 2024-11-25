package expression.exceptions;

import expression.*;
import expression.parser.BaseParser;
import expression.parser.CharSource;
import expression.parser.StringCharSource;

import java.math.BigInteger;
import java.util.*;

public class ExpressionParser implements TripleListParser {


    @Override
    public TripleExpression parse(String expression) throws ParseException {
        return parse(new StringCharSource(expression));
    }

    public UniversalExpression parse(String expression, String mode) throws ParseException {
        return parse(new StringCharSource(expression), mode);
    }


    public TripleExpression parse(CharSource exp) throws ParseException {
        return new Express(exp).parseExpression();
    }

    public UniversalExpression parse(CharSource exp, String mode) throws ParseException {
        return new Express(exp, mode).parseExpression();
    }

    @Override
    public ListExpression parse(String expression, List<String> variables) throws Exception {
        return new Express(new StringCharSource(expression), variables).parseExpression();
    }

    private final static class Express extends BaseParser {
        HashSet<Character> binOps = new HashSet<>(Arrays.asList(
                '*',
                '/',
                '+',
                '-',
                '|',
                '&',
                '^'
        ));

        // :NOTE: hashset instead of set
        List<HashSet<Character>> prioritizedOps = new ArrayList<>(List.of(
                new HashSet<>(Arrays.asList('*', '/')),
                new HashSet<>(Arrays.asList('+', '-')),
                new HashSet<>(List.of('|')),
                new HashSet<>(List.of('&')),
                new HashSet<>(List.of('^'))
        ));

        HashSet<Character> unOps = new HashSet<>(Arrays.asList(
                'l',
                't',
                '-'
        ));

        Set<Character> opSymbols = new HashSet<>();
        Set<String> vars = new LinkedHashSet<>(Arrays.asList("x", "y", "z"));

        String mode = "i";

        private Express(final CharSource source) {
            super(source);
        }

        private Express(final CharSource source, List<String> vars) {
            super(source);
            this.vars = new LinkedHashSet<>(vars);
            for (String s : vars) {
                for (char ch : s.toCharArray()) {
                    opSymbols.add(ch);
                }
            }
        }

        private Express(final CharSource source, String mode) {
            super(source);
            this.mode = mode;
        }


        public UniversalExpression parseExpression() throws ParseException {
            UniversalExpression res = parseOps(5);
            if (eof()) {
                return res;
            }
            throw new UnknownSymbolException("Unexpected Symbol. Found: " + ch + " pos: " + id);
        }


        private char defineErrorSymb() {
            if (ch == END) {
                return prev;
            } else {
                return ch;
            }
        }


        private UniversalExpression parseOps(int priorityRestriction) throws ParseException {

            UniversalExpression res = parseValue();

            outer: for (int i = 0; i < prioritizedOps.size(); i++) {
                if (priorityRestriction < i) {
                    return res;
                }
                skipWhiteSpace();
                char currOp = ch;
                if (!binOps.contains(currOp) && ch != END && ch != ')') {
                    throw new UnknownSymbolException("Operator expected. Found: " + ch + " pos: " + id);
                }
                while (true) {
                    skipWhiteSpace();
                    currOp = ch;
                    if (prioritizedOps.get(i).contains(currOp)) {
                        take();
                        UniversalExpression res2 = parseOps(i -1);
                        switch (currOp) {
                            case '*' -> res = new CheckedMultiply(res, res2);
                            case '/' -> res = new CheckedDivide(res, res2);
                            case '+' -> res = new CheckedAdd(res, res2);
                            case '-' -> res = new CheckedSubtract(res, res2);
                            case '|' -> res = new Or(res, res2);
                            case '&' -> res = new And(res, res2);
                            case '^' -> res = new XOR(res, res2);
                        }
                    } else {
                        break;
                    }
                    skipWhiteSpace();
                    if (ch == END) {
                        break outer;
                    }
                }
            }
            return res;
        }
        private UniversalExpression parseValue() throws ParseException {
            skipWhiteSpace();
            char val = ch;
            if (val == '(') {

                return parseBrackets();

            } else if (ch == ')') {

                throw new NoArgumentException("No argument. Found: " + defineErrorSymb() + " pos: " + id);

            } else if (Character.isDigit(val)) {
                try {
                    return switch (mode) {
                        case "d" -> new Const(Double.parseDouble(takeDouble()));
                        case "bi" -> new Const(new BigInteger(takeNumber()));
                        default -> new Const(Integer.parseInt(takeNumber()));
                    };
                } catch (NumberFormatException e) {
                    if (Objects.equals(mode, "i")) {
                        throw new NumberOverflowException("Integer overflow");
                    } else {
                        throw new UnknownSymbolException("Unknown Symbol");
                    }
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
                            yield new CheckedNegate(parseBrackets());
                        } else if (Character.isDigit(ch)) {
                            try {
                                yield switch (mode) {
                                    case "d" -> new Const(Double.parseDouble("-" + takeDouble()));
                                    case "bi" -> new Const(new BigInteger("-" + takeNumber()));
                                    default -> new Const(Integer.parseInt("-" + takeNumber()));
                                };
                            } catch (NumberFormatException e) {
                                if (Objects.equals(mode, "i")) {
                                    throw new NumberOverflowException("Integer underflow");
                                } else {
                                    throw new UnknownSymbolException("Unknown Symbol");
                                }
                            }

                        }
                        yield new CheckedNegate(parseValue());
                    }

                    case 'l' -> {
                        if (take('0')) {
                            if (ch == END) {
                                throw new NoArgumentException("No argument. Found: end of input");
                            }
                            if (!Character.isWhitespace(ch) && ch != '(') {
                                throw new UnknownSymbolException("Unexpected symbol. Found: " + ch + " pos: " + id);
                            }
                            yield new MajorBit(parseValue());
                        } else {
                            throw new NoArgumentException("No argument. Found: " + val + " pos: " + id);
                        }
                    }

                    case 't' -> {
                        if (take('0')) {
                            if (ch == END) {
                                throw new NoArgumentException("No argument. Found: end of input");
                            }
                            if (!Character.isWhitespace(ch) && ch != '(') {
                                throw new UnknownSymbolException("Unexpected symbol. Found: " + ch + " pos: " + id);
                            }
                            yield new MinorBit(parseValue());
                        } else {
                            throw new NoArgumentException("No argument. Found: " + val + " pos: " + id);
                        }
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + val + " pos: " + id);
                };

            } else if (binOps.contains(ch)) {
                throw new NoArgumentException("No argument. Found: " + ch + " pos: " + id);
            } else if (eof()) {
                throw new NoArgumentException("No argument. Found: end of input");
            } else {
                throw new UnknownSymbolException("Unexpected Symbol. Found: " + defineErrorSymb() + " pos: " + id);
            }
        }

        private UniversalExpression parseBrackets() throws ParseException {
            take();
            UniversalExpression res = parseOps(5);
            if (take(')')) {
                return res;
            }
            throw new UnknownSymbolException("Unclosed bracket " + " pos: " + id);
        }
    }

}
