package io.akryl

import react.Context
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
fun ComponentScope.useEffect(dependencies: Array<Any?>? = undefined, effect: () -> Unit) {
    React.useEffect({
        effect()
        undefined
    }, dependencies)
}

@Suppress("unused")
fun <T> ComponentScope.useContext(context: Context<T>): T {
    return React.useContext(context)
}

@Suppress("unused")
fun <R> ComponentScope.useCallback(dependencies: List<Any?>? = undefined, callback: () -> R): () -> R {
    return React.useCallback(callback, dependencies?.toTypedArray())
}
