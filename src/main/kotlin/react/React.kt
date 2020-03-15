package react

@JsModule("react")
@JsNonModule
external object React {
    fun createElement(type: String, props: dynamic = definedExternally, vararg children: dynamic): ReactElement<dynamic>
    fun <P> createElement(type: Component<P>, props: P, vararg children: ReactElement<*>): ReactElement<P>
    fun <P> cloneElement(element: ReactElement<P>, props: P, vararg children: ReactElement<*>): ReactElement<P>
    fun memo(inner: dynamic): dynamic
    fun useState(initialState: dynamic): Array<dynamic>
    fun useEffect(effect: () -> EffectDisposer?, dependencies: Array<Any?>? = definedExternally)
    fun <R> useCallback(callback: () -> R, dependencies: Array<Any?>? = definedExternally): () -> R
    fun <T> useContext(context: Context<T>): T
    fun <R> useRef(initialValue: R): MutableRefObject<R>
    fun useDebugValue(value: Any?)
    fun <R> useMemo(fn: () -> R, dependencies: Array<Any?>? = definedExternally): R
    fun isValidElement(obj: dynamic): Boolean
    fun <T> createContext(defaultValue: T): Context<T>
}

external interface Component<P> {
    operator fun invoke(props: P): ReactElement<P>?
}

typealias ReactNode = Array<ReactElement<*>?>

class ProviderProps<T>(
    val value: T,
    val children: ReactNode?
)

typealias Provider<T> = Component<ProviderProps<T>>

class ConsumerProps<T>(
    val children: (value: T) -> ReactElement<*>
)

typealias Consumer<T> = Component<ConsumerProps<T>>

@Suppress("PropertyName")
external interface Context<T> {
    val Provider: Provider<T>
    val Consumer: Consumer<T>
    val displayName: String?
}

external interface ReactElement<P> {
    val type: dynamic
    val props: P
    val key: Any?
}

typealias FunctionalComponent<P> = (props: P) -> ReactElement<P>?

typealias EffectDisposer = () -> Unit

external interface MutableRefObject<R> {
    var current: R
}