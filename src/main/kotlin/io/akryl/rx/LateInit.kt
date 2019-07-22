package io.akryl.rx

import kotlin.reflect.KProperty

class LateInit<R> {
  private var initialized = false
  private var value: R? = null

  @Suppress("UNCHECKED_CAST")
  operator fun getValue(self: Any?, property: KProperty<*>): R {
    if (!initialized) {
      throw UninitializedPropertyAccessException("lateinit property ${property.name} has not been initialized")
    }
    return value as R
  }

  operator fun setValue(self: Any?, property: KProperty<*>, newValue: R) {
    initialized = true
    value = newValue
  }
}

fun <R> lateInit() = LateInit<R>()