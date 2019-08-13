@file:Suppress("unused")

package io.akryl.css

class LineHeightScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun normal() = this("normal")
  fun initial() = this("initial")
  fun inherit() = this("inherit")

  operator fun invoke(value: Linear) = this(value.toString())
  operator fun invoke(value: Number) = this(value.toString())
}
