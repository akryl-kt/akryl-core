package io.akryl

import js.*
import kotlin.reflect.KProperty

const val OBSERVABLE_MARKER = "\$observable"

interface Observer {
  fun changed()
}

interface Transient {
  @Suppress("unused")
  @JsName("\$observable")
  val observable get() = this
}

interface Observable {
  fun subscribe(observer: Observer)
  fun unsubscribe(observer: Observer)
}

interface ReactiveHandle {
  fun dispose()
}

private class ObserverImpl(private val callback: () -> Unit) : Observer, ReactiveHandle {
  private val dependencies = ArrayList<Observable>()

  override fun changed() {
    dispose()
    callback()
  }

  fun observed(dependency: Observable) {
    dependency.subscribe(this)
    dependencies.add(dependency)
  }

  override fun dispose() {
    dependencies.forEach { it.unsubscribe(this) }
    dependencies.clear()
  }
}

object ChangeDetector {
  private var stack = ArrayList<ObserverImpl>()

  fun <T> evaluate(fn: () -> T, callback: () -> Unit): Pair<T, ReactiveHandle> {
    check(stack.size < 1000) { "ChangeDetector stack is too deep" }
    val observer = ObserverImpl(callback)
    try {
      stack.add(observer)
      return Pair(fn(), observer)
    } finally {
      stack.removeAt(stack.size - 1)
    }
  }

  fun observed(dependency: Observable) {
    stack.lastOrNull()?.observed(dependency)
  }
}

interface ComputedPropertyContainer {
  @JsName("\$isInitialized")
  val isInitialized: Boolean

  fun registerComputedProperty(computedProperty: ComputedProperty<*, *>)
}

interface EmptyComputedPropertyContainer : ComputedPropertyContainer {
  override val isInitialized: Boolean get() = true
  override fun registerComputedProperty(computedProperty: ComputedProperty<*, *>) {}
}

// todo async property evaluation by requestAnimationFrame, prevent fire() if not changed

class ComputedProperty<T : ComputedPropertyContainer?, R>(private val fn: T.() -> R) : ReactiveHandle, Observable, Transient {
  private var dirty = true
  private var instance: T? = null
  private var value: R? = null
  private var handle: ReactiveHandle? = null
  private val subscriptions = HashSet<Observer>()

  override fun subscribe(observer: Observer) {
    subscriptions.add(observer)
  }

  override fun unsubscribe(observer: Observer) {
    subscriptions.remove(observer)
  }

  @Suppress("UNCHECKED_CAST")
  fun compute(): R {
    val (result, handle) = ChangeDetector.evaluate({ (instance as T).fn() }, this::fire)
    this.handle = handle
    return result
  }

  @Suppress("UNCHECKED_CAST")
  fun get(instance: T?): R {
    ChangeDetector.observed(this)
    if (dirty) {
      this.instance = instance
      value = compute()
      dirty = !(instance?.isInitialized ?: true)
    }
    return value as R
  }

  override fun dispose() {
    handle?.dispose()
  }

  operator fun getValue(target: T, property: KProperty<*>) = get(target)

  @Suppress("UNCHECKED_CAST")
  operator fun getValue(target: Any?, property: KProperty<*>) = get(target as T?)

  private fun fire() {
    dirty = true
    val subs = ArrayList(subscriptions)
    subscriptions.clear()
    subs.forEach { it.changed() }
  }
}

@Suppress("unused", "RemoveExplicitTypeArguments")
fun <T : ComputedPropertyContainer?, R> T.computed(fn: T.() -> R): ComputedProperty<T, R> {
  val prop = ComputedProperty<T, R>(fn)
  this?.registerComputedProperty(prop)
  return prop
}

// todo fix non reactive reference in constructor
fun <T> observable(target: T): T {
  return if (target != null && jsTypeOf(target) == "object") {
    if (JsArray.isArray(target)) {
      observable(target, ::ArrayHandler)
    } else {
      observable(target, ::ObjectHandler)
    }
  } else {
    target
  }
}

internal class ProxyObservable : Observable {
  private val subscriptions = HashSet<Observer>()

  val count get() = subscriptions.size

  override fun subscribe(observer: Observer) {
    subscriptions.add(observer)
  }

  override fun unsubscribe(observer: Observer) {
    subscriptions.remove(observer)
  }

  fun fire() {
    val subs = ArrayList(subscriptions)
    subscriptions.clear()
    subs.forEach { it.changed() }
  }

  override fun toString(): String {
    return "ProxyObservable(${subscriptions.size})"
  }
}

private fun isMagicVar(name: String) = jsTypeOf(name) == "string" && name.startsWith("$")

@Suppress("UnsafeCastFromDynamic")
private open class ObjectHandler : ProxyHandler {
  private val subscriptions = HashMap<String, ProxyObservable>()
  protected val keysSubscription = ProxyObservable()

  override fun ownKeys(target: Any): Array<String> {
    ChangeDetector.observed(keysSubscription)

    return JsObject.getOwnPropertyNames(target)
      .filterNot { isMagicVar(it) }
      .toTypedArray()
  }

  override fun get(target: dynamic, name: String, receiver: Any): Any? {
    if (isMagicVar(name)) return target[name]

    var sub = subscriptions[name]
    if (sub == null) {
      sub = ProxyObservable()
      subscriptions[name] = sub
    }
    ChangeDetector.observed(sub)

    val get: dynamic = JsObject
      .getOwnPropertyDescriptor<Any?>(target, name)
      ?.unsafeCast<AccessorPropertyDescriptor<Any?>>()
      ?.get

    if (get != null) {
      return get.apply(receiver)
    }

    if (hasOwnProperty(target, name)) return target[name]

    val proto = JsObject.getPrototypeOf(target)
    if (proto != null) {
      return get(proto, name, receiver)
    }

    return undefined
  }

  override fun set(target: dynamic, name: String, value: Any?, receiver: Any): Boolean {
    if (isMagicVar(name)) {
      target[name] = value
      return true
    }

    val added = !hasOwnProperty(target, name)
    val changed = added || target[name] != value
    target[name] = observable(value)

    if (changed) {
      subscriptions[name]?.fire()
    }
    if (added) {
      keysSubscription.fire()
    }
    return true
  }

  override fun deleteProperty(target: Any, name: String): Boolean {
    js("delete target[name]")

    if (isMagicVar(name)) return true

    subscriptions[name]?.fire()
    keysSubscription.fire()
    return true
  }
}

private class ArrayHandler : ObjectHandler() {
  override fun get(target: dynamic, name: String, receiver: Any): Any? {
    if (name == "length") {
      ChangeDetector.observed(keysSubscription)
    }
    return super.get(target, name, receiver)
  }
}

@Suppress("UnsafeCastFromDynamic")
private fun <T> observable(target: T, handler: () -> ProxyHandler): T {
  val obj: dynamic = target
  var observable = obj[OBSERVABLE_MARKER]

  if (observable != null) {
    return observable
  }
  observable = proxy(target, handler())
  obj[OBSERVABLE_MARKER] = observable

  for (k in JsObject.keys(obj)) {
    if (!isMagicVar(k)) {
      obj[k] = observable(obj[k])
    }
  }

  return observable
}
