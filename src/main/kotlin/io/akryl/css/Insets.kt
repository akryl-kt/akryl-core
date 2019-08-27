@file:Suppress("unused")

package io.akryl.css

class InsetsScope(properties: MutableMap<String, Any?>, name: String) : StringScope(properties, name) {
  fun none() = this("0")
  fun auto() = this("auto")
  fun initial() = this("initial")
  fun inherit() = this("inherit")

  operator fun invoke(all: Linear) = this(all.toString())
  operator fun invoke(vertical: Linear, horizontal: Linear) = this("$vertical $horizontal")
  operator fun invoke(top: Linear, right: Linear, bottom: Linear, left: Linear) = this("$top $right $bottom $left")
}
