package io.akryl

import react.Context
import react.EffectDisposer
import react.MutableRefObject
import react.React

typealias SetStateAction<S> = (newState: S) -> Unit

@Suppress("unused")
fun <S> ComponentScope.useState(initialState: S): Pair<S, SetStateAction<S>> {
    val (state, setState) = React.useState(initialState)
    return Pair(
        state.unsafeCast<S>(),
        setState.unsafeCast<SetStateAction<S>>()
    )
}

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

@Suppress("unused")
fun ComponentScope.useEffect(dependencies: List<Any?>? = undefined, effect: DisposeScope.() -> Unit) {
    React.useEffect({
        DisposeScope().apply(effect).build()
    }, dependencies?.toTypedArray())
}

@Suppress("unused")
fun <T> ComponentScope.useContext(context: Context<T>): T {
    return React.useContext(context)
}

@Suppress("unused")
fun <R> ComponentScope.useCallback(dependencies: List<Any?>? = undefined, callback: () -> R): () -> R {
    return React.useCallback(callback, dependencies?.toTypedArray())
}

@Suppress("unused")
fun <R> ComponentScope.useRef(initialValue: R): MutableRefObject<R> {
    return React.useRef(initialValue)
}

@Suppress("unused")
fun ComponentScope.useDebugValue(value: Any?) {
    React.useDebugValue(value)
}
