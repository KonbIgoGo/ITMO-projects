package queue;

// Model: a[1]..a[n]
// Inv: n >= 0 && forall i=1..n: a[i] != null
// Let: immutable(k): forall i=1..k: a'[i] = a[i]
public interface Queue {

    // :NOTE:4 почему и вставка, и удаление происходит из головы?
    // Pre: element != null
    // Post: n' = n + 1 &&
    //       a'[n] = element &&
    //       immutable(n)
    void enqueue(Object element);

    // Pre: n > 0
    // Post: R = a[1] && n' = n - 1 && immutable(n')
    Object dequeue();

    // Pre: n > 0
    // Post: R = a[1] && n' = n && immutable(n)
    Object element();

    // Pre: true
    // Post: n' = 0;
    void clear();

    // Pre: true
    // Post: R = n && n' = n && immutable(n)
    int size();

    // Pre: true
    // Post: R = (n = 0) && n' = n && immutable(n)
    boolean isEmpty();

    // Pre: n != null
    // let noFollowingDoubles = forall i, a[i] != a[i+1]
    // let immutableOrder = forall a[i] != a[i+1], [a[1], ..., a[i], a[i+1], ... ,a[n']]
    // Post: n'.len <= n.len && noFollowingDoubles(n') && immutableOrder(n, n') && n' is subSet of n &&
    // && ((n.len > 0 && n'.len > 0) || (n.len == 0 && n'len == 0)) &&
    // :NOTE: queue = [1,3,3,4]; dedup(); queue = [1,3];
    void dedup();
}
