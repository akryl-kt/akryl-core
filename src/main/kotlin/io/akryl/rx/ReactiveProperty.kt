package io.akryl.rx

import kotlin.reflect.KProperty

class ReactiveProperty<R>(private var _value: R) : Observable, Transient {
  private val inner = ObservableProperty()

  override fun subscribe(observer: Observer) = inner.subscribe(observer)
  override fun unsubscribe(observer: Observer) = inner.unsubscribe(observer)

  var value: R
    get() {
      inner.observed()
      return _value
    }
    set(newValue) {
      if (newValue != _value) {
        _value = newValue
        inner.fire()
      }
    }

  fun fire() {
    inner.fire()
  }

  operator fun getValue(target: Any?, property: KProperty<*>) = value
  operator fun setValue(target: Any?, property: KProperty<*>, newValue: R) { value = newValue }

  override fun equals(other: Any?): Boolean {
    inner.observed()

    if (this === other) return true
    if (other !is ReactiveProperty<*>) return false

    if (_value != other._value) return false

    return true
  }

  override fun hashCode(): Int {
    inner.observed()
    return _value?.hashCode() ?: 0
  }

  override fun toString(): String {
    inner.observed()
    return _value.toString()
  }
}

fun <R> reactive(initialValue: R) = ReactiveProperty(initialValue)