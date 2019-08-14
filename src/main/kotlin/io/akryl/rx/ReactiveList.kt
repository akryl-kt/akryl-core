package io.akryl.rx

internal class ReactiveList<E>(source: MutableList<E>, factory: () -> MutableList<ReactiveProperty<E>>) : AbstractMutableList<E>() {
  private val sizeProp = ReactiveProperty(source.size)
  private val inner = factory()

  init {
      for (item in source) {
        inner.add(ReactiveProperty(observable(item)))
      }
  }

  override val size get() = sizeProp.value

  override fun add(index: Int, element: E) {
    inner.add(index, ReactiveProperty(observable(element)))
    sizeProp.value = inner.size
  }

  override fun removeAt(index: Int): E {
    val result = inner.removeAt(index)
    result.fire()
    sizeProp.value = inner.size
    return result.value
  }

  override fun set(index: Int, element: E): E {
    val prop = inner[index]
    val oldElement = prop.value
    prop.value = observable(element)
    return oldElement
  }

  override fun get(index: Int): E {
    return inner[index].value
  }
}