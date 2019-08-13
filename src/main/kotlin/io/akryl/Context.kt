package io.akryl

import io.akryl.react.Context
import io.akryl.react.ProviderProps
import io.akryl.react.ReactNode
import io.akryl.react.createElement

fun <T> Context<T>.provide(value: T, child: ReactNode): ReactNode {
  return createElement(this.Provider, ProviderProps(value), build(child))
}
