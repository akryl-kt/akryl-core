package io.akryl.rx

interface EmptyReactiveContainer : ReactiveContainer, Transient {
  override fun registerReactiveHandle(handle: ReactiveHandle) {}
}