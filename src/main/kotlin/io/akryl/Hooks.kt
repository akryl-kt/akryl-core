package io.akryl

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
