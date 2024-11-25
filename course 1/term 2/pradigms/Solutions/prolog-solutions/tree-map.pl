node(V, L, R, H).
get_pair(node(Value, _, _, _), Value).
get_left(node(_, Left, _, _), Left).
get_right(node(_, _, Right, _), Right).
get_height(node(_, _, _, Height), Height).

empty(L) :- length(L, Len), Len =:= 0.

par_ch_data(H1, H2, Ch, Par) :- get_height(H1, Ch), get_height(H2, Par).
par_to_update(H, Val, Left) :- get_pair(H, Val), get_left(H, Left).

fold_stack([H1, H2|S], S1) :-
    par_ch_data(H1, H2, Ch, Par),
    not(Par =:= Ch + 1),
    !,
    append([H1, H2], S, S1).

fold_stack([H1, H2|S], S1) :-
    par_ch_data(H1, H2, Ch, Par),
    Par =:= Ch + 1,
    !,
    par_to_update(H2, Val, Left),
    append([node(Val, Left, H1, Par)], S, S1).

fold_stack([H1, H2|S], S1) :-
    par_ch_data(H1, H2, Ch, Par),
    Par =:= Ch + 1,
    not(empty(S)),
    !,
    par_to_update(H2, Val, Left),
    append([node(Val, Left, H1, Par)], S, A),
    fold_stack(A, S1).

force_fold_stack(S, S1) :-
    length(S, Len), Len =:= 1,
    !,
    append(S, [], S1).

force_fold_stack([H1, H2 | S], S1) :-
    empty(S),
    append([H1, H2], S, SC),
    length(SC, Len),
    Len >= 2,
    !,
    par_to_update(H2, Val, Left),
    get_height(H2, Par),
    append([node(Val, Left, H1, Par)], S, S1).

force_fold_stack([H1, H2 | S], S1) :-
    not(empty(S)), !,
    par_to_update(H2, Val, Left),
    get_height(H2, Par),
    append([node(Val, Left, H1, Par)], S, A),
    force_fold_stack(A, S1).

check_neccecarity_of_fold(S, S1) :-
    length(S, Len), Len >= 2, !,
    fold_stack(S, S1).

check_neccecarity_of_fold(S, S1) :-
    length(S, Len), not(Len >= 2), !,
    append([], S, S1).

map_build_right([H | R], T, [N | S]) :-
    get_height(N, PH),
    PH =:= 2,!,
    par_to_update(N, Val, Left),
    append([node(Val, Left, node(H, -1, -1, 1), PH)], S, S1),
    check_neccecarity_of_fold(S1, S2),
    map_build_left(R, T, S2).

map_build_right([H | R], T, [N | S]) :-
    get_height(N, PH),
    PH > 2,!,
    append([node(H, -1, -1, 1), N], S, S1),
    check_neccecarity_of_fold(S1, S2),
    map_build_left(R, T, S2).

build_left_data(H, N, S, S1) :-
    get_height(N, Height),
    Height1 is Height + 1,
    append([node(H, N, -1, Height1)], S, S1).

map_build_left([H | R], T, [N | S]) :-
    empty(R), !,
    build_left_data(H, N, S, S1),
    force_fold_stack(S1, [Res | _]),
    T = Res.

map_build_left(H, T, S) :-
    empty(H), not(empty(S)),!,
    force_fold_stack(S, [Res | _]),
    T = Res.

map_build_left(H, T, [N | S]) :-
    empty(H), empty(S),!,
    T = N.

map_build_left([H | R], T, [N | S]) :-
    not(empty(R)), !,
    build_left_data(H, N, S, S1),
    map_build_right(R, T, S1).

map_build(A, V) :- empty(A),!, V = 0.

map_build([H|R], T) :- not(empty(R)),!, map_build_left(R, T, [node(H, -1, -1, 1)]).

map_build([H|R], T) :- empty(R),!, T = node(H, -1, -1, 1).

map_get(T, K, V) :-
    get_pair(T, (K1, V1)),
    K =:= K1, !, V = V1.

map_get(T, K, V) :-
    get_pair(T, (K1, V1)),
    not(K =:= K1), K1 > K, !,
    get_left(T, T1),
    map_get(T1, K, V).

map_get(T, K, V) :-
    get_pair(T, (K1, V1)),
    not(K =:= K1), K1 < K, !,
    get_right(T, T1),
    map_get(T1, K, V).

map_lastEntry(M, P) :-
    get_right(M, R),
    not(R is -1),
    map_lastEntry(R, P).

map_lastEntry(M, P) :-
    get_right(M, R),
    R is -1,
    get_pair(M, P).

map_lastKey(M, K) :- map_lastEntry(M, (K, _)).

map_lastValue(M, V) :- map_lastEntry(M, (_, V)).