#pragma once

#include <iterator>
#include <utility>

namespace utils
{
	template< typename T >
	class Node
	{
		using value_type = T;
		using size_type = size_t;

		Node< T > *next = nullptr;
		Node< T > *prev = nullptr;

		Node< T > *next_f = nullptr;
		Node< T > *prev_f = nullptr;

		value_type elem;
		size_type idx = 0;

	  public:
		void set_idx(size_type id) noexcept { idx = id; }
		void set_next(Node< T > *nxt) noexcept { next = nxt; };
		void set_prev(Node< T > *prv) noexcept { prev = prv; };
		void set_elem(value_type &&el) noexcept { elem = std::forward< T >(el); }
		void set_next_f(Node< T > *nxt) noexcept { next_f = nxt; };
		void set_prev_f(Node< T > *prv) noexcept { prev_f = prv; };

		Node< T > *get_next_f() const noexcept { return next_f; };
		Node< T > *get_prev_f() const noexcept { return prev_f; };
		Node< T > *get_next() const noexcept { return next; };
		Node< T > *get_prev() const noexcept { return prev; };
		value_type *get_elem() noexcept { return &elem; };
		size_type get_idx() const noexcept { return idx; };

		Node(const Node< T > &other) : elem(other.elem){};

		Node(const value_type &el, size_type id) : elem(el), idx(id){};
		Node(value_type &&el, size_type id) : elem{ std::forward< T >(el) }, idx(id){};

		Node(){};
	};

	template< typename T >
	class Block
	{
		using value_type = T;
		using size_type = size_t;

		Node< T > *tail = nullptr;
		Node< T > *head = nullptr;

		size_type size = 0;
		size_type capacity = 0;

	  public:
		size_type get_capacity() noexcept { return capacity; }
		size_type get_size() noexcept { return size; }

		Node< T > *begin() noexcept { return head; };
		Node< T > *end() noexcept { return tail; };
		template< typename Y >
		Node< T > *insert(Y &&elem);
		void erase(Node< T > *elem);

		bool full() noexcept { return size >= capacity; }
		bool empty() noexcept { return size == 0; }

		Block(size_type sz) : capacity(sz){};
		Block() : capacity(64){};
		~Block()
		{
			while (head != nullptr)
			{
				Node< T > *nxt = head->get_next();
				delete head;
				head = nxt;
			}
		};
	};

	template< typename T >
	template< typename Y >
	Node< T > *Block< T >::insert(Y &&elem)
	{
		size++;
		Node< T > *to_return;
		if (head == nullptr)
		{
			to_return = new Node< T >(std::forward< Y >(elem), 1);
			head = tail = to_return;
		}
		else
		{
			to_return = new Node< T >(std::forward< Y >(elem), tail->get_idx() + 1);
			tail->set_next(to_return);
			to_return->set_prev(tail);
			tail = to_return;
		}
		return to_return;
	}

	template< typename T >
	void Block< T >::erase(Node< T > *elem)
	{
		if (elem == head)
		{
			head = head->get_next();
			delete head->get_prev();
			head->set_prev(nullptr);
		}
		else
		{
			if (elem->get_next() != nullptr)
			{
				elem->get_next()->set_prev(elem->get_prev());
			}
			if (elem->get_prev() != nullptr)
			{
				elem->get_prev()->set_next(elem->get_next());
			}

			delete elem;
		}

		size--;
	}
}	 // namespace utils

template< typename T >
class BucketStorage
{
	template< typename T1, bool isConst >
	class base_iterator
	{
		utils::Node< utils::Block< T1 > > *n_ptr = nullptr;
		utils::Node< T1 > *m_ptr = nullptr;

	  public:
		using value_type = T1;
		using size_type = size_t;
		using pointer = T1 *;
		using difference_type = std::ptrdiff_t;
		using reference = T1 &;
		using const_reference = const T1 &;
		using iterator_category = std::bidirectional_iterator_tag;
		using const_pointer = const T1 *;

		reference operator*() const { return *(m_ptr->get_elem()); }
		pointer operator->() { return m_ptr->get_elem(); }

		utils::Node< T1 > *get_ptr() const { return m_ptr; }
		const utils::Node< T1 > *get_const_ptr() const { return m_ptr; }

		utils::Node< utils::Block< T1 > > *get_n_ptr() const { return n_ptr; }

		base_iterator &operator++()
		{
			if (m_ptr->get_next() == nullptr)
			{
				n_ptr = n_ptr->get_next();
				if (n_ptr->get_elem() != nullptr)
				{
					m_ptr = n_ptr->get_elem()->begin();
				}
			}
			else
			{
				m_ptr = m_ptr->get_next();
			}
			return *this;
		}

		base_iterator &operator--()
		{
			if (n_ptr->get_idx() == 0 || m_ptr->get_prev() == nullptr)
			{
				n_ptr = n_ptr->get_prev();
				m_ptr = n_ptr->get_elem()->end();
				return *this;
			}
			m_ptr = m_ptr->get_prev();
			return *this;
		}

