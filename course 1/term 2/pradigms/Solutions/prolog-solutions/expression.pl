%===========КОД С ЛЕКЦИЙ================
:- load_library('alice.tuprolog.lib.DCGLibrary').

lookup(K, [(K, V) | _], V).
lookup(K, [_ | T], V) :- lookup(K, T, V).

expr_term(variable(Name), Name) :-  atom(Name).
expr_term(const(Value), Value) :- number(Value).
expr_term(operation(Op, A, B), R) :- R =.. [Op, AT, BT], expr_term(A, AT), expr_term(B, BT).

expr_text(E, S) :- ground(E), expr_term(E, T), text_term(S, T).
expr_text(E, S) :-   atom(S), text_term(S, T), expr_term(E, T).

all_member([], _).
all_member([H | T], Values) :- member(H, Values), all_member(T, Values).

nonvar(V, T) :- var(V).
nonvar(V, T) :- nonvar(V), call(T).

skip_ws([H|T], A, R, P, F) :-
    H = ' ',!,
    skip_ws(T, A, R, H, F).

skip_ws([H|T], A, R, P, F) :- H = ')', !, append(A, [H], A1), skip_ws(T, A1, R, H, 1).
skip_ws([H|T], A, R, P, F) :- H = '(', P = ' ', F = 1, !, append(A, [' ', H], A1), skip_ws(T, A1, R, H, 0).
skip_ws([H|T], A, R, P, F) :- H = '(', not(P = ' '), !, append(A, [H], A1), skip_ws(T, A1, R, H, 0).
skip_ws([H|T], A, R, P, F) :- H = '(', P = ' ', F = 0, !, append(A, [H], A1), skip_ws(T, A1, R, H, 0).
skip_ws([H|T], A, R, P, F) :- not(H = ' '), P = ' ', F =:= 1, !, append(A, [' ', H], A1), skip_ws(T, A1, R, H, 1).
skip_ws([H|T], A, R, P, F) :- not(H = ' '), P = ' ', F =:= 0, !, append(A, [H], A1), skip_ws(T, A1, R, H, 1).
skip_ws([H|T], A, R, P, F) :- not(H = ' '), not(P = ' '), !, append(A, [H], A1), skip_ws(T, A1, R, H, 1).


skip_ws([], A, R, P, F) :- R = A, !.




expr_p(variable(Name)) --> [Name], { member(Name, [x, y, z])}.


chars_p([]) --> [].
chars_p([H|T]) -->
    {member(H, ['x', 'X', 'y', 'Y', 'z', 'Z'])},
    [H], chars_p(T).

expr_p(const(Value)) -->
    {nonvar(Value, number_chars(Value, Chars))},
    digits_p(Chars),
    {Chars = [H | T], H = '-', not(T = [])},
    {number_chars(Value, Chars)}.

expr_p(const(Value)) -->
    {nonvar(Value, number_chars(Value, Chars))},
    digits_p(Chars),
    {Chars = [H | T], not(H = '-')},
    {number_chars(Value, Chars)}.

digits_p([]) --> [].
digits_p([H|T]) -->
    {member(H, ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', '-'])},
    [H], digits_p(T).

operation(op_add, A, B, R) :- R is A + B.
operation(op_subtract, A, B, R) :- R is A - B.
operation(op_multiply, A, B, R) :- R is A * B.
operation(op_divide, A, B, R) :- R is A / B.
operation(op_negate, A, R) :- R is -A.
operation(op_square, A, R) :- R is A * A.
operation(op_sqrt, A, R) :- R is sqrt(abs(A)).


evaluate(const(Value), _, Value).
evaluate(variable(Name), Vars, R) :- lookup(Name, Vars, R).
evaluate(operation(Op, A, B), Vars, R) :-
    evaluate(A, Vars, AV),
    evaluate(B, Vars, BV),!,
    operation(Op, AV, BV, R).

evaluate(operation(Op, A), Vars, R) :-
    evaluate(A, Vars, AV),
    operation(Op, AV, R).



op_p(op_add) --> ['+'].
op_p(op_subtract) --> ['-'].
op_p(op_negate) --> ['n', 'e', 'g', 'a', 't', 'e'].
op_p(op_multiply) --> ['*'].
op_p(op_divide) --> ['/'].
op_p(op_square) --> ['s', 'q', 'u', 'a', 'r', 'e'].
op_p(op_sqrt) --> ['s', 'q', 'r', 't'].


expr_p(operation(Op, A)) -->  ['('], op_p(Op), [' '], expr_p(A), [')'].
expr_p(operation(Op, A, B)) --> ['('], op_p(Op), [' '], expr_p(A), [' '], expr_p(B), [')'].

prefix_str(E, A) :- ground(E), phrase(expr_p(E), C), atom_chars(A, C).
prefix_str(E, A) :- atom(A), atom_chars(A,C), skip_ws(C, [], C1, ' ', 0), phrase(expr_p(E), C1).





