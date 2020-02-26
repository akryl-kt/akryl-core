package io.akryl

import react.Context
import react.ProviderProps
import react.React
import react.ReactElement

/**
 * Accepts a [value] prop to be passed to consuming components that are descendants of this [provider].
 * One [provider] can be connected to many consumers.
 * Providers can be nested to override values deeper within the tree.
 * @see [useContext]
 */
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
