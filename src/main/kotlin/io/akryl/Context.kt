package io.akryl

import react.Context
import react.ProviderProps
import react.React
import react.ReactElement

fun <T> Context<T>.provider(value: T, children: List<ReactElement<*>>): ReactElement<ProviderProps<T>> {
    @Suppress("RemoveExplicitTypeArguments") // Type inference failing on ProviderProps<T>
    return React.createElement(
        Provider,
        ProviderProps<T>(
            value = value,
            children = children.toTypedArray()
        )
    )
}
