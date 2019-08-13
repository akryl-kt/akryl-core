@file:Suppress("unused")

package io.akryl.css

class FlexScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun auto() = this("auto")
  fun none() = this("none")
  fun initial() = this("initial")
  fun inherit() = this("inherit")

  operator fun invoke(
    grow: Number,
    shrink: Number,
    basis: Linear
  ) = this("$grow $shrink $basis")
}