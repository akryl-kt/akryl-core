package io.akryl

import react.FunctionalComponent
import react.React
import react.ReactNode

typealias ComponentWrapper<P> = (FunctionalComponent<P>) -> FunctionalComponent<P>

typealias RenderFunction = ComponentScope.() -> ReactNode

interface ComponentScope

@Suppress("FunctionName", "UNUSED_PARAMETER")
fun __akryl_react_component_marker__(
    react: React,
    wrapper: ComponentWrapper<*>,
    render: RenderFunction
): ReactNode = throw NotImplementedError("Implemented by babel-plugin-akryl")

@Suppress("NOTHING_TO_INLINE")
inline fun component(noinline wrapper: ComponentWrapper<*> = { it }, noinline render: RenderFunction): ReactNode {
    return __akryl_react_component_marker__(React, wrapper, render)
}

@Suppress("NOTHING_TO_INLINE", "UnsafeCastFromDynamic")
inline fun memo(noinline render: RenderFunction): ReactNode {
    return component({ React.memo(it) }, render)
}
