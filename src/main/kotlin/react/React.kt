package react

@JsModule("react")
@JsNonModule
external object React {
    fun createElement(type: dynamic, props: dynamic, vararg children: dynamic): ReactNode
    fun memo(inner: dynamic): dynamic
    fun useState(initialState: dynamic): Array<dynamic>
}

external interface ReactNode

typealias FunctionalComponent<P> = (props: P) -> ReactNode
