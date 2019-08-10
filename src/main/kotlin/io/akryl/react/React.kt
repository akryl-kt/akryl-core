@file:JsModule("react")

package io.akryl.react

external fun createElement(
  type: FunctionalComponent,
  props: dynamic = definedExternally,
  vararg children: ReactNode = definedExternally
): ReactNode

external fun createElement(
  type: String,
  props: dynamic = definedExternally,
  vararg children: ReactNode = definedExternally
): ReactNode

external fun useState(initialValue: dynamic): Array<dynamic>

external interface Ref<T> {
  var current: T
}

external fun <T> useRef(initialValue: T): Ref<T>

external fun useEffect(callback: () -> EffectDisposer?, dependencies: Array<out Any?> = definedExternally)

external fun memo(
  component: FunctionalComponent,
  arePropsEquals: (dynamic, dynamic) -> Boolean = definedExternally
): FunctionalComponent