package io.akryl.rx

internal class ReactiveList<E>(source: MutableList<E>, factory: () -> MutableList<ReactiveProperty<E>>) : AbstractMutableList<E>() {
  private val sizeProp = ReactiveProperty(source.size)
  private val inner = factory()

  init {
      for (item in source) {
        inner.add(ReactiveProperty(observable(item)))
      }
  }

  override val size get() = sizeProp.get()

  override fun add(index: Int, element: E) {
    inner.add(index, ReactiveProperty(observable(element)))
    sizeProp.set(inner.size)
  }

  override fun removeAt(index: Int): E {
    val result = inner.removeAt(index)
    result.fire()
    sizeProp.set(inner.size)
    return result.get()
  }

  override fun set(index: Int, element: E): E {
    val prop = inner[index]
    val oldElement = prop.get()
    prop.set(observable(element))
    return oldElement
  }

  override fun get(index: Int): E {
    return inner[index].get()
  }
}