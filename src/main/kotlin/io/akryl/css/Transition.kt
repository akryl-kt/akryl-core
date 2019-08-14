package io.akryl.css

class TransitionScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun initial() = this("initial")
  fun inherit() = this("inherit")

  operator fun invoke(
    property: StringScope,
    duration: Duration,
    timing: Timing = Timing.ease,
    delay: Duration = Duration.zero
  ): TransitionScope {
    val value = "${property.name} $duration $timing $delay"
    properties[name] = properties[name]?.let { "$it, $value" } ?: value
    return this
  }
}

class Timing(val value: String) {
  companion object {
    val ease = Timing("ease")
    val linear = Timing("linear")
    val easeIn	 = Timing("ease-in")
    val easeOut = Timing("ease-out")
    val easeInOut = Timing("ease-in-out")
    val stepStart = Timing("step-start")
    val stepEnd = Timing("stepEnd")
  }

  override fun toString() = value
}

class Duration(val value: String) {
  companion object {
    val zero = Duration("0s")
  }

  override fun toString() = value
}

val Number.s get() = Duration("${this}s")
val Number.ms get() = Duration("${this}ms")
