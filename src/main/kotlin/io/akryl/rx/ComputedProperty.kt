package io.akryl.rx

import kotlin.reflect.KProperty

class ComputedProperty<R>(
  private val container: ReactiveContainer,
  private val fn: () -> R
) : ReactiveHandle, Transient, Observable {
  private val inner = ObservableProperty()
  private var dirty = true
  private var value: R? = null
  private var handle: ReactiveHandle? = null

  override fun subscribe(observer: Observer) = inner.subscribe(observer)
  override fun unsubscribe(observer: Observer) = inner.unsubscribe(observer)

  override fun dispose() {
    handle?.dispose()
    handle = null
  }

  @Suppress("UNCHECKED_CAST")
  fun get(): R {
    if (dirty) {
      val (value, handle) = ChangeDetector.evaluate(fn, this::fire)
      this.value = value
      this.handle = handle
      dirty = !container.observable
    }
    inner.observed()
    return value as R
  }

  operator fun getValue(target: Any?, property: KProperty<*>) = get()

  private fun fire() {
    dirty = true
    inner.fire()
  }
}

fun <R> ReactiveContainer.computed(fn: () -> R): ComputedProperty<R> {
  val prop = ComputedProperty(this, fn)
  this.registerReactiveHandle(prop)
  return prop
}