		base_iterator operator++(int)
		{
			base_iterator it = base_iterator(n_ptr, m_ptr);
			++(*this);
			return it;
		}
		base_iterator operator--(int)
		{
			base_iterator it = base_iterator(n_ptr, m_ptr);
			--(*this);
			return it;
		}

		friend bool operator==(const base_iterator &a, const base_iterator &b) noexcept
		{
			return a.get_ptr() == b.get_ptr();
		}
		friend bool operator==(const base_iterator< T1, isConst > &a, const base_iterator< T1, !isConst > &b) noexcept
		{
			return a.get_ptr() == b.get_ptr();
		}
		friend bool operator!=(const base_iterator &a, const base_iterator &b) noexcept { return not(a == b); }
		friend bool operator!=(const base_iterator< T1, isConst > &a, const base_iterator< T1, !isConst > &b) noexcept
		{
			return not(a.get_ptr() != b.get_ptr());
		}
		friend bool operator>(const base_iterator &a, const base_iterator &b) noexcept
		{
			if (a.get_n_ptr()->get_idx() == 0 && b.get_n_ptr()->get_idx())
			{
				return true;
			}

			if ((a.get_n_ptr()->get_idx() != 0 && b.get_n_ptr()->get_idx() == 0) ||
				(a.get_n_ptr()->get_idx() == 0 && b.get_n_ptr()->get_idx() == 0))
			{
				return false;
			}

			if (a.get_n_ptr()->get_idx() > b.get_n_ptr()->get_idx())
			{
				return true;
			}
			else if (a.get_n_ptr()->get_idx() == b.get_n_ptr()->get_idx())
			{
				return a.get_ptr()->get_idx() > b.get_ptr()->get_idx();
			}
			return false;
		}
		friend bool operator>=(const base_iterator &a, const base_iterator &b) noexcept
		{
			return ((a == b) || (a > b));
		}
		friend bool operator<(const base_iterator &a, const base_iterator &b) noexcept { return not(a >= b); }
		friend bool operator<=(const base_iterator &a, const base_iterator &b) noexcept { return not(a > b); }

		operator base_iterator< T1, !isConst >() { return base_iterator< T1, !isConst >(n_ptr, m_ptr); }

		base_iterator(utils::Node< utils::Block< T1 > > *node_ptr)
		{
			n_ptr = node_ptr;
			m_ptr = n_ptr->get_elem()->begin();
		}

		base_iterator(utils::Node< utils::Block< T1 > > *node_ptr, utils::Node< T1 > *m_ptr)
		{
			n_ptr = node_ptr;
			this->m_ptr = m_ptr;
		}
		base_iterator(const base_iterator< T1, isConst > &iterator) = default;
		base_iterator(){};
	};

  public:
	using value_type = T;
	using size_type = size_t;
	using pointer = T *;
	using difference_type = std::ptrdiff_t;
	using reference = T &;
	using const_reference = const T &;
	using iterator_category = std::bidirectional_iterator_tag;
	using const_pointer = const T *;

	using const_iterator = base_iterator< T, true >;
	using iterator = base_iterator< T, false >;

	bool empty() const noexcept { return total_size == 0; };
	size_type size() const noexcept { return total_size; }
	size_type capacity() const noexcept { return block_amount * block_capacity; };

	iterator begin() noexcept { return iterator(head); };
	iterator end() noexcept { return iterator(tail); };
	const_iterator begin() const noexcept { return const_iterator(head); };
	const_iterator end() const noexcept { return const_iterator(tail); };
	const_iterator cbegin() const noexcept { return const_iterator(head); };
	const_iterator cend() const noexcept { return const_iterator(tail); };

	iterator insert(value_type &&elem) { return insert_impl(std::move(elem)); };
	iterator insert(const value_type &elem) { return insert_impl(elem); };
	iterator erase(const_iterator it);
	iterator erase(iterator it);
	iterator get_to_distance(iterator it, const difference_type distance);
	void shrink_to_fit();
	void clear();
	void swap(BucketStorage< T > &other) noexcept
	{
		using std::swap;
		swap(head, other.head);
		swap(tail, other.tail);
		swap(head_free, other.head_free);
		swap(tail_free, other.tail_free);
		swap(total_size, other.total_size);
		swap(block_capacity, other.block_capacity);
		swap(block_amount, other.block_amount);
	};
	friend void swap(BucketStorage< T > &a, BucketStorage< T > &b) noexcept { a.swap(b); };

	BucketStorage< T > &operator=(const BucketStorage< T > &other)
	{
		if (this == &other)
		{
			return *this;
		}

		BucketStorage< T > tmp(other);
		this->swap(tmp);
		return *this;
	}
	BucketStorage< T > &operator=(BucketStorage< T > &&other) noexcept
	{
		if (this == &other)
		{
			return *this;
		}

		clear();
		this->swap(other);
		return *this;
	};

