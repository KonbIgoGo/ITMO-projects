# BucketStorage STL-Compatible Container (C++)

## 📌 Overview

This project implements a **template-based STL-compatible container** `BucketStorage<T>`, tailored for scenarios involving frequent creation and deletion of fixed-size objects. It is written in **C++20** and does **not use STL containers or classes with non-trivial destructors**.

The container guarantees **O(1)** complexity for insertion, deletion, and iteration over active elements, while preserving **stable memory addresses** for objects throughout their lifetime (unless explicitly shrunk).

## 🛠 Requirements

- Language: **C++20**
- STL usage: ❌ No standard containers or types with non-trivial destructors
- Allocators: ❌ Not allowed (`std::allocator`)
- Copy/move: ✅ Full Rule of Five (via copy-and-swap idiom)
- Exception safety: ✅ All memory errors must throw C++ exceptions
- Iterators: ✅ Must satisfy `BidirectionalIterator` requirements
- Must meet C++ Named Requirements:
  - `DefaultConstructible`
  - `CopyConstructible`
  - `CopyAssignable`
  - `MoveConstructible`
  - `MoveAssignable`
  - `Destructible`

## 📦 Container Design

### Memory Structure

- The container stores **blocks**, each capable of holding multiple objects.
- Each block contains:
  - **Object slots**
  - **Activity markers**: `0` means active, `>0` means free (stores distance to next active)
- If all slots in a block are free → block is deleted.
- If all blocks are full → new block is allocated.

### Stability Guarantee

All object pointers and iterators remain valid **unless**:
- The object is explicitly erased
- `clear()` or `operator=` is called
- `shrink_to_fit()` is called and `capacity() != size()`

## ⚙️ Complexity

| Operation                         | Complexity |
|----------------------------------|------------|
| Insert                           | `O(1)`     |
| Erase                            | `O(1)`     |
| Next/Prev iterator traversal     | `O(1)`     |

## 🔧 Constructors

- `BucketStorage()` — Default constructor
- Copy constructor
- Move constructor
- Copy assignment
- Move assignment
- `explicit BucketStorage(size_type block_capacity)` — Sets per-block capacity (default: 64)

## 🔁 Member Functions

| Function | Description |
|---------|-------------|
| `iterator insert(const value_type& value)` | Insert by copy |
| `iterator insert(value_type&& value)` | Insert by move |
| `iterator erase(const_iterator it)` | Erase element, return iterator to next active (or `end()`) |
| `bool empty()` | Check if container is empty |
| `size_type size()` | Number of active elements |
| `size_type capacity()` | Total slots available without resizing |
| `void shrink_to_fit()` | Reduce capacity to match current size (**may invalidate pointers**) |
| `void clear()` | Destroy all elements and reset size |
| `void swap(BucketStorage& other)` | Swap contents with another container |
| `iterator begin(), const_iterator cbegin()` | Begin iterators |
| `iterator end(), const_iterator cend()` | End iterators |
| `iterator get_to_distance(iterator it, difference_type distance)` | Move iterator forward/back by distance |

## 🔁 Iterator Class

The container defines a **bidirectional iterator** with full support for:

Operators:
- Increment/Decrement: `++`, `--`
- Comparison: `==`, `!=`, `<`, `<=`, `>`, `>=`
- Dereferencing: `*`, `->`
- Assignment: `=`

All iterator operations are `O(1)`.

### 📌 Iterator Invalidation Rules

| Operation        | Iterator Invalidated?      |
|------------------|-----------------------------|
| Read-only access | ❌ Never                    |
| `clear`, `operator=` | ✅ Always                 |
| `shrink_to_fit()` | ✅ If `capacity() != size()` |
| `erase()`         | ✅ Only for erased element   |

## 📚 References

- [Trivial destructor](https://en.cppreference.com/w/cpp/language/destructor#Trivial_destructor)
- [STL Container requirements](https://en.cppreference.com/w/cpp/named_req/Container)
- [Named Requirements: DefaultConstructible](https://en.cppreference.com/w/cpp/named_req/DefaultConstructible)
- [Named Requirements: CopyConstructible](https://en.cppreference.com/w/cpp/named_req/CopyConstructible)
- [Named Requirements: MoveConstructible](https://en.cppreference.com/w/cpp/named_req/MoveConstructible)
- [BidirectionalIterator](https://en.cppreference.com/w/cpp/named_req/BidirectionalIterator)
- [Copy-and-Swap Idiom (StackOverflow)](https://stackoverflow.com/a/3279550)

---

Happy hacking! 🚀
