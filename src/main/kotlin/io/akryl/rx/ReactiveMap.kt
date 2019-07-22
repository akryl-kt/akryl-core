package io.akryl.rx

internal class ReactiveMap<K, V>(source: MutableMap<K, V>, factory: () -> MutableMap<K, ReactiveProperty<V>>) : AbstractMutableMap<K, V>() {
  private val sizeProp = ReactiveProperty(source.size)
  private val inner = factory()

  private inner class EntrySet : AbstractMutableSet<MutableMap.MutableEntry<K, V>>() {
    override fun add(element: MutableMap.MutableEntry<K, V>): Boolean = throw UnsupportedOperationException("Add is not supported on entries")

    override fun clear() {
      this@ReactiveMap.clear()
    }

    override operator fun contains(element: MutableMap.MutableEntry<K, V>): Boolean {
      val key = element.key
      val value = element.value
      val ourValue = get(key)

      if (value != ourValue) {
        return false
      }

      if (ourValue == null && !containsKey(key)) {
        return false
      }

      return true
    }

    override operator fun iterator(): MutableIterator<MutableMap.MutableEntry<K, V>> = EntryIterator()

    override fun remove(element: MutableMap.MutableEntry<K, V>): Boolean {
      if (contains(element)) {
        this@ReactiveMap.remove(element.key)
        return true
      }
      return false
    }

    override val size: Int get() = this@ReactiveMap.size
  }

  private class Entry<K, V>(val entry: MutableMap.MutableEntry<K, ReactiveProperty<V>>) : MutableMap.MutableEntry<K, V> {
    override val key = entry.key
    override val value get() = entry.value.get()

    override fun setValue(newValue: V): V {
      val oldValue = value
      entry.value.set(observable(newValue))
      return oldValue
    }

    override fun hashCode() = entry.hashCode()
    override fun toString() = entry.toString()
    override fun equals(other: Any?) = entry == other
  }

  private inner class EntryIterator : MutableIterator<MutableMap.MutableEntry<K, V>> {
    private val inner = this@ReactiveMap.inner.iterator()
    private lateinit var current: Entry<K, V>

    init {
        sizeProp.get()
    }

    override fun hasNext() = inner.hasNext()

    override fun next(): MutableMap.MutableEntry<K, V> {
      val entry = inner.next()
      current = Entry(entry)
      return current
    }

    override fun remove() {
      inner.remove()
      current.entry.value.fire()
      sizeProp.set(this@ReactiveMap.inner.size)
    }
  }

  init {
    for ((k, v) in source) {
      inner[k] = ReactiveProperty(observable(v))
    }
  }

  override fun clear() {
    val values = ArrayList(inner.values)
    inner.clear()
    values.forEach { it.fire() }
    sizeProp.set(inner.size)
  }

  override fun containsKey(key: K): Boolean {
    val value = inner[key]
    return if (value != null) {
      value.get()
      true
    } else {
      sizeProp.get()
      false
    }
  }

  override fun containsValue(value: V): Boolean {
    val result = inner.any { it.value.get() == value }
    if (!result) sizeProp.get()
    return result
  }

  private var _entries: MutableSet<MutableMap.MutableEntry<K, V>>? = null
  override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
    get() {
      if (_entries == null) {
        _entries = EntrySet()
      }
      return _entries!!
    }

  override operator fun get(key: K): V? {
    val value = inner[key]
    return if (value != null) {
      value.get()
    } else {
      sizeProp.get()
      return null
    }
  }

  override fun put(key: K, value: V): V? {
    val prop = inner[key]
    val oldValue = if (prop != null) {
      val oldValue = prop.get()
      prop.set(value)
      oldValue
    } else {
      inner[key] = ReactiveProperty(observable(value))
      null
    }
    sizeProp.set(inner.size)
    return oldValue
  }

  override fun remove(key: K): V? {
    val prop = inner.remove(key)
    sizeProp.set(inner.size)
    prop?.fire()
    return prop?.get()
  }

  override val size get() = sizeProp.get()
}