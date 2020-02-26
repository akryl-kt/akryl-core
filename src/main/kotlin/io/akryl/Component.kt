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

/**
 * Converts a simple Kotlin function [render] into a React component.
 * Parameters of an enclosing function will be converted to the component props.
 *
 * For example, this Kotlin code:
 *
 * ```
 * fun greeting(name: String) = component {
 *     Div(text = "Hello, $name!")
 * }
 *
 * val element = greeting(name = "World")
 * ```
 *
 * will be converted to this JS code:
 *
 * ```
 * function greeting$lambda({name}) {
 *     return React.createElement("div", {}, `Hello, {name}!`);
 * }
 *
 * function greeting(name) {
 *     return React.createElement(greeting$lambda, {name});
 * }
 *
 * const element = greeting("World");
 * ```
 *
 * You can provide additional [wrapper] function what will be called with the component as a first argument.
 * The [wrapper] must be a lambda function without closure. It can use top-level static declarations only.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun component(noinline wrapper: ComponentWrapper<*> = { it }, noinline render: RenderFunction): ReactElement<*> {
    return __akryl_react_component_marker__(React, wrapper, render)
}

/**
 * Wraps the [component] function and adds memoization to a component.
 * An underlying [render] function will be called only when one of the props values are changed.
 * Useful when the component contains some expansive computations.
 * Works similar to [React.memo] but with additional syntax sugar.
 *
 * Example:
 * ```
 * fun markdown(md: String) = memo {
 *     val html = renderMarkdown(md)
 *     Div(innerHtml = html)
 * }
 * ```
 */
@Suppress("NOTHING_TO_INLINE", "UnsafeCastFromDynamic")
inline fun memo(noinline render: RenderFunction): ReactElement<*> {
    return component({ React.memo(it) }, render)
}

/**
 * Adds a [key] to [this] react element.
 * The key is useful inside lists of elements. It adds identity to a list item
 * and prevents unnecessary re-rendering.
 *
 * Example:
 * ```
 * Ul(children = items.map { item ->
 *   Li(text = item.value).withKey(item.id)
 * })
 * ```
 */
@Suppress("UNUSED_PARAMETER")
fun <P> ReactElement<P>.withKey(key: Any): ReactElement<P> {
    val element = this
    return React.cloneElement(element, js("Object.assign({}, element.props, {key: key})") as P)
}
