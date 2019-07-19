package io.akryl.rx

// todo tests

class ReactiveMap<K, V>(private val inner: MutableMap<K, V>) : MutableMap<K, V>, Transient {
  private val sizeProp = ObservableProperty()
  private val itemsProps = HashMap<K, ObservableProperty>()

  init {
      for ((k, v) in inner) {
        inner[k] = observable(v)
        itemsProps[k] = ObservableProperty()
      }
  }

  override val size: Int get() {
    sizeProp.observed()
    return inner.size
  }

  override val entries: MutableSet<MutableMap.MutableEntry<K, V>> = EntrySet()
  override val keys: MutableSet<K> = KeySet()
  override val values: MutableCollection<V> = ValueSet()

  private inner class EntryIterator(
    private val inner: MutableIterator<MutableMap.MutableEntry<K, V>>
  ) : MutableIterator<MutableMap.MutableEntry<K, V>> {
    private lateinit var current: MutableMap.MutableEntry<K, V>

    override fun hasNext() = inner.hasNext()

    override fun next(): MutableMap.MutableEntry<K, V> {
      val result = inner.next()
      itemProp(result.key).observed()
      current = result
      return result
    }

    override fun remove() {
      inner.remove()
      itemProp(current.key).fire()
      sizeProp.fire()
    }
  }

  private inner class EntrySet : MutableSet<MutableMap.MutableEntry<K, V>> {
    override fun add(element: MutableMap.MutableEntry<K, V>) = throw NotImplementedError()
    override fun addAll(elements: Collection<MutableMap.MutableEntry<K, V>>) = throw NotImplementedError()
    override fun clear() = throw NotImplementedError()
    override fun remove(element: MutableMap.MutableEntry<K, V>) = throw NotImplementedError()
    override fun removeAll(elements: Collection<MutableMap.MutableEntry<K, V>>) = throw NotImplementedError()
    override fun retainAll(elements: Collection<MutableMap.MutableEntry<K, V>>) = throw NotImplementedError()
    override fun contains(element: MutableMap.MutableEntry<K, V>) = throw NotImplementedError()
    override fun containsAll(elements: Collection<MutableMap.MutableEntry<K, V>>) = throw NotImplementedError()

    override fun isEmpty() = this@ReactiveMap.isEmpty()
    override val size get() = this@ReactiveMap.size

    override fun iterator(): MutableIterator<MutableMap.MutableEntry<K, V>> {
      sizeProp.observed()
      return EntryIterator(inner.entries.iterator())
    }
  }

  private inner class KeyIterator(
    inner: MutableIterator<MutableMap.MutableEntry<K, V>>
  ) : MutableIterator<K> {
    private val inner = EntryIterator(inner)
    override fun hasNext() = inner.hasNext()
    override fun next() = inner.next().key
    override fun remove() = inner.remove()
  }

  private inner class KeySet : MutableSet<K> {
    override fun add(element: K) = throw NotImplementedError()
    override fun addAll(elements: Collection<K>) = throw NotImplementedError()
    override fun clear() = throw NotImplementedError()
    override fun remove(element: K) = throw NotImplementedError()
    override fun removeAll(elements: Collection<K>) = throw NotImplementedError()
    override fun retainAll(elements: Collection<K>) = throw NotImplementedError()
    override fun contains(element: K) = throw NotImplementedError()
    override fun containsAll(elements: Collection<K>) = throw NotImplementedError()
    override fun isEmpty() = throw NotImplementedError()
    override val size get() = throw NotImplementedError()

    override fun iterator(): MutableIterator<K> {
      sizeProp.observed()
      return KeyIterator(inner.entries.iterator())
    }
  }

  private inner class ValueIterator(
    inner: MutableIterator<MutableMap.MutableEntry<K, V>>
  ) : MutableIterator<V> {
    private val inner = EntryIterator(inner)
    override fun hasNext() = inner.hasNext()
    override fun next() = inner.next().value
    override fun remove() = inner.remove()
  }

  private inner class ValueSet : MutableSet<V> {
    override fun add(element: V) = throw NotImplementedError()
    override fun addAll(elements: Collection<V>) = throw NotImplementedError()
    override fun clear() = throw NotImplementedError()
    override fun remove(element: V) = throw NotImplementedError()
    override fun removeAll(elements: Collection<V>) = throw NotImplementedError()
    override fun retainAll(elements: Collection<V>) = throw NotImplementedError()
    override fun contains(element: V) = throw NotImplementedError()
    override fun containsAll(elements: Collection<V>) = throw NotImplementedError()
    override fun isEmpty() = throw NotImplementedError()
    override val size get() = throw NotImplementedError()

    override fun iterator(): MutableIterator<V> {
      sizeProp.observed()
      return ValueIterator(inner.entries.iterator())
    }
  }

  override fun containsKey(key: K): Boolean {
    return if (inner.containsKey(key)) {
      itemProp(key).observed()
      true
    } else {
      sizeProp.observed()
      false
    }
  }

  override fun containsValue(value: V): Boolean {
    for ((k, v) in inner) {
      if (v == value) {
        itemProp(k).observed()
        return true
      }
    }

    sizeProp.observed()
    return false
  }

  override fun get(key: K): V? {
    itemProp(key).observed()
    return inner[key]
  }

  override fun isEmpty(): Boolean {
    sizeProp.observed()
    return inner.isEmpty()
  }

  override fun clear() {
    inner.clear()
    itemsProps.forEach { it.value.fire() }
    itemsProps.clear()
    sizeProp.fire()
  }

  override fun put(key: K, value: V): V? {
    val newValue = observable(value)
    val oldValue = inner.put(key, newValue)
    with(itemProp(key)) {
      observed()
      fire()
    }
    if (oldValue == null) {
      sizeProp.fire()
    }
    return oldValue
  }

  override fun putAll(from: Map<out K, V>) {
    val oldSize = inner.size
    for ((k, v) in from) {
      inner[k] = observable(v)
      itemProp(k).fire()
    }
    if (oldSize != size) {
      sizeProp.fire()
    }
  }

  override fun remove(key: K): V? {
    val value = inner.remove(key)
    if (value != null) {
      with(itemProp(key)) {
        observed()
        fire()
      }
      itemsProps.remove(key)
      sizeProp.fire()
    }
    return value
  }

  override fun equals(other: Any?): Boolean {
    itemsProps.forEach { it.value.observed() }
    sizeProp.observed()
    return inner == other
  }

  override fun hashCode(): Int {
    itemsProps.forEach { it.value.observed() }
    sizeProp.observed()
    return inner.hashCode()
  }

  override fun toString(): String {
    itemsProps.forEach { it.value.observed() }
    sizeProp.observed()
    return inner.toString()
  }

  private fun itemProp(key: K): ObservableProperty {
    var prop = itemsProps[key]
    if (prop == null) {
      prop = ObservableProperty()
      itemsProps[key] = prop
    }
    return prop
  }
}