package io.akryl

import react.FunctionalComponent
import react.React
import react.ReactElement

typealias ComponentWrapper<P> = (FunctionalComponent<P>) -> FunctionalComponent<P>

typealias RenderFunction = ComponentScope.() -> ReactElement<*>?

interface ComponentScope

@Suppress("FunctionName", "UNUSED_PARAMETER")
fun __akryl_react_component_marker__(
    react: React,
    wrapper: ComponentWrapper<*>,
    render: RenderFunction
): ReactElement<*> = throw NotImplementedError("Implemented by babel-plugin-akryl")

@Suppress("NOTHING_TO_INLINE")
inline fun component(noinline wrapper: ComponentWrapper<*> = { it }, noinline render: RenderFunction): ReactElement<*> {
    return __akryl_react_component_marker__(React, wrapper, render)
}

@Suppress("NOTHING_TO_INLINE", "UnsafeCastFromDynamic")
inline fun memo(noinline render: RenderFunction): ReactElement<*> {
    return component({ React.memo(it) }, render)
}

@Suppress("UNUSED_PARAMETER")
fun <P> ReactElement<P>.withKey(key: Any): ReactElement<P> {
    val element = this
    return React.cloneElement(element, js("Object.assign({}, element.props, {key: key})") as P)
}