	BucketStorage(){};
	BucketStorage(const size_type capacity) : block_capacity(capacity){};
	BucketStorage(const BucketStorage &other) : block_capacity(other.block_capacity)
	{
		for (T &i : other)
		{
			insert(i);
		}
	};
	BucketStorage(BucketStorage &&other) noexcept { this->swap(other); };
	~BucketStorage()
	{
		clear();
		delete tail;
	};

  private:
	template< typename Y >
	iterator insert_impl(Y &&elem);

	size_type block_capacity = 64;
	size_type total_size = 0;
	size_type block_amount = 0;

	utils::Node< utils::Block< T > > *tail = new utils::Node< utils::Block< T > >(utils::Block< T >(), 0);
	utils::Node< utils::Block< T > > *head = tail;

	utils::Node< utils::Block< T > > *head_free = nullptr;
	utils::Node< utils::Block< T > > *tail_free = nullptr;
};

template< typename T >
template< typename Y >
typename BucketStorage< T >::iterator BucketStorage< T >::insert_impl(Y &&elem)
{
	using namespace utils;
	if (block_amount * block_capacity == total_size)
	{
		block_amount++;
		Node< Block< T > > *new_node = new Node< Block< T > >(Block< T >(block_capacity), block_amount);
		head_free = tail_free = new_node;

		if (head == tail)
		{
			head = new_node;
			head->set_next(tail);
			tail->set_prev(head);
		}
		else
		{
			new_node->set_prev(tail->get_prev());
			new_node->set_next(tail);

			tail->get_prev()->set_next(new_node);
			tail->set_prev(new_node);
		}
	}

	Node< T > *m_ptr = head_free->get_elem()->insert(std::forward< Y >(elem));
	Node< Block< T > > *n_ptr = head_free;

	if (head_free->get_elem()->get_size() == block_capacity)
	{
		if (head_free->get_next_f() != nullptr)
		{
			head_free = head_free->get_next_f();
			head_free->get_prev_f()->set_next_f(nullptr);
			head_free->set_prev_f(nullptr);
		}
		else
		{
			head_free = tail_free = nullptr;
		}
	}

	total_size++;
	return iterator(n_ptr, m_ptr);
}

template< typename T >
void BucketStorage< T >::clear()
{
	while (head != tail)
	{
		head = head->get_next();
		delete head->get_prev();
	}
	head_free = nullptr;
	tail_free = nullptr;
	total_size = 0;
	block_amount = 0;
}

template< typename T >
typename BucketStorage< T >::iterator BucketStorage< T >::erase(const_iterator it)
{
	using namespace utils;
	if (empty())
	{
		return end();
	}

	iterator nxt = iterator(it.get_n_ptr(), it.get_ptr());
	nxt++;

	Node< Block< T > > *block = it.get_n_ptr();
	if (block->get_elem()->get_size() == 1)
	{
		if (block->get_next() != nullptr)
		{
			block->get_next()->set_prev(block->get_prev());
		}
		if (block->get_prev() != nullptr)
		{
			block->get_prev()->set_next(block->get_next());
		}

		if (block->get_next_f() != nullptr)
		{
			block->get_next_f()->set_prev_f(block->get_prev_f());
		}

		if (block->get_prev_f() != nullptr)
		{
			block->get_prev_f()->set_next_f(block->get_next_f());
		}

		if (block == head)
		{
			head = head->get_next();
		}

		if (block == head_free)
		{
			head_free = head_free->get_next_f();
		}

		if (block == tail_free)
		{
			tail_free = tail_free->get_prev_f();
		}

		delete block;
		block_amount--;
	}
	else
	{
		if (block->get_elem()->get_size() == block->get_elem()->get_capacity())
		{
			if (head_free == nullptr)
			{
				head_free = tail_free = block;
			}
			else
			{
				tail_free->set_next_f(block);
				block->set_prev_f(tail_free);
				tail_free = block;
			}
		}
		block->get_elem()->erase(it.get_ptr());
	}

	total_size--;
	return nxt;
}

template< typename T >
typename BucketStorage< T >::iterator BucketStorage< T >::erase(iterator it)
{
	return erase(static_cast< const_iterator >(it));
}

template< typename T >
void BucketStorage< T >::shrink_to_fit()
{
	if ((block_amount - 1) * block_capacity >= total_size)
	{
		BucketStorage< T > tmp = BucketStorage(*this);
		this->clear();

		for (iterator i = tmp.begin(); i != tmp.end(); i++)
		{
			insert(std::move(*i));
		}
	}
}

template< typename T >
typename BucketStorage< T >::iterator BucketStorage< T >::get_to_distance(iterator it, const difference_type distance)
{
	if (distance > 0)
	{
		for (difference_type i = 0; i < distance; it++, i++)
			;
	}
	else
	{
		for (difference_type i = 0; i > distance; it--, i--)
			;
	}
	return it;
}
