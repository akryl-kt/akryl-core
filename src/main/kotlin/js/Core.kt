package js

import kotlin.browser.document
import kotlin.browser.window

interface ProxyHandler {
  @JsName("ownKeys")
  fun ownKeys(target: Any): Array<String>

  @JsName("get")
  fun get(target: Any, name: String, receiver: Any): Any?

  @JsName("set")
  fun set(target: Any, name: String, value: Any?, receiver: Any): Boolean

  @JsName("deleteProperty")
  fun deleteProperty(target: Any, name: String): Boolean
}

external class Proxy(target: Any?, handler: ProxyHandler)

@Suppress("UnnecessaryVariable", "UnsafeCastFromDynamic")
fun <T> proxy(target: T, handler: ProxyHandler): T {
  val proxy: dynamic = Proxy(target, handler)
  return proxy
}

interface PropertyDescriptor<T>

val <T> PropertyDescriptor<T>.accessor get(): AccessorPropertyDescriptor<T>? {
  val pd = this.unsafeCast<AccessorPropertyDescriptor<T>>()
  return if (pd.get != null || pd.set != null) pd else null
}

val <T> PropertyDescriptor<T>.data get() = if (accessor == null) this.unsafeCast<DataPropertyDescriptor<T>>() else null

class AccessorPropertyDescriptor<T>(
  var configurable: Boolean = false,
  val enumerable: Boolean = false,
  val get: (() -> T)? = undefined,
  val set: ((T) -> Unit)? = undefined
) : PropertyDescriptor<T>

class DataPropertyDescriptor<T>(
  var configurable: Boolean = false,
  val enumerable: Boolean = false,
  val writable: Boolean = false,
  val value: T? = undefined
) : PropertyDescriptor<T>

@JsName("Object")
external object JsObject {
  @JsName("keys")
  fun keys(obj: Any): Array<String>

  @JsName("getOwnPropertyNames")
  fun getOwnPropertyNames(obj: Any): Array<String>

  @JsName("getOwnPropertyDescriptor")
  fun <R> getOwnPropertyDescriptor(obj: Any, name: String): PropertyDescriptor<R>?

  @JsName("getPrototypeOf")
  fun getPrototypeOf(obj: Any): dynamic

  @JsName("setPrototypeOf")
  fun setPrototypeOf(obj: Any, prototype: Any?)

  @JsName("create")
  fun create(prototype: Any?): Any?

  @JsName("defineProperty")
  fun defineProperty(obj: Any, name: String, pd: PropertyDescriptor<*>)
}

@JsName("Array")
external object JsArray {
  @JsName("isArray")
  fun isArray(obj: Any?): Boolean
}

fun hasOwnProperty(obj: dynamic, name: String): Boolean {
  return JsObject.asDynamic().hasOwnProperty.bind(obj)(name) as Boolean
}

val isNode get() = try {
  window == document
} catch (ex: Throwable) {
  true
}

@JsName("decodeURI")
external fun decodeUri(value: String): String

@JsName("encodeURI")
external fun encodeUri(value: String): String

@JsName("Set")
external class JsSet<E> {
  fun add(item: E)
  fun delete(item: E)
  fun has(item: E): Boolean
}

private val plainObjectStrings = arrayOf("[object Object]", "[object Array]")

@Suppress("UNUSED_PARAMETER")
fun isPlainObject(obj: dynamic): Boolean {
  val str = js("Object.prototype.toString.apply(obj)")
  return str in plainObjectStrings
}