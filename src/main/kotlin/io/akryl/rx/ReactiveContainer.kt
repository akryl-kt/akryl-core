package io.akryl.rx

interface ReactiveContainer {
  fun registerReactiveHandle(handle: ReactiveHandle)
}