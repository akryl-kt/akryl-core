package io.akryl.rx

interface Transient {
  @JsName(OBSERVABLE_MARKER)
  val observable get() = true
}