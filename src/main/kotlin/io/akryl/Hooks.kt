package io.akryl

import io.akryl.react.Context
import io.akryl.react.EffectDisposer
import io.akryl.react.Ref
import io.akryl.rx.*
import kotlin.reflect.KProperty
import io.akryl.react.useContext as reactUseContext
import io.akryl.react.useEffect as reactUseEffect
import io.akryl.react.useRef as reactUseRef
import io.akryl.react.useState as reactUseState

data class StateProperty<R>(
  val state: R,
  val setState: (R) -> Unit
) {
  var value: R
    get() = state
    set(value) { setState(value) }

  operator fun getValue(self: Any?, property: KProperty<*>): R {
    return state
  }

  operator fun setValue(self: Any?, property: KProperty<*>, newValue: R) {
    setState(newValue)
  }
}

@Suppress("UNCHECKED_CAST")
class RefProperty<R>(
  private val ref: Ref<R?>
) {
  var value: R
    get() = ref.current as R
    set(value) { ref.current = value }

  operator fun getValue(self: Any?, property: KProperty<*>): R {
    return ref.current as R
  }

  operator fun setValue(self: Any?, property: KProperty<*>, newValue: R) {
    ref.current = newValue
  }
}

fun <R> useState(initialValue: R): StateProperty<R> {
  val (state, setState) = reactUseState(initialValue)
  return StateProperty(state.unsafeCast<R>(), setState.unsafeCast<(R) -> Unit>())
}

class DisposeScope {
  var disposer: EffectDisposer? = undefined
    private set

  fun dispose(disposer: EffectDisposer) {
    this.disposer = disposer
  }
}

inline fun useEffect(vararg dependencies: Any?, crossinline callback: DisposeScope.() -> Unit) {
  val wrapper = {
    val scope = DisposeScope()
    scope.callback()
    scope.disposer
  }

  if (dependencies.isEmpty()) {
    reactUseEffect(wrapper)
  } else {
    reactUseEffect(wrapper, dependencies)
  }
}

inline fun <R> useRef(initialValue: () -> R): RefProperty<R> {
  val ref = reactUseRef<R?>(undefined)
  if (ref.current === undefined) {
    ref.current = initialValue()
  }
  return RefProperty(ref)
}

fun <R> useReactive(initialValue: () -> R): ReactiveProperty<R> {
  val ref by useRef { ReactiveProperty(initialValue()) }
  return ref
}

fun <C : Component, R> C.useComputed(fn: C.() -> R): ComputedProperty<R> {
  var thisRef by useRef { this }
  thisRef = this

  val propRef by useRef { ComputedProperty(EmptyReactiveContainer) { thisRef.fn() } }

  useEffect(Unit) {
    dispose { propRef.dispose() }
  }

  useEffect(this) {
    propRef.changed()
  }

  return propRef
}

fun <C : Component, R> C.useComputed(vararg dependencies: Any?, fn: C.() -> R): ComputedProperty<R> {
  var thisRef by useRef { this }
  thisRef = this

  val propRef by useRef { ComputedProperty(EmptyReactiveContainer) { thisRef.fn() } }

  useEffect(Unit) {
    dispose { propRef.dispose() }
  }

  useEffect(*dependencies) {
    propRef.changed()
  }

  return propRef
}

fun <T> useContext(context: Context<T>): T? {
  return reactUseContext(context)
}

private object EmptyReactiveContainer : ReactiveContainer, Transient {
  override fun registerReactiveHandle(handle: ReactiveHandle) {}
}