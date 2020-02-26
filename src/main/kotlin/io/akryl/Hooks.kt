package io.akryl

import react.Context
import react.EffectDisposer
import react.MutableRefObject
import react.React

typealias SetStateAction<S> = (newState: S) -> Unit

/**
 * Returns a pair of a stateful value, and a function to update it.
 *
 * Example:
 * ```
 * val (state, setState) = useState(0)
 * ```
 *
 * During the initial render, the returned `state` is the same as the [initialState].
 * The `setState` function is used to update the state. It accepts a new state value
 * and enqueues a re-render of the component.
 */
@Suppress("unused")
fun <S> ComponentScope.useState(initialState: S): Pair<S, SetStateAction<S>> {
    val (state, setState) = React.useState(initialState)
    return Pair(
        state.unsafeCast<S>(),
        setState.unsafeCast<SetStateAction<S>>()
    )
}

/**
 * Overload of the [useState] that accepts an [initializer] lambda that returns an initial state.
 * It is useful when computation of the initial state takes a lot of time.
 *
 * Example:
 * ```
 * val (state, setState) = useState { /* compute initial state */ }
 * ```
 */
@Suppress("unused")
fun <S> ComponentScope.useState(initializer: () -> S): Pair<S, SetStateAction<S>> {
    val (state, setState) = React.useState(initializer)
    return Pair(
        state.unsafeCast<S>(),
        setState.unsafeCast<SetStateAction<S>>()
    )
}

class DisposeScope {
    private val items = ArrayList<EffectDisposer>()

    fun dispose(block: EffectDisposer) {
        items.add(block)
    }

    fun build(): EffectDisposer {
        val items = ArrayList(this.items)
        return {
            items.forEach { it() }
        }
    }
}

/**
 * Accepts a function [effect] that contains imperative, possibly effectful code.
 * Use [useEffect] for mutations, subscriptions, timers, logging, and other side effects
 * that are not allowed inside the main body of a functional component.
 * The [effect] function will run after the render is committed to the screen.
 *
 * Example:
 * ```
 * useEffect {
 *     document.title = title
 * }
 * ```
 *
 * By default, effects run after every completed render, but you can choose to fire them
 * only when certain values have changed. To implement this, pass a list of [dependencies] to the [useEffect].
 *
 * Example:
 * ```
 * useEffect(listOf(title)) {
 *     document.title = title
 * }
 * ```
 *
 * Often, effects create resources that need to be cleaned up before the component leaves the screen,
 * such as a subscription or timer ID. To do this, the function passed to [useEffect] can call [DisposeScope.dispose].
 *
 * Example:
 * ```
 * useEffect {
 *     val timerId = window.setTimeout({ /* some code */ }, 1000)
 *     dispose { window.clearTimeout(timerId) }
 * }
 * ```
 *
 * The [DisposeScope.dispose] can be called multiple times.
 * All passed lambdas will be executed in the same order as [DisposeScope.dispose] were called.
 */
@Suppress("unused")
fun ComponentScope.useEffect(dependencies: List<Any?>? = undefined, effect: DisposeScope.() -> Unit) {
    React.useEffect({
        DisposeScope().apply(effect).build()
    }, dependencies?.toTypedArray())
}

/**
 * Accepts a [context] object (the value returned from [React.createContext])
 * and returns the current context value for that context.
 * When the nearest [Context.provider] above the component updates,
 * this hook will trigger a re-render.
 *
 * Example:
 * ```
 * val MyContext = React.createContext(0)
 *
 * fun app() = component {
 *     MyContext.provider(value = 10, children = listOf(
 *         button()
 *     ))
 * }
 *
 * fun button() = component {
 *     val value = useContext(MyContext)
 *     Div(text = "value = $value")
 * }
 * ```
 */
@Suppress("unused")
fun <T> ComponentScope.useContext(context: Context<T>): T {
    return React.useContext(context)
}

/**
 * Returns a memoized [callback].
 * Pass a [callback] and a list of [dependencies]. [useCallback] will return a memoized version of the [callback]
 * that only changes if one of the [dependencies] has changed.
 *
 * Example:
 * ```
 * val cb = useCallback(listOf(value)) {
 *     console.log(value)
 * }
 * ```
 */
@Suppress("unused")
fun <R> ComponentScope.useCallback(dependencies: List<Any?>? = undefined, callback: () -> R): () -> R {
    return React.useCallback(callback, dependencies?.toTypedArray())
}

/**
 * Returns a mutable ref object whose [MutableRefObject.current] property is initialized
 * to the passed [initialValue].
 * The returned object will persist for the full lifetime of the component.
 *
 * Example:
 * ```
 * fun example() = component {
 *     val ref = useRef<HTMLDivElement?>(null)
 *     useEffect(emptyList()) {
 *         ref.current?.focus()
 *     }
 *     Input(ref = ref)
 * }
 * ```
 */
@Suppress("unused")
fun <R> ComponentScope.useRef(initialValue: R): MutableRefObject<R> {
    return React.useRef(initialValue)
}

/**
 * Can be used to display a label [value] for custom hooks in React DevTools.
 *
 * Example:
 * ```
 * fun ComponentScope.useFriendStatus(status: Boolean) {
 *     useDebugValue(if (status) "Online" else "Offline")
 * }
 * ```
 */
@Suppress("unused")
fun ComponentScope.useDebugValue(value: Any?) {
    React.useDebugValue(value)
}
