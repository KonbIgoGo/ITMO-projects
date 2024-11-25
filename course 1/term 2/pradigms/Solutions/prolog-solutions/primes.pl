get_sqrt(A, B) :- B is round(sqrt(A)).

init(N) :- get_sqrt(N, N1), assert(border(A) :- A is N1).

division(N, B, C) :- C =:= B, !.
division(N, B, C) :- C =:= N, !.
division(N, B, C) :-
     C =< B, !,
     not(0 is mod(N, C)),
     C1 is C + 1,
     division(N, B, C1).

prime(N) :- border(B), division(N, B, 2).

prime(1).
composite(N) :- \+ prime(N).

prime_divisors(H, [], C) :- C > H.

prime_divisors(H, V, C) :-
    not(0 is mod(H, C)), !,
    C1 is C + 1,
    prime_divisors(H, V, C1).

prime_divisors(H, V, C) :-
    not(prime(C)), !,
    C1 is C + 1,
    prime_divisors(H, V, C1).

prime_divisors(H, [C | T], C) :-
    prime(C),
    0 is mod(H, C), !,
    H1 is H / C,
    prime_divisors(H1, T, C).

prime_divisors(H, [C | T], C) :-
    prime(C),
    0 is mod(H, C), !,
    H1 is H / C,
    C1 is C + 1,
    prime_divisors(H1, T, C1).

prime_divisors(0, []) :- !.
prime_divisors(1, []) :- !.

prime_divisors(H, V) :-
    prime(H),!,
    append([], [H], V).
prime_divisors(H, V) :-
    prime_divisors(H, V, 2), !.

nth_prime(N, P, C, PC) :- C =:= N, prime(PC), !, P is PC.
nth_prime(N, P, C, PC) :-
    prime(PC), !,
    P1 is PC + 1,
    C1 is C + 1,
    nth_prime(N, P, C1, P1).

nth_prime(N, P, C, PC) :-
    not(prime(PC)), !,
    P1 is PC + 1,
    nth_prime(N, P, C, P1).

nth_prime(N, P) :- nth_prime(N, P, 1, 2).