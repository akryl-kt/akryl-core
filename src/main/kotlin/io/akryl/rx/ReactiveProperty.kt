package io.akryl.rx

import kotlin.reflect.KProperty

class ReactiveProperty<R>(private var value: R) : Observable, Transient {
  private val inner = ObservableProperty()

  override fun subscribe(observer: Observer) = inner.subscribe(observer)
  override fun unsubscribe(observer: Observer) = inner.unsubscribe(observer)

  fun get(): R {
    inner.observed()
    return value
  }

  fun set(newValue: R) {
    if (newValue != value) {
      value = newValue
      inner.fire()
    }
  }

  fun fire() {
    inner.fire()
  }

  operator fun getValue(target: Any?, property: KProperty<*>) = get()
  operator fun setValue(target: Any?, property: KProperty<*>, value: R) = set(value)

  override fun equals(other: Any?): Boolean {
    inner.observed()

    if (this === other) return true
    if (other !is ReactiveProperty<*>) return false

    if (value != other.value) return false

    return true
  }

  override fun hashCode(): Int {
    inner.observed()
    return value?.hashCode() ?: 0
  }

  override fun toString(): String {
    inner.observed()
    return value.toString()
  }
}

fun <R> reactive(initialValue: R) = ReactiveProperty(initialValue)