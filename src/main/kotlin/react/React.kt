package react

@JsModule("react")
@JsNonModule
external object React {
    fun createElement(type: dynamic, props: dynamic = definedExternally, vararg children: dynamic): ReactElement<dynamic>
    fun memo(inner: dynamic): dynamic
    fun useState(initialState: dynamic): Array<dynamic>
    fun useEffect(effect: () -> EffectDisposer?, dependencies: Array<Any?>?)
    fun isValidElement(obj: dynamic): Boolean
}

external interface ReactElement<P> {
    val type: dynamic
    val props: P
    val key: Any?
}

typealias FunctionalComponent<P> = (props: P) -> ReactElement<P>?

typealias EffectDisposer = () -> Unit