package io.akryl

import io.akryl.react.EffectDisposer
import io.akryl.react.Ref
import io.akryl.rx.*
import kotlin.reflect.KProperty
import io.akryl.react.useEffect as reactUseEffect
import io.akryl.react.useRef as reactUseRef
import io.akryl.react.useState as reactUseState

class StateProperty<R>(
  private val state: R,
  private val setState: (R) -> Unit
) {
  operator fun getValue(self: Any?, property: KProperty<*>): R {
    return state
  }

  operator fun setValue(self: Any?, property: KProperty<*>, newValue: R) {
    setState(newValue)
  }
}

class RefProperty<R>(
  private val ref: Ref<R?>
) {
  @Suppress("UNCHECKED_CAST")
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

fun <R> useComputed(fn: () -> R): ComputedProperty<R> {
  val ref by useRef { ComputedProperty(EmptyReactiveContainer, fn) }

  useEffect(Unit) {
    dispose { ref.dispose() }
  }

  return ref
}

private object EmptyReactiveContainer : ReactiveContainer, Transient {
  override fun registerReactiveHandle(handle: ReactiveHandle) {}
}