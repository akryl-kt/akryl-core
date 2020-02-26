package io.akryl

import react.Context
import react.ProviderProps
import react.React
import react.ReactElement

fun <T> Context<T>.provider(value: T, children: List<ReactElement<*>>): ReactElement<ProviderProps<T>> {
    return React.createElement(
        Provider,
        ProviderProps(
            value = value,
            children = undefined
        ),
        *children.toTypedArray()
    )
}
