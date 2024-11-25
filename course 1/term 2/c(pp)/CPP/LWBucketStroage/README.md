## Требования к оформлению класса

Сначала описываются интерфейсы шаблонных классов, затем – реализация их методов.

Пример оформления:

```cpp
// ------------------------------------
// START OF INTERFACE
// ------------------------------------

template< typename T >
class BucketStorage
{
  public:
    BucketStorage();
    BucketStorage(const BucketStorage &other);
    BucketStorage(BucketStorage &&other);

    ~BucketStorage();

  private:
};

// ------------------------------------
// START OF IMPLEMENTATION
// ------------------------------------

template< typename T >
BucketStorage< T >::BucketStorage()
{
}

template< typename T >
BucketStorage< T >::BucketStorage(const BucketStorage &other)
{
}

template< typename T >
BucketStorage< T >::BucketStorage(BucketStorage &&other)
{
}

template< typename T >
BucketStorage< T >::~BucketStorage()
{
}
```
