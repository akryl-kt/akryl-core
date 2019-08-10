package io.akryl.react

interface ReactNode

typealias FunctionalComponent = (props: dynamic) -> ReactNode

typealias EffectDisposer = () -> Unit

fun createTextElement(value: String) = value.unsafeCast<ReactNode>()