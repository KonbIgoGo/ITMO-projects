"use strict";

const cnst = (val) => () => {
    return val;
}

const variable = (val) => (x, y, z) => {
    switch (val) {
        // :NOTE: Не расширяемо
        case "x":
            return x;
        case "y":
            return y;
        case "z":
            return z;

        default:
            break;
    }
}

const bin = (f) => (arg1, arg2) => (x, y, z) => {
    return f(arg1(x, y, z), arg2(x, y, z));
}
const unary = (f) => (arg) => (x, y, z) => {
    return f(arg(x, y, z))
}

const pi = (x, y, z) => cnst(Math.PI)(x, y, z);
const e = (x, y, z) => cnst(Math.E)(x, y, z);

const multiply = bin((a, b) => a * b);
const divide = bin((a, b) => a / b);
const add = bin((a, b) => a + b);
const subtract = bin((a, b) => a - b);
const negate = unary((arg) => -arg)
const sqrt = unary(Math.sqrt); // :NOTE: Math.pow()
const square = unary((arg) => arg ** 2); // :NOTE: Math.pow



// console.log(parse("x y 0 max3 x y 1 max3 x y + y z - z x * max3 max3")(0,0,0))
