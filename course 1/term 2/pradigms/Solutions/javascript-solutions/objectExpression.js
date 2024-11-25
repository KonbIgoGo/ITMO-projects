let ElementPrototype = {
    toString() {
        return "".concat(this.val);
    },
    prefix() {
        return this.toString()
    }
}

const varlist = ["x", "y", "z"];

const abstractElement = (evaluate) => {
    function elem(val) {
        this.val = val;
    }

    elem.prototype = Object.create(ElementPrototype);
    elem.prototype.evaluate = evaluate;
    return elem;
}

const Const = abstractElement(function () {
    return this.val
});
const Variable = abstractElement(function (...args) {
    return args[varlist.indexOf(this.val)]
});

let OpPrototype = {
    toString() {return `${this.val.map(e => e.toString()).join(" ")} ${this.op}`},
    prefix() {return `(${this.op} ${this.val.map(e => e.prefix()).join(" ")})`}
}

const operations = new Map([])

const makeOp = (evaluate, op) => {
    function operation(...val) {
        this.val = val
    }
    operation.prototype = Object.create(OpPrototype);
    operation.prototype.op = op;
    operation.prototype.evaluate = function (x, y, z) {
        return evaluate(...this.val.map(i => i.evaluate(x, y, z)))
    };
    if (!operations.has(op)) {
        operations.set(op, {"val": (...args) => new operation(...args), "arity": evaluate.length})
    }
    // :NOTE: error
    return operation;
}

const Add = makeOp((a, b) => a + b, "+")
const Subtract = makeOp((a, b) => a - b, "-")
const Multiply = makeOp((a, b) => a * b, "*")
const Divide = makeOp((a, b) => a / b, "/")

const Negate = makeOp((a) => -(a), "negate")
const Sin = makeOp((a) => Math.sin(a), "sin")
const Cos = makeOp((a) => Math.cos(a), "cos")

const Mean = makeOp((...args) => args.reduce((sum, i) => sum + i, 0) / args.length, "mean")
const Var = makeOp((...args) => {
    return (args.reduce((sum, i) => sum + (i * i), 0) / args.length) - ((args.reduce((sum, i) => sum + i, 0) / args.length) ** 2)
}, "var");

// ==============PARSER=================

const takeNumber = (expr) => {
    let num = ""
    while (Number.isInteger(parseInt(expr[pointer]))) {
        num += expr[pointer++];
    }
    return parseInt(num)
}

const take = (str, expr) => {
    let res = " ";
    for (let i = 0; i < str.length; i++) {
        res += expr[pointer++];
    }
    return res === str;

}

const parse = (expr) => {
    let args = [];
    expr.split(/\s+/).filter((token) => {
        const num = parseInt(token);

        if (operations.has(token)) {
            const op = operations.get(token).val(...args.slice(args.length - operations.get(token).arity));
            args = args.slice(0, args.length - operations.get(token).arity)
            args.push(op)
        } else if (varlist.includes(token)) {
            args.push(new Variable(token));
        } else if (Number.isInteger(num)) {
            args.push(new Const(num));
        }
    })
    return args.pop();
}


//===============PREFIX PARSER=======================


const errorConstructor = (name) => {
    function err(msg) {
        this.message = msg;
    }
    err.prototype = Object.create(Error);
    Object.defineProperties(err.prototype, {
        name: {
            writable: true
        }
    });
    err.prototype.name = name
    return err;
}

const ParseError = errorConstructor("ParseError");
const NoArgumentError = errorConstructor("NoArgumentError");
const NoOperationError = errorConstructor("NoOperationError");
const UnexpectedTokenError = errorConstructor("UnexpectedTokenError");


const parsePrefix = (expr) => {
    let idx = 0;

    //parentheses balance encounting
    if ((expr.match(/\(/g) || expr.match(/\)/g)) && expr.match(/\(/g).length !== expr.match(/\)/g).length) {
        throw new ParseError("Wrong parentheses balance");
    }
    expr = expr.replaceAll("(", " ( ");
    expr = expr.replaceAll(")", " ) ");

    const tokens = expr.split(/\s+/).filter(token => {
        if (token) {
            return token;
        }
    })

    const parse = () => {

        let ops = []
        let args = [];
        let isSingle = true;
        while (true) {
            while (tokens[idx] === "(") {
                idx++;
                args.push(parse())
            }
            if (operations.has(tokens[idx])) {
                ops.push(tokens[idx]);
                isSingle = false;
            } else {
                const num = parseInt(tokens[idx]);
                if (Number.isInteger(num)) {
                    if (num.toString() !== tokens[idx]) {
                        throw new UnexpectedTokenError(`Unexpected token ${tokens[idx]} on pos ${idx}}`);
                    }
                    args.push(new Const(num));
                    // :NOTE: support more variable names
                } else if (varlist.includes(tokens[idx])) {
                    args.push(new Variable(tokens[idx]));
                } else {
                    if (tokens[idx] !== ")" && idx < tokens.length) {
                        throw new UnexpectedTokenError(`Unexpected token ${tokens[idx]} on pos ${idx}}`);
                    } else {
                        // :NOTE: ===
                        if (tokens[idx] === ')' && isSingle) {
                            throw new NoOperationError(`No operation on pos ${tokens[idx]}`);
                        }
                        idx++;

                        if (ops.length > 0) {
                            const op = ops.pop()
                            if (operations.get(op).arity === 0 && args.length !== 0 || operations.get(op).arity === args.length) {
                                let res;
                                if (operations.get(op).arity === 0) {
                                    res = operations.get(op).val(...args);
                                    args = [];
                                } else {
                                    res = operations.get(op).val(...args.slice(args.length - operations.get(op).arity));
                                    args = args.slice(0, args.length - operations.get(op).arity)
                                }
                                args.push(res)
                            } else {
                                throw new NoArgumentError(`Argument expected on pos ${idx}`);
                            }
                        }
                        if (args.length > 1 || (args.length === 0 && ops.length === 0)) {
                            throw new NoOperationError(`Operator expected on pos ${idx}`)
                        }

                        if (ops.length > 0) {
                            throw new NoArgumentError(`Argument expected on pos ${idx}`)
                        }

                        return args.pop();
                    }
                }
            }
            idx++;
        }
    }
    return parse();
}