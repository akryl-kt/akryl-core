@file:Suppress("unused")

package io.akryl.css

class IntScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun auto() = this("auto")
  fun initial() = this("initial")
  fun inherit() = this("inherit")

  operator fun invoke(value: Int) = this(value.toString())
}

class NumberScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun auto() = this("auto")
  fun initial() = this("initial")
  fun inherit() = this("inherit")

  operator fun invoke(value: Number) = this(value.toString())
}