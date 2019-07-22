@file:Suppress("UnsafeCastFromDynamic")

package io.akryl.rx

import js.*

const val OBSERVABLE_MARKER = "\$observable"

interface Observer {
  fun changed()
}

interface Observable {
  fun subscribe(observer: Observer)
  fun unsubscribe(observer: Observer)
}

val Any?.observable get() = this.asDynamic()[OBSERVABLE_MARKER] == true

fun <T> observable(target: T): T {
  if (target == null || !isPlainObject(target) || !isKotlinObject(target)) return target
  if (target.observable) return target

  val obj = target.asDynamic()
  defineObservableMarker(obj)

  when (obj::class) {
    ArrayList::class -> replaceObject(target, ReactiveList<Any?>(obj, ::ArrayList))
    HashMap::class -> replaceObject(target, ReactiveMap<Any?, Any?>(obj, ::HashMap))
    LinkedHashMap::class -> replaceObject(target, ReactiveMap<Any?, Any?>(obj, ::LinkedHashMap))
    else -> defineObservableProperties(obj)
  }

  return target
}

private fun isKotlinObject(obj: dynamic): Boolean {
  // todo full check
  return JsObject.getPrototypeOf(obj) != null
}

private fun isMagicVar(name: String) = jsTypeOf(name) == "string" && name.startsWith("$")

private fun defineObservableMarker(obj: dynamic) {
  JsObject.defineProperty(obj, OBSERVABLE_MARKER, DataPropertyDescriptor(
    configurable = true,
    enumerable = false,
    writable = false,
    value = true
  ))
}

private fun defineObservableProperties(obj: dynamic) {
  for (key in JsObject.keys(obj)) {
    if (isMagicVar(key)) continue
    val pd = JsObject
      .getOwnPropertyDescriptor<Any?>(obj, key)
      ?.data
      ?: continue
    defineProperty(obj, key, pd.value)
  }
}

private fun defineProperty(obj: dynamic, key: String, initialValue: Any?) {
  var value = observable(initialValue)
  val prop = ObservableProperty()

  JsObject.defineProperty(obj, key, AccessorPropertyDescriptor(
    configurable = true,
    enumerable = true,
    get = {
      prop.observed()
      value
    },
    set = { newValue ->
      value = newValue
      prop.fire()
    }
  ))
}

private fun replaceObject(dest: dynamic, source: dynamic) {
  JsObject.setPrototypeOf(dest, null)
  for (key in JsObject.keys(dest)) {
    js("delete dest[key]")
  }

  for (key in JsObject.keys(source)) {
    dest[key] = source[key]
  }
  JsObject.setPrototypeOf(dest, JsObject.getPrototypeOf(source))
}